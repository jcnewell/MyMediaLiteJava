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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class to map String external entity IDs to internal ones to ensure that there are no gaps in the numbering
 * @version 2.03
 */
public class EntityMapping implements IEntityMapping {

  /**
   * Contains the mapping from the original (external) IDs to the internal IDs.
   */
  protected HashMap<String, Integer> original_to_internal = new HashMap<String, Integer>();

  /**
   * Contains the mapping from the internal IDs to the original (external) IDs.
   */
  protected HashMap<Integer, String> internal_to_original = new HashMap<Integer, String>();

  /**
   * Get all the original (external) entity IDs
   * @return all original (external) entity IDs
   */
  @Override
  public Collection<String> originalIDs() {
    return original_to_internal.keySet();
  }

  /**
   * Get all the internal entity IDs.
   * @return all internal entity IDs
   */
  @Override
  public Collection<Integer> internalIDs() {
    return internal_to_original.keySet();
  }

  /**
   * Get original (external) ID of a given entity.
   * @param internal_id the internal ID of the entity
   * @return the original (external) ID of the entity
   * @throws if the given internal ID is unknown
   */
  @Override
  public String toOriginalID(int internal_id) throws IllegalArgumentException {
    String original_id = internal_to_original.get(internal_id);   
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
  @Override
  public Integer toInternalID(String original_id) {
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
  @Override
  public List<String> toOriginalID(IntList internal_id_list) {
    ArrayList<String> result = new ArrayList<String>(internal_id_list.size());
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
  @Override
  public IntList toInternalID(List<String> original_id_list) {
    IntList result = new IntArrayList(original_id_list.size());
    for (String id : original_id_list) {
      result.add(toInternalID(id));
    }
    return result;
  }
  
  /**
   * Save this entity mapping.
   * @param writer
   * @throws IOException
   */
  public void saveMapping(PrintWriter writer) throws IOException {
    writer.println(original_to_internal.size());
    for(Entry<String, Integer> entry : original_to_internal.entrySet()) {
      writer.println(entry.getKey() + " " + entry.getValue());
      if(writer.checkError()) throw new IOException("Error writing model file");
    }
  }
  
  /**
   * Load an entity mapping.
   * @param reader
   * @throws IOException
   */
  public void loadMapping(BufferedReader reader) throws IOException {
    int size = Integer.parseInt(reader.readLine());
    HashMap<String, Integer> original_to_internal = new HashMap<String, Integer>(size);
    HashMap<Integer, String> internal_to_original = new HashMap<Integer, String>(size);
    
    for(int i=0; i<size; i++) {
      String[] pair =  reader.readLine().split(" ");
      String original_id = pair[0].trim();
      int internal_id = Integer.parseInt(pair[1]);
      original_to_internal.put(original_id, internal_id);
      internal_to_original.put(internal_id, original_id);
    }
    
    this.original_to_internal = original_to_internal;      
    this.internal_to_original = internal_to_original;
  }
  
}