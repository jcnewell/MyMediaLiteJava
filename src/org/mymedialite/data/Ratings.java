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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.management.openmbean.InvalidKeyException;


/**
 * Data structure for storing ratings
 * Small memory overhead for added flexibility.
 * This data structure supports incremental updates.
 * @version 2.03
 */
public class Ratings extends DataSet implements IRatings {

  protected DoubleList values = new DoubleArrayList();
  protected double minRating = Double.MAX_VALUE;
  protected double maxRating = Double.MIN_NORMAL;
  private IntList countByUser;
  private IntArrayList countByItem;


  public DoubleList values() {
    return values;
  }
  
  @Override
  public double get(int index) {
    return values.getDouble(index);
  }

  @Override
  public double set(int index, double rating) {
    return values.set(index, rating);
  }	

  public void setMinRating(double value) {
    minRating = value;    
  }
  
  public double minRating() {
    return minRating;    
  }

  public void setMaxRating(double value) {
    maxRating = value;
  }
  
  public double maxRating() {
    return maxRating;
  }

  @Override
  public IntList countByUser() {
    if (countByUser == null)
      buildByUserCounts();
    return countByUser;
  }

  public void buildByUserCounts() {
    countByUser = new IntArrayList(maxUserID + 1);
    for (int index = 0; index < size(); index++) {
      int userId = users.getInt(index);
      Integer count = countByUser.get(userId);
      if (count != null)
        countByUser.set(userId, count + 1);
      else
        countByUser.set(userId, 1);
    }
  }

  @Override
  public IntList countByItem() {
    if (countByItem == null || countByItem.size() < maxItemID + 1)
      buildByItemCounts();
    return countByItem;
  }

  public void buildByItemCounts() {
    countByItem = new IntArrayList(maxItemID + 1);
    for (int index = 0; index < size(); index++) {
      int itemId = items.getInt(index);
      Integer count = countByItem.get(itemId);
      if (count != null)
        countByItem.set(itemId, count + 1);
      else
        countByItem.set(itemId, 1);
    }
  }       

  @Override
  public double average() {
    double sum = 0;
    for (int index = 0; index < size(); index++)
      sum += get(index);
    double average = sum / size();
    return average;
  }

  @Override
  public IntSet getUsers(IntList indices) {
    IntSet result_set = new IntOpenHashSet();
    for (int index : indices)
      result_set.add(users.getInt(index));
    return result_set;
  }

  @Override
  public IntSet getItems(IntList indices) {
    IntSet result_set = new IntOpenHashSet();
    for (int index : indices)
      result_set.add(items.getInt(index));
    return result_set;
  }
  
  @Override
  public double get(int user_id, int item_id) {
    for (int index = 0; index < values.size(); index++)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return values.getDouble(index);
    throw new InvalidKeyException("rating " + user_id +  ", " + item_id + " not found.");
  }
  
  @Override
  public Double tryGet(int user_id, int item_id) {
    for (int index = 0; index < values.size(); index++)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return values.get(index);
    return null;
  }

  @Override
  public double get(int user_id, int item_id, Collection<Integer> indexes) {
    for (int index : indexes)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return values.getDouble(index);
    throw new InvalidKeyException("rating " + user_id + ", " + item_id +  " not found.");
  }

  @Override
  public Double tryGet(int user_id, int item_id, Collection<Integer> indexes) {
    for (int index : indexes)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return values.get(index);
    return null;
  }

  @Override
  public Integer tryGetIndex(int user_id, int item_id) {
    for (int i = 0; i < size(); i++)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;
    return null;
  }

  @Override
  public Integer tryGetIndex(int user_id, int item_id, Collection<Integer> indexes) {
    for (int i : indexes)
      if (users.getInt(i) == user_id && items.getInt(i) == item_id)
        return i;
    return null;
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
  public void add(int user_id, int item_id, float rating) {
    add(user_id, item_id, (double) rating);
    byUser = null;
  }       

  @Override
  public void add(int user_id, int item_id, byte rating) {
    add(user_id, item_id, (double) rating);
    byUser = null;
  }

  @Override
  public void add(int user_id, int item_id, double rating) {
    users.add(user_id);
    items.add(item_id);
    values.add(rating);

    int pos = users.size() - 1;

    if (user_id > maxUserID)
      maxUserID = user_id;
    if (item_id > maxItemID)
      maxItemID = item_id;
    if (rating < minRating)
      minRating = rating;
    if (rating > maxRating)
      maxRating = rating;

    // Update index data structures if necessary.
    if (byUser != null) {
      for (int u = byUser.size(); u <= user_id; u++)
        byUser.add(new IntArrayList());
      byUser.get(user_id).add(pos);
    }
    if (byItem != null) {
      for (int i = byItem.size(); i <= item_id; i++)
        byItem.add(new IntArrayList());
      byItem.get(item_id).add(pos);
    }
  }

  /** Override an existing value if it exists. */
  public void addOrUpdate(int user_id, int item_id, double rating) {
    for (int index = 0; index < values.size(); index++)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id) {
        values.set(index, rating);
        return;
      }
    add(user_id, item_id, rating);
  }

  @Override
  public void removeAt(int index) {
    users.remove(index);
    items.remove(index);
    values.removeDouble(index);
  }

  @Override
  public void removeUser(int user_id) {
    for (int index = 0; index < size(); index++)
      if (users.getInt(index) == user_id) {
        users.remove(index);
        items.remove(index);
        values.remove(index);
      }
    if (maxUserID == user_id)
      maxUserID--;
  }

  @Override
  public void removeItem(int item_id) {
    for (int index = 0; index < size(); index++)
      if (items.getInt(index) == item_id) {
        users.remove(index);
        items.remove(index);
        values.remove(index);
      }
    if (maxItemID == item_id)
      maxItemID--;
  }       
  
  public boolean isReadOnly() {
    return true;
  }

//  @Override
//  public boolean add(Double e) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void add(int index, Double element) {
//    throw new UnsupportedOperationException();
//  }

//  @Override
//  public boolean addAll(Collection<? extends Double> c) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean addAll(int index, Collection<? extends Double> c) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void clear() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean contains(Object o) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean containsAll(Collection<?> c) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public int indexOf(Object o) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean isEmpty() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public int lastIndexOf(Object o) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean remove(Object o) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean removeAll(Collection<?> c) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean retainAll(Collection<?> c) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public Double set(int index, Double element) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public Object[] toArray() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public <T> T[] toArray(T[] a) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public int compareTo(List<? extends Double> o) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean addAll(DoubleCollection arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean contains(double arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean containsAll(DoubleCollection arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean rem(double arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean removeAll(DoubleCollection arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean retainAll(DoubleCollection arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public double[] toArray(double[] arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public double[] toDoubleArray() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public double[] toDoubleArray(double[] arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean add(double arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void add(int arg0, double arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean addAll(DoubleList arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean addAll(int arg0, DoubleCollection arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean addAll(int arg0, DoubleList arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void addElements(int arg0, double[] arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void addElements(int arg0, double[] arg1, int arg2, int arg3) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleList doubleSubList(int arg0, int arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void getElements(int arg0, double[] arg1, int arg2, int arg3) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public int indexOf(double arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public int lastIndexOf(double arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public double removeDouble(int arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void removeElements(int arg0, int arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void size(int arg0) {
//    throw new UnsupportedOperationException(); 
//  }
//
//  @Override
//  public DoubleList subList(int arg0, int arg1) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleListIterator doubleListIterator() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleListIterator doubleListIterator(int arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleListIterator iterator() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleListIterator listIterator() {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleListIterator listIterator(int arg0) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public DoubleIterator doubleIterator() {
//    throw new UnsupportedOperationException();
//  }

}