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

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Array-based storage for rating data.
 * 
 * Very memory-efficient.
 *
 * This data structure does NOT support incremental updates.
 * @version 2.03
 */
public class StaticFloatRatings extends StaticRatings {

  ArrayList<Float> float_values;

  /**
   * 
   */
  public Double get(int index) {
    return new Double(float_values.get(index));
  }

  public Double set(int index, Double rating) {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  public Double get(int user_id, int item_id) {
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return new Double(float_values.get(index));

    throw new IndexOutOfBoundsException("rating " + user_id + ". " + item_id + " not found.");
  }

  /**
   * 
   */
  public StaticFloatRatings(int size) {
    users = new ArrayList<Integer>(size);
    items = new ArrayList<Integer>(size);
    float_values = new ArrayList<Float>(size);
  }

  /**
   */
  public void add(int user_id, int item_id, double rating) {
    add(user_id, item_id, (float) rating);
  }

  /**
   * 
   */
  public void add(int user_id, int item_id, byte rating) {
    add(user_id, item_id, (float) rating);
  }

  /**
   * 
   */
  public void add(int user_id, int item_id, float rating) {
    if (pos == float_values.size())
      throw new RuntimeException("Ratings storage instanceof full, only space for " + size() + " ratings");

    users.set(pos, user_id);
    items.set(pos, item_id);
    float_values.set(pos, rating);

    if (user_id > maxUserID)
      maxUserID = user_id;
    if (item_id > maxItemID)
      maxItemID = item_id;

    if (rating > maxRating)
      maxRating = rating;
    if (rating < minRating)
      minRating = rating;

    pos++;
  }

  /**
   * 
   */
  public Double tryGet(int user_id, int item_id, double rating) {
    //TODO does anything rely on rating being marked out.
    //rating = Double.NEGATIVE_INFINITY;
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return new Double(float_values.get(index));
        
    return null;
  }

  /**
   * 
   */
  public Double get(int user_id, int item_id, Collection<Integer> indexes) {
    for (int index : indexes)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return (double) float_values.get(index);

    throw new IndexOutOfBoundsException("rating " + user_id + ", " + item_id + " not found.");
  }

  /**
   * 
   */
  public Double tryGet(int user_id, int item_id, Collection<Integer> indexes) {
    //TODO does anything rely on rating being marked out.
    //rating = Double.NEGATIVE_INFINITY;
    for (int index : indexes)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return new Double(float_values.get(index));

    return null;
  }

}
