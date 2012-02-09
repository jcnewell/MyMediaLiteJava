//Copyright (C) 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mymedialite.util.Random;

/**
 * Abstract dataset class that implements some common functions.
 * @version 2.03
 */
public abstract class DataSet implements IDataSet {

  protected List<Integer> users = new ArrayList<Integer>();
  protected List<Integer> items = new ArrayList<Integer>();  

  /**
   * 
   */
  public List<Integer> users() {
    return users;
  }

  /**
   * 
   */
  public List<Integer> items() {
    return items;
  }

  /**
   * 
   */
  public int size() {
    return users.size();
  }

  public int maxUserID() {
    return maxUserID;
  }

  protected int maxUserID = -1;

  public int maxItemID() {
    return maxItemID;
  }

  protected int maxItemID = -1;

  /**
   * 
   */
  public List<List<Integer>> byUser() {
    if (byUser == null)
      buildUserIndices();
    return byUser;

  }

  /**
   * Rating indices organized by user.
   */
  protected List<List<Integer>> byUser;

  /**
   * 
   */
  public List<List<Integer>> byItem() {
    if (byItem == null)
      buildItemIndices();
    return byItem;
  }

  /**
   * Rating indices organized by item.
   */
  protected List<List<Integer>> byItem;

  /**
   * 
   */
  public List<Integer> randomIndex() {

    if (randomIndex == null || randomIndex.size() != size())
      buildRandomIndex();

    return randomIndex;
  }

  private List<Integer> randomIndex;

  /**
   * 
   */
  public List<Integer> allUsers() {
    Set<Integer> resultSet = new HashSet<Integer>();
    for (int index = 0; index < users.size(); index++)
      resultSet.add(users.get(index));
    return new ArrayList<Integer>(resultSet);
  }

  /**
   * 
   */
  public List<Integer> allItems() {
    Set<Integer> resultSet = new HashSet<Integer>();
    for (int index = 0; index < items.size(); index++)
      resultSet.add(items.get(index));
    return new ArrayList<Integer>(resultSet);
  }

  /**
   * 
   */
  public void buildUserIndices() {
    byUser = new ArrayList<List<Integer>>();
    for (int u = 0; u <= maxUserID; u++)
      byUser.add(new ArrayList<Integer>());

    // one pass over the data
    for (int index = 0; index < size(); index++)
      byUser.get(users.get(index)).add(index);
  }

  /**
   * 
   */
  public void buildItemIndices() {
    byItem = new ArrayList<List<Integer>>();
    for (int i = 0; i <= maxItemID; i++)
      byItem.add(new ArrayList<Integer>());

    // One pass over the data
    for (int index = 0; index < size(); index++)
      byItem.get(items.get(index)).add(index);
  }

  /**
   * 
   */
  public void buildRandomIndex() {
    if (randomIndex == null || randomIndex.size() != size()) {
      randomIndex = new ArrayList<Integer>(size());
      for (int index = 0; index < size(); index++)
        randomIndex.add(index, index);
    }
    Collections.shuffle(randomIndex, Random.getInstance());
  }

  /**
   * 
   */
  public abstract void removeUser(int user_id);

  /**
   * 
   */
  public abstract void removeItem(int item_id);

  /**
   * 
   */
  public Set<Integer> getUsers(List<Integer> indices) {
    Set<Integer> result_set = new HashSet<Integer>();
    for (int index : indices)
      result_set.add(users.get(index));
    return result_set;
  }

  /**
   * 
   */
  public Set<Integer> getItems(List<Integer> indices) {
    Set<Integer> result_set = new HashSet<Integer>();
    for (int index : indices)
      result_set.add(items.get(index));
    return result_set;
  }

}