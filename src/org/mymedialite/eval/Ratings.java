// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011 Zeno Gantner, Chris Newell
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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mymedialite.data.IRatings;
import org.mymedialite.data.ISplit;
import org.mymedialite.ratingprediction.IIncrementalRatingPredictor;
import org.mymedialite.ratingprediction.IRatingPredictor;
import org.mymedialite.ratingprediction.RatingPredictor;

/** Evaluation class */
public class Ratings {
	/** the evaluation measures for rating prediction offered by the class */
	public static Collection<String> getMeasures() {
		HashSet<String> measures = new HashSet<String>();
		measures.add("RMSE");
		measures.add("MAE");
		measures.add("NMAE");
		return measures;
	}

  /** 
   * Format rating prediction results.
   * @param result the result dictionary
   * @return a string containing the results
   */
  public static String formatResults(Map<String, Double> result) {
    String results = "RMSE: " + result.get("RMSE") +
    "MAE:  " + result.get("MAE") +
    "NMAE: " + result.get("NMAE");
    return results;
  }

  /** 
   * Write the rating prediction results to STDOUT
   * @param result the result dictionary
   */
  public static void displayResults(Map<String, Double> result) {
    System.out.println("RMSE: " + result.get("RMSE"));
    System.out.println("MAE:  " + result.get("MAE"));
    System.out.println("NMAE: " + result.get("NMAE"));
  }

  /** 
   * Evaluates a rating predictor for RMSE, MAE, and NMAE
   * For NMAE, see "Eigentaste: A Constant Time Collaborative Filtering Algorithm" by Goldberg et al.
   * @param recommender Rating prediction engine
   * @param ratings Test cases
   * @return a Dictionary containing the evaluation results
   */
  public static HashMap<String, Double> evaluate(IRatingPredictor recommender, IRatings ratings) {
    double rmse = 0;
    double mae  = 0;

    if (recommender == null) throw new IllegalArgumentException("null recommender");
    if (ratings == null) throw new IllegalArgumentException("null ratings");

    for (int index = 0; index < ratings.size(); index++) {
      double error = (recommender.predict(ratings.getUsers().get(index), ratings.getItems().get(index)) - ratings.get(index));

      rmse += error * error;
      mae  += Math.abs(error);
    }
    mae  = mae / ratings.size();
    rmse = Math.sqrt(rmse / ratings.size());

    HashMap<String, Double> result = new HashMap<String, Double>();
    result.put("RMSE", rmse);
    result.put("MAE",  mae);
    result.put("NMAE", mae / (recommender.getMaxRating() - recommender.getMinRating()));
    return result;
  }

  /**
   * Online evaluation for rating prediction.
   * Every rating that is tested is added to the training set afterwards.
   * @param recommender rating predictor
   * @param ratings Test cases
   * @return a Dictionary containing the evaluation results
   */
  public static HashMap<String, Double> evaluateOnline(IIncrementalRatingPredictor recommender, IRatings ratings) throws IllegalArgumentException {
    double rmse = 0;
    double mae  = 0;

    if (recommender == null) throw new IllegalArgumentException("recommender");
    if (ratings == null)     throw new IllegalArgumentException("ratings");

    // Iterate in random order    // TODO also support chronological order
    for (int index : ratings.getRandomIndex()) {
      double error = (recommender.predict(ratings.getUsers().get(index), ratings.getItems().get(index)) - ratings.get(index));

      rmse += error * error;
      mae  += Math.abs(error);

      recommender.addRating(ratings.getUsers().get(index), ratings.getItems().get(index), ratings.get(index));
    }
    mae  = mae / ratings.size();
    rmse = Math.sqrt(rmse / ratings.size());

    HashMap<String, Double> result = new HashMap<String, Double>();
    result.put("RMSE", rmse);
    result.put("MAE", mae);
    result.put("NMAE", mae / (recommender.getMaxRating() - recommender.getMinRating()));
    return result;
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender a rating predictor
   * @param split a rating dataset split
   * @return a dictionary containing the average results over the different folds of the split
   */
  public static HashMap<String, Double> evaluateOnSplit(RatingPredictor recommender, ISplit<IRatings> split) {
    return evaluateOnSplit(recommender, split, false);
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender a rating predictor
   * @param split a rating dataset split
   * @param show_results set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   */
  public static HashMap<String, Double> evaluateOnSplit(
		  RatingPredictor recommender,
		  ISplit<IRatings> split,
		  boolean show_results) {
	  
	  HashMap<String, Double> avg_results = new HashMap<String, Double>();

	  for (int i = 0; i < split.getNumberOfFolds(); i++)
		  try {
			  RatingPredictor split_recommender = recommender.clone(); // to avoid changes : recommender
			  split_recommender.setRatings(split.train().get(i));
			  split_recommender.train();
			  HashMap<String, Double> fold_results = evaluate(split_recommender, split.test().get(i));
	
			  for (String key : fold_results.keySet()) {
				  if (avg_results.containsKey(key)) {
					  avg_results.put(key, avg_results.get(key) + fold_results.get(key));
				  } else {
					  avg_results.put(key, fold_results.get(key));
				  }
			  }
			  if (show_results)
				  System.err.println("fold " +  i + " " + formatResults(fold_results));
		  } catch (CloneNotSupportedException e) {
			  // nothing to do here
		  }

	  for (String key : avg_results.keySet()) {
		  avg_results.put(key, avg_results.get(key) / split.getNumberOfFolds());
	  }
	  return avg_results;
  }
}
