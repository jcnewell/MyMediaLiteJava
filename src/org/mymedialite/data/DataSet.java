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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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

  protected IntList users = new IntArrayList();
  protected IntList items = new IntArrayList();  

  /**
   * 
   */
  public IntList users() {
    return users;
  }

  /**
   * 
   */
  public IntList items() {
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
  public List<IntList> byUser() {
    if (byUser == null)
      buildUserIndices();
    return byUser;

  }

  /**
   * Rating indices organized by user.
   */
  protected List<IntList> byUser;

  /**
   * 
   */
  public List<IntList> byItem() {
    if (byItem == null)
      buildItemIndices();
    return byItem;
  }

  /**
   * Rating indices organized by item.
   */
  protected List<IntList> byItem;

  /**
   * 
   */
  public IntList randomIndex() {

    if (randomIndex == null || randomIndex.size() != size())
      buildRandomIndex();

    return randomIndex;
  }

  private IntList randomIndex;

  /**
   * 
   */
  public IntList allUsers() {
    IntSet resultSet = new IntOpenHashSet();
    for (int index = 0; index < users.size(); index++)
      resultSet.add(users.getInt(index));
    return new IntArrayList(resultSet);
  }

  /**
   * 
   */
  public IntList allItems() {
    IntSet resultSet = new IntOpenHashSet();
    for (int index = 0; index < items.size(); index++)
      resultSet.add(items.getInt(index));
    return new IntArrayList(resultSet);
  }

  /**
   * 
   */
  public void buildUserIndices() {
    byUser = new ArrayList<IntList>();
    for (int u = 0; u <= maxUserID; u++)
      byUser.add(new IntArrayList());

    // one pass over the data
    for (int index = 0; index < size(); index++)
      byUser.get(users.getInt(index)).add(index);
  }

  /**
   * 
   */
  public void buildItemIndices() {
    byItem = new ArrayList<IntList>();
    for (int i = 0; i <= maxItemID; i++)
      byItem.add(new IntArrayList());

    // One pass over the data
    for (int index = 0; index < size(); index++)
      byItem.get(items.getInt(index)).add(index);
  }

  /**
   * 
   */
  public void buildRandomIndex() {
    if (randomIndex == null || randomIndex.size() != size()) {
      randomIndex = new IntArrayList(size());
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
  public IntSet getUsers(IntList indices) {
    IntSet result_set = new IntArraySet();
    for (int index : indices)
      result_set.add(users.getInt(index));
    return result_set;
  }

  /**
   * 
   */
  public IntSet getItems(IntList indices) {
    IntSet result_set = new IntArraySet();
    for (int index : indices)
      result_set.add(items.getInt(index));
    return result_set;
  }

}