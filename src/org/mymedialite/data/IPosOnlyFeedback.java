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

package org.mymedialite.data;

import org.mymedialite.datatype.IBooleanMatrix;

/**
 * Interface for implicit, positive-only user feedback.
 * @version 2.03
 */
public interface IPosOnlyFeedback extends IDataSet {
  
  /**
   * By-user access, users are stored in the rows, items in the culumns.
   */
  IBooleanMatrix userMatrix();

  /**
   * By-item access, items are stored in the rows, users in the culumns.
   */
  IBooleanMatrix itemMatrix();

  /**
   * Add a user-item event to the data structure.
   * @param userId the user ID
   * @param itemId the item ID
   */
  void add(int userId, int itemId);
  
  /**
   * Get a copy of the item matrix
   * @return a copy of the item matrix
   */
  IBooleanMatrix getItemMatrixCopy();

  /**
   * Get a copy of the user matrix
   * @return a copy of the user matrix
   */
  IBooleanMatrix getUserMatrixCopy();
  
  /**
   * Remove a user-item event from the data structure.
   * @param userId the user ID
   * @param itemId the item ID
   */
  void remove(int userId, int itemId);

  /**
   * Get the transpose of the dataset (users and items exchanged)
   * @return the transpose of the dataset
   */
  IPosOnlyFeedback transpose();

}

