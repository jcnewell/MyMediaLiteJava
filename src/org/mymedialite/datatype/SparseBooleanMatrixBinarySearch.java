// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011, 2012 Zeno Gantner, Chris Newell
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Sparse representation of a boolean matrix, using binary search (memory efficient).
 * 
 * Fast row-wise access is possible.
 * Indexes are zero-based.
 * @version 2.03
 */
public class SparseBooleanMatrixBinarySearch implements IBooleanMatrix {

  /**
   * Internal representation of this data: list of rows.
   */
  protected List<IntList> row_list = new ArrayList<IntList>();

  /**
   */
  @Override
  public Boolean get(int x, int y) {

    if (x < row_list.size())
      return Arrays.binarySearch(row_list.get(x).toIntArray(), y) >= 0;
      else
        return false;
  }

  @Override
  public void set(int x, int y, Boolean value) {    
    if (value) {
      int index = Arrays.binarySearch(get(x).toIntArray(), y);
      if (index < 0) // ... and not there yet
        row_list.get(x).set(~index, y);

    } else if (row_list.size() > x && Collections.binarySearch(row_list.get(x), y) >= 0) // if false
      row_list.get(x).remove(y);
  }

  /**
   * 
   */
  @Override
  public IntCollection get(int x) { 
    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++)
        row_list.add(new IntArrayList());

    return row_list.get(x);
  }

  /**
   * 
   */
  @Override
  public boolean isSymmetric() {
    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i))
        if (!get(j, i))
          return false;

    return true;
  }

  /**
   * 
   */
  @Override
  public IMatrix<Boolean> createMatrix(int x, int y) {
    return new SparseBooleanMatrixBinarySearch();
  }
  
  @Override
  public void init(Boolean d) {
    throw new UnsupportedOperationException("SparseMatrices cannot be initialized with default values.");
  }

  /**
   * 
   */
  @Override
  public IntList getEntriesByRow(int row_id) {
    return row_list.get(row_id);
  }

  /**
   * 
   */
  @Override
  public int numEntriesByRow(int row_id) {
    return row_list.get(row_id).size();
  }

  /**
   * Takes O(N log(M)) worst-case time, where N is the number of rows and M is the number of columns.
   */
  @Override
  public IntList getEntriesByColumn(int column_id) {
    IntList list = new IntArrayList();
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (Collections.binarySearch(row_list.get(row_id), column_id) >= 0)
        list.add(row_id);

    return list;
  }

  /**
   * 
   */
  @Override
  public int numEntriesByColumn(int column_id) {
    int counter = 0;

    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (Collections.binarySearch(row_list.get(row_id), column_id) >= 0)
        counter++;
    return counter;
  }


  /**
   * The non-empty rows of the matrix (the ones that contain at least one true entry), with their IDs.
   * @return The non-empty rows of the matrix (the ones that contain at least one true entry), with their IDs
   */
  public List<Pair<Integer, int[]>> getNonEmptyRows()  {
    ArrayList<Pair<Integer, int[]>> return_list = new ArrayList<Pair<Integer, int[]>>();
    for (int i = 0; i < row_list.size(); i++)
      if (row_list.get(i).size() > 0)
        return_list.add(new Pair<Integer, int[]>(i, row_list.get(i).toIntArray()));

    return return_list;
  }


  /**
   * 
   */
  @Override
  public IntCollection nonEmptyRowIDs() {
    IntList row_ids = new IntArrayList();
    for (int i = 0; i < row_list.size(); i++)
      if (row_list.get(i).size() > 0)
        row_ids.add(i);

    return row_ids;
  }

  /**
   * 
   */
  @Override
  public IntCollection nonEmptyColumnIDs() {
    IntSet col_ids = new IntArraySet();
    // Iterate over the complete data structure to find column IDs
    for (int i = 0; i < row_list.size(); i++)
      for (int id : row_list.get(i))
        col_ids.add(id);

    return col_ids;
  }

  /**
   * The number of rows in the matrix.
   * @return The number of rows in the matrix
   */
  @Override
  public int numberOfRows()	{
    return row_list.size();
  }

  /**
   * The number of columns in the matrix.
   * @return The number of columns in the matrix
   */
  @Override
  public int numberOfColumns() {
    int max_column_id = -1;
    for (IntList row : row_list)
      if (row.size() > 0)
        for(int y: row) 
          max_column_id = Math.max(max_column_id, row.get(y));

    return max_column_id + 1;
  }

  /**
   * The number of (true) entries.
   * @return The number of (true) entries
   */
  @Override
  public int numberOfEntries() {
    int n = 0;
    for (IntList row : row_list)
      n += row.size();
    return n;
  }

  /**
   * 
   */
  public void grow(int num_rows, int num_cols) {
    // If necessary, grow rows
    if (num_rows > numberOfRows())
      for (int i = row_list.size(); i < num_rows; i++)
        row_list.add(new IntArrayList());
  }

  /**
   * Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged.
   * @return the transpose of the matrix
   */
  @Override
  public IMatrix<Boolean> transpose() {
    SparseBooleanMatrixBinarySearch transpose = new SparseBooleanMatrixBinarySearch();
    for (int i = 0; i < row_list.size(); i++)
      for (int j : get(i))
        transpose.set(j, i, true);
    return transpose;
  }

  /**
   * 
   */
  @Override
  public int overlap(IBooleanMatrix s) {
    int c = 0;
    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i))
        if (s.get(i, j))
          c++;

    return c;
  }
  
}
