//Copyright (C) 2010 Steffen Rendle, Zeno Gantner, Chris Newell
//Copyright (C) 2011 Zeno Gantner
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
import java.util.Arrays;
import java.util.List;

/**
 * Class for storing dense matrices.
 * The data is stored in row-major mode.
 * Indexes are zero-based.
 * @param <T> the type of the matrix entries
 */
public class Matrix<T> implements IMatrix<T>  {

	/** Data array: data is stored in columns. */
	public Object[] data;

	/** Dimension 1, the number of rows */
	public int dim1;

	/** Dimension 2, the number of columns */
	public int dim2;

	/**
	 * Initializes a new instance of the Matrix class
	 * @param dim1 the number of rows
	 * @param dim2 the number of columns
	 */
	public Matrix(int dim1, int dim2) {
		if (dim1 < 0)
			throw new IllegalArgumentException("dim1 must be at least 0");
		if (dim2 < 0)
			throw new IllegalArgumentException("dim2 must be at least 0");

		this.dim1 = dim1;
		this.dim2 = dim2;
		this.data = new Object[dim1 * dim2];
	}

	/**
	 * Copy constructor. Creates a deep copy of the given matrix.
	 * @param matrix the matrix to be copied
	 */
	public Matrix(Matrix<T> matrix) {
		this.dim1 = matrix.dim1;
		this.dim2 = matrix.dim2;
		this.data = Arrays.copyOf(matrix.data, matrix.data.length);
	}

	/**
	 * Constructor that takes a list of lists to initialize the matrix.
	 * @param data a list of lists of T
	 */
	public Matrix(List<List<T>> data) {
		this.dim1 = data.size();
		this.dim2 = data.get(0).size();
		this.data = new Object[dim1 * dim2];
		for (int i = 0; i < dim1; i++)
			for (int j = 0; j < dim2; j++)
				this.data[i * dim2 + j] = data.get(i).get(j);
	}

	public IMatrix<T> createMatrix(int num_rows, int num_columns) {
		return new Matrix<T>(num_rows, num_columns);
	}

	public IMatrix<T> transpose() {
		Matrix<T> transpose = new Matrix<T>(dim2, dim1);
		for (int i = 0; i < dim1; i++)
			for (int j = 0; j < dim2; j++)
				transpose.data[j * dim1 + i] = data[i * dim2 + j];
		return transpose;
	}

	public int getNumberOfRows() {
		return dim1;
	}

	public int getNumberOfColumns() { 
		return dim2;
	}

	@SuppressWarnings({"unchecked"})
	public T get(int i, int j) {
		// TODO deactivate in production code
		if (i >= this.dim1)
			throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + this.dim1);
		if (j >= this.dim2)
			throw new IllegalArgumentException("j too big: " + j + ", dim2 is " + this.dim2);

		return (T) data[i * dim2 + j];    
	}

	public void set(int i, int j, T value) {
		data[i * dim2 + j] = value;
	}

	public boolean isSymmetric() {
		if (dim1 != dim2)
			return false;
		for (int i = 0; i < dim1; i++)
			for (int j = i + 1; j < dim2; j++)
				if (!get(i, j).equals(get(j, i)))
					return false;
		return true;
	}

	/**
	 * Returns a copy of the i-th row of the matrix
	 * @param i the row ID
	 * @return a List<T> containing the row data
	 */
	//  @SuppressWarnings({"unchecked"})
	//  public T[] getRow(int i) {
	//    Object[] row = new Object[this.dim2];
	//    for (int x = 0; x < this.dim2; x++) {
	//      row[x] = get(i, x);
	//    }
	//    return (T[]) row;
	//  }
	// TODO Is it really necessary to switch to list here and in getColumn? Where is this method used?
	public List<T> getRow(int i) {
		List<T> row = new ArrayList<T>(this.dim2);
		for (int x = 0; x < this.dim2; x++)
			row.set(x, get(i, x));
		return row;
	}

	/**
	 * Returns a copy of the j-th column of the matrix
	 * @param j the column ID
	 * @return T[] containing the column data
	 */
	//  @SuppressWarnings({"unchecked"})
	//  public T[] getColumn(int j) {
	//    Object[] column = new Object[this.dim1];
	//    for (int x = 0; x < this.dim1; x++) {
	//      column[x] = get(x, j);
	//    }
	//    return (T[]) column;
	//  }
	public List<T> getColumn(int j) {
		List<T> column = new ArrayList<T>(this.dim1);
		for (int x = 0; x < this.dim1; x++)
			column.set(x, get(x, j));
		return column;
	}

	/**
	 * Sets the values of the i-th row to the values in a given array
	 * @param i the row ID
	 * @param row A of length dim1
	 */
	//  public void setRow(int i, T[] row) {
	//    if (row.length != this.dim2)  
	//      throw new IllegalArgumentException("Array length " + row.length + " must equal number of columns " + this.dim2);
	//    for (int j = 0; j < this.dim2; j++) {
	//      set(i, j, row[j]);
	//    }
	//  }
	public void setRow(int i, List<T> row) {
		if (row.size() != this.dim2)  
			throw new IllegalArgumentException("Array length " + row.size() + " must equal number of columns " + this.dim2);
		for (int j = 0; j < this.dim2; j++)
			set(i, j, row.get(j));
	}

	/**
	 * Sets the values of the j-th column to the values in a given array
	 * @param j the column ID
	 * @param column A T[] of length dim2
	 */
	//  public void setColumn(int j, T[] column) {
	//    if (column.length != this.dim1)
	//      throw new IllegalArgumentException("Array length " + column.length + " must equal number of rows " + this.dim1);
	//    for (int i = 0; i < this.dim1; i++) {
	//      set(i, j, column[i]);
	//    }
	//  }
	public void setColumn(int j, List<T> column) {
		if (column.size() != this.dim1)
			throw new IllegalArgumentException("Array length " + column.size() + " must equal number of rows " + this.dim1);
		for (int i = 0; i < this.dim1; i++)
			set(i, j, column.get(i));
	}

	/**
	 * Initialize the matrix with a default value
	 * @param d the default value
	 */
	public void init(T d) {
		for (int i = 0; i < dim1 * dim2; i++)
			data[i] = d;
	}

	/**
	 * Enlarges the matrix to num_rows rows 
	 * Do nothing if num_rows is less than dim1.
	 * The new entries are filled with zeros.
	 * @param num_rows the minimum number of rows
	 */
	public void addRows(int num_rows) {
		if (num_rows > dim1) {
			// create new data structure
			Object[] data_new = new Object[num_rows * dim2];
			System.arraycopy(data, 0, data_new, 0, data.length);

			// replace old data structure
			this.dim1 = num_rows;
			this.data = data_new;
		}
	}

	/**
	 * Grows the matrix to the requested size, if necessary 
	 * The new entries are filled with zeros.
	 * @param num_rows the minimum number of rows
	 * @param num_cols the minimum number of columns
	 */
	public void grow(int num_rows, int num_cols) {
		if (num_rows > dim1 || num_cols > dim2) {
			// create new data structure
			Object[] new_data = new Object[num_rows * num_cols];
			for (int i = 0; i < dim1; i++) {
				for (int j = 0; j < dim2; j++) {
					new_data[i * num_cols + j] = get(i, j);
				}
			}
			// replace old data structure
			this.dim1 = num_rows;
			this.dim2 = num_cols;
			this.data = new_data;
		}
	}

	/**
	 * Sets an entire row to a specified value
	 * @param v the value to be used
	 * @param i the row ID
	 */
	public void setRowToOneValue(int i, T v) {
		for (int j = 0; j < dim2; j++) {
			set(i, j, v);
		}
	}

	/**
	 * Sets an entire column to a specified value 
	 * @param v the value to be used
	 * @param j the column ID
	 */
	public void setColumnToOneValue(int j, T v) {
		for (int i = 0; i < dim1; i++) {
			set(i, j, v);
		}
	}
}