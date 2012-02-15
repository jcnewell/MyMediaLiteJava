//Copyright (C) 2011 Zeno Gantner, Chris Newell
//
//This file is part of MyMediaLite.
//
//MyMediaLite is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//MyMediaLite is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.datatype;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Proxy class that allows access to selected elements of an underlying list data structure.
 * @version 2.03
 */
public class IntListProxy implements IntList {

  IntList list;
  IntList indices;

  /**
   * Create a new IntListProxy object.
   * @param list the list to proxy
   * @param indices an index list pointing to entries in the list
   */
  public IntListProxy(IntList list, IntList indices) {
    this.list = list;
    this.indices = indices;
  }

  @Override
  public Integer get(int index) {
    return list.get(indices.getInt(index));
  }

  @Override
  public int getInt(int index) {
    return list.getInt(indices.getInt(index));
  }

  @Override
  public Integer set(int index, Integer value) {
    return list.set(indices.getInt(index), value);
  }

  @Override
  public int size() {
    return indices.size(); 
  }

  @Override
  public boolean contains(int item) {
    for (int index : indices)
      if (list.getInt(index) == item)
        return true;
    return false;
  } 

  /**
   * 
   */
  public boolean isReadOnly() { 
    return true;
  }

  /**
   * 
   */
  public boolean isFixedSize() { 
    return true; 
  }
  
  @Override
  public IntListIterator iterator() {
    IntList subList = new IntArrayList();
    for (int index : indices)
      subList.add(list.getInt(index));

    return list.iterator();
  }

  @Override
  public IntListIterator listIterator() {
    IntList subList = new IntArrayList();
    for (int index : indices)
      subList.add(list.getInt(index));

    return subList.iterator();
  }
  
  @Override
  public IntIterator intIterator() {
    IntList subList = new IntArrayList();
    for (int index : indices)
      subList.add(list.getInt(index));

    return subList.iterator();
  }

  @Override
  public IntListIterator listIterator(int index) {
    IntList subList = new IntArrayList();
    for (int i = index; i<indices.size(); i++)
      subList.add(list.get(indices.getInt(i)));

    return subList.listIterator();
  }
  
  @Override
  public IntListIterator intListIterator() {
    IntList subList = new IntArrayList();
    for (int index : indices)
      subList.add(list.getInt(index));

    return subList.iterator();
  }

  @Override
  public IntListIterator intListIterator(int index) {
    IntList subList = new IntArrayList();
    for (int i = index; i<indices.size(); i++)
      subList.add(list.get(indices.getInt(i)));

    return subList.listIterator();
  }

  @Override
  public void removeElements(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean add(Integer e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, Integer element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends Integer> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(List<? extends Integer> o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(IntCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(IntCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean rem(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(IntCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(IntCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] toArray(int[] arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] toIntArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] toIntArray(int[] arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(IntList arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int arg0, IntCollection arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int arg0, IntList arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addElements(int arg0, int[] arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addElements(int arg0, int[] arg1, int arg2, int arg3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getElements(int arg0, int[] arg1, int arg2, int arg3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntList intSubList(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int removeInt(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int set(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void size(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntList subList(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

}
