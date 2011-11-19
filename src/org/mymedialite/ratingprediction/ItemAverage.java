package org.mymedialite.ratingprediction;

//Copyright (C) 2010 Zeno Gantner, Steffen Rendle
//Copyright (C) 2011 Zeno Gantner
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

/**
 * Uses the average rating value of an item for prediction.
 *
 * This engine does NOT support online updates.
 */
public class ItemAverage extends EntityAverage {

  /// <inheritdoc/>
  @Override
  public void train() {
    super.train(ratings.getItems(), ratings.getMaxItemID());
  }

  /// <inheritdoc/>
  @Override
  public boolean canPredict(int user_id, int item_id) {
    return (item_id <= ratings.getMaxItemID());
  }

  /// <inheritdoc/>
  @Override
  public double predict(int user_id, int item_id) {
    if (item_id <= ratings.getMaxItemID()) {
      return entity_averages.get(item_id);
    } else {
      return global_average;
    }
  }

  /// <inheritdoc/>
  @Override
  public String toString() {
    return "ItemAverage";
  }
}