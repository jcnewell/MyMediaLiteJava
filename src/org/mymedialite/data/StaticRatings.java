//Copyright (C) 2011 Zeno Gantner
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

/**
 * Array-based storage for rating data..
 * 
 * Very memory-efficient.
 *
 * This data structure does NOT support incremental updates.
 * @version 2.03
 */
public class StaticRatings extends Ratings {

  //TODO for better performance, build array-based indices

  /**
   * The position where the next rating will be stored.
   */
  protected int pos = 0;

  private int size;
  
  /**
   * 
   */
  public int size() { 
    return pos; 
  }

  /**
   * 
   */
  public StaticRatings() { }

  /**
   */
  public StaticRatings(int size) { 
    this.size = size;
    users  = new ArrayList<Integer>(size);
    items  = new ArrayList<Integer>(size);
    values = new ArrayList<Double>(size);
  }

  /**
   * @throws IndexOutOfBoundsException 
   */
  public void add(int user_id, int item_id, double rating) {
    
    if (pos == size)
      throw new IndexOutOfBoundsException("Ratings storage instanceof full, only space for " + size() + " ratings");

    users.add(user_id);
    items.add(item_id);
    values.add(rating);

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
  public void removeAt(int index) {
    throw new UnsupportedOperationException();
  }

  /**
   */
  public void removeUser(int user_id) {
    throw new UnsupportedOperationException();
  }

  /**
   */
  public void removeItem(int item_id) {
    throw new UnsupportedOperationException();
  }

}


