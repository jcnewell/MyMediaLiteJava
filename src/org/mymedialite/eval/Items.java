// Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

import java.util.*;

import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.ISplit;
import org.mymedialite.datatype.IBooleanMatrix;
import org.mymedialite.eval.measures.AUC;
import org.mymedialite.eval.measures.NDCG;
import org.mymedialite.eval.measures.PrecisionAndRecall;
import org.mymedialite.eval.measures.ReciprocalRank;
import org.mymedialite.IRecommender;
import org.mymedialite.itemrec.Extensions;
import org.mymedialite.itemrec.ItemRecommender;
import org.mymedialite.util.Utils;

/**
 * Evaluation class for item recommendation.
 * @version 2.03
 */
public class Items {

  // this is a static class, but Java does not allow us to declare that ;-)
  private Items() {}

  /**
   * Get the evaluation measures for item prediction offered by the class.
   */
  static public Collection<String> getMeasures() { 
    String[] array = { "AUC", "prec@5", "prec@10", "MAP", "recall@5", "recall@10", "NDCG", "MRR" };
    return Arrays.asList(array);
  }

  /**
   * Evaluation for rankings of item recommenders.
   * User-item combinations that appear in both sets are ignored for the test set, and thus in the evaluation.
   * The evaluation measures are listed in the ItemPredictionMeasures property.
   * Additionally, 'num_users' and 'num_items' report the number of users that were used to compute the results
   * and the number of items that were taken into account.
   * 
   * Literature:
   *   C. Manning, P. Raghavan, H. Sch&uuml;tze: Introduction to Information Retrieval, Cambridge University Press, 2008
   * 
   * @param recommender item recommender
   * @param test test cases
   * @param train training data
   * @param test_users a collection of integers with all relevant_users
   * @param candidate_items a collection of integers with all candidate items
   * @return a dictionary containing the evaluation results
   */
  public static ItemRecommendationEvaluationResults evaluate(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback train,
      Collection<Integer> test_users,
      Collection<Integer> candidate_items) {
    return evaluate(recommender, test, train, test_users, candidate_items, null, null);
  }

  /**
   * Evaluation for rankings of items.
   * 
   * User-item combinations that appear in both sets are ignored for the test set, and thus in the evaluation,
   * except when the boolean argument repeated_events is set.
   * 
   * The evaluation measures are listed in the ItemPredictionMeasures property.
   * Additionally, 'num_users' and 'num_items' report the number of users that were used to compute the results
   * and the number of items that were taken into account.
   *
   * Literature:
   *   C. Manning, P. Raghavan, H. Sch&uuml;tze: Introduction to Information Retrieval, Cambridge University Press, 2008
   *
   * @param recommender item recommender
   * @param test test cases
   * @param training training data
   * @param test_users a collection of integers with all relevant users
   * @param candidate_items a collection of integers with all relevant items
   * @param candidate_item_mode the mode used to determine the candidate items. The default is CandidateItems.OVERLAP
   * @param repeated_events allow repeated events in the evaluation (i.e. items accessed by a user before may be in the recommended list). The default is false.
   * @return a dictionary containing the evaluation results
   */
  public static ItemRecommendationEvaluationResults evaluate(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback training,
      Collection<Integer> test_users,
      Collection<Integer> candidate_items,
      CandidateItems candidate_item_mode,
      Boolean repeated_events) {

    if(candidate_item_mode == null)  candidate_item_mode = CandidateItems.OVERLAP;
    if(repeated_events == null)  repeated_events = false;
    
    if(candidate_item_mode.equals(CandidateItems.TRAINING)) {
      candidate_items = training.allItems();
    } else if(candidate_item_mode.equals(CandidateItems.TEST)) {
      candidate_items = test.allItems();
    } else if(candidate_item_mode.equals(CandidateItems.OVERLAP)) {
      candidate_items = Utils.intersect(test.allItems(), training.allItems());
    } else if(candidate_item_mode.equals(CandidateItems.UNION)) {
      candidate_items = Utils.union(test.allItems(), training.allItems());
    }
    
    if (candidate_items == null)
      throw new IllegalArgumentException("candidate_items == null!");
    if (test_users == null)
      test_users = test.allUsers();
    
    int num_users = 0;
    ItemRecommendationEvaluationResults result = new ItemRecommendationEvaluationResults();
    
    IBooleanMatrix training_user_matrix = training.userMatrix();
    IBooleanMatrix test_user_matrix     = test.userMatrix();
    
    for (Integer user_id : test_users) {
      // Items viewed by the user in the test set that were also present in the training set.
      HashSet<Integer> correct_items = new HashSet<Integer>(Utils.intersect(test_user_matrix.get(user_id), candidate_items));

      // The number of items that will be used for this user.
      HashSet<Integer> candidate_items_in_train = new HashSet<Integer> (Utils.intersect(training_user_matrix.get(user_id), candidate_items));

      int num_eval_items = candidate_items.size() - (repeated_events ? 0 : candidate_items_in_train.size());

      // Skip all users that have 0 or #relevant_items test items.
      if (correct_items.size() == 0) continue;
      if (num_eval_items - correct_items.size() == 0) continue;


      List<Integer> prediction_list = Extensions.predictItems(recommender, user_id, candidate_items);    
      //System.out.println("correct_items:   "  + correct_items.size());
      //System.out.println("num_eval_items:  "  + num_eval_items);
      //System.out.println("candidate_items: " + candidate_items.size());
      //System.out.println("training items:  "  + training.getUserMatrix().getRow(user_id).size());
      //System.out.println("prediction_list: " + prediction_list.size());
      if (prediction_list.size() != candidate_items.size()) throw new RuntimeException("Not all items have been ranked.");

      Collection<Integer> ignore_items = repeated_events ? new ArrayList<Integer>() : training_user_matrix.get(user_id);

      double auc     = AUC.compute(prediction_list, correct_items, ignore_items);
      double map     = PrecisionAndRecall.AP(prediction_list, correct_items, ignore_items);
      double ndcg    = NDCG.compute(prediction_list, correct_items, ignore_items);
      double rr      = ReciprocalRank.compute(prediction_list, correct_items, ignore_items); 

      int[] positions = new int[] { 5, 10 };
      Map<Integer, Double> prec = PrecisionAndRecall.precisionAt(prediction_list, correct_items, ignore_items, positions);
      Map<Integer, Double> recall = PrecisionAndRecall.recallAt(prediction_list, correct_items, ignore_items, positions);

      num_users++;
      result.put("AUC",       result.get("AUC")       + auc);
      result.put("MAP",       result.get("MAP")       + map);
      result.put("NDCG",      result.get("NDCG")      + ndcg);
      result.put("MRR",       result.get("MRR")       + rr);
      result.put("prec@5",    result.get("prec@5")    + prec.get(5));
      result.put("prec@10",   result.get("prec@10")   + prec.get(10));
      result.put("recall@5",  result.get("recall@5")  + recall.get(5));
      result.put("recall@10", result.get("recall@10") + recall.get(10));

      if (num_users % 1000 == 0)
        System.err.println(".");
      if (num_users % 60000 == 0)
        System.err.println();

    }
    
    for(String measure : getMeasures())
      result.put(measure, result.get(measure) / num_users);
    
    result.put("num_users", (double)num_users);
    result.put("num_lists", (double)num_users);
    result.put("num_items", (double)candidate_items.size());
    
    return result;
  }

  /**
   * Format item prediction results.
   * @param result the result dictionary
   * @return a string containing the results
   */
  public static String formatResults(Map<String, Double> result) {
    String string = "AUC " + result.get("AUC") +
        " prec@5 "         + result.get("prec@5") +
        " prec@10 "        + result.get("prec@10") +
        " MAP "            + result.get("MAP") +
        " recall@5 "       + result.get("recall@5") +
        " recall@10 "      + result.get("recall@10") +
        " NDCG "           + result.get("NDCG") +
        " MRR "            + result.get("MRR") +
        " num_users "      + result.get("num_users") +
        " num_items "      + result.get("num_items") +
        " num_lists "      + result.get("num_lists");
    return string;
  }

  /** 
   * Display item prediction results.
   * @param result the result dictionary
   */
  static public void displayResults(HashMap<String, Double> result) {
    System.out.println("AUC        " + result.get("AUC"));
    System.out.println("prec@5     " + result.get("prec@5"));
    System.out.println("prec@10    " + result.get("prec@10"));
    System.out.println("MAP        " + result.get("MAP"));
    System.out.println("recall@5   " + result.get("recall@5"));
    System.out.println("recall@10  " + result.get("recall@10"));
    System.out.println("NDCG       " + result.get("NDCG"));
    System.out.println("MRR        " + result.get("MRR"));
    System.out.println("num_users  "    + result.get("num_users"));
    System.out.println("num_items  "    + result.get("num_items"));
    System.out.println("num_lists  "    + result.get("num_lists")); 
  }

}