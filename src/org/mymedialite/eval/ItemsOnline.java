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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.mymedialite.IRecommender;
import org.mymedialite.itemrec.IIncrementalItemRecommender;
import org.mymedialite.util.Utils;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Online evaluation for rankings of items
 * @version 2.03
 */
public class ItemsOnline {

  //TODO consider micro- (by item) and macro-averaging (by user, the current thing); repeated events

  /**
   * Online evaluation for rankings of items.
   * @param recommender the item recommender to be evaluated
   * @param test test cases
   * @param training training data (must be connected to the recommender's training data)
   * @param test_users a list of all test user IDs
   * @param candidate_items a list of all candidate item IDs
   * @param  candidate_item_mode the mode used to determine the candidate items
   * @return a dictionary containing the evaluation results (averaged by user)
   */
  public static HashMap<String, Double> evaluate(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback training,
      List<Integer> test_users,
      List<Integer> candidate_items,
      CandidateItems candidate_item_mode) {

    if (!(recommender instanceof IIncrementalItemRecommender))
      throw new IllegalArgumentException("recommender must be of type IIncrementalItemRecommender");
    
    IIncrementalItemRecommender incremental_recommender = (IIncrementalItemRecommender)recommender;
   
    // prepare candidate items once to avoid recreating them
    if(candidate_item_mode.equals(CandidateItems.TRAINING))
      candidate_items = training.allItems();
    
    else if(candidate_item_mode.equals(CandidateItems.TEST))
      candidate_items = test.allItems();
   
    else if(candidate_item_mode.equals(CandidateItems.OVERLAP))
      candidate_items = new ArrayList<Integer>(Utils.intersect(test.allItems(), training.allItems()));
       
    else if(candidate_item_mode.equals(CandidateItems.UNION))
      candidate_items = new ArrayList<Integer>(Utils.union(test.allItems(), training.allItems()));
 
    candidate_item_mode = CandidateItems.EXPLICIT;
    
    // For better handling, move test data points into arrays.
    int[] users = new int[test.size()];
    int[] items = new int[test.size()];
    int pos = 0;
    for (int user_id : test.userMatrix().nonEmptyColumnIDs()) {
      for (int item_id : test.userMatrix().get(user_id)) {
        users[pos] = user_id;
        items[pos] = item_id;
        pos++;
      }
    }

    // Random order of the test data points.
    List<Integer> random_index = new ArrayList<Integer>(test.size());
    for (int index = 0; index < random_index.size(); index++) random_index.add(index, index);
    Collections.shuffle(random_index);

    HashMap<Integer, HashMap<String, Double>> results_by_user = new HashMap<Integer, HashMap<String, Double>>();

    for (int index : random_index) {
      if (test_users.contains(users[index]) && candidate_items.contains(items[index])) {
        // Evaluate user.
        PosOnlyFeedback<SparseBooleanMatrix> current_test = null;
        try {
          current_test = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
        } catch (Exception e) {
          e.printStackTrace();
        }
        current_test.add(users[index], items[index]);
        HashMap<String, Double> current_result = evaluate(recommender, current_test, training, current_test.allUsers(), candidate_items,candidate_item_mode);

        if (current_result.get("num_users") == 1) {
          HashMap<String, Double> result = results_by_user.get(users[index]);
          if (results_by_user.containsKey(users[index])) {
            for (String measure : Items.getMeasures()) {
              result.put(measure, result.get(measure) + current_result.get(measure));
            }
            result.put("num_items", result.get("num_items") + 1); 
          } else {
            results_by_user.put(users[index], current_result);
            current_result.put("num_items", 1D); 
            current_result.remove("num_users"); 
          }
        }
      }

      // Update recommender
      incremental_recommender.addFeedback(users[index], items[index]);
    }

    HashMap<String, Double> results = new HashMap<String, Double>();
    for (String measure : Items.getMeasures())
      results.put(measure, 0D);

    for (int u : results_by_user.keySet()) {
      HashMap<String, Double> userResult = results_by_user.get(u);
      for (String measure : Items.getMeasures()) {
        results.put(measure, userResult.get(measure) / userResult.get("num_items"));
      }
    }

    for (String measure : Items.getMeasures()) {
      results.put(measure, results.get(measure) / results_by_user.size());
    }

    results.put("num_users", new Double(results_by_user.size()));
    results.put("num_items", new Double(candidate_items.size()));
    results.put("num_lists", new Double(test.size())); // FIXME this is not exact

    return results;
  }

}
