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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Identity mapping for entity IDs: Every original ID is mapped to itself.
 * @version 2.03
 */
public final class IdentityMapping implements IEntityMapping {

  private int maxEntityID;

  public int getMaxEntityID() {
    return maxEntityID;
  }

  /**
   * 
   */
  public Collection<String> originalIDs() {
    Collection<String> id_list = new ArrayList<String>(maxEntityID + 1);
    for (int i = 0; i <= maxEntityID; i++) id_list.add(Integer.toString(i));
    return id_list;
  }

  /**
   * 
   */
  public Collection<Integer> internalIDs() {
    Collection<Integer> id_list = new ArrayList<Integer>(maxEntityID + 1);
    for (int i = 0; i <= maxEntityID; i++) id_list.add(i);
    return id_list;
  }

  /**
   * 
   */
  public String toOriginalID(int internal_id) {
    maxEntityID = Math.max(maxEntityID, internal_id);
    return Integer.toString(internal_id);
  }

  /**
   * 
   */
  public Integer toInternalID(String original_id) {
    try {
      int internal_id = Integer.parseInt(original_id);
      maxEntityID = Math.max(maxEntityID, internal_id);
      return internal_id;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("original_id must be an integer and cannot be greater than Integer.MAX_VALUE");
    }
  }

  /**
   * 
   */
  public List<String> toOriginalID(List<Integer> internal_id_list) {
    List<String> original_ids = new ArrayList<String>(internal_id_list.size());
    for (int i = 0; i < internal_id_list.size(); i++) {
      int internal_id = internal_id_list.get(i);
      maxEntityID = Math.max(maxEntityID, internal_id);
      original_ids.add(Integer.toString(internal_id));
    }
    return original_ids;
  }

  /**
   * 
   */
  public List<Integer> toInternalID(List<String> original_id_list) {
    List<Integer> internal_ids = new ArrayList<Integer>(original_id_list.size());
    for (int i = 0; i < original_id_list.size(); i++) {
      String original_id = original_id_list.get(i);
      try{ 
        int internal_id = Integer.parseInt(original_id);
        maxEntityID = Math.max(maxEntityID, internal_id);
        internal_ids.add(internal_id);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("original_id must be an integer and cannot be greater than Integer.MAX_VALUE");
      }
    }
    return internal_ids;
  }
  
}
