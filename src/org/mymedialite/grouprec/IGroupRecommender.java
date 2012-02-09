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

package org.mymedialite.grouprec;

import java.util.Collection;
import java.util.List;

/**
 * Interface for group recommenders.
 * @version 2.03
 */
public interface IGroupRecommender {

  /**
   * Rank items for a given group of users.
   * @param users the users
   * @param items the items to be ranked
   * @return a ranked list of items, highest-ranking item comes first
   */
  List<Integer> rankItems(Collection<Integer> users, Collection<Integer> items);

}
