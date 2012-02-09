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
import java.util.List;

/**
 * Array-based storage for rating data..
 * 
 * Very memory-efficient.
 *
 * This data structure does NOT support incremental updates.
 * @version 2.03
 */
public class StaticByteRatings extends StaticRatings {

  List<Byte> byte_values;

  /**
   */
  public Double get(int index) {
    return (double) byte_values.get(index);
  }

  public Double set(int index, Double rating) {
    throw new UnsupportedOperationException();
  }

  /**
   */
  public Double get(int user_id, int item_id) {
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return new Double(byte_values.get(index));

    throw new IllegalArgumentException("rating " + user_id + ", " + item_id + " not found.");
  }

  /**
   */
  public StaticByteRatings(int size) {
    users  = new ArrayList<Integer>(size);
    items  = new ArrayList<Integer>(size);
    byte_values = new ArrayList<Byte>(size);
  }

  /**
   */
  public void Add(int user_id, int item_id, double rating)
  {
    Add(user_id, item_id, (byte) rating);
  }

  /**
   */
  public void add(int user_id, int item_id, byte rating) {
    if (pos == byte_values.size())
      throw new IndexOutOfBoundsException("Ratings storage instanceof full, only space for " + size() + "ratings");

    users.set(pos, user_id);
    items.set(pos, item_id);
    byte_values.set(pos, rating);

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
   */
  public Double tryGet(int user_id, int item_id) {
    // TODO Is anything replying on this as a return value? (in C# it was marked "out")
    Double rating = Double.NEGATIVE_INFINITY;
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.get(index) == user_id && items.get(index) == item_id) {
        rating = new Double(byte_values.get(index));
        return rating;
      }

    return null;
  }

  /**
   */
  public Double get(int user_id, int item_id, Collection<Integer> indexes) {
    for (int index : indexes)
      if (users.get(index) == user_id && items.get(index) == item_id)
        return new Double(byte_values.get(index));

    throw new IllegalArgumentException("rating " + user_id + ", " + item_id + " not found.");
  }

  /**
   */
  public Double tryGet(int user_id, int item_id, Collection<Integer> indexes) {
    // TODO Is anything replying on this as a return value? (in C# it was marked "out")
    double rating = Double.NEGATIVE_INFINITY;

    for (int index : indexes)
      if (users.get(index) == user_id && items.get(index) == item_id) {
        rating = new Double(byte_values.get(index));
        return rating;
      }

    return null;
  }

}

