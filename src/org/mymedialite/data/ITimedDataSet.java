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
//

package org.mymedialite.data;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * interface for data sets with time information.
 * @version 2.03
 */
public interface ITimedDataSet extends IDataSet, Comparator<Integer> {

  /**
   * the item entries.
   */
  List<Date> times();

  /**
   * earliest time.
   */
  Date earliestTime();

  /**
   * latest time.
   */
  Date latestTime();

  /**
   * Compares the dates of two entries in the Dataset.
   * @param index1 the index of the first entry
   * @param index2 the index of the second entry
   * @return the value 0 if the date of the two entries is equal; a value less than 0 if the date of the first entry is before the date of the second entry; and a value greater than 0 if the date of the first entry is after the date of the second entry.
   */
  int compare(Integer index1, Integer index2);
  
}
