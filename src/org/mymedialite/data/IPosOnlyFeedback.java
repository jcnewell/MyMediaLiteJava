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

import java.util.Collection;
import org.mymedialite.datatype.IBooleanMatrix;

/**
 * Interface for implicit, positive-only user feedback.
 */
public interface IPosOnlyFeedback {
  
  /**
   * By-user access, users are stored in the rows, items in the culumns.
   */
  IBooleanMatrix getUserMatrix();

  /**
   * By-item access, items are stored in the rows, users in the culumns.
   */
  IBooleanMatrix getItemMatrix();

  /**
   * The maximum user ID.
   */
  int getMaxUserID();

  /**
   * The maximum item ID.
   */
  int getMaxItemID();

  /**
   * The number of feedback events.
   */
  int size();

  /**
   * Get all users that have given feedback.
   */
  Collection<Integer> getAllUsers();

  /**
   * Get all items mentioned at least once.
   */
  Collection<Integer> getAllItems();

  /**
   * Add a user-item event to the data structure.
   * @param user_id the user ID
   * @param item_id the item ID
   */
  void add(int user_id, int item_id);

  /**
   * Remove a user-item event from the data structure.
   * @param user_id the user ID
   * @param item_id the item ID
   */
  void remove(int user_id, int item_id);

  /**
   * Remove all feedback by a given user.
   * @param user_id the user id
   */
  void removeUser(int user_id);

  /**
   * Remove all feedback about a given item.
   * @param item_id the item ID
   */
  void removeItem(int item_id);

  /**
   * Compute the number of overlapping events in two feedback datasets.
   * @param s the feedback dataset to compare to
   * @return the number of overlapping events, i.e. events that have the same user and item ID
   */
  int overlap(IPosOnlyFeedback s);

}

