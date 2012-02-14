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
public class ItemData {

  /**
   * Read in implicit feedback data from a file.
   * 
   * Each line must consist of at least two fields, the first being a user identifier, the second
   * being an item identifier. Additional fields and empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   *
   * @param filename the name of the file to be read from or "-" if STDIN
   * @param user_mapping a user IEntityMapping object
   * @param item_mapping an item IEntityMapping object
   * @param ignore_first_line if true, ignore the first line
   * @return a IPosOnlyFeedback object with the user-wise collaborative data
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
   * 
   * Each line must consist of at least two fields, the first being a user identifier, the second
   * being an item identifier. Additional fields and empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   * 
   * @param reader the TextReader to be read from
   * @param user_mapping a user IEntityMapping object
   * @param item_mapping an item IEntityMapping object
   * @param ignore_first_line if true, ignore the first line
   * @return a PosOnlyFeedback object with the user-wise collaborative data
   */
  static public <T> IPosOnlyFeedback read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {

    if (user_mapping == null) user_mapping = new IdentityMapping();
    if (item_mapping == null) item_mapping = new IdentityMapping();
    if (ignore_first_line) reader.readLine();

    PosOnlyFeedback<SparseBooleanMatrix> feedback = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
    String line;

    while ((line = reader.readLine()) != null ) {
      line = line.trim();
      if(line.length() == 0) continue;
      String[] tokens = line.split(Constants.SPLIT_CHARS, 0);
      if(tokens.length < 2) throw new IOException("Expected at least two columns: " + line);
      
      int user_id = user_mapping.toInternalID((tokens[0]));      
      int item_id = item_mapping.toInternalID((tokens[1]));
      feedback.add(user_id, item_id);
    }

    return feedback;
  }

}