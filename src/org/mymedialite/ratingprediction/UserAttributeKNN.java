// Copyright (C) 2010, 2011, 2012 Zeno Gantner, Chris Newell
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

import org.mymedialite.IUserAttributeAwareRecommender;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Weighted kNN recommender based on user attributes.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class UserAttributeKNN extends UserKNN implements IUserAttributeAwareRecommender {

  private SparseBooleanMatrix userAttributes;
  private int numUserAttributes;
  
  public SparseBooleanMatrix getUserAttributes() { 
    return userAttributes;
  }
  
  public void setUserAttributes(SparseBooleanMatrix matrix) {
      this.userAttributes = matrix;
      this.numUserAttributes = userAttributes.numberOfColumns();
      this.maxUserID = Math.max(maxUserID, userAttributes.numberOfRows() - 1);
    }

  /**
   */
  public int numUserAttributes() {
    return numUserAttributes;  
  }
  
  /**
   */
  protected void retrainUser(int user_id) {
    baseline_predictor.retrainUser(user_id);
  }

  /**
   */
  public void train() {
    baseline_predictor.train();
    this.correlation = BinaryCosine.create(userAttributes);
  }

  public String toString() {
    return "UserAttributeKNN k=" + (k == Integer.MAX_VALUE ? "inf" : k) + " reg_u=" + getRegU() + " reg_i=" + getRegI();
  }
  
}



