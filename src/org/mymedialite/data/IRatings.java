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
import java.util.HashSet;
import java.util.List;

/** Interface for rating datasets. */
public interface IRatings { // extends List<Double> {
	  
  public int size();

  /** The user entries. */
  List<Integer> getUsers();
  
  /** The item entries. */
  List<Integer> getItems();
  
  /** The maximum user ID in the dataset. */
  int getMaxUserID();
  
  /** The maximum item ID in the dataset. */
  int getMaxItemID();
  
  /** Get the minimum rating in the dataset. */
  double getMinRating();
  
  /** Get the maximum rating in the dataset. */
  double getMaxRating();

  /** Indices by user. */
  List<List<Integer>> getByUser();
  
  /** Indices by item. */
  List<List<Integer>> getByItem();
  
  /** Get a randomly ordered list of all indices. */
  List<Integer> getRandomIndex();
  
  /**
   * Get the rating count by user.
   */
  List<Integer> getCountByUser();
  
  /**
   * Get the rating count by item.
   */
  List<Integer> getCountByItem();  
  
  /** Build the user indices. */
  void buildUserIndices();
  
  /** Build the item indices. */
  void buildItemIndices();
  
  /** Build the random index. */
  void buildRandomIndex();

  /** Average rating in the dataset. */
  double getAverage();

  /** All user IDs in the dataset. */
  HashSet<Integer> getAllUsers();
  
  /** All item IDs in the dataset. */
  HashSet<Integer> getAllItems();

  /** 
   * Get all users that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return the set of users
   */
  HashSet<Integer> getUsers(List<Integer> indices);
  
  /**
   * Get all items that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return the set of items
   */
  HashSet<Integer> getItems(List<Integer> indices);

  /**
   * Directly access ratings.
   * @param indices the indices of the rating
   * @return the rating
   */
  double get(int indexes);
  
  /**
   * Directly access rating by user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the first found rating of the given item by the given user
   */
  double get(int user_id, int item_id);

  /** 
   * Try to retrieve a rating for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return will contain the first rating encountered that matches the user ID and item ID
   * @return true if a rating was found for the user and item   - RETURNS NULL INSTEAD
   */
  Double tryGet(int user_id, int item_id);

  /**
   * Try to retrieve a rating for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @param rating will contain the first rating encountered that matches the user ID and item ID
   * @return true if a rating was found for the user and item
   */
  Double tryGet(int user_id, int item_id, Collection<Integer> indexes);

  /** 
   * Directly access rating by user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the first rating encountered that matches the user ID and item ID
   */
  double get(int user_id, int item_id, Collection<Integer> indexes);

  /**
   * Get index of rating for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the index of the first rating encountered that matches the user ID and item ID
   */
  int getIndex(int user_id, int item_id);

  /**
   * Get index of rating for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the index of the first rating encountered that matches the user ID and item ID
   */
  int getIndex(int user_id, int item_id, Collection<Integer> indexes);

  /**
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param index will contain the index of the first rating encountered that matches the user ID and item ID
   * @return true if an index was found for the user and item
   */
  Integer tryGetIndex(int user_id, int item_id);

  /** 
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @param index will contain the index of the first rating encountered that matches the user ID and item ID
   * @return true if an index was found for the user and item
   */
  Integer tryGetIndex(int user_id, int item_id, Collection<Integer> indexes);

  /**
   * Add byte-valued rating to the collection.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param rating the rating
   */
  void add(int user_id, int item_id, byte rating);

  /**
   * Add float-valued rating to the collection.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param rating the rating
   */
  void add(int user_id, int item_id, float rating);
  
  /** 
   * Add a new rating.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param rating the rating value
   */
  void add(int user_id, int item_id, double rating);

  /**
   * Override an exiting value if it exists.
   * @param user_id
   * @param item_id
   * @param rating
   */
  void addOrUpdate(int user_id, int item_id, double rating);
  
  /**
   * Remove all ratings by a given user.
   * @param user_id the user ID
   */
  void removeUser(int user_id);

  /**
   * Remove all ratings of a given item.
   * @param item_id the item ID
   */
  void removeItem(int item_id);

}

