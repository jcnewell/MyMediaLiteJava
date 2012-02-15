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

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

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

  FloatList float_values;
  
  @Override
  public double get(int index) {
    return float_values.getFloat(index);
  }

  @Override
  public double set(int index, double rating) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double get(int user_id, int item_id) {
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return float_values.getFloat(index);

    throw new IndexOutOfBoundsException("rating " + user_id + ". " + item_id + " not found.");
  }
  
  public StaticFloatRatings(int size) {
    users = new IntArrayList(size);
    items = new IntArrayList(size);
    float_values = new FloatArrayList(size);
  }

  @Override
  public void add(int user_id, int item_id, double rating) {
    add(user_id, item_id, rating);
  }

  @Override
  public void add(int user_id, int item_id, byte rating) {
    add(user_id, item_id, rating);
  }

  @Override
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

  public Double tryGet(int user_id, int item_id, double rating) {
    //TODO does anything rely on rating being marked out.
    //rating = Double.NEGATIVE_INFINITY;
    //TODO speed up
    for (int index = 0; index < pos; index++)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return new Double(float_values.getFloat(index));
        
    return null;
  }

  public double get(int user_id, int item_id, IntCollection indexes) {
    for (int index : indexes)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return float_values.getFloat(index);

    throw new IndexOutOfBoundsException("rating " + user_id + ", " + item_id + " not found.");
  }

  /**
   * 
   */
  public Double tryGet(int user_id, int item_id, IntCollection indexes) {
    //TODO does anything rely on rating being marked out.
    //rating = Double.NEGATIVE_INFINITY;
    for (int index : indexes)
      if (users.getInt(index) == user_id && items.getInt(index) == item_id)
        return new Double(float_values.getFloat(index));

    return null;
  }

}
