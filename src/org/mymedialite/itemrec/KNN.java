// Copyright (C) 2010, 2011 Zeno Gantner
// Copyright (C) 2011 Chris Newell
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

package org.mymedialite.itemrec;

import java.io.*;
import java.util.List;

import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.WeightedItem;
import org.mymedialite.io.Model;
import org.mymedialite.itemrec.ItemRecommender;

/**
 * Base class for item recommenders that use some kind of kNN model.
 * @version 2.03
 */
public abstract class KNN extends IncrementalItemRecommender {

  private static final String VERSION = "2.03";

  /**
   * The number of neighbors to take into account for prediction.
   */
  public int k = 80;

  /**
   * Pre-computed nearest neighbors.
   */
  protected int[][] nearest_neighbors;

  /**
   * Correlation matrix over some kind of entity.
   */
  protected CorrelationMatrix correlation;

  /** { @inheritDoc } */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }

  /** { @inheritDoc } */
  public void saveModel(PrintWriter writer) {
    writer.println(nearest_neighbors.length);
    for (int[] nn : nearest_neighbors) {
      writer.write(Integer.toString(nn[0]));
      for (int i = 1; i < nn.length; i++)
        writer.print(" " + Integer.toString(nn[i]));

      writer.println();
    }
    correlation.write(writer);
    writer.flush();
    writer.close();
  }

  /** { @inheritDoc } */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }

  /** { @inheritDoc } */
  public void loadModel(BufferedReader reader) throws IOException {
    int num_users = Integer.parseInt(reader.readLine());
    int[][] nearest_neighbors = new int[num_users][];
    for (int u = 0; u < nearest_neighbors.length; u++) {
      String[] numbers = reader.readLine().split(" ");
      nearest_neighbors[u] = new int[numbers.length];
      for (int i = 0; i < numbers.length; i++) {
        nearest_neighbors[u][i] = Integer.parseInt(numbers[i]);
      }
    }
    this.correlation = CorrelationMatrix.readCorrelationMatrix(reader);
    reader.close();
    this.k = nearest_neighbors[0].length;
    this.nearest_neighbors = nearest_neighbors;
  }

  @Override
  public void addItem(int item_id) {
    if (item_id > maxItemID)
     throw new UnsupportedOperationException("New items are not supported");
  }
  
  @Override
  public void removeItem(int item_id) {
    throw new UnsupportedOperationException();
  }
  
}
