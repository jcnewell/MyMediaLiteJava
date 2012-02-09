// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

import org.mymedialite.IUserAttributeAwareRecommender;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * k-nearest neighbor user-based collaborative filtering using cosine-similarity over the user attibutes.
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class UserAttributeKNN extends UserKNN implements IUserAttributeAwareRecommender {

  private SparseBooleanMatrix userAttributes;
  
  /**
   * 
   */
  @Override
  public SparseBooleanMatrix getUserAttributes() {
    return userAttributes;
  }

  /**
   * 
   */
  @Override
  public void setUserAttributes(SparseBooleanMatrix userAttributes) {
    this.userAttributes = userAttributes;
    this.maxUserID = Math.max(maxUserID, userAttributes.numberOfRows() - 1);
  }
  
  /**
   * 
   */
  @Override
  public int numUserAttributes() {
    return userAttributes.numberOfColumns();
  }
  
  /**
   */
  public void train() {
    correlation = BinaryCosine.create(userAttributes);

    int num_users = userAttributes.numberOfRows();
    this.nearest_neighbors = new int[num_users][];
    for (int u = 0; u < num_users; u++)
      nearest_neighbors[u] = correlation.getNearestNeighbors(u, k);
  }

  /**
   */
  public String toString() {
    return "UserAttributeKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }
  
}
