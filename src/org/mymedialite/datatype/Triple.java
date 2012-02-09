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

/**
 * Generic triple class.
 * @version 2.03
 */
public class Triple<T, U, V> {

  /**
   * Default constructor.
   */
  public Triple() { }

  /**
   * Create a Triple object from existing data.
   * @param first the first component
   * @param second the second component
   * @param third the third component
   */
  public Triple(T first, U second, V third) {
    this.first  = first;
    this.second = second;
    this.third  = third;
  }

  /**
   * the first component.
   */
  public T first;

  /**
   * the second component.
   */
  public U second;

  /**
   * the third component.
   */
  public V third;

}
