// Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

/**
 * Uses the average rating value of a user for predictions.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class UserAverage extends EntityAverage {

  /**
   * 
   */
  public void train() {
    super.train(ratings.users(), ratings.maxUserID());
  }

  /**
   * 
   */
  public boolean canPredict(int user_id, int item_id) {
    return (user_id <= ratings.maxUserID());
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id) {
    if (user_id < entity_averages.size())
      return entity_averages.get(user_id);
    else
      return global_average;
  }

  /**
   * 
   */
  public void addRating(int user_id, int item_id, double rating) {
    super.addRating(user_id, item_id, rating);
    retrain(user_id, ratings.byUser().get(user_id), ratings.users());
  }

  /**
   * 
   */
  public void updateRating(int user_id, int item_id, double rating) {
    super.updateRating(user_id, item_id, rating);
    retrain(user_id, ratings.byUser().get(user_id), ratings.users());
  }

  /**
   * 
   */
  public void removeRating(int user_id, int item_id) {
    super.removeRating(user_id, item_id);
    retrain(user_id, ratings.byUser().get(user_id), ratings.users());
  }

  /**
   * 
   */
  public void addUser(int user_id) {
    while (entity_averages.size() < user_id + 1)
      entity_averages.add(0.0D);
  }

  /**
   * 
   */
  public void removeUser(int user_id) {
    entity_averages.set(user_id, global_average);
  }
  
}
