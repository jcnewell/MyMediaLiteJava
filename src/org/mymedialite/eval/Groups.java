// Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.eval.measures.AUC;
import org.mymedialite.eval.measures.NDCG;
import org.mymedialite.eval.measures.PrecisionAndRecall;
import org.mymedialite.eval.measures.ReciprocalRank;
import org.mymedialite.grouprec.GroupRecommender;

/**
 * Evaluation class for group recommendation.
 * @version 2.03
 */
public class Groups {

  // Prevent instantiation.
  private Groups() {}

  /**
   * Evaluation for rankings of items recommended to groups.
   * 
   * 
   * @param recommender group recommender
   * @param test test cases
   * @param train training data
   * @param group_to_user group to user relation
   * @param candidate_items a collection of integers with all candidate items
   * @param ignore_overlap if true, ignore items that appear for a group in the training set when evaluating for that user
   * @return a dictionary containing the evaluation results
   * @throws Exception 
   */
  public static ItemRecommendationEvaluationResults evaluate(
      GroupRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback train,
      SparseBooleanMatrix group_to_user,
      Collection<Integer> candidate_items,
      boolean ignore_overlap) throws Exception {

    ItemRecommendationEvaluationResults result = new ItemRecommendationEvaluationResults();

    int num_groups = 0;

    for (int group_id : group_to_user.nonEmptyRowIDs()) {
      
      List<Integer> users = group_to_user.getEntriesByRow(group_id);
      HashSet<Integer> correct_items = new HashSet<Integer>();

      for (int user_id : users)
        correct_items.addAll(test.userMatrix().get(user_id));
      
      correct_items.retainAll(candidate_items);

      HashSet<Integer> candidate_items_in_train = new HashSet<Integer>();

      for (int user_id : users)
        candidate_items_in_train.addAll(train.userMatrix().get(user_id));

      candidate_items_in_train.retainAll(candidate_items);
      int num_eval_items = candidate_items.size() - (ignore_overlap ? candidate_items_in_train.size() : 0);

      // skip all groups that have 0 or #candidate_items test items
      if (correct_items.size() == 0) continue;
      if (num_eval_items - correct_items.size() == 0) continue;

      List<Integer> prediction_list = recommender.rankItems(users, candidate_items);
      if (prediction_list.size() != candidate_items.size())
        throw new Exception("Not all items have been ranked.");

      HashSet<Integer> ignore_items = ignore_overlap ? candidate_items_in_train : new HashSet<Integer>();

      double auc  = AUC.compute(prediction_list, correct_items, ignore_items);
      double map  = PrecisionAndRecall.AP(prediction_list, correct_items, ignore_items);
      double ndcg = NDCG.compute(prediction_list, correct_items, ignore_items);
      double rr   = ReciprocalRank.compute(prediction_list, correct_items, ignore_items);
      int[] positions = new int[] { 5, 10 };
      HashMap<Integer, Double> prec   = PrecisionAndRecall.precisionAt(prediction_list, correct_items, ignore_items, positions);
      HashMap<Integer, Double> recall = PrecisionAndRecall.recallAt(prediction_list, correct_items, ignore_items, positions);

      // thread-safe incrementing
      num_groups++;
      result.put("AUC",       result.get("AUC") + auc);
      result.put("MAP",       result.get("MAP") + map);
      result.put("NDCG",      result.get("NDCG") + ndcg);
      result.put("MRR",       result.get("MRR") + rr);
      result.put("prec@5",    result.get("prec@5") + prec.get(5));
      result.put("prec@10",   result.get("prec@10") + prec.get(10));
      result.put("recall@5",  result.get("recall@5") + recall.get(5));
      result.put("recall@10", result.get("recall@10") + recall.get(10));

      if (num_groups % 1000 == 0)
        System.err.print(".");
      if (num_groups % 60000 == 0)
        System.err.println();
    }

    result.put("num_groups", (double)num_groups);
    result.put("num_lists",  (double)num_groups);
    result.put("num_items",  (double)candidate_items.size());

    return result;
  }
}
