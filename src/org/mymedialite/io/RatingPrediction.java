//Copyright (C) 2010, 2011 Zeno Gantner
//Copyright (C) 2011 Artus Krohn-Grimberghe, Chris Newell
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
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.Ratings;

 /** Class that offers methods for reading in rating data */
 public class RatingPrediction {
   
   /** 
    * Read in rating data from a file.
    * @param filename the name of the file to read from, "-" if STDIN
    * @param user_mapping mapping object for user IDs
    * @param item_mapping mapping object for item IDs
    * @return the rating data 
    */
   public static IRatings read(String filename, IEntityMapping user_mapping, IEntityMapping item_mapping) throws IOException, NumberFormatException {
     BufferedReader reader;
     if (filename.equals("-")) {
       reader = new BufferedReader(new InputStreamReader(System.in));
     } else {
       reader = new BufferedReader(new FileReader(filename));
     }
     return read(reader, user_mapping, item_mapping);
   }

   /** 
    * Read in rating data from a TextReader
    * @param reader the <see cref="TextReader"/> to read from
    * @param min_rating the lowest possible rating value, warn on out of range ratings
    * @param max_rating the highest possible rating value, warn on out of range ratings
    * @param user_mapping mapping object for user IDs
    * @param item_mapping mapping object for item IDs
    * @return the rating data 
    */
   public static IRatings read(BufferedReader reader, IEntityMapping user_mapping, IEntityMapping item_mapping) throws IOException, NumberFormatException {
     IRatings ratings = new Ratings();
     boolean out_of_range_warning_issued = false;
     Pattern pattern = Pattern.compile("[,\\s]+");
     String line;
     while ((line = reader.readLine()) != null ) {
       if(line.trim().length() == 0) continue;
       String[] tokens = pattern.split(line);
       if(tokens.length < 3) throw new IOException("Expected at least three columns: " + line);
       int user_id = user_mapping.toInternalID(Integer.parseInt(tokens[0]));
       int item_id = item_mapping.toInternalID(Integer.parseInt(tokens[1]));
       double rating = Double.parseDouble(tokens[2]);
       ratings.add(user_id, item_id, rating);
     }
     return ratings;
   }

//   /// <summary>Read in rating data from an IDataReader, e.g. a database via DbDataReader</summary>
//   /// <param name="reader">the <see cref="IDataReader"/> to read from</param>
//   /// <param name="user_mapping">mapping object for user IDs</param>
//   /// <param name="item_mapping">mapping object for item IDs</param>
//   /// <returns>the rating data</returns>
//   static public IRatings
//       Read(IDataReader reader, EntityMapping user_mapping, EntityMapping item_mapping)
//   {
//       var ratings = new Ratings();
//
//       if (reader.FieldCount < 3)
//           throw new IOException("Expected at least three columns.");
//
//       while (reader.Read())
//       {
//           int user_id = user_mapping.ToInternalID(reader.GetInt32(0));
//           int item_id = item_mapping.ToInternalID(reader.GetInt32(1));
//           double rating = reader.GetDouble(2);
//
//           ratings.Add(user_id, item_id, rating);
//       }
//       return ratings;
//   }
}