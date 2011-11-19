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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class for storing sparse matrices.
 * The data is stored in row-major mode.
 * Indexes are zero-based.
 * <typeparam name="T the matrix element type, must have a default constructor/value</typeparam>
 */
public class SparseMatrix<T> implements IMatrix<T> { // where T:new() {

	private int numberOfColumns;

	/**
	 * List that stores the rows of the matrix.
	 */
	protected List<HashMap<Integer, T>> row_list = new ArrayList<HashMap<Integer, T>>();

	/**
	 * Create a sparse matrix with a given number of rows.
	 * @param num_rows the number of rows
	 * @param num_cols the number of columns
	 */
	public SparseMatrix(int num_rows, int num_cols) {
		for (int i = 0; i < num_rows; i++) {
			row_list.add(new HashMap<Integer, T>());
		}
		this.numberOfColumns = num_cols;
	}

	/**
	 * 
	 */
	public IMatrix<T> createMatrix(int num_rows, int num_columns) {
		return new SparseMatrix<T>(num_rows, num_columns);
	}

	/**
	 * 
	 */
	public boolean isSymmetric() {
		//    if (getNumberOfRows() != getNumberOfColumns()) return false;
		//    for (int i = 0; i < row_list.size(); i++)
		//      for (var j : row_list[i].keys)
		//      {
		//        if (i > j)
		//          continue; // check every pair only once
		//
		//        if (! this[i, j].equals(this[j, i]))
		//          return false;
		//      }
		return true;
	}

	/**
	 * 
	 */
	public int getNumberOfRows() {
		return row_list.size();
	}

	/**
	 * 
	 */
	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	/**
	 * 
	 */
	public IMatrix<T> transpose() {
		//    SparseMatrix<T> transpose = new SparseMatrix<T>(NumberOfColumns, NumberOfRows);
		//    for (Pair<Integer, int> p : NonEmptyEntryIDs) {
		//      transpose[p.second, p.first] = this[p.first, p.second];
		//    }
		//    return transpose;
		return null;
	}

	/**
	 * Get a row of the matrix.
	 * @param x the row ID
	 */
	public HashMap<Integer, T> get(int x) {
		if (x >= row_list.size()) {
			return new HashMap<Integer, T>();
		} else { 
			return row_list.get(x);
		}
	}

	/**
	 * Access the elements of the sparse matrix.
	 * @param x the row ID
	 * @param y the column ID
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y) {
		T result;
		if (x < row_list.size()) {
			result = row_list.get(x).get(y);
			if(result != null) {
				return result;
			}
		}
		return (T)(new Object());
	}

	public void set(int x, int y, T value) {
		if (x >= row_list.size()) {
			for (int i = row_list.size(); i <= x; i++) row_list.add(new HashMap<Integer, T>());
		}
		row_list.get(x).put(y, value);
	}

	/**
	 * 
	 * The non-empty rows of the matrix (the ones that contain at least one non-zero entry),
	 * with their IDs
	 * .
	 */
	public HashMap<Integer, HashMap<Integer, T>> getNonEmptyRows() {
		HashMap<Integer, HashMap<Integer, T>> return_list = new HashMap<Integer, HashMap<Integer, T>>();
		for(int i=0; i < row_list.size(); i++) {
			HashMap<Integer, T> row = get(i);
			if(row.size() > 0) {
				return_list.put(i, row);
			}
		}
		return return_list;
	}

	/**
	 * The row and column IDs of non-empty entries in the matrix.
	 * @return The row and column IDs of non-empty entries in the matrix
	 */
	public List<Pair<Integer, Integer>> getNonEmptyEntryIDs() {
		List <Pair<Integer, Integer>> return_list = new ArrayList<Pair<Integer, Integer>>();
		for (Entry<Integer, HashMap<Integer, T>> id_row : getNonEmptyRows().entrySet()) {
			for (Integer col_id : id_row.getValue().keySet()) {
				return_list.add(new Pair<Integer, Integer>(id_row.getKey(), col_id));
			}
		}
		return return_list;
	}

	/**
	 * The number of non-empty entries in the matrix.
	 * @return The number of non-empty entries in the matrix
	 */
	public int getNumberOfNonEmptyEntries() {
		int counter = 0;
		for (HashMap<Integer, T> row : row_list) {
			counter += row.size();
		}
		return counter;
	}

}

