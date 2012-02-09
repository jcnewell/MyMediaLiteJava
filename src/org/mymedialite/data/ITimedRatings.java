// Copyright (C) 2011 Zeno Gantner
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

import java.util.Date;

/**
 * Interface for rating datasets with time information.
 * @version 2.03
 */
public interface ITimedRatings extends IRatings, ITimedDataSet {

  /**
   * Add a rating event including time information.
   * 
   * It is up to the user of a class implementing this interface to decide whether the DateTime
   * object represent local time, UTC, or any other time.
   * 
   * @param user_id the user ID
   * @param item_id the item ID
   * @param rating the rating value
   * @param time a {@link java.util.Date} specifying the time of the rating event
   */
  void add(int user_id, int item_id, double rating, Date time);

}
