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

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Proxy class that allows access to selected elements of an underlying list data structure.
 * @version 2.03
 */
public class ListProxy<T> implements List<T> {

  List<T> list;
  IntList indices;

  /**
   * Create a new ListProxy object.
   * @param list the list to proxy
   * @param indices an index list pointing to entries in the list
   */
  public ListProxy(List<T> list, IntList indices) {  // List<Integer>
    this.list = list;
    this.indices = indices;
  }

  /**
   */
  public T get(int index) {
    return list.get(indices.getInt(index));
  }

  public T set(int index, T value) {
    return list.set(indices.getInt(index), value);
  }

  /**
   * 
   */
  public int size() {
    return indices.size(); 
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

  /**
   * 
   */
  public boolean add(T item) {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  public boolean contains(Object item) {
    for (int index : indices)
      if (list.get(index).equals(item))
        return true;
    return false;
  }

  /**
   * 
   */
  public int indexOf(Object item) { throw new UnsupportedOperationException(); }

  /**
   * 
   */
  public boolean remove(Object item) { throw new UnsupportedOperationException(); }

  @Override
  public Iterator<T> iterator() {
    List<T> subList = new ArrayList<T>();
    for (int index : indices)
      subList.add(list.get(index));

    return list.iterator();
  }
  
  @Override
  public ListIterator<T> listIterator() {
    List<T> subList = new ArrayList<T>();
    for (int index : indices)
      subList.add(list.get(index));

    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    List<T> subList = new ArrayList<T>();
    for (int i = index; i<indices.size(); i++)
      subList.add(list.get(indices.getInt(i)));

    return list.listIterator();
  }

  @Override
  public void add(int arg0, T arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int arg0, Collection<? extends T> arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> arg0) {
    outer:
    for(Object item : arg0) {
      for (int index : indices) {
        if (list.get(index).equals(item))
          continue outer;
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
   return  indices.size() == 0;
  }

  @Override
  public int lastIndexOf(Object arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T remove(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<T> subList(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> E[] toArray(E[] arg0) {
    throw new UnsupportedOperationException();
  }

}
