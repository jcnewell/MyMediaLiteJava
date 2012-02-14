// Copyright (C) 2010, 2011 Zeno Gantner
// Copyright (C) 2011 Chris Newell, Zeno Gantner
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

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.Ratings;

/**
 * Class that offers methods for reading in rating data
 * @version 2.03
 */
public class RatingData {

  /** 
   * Read in rating data from a file.
   * 
   * Each line must consist of at least three fields, the first being a user identifier, the second being
   * an item identifier and the third being a rating value. Additional fields and empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   * 
   * @param filename the name of the file to read from
   * @param userMapping mapping object for user IDs
   * @param itemMapping mapping object for item IDs
   * @param ignoreFirstLine if true, ignore the first line
   * @return the rating data 
   */
  public static IRatings read(String filename, IEntityMapping userMapping, IEntityMapping itemMapping, boolean ignoreFirstLine) throws IOException, NumberFormatException {
    return read(new BufferedReader(new FileReader(filename)), userMapping, itemMapping, ignoreFirstLine);
  }

  /** 
   * Read in rating data from a BufferedReader
   * 
   * Each line must consist of at least three fields, the first being a user identifier, the second being
   * an item identifier and the third being a rating value. Additional fields and empty lines are ignored.
   * 
   * See Constants.SPLIT_CHARS for details of the permissible field separators.
   * 
   * @param reader the BufferedReader to read from
   * @param userMapping mapping object for user IDs
   * @param itemMapping mapping object for item IDs
   * @param ignoreFirstLine if true, ignore the first line
   * @return the rating data 
   */
  public static IRatings read(BufferedReader reader, IEntityMapping userMapping, IEntityMapping itemMapping, boolean ignoreFirstLine) throws IOException, NumberFormatException {
    if (userMapping == null)
      userMapping = new IdentityMapping();
    if (itemMapping == null)
      itemMapping = new IdentityMapping();
    if (ignoreFirstLine)
      reader.readLine();
    
    IRatings ratings = new Ratings();
    String line;
    while ((line = reader.readLine()) != null ) {
      line = line.trim();
      if(line.length() == 0) continue;
      String[] tokens  = line.split(Constants.SPLIT_CHARS, 0);
      if(tokens.length < 3) throw new IOException("Expected at least three columns: " + line);
      int user_id = userMapping.toInternalID(tokens[0]);
      int item_id = itemMapping.toInternalID(tokens[1]);
      double rating = Double.parseDouble(tokens[2]);
      ratings.add(user_id, item_id, rating);
    }
    return ratings;
  }
}