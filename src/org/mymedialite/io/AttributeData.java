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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IHierarchicalEntityMapping;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Class that offers static methods to read (binary) attribute data into SparseBooleanMatrix objects.
 * 
 * The expected (sparse) line format is:
 * ENTITY_ID SEPARATOR ATTRIBUTE_ID
 * for attributes that are set.
 * SEPARATOR can be space, tab, or comma.
 * @version 2.03
 */
public class AttributeData {

  // Prevent instantiation.
  private AttributeData() {}
  
  /**
   * Read binary attribute data from a file.
   * 
   * The expected (sparse) line format is:
   * ENTITY_ID whitespace/comma ATTRIBUTE_ID
   * for the relations that hold.
   * 
   * @param filename the name of the file to be read from
   * @param mapping the mapping object for the given entity type
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
   * Read binary attribute data from a BufferedReader.
   * 
   * The expected (sparse) line format is:
   * ENTITY_ID whitespace/comma ATTRIBUTE_ID
   * for the relations that hold.
   * 
   * @param reader a BufferedReader to be read from
   * @param mapping the mapping object for the given entity type
   * @return the attribute data
   */
  public static SparseBooleanMatrix read(BufferedReader reader, IEntityMapping itemMapping, IEntityMapping attributeMapping) throws IOException {
    SparseBooleanMatrix matrix = new SparseBooleanMatrix();

    String line;
    while ((line = reader.readLine()) != null) {
      // Ignore empty lines
      if (line.length() == 0) continue;

      String[] tokens = line.split(Constants.SPLIT_CHARS);

      if (tokens.length != 2) throw new IOException("Expected exactly 2 columns: " + line);

      int entity_id = itemMapping.toInternalID(tokens[0]);
      int attr_id   = attributeMapping.toInternalID(tokens[1]);

      matrix.set(entity_id, attr_id, true);
    }

    return matrix;
  }

}
