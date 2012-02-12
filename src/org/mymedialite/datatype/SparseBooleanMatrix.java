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
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collections;

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

  ArrayList<IntArraySet> row_list = new ArrayList<IntArraySet>();

  /** Default constructor */
  public SparseBooleanMatrix() {}

  /** {@inheritDoc} */
  public Boolean get(int x, int y) {
    if (x < row_list.size())
      return row_list.get(x).contains(y);
    else
      return false;
  }

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
  public IntSet get(int x) {
    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++)
        row_list.add(new IntArraySet());
    return row_list.get(x);
  }

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

  public IMatrix<Boolean> createMatrix(int x, int y) {
    return new SparseBooleanMatrix();
  }

  public IntList getEntriesByRow(int row_id) {
    return new IntArrayList(row_list.get(row_id));
  }

  public int numEntriesByRow(int row_id) {
    return row_list.get(row_id).size();
  }       

  /**
   *  Takes O(N) worst-case time, where N is the number of rows, if the internal hash table can be queried in constant time.
   */
  public IntList getEntriesByColumn(int column_id) {
    IntList list = new IntArrayList();
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (row_list.get(row_id).contains(column_id))
        list.add(row_id);
    return list;
  }     

  public int numEntriesByColumn(int column_id) {
    int count = 0;
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (row_list.get(row_id).contains(column_id))
        count++;
    return count;
  }

  /**
   * The IDs of the non-empty rows in the matrix (the ones that contain at least one true entry)
   */
  public IntCollection nonEmptyRowIDs() {
    IntSet row_ids = new IntArraySet();
    for (int i = 0; i < row_list.size(); i++)
      if (row_list.get(i).size() > 0)
        row_ids.add(i);
    return row_ids;
  }

  /**
   * Get the IDs of the non-empty columns in the matrix (the ones that contain at least one true entry)
   */
  public IntCollection nonEmptyColumnIDs() {
    IntSet col_ids = new IntArraySet();
    for (int i = 0; i < row_list.size(); i++)
      for (int id : row_list.get(i).toIntArray())
        col_ids.add(id);
    return col_ids;
  }

  //  public void setRow(int x, IntArraySet row) {
  //      row_list.set(x, row);
  //  }
  //
  //  public void addRow(int x, IntArraySet row) {
  //    row_list.add(x, row);
  //  }

  public int numberOfRows() {
    return row_list.size();
  }

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
  public int numberOfEntries() {
    int n = 0;
    for (IntSet row : row_list)
      n += row.size();
    return n;
  }

  //  /**
  //   * Add an entry to the matrix.
  //   * @param x the row ID
  //   * @param y the column ID
  //   */
  //  public void add(int x, int y) {
  //    IntArraySet row = getRow(x);
  //    row.add(y);
  //  }

  //  /**
  //   * Remove an entry from the matrix.
  //   * @param x the row ID
  //   * @param y the column ID
  //   */
  //  public void removeEntry(int x, int y) {
  //      getRow(x).remove(y);
  //  }

  /**
   * Removes a column, and fills the gap by decrementing all occurrences of higher column IDs by one.
   * @param y the column ID
   */
  public void removeColumn(int y) {
    for (int row_id = 0; row_id < row_list.size(); row_id++) {
      IntList cols = new IntArrayList(row_list.get(row_id));
      for (int col_id : cols) {
        if (col_id >= y)
          row_list.get(row_id).remove(y);
        if (col_id > y)
          row_list.get(row_id).add(col_id - 1);
      }
    }
  }

  /**
   * Removes several columns, and fills the gap by decrementing all occurrences of higher column IDs.
   * @param delete_columns an array with column IDs
   */
  public void removeColumn(int[] delete_columns) {
    for (int row_id = 0; row_id < row_list.size(); row_id++) {
      IntList cols = new IntArrayList(row_list.get(row_id));
      NEXT_COL:
        for (int col_id : cols) {
          int decrease_by = 0;
          for (int y : delete_columns) {
            if (col_id == y) {
              row_list.get(row_id).remove(y);
              continue NEXT_COL;
            }
            if (col_id > y)
              decrease_by++;
          }

          // Decrement column ID
          row_list.get(row_id).remove(col_id);
          row_list.get(row_id).add(col_id - decrease_by);
        }
    }
  }

  /**
   * Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged.
   * @return the transpose of the matrix (copy)
   */
  public IMatrix<Boolean> transpose() {
    SparseBooleanMatrix transpose = new SparseBooleanMatrix();
    for (int i = 0; i < row_list.size(); i++) {
      for(int j : this.get(i)) {
        transpose.set(j, i, true);
      }
    }
    return transpose;
  }

  public int overlap(IBooleanMatrix s) {
    int c = 0;

    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i).toIntArray())
        if (s.get(i, j))
          c++;
    return c;
  }

}
