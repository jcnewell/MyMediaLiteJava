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

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data structure for storing ratings with time information.
 * 
 * Small memory overhead for added flexibility.
 *
 * This data structure supports incremental updates.
 *
 * Loading the Netflix Prize data set (100,000,000 ratings) into this data structure requires about 3.2 GB of memory.
 * @version 2.03 
 */
public class TimedRatings extends Ratings implements ITimedRatings {

  protected List<Date> times;
  protected Date earliestTime;
  protected Date latestTime;
  
  /**
   * 
   */
  public List<Date> times() {
    return times;
  }

  /**
   * 
   */
  public Date earliestTime() {
    return earliestTime;
  }

  /**
   * 
   */
  public Date latestTime() {
    return latestTime;
  }

  /**
   * Default constructor.
   */
  public TimedRatings() {
    times = new ArrayList<Date>();
    earliestTime = new Date();
    earliestTime.setTime(Long.MAX_VALUE);
    latestTime = new Date();
    latestTime.setTime(0L);
  }

  /**
   */
  public void add(int user_id, int item_id, double rating) {
    throw new UnsupportedOperationException();
  }

  /**
   */
  public void add(int user_id, int item_id, double rating, Date time) {
    users.add(user_id);
    items.add(item_id);
    values.add(rating);
    times.add(time);

    int pos = users.size() - 1;

    if (user_id > maxUserID)
      maxUserID = user_id;
    if (item_id > maxItemID)
      maxItemID = item_id;
    if (rating < minRating)
      minRating = rating;
    if (rating > maxRating)
      maxRating = rating;
    if (time.before(earliestTime))
      earliestTime = time;
    if (time.after(latestTime))
      latestTime = time;

    // Update index data structures if necessary
    if (byUser != null) {
      for (int u = byUser.size(); u <= user_id; u++)
        byUser.add(new ArrayList<Integer>());
      byUser.get(user_id).add(pos);
    }
    
    if (byItem != null) {
      for (int i = byItem.size(); i <= item_id; i++)
        byItem.add(new ArrayList<Integer>());
      byItem.get(item_id).add(pos);
    }
    
    //if (by_time != null)
  }

  @Override
  public int compare(Integer index1, Integer index2) {
    return times.get(index1).compareTo(times.get(index2));
  }
  
}

