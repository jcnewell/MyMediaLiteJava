// Copyright (C) 2011 Zeno Gantner
// Copyright (C) 2012 Zeno Gantner, Chris Newell
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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.datatype;

import java.util.HashMap;

/**
 * A skew symmetric (anti-symmetric) sparse matrix; consumes less memory.
 * 
 * Be careful when accessing the matrix via the NonEmptyEntryIDs and
 * NonEmptyRows properties: these contain only the entries with x &gt; y,
 * but not their antisymmetric counterparts.
 *  * @version 2.03
 */
public class SkewSymmetricSparseMatrix extends SymmetricSparseMatrix<Float> {

  /**
   * Create a skew symmetric sparse matrix with a given dimension.
   * @param dimension the dimension (number of rows/columns)
   */
  public SkewSymmetricSparseMatrix(int dimension) {
    super(dimension);
  }
  
  /**
   * Get an element of the sparse matrix.
   * @param x the row ID
   * @param y the column ID
   * @return the value
   */
  public Float get(int x, int y) {
    if (x < y) {      
      if (x < row_list.size()) {
        Float result = row_list.get(x).get(y);
        if(result != null) return result;
      }
    } else if (x > y) {
      if (y < row_list.size()) {  
        Float result = row_list.get(y).get(x);
        if(result != null) return -result;  // minus for anti-symmetry
      }
    }
    return 0.0F;
  }

  /**
   * Set an element of the sparse matrix.
   * @param x the row ID
   * @param y the column ID
   * @param value the value to set
   */
  public void set(int x, int y, Float value) {
    if (x < y) {
      if (x >= row_list.size())
        for (int i = row_list.size(); i <= x; i++)
          row_list.add(new HashMap<Integer, Float>() );

      row_list.get(x).put(y, value);
    } else if (x > y) {
      if (y >= row_list.size())
        for (int i = row_list.size(); i <= y; i++)
          row_list.add(new HashMap<Integer, Float>() );

      row_list.get(y).put(x, -value);
    } else {
      // All elements on the diagonal must be zero
      if (value != 0)
        throw new IllegalArgumentException("Elements of the diagonal of a skew symmetric matrix must equal 0");
    }
  }


  /**
   * Only true if all entries are zero.
   * @return true only if all entries are zero
   */
  public boolean isSymmetric() {
    for (int i = 0; i < row_list.size(); i++)
      for (Integer j : row_list.get(i).keySet())
        if (get(i, j) != 0)
          return false;
    return true;
  }

  /**
   */
  public IMatrix<Float> createMatrix(int num_rows, int num_columns) {
    if (num_rows != num_columns)
      throw new IllegalArgumentException("Skew symmetric matrices must have the same number of rows and columns.");
    return new SkewSymmetricSparseMatrix(num_rows);
  }
  
}
