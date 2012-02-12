// Copyright (C) 2010 Zeno Gantner
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

package org.mymedialite.ratingprediction;

import java.io.IOException;
import java.io.PrintWriter;

import org.mymedialite.IRecommender;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.IdentityMapping;

/**
 * Class that contains static methods for rating prediction.
 * @version 2.03
 */
public class Extensions {

  // Prevent instantiation.
  private Extensions() {}

  /**
   * Rate a given set of instances and write it to a TextWriter.
   * @param recommender rating predictor
   * @param ratings test cases
   * @param writer the TextWriter to write the predictions to
   * @param userMapping an EntityMapping object for the user IDs
   * @param itemMapping an EntityMapping object for the item IDs
   * @param separator the field separator to use
   */
  public static void writePredictions(
      IRecommender recommender,
      IRatings ratings,
      PrintWriter writer,
      IEntityMapping userMapping,
      IEntityMapping itemMapping,
      String separator) {
    
    if (userMapping == null)
      userMapping = new IdentityMapping();
    if (itemMapping == null)
      itemMapping = new IdentityMapping();
    if (separator== null)
      separator = "\t";
    
    for (int index = 0; index < ratings.size(); index++) {
      writer.println(userMapping.toOriginalID(ratings.users().get(index)) + separator + itemMapping.toOriginalID(ratings.items().get(index)) +
        separator + recommender.predict(ratings.users().get(index), ratings.items().get(index)));
    }
  }

  /**
   * Rate a given set of instances and write it to a file.
   * @param recommender rating predictor
   * @param ratings test cases
   * @param filename the name of the file to write the predictions to
   * @param userMapping an EntityMapping object for the user IDs
   * @param itemMapping an EntityMapping object for the item IDs
   * @param separator the field separator to use
   */
  public static void writePredictions(
      IRecommender recommender,
      IRatings ratings,
      String filename,
      IEntityMapping userMapping,
      IEntityMapping itemMapping,
      String separator) throws IOException {
    
    PrintWriter writer = new PrintWriter(filename);
    writePredictions(recommender, ratings, writer, userMapping, itemMapping, separator);
  }
}
