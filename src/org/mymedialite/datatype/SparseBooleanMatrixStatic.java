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
import java.util.List;
import java.util.Map.Entry;

/**
 * Sparse representation of a boolean matrix, using binary search (memory efficient).
 * 
 * This data structure is static, which means that rows are represented as int arrays,
 * a can be assigned, but not modified.
 *
 * Fast row-wise access is possible.
 * Indexes are zero-based.
 * @version 2.03
 */
public class SparseBooleanMatrixStatic implements IBooleanMatrix {

  /**
   * Internal representation of this data: list of rows.
   */
  protected List<int[]> row_list = new ArrayList<int[]>();

  /**
   * 
   */
  @Override
  public Boolean get(int x, int y) {
    if (x < row_list.size())
      return Arrays.binarySearch(row_list.get(x), y) >= 0;
    else
      return false;
  }
  
  @Override
  public void set(int x, int y, Boolean value) {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  @Override
  public IntSet get(int x) {
    if (x >= row_list.size())
      return new IntArraySet(0);

    return new IntArraySet(row_list.get(x));
  }

  // TODO confirm int[] the correct type to use here rather than Collection<Integer>
  public void setRow(int x, int[] row) {
    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++)
        row_list.add(new int[0]);

    row_list.set(x, row);  
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
    return new SparseBooleanMatrixStatic();
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
    return new IntArrayList(row_list.get(row_id));
  }

  /**
   * 
   */
  @Override
  public int numEntriesByRow(int row_id) {
    return row_list.get(row_id).length;
  }

  /**
   * Takes O(N log(M)) worst-case time, where N is the number of rows and M is the number of columns.
   */
  @Override
  public IntList getEntriesByColumn(int column_id) {
    IntList list = new IntArrayList();
    for (int row_id = 0; row_id < numberOfRows(); row_id++)
      if (Arrays.binarySearch(row_list.get(row_id), column_id) >= 0)
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
      if (Arrays.binarySearch(row_list.get(row_id), column_id) >= 0)
        counter++;
    
    return counter;
  }

  /**
   * The non-empty rows of the matrix (the ones that contain at least one true entry), with their IDs.
   */
  public List<Pair<Integer, int[]>> getNonEmptyRows()  {
    ArrayList<Pair<Integer, int[]>> return_list = new ArrayList<Pair<Integer, int[]>>();
      for (int i = 0; i < row_list.size(); i++)
        if (row_list.get(i).length > 0)
          return_list.add(new Pair<Integer, int[]>(i, row_list.get(i)));
      return return_list;
  }

  /**
   * 
   */
  @Override
  public IntCollection nonEmptyRowIDs() {
    IntList row_ids = new IntArrayList();
      for (int i = 0; i < row_list.size(); i++)
        if (row_list.get(i).length > 0)
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
  public int numberOfRows() {
    return row_list.size();
  }

  /**
   * The number of columns in the matrix.
   * @return The number of columns in the matrix
   */
  @Override
  public int numberOfColumns() {
    int max_column_id = -1;
    for (int[] row : row_list)
      if (row.length > 0)
        for(int y: row) 
          max_column_id = Math.max(max_column_id, row[y]);

    return max_column_id + 1;
  }

  /**
   * The number of (true) entries.
   * @return The number of (true) entries
   */
  @Override
  public int numberOfEntries() {
    int n = 0;
    for (int[] row : row_list)
      n += row.length;
    return n;
  }

  /**
   * 
   */
  public void grow(int num_rows, int num_cols) {
    throw new UnsupportedOperationException();
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
