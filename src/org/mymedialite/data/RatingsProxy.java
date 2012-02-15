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

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;

import org.mymedialite.datatype.DoubleListProxy;
import org.mymedialite.datatype.IntListProxy;
import org.mymedialite.datatype.ListProxy;

/**
 * Data structure that allows access to selected entries of a rating data structure.
 */
public class RatingsProxy extends Ratings {

  /**
   * Create a RatingsProxy object.
   * @param ratings a ratings data structure
   * @param indices an index list pointing to entries in the ratings
   * @version 2.03
   */
  public RatingsProxy(IRatings ratings, IntList indices) {

    users  = new IntListProxy(ratings.users(), indices);
    items  = new IntListProxy(ratings.items(), indices);
    values = new DoubleListProxy(ratings.values(), indices);

    maxUserID = ratings.maxUserID();
    maxItemID = ratings.maxItemID();
    maxRating = ratings.maxRating();
    minRating = ratings.minRating();

  }

}

