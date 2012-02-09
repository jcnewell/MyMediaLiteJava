//Copyright (C) 2010 Steffen Rendle, Zeno Gantner
//Copyright (C) 2011 Zeno Gantner, Chris Newell
//
//This file is part of MyMediaLite.
//
//MyMediaLite is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//MyMediaLite is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.itemrec;

import java.util.List;

import org.mymedialite.IItemSimilarityProvider;
import org.mymedialite.correlation.BinaryCosine;

/**
 * Unweighted k-nearest neighbor item-based collaborative filtering using cosine similarity.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class ItemKNN extends KNN implements IItemSimilarityProvider {

  /**
   * 
   */
  public void train() {
    
    correlation = BinaryCosine.create(feedback.itemMatrix());

    int num_items = maxItemID + 1;
    this.nearest_neighbors = new int[num_items][];
    for (int i = 0; i < num_items; i++)
      nearest_neighbors[i] = correlation.getNearestNeighbors(i, k);
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id) {
    if ((user_id < 0) || (user_id > maxUserID))
      return 0;
    if ((item_id < 0) || (item_id > maxItemID))
      return 0;

    int count = 0;
    for (int neighbor : nearest_neighbors[item_id])
      if (feedback.userMatrix().get(neighbor, user_id))
        count++;

    return (double) count / k;
  }

  /**
   * 
   */
  public float getItemSimilarity(int item_id1, int item_id2) {
    return correlation.get(item_id1, item_id2);
  }

  /**
   * 
   */
  public int[] getMostSimilarItems(int item_id, int n) {
    if (n == k)
      return nearest_neighbors[item_id];
    else if (n < k) {
      int[] mostSimilarItems = new int[n];
      System.arraycopy(nearest_neighbors, 0, mostSimilarItems, 0, n);
      return mostSimilarItems;
    } else {
      return correlation.getNearestNeighbors(item_id, n);
    }
  }

  /**
   * 
   */
  public String toString() {
    return "ItemKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }

}

