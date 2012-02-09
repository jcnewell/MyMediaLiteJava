// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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

package org.mymedialite.itemrec;

/**
 * Weighted k-nearest neighbor user-based collaborative filtering using cosine-similarity.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class WeightedUserKNN extends UserKNN {

  /**
   * 
   */
  public double predict(int user_id, int item_id) {

    if ((user_id < 0) || (user_id > maxUserID))
      return 0;

    if ((item_id < 0) || (item_id > maxItemID))
      return 0;

    if (k == Integer.MAX_VALUE) {
      return correlation.sumUp(user_id, feedback.itemMatrix().get(item_id));
    } else {
      double result = 0;
      for (int neighbor : nearest_neighbors[user_id])
        if (feedback.userMatrix().get(neighbor, item_id))
          result += correlation.get(user_id, neighbor);
      return result;
    }
  }

  /**
   * 
   */
  public String toString() {
    return "WeightedUserKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }

}
