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

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;

/**
 * Interface for different kinds of collaborative filtering data sets.
 * 
 * Implementing classes/inheriting interfaces are e.g. for rating data and for positive-only implicit feedback.
 *
 * The main feature of a dataset is that it has some kind of order (not explicitly stated)
 * - random, chronological, user-wise, or item-wise - and that it contains tuples of users and
 * items (not necessarily unique tuples).
 *
 * Implementing classes and inheriting interfaces can add additional data to each user-item tuple,
 * e.g. the date/time of an event, location, context, etc., as well as additional index structures
 * to access the dataset in a certain fashion.
 * 
 * @author Zeno Gantner
 * @version 2.03
 */
public interface IDataSet {

  /**
   * @return the number of interaction events in the dataset.
   */
  int size();

  /**
   * @return the user entries.
   */
  IntList users();

  /**
   * @return the item entries.
   */
  IntList items();

  /**
   * @return the maximum user ID in the dataset.
   */
  int maxUserID();

  /**
   * @return the maximum item ID in the dataset.
   */
  int maxItemID();

  /**
   * @return all user IDs in the dataset.
   */
  IntList allUsers();

  /**
   * @return all item IDs in the dataset.
   */
  IntList allItems();

  /**
   * indices by user.
   * Should be implemented as a lazy data structure
   */
  List<IntList> byUser();

  /**
   * indices by item.
   * Should be implemented as a lazy data structure
   */
  List<IntList> byItem();

  /**
   * get a randomly ordered list of all indices.
   * Should be implemented as a lazy data structure
   */
  IntList randomIndex();

  /** Build the user indices. */
  void buildUserIndices();

  /** Build the item indices. */
  void buildItemIndices();

  /** Build the random index. */
  void buildRandomIndex();

  /**
   * Remove all events related to a given user.
   * @param user_id the user ID
   */
  void removeUser(int user_id);

  /**
   * Remove all events related to a given item.
   * @param item_id the item ID
   */
  void removeItem(int item_id);

  /**
   * Get all users that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return all users referenced by the list of indices
   */
  IntSet getUsers(IntList indices);
  
  /**
   * Get all items that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return all items referenced by the list of indices
   */
  IntSet getItems(IntList indices);

  /**
   * Get index for a given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the index of the first event encountered that matches the user ID and item ID
   */
  int getIndex(int user_id, int item_id);
  
  /**
   * Get index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the index of the first event encountered that matches the user ID and item ID
   */
  int getIndex(int user_id, int item_id, IntCollection indexes);

  /**
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the index of the first event encountered that matches the user ID and item ID or null, if not found
   */
  Integer tryGetIndex(int user_id, int item_id);
  
  /**
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the index of the first event encountered that matches the user ID and item ID or null, if not found
   */
  Integer tryGetIndex(int user_id, int item_id, IntCollection indexes);

}
