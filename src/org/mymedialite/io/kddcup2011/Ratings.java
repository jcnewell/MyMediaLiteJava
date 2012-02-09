//Copyright (C) 2011 Zeno Gantner
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

package org.mymedialite.io.kddcup2011;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.StaticByteRatings;

/**
 * Class that offers static methods for reading in rating data from the KDD Cup 2011 files.
 * @version 2.03
 */
public class Ratings {
  
  // Prevent instantiation.
  private Ratings() {}
  
  /**
   * Read in rating data from a file.
   * @param filename the name of the file to read from
   * @return the rating data
   * @throws IOException 
   */
  public static IRatings read(String filename) throws IOException {
    // Create ratings data structure
    IRatings ratings = new StaticByteRatings(getNumberOfRatings(new BufferedReader(new FileReader(filename))));

    BufferedReader reader = new BufferedReader(new FileReader(filename));
    
    // Read in ratings
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");

      int user_id          = Integer.parseInt(tokens[0]);
      int num_user_ratings = Integer.parseInt(tokens[1]); // number of ratings for this user

      for (int i = 0; i < num_user_ratings; i++) {
        line = reader.readLine();

        tokens = line.split("\t");

        int item_id = Integer.parseInt(tokens[0]);
        byte rating = Byte.parseByte(tokens[1]);

        ratings.add(user_id, item_id, rating);
      }
    }
    return ratings;
  }

  /**
   * Read in test rating data (Track 1) from a file.
   * @param filename the name of the file to read from
   * @return the rating data
   * @throws IOException 
   */
  public static IRatings readTest(String filename) throws IOException {
    IRatings ratings = new StaticByteRatings(getNumberOfRatings(new BufferedReader(new FileReader(filename))));
    
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");

      int user_id     = Integer.parseInt(tokens[0]);
      int num_user_ratings = Integer.parseInt(tokens[1]); // number of ratings for this user

      for (int i = 0; i < num_user_ratings; i++) {
        line = reader.readLine();
        tokens = line.split("\t");
        int item_id = Integer.parseInt(tokens[0]);
        ratings.add(user_id, item_id, 0);
      }
    }
    return ratings;
  }

  /**
   * Read in rating data from a file.
   * @param filename the name of the file to read from
   * @return the rating data
   */
  public static IRatings read80Plus(String filename) throws IOException {

    // Create ratings data structure
    IRatings ratings = new StaticByteRatings(getNumberOfRatings(new BufferedReader(new FileReader(filename))));

    BufferedReader reader = new BufferedReader(new FileReader(filename));
    
    // Read in ratings
    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");

      int user_id          = Integer.parseInt(tokens[0]);
      int num_user_ratings = Integer.parseInt(tokens[1]); // number of ratings for this user

      for (int i = 0; i < num_user_ratings; i++) {
        line = reader.readLine();
        tokens = line.split("\t");
        int item_id = Integer.parseInt(tokens[0]);
        byte rating = Byte.parseByte(tokens[1]);
        ratings.add(user_id, item_id, rating >= 80 ? 1 : 0);
      }
    }
    return ratings;
  }

  static int getNumberOfRatings(BufferedReader reader) throws IOException {
    int num_ratings = 0;

    String line;
    while ((line = reader.readLine()) != null)
      if (!line.contains("|"))
        num_ratings++;

    return num_ratings;
  }
}

