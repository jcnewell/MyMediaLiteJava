// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mymedialite.IRecommender;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.eval.measures.AUC;
import org.mymedialite.eval.measures.NDCG;
import org.mymedialite.eval.measures.PrecisionAndRecall;
import org.mymedialite.eval.measures.ReciprocalRank;

/**
 * Evaluation class for filtered item recommendation.
 * @version 2.03
 */
public class ItemsFiltered {
  
  // Prevent instantiation.
  private ItemsFiltered() {}

  /**
   * For a given user and the test dataset, return a dictionary of items filtered by attributes.
   * @param user_id the user ID
   * @param test the test dataset
   * @param item_attributes 
   * @return a dictionary containing a mapping from attribute IDs to collections of item IDs
   */
  public static HashMap<Integer, Collection<Integer>> getFilteredItems(int user_id, IPosOnlyFeedback test, SparseBooleanMatrix item_attributes) {

    HashMap<Integer, Collection<Integer>> filtered_items = new HashMap<Integer, Collection<Integer>>();

    for (int item_id : test.userMatrix().get(user_id)) {
      for (int attribute_id : item_attributes.get(item_id)) {
        if (!filtered_items.containsKey(attribute_id)) filtered_items.put(attribute_id, new HashSet<Integer>());
        filtered_items.get(attribute_id).add(item_id);
      }
    }
    return filtered_items;
  }

  /**
   * Evaluation for rankings of filtered items.
   * 
   * 
   * @param recommender item recommender
   * @param test test cases
   * @param train training data
   * @param item_attributes the item attributes to be used for filtering
   * @param test_users a collection of integers with all test users
   * @param candidate_items a collection of integers with all candidate items
   * @param repeated_events allow repeated events in the evaluation (i.e. items accessed by a user before may be in the recommended list)
   * @return a dictionary containing the evaluation results
   * @throws Exception 
   */
  public static ItemRecommendationEvaluationResults evaluateFiltered(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback train,
      SparseBooleanMatrix item_attributes,
      List<Integer> test_users,
      List<Integer> candidate_items,
      boolean repeated_events) throws Exception {

    SparseBooleanMatrix items_by_attribute = (SparseBooleanMatrix) item_attributes.transpose();

    int num_users = 0;
    int num_lists = 0;
    ItemRecommendationEvaluationResults result = new ItemRecommendationEvaluationResults();

    result.put("AUC", 0.0);
    result.put("MAP", 0.0);
    result.put("NDCG", 0.0);
    result.put("MRR", 0.0);
    result.put("prec@5", 0.0);
    result.put("prec@10", 0.0);
    result.put("recall@5", 0.0);
    result.put("recall@10", 0.0);
    
    // TODO explore parallel processing options
    OUTER:
      for (int user_id : test_users) {
        HashMap<Integer, Collection<Integer>> filtered_items = getFilteredItems(user_id, test, item_attributes);
        int last_user_id = -1;

        for (int attribute_id : filtered_items.keySet()) {
          HashSet<Integer> filtered_candidate_items = new HashSet<Integer>(items_by_attribute.get(attribute_id));
          filtered_candidate_items.retainAll(candidate_items);

          HashSet<Integer> correct_items = new HashSet<Integer>(filtered_items.get(attribute_id));
          correct_items.retainAll(filtered_candidate_items);

          // the number of candidate items for this user
          HashSet<Integer> candidate_items_in_train = new HashSet<Integer>(train.userMatrix().get(user_id));
          candidate_items_in_train.retainAll(filtered_candidate_items);
          int num_eval_items = filtered_candidate_items.size() - candidate_items_in_train.size();

          // skip all users that have 0 or #filtered_candidate_items test items
          if (correct_items.size() == 0)
            continue OUTER;
          if (num_eval_items - correct_items.size() == 0)
            continue OUTER;

          // evaluation
          List<Integer> prediction_list = org.mymedialite.itemrec.Extensions.predictItems(recommender, user_id, filtered_candidate_items);
          Collection<Integer> ignore_items = repeated_events ? new ArrayList<Integer>() : train.userMatrix().get(user_id);

          double auc  = AUC.compute(prediction_list, correct_items, ignore_items);
          double map  = PrecisionAndRecall.AP(prediction_list, correct_items, ignore_items);
          double ndcg = NDCG.compute(prediction_list, correct_items, ignore_items);
          double rr   = ReciprocalRank.compute(prediction_list, correct_items, ignore_items);
          int[] positions = new int[] { 5, 10 };
          HashMap<Integer, Double> prec = PrecisionAndRecall.precisionAt(prediction_list, correct_items, ignore_items, positions);
          HashMap<Integer, Double> recall = PrecisionAndRecall.recallAt(prediction_list, correct_items, ignore_items, positions);

          // counting stats
          num_lists++;
          if (last_user_id != user_id) {
            last_user_id = user_id;
            num_users++;
          }

          // result bookkeeping
          result.put("AUC",       result.get("AUC")       + auc);
          result.put("MAP",       result.get("MAP")       + map);
          result.put("NDCG",      result.get("NDCG")      + ndcg);
          result.put("MRR",       result.get("MRR")       + rr);
          result.put("prec@5",    result.get("prec@5")    + prec.get(5));
          result.put("prec@10",   result.get("prec@10")   + prec.get(10));
          result.put("recall@5",  result.get("recall@5")  + recall.get(5));
          result.put("recall@10", result.get("recall@10") + recall.get(10));
          
          if (prediction_list.size() != filtered_candidate_items.size())
            throw new Exception("Not all items have been ranked.");

          if (num_lists % 5000 == 0)
            System.err.print(".");
          if (num_lists % 300000 == 0)
            System.err.println();
        }
      }

    for (String measure : Items.getMeasures())
      result.put(measure, result.get(measure) / num_lists);
    
    result.put("num_users", (double)num_users);
    result.put("num_lists", (double)num_lists);
    result.put("num_items", (double)candidate_items.size());

    return result;
  }

  // TODO implement online eval
}
