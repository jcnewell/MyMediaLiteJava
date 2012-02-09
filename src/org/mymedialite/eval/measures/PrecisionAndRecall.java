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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Precision and recall at different positions in the list.
 * Precision and recall are classical evaluation measures from information retrieval.
 *
 * This class contains methods for computing precision and recall up to different positions
 * in the recommendation list, and the average precision (AP).
 *
 * The mean of the AP over different users is called mean average precision (MAP)
 * @version 2.03
 */
public class PrecisionAndRecall {

  // Prevent instantiation.
  private PrecisionAndRecall() {}

  /**
   * Compute the average precision (AP) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @return the AP for the given list
   */
  public static double AP(List<Integer> ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {

    if (ignore_items == null)
      ignore_items = new ArrayList<Integer>();

    int hit_count       = 0;
    double avg_prec_sum = 0;
    int left_out        = 0;

    for (int i = 0; i < ranked_items.size(); i++) {
      int item_id = ranked_items.get(i);
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }

      if (!correct_items.contains(item_id))
        continue;

      hit_count++;

      avg_prec_sum += (double) hit_count / (i + 1 - left_out);
    }

    if (hit_count != 0)
      return avg_prec_sum / hit_count;
    else
      return 0;
  }

  /**
   * Compute the precision@N of a list of ranked items at several N.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param ns the cutoff positions in the list
   * @return the precision@N for the given data at the different positions N
   */
  public static HashMap<Integer, Double> precisionAt(
      List<Integer> ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int[] ns) {

    HashMap<Integer, Double> precision_at_n = new HashMap<Integer, Double>();
    for (int n : ns)
      precision_at_n.put(n, precisionAt(ranked_items, correct_items, ignore_items, n));

    return precision_at_n;
  }

  /**
   * Compute the precision@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param n the cutoff position in the list
   * @return the precision@N for the given data
   */
  public static double precisionAt(List<Integer> ranked_items, Collection<Integer> correct_items, int n) {
    return precisionAt(ranked_items, correct_items, new HashSet<Integer>(), n);
  }

  /**
   * Compute the precision@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param n the cutoff position in the list
   * @return the precision@N for the given data
   */
  public static double precisionAt(
      List<Integer> ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int n) {

    return (double) hitsAt(ranked_items, correct_items, ignore_items, n) / n;
  }

  /**
   * Compute the recall@N of a list of ranked items at several N.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param ns the cutoff positions in the list
   * @return the recall@N for the given data at the different positions N
   */
  public static HashMap<Integer, Double> recallAt(
      List<Integer> ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int[] ns)
      {
    
    HashMap<Integer, Double> recall_at_n = new HashMap<Integer, Double>();
    for (int n : ns)
      recall_at_n.put(n, recallAt(ranked_items, correct_items, ignore_items, n));
  
    return recall_at_n;
  }

  /**
   * Compute the recall@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param n the cutoff position in the list
   * @return the recall@N for the given data
   */
  public static double recallAt(List<Integer> ranked_items, Collection<Integer> correct_items, int n) {
    return recallAt(ranked_items, correct_items, new HashSet<Integer>(), n);
  }

  /**
   * Compute the recall@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param n the cutoff position in the list
   * @return the recall@N for the given data
   */
  public static double recallAt(
      List<Integer> ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int n) {
    
    return (double) hitsAt(ranked_items, correct_items, ignore_items, n) / correct_items.size();
  }

  /**
   * Compute the number of hits until position N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param n the cutoff position in the list
   * @return the hits@N for the given data
   */
  public static int hitsAt(
      List<Integer> ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int n) {
    
    if (n < 1)
      throw new IllegalArgumentException("n must be at least 1.");

    int hit_count = 0;
    int left_out  = 0;

    for (int i = 0; i < ranked_items.size(); i++) {
      int item_id = ranked_items.get(i);
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }

      if (!correct_items.contains(item_id))
        continue;

      if (i < n + left_out)
        hit_count++;
      else
        break;
    }

    return hit_count;
  }
}
