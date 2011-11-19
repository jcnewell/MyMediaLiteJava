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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.mymedialite.itemrec.IIncrementalItemRecommender;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;

public class ItemsOnline {
	  /**
	   * Online evaluation for rankings of items.
	   * @param recommender item recommender
	   * @param test test cases
	   * @param train training data (must be connected to the recommender's training data)
	   * @param test_users a collection of integers with all relevant users
	   * @param candidate_items a collection of integers with all relevant items
	   * @return a dictionary containing the evaluation results (averaged by user)
	   */
	  public static HashMap<String, Double> evaluate(
	      IIncrementalItemRecommender recommender,
	      IPosOnlyFeedback test,
	      IPosOnlyFeedback train,
	      Collection<Integer> test_users,
	      Collection<Integer> candidate_items) {

	    // For better handling, move test data points into arrays.
	    int[] users = new int[test.size()];
	    int[] items = new int[test.size()];
	    int pos = 0;
	    for (int user_id : test.getUserMatrix().getNonEmptyColumnIDs()) {
	      for (int item_id : test.getUserMatrix().getRow(user_id)) {
	        users[pos] = user_id;
	        items[pos] = item_id;
	        pos++;
	      }
	    }

	    // Random order of the test data points.
	    int[] random_index = new int[test.size()];
	    for (int index = 0; index < random_index.length; index++) random_index[index] = index;
	    Collections.shuffle(Arrays.asList(random_index));
	    
	    HashMap<Integer, HashMap<String, Double>> results_by_user = new HashMap<Integer, HashMap<String, Double>>();

	    for (int index : random_index) {
	      if (test_users.contains(users[index]) && candidate_items.contains(items[index])) {
	        // Evaluate user.
	        PosOnlyFeedback<SparseBooleanMatrix> current_test = new PosOnlyFeedback<SparseBooleanMatrix>(new SparseBooleanMatrix());
	        current_test.add(users[index], items[index]);
	        HashMap<String, Double> current_result = evaluate(recommender, current_test, train, current_test.getAllUsers(), candidate_items);

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

	      // update recommender
	      recommender.addFeedback(users[index], items[index]);
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
