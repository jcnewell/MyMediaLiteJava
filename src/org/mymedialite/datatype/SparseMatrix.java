// Copyright (C) 2010, 2011 Zeno Gantner
// Copyright (C) 2011 Chris Newell
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

import java.util.*;

/**
 * Class for storing sparse matrices.
 * The data is stored in row-major mode.
 * Indexes are zero-based.
 * T the matrix element type, must have a default constructor/value
 * @version 2.03
 */
public class SparseMatrix<T> implements IMatrix<T> {

  private int numberOfColumns;

  /**
   * List that stores the rows of the matrix.
   */
  protected List<HashMap<Integer, T>> row_list = new ArrayList<HashMap<Integer, T>>();

  /**
   * The default values for elements.
   */
  private T d = null;

  /**
   * Create a sparse matrix with a given number of rows.
   * @param num_rows the number of rows
   * @param num_cols the number of columns
   */
  public SparseMatrix(int num_rows, int num_cols) {
    this(num_rows, num_cols, null);
  }

  /**
   * Create a sparse matrix with a given number of rows.
   * @param num_rows the number of rows
   * @param num_cols the number of columns
   * @param d the default value for elements
   */
  public SparseMatrix(int num_rows, int num_cols, T d) {
    for (int i = 0; i < num_rows; i++) {
      row_list.add(new HashMap<Integer, T>());
    }
    this.numberOfColumns = num_cols;
    this.d = d;
  }

  /**
   * 
   */
  @Override
  public IMatrix<T> createMatrix(int num_rows, int num_columns) {
    return new SparseMatrix<T>(num_rows, num_columns, null);
  }

  /**
   * 
   */
  @Override
  public boolean isSymmetric() {
    if (numberOfRows() != numberOfColumns()) return false;
    for (int i = 0; i < row_list.size(); i++)
      for (int j : row_list.get(i).keySet()) {
        if (i > j)
          continue; // check every pair only once

        if (! get(i, j).equals(get(j, i)))
          return false;
      }
    return true;
  }

  /**
   * 
   */
  @Override
  public int numberOfRows() {
    return row_list.size();
  }

  /**
   * 
   */
  @Override
  public int numberOfColumns() {
    return numberOfColumns;
  }

  /**
   * 
   */
  @Override
  public IMatrix<T> transpose() {
    SparseMatrix<T> transpose = new SparseMatrix<T>(numberOfColumns(), numberOfRows());
    for (Pair<Integer, Integer> p : nonEmptyEntryIDs()) {
      transpose.set(p.second, p.first, get(p.first, p.second));
    }
    return transpose;
  }

  /**
   * Get a row of the matrix.
   * @param x the row ID
   */
  public HashMap<Integer, T> get(int x) {
    if (x >= row_list.size())
      return new HashMap<Integer, T>();
    else 
      return row_list.get(x);
  }

  /**
   * Access the elements of the sparse matrix.
   * @param x the row ID
   * @param y the column ID
   */
  @Override
  public T get(int x, int y) {
    T result;
    if (x < row_list.size()) {
      result = row_list.get(x).get(y);
      if(result != null) {
        return result;
      }
    }
    return d;
  }

  @Override
  public void set(int x, int y, T value) {
    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++) row_list.add(new HashMap<Integer, T>());

    row_list.get(x).put(y, value);
  }

  /**
   * 
   * The non-empty rows of the matrix (the ones that contain at least one non-zero entry),
   * with their IDs
   * .
   */
  public HashMap<Integer, HashMap<Integer, T>> nonEmptyRows() {
    HashMap<Integer, HashMap<Integer, T>> return_list = new HashMap<Integer, HashMap<Integer, T>>();
    for(int i=0; i < row_list.size(); i++) {
      HashMap<Integer, T> row = get(i);
      if(row.size() > 0)
        return_list.put(i, row);
    }
    return return_list;
  }

  /**
   * The row and column IDs of non-empty entries in the matrix.
   * @return The row and column IDs of non-empty entries in the matrix
   */
  public List<Pair<Integer, Integer>> nonEmptyEntryIDs() {
    List <Pair<Integer, Integer>> return_list = new ArrayList<Pair<Integer, Integer>>();
    for (Map.Entry<Integer, HashMap<Integer, T>> id_row : nonEmptyRows().entrySet())
      for (Integer col_id : id_row.getValue().keySet())
        return_list.add(new Pair<Integer, Integer>(id_row.getKey(), col_id));
    return return_list;
  }

  /**
   * The number of non-empty entries in the matrix.
   * @return The number of non-empty entries in the matrix
   */
  public int numberOfNonEmptyEntries() {
    int counter = 0;
    for (HashMap<Integer, T> row : row_list)
      counter += row.size();
    return counter;
  }

}

