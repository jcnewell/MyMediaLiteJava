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
 * Data structure for implicit, positive-only user feedback.
 * This data structure supports incremental updates.
 */
public class PosOnlyFeedback<T extends IBooleanMatrix> implements IPosOnlyFeedback {
    
  /** By-user access, users are stored in the rows, items in the columns */
  public IBooleanMatrix userMatrix;

  /** By-item access, items are stored in the rows, users in the columns */
  public IBooleanMatrix itemMatrix;
  
  /** The maximum user ID */
  private int maxUserID = 0;

  /** The maximum item ID */
  private int maxItemID = 0;

  /**
   * Create a PosOnlyFeedback object.
   * @param t the user-item matrix
   */
  public PosOnlyFeedback(T t) {
    userMatrix = t;
    maxUserID = userMatrix.getNumberOfRows();
    maxItemID = userMatrix.getNumberOfColumns();
  }
  
  public IBooleanMatrix getUserMatrix() {
    return userMatrix;
  }

  @Override
  public int getMaxUserID() {
    return maxUserID;
  }

  @Override
  public int getMaxItemID() {
    return maxItemID;
  }
  
  public IBooleanMatrix getItemMatrix() {
    if(itemMatrix == null) itemMatrix = (IBooleanMatrix) userMatrix.transpose();
    return itemMatrix;
  }
  
  /** the number of feedback events. */
  public int size() {
    return userMatrix.getNumberOfEntries();
  }
  
  /**
   * Get all users that have given feedback.
   */
  public Collection<Integer> getAllUsers() {
    return userMatrix.getNonEmptyRowIDs();
  }
  
  /**
   * Get all items mentioned at least once.
   */
  public Collection<Integer> getAllItems() {
    if (itemMatrix == null) {
      return userMatrix.getNonEmptyColumnIDs();
    } else {
      return itemMatrix.getNonEmptyRowIDs();
    }
  }
  
  /**
   * Add a user-item event to the data structure
   * @param user_id the user ID
   * @param item_id the item ID
   */
  public void add(int user_id, int item_id) {
    userMatrix.set(user_id, item_id, true);
    if (itemMatrix != null) itemMatrix.set(item_id, user_id, true);
    if (user_id > maxUserID) maxUserID = user_id;
    if (item_id > maxItemID) maxItemID = item_id;
  }
  
  /**
   * Remove a user-item event from the data structure.
   * @param user_id the user ID
   * @param item_id >the item ID
   */
  public void remove(int user_id, int item_id) {
    userMatrix.set(user_id, item_id, false);
    if (itemMatrix != null) itemMatrix.set(item_id, user_id, false);
  }

  /**
   * Remove all feedback by a given user.
   * @param user_id the user ID
   */
  public void removeUser(int user_id) {
    userMatrix.getRow(user_id).clear();
    if (itemMatrix != null) {
      for (int i = 0; i < itemMatrix.getNumberOfRows(); i++) {
        itemMatrix.getRow(i).remove(user_id);
      }
    }
  }

  /**
   * Remove all feedback about a given item</summary>
   * @param item_id the item ID
   */
  public void removeItem(int item_id) {
    for (int u = 0; u < userMatrix.getNumberOfRows(); u++) {
      userMatrix.getRow(u).remove(item_id);
    }
    if (itemMatrix != null)  itemMatrix.getRow(item_id).clear();
  }

  /**
   * Compute the number of overlapping events in two feedback datasets.
   * @param s the feedback dataset to compare to
   * @return the number of overlapping events, i.e. events that have the same user and item ID
   */
  public int overlap(IPosOnlyFeedback s) {
      return userMatrix.overlap(s.getUserMatrix());
  }

}