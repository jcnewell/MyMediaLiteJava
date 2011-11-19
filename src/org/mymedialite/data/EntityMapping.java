// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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
//    You should have received a copy of the GNU General Public License
//    along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** Class to map external entity IDs to internal ones to ensure that there are no gaps in the numbering */
public final class EntityMapping implements IEntityMapping {

  /**
   * Contains the mapping from the original (external) IDs to the internal IDs.
   */
  private HashMap<Integer, Integer> original_to_internal = new HashMap<Integer, Integer>();

  /**
   * Contains the mapping from the internal IDs to the original (external) IDs.
   */
  private HashMap<Integer, Integer> internal_to_original = new HashMap<Integer, Integer>();

  /**
   * Get all the original (external) entity IDs
   * @return all original (external) entity IDs
   */
  public Collection<Integer> getOriginalIDs() {
    return original_to_internal.keySet();
  }

  /**
   * Get all the internal entity IDs.
   * @return all internal entity IDs
   */
  public Collection<Integer> getInternalIDs() {
    return internal_to_original.keySet();
  }

  /**
   * Get original (external) ID of a given entity.
   * @param internal_id the internal ID of the entity
   * @return the original (external) ID of the entity
   * @throws if the given internal ID is unknown
   */
  public int toOriginalID(int internal_id) throws IllegalArgumentException {
    Integer original_id = internal_to_original.get(internal_id);   
    if (original_id != null) {
      return original_id;
    } else {
      throw new IllegalArgumentException("Unknown internal ID: " + internal_id);
    }
  }
  
  /**
   * Get internal ID of a given entity.
   * If the given external ID is unknown, create a new internal ID for it and store the mapping.
   * @param original_id the original (external) ID of the entity
   * @return the internal ID of the entity
   */
  public int toInternalID(int original_id) {
    Integer internal_id = original_to_internal.get(original_id);
    if (internal_id != null) {
      return internal_id;
    } else {
      internal_id = original_to_internal.size();
      original_to_internal.put(original_id, internal_id);
      internal_to_original.put(internal_id, original_id);
      return internal_id;
    }
  }

  /**
   * Get the original (external) IDs of a list of given entities.
   * @param internal_id_list the list of internal IDs
   * @return the list of original (external) IDs
   */
  public List<Integer> toOriginalID(List<Integer> internal_id_list) {
    ArrayList<Integer> result = new ArrayList<Integer>(internal_id_list.size());
    for (Integer id : internal_id_list) {
      result.add(toOriginalID(id));
    }
    return result;
  }

  /**
   *  Get the internal IDs of a list of given entities.
   *  @param original_id_list the list of original (external) IDs
   *  @return a list of internal IDs
   */
  public List<Integer> toInternalID(List<Integer> original_id_list) {
    ArrayList<Integer> result = new ArrayList<Integer>(original_id_list.size());
    for (Integer id : original_id_list) {
      result.add(toInternalID(id));
    }
    return result;
  }
  
  /**
   *  Get the size of this entity mapping.
   *  @return the entity count
   */
  public int size() {
    return original_to_internal.size();
  }

  /**
   * Save the mapping using the supplied writer.
   */
  public void saveMapping(PrintWriter writer) {
    writer.println(original_to_internal.size());
    for(Entry<Integer, Integer> entry : original_to_internal.entrySet()) {
      writer.println(entry.getKey() + " " + entry.getValue());
    }
    boolean error = writer.checkError();
    if(error) System.out.println("Error writing file.");
  }
  
  /**
   * Load a mapping from the supplied reader.
   */
  public void loadMapping(BufferedReader reader) throws IOException {
    int size = Integer.parseInt(reader.readLine());
    HashMap<Integer, Integer> original_to_internal = new HashMap<Integer, Integer>(size);
    HashMap<Integer, Integer> internal_to_original = new HashMap<Integer, Integer>(size);
    for(int i=0; i<size; i++) {
      String[] pair =  reader.readLine().split(" ");
      int original_id = Integer.parseInt(pair[0]);
      int internal_id = Integer.parseInt(pair[1]);
      original_to_internal.put(original_id, internal_id);
      internal_to_original.put(internal_id, original_id);
    }
    this.original_to_internal = original_to_internal;      
    this.internal_to_original = internal_to_original;
  }
  
}