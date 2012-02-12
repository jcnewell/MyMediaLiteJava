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

/**
 * Class for storing dense matrices.
 * 
 * The data is stored in row-major mode.
 * Indexes are zero-based.
 * 
 * @param <T> the type of the matrix entries
 * @version 2.03
 */
public class SymmetricMatrix<T> implements IMatrix<T> {

  /**
   * Data array: data is stored in columns..
   */
  protected Object[][] data;

  /**
   * Dimension, the number of rows and columns.
   */
  public int dim;

  /**
   * 
   */
  public boolean isSymmetric() {
    return true; 
  }

  /**
   * 
   */
  public int numberOfRows() {
    return dim; 
  }

  /**
   * 
   */
  public int numberOfColumns() { 
    return dim;
  }

  /**
   * Initializes a new instance of the SymmetricMatrix class.
   * @param dim the number of rows and columns
   * @param d the default value for elements, or null
   */
  public SymmetricMatrix(int dim,  T d) {
    if (dim < 0) throw new IllegalArgumentException("dim must be at least 0");
    this.dim = dim;
    this.data = new Object[dim][];
    for (int i = 0; i < dim; i++)
      data[i] = new Object[i + 1];
    
    if(d != null)
      for (int i = 0; i < dim; i++)
        for (int j = 0; j <= i; j++)
          data[i][j] = d;
  }

  /**
   * 
   */
  public IMatrix<T> createMatrix(int num_rows, int num_columns) {
    if (num_rows != num_columns)
      throw new IllegalArgumentException("num_rows must equal num_columns for symmetric matrices");
    return new SymmetricMatrix<T>(num_rows, null);
  }

  /**
   * Initialize the matrix with a default value
   * @param d the default value
   */
  public void init(T d) {
    for (int i = 0; i < dim; i++)
      for (int j = 0; j <= i; j++)
        data[i][j] = d;
  }

  /**
   * 
   */
  public IMatrix<T> transpose() {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public T get(int i, int j) {
    if (i >= j)
      return (T)data[i][j];
    else
      return (T)data[j][i];
  }

  public void set(int i, int j, T value) {
    if (i >= j)
      data[i][j] = value;
    else
      data[j][i] = value;
  }

  /**
   * 
   */
  public void grow(int num_rows, int num_columns) {
    if (num_rows != num_columns)
      throw new IllegalArgumentException("num_rows must equal num_columns for symmetric matrices");

    if (num_rows > dim) {

      // Create new data structure
      Object [][] new_data = new Object[num_rows][];
      for (int i = 0; i < num_rows; i++)
        new_data[i] = new Object[i + 1];

      for (int i = 0; i < dim; i++)
        for (int j = 0; j <= i; j++)
          new_data[i][j] = get(i, j);

      // Replace old data structure
      this.dim = num_rows;
      this.data = new_data;
    }
  }

}
