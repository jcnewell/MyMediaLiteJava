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
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Class that contains static methods for reading in implicit feedback data for ItemRecommender.
 * @version 2.03
 */
public class ItemDataRatingThreshold {

  // Prevent instantiation.
  private ItemDataRatingThreshold() {}

  /**
   * Read in rating data which will be interpreted as implicit feedback data from a file.
   * @param filename name of the file to be read from
   * @param rating_threshold the minimum rating value needed to be accepted as positive feedback
   * @param user_mapping user <see cref="IEntityMapping"/> object
   * @param item_mapping item <see cref="IEntityMapping"/> object
   * @param ignore_first_line if true, ignore the first line
   * @return a <see cref="IPosOnlyFeedback"/> object with the user-wise collaborative data
   */
  public static IPosOnlyFeedback read(String filename, double rating_threshold, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return read(reader, rating_threshold, user_mapping, item_mapping, ignore_first_line);
    } catch (Exception e) {
      throw new IOException("Unable to read " + filename + ": " + e.getMessage());
    }
  }

  /**
   * Read in rating data which will be interpreted as implicit feedback data from a TextReader.
   * @param reader the TextReader to be read from
   * @param rating_threshold the minimum rating value needed to be accepted as positive feedback
   * @param user_mapping user <see cref="IEntityMapping"/> object
   * @param item_mapping item <see cref="IEntityMapping"/> object
   * @param ignore_first_line if true, ignore the first line
   * @return a <see cref="IPosOnlyFeedback"/> object with the user-wise collaborative data
   */
  public static IPosOnlyFeedback read(BufferedReader reader, double rating_threshold, IEntityMapping user_mapping, IEntityMapping item_mapping, boolean ignore_first_line) throws Exception {
    if (user_mapping == null) user_mapping = new IdentityMapping();
    if (item_mapping == null) item_mapping = new IdentityMapping();
    if (ignore_first_line) reader.readLine();

    PosOnlyFeedback<SparseBooleanMatrix> feedback = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);

    String line;
    while ((line = reader.readLine()) != null) {
      if (line.trim().length() == 0) continue;

      String[] tokens = line.split(Constants.SPLIT_CHARS);

      if (tokens.length < 3) throw new IOException("Expected at least 3 columns: " + line);

      int user_id   = user_mapping.toInternalID(tokens[0]);
      int item_id   = item_mapping.toInternalID(tokens[1]);
      double rating = Double.parseDouble(tokens[2]);

      if (rating >= rating_threshold) feedback.add(user_id, item_id);
    }

    return feedback;
  }

//  /**
//   * Read in rating data which will be interpreted as implicit feedback data from an IDataReader, e.g. a database via DbDataReader.
//   * @param reader the IDataReader to be read from
//   * @param rating_threshold the minimum rating value needed to be accepted as positive feedback
//   * @param user_mapping user <see cref="IEntityMapping"/> object
//   * @param item_mapping item <see cref="IEntityMapping"/> object
//   * @return a <see cref="IPosOnlyFeedback"/> object with the user-wise collaborative data
//   */
//  public static IPosOnlyFeedback Read(IDataReader reader, double rating_threshold, IEntityMapping user_mapping, IEntityMapping item_mapping) {
//    PosOnlyFeedback<SparseBooleanMatrix> feedback = new PosOnlyFeedback<SparseBooleanMatrix>(new SparseBooleanMatrix());
//
//    if (reader.FieldCount < 3)
//      throw new Exception("Expected at least 3 columns.");
//
//    while (reader.Read()) {
//      int user_id = user_mapping.ToInternalID(reader.GetInt32(0));
//      int item_id = item_mapping.ToInternalID(reader.GetInt32(1));
//      double rating = reader.GetDouble(2);
//
//      if (rating >= rating_threshold)
//        feedback.add(user_id, item_id);
//    }
//
//    return feedback;
//  }
  
}
