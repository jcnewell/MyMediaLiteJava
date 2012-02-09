//Copyright (C) 2010, 2011 Zeno Gantner
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

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.RatingType;
import org.mymedialite.data.StaticByteRatings;
import org.mymedialite.data.StaticFloatRatings;
import org.mymedialite.data.StaticRatings;

/**
 * Class that offers methods for reading in static rating data.
 * @version 2.03
 */
public class StaticRatingData {
  
  // Prevent instantiation.
  private StaticRatingData() {}
  
  /**
   * Read in static rating data from a file.
   * @param filename the name of the file to read from
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param rating_type the data type to be used for storing the ratings
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data
   * @throws IOException 
   */
  public static IRatings read(
      String filename,
      IEntityMapping user_mapping, 
      IEntityMapping item_mapping,
      RatingType rating_type,
      boolean ignore_first_line) throws IOException {
    
    int size = 0;
    BufferedReader reader1 = new BufferedReader(new FileReader(filename));
    while (reader1.readLine() != null)
      size++;
    if (ignore_first_line)
      size--;
    
    BufferedReader reader2 = new BufferedReader(new FileReader(filename));
    return read(reader2, size, user_mapping, item_mapping, rating_type, ignore_first_line);   
  }

  /**
   * Read in static rating data from a TextReader.
   * @param reader the <see cref="TextReader"/> to read from
   * @param size the number of ratings in the file
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param rating_type the data type to be used for storing the ratings
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data
   * @throws IOException 
   */
  public static IRatings read(
      BufferedReader reader,
      int size,
      IEntityMapping user_mapping,
      IEntityMapping item_mapping,
      RatingType rating_type,
      boolean ignore_first_line) throws IOException {
    
    if (user_mapping == null)
      user_mapping = new IdentityMapping();
    if (item_mapping == null)
      item_mapping = new IdentityMapping();
    if (ignore_first_line)
      reader.readLine();
    if(rating_type == null)
      rating_type = RatingType.DOUBLE;

    IRatings ratings;
    if (rating_type == RatingType.BYTE)
      ratings = new StaticByteRatings(size);
    else if (rating_type == RatingType.FLOAT)
      ratings = new StaticFloatRatings(size);
    else
      ratings = new StaticRatings(size);

    String line;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0)
        continue;

      String[] tokens = line.split(Constants.SPLIT_CHARS);

      if (tokens.length < 3)
        throw new IOException("Expected at least 3 columns: " + line);

      int user_id = user_mapping.toInternalID(tokens[0].trim());
      int item_id = item_mapping.toInternalID(tokens[1].trim());
      double rating = Double.parseDouble(tokens[2]);
      ratings.add(user_id, item_id, rating);
    }
    return ratings;
  }
}
