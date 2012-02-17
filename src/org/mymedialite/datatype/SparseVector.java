// Copyright (C) 2010 Zeno Gantner 
// Copyright (C) 2012 Chris Newell
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

import java.util.HashMap;

/**
 * 
 * Class for storing sparse vectors.
 * Indexes are zero-based.
 *
 * @param <T>
 * @version 2.03
 */
/**
 * @author newell
 *
 * @param <T>
 */
public class SparseVector<T> {

  Class<T> c;
  
  /**
   * Internal data representation as dictionary.
   */
  protected HashMap<Integer, T> data = new HashMap<Integer, T>();

  public SparseVector(Class<T> c) {
    this.c = c;    
  }
  
  /** 
   * Access an element of the vector
   * .
   * @param x the index
   */
  public T get(int x) {

    T result = data.get(x);
    if(result == null) {
     try {
      result = c.newInstance();
      } catch (Exception e) {
      e.printStackTrace();
      }
    }
    return result;
  }
  
}