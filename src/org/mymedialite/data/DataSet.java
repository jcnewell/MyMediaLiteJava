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
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.openmbean.InvalidKeyException;

import org.mymedialite.util.Random;


/**
 * Abstract dataset class that implements some common functions.
 * @version 2.03
 */
public abstract class DataSet implements IDataSet {

  protected IntList users = new IntArrayList();
  protected IntList items = new IntArrayList();  

  @Override
  public IntList users() {
    return users;
  }

  @Override
  public IntList items() {
    return items;
  }

  @Override
  public int size() {
    return users.size();
  }

  @Override
  public int maxUserID() {
    return maxUserID;
  }

  protected int maxUserID = -1;

  @Override
  public int maxItemID() {
    return maxItemID;
  }

  protected int maxItemID = -1;

  @Override
  public List<IntList> byUser() {
    if (byUser == null)
      buildUserIndices();
    return byUser;

  }

  /** Rating indices organized by user */
  protected List<IntList> byUser;

  @Override
  public List<IntList> byItem() {
    if (byItem == null)
      buildItemIndices();
    return byItem;
  }

  /** Rating indices organized by item */
  protected List<IntList> byItem;

  @Override
  public IntList randomIndex() {
    if (randomIndex == null || randomIndex.size() != size())
      buildRandomIndex();

    return randomIndex;
  }

  private IntList randomIndex;

  @Override
  public IntList allUsers() {
    IntSet resultSet = new IntOpenHashSet();
    for (int index = 0; index < users.size(); index++)
      resultSet.add(users.getInt(index));
    return new IntArrayList(resultSet);
  }

  @Override
  public IntList allItems() {
    IntSet resultSet = new IntOpenHashSet();
    for (int index = 0; index < items.size(); index++)
      resultSet.add(items.getInt(index));
    return new IntArrayList(resultSet);
  }

  @Override
  public void buildUserIndices() {
    byUser = new ArrayList<IntList>();
    for (int u = 0; u <= maxUserID; u++)
      byUser.add(new IntArrayList());

    // one pass over the data
    for (int index = 0; index < size(); index++)
      byUser.get(users.getInt(index)).add(index);
  }

  @Override
  public void buildItemIndices() {
    byItem = new ArrayList<IntList>();
    for (int i = 0; i <= maxItemID; i++)
      byItem.add(new IntArrayList());

    // One pass over the data
    for (int index = 0; index < size(); index++)
      byItem.get(items.getInt(index)).add(index);
  }

  @Override
  public void buildRandomIndex() {
    if (randomIndex == null || randomIndex.size() != size()) {
      randomIndex = new IntArrayList(size());
      for (int index = 0; index < size(); index++)
        randomIndex.add(index, index);
    }
    Collections.shuffle(randomIndex, Random.getInstance());
  }

  @Override
  public IntSet getUsers(IntList indices) {
    IntSet result_set = new IntArraySet();
    for (int index : indices)
      result_set.add(users.getInt(index));
    return result_set;
  }

  @Override
  public IntSet getItems(IntList indices) {
    IntSet result_set = new IntArraySet();
    for (int index : indices)
      result_set.add(items.getInt(index));
    return result_set;
  }
  
  @Override
  public int getIndex(int user_id, int item_id) {
    for (int i = 0; i < size(); i++)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;

    throw new InvalidKeyException("index " + user_id + "' " + item_id + " not found.");
  }

  @Override
  public int getIndex(int user_id, int item_id, IntCollection indexes) {
    for (int i : indexes)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;
    
    throw new InvalidKeyException("index " + user_id + "' " + item_id + " not found.");
  }
  
  @Override
  public Integer tryGetIndex(int user_id, int item_id) {
    for (int i = 0; i < size(); i++)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;

    return null;
  }
  
  @Override
  public Integer tryGetIndex(int user_id, int item_id, IntCollection indexes) {
    for (int i : indexes)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;

    return null;
  }
  
}