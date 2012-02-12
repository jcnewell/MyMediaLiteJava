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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A symmetric sparse matrix; consumes less memory.
 * 
 * Be careful when accessing the matrix via the NonEmptyRows property: this contains
 * only the entries with x &gt; y, but not their symmetric counterparts.
 * @version 2.03
 */
public class SymmetricSparseMatrix<T> extends SparseMatrix<T> {

  private T d;
  
  /**
   * Create a symmetric sparse matrix with a given dimension.
   * @param dimension the dimension (number of rows/columns)
   * @param d the default value for elements, or null
   */
  public SymmetricSparseMatrix(int dimension, T d) {
    super(dimension, dimension, null);
    this.d = d;
  }

  /**
   * Access the elements of the sparse matrix.
   * @param x the row ID
   * @param y the column ID
   */
  public T get(int x, int y) {
    // Ensure x <= y
    if (x > y) {
      int tmp = x;
      x = y;
      y = tmp;
    }
    
    T result;
    if (x < row_list.size()) {
      result = row_list.get(x).get(y);
      if(result != null) {
        return result;
      }
    }
    return d;
  }

  public void set(int x, int y, T value) {
    // Ensure x <= y
    if (x > y) {
      int tmp = x;
      x = y;
      y = tmp;
    }

    if (x >= row_list.size())
      for (int i = row_list.size(); i <= x; i++)
        row_list.add(new HashMap<Integer, T>());

    row_list.get(x).put(y, value);
  }

  /**
   * Always true because the data type is symmetric.
   * @return Always true because the data type is symmetric
   */
  public boolean isSymmetric() {
    return true;
  }

  /**
   * 
   */
  public IMatrix<T> createMatrix(int num_rows, int num_columns) {
    if (num_rows != num_columns)
      throw new IllegalArgumentException("Symmetric matrices must have the same number of rows and columns.");
    return new SymmetricSparseMatrix<T>(num_rows, null);
  }

  /**
   * 
   */
  public List<Pair<Integer, Integer>> nonEmptyEntryIDs() {
    List<Pair<Integer, Integer>> return_list = new ArrayList<Pair<Integer, Integer>>();   
    for (int row_id : nonEmptyRows().keySet()) {
      HashMap<Integer, T> row = row_list.get(row_id);
      for (int col_id : row.keySet()) {
        return_list.add(new Pair<Integer, Integer>(row_id, col_id));
        if (row_id != col_id)
          return_list.add(new Pair<Integer, Integer>(col_id, row_id));
      }
    }
    return return_list;
  }

  /**
   * 
   */
  public int numberOfNonEmptyEntries() {
    int counter = 0;
    for (int i = 0; i < row_list.size(); i++) {
      counter += 2 * row_list.get(i).size();

      // Adjust for diagonal elements
      if (row_list.get(i).containsKey(i))
        counter--;
    }
    return counter;
  }
  
}


