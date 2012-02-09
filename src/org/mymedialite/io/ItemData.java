// Copyright (C) 2010, 2011 Zeno Gantner
// Copyright (C) 2011 Artus Krohn-Grimberghe, Chris Newell
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
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;

/** 
 * Class that contains static methods for reading in implicit feedback data for ItemRecommenders.
 * @version 2.03
 */
public class ItemData	{

  /**
   * Read in implicit feedback data from a file.
   * @param filename the name of the file to be read from or "-" if STDIN
   * @param user_mapping a user { @link org.mymedialite.data.IEntityMapping IEntityMapping } object
   * @param item_mapping an item { @link org.mymedialite.data.IEntityMapping IEntityMapping } object
   * @param ignore_first_line if true, ignore the first line
   * @return a { @link org.mymedialite.data.PosOnlyFeedback PosOnlyFeedback } object with the user-wise collaborative data
   */
  static public <T> IPosOnlyFeedback read(String filename, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {
    BufferedReader reader;
    if (filename.equals("-")) {
      reader = new BufferedReader(new InputStreamReader(System.in));
    } else {
      reader = new BufferedReader(new FileReader(filename));
    }
    return read(reader, user_mapping, item_mapping, ignore_first_line);
  }

  /**
   * Read in implicit feedback data from a TextReader.
   * @param reader the TextReader to be read from
   * @param user_mapping a user { @link org.mymedialite.data.IEntityMapping IEntityMapping } object
   * @param item_mapping an item { @link org.mymedialite.data.IEntityMapping IEntityMapping } object
   * @param ignore_first_line if true, ignore the first line
   * @return a { @link org.mymedialite.data.PosOnlyFeedback PosOnlyFeedback } object with the user-wise collaborative data
   */
  static public <T> IPosOnlyFeedback read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {

    if (user_mapping == null) user_mapping = new IdentityMapping();
    if (item_mapping == null) item_mapping = new IdentityMapping();
    if (ignore_first_line) reader.readLine();

    PosOnlyFeedback<SparseBooleanMatrix> feedback = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
    Pattern pattern = Pattern.compile("[,\\s]+");
    String line;

    while ((line = reader.readLine()) != null ) {
      if(line.trim().length() == 0) continue;
      String[] tokens = pattern.split(line);
      if(tokens.length < 2) throw new IOException("Expected at least two columns: " + line);
      int user_id = user_mapping.toInternalID((tokens[0]));      
      int item_id = item_mapping.toInternalID((tokens[1]));
      feedback.add(user_id, item_id);
    }

    return feedback;
  }

  //  /// <summary>Read in implicit feedback data from an IDataReader, e.g. a database via DbDataReader</summary>
  //  /// <param name="reader">the IDataReader to be read from</param>
  //  /// <param name="user_mapping">user <see cref="IEntityMapping"/> object</param>
  //  /// <param name="item_mapping">item <see cref="IEntityMapping"/> object</param>
  //  /// <returns>a <see cref="IPosOnlyFeedback"/> object with the user-wise collaborative data</returns>
  //  static public IPosOnlyFeedback Read(IDataReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping)
  //  {
  //      var feedback = new PosOnlyFeedback<SparseBooleanMatrix>();
  //
  //      if (reader.FieldCount < 2)
  //          throw new IOException("Expected at least two columns.");
  //
  //      while (reader.Read())
  //      {
  //          int user_id = user_mapping.ToInternalID(reader.GetInt32(0));
  //          int item_id = item_mapping.ToInternalID(reader.GetInt32(1));
  //
  //          feedback.Add(user_id, item_id);
  //      }
  //
  //      return feedback;
  //  }

}