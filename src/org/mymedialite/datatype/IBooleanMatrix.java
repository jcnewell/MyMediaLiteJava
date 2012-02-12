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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.datatype;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Interface for boolean matrices.
 * @version 2.03
 */
public interface IBooleanMatrix extends IMatrix<Boolean> {

  /**
   * Get a row of the matrix.
   * @param x the row ID
   */
  IntCollection get(int x);

  /**
   * The number of (true) entries.
   */
  int numberOfEntries();

  /**
   * The IDs of the non-empty rows in the matrix (the ones that contain at least one true entry).
   */
  IntCollection nonEmptyRowIDs();

  /**
   * The IDs of the non-empty columns in the matrix (the ones that contain at least one true entry).
   */
  IntCollection nonEmptyColumnIDs();

  /**
   * Get all true entries (column IDs) of a row.
   * @param row_id the row ID
   * @return a list of column IDs
   */
  IntList getEntriesByRow(int row_id);

  /**
   * Get all the number of entries in a row.
   * @param row_id the row ID
   * @return the number of entries in row row_id
   */
  int numEntriesByRow(int row_id);

  /**
   * Get all true entries (row IDs) of a column.
   * @param column_id the column ID
   * @return a list of row IDs
   */
  IntList getEntriesByColumn(int column_id);

  /**
   * Get all the number of entries in a column.
   * @param column_id the column ID
   * @return the number of entries in column column_id
   */
  int numEntriesByColumn(int column_id);

  /**
   * Get the overlap of two matrices, i.e. the number of true entries where they agree.
   * @param s the <see cref="IBooleanMatrix"/> to compare to
   * @return the number of entries that are true in both matrices
   */
  int overlap(IBooleanMatrix s);

}

