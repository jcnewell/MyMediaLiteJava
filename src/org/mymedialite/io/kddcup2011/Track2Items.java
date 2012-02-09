// Copyright (C) 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.io.kddcup2011;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that offers static methods for reading in test data from the KDD Cup 2011 files.
 * @version 2.03
 */
public class Track2Items {

  /**
   * Read track 2 candidates from a file.
   * @param filename the name of the file to read from
   * @return the candidates
   * @throws IOException 
   */
  public static HashMap<Integer, int[]> read(String filename) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return read(reader);
  }

  /**
   * Read track 2 candidates from a TextReader.
   * @param reader the <see cref="TextReader"/> to read from
   * @return the candidates
   * @throws IOException 
   */
  public static HashMap<Integer, int[]> read(BufferedReader reader) throws IOException {

    HashMap<Integer, int[]> candidates = new HashMap<Integer, int[]>();

    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("|");

      int user_id     = Integer.parseInt(tokens[0]);
      int num_ratings = Integer.parseInt(tokens[1]); // number of ratings for this user

      int[] user_candidates = new int[num_ratings];
      for (int i = 0; i < num_ratings; i++) {
        line = reader.readLine();
        user_candidates[i] = Integer.parseInt(line);
      }
      candidates.put(user_id, user_candidates);
    }
    return candidates;
  }
}
