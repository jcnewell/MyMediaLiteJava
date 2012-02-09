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

import org.mymedialite.datatype.CombinedList;

/**
 * Combine two IRatings objects.
 * @version 2.03
 */
public class CombinedRatings extends Ratings {

  /**
   * Create a CombinedRatings object from two existing IRatings objects.
   * @param ratings1 the first data set
   * @param ratings2 the second data set
   */
  public CombinedRatings(IRatings ratings1, IRatings ratings2) {

    users = new CombinedList<Integer>(ratings1.users(), ratings2.users());
    items = new CombinedList<Integer>(ratings1.items(), ratings2.items());
    values = new CombinedList<Double>(ratings1, ratings2);

    maxUserID = Math.max(ratings1.maxUserID(), ratings2.maxUserID());
    maxItemID = Math.max(ratings1.maxItemID(), ratings2.maxItemID());
    
  }
}

