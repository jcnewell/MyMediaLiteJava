// Copyright (C) 2010 Zeno Gantner, Chris Newell
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

package org.mymedialite.taxonomy;

/**
 * Type to refer to different kinds of entities like users and items.
 */
public class EntityType {
  
  /**
   * Users.
   */
  public static final int USER = 0;

  /**
   * Items like movies, DVDs, books, products, etc..
   */
  public static final int ITEM = 1;

  /**
   * Folksonomy tags.
   */
  public static final int TAG = 2;

  /**
   * Timestamps.
   */
  public static final int TIMESTAMP = 3;

}

