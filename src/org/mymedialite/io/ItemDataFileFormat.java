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

package org.mymedialite.io;

/**
 * Represents different rating file formats.
 * @version 2.03
 */
public enum ItemDataFileFormat {

  /**
   * The default tab/comma separated rating format (e.g. MovieLens 100K, Apache Mahout).
   */
  DEFAULT,

  /**
   * Like the default format, but ignore the first line.
   */
  IGNORE_FIRST_LINE,

//  /**
//   * The MovieLens 1M/10M format (fields separated by "::").
//   */
//  MOVIELENS_1M,

//  /**
//   * The KDD Cup 2011 rating format.
//   */
//  KDDCUP_2011

}

