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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.data;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mymedialite.datatype.ListProxy;

/**
 * Data structure that allows access to selected entries of a timed rating data structure.
 * @version 2.03
 */
public class TimedRatingsProxy extends TimedRatings {

  /**
   * Create a TimedRatingsProxy object.
   * @param ratings a ratings data structure
   * @param indices an index list pointing to entries in the ratings
   */
  public TimedRatingsProxy(ITimedRatings ratings, IntList indices) {
    
    users  = new ListProxy<Integer>(ratings.users(), indices);
    items  = new ListProxy<Integer>(ratings.items(), indices);
    values = new ListProxy<Double>(ratings, indices);
    times  = new ListProxy<Date>(ratings.times(), indices);

    maxUserID = ratings.maxUserID();
    maxItemID = ratings.maxItemID();
    maxRating = ratings.maxRating();
    minRating = ratings.minRating();

    Date maxTime = new Date();
    maxTime.setTime(Long.MAX_VALUE);
    Date minTime = new Date();
    maxTime.setTime(0L);
    
    // TODO check this
    earliestTime = size() > 0 ? Collections.min(times) : maxTime;
    latestTime   = size() > 0 ? Collections.max(times) : minTime;

  }

}

