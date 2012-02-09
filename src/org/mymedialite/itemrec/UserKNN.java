// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newelll
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

import java.util.List;

import org.mymedialite.IUserSimilarityProvider;
import org.mymedialite.correlation.BinaryCosine;

/**
 * k-nearest neighbor user-based collaborative filtering using cosine-similarity (unweighted).
 * 
 * k=inf equals most-popular.
 *
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class UserKNN extends KNN implements IUserSimilarityProvider {

  /**
   * 
   */
  public void train() {
    this.correlation = BinaryCosine.create(feedback.userMatrix());

    int num_users = maxUserID + 1;
    this.nearest_neighbors = new int[num_users][];
    for (int u = 0; u < num_users; u++)
      nearest_neighbors[u] = correlation.getNearestNeighbors(u, k);
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
    for (int neighbor : nearest_neighbors[user_id]) {
      if (feedback.userMatrix().get(neighbor, item_id))
        count++;
    }
    return (double) count / k;
  }

  /**
   * 
   */
  public float getUserSimilarity(int user_id1, int user_id2) {
    return correlation.get(user_id1, user_id2);
  }

  /**
   * 
   */
  public int[] getMostSimilarUsers(int user_id, int n) {
    if (n == k) {
      return nearest_neighbors[user_id];
    } else if (n < k) {
      int[] mostSimilarItems = new int[n];
      System.arraycopy(nearest_neighbors, 0, mostSimilarItems, 0, n);
      return mostSimilarItems;
    } else {
      return correlation.getNearestNeighbors(user_id, n);
    }
  }

  /**
   * 
   */
  public String toString() {
    return "UserKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }
}
