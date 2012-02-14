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
//

package org.mymedialite.ratingprediction;

import java.util.Date;

import org.mymedialite.data.IRatings;
import org.mymedialite.data.ITimedRatings;

/**
 * Abstract class for time-aware rating predictors.
 * IllegalArgumentException is thrown when an argument passed to a method is invalid.
 * @version 2.03
 */
public abstract class TimeAwareRatingPredictor extends RatingPredictor implements ITimeAwareRatingPredictor {

  /**
   * rating data, including time information.
   */
  protected ITimedRatings timed_ratings;

  protected IRatings ratings;

  /**
   * the rating data, including time information.
   */
  public ITimedRatings getTimedRatings() {
    return timed_ratings;
  }

  public void setTimedRatings(ITimedRatings timedRatings) {
    this.setRatings(timedRatings);
    this.timed_ratings = timedRatings;
  }

  /**
   * 
   */
  public IRatings getRatings() {
    return ratings;
  }

  public void setRatings(IRatings ratings) {
    if (!(ratings instanceof ITimedRatings))
      throw new IllegalArgumentException("Ratings must be of type ITimedRatings.");

    super.setRatings(ratings);
    timed_ratings = (ITimedRatings) ratings;
  }

  /**
   * 
   */
  public abstract double predict(int user_id, int item_id, Date time);

}


