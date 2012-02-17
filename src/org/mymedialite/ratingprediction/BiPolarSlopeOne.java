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
import java.util.ArrayList;
import java.util.List;

import org.mymedialite.datatype.SkewSymmetricSparseMatrix;
import org.mymedialite.datatype.SymmetricSparseMatrix;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;
import org.mymedialite.io.VectorExtensions;

/**
 * Bi-polar frequency-weighted Slope-One rating prediction.
 * 
 * Literature:
 *     Daniel Lemire, Anna Maclachlan:
 *     Slope One Predictors for Online Rating-Based Collaborative Filtering.
 *     SIAM Data Mining (SDM 2005).
 *     http://www.daniel-lemire.com/fr/abstracts/SDM2005.html
 *
 * This recommender does NOT support incremental updates. They would be easy to implement, though.
 * @version 2.03
 */
public class BiPolarSlopeOne extends RatingPredictor {
  
  private static final String VERSION = "2.03";
  
  private SkewSymmetricSparseMatrix  diff_matrix_like;
  private SymmetricSparseMatrix<Integer> freq_matrix_like;
  private SkewSymmetricSparseMatrix  diff_matrix_dislike;
  private SymmetricSparseMatrix<Integer> freq_matrix_dislike;

  private double global_average;
  private List<Double> user_average;

  /**
   * 
   */
  public boolean canPredict(int user_id, int item_id) {
    if (user_id > maxUserID || item_id > maxItemID)
      return false;

    for (int index : ratings.byUser().get(user_id)) {
      if (freq_matrix_like.get(item_id, ratings.items().get(index)) != 0)
        return true;
      if (freq_matrix_dislike.get(item_id, ratings.items().get(index)) != 0)
        return true;
    }
    return false;
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id) {
    if (item_id > maxItemID || user_id > maxUserID)
      return global_average;

    double prediction = 0.0;
    int frequencies = 0;

    for (int index : ratings.byUser().get(user_id)) {
      if (ratings.get(index) > user_average.get(user_id)) {
        int f = freq_matrix_like.get(item_id, ratings.items().get(index));
        if (f != 0) {
          prediction  += ( diff_matrix_like.get(item_id, ratings.items().get(index)) + ratings.get(index) ) * f;
          frequencies += f;
        }
      } else {
        int f = freq_matrix_dislike.get(item_id, ratings.items().get(index));
        if (f != 0) {
          prediction  += ( diff_matrix_dislike.get(item_id, ratings.items().get(index)) + ratings.get(index) ) * f;
          frequencies += f;
        }
      }
    }

    if (frequencies == 0)
      return global_average;

    double result = prediction / frequencies;

    if (result > maxRating)
      return maxRating;
    if (result < minRating)
      return minRating;
    return result;
  }

  /**
   */
  public void train() {
    initModel();

    // Default value if no prediction can be made
    global_average = ratings.average();

    // Compute difference sums and frequencies
    for (int user_id : ratings.allUsers()) {
      double user_avg = 0;
      for (int index : ratings.byUser().get(user_id))
        user_avg += ratings.get(index);
 
      user_avg = user_avg / ratings.byUser().get(user_id).size();

      // Store for later use
      user_average.set(user_id, user_avg);

      for (int index : ratings.byUser().get(user_id)) {
        for (int index2 : ratings.byUser().get(user_id)) {
          if (ratings.get(index) > user_avg && ratings.get(index2) > user_avg) {
            freq_matrix_like.set(ratings.items().get(index), ratings.items().get(index2), freq_matrix_like.get(ratings.items().get(index), ratings.items().get(index2)) + 1);
            diff_matrix_like.set(ratings.items().get(index), ratings.items().get(index2), diff_matrix_like.get(ratings.items().get(index), ratings.items().get(index2)) + (float) (ratings.get(index) - ratings.get(index2)));
          } else if (ratings.get(index) < user_avg && ratings.get(index2) < user_avg) {
            
            freq_matrix_dislike.set(ratings.items().get(index), ratings.items().get(index2), freq_matrix_dislike.get(ratings.items().get(index), ratings.items().get(index2)) + 1);
            diff_matrix_dislike.set(ratings.items().get(index), ratings.items().get(index2), diff_matrix_dislike.get(ratings.items().get(index), ratings.items().get(index2)) + (float) (ratings.get(index) - ratings.get(index2)));
          }
        }
      }
    }

    // Compute average differences
    for (int i = 0; i <= maxItemID; i++) {
      for (int j : freq_matrix_like.get(i).keySet())
        diff_matrix_like.set(i, j, diff_matrix_like.get(i, j) / freq_matrix_like.get(i, j));
      
      for (int j : freq_matrix_dislike.get(i).keySet())
        diff_matrix_dislike.set(i, j, diff_matrix_dislike.get(i, j) / freq_matrix_dislike.get(i, j));
    }
  }

  /**
   * 
   */
  public void initModel() {
    // Create data structure
    diff_matrix_like = new SkewSymmetricSparseMatrix(maxItemID + 1);
    freq_matrix_like = new SymmetricSparseMatrix<Integer>(maxItemID + 1, 0);
    diff_matrix_dislike = new SkewSymmetricSparseMatrix(maxItemID + 1);
    freq_matrix_dislike = new SymmetricSparseMatrix<Integer>(maxItemID + 1, 0);
    user_average = new ArrayList<Double>(maxUserID + 1);
    for(int i=0; i < maxUserID + 1; i++) user_average.add(null);
  }

  /**
   * 
   */
  public void loadModel(String filename) throws IOException {
    initModel();

    BufferedReader reader = Model.getReader(filename, this.getClass());
    double global_average = Double.parseDouble(reader.readLine());

    SkewSymmetricSparseMatrix diff_matrix_like = (SkewSymmetricSparseMatrix) IMatrixExtensions.readFloatMatrix(reader, this.diff_matrix_like);
    SymmetricSparseMatrix<Integer> freq_matrix_like = (SymmetricSparseMatrix<Integer>) IMatrixExtensions.readIntegerMatrix(reader, this.freq_matrix_like);
    SkewSymmetricSparseMatrix diff_matrix_dislike = (SkewSymmetricSparseMatrix) IMatrixExtensions.readFloatMatrix(reader, this.diff_matrix_dislike);
    SymmetricSparseMatrix<Integer> freq_matrix_dislike = (SymmetricSparseMatrix<Integer>) IMatrixExtensions.readIntegerMatrix(reader, this.freq_matrix_dislike);
    List<Double> user_average = VectorExtensions.readVector(reader);
    reader.close();
    
    // Assign new model
    this.global_average = global_average;
    this.diff_matrix_like = diff_matrix_like;
    this.freq_matrix_like = freq_matrix_like;
    this.diff_matrix_dislike = diff_matrix_dislike;
    this.freq_matrix_dislike = freq_matrix_dislike;
    this.user_average = user_average;

  }

  /**
   * 
   */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(Double.toString(global_average));
    IMatrixExtensions.writeSparseMatrix(writer, diff_matrix_like);
    IMatrixExtensions.writeSparseMatrix(writer, freq_matrix_like);
    IMatrixExtensions.writeSparseMatrix(writer, diff_matrix_dislike);
    IMatrixExtensions.writeSparseMatrix(writer, freq_matrix_dislike);
    VectorExtensions.writeVector(writer, user_average);
    writer.flush();
    writer.close();
  }
  
}
