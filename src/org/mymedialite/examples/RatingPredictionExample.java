// Copyright (C) Zeno Gantner, Chris Newell
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

package org.mymedialite.examples;

import java.io.IOException;
import java.util.Map;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.io.RatingData;
import org.mymedialite.ratingprediction.MatrixFactorization;
import org.mymedialite.ratingprediction.RatingPredictor;

/**
 * Example program for Rating Predictors.
 * @version 2.03
 */
public class RatingPredictionExample {
  
  public static void main(String[] args) {
    double min_rating = 1;
    double max_rating = 5;
        
    // Load the data
    IEntityMapping user_mapping = new EntityMapping();
    IEntityMapping item_mapping = new EntityMapping();
    IRatings training_data;
    IRatings test_data;
    training_data = null;
    test_data = null;
    try {
      training_data = RatingData.read(args[0], user_mapping, item_mapping, false);
      test_data = RatingData.read(args[1], user_mapping, item_mapping, false);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Set up the recommender
    RatingPredictor recommender = new MatrixFactorization();
    recommender.setMinRating(min_rating);
    recommender.setMaxRating(max_rating);
    recommender.setRatings(training_data);
    recommender.train();

    // Measure the accuracy on the test data set
    Map<String, Double> results = org.mymedialite.eval.Ratings.evaluate(recommender, test_data);
    System.out.println("RMSE=" + results.get("RMSE") + " MAE=" + results.get("MAE"));

    // Make a prediction for a certain user and item
    System.out.println(recommender.predict(user_mapping.toInternalID("1"), item_mapping.toInternalID("1")));
  }

}
