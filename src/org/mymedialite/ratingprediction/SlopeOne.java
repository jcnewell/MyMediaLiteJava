// Copyright (C) 2011 Zeno Gantner, Chris Newell
//
// This file is part of MyMediaLite.
//
// MyMediaLite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// MyMediaLite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.ratingprediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.mymedialite.datatype.SkewSymmetricSparseMatrix;
import org.mymedialite.datatype.SymmetricSparseMatrix;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;

/**
 * Frequency-weighted Slope-One rating prediction.
 * 
 * Daniel Lemire, Anna Maclachlan:
 * Slope One Predictors for Online Rating-Based Collaborative Filtering.
 * SIAM Data Mining (SDM 2005).
 * http://www.daniel-lemire.com/fr/abstracts/SDM2005.html
 *
 * This recommender does NOT support incremental updates. They would be easy to implement, though.
 * @version 2.03
 */
public class SlopeOne extends RatingPredictor {

  private static final String VERSION = "2.03";
  
  private SkewSymmetricSparseMatrix diff_matrix;
  private SymmetricSparseMatrix<Integer> freq_matrix;
  private double global_average;

  /**
   * 
   */
  public boolean canPredict(int user_id, int item_id) {
    if (user_id > maxUserID || item_id > maxItemID)
      return false;

    for (int index : ratings.byUser().get(user_id))
      if (freq_matrix.get(item_id, ratings.items().get(index)) != 0)
        return true;
    return false;
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id) {
    if (item_id > maxItemID || user_id > maxUserID)
      return global_average;

    double prediction = 0.0;
    int frequency = 0;

    for (int index : ratings.byUser().get(user_id)) {
      int other_item_id = ratings.items().get(index);
      int f = freq_matrix.get(item_id, other_item_id);
      if (f != 0) {
        prediction += ( diff_matrix.get(item_id, other_item_id) + ratings.get(index) ) * f;
        frequency += f;
      }
    }

    if (frequency == 0)
      return global_average;

    return prediction / frequency;
  }

  public void initModel() {
    diff_matrix = new SkewSymmetricSparseMatrix(maxItemID + 1);
    freq_matrix = new SymmetricSparseMatrix<Integer>(maxItemID + 1, 0);
  }

  /**
   * 
   */
  public void train() {
    initModel();

    // Default value if no prediction can be made
    global_average = ratings.average();

    // Compute difference sums and frequencies
    for (List<Integer> by_user_indices : ratings.byUser()) {
      for (int i = 0; i < by_user_indices.size(); i++) {
        int index1 = by_user_indices.get(i);

        for (int j = i + 1; j < by_user_indices.size(); j++) {
          int index2 = by_user_indices.get(j);

          freq_matrix.set(ratings.items().get(index1), ratings.items().get(index2), freq_matrix.get(ratings.items().get(index1), ratings.items().get(index2)) + 1);
          diff_matrix.set(ratings.items().get(index1), ratings.items().get(index2), diff_matrix.get(ratings.items().get(index1), ratings.items().get(index2)) + (float) (ratings.get(index1) - ratings.get(index2)));
        }
      }
    }

    // Compute average differences
    for (int i = 0; i <= maxItemID; i++)
      for (int j : freq_matrix.get(i).keySet())
        diff_matrix.set(i, j, diff_matrix.get(i, j) / freq_matrix.get(i, j));
  }

  /**
   */
  public void loadModel(String filename) throws IOException {
    initModel();
    BufferedReader reader = Model.getReader(filename, this.getClass());
    double global_average = Double.parseDouble(reader.readLine());

    SkewSymmetricSparseMatrix diff_matrix = (SkewSymmetricSparseMatrix) IMatrixExtensions.readFloatMatrix(reader, this.diff_matrix);
    SymmetricSparseMatrix<Integer> freq_matrix = (SymmetricSparseMatrix<Integer>) IMatrixExtensions.readIntegerMatrix(reader, this.freq_matrix);

    // assign new model
    this.global_average = global_average;
    this.diff_matrix = diff_matrix;
    this.freq_matrix = freq_matrix;

  }

  /**
   */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(Double.toString(global_average));
    IMatrixExtensions.writeSparseMatrix(writer, diff_matrix);
    IMatrixExtensions.writeSparseMatrix(writer, freq_matrix);
  }

}
