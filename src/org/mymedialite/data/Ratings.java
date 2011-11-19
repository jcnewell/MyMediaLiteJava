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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.management.openmbean.InvalidKeyException;

import org.mymedialite.util.IntHashSet;

/**
 * Data structure for storing ratings</summary>
 * Small memory overhead for added flexibility.
 * This data structure supports incremental updates.
 */
public class Ratings implements IRatings {
  
  private ArrayList<Integer> users;
  
  private ArrayList<Integer> items;
  
  protected ArrayList<Double> values;
  
  private int maxUserID;
  
  private int maxItemID;
  
  double minRating;
  
  double maxRating;
  
  private List<List<Integer>> byUser;
  
  private List<List<Integer>> byItem;
  
  private List<Integer> randomIndex;
  
  private ArrayList<Integer> countByUser;
  
  private ArrayList<Integer> countByItem;
  
  /** Default constructor. */
  public Ratings() {
    users  = new ArrayList<Integer>();
    items  = new ArrayList<Integer>();
    values = new ArrayList<Double>();
    minRating = Double.MAX_VALUE;
    maxRating = Double.MIN_VALUE;
  }
  
  protected void setUsers(ArrayList<Integer> users) {
    this.users = users;
  }
  
  public List<Integer> getUsers() {
    return users;
  }

  protected void setItems(ArrayList<Integer> items) {
    this.items = items;
  }
  
  public List<Integer> getItems() {
    return items;
  }
  

  public double get(int index) {
    return values.get(index);
  }
  
  public void set(int index) {
    throw new UnsupportedOperationException();
  }
  
  public int size() {
    return values.size();
  }

  protected void setMaxUserID(int maxUserID) {
    this.maxUserID = maxUserID;
  }
  
  @Override
  public int getMaxUserID() {
    return maxUserID;
  }

  protected void setMaxItemID(int maxItemID) {
    this.maxItemID = maxItemID;
  }
  
  @Override
  public int getMaxItemID() {
    return maxItemID;
  }

  protected void setMinRating(double value) {
    minRating = value;    
  }
  
  @Override
  public double getMinRating() {
    return minRating;    
  }

  protected void setMaxRating(double value) {
    maxRating = value;
  }
  
  @Override
  public double getMaxRating() {
    return maxRating;
  }

  /** Ratings indices organized by user. */
  public List<List<Integer>> getByUser() {
    if (byUser == null) buildUserIndices();
    return byUser;
  }

  public void buildUserIndices() {
    byUser = new ArrayList<List<Integer>>(maxUserID + 1);
    for (int u = 0; u <= maxUserID; u++) {
      byUser.add(u, new ArrayList<Integer>());
    }
    // One pass over the data.
    for (int index = 0; index < size(); index++) {
      byUser.get(users.get(index)).add(index);
    }
  }

  public List<List<Integer>> getByItem() {
    if (byItem == null) buildItemIndices();
    return byItem;
  }

  public void buildItemIndices() {
    byItem = new ArrayList<List<Integer>>(maxItemID + 1);
    for (int i = 0; i <= maxItemID; i++) {
      byItem.add(i, new ArrayList<Integer>());
    }

    for (int index = 0; index < size(); index++) {
      byItem.get(items.get(index)).add(index);
    }
  }

  public List<Integer> getRandomIndex() {
    if (randomIndex == null || randomIndex.size() != size()) buildRandomIndex();
    return randomIndex;
  }

  public void buildRandomIndex() {
    randomIndex = new ArrayList<Integer>(size());
    for (int index = 0; index < size(); index++) {
      randomIndex.add(index, index);
    }
    Collections.shuffle(randomIndex);
  }

  public List<Integer> getCountByUser() {
    if (countByUser == null) buildByUserCounts();
    return countByUser;
  }

  public void buildByUserCounts() {
    countByUser = new ArrayList<Integer>(maxUserID + 1);
    for (int index = 0; index < size(); index++) {
      int userId = users.get(index);
      Integer count = countByUser.get(userId);
      if(count != null) {
        countByUser.set(userId, count + 1);
      } else {
        countByUser.set(userId, 1);
      }
    }
  }       
  
  public List<Integer> getCountByItem() {
    System.out.println("getCountByItem");
    if (countByItem == null || countByItem.size() < maxItemID + 1) buildByItemCounts();
    return countByItem;
  }

  public void buildByItemCounts() {
    System.out.println("buildByItemCounts");
    countByItem = new ArrayList<Integer>(maxItemID + 1);
    for (int index = 0; index < size(); index++) {
      int itemId = items.get(index);
      Integer count = countByItem.get(itemId);
      if(count != null) {
        countByItem.set(itemId, count + 1);
      } else {
        countByItem.set(itemId, 1);
      }
    }
  }       
  
  // TODO speed up
  public double getAverage() {
    double sum = 0;
    for (int index = 0; index < size(); index++) {
      sum += get(index);
    }
    double average = sum / size();
    return average;
  }

  // TODO think whether we want to have a set or a list here
  public IntHashSet getAllUsers() {
    IntHashSet result_set = new IntHashSet();
    for (int index = 0; index < users.size(); index++) {
     result_set.add(users.get(index));
    }
    return result_set;
  }

  public IntHashSet getAllItems() {
    IntHashSet result_set = new IntHashSet();
    for (int index = 0; index < items.size(); index++) {
      result_set.add(items.get(index));
    }
    return result_set;
  }

  public IntHashSet getUsers(List<Integer> indices) {
    IntHashSet result_set = new IntHashSet();
    for (int index : indices) {
      result_set.add(users.get(index));
    }
    return result_set;
  }

  public IntHashSet getItems(List<Integer> indices) {
    IntHashSet result_set = new IntHashSet();
    for (int index : indices) {
      result_set.add(items.get(index));
    }
    return result_set;
  }

  public double get(int user_id, int item_id) {
    // TODO speed up
    for (int index = 0; index < values.size(); index++) {
      if (users.get(index) == user_id && items.get(index) == item_id) return values.get(index);
    }
    throw new InvalidKeyException("rating " + user_id +  ", " + item_id + " not found.");
  }

  public Double tryGet(int user_id, int item_id) {
    // TODO speed up
    for (int index = 0; index < values.size(); index++) {
      if (users.get(index) == user_id && items.get(index) == item_id) {
        return values.get(index);
      }
    }
    return null;
  }

  public double get(int user_id, int item_id, Collection<Integer> indexes) {
    // TODO speed up
    for (int index : indexes) {
      if (users.get(index) == user_id && items.get(index) == item_id) return values.get(index);
    }
    throw new InvalidKeyException("rating " + user_id + ", " + item_id +  " not found.");
  }

  public Double tryGet(int user_id, int item_id, Collection<Integer> indexes) {
    // TODO speed up
    for (int index : indexes) {
      if (users.get(index) == user_id && items.get(index) == item_id) {
        return values.get(index);
      }
    }
    return null;
  }

  public Integer tryGetIndex(int user_id, int item_id) {
    // TODO speed up
    for (int i = 0; i < size(); i++) {
      if (users.get(i) == user_id && items.get(i) == item_id) {
        return i;
      }
    }
    return null;
  }

  public Integer tryGetIndex(int user_id, int item_id, Collection<Integer> indexes) {
    // TODO speed up
    for (int i : indexes) {
      if (users.get(i) == user_id && items.get(i) == item_id) {
        return i;
      }
    }
    return null;
  }

  public int getIndex(int user_id, int item_id) {
      // TODO speed up
      for (int i = 0; i < size(); i++) {
        if (users.get(i) == user_id && items.get(i) == item_id) return i;
      }
      throw new InvalidKeyException("index " + user_id + "' " + item_id + " not found.");
  }

  public int getIndex(int user_id, int item_id, Collection<Integer> indexes) {
      // TODO speed up
      for (int i : indexes) {
          if (users.get(i) == user_id && items.get(i) == item_id) return i;
      }
      throw new InvalidKeyException("index " + user_id + "' " + item_id + " not found.");
  }

  public void add(int user_id, int item_id, float rating) {
      add(user_id, item_id, (double) rating);
      byUser = null;
  }       
  
  public void add(int user_id, int item_id, byte rating) {
      add(user_id, item_id, (double) rating);
      byUser = null;
  }

  public void add(int user_id, int item_id, double rating) {
    users.add(user_id);
    items.add(item_id);
    values.add(rating);

    int pos = users.size() - 1;
    
    // TODO maybe avoid for fast reading.
    if (user_id > maxUserID) maxUserID = user_id;
    if (item_id > maxItemID) maxItemID = item_id;
    if (rating < minRating)  minRating = rating;
    if (rating > maxRating)  maxRating = rating;
      
    // Update index data structures if necessary.
    if (byUser != null) {
      for (int u = byUser.size(); u <= user_id; u++) {
        byUser.add(new ArrayList<Integer>());
      }
      byUser.get(user_id).add(pos);
    }
    if (byItem != null) {
      for (int i = byItem.size(); i <= item_id; i++) {
        byItem.add(new ArrayList<Integer>());
      }
      byItem.get(item_id).add(pos);
    }
  }

  /** Override an existing value if it exists. */
  public void addOrUpdate(int user_id, int item_id, double rating) {
    for (int index = 0; index < values.size(); index++) {
      if (users.get(index) == user_id && items.get(index) == item_id) {
        values.set(index, rating);
        return;
      }
    }
    add(user_id, item_id, rating);
  }

  public void removeAt(int index) {
      users.remove(index);
      items.remove(index);
      values.remove(index);
  }
  
  public void removeUser(int user_id) {
    for (int index = 0; index < size(); index++) {
      if (users.get(index) == user_id) {
        users.remove(index);
        items.remove(index);
        values.remove(index);
      }
    }  
    if (maxUserID == user_id) maxUserID--;
  }

  public void removeItem(int item_id) {
    for (int index = 0; index < size(); index++) {
      if (items.get(index) == item_id) {
        users.remove(index);
        items.remove(index);
        values.remove(index);
      }
    }
    if (maxItemID == item_id) maxItemID--;
  }       
  
  public boolean isReadOnly() {
    return true;
  }
  
  public void add(double item) { throw new UnsupportedOperationException(); }
  
  public void clear() { throw new UnsupportedOperationException(); }
  
  public boolean contains(double item) { throw new UnsupportedOperationException(); }

  public void copyTo(double[] array, int index) { throw new UnsupportedOperationException(); }        
  
  public int indexOf(double item) { throw new UnsupportedOperationException(); }
  
  public void insert(int index, double item) { throw new UnsupportedOperationException(); }

  public boolean remove(double item) { throw new UnsupportedOperationException(); }

}