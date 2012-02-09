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

package org.mymedialite.eval.measures;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Area under the ROC curve (AUC) of a list of ranked items.
 * See http://recsyswiki.com/wiki/Area_Under_the_ROC_Curve
 * @version 2.03
 */
public class AUC {
  
  // Prevent instantiation.
  private AUC() {}
  
  /**
   * Compute the area under the ROC curve (AUC) of a list of ranked items.
   * 
   * See http://recsyswiki.com/wiki/Area_Under_the_ROC_Curve
   * 
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @return the AUC for the given data
   */
  public static double compute(List<Integer> ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {

    if (ignore_items == null)
      ignore_items = new HashSet<Integer>();
    
    Set<Integer> correctItemsIntersection = new HashSet<Integer>(ignore_items);
    correctItemsIntersection.retainAll(correct_items);
    int num_correct_items = correct_items.size() - correctItemsIntersection.size();
    
    Set<Integer> rankedItemsIntersection = new HashSet<Integer>(ignore_items);
    rankedItemsIntersection.retainAll(ranked_items);
    int num_eval_items = ranked_items.size() - rankedItemsIntersection.size();
    
    int num_eval_pairs    = (num_eval_items - num_correct_items) * num_correct_items;
    if (num_eval_pairs < 0)
      throw new IllegalArgumentException("correct_items cannot be larger than ranked_items");

    if (num_eval_pairs == 0)
      return 0.5;

    int num_correct_pairs = 0;
    int hit_count         = 0;
    for (int item_id : ranked_items) {
      if (ignore_items.contains(item_id))
        continue;

      if (!correct_items.contains(item_id))
        num_correct_pairs += hit_count;
      else
        hit_count++;
    }

    return (double) num_correct_pairs / num_eval_pairs;
  }
}
