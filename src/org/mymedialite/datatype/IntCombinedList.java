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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Combines two List objects.
 * @version 2.03
 */
public class IntCombinedList implements IntList {

  IntList first;
  IntList second;

  /**
   * Create a new CombinedList object.
   * @param list1 first list
   * @param list2 second list
   */
  public IntCombinedList(IntList list1, IntList list2) {
    first = list1;
    second = list2;
  }

  @Override
  public Integer get(int index) {
    if (index < first.size())
      return first.get(index);
    else
      return second.get(index - first.size());
  }

  @Override
  public int getInt(int index) {
    if (index < first.size())
      return first.get(index);
    else
      return second.get(index - first.size());
  }

  @Override
  public int size() {
    return first.size() + second.size();
  }
  
  @Override
  public boolean add(int item) {
    return second.add(item);
  }

  @Override
  public void add(int index, int item) { 
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object item) {
    for (int i = 0; i < first.size(); i++)
      if (first.get(i).equals(item)) return true;
    
    for (int i = first.size(); i < first.size() + second.size(); i++)
      if (second.get(i - first.size()).equals(item)) return true;
    
    return false;
  }

  @Override
  public int indexOf(Object item) {
    throw new UnsupportedOperationException();}

  @Override
  public boolean remove(Object item) {
    throw new UnsupportedOperationException(); 
  }

  @Override
  public Integer remove(int index) {
    if (index < first.size())
      return first.remove(index);
    else
      return second.remove(index - first.size());
  }

  @Override
  public boolean addAll(Collection<? extends Integer> c) {
    return second.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Integer> c) {
    throw new UnsupportedOperationException(); 
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c)
      if (!contains(o)) return false;
    return true;
  }

  @Override
  public boolean isEmpty() {
    return first.isEmpty() && second.isEmpty();
  }

  @Override
  public IntListIterator iterator() { throw new UnsupportedOperationException(); }

  @Override
  public int lastIndexOf(Object o) {
    int lastIndex = second.lastIndexOf(o);
    if (lastIndex == -1) lastIndex = first.lastIndexOf(o);
    return lastIndex;
  }

  @Override
  public IntListIterator listIterator() {
    IntList list = new IntArrayList(first);
    list.addAll(second);
    return list.listIterator();
  }

  @Override
  public IntListIterator listIterator(int index) {
    IntList list = new IntArrayList();
    if (index < first.size()) {
      list.addAll(first.subList(index, first.size()));
      list.addAll(second);
    } else {
      list.addAll(second.subList(index, second.size()));
    }
    return list.listIterator();  
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
  public int set(int index, int element) {
    throw new UnsupportedOperationException(); 
  }

  // TODO Needs checking
  @Override
  public IntList subList(int fromIndex, int toIndex) {
    IntList list = new IntArrayList();
    if (fromIndex < first.size())
      list.addAll(first.subList(fromIndex, Math.min(first.size(), toIndex)));
    
    if(toIndex >= first.size())
      list.addAll(second.subList(Math.max(0, fromIndex - first.size()), toIndex - first.size()));

    return list;
  }

  @Override
  public Object[] toArray() {
    Object[] array = new Object[size()]; 
    System.arraycopy(first.toArray(), 0, array, 0, first.size());
    System.arraycopy(second.toArray(), 0, array, first.size(), second.size());
    return array;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E> E[] toArray(E[] a) {
    E[] firstArray  = (E[]) Arrays.copyOf(first.toArray(),  first.size(),  a.getClass());
    E[] secondArray = (E[]) Arrays.copyOf(second.toArray(), second.size(), a.getClass());    
    E[] array = (E[]) Arrays.copyOf(firstArray, size(), a.getClass());
    System.arraycopy(secondArray, 0, array, first.size(), second.size());
    return array;
  }

  @Override
  public boolean add(Integer e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void add(int index, Integer element) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Integer set(int index, Integer element) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int compareTo(List<? extends Integer> o) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean addAll(IntCollection arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean contains(int arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsAll(IntCollection arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public IntIterator intIterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean rem(int arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeAll(IntCollection arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean retainAll(IntCollection arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int[] toArray(int[] arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] toIntArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] toIntArray(int[] arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean addAll(IntList arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addAll(int arg0, IntCollection arg1) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addAll(int arg0, IntList arg1) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addElements(int arg0, int[] arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addElements(int arg0, int[] arg1, int arg2, int arg3) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void getElements(int arg0, int[] arg1, int arg2, int arg3) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int indexOf(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public IntListIterator intListIterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IntListIterator intListIterator(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IntList intSubList(int arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int lastIndexOf(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void removeElements(int arg0, int arg1) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int removeInt(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void size(int arg0) {
    // TODO Auto-generated method stub
    
  }

}
