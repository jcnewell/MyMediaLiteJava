// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.datatype;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.mymedialite.datatype.IMatrix;

/**
 * Sparse representation of a boolean matrix, using HashSets.
 * Fast row-wise access is possible.
 * Indexes are zero-based.
 * 
 * TODO Implement the classes below.
 * If you need a more memory-efficient data structure, try SparseBooleanMatrixBinarySearch
 * or SparseBooleanMatrixStatic.
 * @version 2.03
 */
public class SparseBooleanMatrix implements IBooleanMatrix {

  ArrayList<IntSet> row_list = new ArrayList<IntSet>();

  /** Default constructor */
  public SparseBooleanMatrix() {}

  @Override
  public Boolean get(int x, int y) {
    if (x < row_list.size())
      return row_list.get(x).contains(y);
    else
      return false;
  }

  @Override
  public void set(int x, int y, Boolean value) {
    if (value)
      get(x).add(y);
    else
      get(x).remove(y);
  }

  /**
   * Get a row.
   * @param x the row ID
   * @return the row
   */
  @Override
  public IntSet get(int x) {
    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++)
        row_list.add(new IntOpenHashSet());
    return row_list.get(x);
  }

  @Override
  public boolean isSymmetric() {
    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i).toIntArray()) {
        if (i > j)
          continue;  // check every pair only once
        if (!get(j, i))
          return false;
      }
    return true;
  }

  @Override
  public IMatrix<Boolean> createMatrix(int x, int y) {
    return new SparseBooleanMatrix();
  }

  @Override
  public IntList getEntriesByRow(int row_id) {
    return new IntArrayList(row_list.get(row_id));
  }

  @Override
  public int numEntriesByRow(int row_id) {
    return row_list.get(row_id).size();
  }       

  /**
   *  Takes O(N) worst-case time, where N is the number of rows, if the internal hash table can be queried in constant time.
   */
  @Override
  public IntList getEntriesByColumn(int column_id) {
    IntList list = new IntArrayList();
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (row_list.get(row_id).contains(column_id))
        list.add(row_id);
    return list;
  }     

  @Override
  public int numEntriesByColumn(int column_id) {
    int count = 0;
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (row_list.get(row_id).contains(column_id))
        count++;
    return count;
  }

  /**
   * The non-empty rows of the matrix (the ones that contain at least one true entry), with their IDs.
   * @return The non-empty rows of the matrix (the ones that contain at least one true entry), with their IDs
   */
  public HashMap<Integer, IntSet> nonEmptyRows() {
    HashMap<Integer, IntSet> return_list = new HashMap<Integer, IntSet>();
    for(int i=0; i < row_list.size(); i++) {
      IntSet row = get(i);
      if(row.size() > 0)
        return_list.put(i, row);
    }
    return return_list;
  }

  /**
   * The IDs of the non-empty rows in the matrix (the ones that contain at least one true entry)
   */
  @Override
  public IntCollection nonEmptyRowIDs() {
    IntSet row_ids = new IntOpenHashSet();
    for (int i = 0; i < row_list.size(); i++)
      if (row_list.get(i).size() > 0)
        row_ids.add(i);
    return row_ids;
  }

  /**
   * Get the IDs of the non-empty columns in the matrix (the ones that contain at least one true entry)
   */
  @Override
  public IntCollection nonEmptyColumnIDs() {
    IntSet col_ids = new IntOpenHashSet();
    for (int i = 0; i < row_list.size(); i++)
      for (int id : row_list.get(i).toIntArray())
        col_ids.add(id);
    return col_ids;
  }

  @Override
  public int numberOfRows() {
    return row_list.size();
  }

  @Override
  public int numberOfColumns() {
    int max_column_id = -1;
    for (IntSet row : row_list)
      if(row.size() > 0) 
        max_column_id = Math.max(max_column_id, Collections.max(row));

    return max_column_id + 1;
  }

  /**
   * Returns the number of (true) entries.
   */
  @Override
  public int numberOfEntries() {
    int n = 0;
    for (IntSet row : row_list)
      n += row.size();
    return n;
  }

  @Override
  public void grow(int num_rows, int num_cols) {
    // If necessary, grow rows
    if (num_rows > numberOfRows())
      for (int i = row_list.size(); i < num_rows; i++)
        row_list.add(new IntOpenHashSet());
  }

  /**
   * Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged.
   * @return the transpose of the matrix (copy)
   */
  @Override
  public IMatrix<Boolean> transpose() {
    SparseBooleanMatrix transpose = new SparseBooleanMatrix();
    for (int i = 0; i < row_list.size(); i++) {
      for(int j : this.get(i)) {
        transpose.set(j, i, true);
      }
    }
    return transpose;
  }

  @Override
  public int overlap(IBooleanMatrix s) {
    int c = 0;

    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i).toIntArray())
        if (s.get(i, j))
          c++;
    return c;
  }

}
