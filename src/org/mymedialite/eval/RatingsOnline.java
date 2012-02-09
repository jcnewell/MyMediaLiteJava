//Copyright (C) 2011, 2012 Zeno Gantner, Chris Newell
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
//

package org.mymedialite.eval;

import org.mymedialite.data.IRatings;
import org.mymedialite.ratingprediction.IIncrementalRatingPredictor;
import org.mymedialite.ratingprediction.IRatingPredictor;

/**
 * Online evaluation for rating prediction.
 * @version 2.03
 */
public class RatingsOnline {

  // Prevent instantiation.
  private RatingsOnline() {}
  
  /**
   * Online evaluation for rating prediction.
   * 
   * Every rating that is tested is added to the training set afterwards.
   * 
   * @param recommender rating predictor
   * @param ratings Test cases
   * @return a Dictionary containing the evaluation results
   */
  public static RatingPredictionEvaluationResults evaluateOnline(IRatingPredictor recommender, IRatings ratings) {
    if (recommender == null)
      throw new IllegalArgumentException("recommender is null!");

    if (ratings == null)
      throw new IllegalArgumentException("ratings is null!");

    if (!(recommender instanceof IIncrementalRatingPredictor))
      throw new IllegalArgumentException("recommender must be of type IIncrementalRatingPredictor");
    
    IIncrementalRatingPredictor incremental_recommender = (IIncrementalRatingPredictor)recommender;   

    double rmse = 0;
    double mae  = 0;
    double cbd  = 0;

    // Iterate in random order
    for (int index : ratings.randomIndex()) {
      double prediction = recommender.predict(ratings.users().get(index), ratings.items().get(index));
      double error = prediction - ratings.get(index);

      rmse += error * error;
      mae  += Math.abs(error);
      cbd  += Ratings.computeCBD(ratings.get(index), prediction, ratings.minRating(), ratings.maxRating());
      incremental_recommender.addRating(ratings.users().get(index), ratings.items().get(index), ratings.get(index));
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

}


