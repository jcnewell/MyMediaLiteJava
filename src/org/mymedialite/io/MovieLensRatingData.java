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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.ITimedRatings;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.TimedRatings;

/**
 * Class that offers static methods for reading in MovieLens 1M and 10M rating data.
 * See http://www.grouplens.org/node/73#attachments and http://recsyswiki.com/wiki/MovieLens
 * @version 2.03
 */
public class MovieLensRatingData {
  
  // Prevent instantiation.
  private MovieLensRatingData() {}
  
  /**
   * Read in rating data from a file.
   * @param filename the name of the file to read from, "-" if STDIN
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @return the rating data
   * @throws IOException
   */
  public static ITimedRatings read(String filename, IEntityMapping user_mapping, IEntityMapping item_mapping) throws IOException {
    return read(new BufferedReader(new FileReader(filename)), user_mapping, item_mapping);
  }

  /**
   * Read in rating data from a TextReader.
   * @param reader the <see cref="TextReader"/> to read from
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @return the rating data
   * @throws IOException 
   */
  public static ITimedRatings read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping) throws IOException {
    if (user_mapping == null)
      user_mapping = new IdentityMapping();
    if (item_mapping == null)
      item_mapping = new IdentityMapping();

    TimedRatings ratings = new TimedRatings();
    
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("::");

      if (tokens.length < 4)
        throw new IOException("Expected at least 4 columns: " + line);

      int user_id = user_mapping.toInternalID(tokens[0]);
      int item_id = item_mapping.toInternalID(tokens[1]);
      double rating = Double.parseDouble(tokens[2]);
      long seconds = Long.parseLong(tokens[3]);
      Date date = new Date();
      date.setTime(seconds * 1000);
      
      // TODO check about timezone
      //var offset = TimeZone.CurrentTimeZone.GetUtcOffset(time);
      //date -= offset;
      ratings.add(user_id, item_id, rating, date);
    }
    return ratings;
  }
}
