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

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.*;

/**
 * Interface for rating datasets.
 * @version 2.03
 */
public interface IRatings extends IDataSet {

  DoubleList values();
  
  public int size();

  /** Get the maximum rating in the dataset. */
  double maxRating(); 

  /** Get the minimum rating in the dataset. */
  double minRating();  

  /** Get the rating count by user. */
  IntList countByUser();  

  /** Get the rating count by item. */
  IntList countByItem();  

  /** Average rating in the dataset. */
  double average();

  /**
   * Directly access rating by user and item</summary>
   * @param userId the user ID
   * @param itemId the item ID
   * @return <value>the rating value for the given user and item
   */
  double get(int userId, int itemId);

  /**
   * Directly access ratings.
   * @param index the index of the rating
   * @return the rating value
   */
  double get(int index);

  /**
   * Directly access the ratings
   * @param index the rating index
   * @param rating the rating value
   */
  double set(int index, double rating);

  /** 
   * Get all users that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return the set of users
   */
  IntSet getUsers(IntList indices);
  
  /**
   * Get all items that are referenced by a given list of indices.
   * @param indices the indices to take into account
   * @return the set of items
   */
  IntSet getItems(IntList indices);
  
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
  int getIndex(int user_id, int item_id, IntCollection indexes);

  /**
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the index of the rating that matches the user ID and item ID or null, if not found
   */
  Integer tryGetIndex(int user_id, int item_id);

  /** 
   * Try to get the index for given user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the index of the first rating encountered that matches the user ID and item ID or null, if none is found
   */
  Integer tryGetIndex(int user_id, int item_id, Collection<Integer> indexes);	

  /** 
   * Try to retrieve a rating for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the first rating encountered that matches the user ID and item ID, or null if none is found
   */
  Double tryGet(int user_id, int item_id);

  /**
   * Try to retrieve a rating for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the first rating encountered that matches the user ID and item ID, or null if none found
   */
  Double tryGet(int user_id, int item_id, Collection<Integer> indexes);
  // TODO name 'tryGet' makes no sense here

  /** 
   * Directly access rating by user and item.
   * @param user_id the user ID
   * @param item_id the item ID
   * @param indexes the indexes to look at
   * @return the first rating encountered that matches the user ID and item ID
   */
  double get(int user_id, int item_id, Collection<Integer> indexes);

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
   * Remove the rating at the specified index  
   * @param index the rating index
   */
   void removeAt(int index);
  
}


