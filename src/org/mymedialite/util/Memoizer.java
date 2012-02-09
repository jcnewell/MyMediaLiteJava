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

package org.mymedialite.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Class to Memoize a function.
 *
 * Provides a version of a function that remembers past function results.
 *
 * @param <A> the type of the single argument
 * @param <R> the type of the return value
 * 
 * @version 2.03
 */
public class Memoizer<A, R> {

  private Object object;
  private Method method;
  private HashMap<A, R> map = new HashMap<A, R>();
  
  /**
   * Create a Memoizer
   * 
   * @param object the object on which to invoke the method
   * @param method the method to call
   * @param argType the single argument type
   * @throws NoSuchMethodException
   */
  public Memoizer(Object object, String methodName, Class<A> argType) {
    this.object = object;
    try {
      this.method = object.getClass().getMethod(methodName, argType);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }  
  }
  
  @SuppressWarnings("unchecked")
  public R get(A a) {
    R value = map.get(a);
    if (value != null) return value;
    try {
      value = (R) method.invoke(object, a);
    } catch (Exception e) {
      e.printStackTrace();
    }
    map.put(a, value);
    return value;
  }
  
}
