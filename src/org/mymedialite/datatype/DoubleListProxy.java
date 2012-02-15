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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
public class DoubleListProxy implements DoubleList {

  DoubleList list;
  IntList indices;

  /**
   * Create a new DoubleListProxy object.
   * @param list the list to proxy
   * @param indices an index list pointing to entries in the list
   */
  public DoubleListProxy(DoubleList list, IntList indices) {
    this.list = list;
    this.indices = indices;
  }

  @Override
  public Double get(int index) {
    return list.get(indices.getInt(index));
  }

  @Override
  public double getDouble(int index) {
    return list.getDouble(indices.getInt(index));
  }
  
  @Override
  public double set(int index, double value) {
    return list.set(indices.getInt(index), value);
  }

  @Override
  public int size() {
    return indices.size(); 
  }

  public boolean isReadOnly() { 
    return true;
  }

  public boolean isFixedSize() { 
    return true; 
  }
  
  @Override
  public DoubleListIterator listIterator() {
    DoubleList subList = new DoubleArrayList();
    for (int index : indices)
      subList.add(list.getDouble(index));

    return subList.listIterator();
  }

  @Override
  public DoubleListIterator listIterator(int index) {
    DoubleList subList = new DoubleArrayList();
    for (int i = index; i<indices.size(); i++)
      subList.add(list.getDouble(indices.getInt(i)));

    return subList.listIterator();
  }
  
  @Override
  public DoubleListIterator doubleListIterator() {
    DoubleList subList = new DoubleArrayList();
    for (int index : indices)
      subList.add(list.getDouble(index));

    return subList.listIterator();
  }

  @Override
  public DoubleListIterator doubleListIterator(int index) {
    DoubleList subList = new DoubleArrayList();
    for (int i = index; i<indices.size(); i++)
      subList.add(list.getDouble(indices.getInt(i)));

    return subList.listIterator();
  }
  
  @Override
  public DoubleListIterator iterator() {
    DoubleList subList = new DoubleArrayList();
    for (int index : indices)
      subList.add(list.getDouble(index));

    return subList.iterator();
  }
  
  @Override
  public DoubleIterator doubleIterator() {
    DoubleList subList = new DoubleArrayList();
    for (int index : indices)
      subList.add(list.getDouble(index));

    return subList.iterator();
  }

  @Override
  public boolean add(Double e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, Double element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends Double> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends Double> c) {
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
  public Double remove(int index) {
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
  public Double set(int index, Double element) {
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
  public int compareTo(List<? extends Double> o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(DoubleCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(double arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(DoubleCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean rem(double arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(DoubleCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(DoubleCollection arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double[] toArray(double[] arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double[] toDoubleArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double[] toDoubleArray(double[] arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(double arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int arg0, double arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(DoubleList arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int arg0, DoubleCollection arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int arg0, DoubleList arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addElements(int arg0, double[] arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addElements(int arg0, double[] arg1, int arg2, int arg3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleList doubleSubList(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getElements(int arg0, double[] arg1, int arg2, int arg3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(double arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf(double arg0) {
    throw new UnsupportedOperationException();
  }



  @Override
  public double removeDouble(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeElements(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void size(int arg0) {
    throw new UnsupportedOperationException();
    
  }

  @Override
  public DoubleList subList(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

}
