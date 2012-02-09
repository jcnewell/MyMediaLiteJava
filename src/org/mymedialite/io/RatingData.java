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
   * @param filename the name of the file to read from
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data 
   */
  public static IRatings read(String filename, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws IOException, NumberFormatException {
    return read(new BufferedReader(new FileReader(filename)), user_mapping, item_mapping, ignore_first_line);
  }

  /** 
   * Read in rating data from a BufferedReader
   * @param reader the BufferedReader to read from
   * @param min_rating the lowest possible rating value, warn on out of range ratings
   * @param max_rating the highest possible rating value, warn on out of range ratings
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data 
   */
  public static IRatings read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws IOException, NumberFormatException {
    if (user_mapping == null)
      user_mapping = new IdentityMapping();
    if (item_mapping == null)
      item_mapping = new IdentityMapping();
    if (ignore_first_line)
      reader.readLine();
    
    IRatings ratings = new Ratings();
    Pattern pattern = Pattern.compile("[,\\s]+");
    String line;
    while ((line = reader.readLine()) != null ) {
      if(line.trim().length() == 0)
        continue;
      String[] tokens = pattern.split(line);
      if(tokens.length < 3)
        throw new IOException("Expected at least three columns: " + line);
      int user_id = user_mapping.toInternalID(tokens[0]);
      int item_id = item_mapping.toInternalID(tokens[1]);
      double rating = Double.parseDouble(tokens[2]);
      ratings.add(user_id, item_id, rating);
    }
    return ratings;
  }
}