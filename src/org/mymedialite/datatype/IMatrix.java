//Copyright (C) 2011 Zeno Gantner, Chris Newell
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

/**
 * Generic interface for matrix data types
 * @version 2.03
 */
public interface IMatrix<T> {

  /** 
   * Get the value at (i,j)
   * @param x the row ID
   * @param y the column ID
   * @return the value at (i,j)
   */
  T get(int x, int y);

  /** 
   * Set the value at (i,j)
   * @param x the row ID
   * @param y the column ID
   * @param value the value
   */
  void set(int x, int y, T value);
  
  /** 
   * Get the number of rows of the matrix.
   * @return the number of rows of the matrix
   */
  int numberOfRows();

  /** 
   * Get the number of columns of the matrix.
   * @return rhe number of columns of the matrix
   */
  int numberOfColumns();

  /**
   * True if the matrix is symmetric, false otherwise.
   * @return true if the matrix is symmetric, false otherwise
   */
  boolean isSymmetric();
  
  /**
   * Get the transpose of the matrix, i.e. a matrix where rows and columns are interchanged.
   * @return the transpose of the matrix (copy)
   */
  IMatrix<T> transpose();

  /**
   * Create a matrix with a given number of rows and columns.
   * @param num_rows the number of rows
   * @param num_columns the number of columns
   * @return a matrix with num_rows rows and num_column columns
   */
  IMatrix<T> createMatrix(int num_rows, int num_columns);
  
  /**
   * Grows the matrix to the requested size, if necessary.
   * The new entries are filled with zeros.
   * @param numRows the minimum number of rows
   * @param numCols the minimum number of columns
   */
  void grow(int numRows, int numCols);

}