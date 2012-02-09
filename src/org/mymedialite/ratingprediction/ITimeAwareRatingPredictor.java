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

import java.util.Date;

import org.mymedialite.data.ITimedRatings;

/**
 * Interface for time-aware rating predictors.
 * 
 * Time-aware rating predictors use the information contained in the dates/times
 * of the ratings to build more accurate models.
 *
 * They may or may not use time information at prediction (as opposed to training) time.
 * @version 2.03
 */
public interface ITimeAwareRatingPredictor extends IRatingPredictor {

  /**
   * Get the training data that also contains the time information.
   */
  ITimedRatings getTimedRatings();

  /**
   * Set the training data that also contains the time information.
   */
  void setTimedRatings(ITimedRatings timedRatings);
  
  /**
   * predict rating at a certain point in time.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param time the time of the rating event
   * @return the prediction value
   */
  double predict(int user_id, int item_id, Date time);

}
