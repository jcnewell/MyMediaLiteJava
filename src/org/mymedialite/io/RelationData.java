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
import org.mymedialite.datatype.SparseBooleanMatrix;
 
/**
 * Class that offers static methods to read (binary) relation over entities into SparseBooleanMatrix objects.
 * @version 2.03
 */
public class RelationData {
  
  // Prevent instantiation.
  private RelationData() {}
  
  /**
   * Read binary attribute data from file.
   * 
   * The expected (sparse) line format is:
   * ENTITY_ID whitespace/comma ENTITY_ID
   * for the relations that hold.
   * 
   * @param filename the name of the file to be read from
   * @param mapping the mapping object for the given entity type
   * @return the relation data
   */
  public static SparseBooleanMatrix read(String filename, IEntityMapping mapping) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return read(reader, mapping);
    } catch (IOException e) {
      throw new IOException("Unable to read file " + filename + ": " + e.getMessage());
    }
  }

  /**
   * Read binary relation data from file.
   * 
   * The expected (sparse) line format is:
   * ENTITY_ID whitespace/comma ENTITY_ID
   * for the relations that hold.
   * 
   * @param reader a StreamReader to be read from
   * @param mapping the mapping object for the given entity type
   * @return the relation data
   */
  public static SparseBooleanMatrix read(BufferedReader reader, IEntityMapping mapping) throws IOException {
    SparseBooleanMatrix matrix = new SparseBooleanMatrix();
    String line;
    while ((line = reader.readLine()) != null) {
      // ignore empty lines
      if (line.length() == 0) continue;

      String[] tokens = line.split(Constants.SPLIT_CHARS);

      if (tokens.length != 2) throw new IOException("Expected exactly 2 columns: " + line);

      int entity1_id = mapping.toInternalID(tokens[0]);
      int entity2_id = mapping.toInternalID(tokens[1]);
      matrix.set(entity1_id, entity2_id, true);
    }
    return matrix;
  }

//  /**
//   * Read binary relation data from an IDataReader, e.g. a database via DbDataReader.
//   * @param reader an IDataReader to be read from
//   * @param mapping the mapping object for the given entity type
//   * @return the relation data
//   */
//  public static SparseBooleanMatrix Read(IDataReader reader, IEntityMapping mapping)
//  {
//    if (reader.FieldCount < 2)
//      throw new Exception("Expected at least 2 columns.");
//
//    var matrix = new SparseBooleanMatrix();
//
//    while (!reader.Read())
//    {
//      int entity1_id = mapping.ToInternalID(reader.GetInt32(0));
//      int entity2_id = mapping.ToInternalID(reader.GetInt32(0));
//
//      matrix[entity1_id, entity2_id] = true;
//    }
//
//    return matrix;
//  }
  
}
