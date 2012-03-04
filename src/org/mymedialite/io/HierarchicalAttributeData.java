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

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Class that offers static methods to read hierarchical attribute data into SparseBooleanMatrix objects.
 *
 * @version 2.03
 */
public class HierarchicalAttributeData {

  // Prevent instantiation.
  private HierarchicalAttributeData() {}
  
  private static final String separator = "/";
  
  /**
   * Read hierachical attribute data from a file.
   * 
   * Each line must consist of an item identifier followed by one or more hierarchical attributes 
   * which are represented by a path. The path is divided into hierarchical nodes by a "/" character. 
   * character. Leading and trailing separator characters are ignored.
   *
   * There can be one or more lines per item. Empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   * 
   * @param filename the name of the file to be read from
   * @param itemMapping the mapping object for the given entity type
   * @param attributeMapping the mapping object for the attributes
   * @return the attribute data
   */
  public static SparseBooleanMatrix read(String filename, IEntityMapping itemMapping, IEntityMapping attributeMapping) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return read(reader, itemMapping, attributeMapping);
    } catch (IOException e) {
      throw new IOException("Unable to read file " + filename + ": " + e.getMessage());
    }
  }

  /**
   * Read hierachical attribute data from a BufferedReader.
   * 
   * Each line must consist of an item identifier followed by one or more hierarchical attributes 
   * which are represented by a path. The path is divided into hierarchical nodes by a "/" character. 
   * character. Leading and trailing separator characters are ignored.
   *
   * There can be one or more lines per item. Empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   * 
   * @param reader a BufferedReader to be read from
   * @param itemMapping the mapping object for the given entity type
   * @param attributeMapping the mapping object for the attributes
   * @return the attribute data
   */
  public static SparseBooleanMatrix read(BufferedReader reader, IEntityMapping itemMapping, IEntityMapping attributeMapping) throws IOException {
    SparseBooleanMatrix matrix = new SparseBooleanMatrix();

    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0) continue;
      String[] tokens = line.split(Constants.SPLIT_CHARS, 0);
      if (tokens.length < 2) throw new IOException("Expected at least 2 columns: " + line);

      int entity_id = itemMapping.toInternalID(tokens[0]);
      for(int i = 1; i < tokens.length; i++) {   
        String path = trim(tokens[i]);
         Integer attr_id = attributeMapping.toInternalID(path);
        matrix.set(entity_id, attr_id, true);
        int index;
        while((index = path.lastIndexOf(separator)) > 0) {        
          path = path.substring(0, index);
          Integer parent_id = attributeMapping.toInternalID(path);
          matrix.set(entity_id, parent_id, true);
        }
      }
    }
    return matrix;
  }

  private static String trim(String path) {
    int start = 0;
    int length = path.length();
    if(path.indexOf(separator) == 0) start++;
    if(path.lastIndexOf(separator) == length - 1) length--;
    //return path.substring(start, length);
    
    // TODO remove this
    path = path.substring(start, length);
    if(path.startsWith("genres/")) path = path.substring(7);
    if(path.startsWith("formats/")) path = path.substring(8);
    if(path.startsWith("people/")) path = path.substring(7);
    if(path.startsWith("subjects/")) path = path.substring(9);
    if(path.startsWith("places/")) path = path.substring(7);
    if(path.startsWith("topics/")) path = path.substring(7);
    return path;
  }
  
}

