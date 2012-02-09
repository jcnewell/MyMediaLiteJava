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
//

package org.mymedialite.eval;

import java.util.HashMap;
import org.mymedialite.IIterativeModel;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.ISplit;
import org.mymedialite.data.RatingCrossValidationSplit;
import org.mymedialite.ratingprediction.RatingPredictor;

/**
 * Cross-validation for rating prediction.
 * @version 2.03
 */
public class RatingsCrossValidation {

  // Prevent instantiation.
  private RatingsCrossValidation() { }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender a rating predictor
   * @param num_folds the number of folds
   * @param compute_fit if set to true measure fit on the training data as well
   * @param show_results if set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   * @throws Exception 
   */
  public static RatingPredictionEvaluationResults doCrossValidation(RatingPredictor recommender,
      Integer num_folds,
      Boolean compute_fit,
      Boolean show_results) throws Exception {

    if(num_folds == null)    num_folds = 5;
    if(compute_fit == null)  compute_fit = false;
    if(show_results == null) show_results = false;

    RatingCrossValidationSplit split = new RatingCrossValidationSplit(recommender.getRatings(), num_folds);
    return doCrossValidation(recommender, split, compute_fit, show_results);
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender a rating predictor
   * @param split a rating dataset split
   * @param compute_fit if set to true measure fit on the training data as well
   * @param show_results set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   * @throws Exception 
   */
  public static RatingPredictionEvaluationResults doCrossValidation(
      RatingPredictor recommender,
      ISplit<IRatings> split,
      Boolean compute_fit,
      Boolean show_results) throws Exception {

    if(compute_fit == null)  compute_fit = false;
    if(show_results == null) show_results = false;

    RatingPredictionEvaluationResults avg_results = new RatingPredictionEvaluationResults();

    for(int i = 0; i < split.numberOfFolds(); i++) {
      try {
        RatingPredictor split_recommender = recommender.clone(); // to avoid changes : recommender
        split_recommender.setRatings(split.train().get(i));
        split_recommender.train();
        HashMap<String, Double> fold_results = Ratings.evaluate(split_recommender, split.test().get(i));
        if (compute_fit)
          fold_results.put("fit", new Double(Ratings.computeFit(split_recommender)));

        for (String key : fold_results.keySet())
          if (avg_results.containsKey(key))
            avg_results.put(key, avg_results.get(key) + fold_results.get(key));
          else
            avg_results.put(key, fold_results.get(key));

        if (show_results)
          System.out.println("fold " + i + " " + fold_results);
      } catch (Exception e) {
        System.err.println("===> ERROR: " + e.getMessage());
        throw e;
      }
    }

    for (String key : Ratings.getMeasures())
      avg_results.put(key, avg_results.get(key) / split.numberOfFolds());

    return avg_results;
  }

  /**
   * Evaluate an iterative recommender on the folds of a dataset split, display results on STDOUT.
   * @param recommender a rating predictor
   * @param num_folds the number of folds
   * @param max_iter the maximum number of iterations
   * @param find_iter the report interval
   * @throws Exception 
   */
  public static void doIterativeCrossValidation(RatingPredictor recommender, int num_folds, int max_iter, Integer find_iter) throws Exception {
    RatingCrossValidationSplit split = new RatingCrossValidationSplit(recommender.getRatings(), num_folds);
    doIterativeCrossValidation(recommender, split, max_iter, find_iter);
  }

  /**
   * Evaluate an iterative recommender on the folds of a dataset split, display results on STDOUT.
   * @param recommender a rating predictor
   * @param split a rating dataset split
   * @param max_iter the maximum number of iterations
   * @param find_iter the report interval
   * @throws Exception 
   */
  public static void doIterativeCrossValidation(RatingPredictor recommender, ISplit<IRatings> split, int max_iter, Integer find_iter) throws Exception {
    if(find_iter == null) find_iter = 1;
    
    if (!(recommender instanceof IIterativeModel))
      throw new IllegalArgumentException("recommender must be of type IIterativeModel");

    RatingPredictor[] split_recommenders     = new RatingPredictor[split.numberOfFolds()];
    IIterativeModel[] iterative_recommenders = new IIterativeModel[split.numberOfFolds()];

    // Initial training and evaluation
    for(int i=0; i<split.numberOfFolds(); i++) {
      try {
        split_recommenders[i] = recommender.clone(); // to avoid changes : recommender
        split_recommenders[i].setRatings(split.train().get(i));
        split_recommenders[i].train();
        iterative_recommenders[i] = (IIterativeModel) split_recommenders[i];
        HashMap<String, Double> fold_results = Ratings.evaluate(split_recommenders[i], split.test().get(i));
        System.out.println("fold " + i + " " + fold_results + " iteration " + iterative_recommenders[i].getNumIter());
      } catch (Exception e) {
        System.err.println("===> ERROR: " + e.getMessage());
        throw e;
      }
    }

    // Iterative training and evaluation
    for (int it = iterative_recommenders[0].getNumIter() + 1; it <= max_iter; it++) {
      for(int i=0; i<split.numberOfFolds(); i++) {
        try {
          iterative_recommenders[i].iterate();

          if (it % find_iter == 0) {
            HashMap<String, Double> fold_results = Ratings.evaluate(split_recommenders[i], split.test().get(i));
            System.out.println("fold " + i + " " + fold_results + " iteration " + it);
          }
        } catch (Exception e) {
          System.err.println("===> ERROR: " + e.getMessage());
          throw e;
        }
      }
    }
  }
  
}


