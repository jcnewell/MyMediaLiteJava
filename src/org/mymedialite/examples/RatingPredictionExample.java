package org.mymedialite.examples;

import java.io.IOException;
import java.util.Map;

import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.IRatings;
import org.mymedialite.io.RatingPrediction;
import org.mymedialite.ratingprediction.MatrixFactorization;
import org.mymedialite.ratingprediction.RatingPredictor;

public class RatingPredictionExample {
  
  public static void main(String[] args) {
    double min_rating = 1;
    double max_rating = 5;
        
    // load the data
    EntityMapping user_mapping = new EntityMapping();
    EntityMapping item_mapping = new EntityMapping();
    IRatings training_data;
    IRatings test_data;
    training_data = null;
    test_data = null;
    try {
      training_data = RatingPrediction.read(args[0], user_mapping, item_mapping);
      test_data = RatingPrediction.read(args[1], user_mapping, item_mapping);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

        // set up the recommender
    RatingPredictor recommender = new MatrixFactorization();
    recommender.setMinRating(min_rating);
    recommender.setMaxRating(max_rating);
    recommender.setRatings(training_data);
    recommender.train();

    // measure the accuracy on the test data set
    Map<String, Double> results = org.mymedialite.eval.Ratings.evaluate(recommender, test_data);
    System.out.println("RMSE=" + results.get("RMSE") + " MAE=" + results.get("MAE"));

    // make a prediction for a certain user and item
    System.out.println(recommender.predict(user_mapping.toInternalID(1), item_mapping.toInternalID(1)));
  }

}
