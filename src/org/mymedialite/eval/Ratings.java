// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011, 2012 Zeno Gantner, Chris Newell
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

package org.mymedialite.eval;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mymedialite.data.IRatings;
import org.mymedialite.data.ISplit;
import org.mymedialite.data.ITimedRatings;
import org.mymedialite.ratingprediction.IRatingPredictor;
import org.mymedialite.ratingprediction.ITimeAwareRatingPredictor;
import org.mymedialite.ratingprediction.RatingPredictor;

/**
 * Evaluation class for rating prediction.
 * @version 2.03
 */
public class Ratings {
  
  /** Prevent instantiation. */
  private Ratings() { }
  
  /**
   * The evaluation measures for rating prediction offered by the class.
   * 
   * See http://recsyswiki.com/wiki/Root_mean_square_error and http://recsyswiki.com/wiki/Mean_absolute_error
   * 
   */
  public static Collection<String> getMeasures() {
    Set<String> measures = new HashSet<String>();
    measures.add("RMSE");
    measures.add("MAE");
    measures.add("NMAE");
    measures.add("CBD");
    return measures;
  }

  /**
   * Evaluates a rating predictor for RMSE, (N)MAE, and CBD.
   * 
   * See http://recsyswiki.com/wiki/Root_mean_square_error and http://recsyswiki.com/wiki/Mean_absolute_error
   *   
   * For NMAE, see the paper by Goldberg et al.
   *   
   * For CBD (capped binomial deviance), see http://www.kaggle.com/c/ChessRatings2/Details/Evaluation
   *   
   * If the recommender can take time into account, and the rating dataset provides rating times,
   * this information will be used for making rating predictions.
   *   
   * Literature:
   *     Ken Goldberg, Theresa Roeder, Dhruv Gupta, and Chris Perkins:
   *     Eigentaste: A Constant Time Collaborative Filtering Algorithm.
   *     Information Retrieval Journal 2001.
   *     http://goldberg.berkeley.edu/pubs/eigentaste.pdf
   * 
   * @param recommender rating predictor
   * @param ratings Test cases
   * @return a Dictionary containing the evaluation results
   */
  public static RatingPredictionEvaluationResults evaluate(IRatingPredictor recommender, IRatings ratings) {
    double rmse = 0;
    double mae  = 0;
    double cbd  = 0;

    if (recommender == null)
      throw new IllegalArgumentException("recommender = null");
    if (ratings == null)
      throw new IllegalArgumentException("ratings = null");

    if (recommender instanceof ITimeAwareRatingPredictor && ratings instanceof ITimedRatings) {
      ITimeAwareRatingPredictor time_aware_recommender = (ITimeAwareRatingPredictor) recommender;
      ITimedRatings timed_ratings = (ITimedRatings) ratings;
      for (int index = 0; index < ratings.size(); index++) {
        double prediction = time_aware_recommender.predict(timed_ratings.users().get(index), timed_ratings.items().get(index), timed_ratings.times().get(index));
        double error = prediction - ratings.get(index);

        rmse += error * error;
        mae  += Math.abs(error);
        cbd  += computeCBD(ratings.get(index), prediction, ratings.minRating(), ratings.maxRating());
      }
    } else {
      for (int index = 0; index < ratings.size(); index++) {
        double prediction = recommender.predict(ratings.users().get(index), ratings.items().get(index));
        double error = prediction - ratings.get(index);        
        rmse += error * error;
        mae  += Math.abs(error);
        cbd  += computeCBD(ratings.get(index), prediction, ratings.minRating(), ratings.maxRating());
      }
    }
    
    mae  = mae / ratings.size();
    rmse = Math.sqrt(rmse / ratings.size());
    cbd  = cbd / ratings.size();

    RatingPredictionEvaluationResults result = new RatingPredictionEvaluationResults();
    result.put("RMSE", rmse);
    result.put("MAE", mae);
    result.put("NMAE", mae / (recommender.getMaxRating() - recommender.getMinRating()));
    result.put("CBD", cbd);
    return result;
  }

  /**
   * Compute the capped binomial deviation (CBD).
   * 
   *   http://www.kaggle.com/c/ChessRatings2/Details/Evaluation
   * 
   * @return The CBD of a given rating and a prediction
   * @param actual_rating the actual rating
   * @param prediction the predicted rating
   * @param min_rating the lower bound of the rating scale
   * @param max_rating the upper bound of the rating scale
   */
  public static double computeCBD(double actual_rating, double prediction, double min_rating, double max_rating) {
    // Map into [0, 1] interval
    prediction    = (prediction - min_rating) / (max_rating - min_rating);
    actual_rating = (actual_rating - min_rating) / (max_rating - min_rating);

    // Cap predictions
    if (prediction < 0.01)
      prediction = 0.01;
    if (prediction > 0.99)
      prediction = 0.99;
    
    return -(actual_rating * Math.log10(prediction) + (1 - actual_rating) * Math.log10(1 - prediction));
  }

  /**
   * Computes the RMSE fit of a recommender on the training data.
   * @return the RMSE on the training data
   * @param recommender the rating predictor to evaluate
   */
  public static double computeFit(RatingPredictor recommender) {
    return evaluate(recommender, recommender.getRatings()).get("RMSE");
  }

}
