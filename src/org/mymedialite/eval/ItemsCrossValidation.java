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

package org.mymedialite.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mymedialite.IIterativeModel;
import org.mymedialite.IRecommender;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.ISplit;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedbackCrossValidationSplit;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.itemrec.ItemRecommender;

/**
 * Cross-validation for item recommendation.
 * @version 2.03
 */
public class ItemsCrossValidation {
  
  // Prevent instantiation.
  private ItemsCrossValidation() {}

  
  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender an item recommender
   * @param num_folds the number of folds
   * @param test_users a collection of integers with all test users
   * @param candidate_items a collection of integers with all candidate items
   * @param candidate_item_mode the mode used to determine the candidate items
   * @param show_results set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   */
  public static ItemRecommendationEvaluationResults doCrossValidation(
      IRecommender recommender,
      int num_folds,
      List<Integer> test_users,
      List<Integer> candidate_items,
      CandidateItems candidate_item_mode,
      boolean show_results) throws Exception {
    
    if (!(recommender instanceof ItemRecommender))
      throw new IllegalArgumentException("recommender must be of type ItemRecommender");

    if(candidate_item_mode == null)
      candidate_item_mode = CandidateItems.OVERLAP;
    
    List<IPosOnlyFeedback> train = new ArrayList<IPosOnlyFeedback>(num_folds);
    List<IPosOnlyFeedback> test = new ArrayList<IPosOnlyFeedback>(num_folds);
    for(int i=0 ; i<num_folds; i++) {
      train.add(new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class));
      test.add(new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class));
    }
    
    PosOnlyFeedbackCrossValidationSplit<PosOnlyFeedback<SparseBooleanMatrix>> split = new PosOnlyFeedbackCrossValidationSplit<PosOnlyFeedback<SparseBooleanMatrix>>(((ItemRecommender) recommender).getFeedback(), num_folds, train, test);
    return doCrossValidation(recommender, split, test_users, candidate_items, candidate_item_mode, show_results);
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender an item recommender
   * @param split a dataset split
   * @param test_users a collection of integers with all test users
   * @param candidate_items a collection of integers with all candidate items
   * @param candidate_item_mode the mode used to determine the candidate items
   * @param show_results set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   * @throws Exception 
   */
  public static ItemRecommendationEvaluationResults doCrossValidation(IRecommender recommender,
      ISplit<IPosOnlyFeedback> split,
      List<Integer> test_users,
      List<Integer> candidate_items,
      CandidateItems candidate_item_mode,
      boolean show_results) throws Exception {

    if (!(recommender instanceof ItemRecommender))
      throw new IllegalArgumentException("recommender must be of type ItemRecommender");
    
    if(candidate_item_mode == null) 
      candidate_item_mode = CandidateItems.OVERLAP;
    
    ItemRecommendationEvaluationResults avg_results = new ItemRecommendationEvaluationResults();

    for(int fold = 0 ; fold < split.numberOfFolds(); fold++) {
      try {
        ItemRecommender split_recommender = ((ItemRecommender) recommender).clone();  // avoid changes : recommender
        split_recommender.setFeedback(split.train().get(fold));
        split_recommender.train();
        ItemRecommendationEvaluationResults fold_results = Items.evaluate(split_recommender, split.train().get(fold), split.test().get(fold), test_users, candidate_items, candidate_item_mode, false);

        for (String key : fold_results.keySet()) {
          if (avg_results.containsKey(key))
            avg_results.put(key, avg_results.get(key) + fold_results.get(key));
          else
            avg_results.put(key, fold_results.get(key));

        }
        
        if (show_results)
          System.out.println("fold " + fold + " " + fold_results);

      } catch (Exception e) {
        System.err.println("===> ERROR: " + e.getMessage() + e.getStackTrace());
        throw e;
      }
    }

    for (String key : Items.getMeasures())
      avg_results.put(key, avg_results.get(key) / split.numberOfFolds());
    
    avg_results.put("num_users", avg_results.get("num_users") / split.numberOfFolds());
    avg_results.put("num_items", avg_results.get("num_items") / split.numberOfFolds());
    return avg_results;
  }

  /**
   * Evaluate an iterative recommender on the folds of a dataset split, display results on STDOUT.
   * @param recommender an item recommender
   * @param num_folds the number of folds
   * @param test_users a collection of integers with all test users
   * @param candidate_items a collection of integers with all candidate items
   * @param candidate_item_mode the mode used to determine the candidate items
   * @param repeated_events allow repeated events in the evaluation (i.e. items accessed by a user before may be in the recommended list)
   * @param max_iter the maximum number of iterations
   * @param find_iter the report interval
   */
  public static void doIterativeCrossValidation(
      IRecommender recommender,
      int num_folds,
      List<Integer> test_users,
      List<Integer> candidate_items,
      CandidateItems candidate_item_mode,
      boolean repeated_events,
      int max_iter,
      int find_iter) throws Exception {
   
    // find_iter = 1
    
    if (!(recommender instanceof ItemRecommender))
      throw new IllegalArgumentException("recommender must be of type ItemRecommender");
    
    List<IPosOnlyFeedback> train = new ArrayList<IPosOnlyFeedback>(num_folds);
    List<IPosOnlyFeedback> test = new ArrayList<IPosOnlyFeedback>(num_folds);
    for(int i=0 ; i<num_folds; i++) {
      train.add(new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class));
      test.add(new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class));
    }

    PosOnlyFeedbackCrossValidationSplit<PosOnlyFeedback<SparseBooleanMatrix>> split = new PosOnlyFeedbackCrossValidationSplit<PosOnlyFeedback<SparseBooleanMatrix>>(((ItemRecommender) recommender).getFeedback(), num_folds, train, test);
    doIterativeCrossValidation(recommender, split, test_users, candidate_items, candidate_item_mode, repeated_events, max_iter, find_iter);
  }

  /**
   * Evaluate an iterative recommender on the folds of a dataset split, display results on STDOUT.
   * @param recommender an item recommender
   * @param split a positive-only feedback dataset split
   * @param test_users a collection of integers with all test users
   * @param candidate_items a collection of integers with all candidate items
   * @param candidate_item_mode the mode used to determine the candidate items
   * @param repeated_events allow repeated events in the evaluation (i.e. items accessed by a user before may be in the recommended list)
   * @param max_iter the maximum number of iterations
   * @param find_iter the report interval
   * @throws Exception 
   */
  public static void doIterativeCrossValidation(
      IRecommender recommender,
      ISplit<IPosOnlyFeedback> split,
      List<Integer> test_users,
      List<Integer> candidate_items,
      CandidateItems candidate_item_mode,
      boolean repeated_events,
      int max_iter,
      int find_iter) throws Exception {
    
    if (!(recommender instanceof IIterativeModel))
      throw new IllegalArgumentException("recommender must be of type IIterativeModel");
    if (!(recommender instanceof ItemRecommender))
      throw new IllegalArgumentException("recommender must be of type ItemRecommender");

    ItemRecommender[] split_recommenders     = new ItemRecommender[split.numberOfFolds()];
    IIterativeModel[] iterative_recommenders = new IIterativeModel[split.numberOfFolds()];

    // initial training and evaluation
    for(int i=0; i < split.numberOfFolds(); i ++) {
      try {
        split_recommenders[i] = ((ItemRecommender) recommender).clone(); // to avoid changes : recommender
        split_recommenders[i].setFeedback(split.train().get(i));
        split_recommenders[i].train();
        iterative_recommenders[i] = (IIterativeModel) split_recommenders[i];
        ItemRecommendationEvaluationResults fold_results = Items.evaluate(split_recommenders[i], split.test().get(i), split.train().get(i), test_users, candidate_items, candidate_item_mode, repeated_events);
        System.out.println("fold " + i + " " + fold_results + " iteration " + iterative_recommenders[i].getNumIter());
      } catch (Exception e) {
        System.err.println("===> ERROR: " + e.getMessage() + e.getStackTrace());
        throw e;
      }
    }

    // iterative training and evaluation
    for (int it = iterative_recommenders[0].getNumIter() + 1; it <= max_iter; it++)
      for(int i = 0; i < split.numberOfFolds(); i++) {
        try {
          iterative_recommenders[i].iterate();

          if (it % find_iter == 0) {
            ItemRecommendationEvaluationResults fold_results = Items.evaluate(split_recommenders[i], split.test().get(i), split.train().get(i), test_users, candidate_items, candidate_item_mode, repeated_events);
            System.out.println("fold " + i + " " + fold_results + " iteration " + it);
          }
        } catch (Exception e) {
          System.err.println("===> ERROR: " + e.getMessage() + e.getStackTrace());
          throw e;
        }
      }
  }
}

