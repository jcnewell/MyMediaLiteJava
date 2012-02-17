//Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.ITimedRatings;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.TimedRatings;
import org.mymedialite.util.Utils;

/**
 * Class that offers methods for reading in rating data with time information.
 * @version 2.03
 */
public class TimedRatingData {
  
  // Prevent instantiation.
  private TimedRatingData() {}
  
  /**
   * Read in rating data from a file.
   * @param filename the name of the file to read from
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data
   * @throws FileNotFoundException 
   */
  public static ITimedRatings read(String filename, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {
    return read(new BufferedReader(new FileReader(filename)), user_mapping, item_mapping, ignore_first_line);
  }

  /**
   * Read in rating data from a TextReader.
   * @param reader the <see cref="TextReader"/> to read from
   * @param user_mapping mapping object for user IDs
   * @param item_mapping mapping object for item IDs
   * @param ignore_first_line if true, ignore the first line
   * @return the rating data
   * @throws ParseException 
   */
  public static ITimedRatings read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {
    if (user_mapping == null)
      user_mapping = new IdentityMapping();
    if (item_mapping == null)
      item_mapping = new IdentityMapping();
    if (ignore_first_line)
      reader.readLine();

    TimedRatings ratings = new TimedRatings();
    Integer unix_time;

    String line;
    while ((line = reader.readLine()) != null) {
      if (line.length() == 0)
        continue;

      String[] tokens = line.split("[,\\s]+");

      if (tokens.length < 4)
        throw new IOException("Expected at least 4 columns: " + line);

      int user_id = user_mapping.toInternalID(tokens[0]);
      int item_id = item_mapping.toInternalID(tokens[1]);
      double rating = Double.parseDouble(tokens[2]);
      String date_String = tokens[3];
      if (tokens[3].startsWith("\"") && tokens.length > 4 && tokens[4].endsWith("\"")) {
        date_String = tokens[3] + " " + tokens[4];
        date_String = date_String.substring(1, date_String.length() - 1);
      }

      if (date_String.length() == 19) {  // format "yyyy-mm-dd hh:mm:ss" 
        String[] date_time_tokens = date_String.split("[\\s-:]");
        Calendar calendar = Calendar.getInstance();
        calendar.set(
            Integer.parseInt(date_time_tokens[0]),
            Integer.parseInt(date_time_tokens[1]),
            Integer.parseInt(date_time_tokens[2]),
            Integer.parseInt(date_time_tokens[3]),
            Integer.parseInt(date_time_tokens[4]),
            Integer.parseInt(date_time_tokens[5])
        );
        
        ratings.add(user_id, item_id, rating, calendar.getTime());
      
      } else if (date_String.length() == 10) {  // format "yyyy-mm-dd"
        String[] date_time_tokens = date_String.split("[\\s-:]");
        Calendar calendar = Calendar.getInstance();
        calendar.set(
            Integer.parseInt(date_time_tokens[0]),
            Integer.parseInt(date_time_tokens[1]),
            Integer.parseInt(date_time_tokens[2])
        );
        
        ratings.add(user_id, item_id, rating, calendar.getTime());

      } else if ((unix_time = Utils.parseInteger(date_String)) != null) {  // unsigned integer value, interpreted as seconds since Unix epoch
        Date date = new Date();
        date.setTime((long)unix_time * 1000);
        ratings.add(user_id, item_id, rating, date);
      
      } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        Date date = dateFormat.parse(date_String);
        ratings.add(user_id, item_id, rating, date);
      }
      
      if (ratings.size() % 200000 == 199999)
        System.err.print(".");
      if (ratings.size() % 12000000 == 11999999)
        System.err.println();
    }
    return ratings;
  }
 
}
