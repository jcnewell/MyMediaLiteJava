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

package org.mymedialite.ratingprediction;

import org.mymedialite.correlation.Pearson;
import org.mymedialite.taxonomy.EntityType;

/**
 * Weighted user-based kNN with Pearson correlation.
 * 
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class UserKNNPearson extends UserKNN {

  /**
   * Shrinkage (regularization) parameter.
   */
  public float shrinkage = 10; 

  /**
   * 
   */
  public void train() {
    baseline_predictor.train();
    this.correlation = Pearson.create(ratings, EntityType.USER, shrinkage);
  }

  /**
   */
  protected void retrainUser(int user_id) {
    baseline_predictor.retrainUser(user_id);
    if (updateUsers)
      for (int i = 0; i <= maxUserID; i++)
        correlation.set(user_id, i, Pearson.computeCorrelation(ratings, EntityType.USER, user_id, i, shrinkage));
  }

  public String toString() {
    return "UserKNNPearson k=" + (k == Integer.MAX_VALUE ? "inf" : k) + " reg_u=" + getRegU() + " reg_i=" + getRegI();
  }

}
