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

/**
 * Weighted k-nearest neighbor item-based collaborative filtering using cosine-similarity 
 * over the item attibutes.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class WeightedItemAttributeKNN extends ItemAttributeKNN {
 
  /**
   * 
   */
  @Override
  public double predict(int user_id, int item_id) {
    if ((user_id < 0) || (user_id > maxUserID))
      return 0;
    if ((item_id < 0) || (item_id > maxItemID))
      return 0;

    if (k == Integer.MAX_VALUE) {
      return correlation.sumUp(item_id, feedback.userMatrix().get(user_id));
    } else {
      double result = 0;
      for (int neighbor : nearest_neighbors[item_id])
        if (feedback.itemMatrix().get(neighbor, user_id))
          result += correlation.get(item_id, neighbor);
      return result;
    }
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return "WeightedItemAttributeKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }

}

