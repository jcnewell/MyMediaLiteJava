//Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

package org.mymedialite.ratingprediction;

/**
 * Uses the average rating value of an item for prediction.
 *
 * This engine does NOT support online updates.
 * @version 2.03
 */
public class ItemAverage extends EntityAverage {

  @Override
  public void train() {
    super.train(ratings.items(), ratings.maxItemID());
  }

  @Override
  public boolean canPredict(int user_id, int item_id) {
    return (item_id <= ratings.maxItemID());
  }

  @Override
  public double predict(int user_id, int item_id) {
    if (item_id <= ratings.maxItemID()) {
      return entity_averages.get(item_id);
    } else {
      return global_average;
    }
  }

  @Override
  public void addRating(int user_id, int item_id, double rating) {
    super.addRating(user_id, item_id, rating);
    retrain(item_id, ratings.byItem().get(item_id), ratings.items());
  }

  @Override
  public void updateRating(int user_id, int item_id, double rating) {
    super.updateRating(user_id, item_id, rating);
    retrain(item_id, ratings.byItem().get(item_id), ratings.items());
  }

  @Override
  public void removeRating(int user_id, int item_id) {
    super.removeRating(user_id, item_id);
    retrain(item_id, ratings.byItem().get(item_id), ratings.items());
  }

  @Override
  public void addItem(int item_id) {
    while (entity_averages.size() < item_id + 1)
      entity_averages.add(0);
  }

  @Override
  public void removeItem(int item_id) {
    entity_averages.set(item_id, global_average);
  }

  @Override
  public String toString() {
    return "ItemAverage";
  }

}