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

import java.util.Collection;
import java.util.List;

/** 
 * Interface to map external entity IDs to internal ones to ensure that there are no gaps in the numbering.
 * @version 2.03
 */
public interface IEntityMapping {
  
  /** 
   * Get all original (external) entity IDs.
   */
  Collection <String> originalIDs();

  /** 
   * Get all internal entity IDs.
   */
  Collection<Integer> internalIDs();

  /** 
   * Get the original (external) ID of a given entity, if the given internal ID is unknown, throw an exception.
   * @param internal_id the internal ID of the entity.
   * @return the original (external) ID of the entity.
   */
  String toOriginalID(int internal_id);

  /** 
   * Get internal ID of a given entity. If the given external ID is unknown, create a new internal ID for it and store the mapping.
   * @param original_id the original (external) ID of the entity.
   * @return the internal ID of the entity.
   */
  Integer toInternalID(String original_id);

  /** 
   * Get the original (external) IDs of a list of given entities.
   * @param internal_id_list the list of internal IDs.
   * @return the list of original (external) IDs.
   */
  List<String> toOriginalID(List<Integer> internal_id_list);

  /** 
   * Get internal IDs of a list of given entities.
   * @param original_id_list the list of original (external) IDs.
   * @return a list of internal IDs.
   */
  List<Integer> toInternalID(List<String> original_id_list);
  
}