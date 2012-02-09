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

import java.util.HashSet;

import org.mymedialite.correlation.BinaryCosine;

/**
 * Weighted user-based kNN with cosine similarity.
 * 
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class UserKNNCosine extends UserKNN {

  /**
   * 
   */
  public void train() {
    baseline_predictor.train();
    this.correlation = BinaryCosine.create(data_user);
  }

  /**
   */
  protected void retrainUser(int user_id) {
    baseline_predictor.retrainUser(user_id);
    if (updateUsers)
      for (int i = 0; i <= maxUserID; i++)
        correlation.set(user_id, i, BinaryCosine.computeCorrelation(new HashSet<Integer>(data_user.get(user_id)), new HashSet<Integer>(data_user.get(i))));
  }

  public String toString() {
    return "UserKNNCosine k=" + (k == Integer.MAX_VALUE ? "inf" : k) + " reg_u=" + getRegU() + " reg_i=" + getRegI();
  }

}
