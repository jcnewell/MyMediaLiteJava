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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.ISplit;
import org.mymedialite.IRecommender;
import org.mymedialite.itemrec.ItemRecommender;
import org.mymedialite.itemrec.Prediction;

/**
 * Evaluation class for item recommendation. 
 */
public class Items {

	// this is a static class, but Java does not allow us to declare that ;-)
	private Items() {}

	/**
	 * Get the evaluation measures for item prediction offered by the class.
	 */
	static public Collection<String> getMeasures() { 
		String[] array = { "AUC", "prec@5", "prec@10", "recall@5", "recall@10", "NDCG", "MAP" };
		return Arrays.asList(array);
  }

  /**
   * Format item prediction results.
   * @param result the result dictionary
   * @return a string containing the results
   */
  public static String formatResults(HashMap<String, Double> result) {
    String string = "AUC "          + result.get("AUC") +
    "prec@5 "       + result.get("prec@5") +
    "prec@10 "      + result.get("prec@10") +
    "recall@5 "     + result.get("recall@5") +
    "recall@10 "    + result.get("recall@10") +
    "MAP "          + result.get("MAP") +
    "NDCG "         + result.get("NDCG") +
    "num_users "    + result.get("num_users") +
    "num_items "    + result.get("num_items") +
    "num_lists "    + result.get("num_lists");
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
    System.out.println("recall@5   " + result.get("recall@5"));
    System.out.println("recall@10  " + result.get("recall@10"));
    System.out.println("MAP        " + result.get("MAP"));
    System.out.println("NDCG       " + result.get("NDCG"));
    System.out.println("num_users  "    + result.get("num_users"));
    System.out.println("num_items  "    + result.get("num_items"));
    System.out.println("num_lists  "    + result.get("num_lists")); 
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
  public static HashMap<String, Double> evaluate(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback train,
      Collection<Integer> test_users,
      Collection<Integer> candidate_items) {
    return evaluate(recommender, test, train, test_users, candidate_items, true);         
  }

  /**
   * Evaluation for rankings of items.
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
   * @param test_users a collection of integers with all relevant users
   * @param candidate_items a collection of integers with all relevant items
   * @param ignore_overlap if true, ignore items that appear for a user in the training set when evaluating for that user
   * @return a dictionary containing the evaluation results
   */
  public static HashMap<String, Double> evaluate(
      IRecommender recommender,
      IPosOnlyFeedback test,
      IPosOnlyFeedback train,
      Collection<Integer> test_users,
      Collection<Integer> candidate_items,
      boolean ignore_overlap) {

    // Compute evaluation measures
    double auc_sum       = 0;
    double map_sum       = 0;
    double prec_5_sum    = 0;
    double prec_10_sum   = 0;
    double recall_5_sum  = 0;
    double recall_10_sum = 0;
    double ndcg_sum      = 0;
    int num_users        = 0;

    for (Integer user_id : test_users) {
      // Items viewed by the user in the test set that were also present in the training set.
      HashSet<Integer> correct_items = new HashSet<Integer>(intersect(test.getUserMatrix().getRow(user_id), candidate_items));

      // The number of items that are really relevant for this user.
      HashSet<Integer> candidate_items_in_train = new HashSet<Integer> (intersect(train.getUserMatrix().getRow(user_id), candidate_items));

      int num_eval_items = candidate_items.size() - (ignore_overlap ? candidate_items_in_train.size() : 0);

      // Skip all users that have 0 or #relevant_items test items.
      if (correct_items.size() == 0) continue;
      if (num_eval_items - correct_items.size() == 0) continue;

      System.out.println("correct_items:  " + correct_items.size());
      System.out.println("num_eval_items: " + num_eval_items);
      System.out.println("relevant_items: " + candidate_items.size());
      System.out.println("training items: " + train.getUserMatrix().getRow(user_id).size());

      num_users++;
      int[] prediction = Prediction.predictItems(recommender, user_id, candidate_items);
      if (prediction.length != candidate_items.size()) throw new RuntimeException("Not all items have been ranked.");

      Collection<Integer> ignore_items = ignore_overlap ? train.getUserMatrix().getRow(user_id) : new ArrayList<Integer>();

      auc_sum     += AUC(prediction, correct_items, ignore_items);
      map_sum     += MAP(prediction, correct_items, ignore_items);
      ndcg_sum    += NDCG(prediction, correct_items, ignore_items);

      int[] ns = new int[] { 5, 10, 15 };
      double[] prec = precisionAt(prediction, correct_items, ignore_items, ns);
      prec_5_sum  += prec[5];
      prec_10_sum += prec[10];
      double[] recall = recallAt(prediction, correct_items, ignore_items, ns);
      recall_5_sum  += recall[5];
      recall_10_sum += recall[10];

      if (num_users % 1000 == 0)  System.err.println(".");
      if (num_users % 60000 == 0) System.err.println();
    }

    HashMap<String, Double> result = new HashMap<String, Double>();
    result.put("AUC",             auc_sum / num_users);
    result.put("MAP",             map_sum / num_users);
    result.put("NDCG",           ndcg_sum / num_users);
    result.put("prec@5",       prec_5_sum / num_users);
    result.put("prec@10",     prec_10_sum / num_users);
    result.put("recall@5",   recall_5_sum / num_users);
    result.put("recall@10", recall_10_sum / num_users);
    result.put("num_users", new Double(num_users));
    result.put("num_lists", new Double(num_users));
    result.put("num_items", new Double(candidate_items.size()));

    return result;
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender an item recommender
   * @param split a dataset split
   * @param relevant_users a collection of integers with all relevant users
   * @param relevant_items a collection of integers with all relevant items
   * @return a dictionary containing the average results over the different folds of the split
   */
  public static HashMap<String, Double> evaluateOnSplit(ItemRecommender recommender,
      ISplit<IPosOnlyFeedback> split,
      Collection<Integer> relevant_users,
      Collection<Integer> relevant_items) {

    return evaluateOnSplit(recommender, split, relevant_users, relevant_items, false);
  }

  /**
   * Evaluate on the folds of a dataset split.
   * @param recommender an item recommender
   * @param split a dataset split
   * @param test_users a collection of integers with all relevant users
   * @param candidate_items a collection of integers with all relevant items
   * @param show_results set to true to print results to STDERR
   * @return a dictionary containing the average results over the different folds of the split
   */
  public static HashMap<String, Double> evaluateOnSplit(ItemRecommender recommender,
      ISplit<IPosOnlyFeedback> split,
      Collection<Integer> test_users,
      Collection<Integer> candidate_items,
      boolean show_results) {

    HashMap<String, Double> avg_results = new HashMap<String, Double>();

    for (int fold = 0; fold < split.getNumberOfFolds(); fold++) {
      ItemRecommender split_recommender = (ItemRecommender)(recommender.clone()); // to avoid changes in recommender
      split_recommender.setFeedback(split.train().get(fold));
      split_recommender.train();
      HashMap<String, Double> fold_results = evaluate(split_recommender, split.train().get(fold), split.test().get(fold), test_users, candidate_items);

      for (String key : fold_results.keySet()) {
        if (avg_results.containsKey(key)) {
          avg_results.put(key, avg_results.get(key) + fold_results.get(key));
        } else {
          avg_results.put(key, fold_results.get(key));
        }
      }
      if (show_results) System.err.print("fold " + fold + " " + formatResults(avg_results));
    }

    for (String key : avg_results.keySet()) avg_results.put(key, avg_results.get(key) / split.getNumberOfFolds());
    return avg_results;
  }

  /**
   * Compute the area under the ROC curve (AUC) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @return the AUC for the given data
   */
  public static double AUC(int[] ranked_items, Collection<Integer> correct_items) {
    return AUC(ranked_items, correct_items, new HashSet<Integer>());
  }

  /**
   * Compute the area under the ROC curve (AUC) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @return the AUC for the given data
   */
  public static double AUC(int[] ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {
    int num_eval_items = ranked_items.length - intersect(ignore_items, ranked_items).size();

    int num_eval_pairs = (num_eval_items - correct_items.size()) * correct_items.size();

    int num_correct_pairs = 0;
    int hit_count         = 0;

    for (int item_id : ranked_items) {
      if (ignore_items.contains(item_id))  continue;

      if (!correct_items.contains(item_id)) {
        num_correct_pairs += hit_count;
      } else {
        hit_count++;
      }
    }
    return ((double) num_correct_pairs) / num_eval_pairs;
  }

  /**
   * Compute the mean average precision (MAP) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @return the MAP for the given data
   */
  public static double MAP(int[] ranked_items, Collection<Integer> correct_items) {
    return MAP(ranked_items, correct_items, new HashSet<Integer>());
  }

  /**
   * Compute the mean average precision (MAP) of a list of ranked items.
   *  @param ranked_items a list of ranked item IDs, the highest-ranking item first
   *  @param correct_items a collection of positive/correct item IDs
   *  @param ignore_items a collection of item IDs which should be ignored for the evaluation
   *  @return the MAP for the given data
   */
  public static double MAP(int[] ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {
    int hit_count       = 0;
    double avg_prec_sum = 0;
    int left_out        = 0;

    for (int i = 0; i < ranked_items.length; i++) {
      int item_id = ranked_items[i];
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }

      if (!correct_items.contains(item_id))  continue;

      hit_count++;

      avg_prec_sum += (double) hit_count / (i + 1 - left_out);
    }

    if (hit_count != 0) {
      return avg_prec_sum / hit_count;
    } else {
      return 0;
    }
  }

  /**
   * Compute the normalized discounted cumulative gain (NDCG) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @return the NDCG for the given data
   */
  public static double NDCG(int[] ranked_items, Collection<Integer> correct_items) {
    return NDCG(ranked_items, correct_items, new HashSet<Integer>());
  }

  /**
   * Compute the normalized discounted cumulative gain (NDCG) of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @return the NDCG for the given data
   */
  public static double NDCG(int[] ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {
    double dcg   = 0;
    double idcg  = ComputeIDCG(correct_items.size());
    int left_out = 0;

    for (int i = 0; i < ranked_items.length; i++) {
      int item_id = ranked_items[i];
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }

      if (!correct_items.contains(item_id)) continue;

      // compute NDCG part
      int rank = i + 1 - left_out;
      dcg += Math.log(2) / Math.log(rank + 1);
    }
    return dcg / idcg;
  }

  /**
   * Compute the precision@N of a list of ranked items at several N.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param ns the cutoff positions in the list
   * @return the precision@N for the given data at the different positions N
   */
  public static double[] precisionAt(int[] ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int[] ns) {
    
    double[] precision_at_n = new double[ns.length];
    for(int n : ns) precision_at_n[n] = precisionAt(ranked_items, correct_items, ignore_items, n)
    ;
    return precision_at_n;
   }
  
  /**
   * Compute the precision@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param n the cutoff position in the list
   * @return the precision@N for the given data
   */
  public static double precisionAt(int[] ranked_items, Collection<Integer> correct_items, int n) {
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
  public static double precisionAt(int[] ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items, int n) throws IllegalArgumentException {
    if (n < 1) throw new IllegalArgumentException("N must be at least 1.");

    int hit_count = 0;
    int left_out  = 0;

    for (int i = 0; i < ranked_items.length; i++) {
      int item_id = ranked_items[i];
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }

      if (!correct_items.contains(item_id)) continue;

      if (i < n + left_out) {
        hit_count++;
      } else {
        break;
      }
    }
    return (double) hit_count / n;
  }
  
  /**
   * Compute the recall@N of a list of ranked items at several N.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @param ns the cutoff positions in the list
   * @return the recall@N for the given data at the different positions N
   */
  public static double[] recallAt(int[] ranked_items,
      Collection<Integer> correct_items,
      Collection<Integer> ignore_items,
      int[] ns) {
    
    double[] recall_at_n = new double[ns.length];
    for (int n : ns) recall_at_n[n] = recallAt(ranked_items, correct_items, ignore_items, n);
    return recall_at_n;
  }

  /**
   * Compute the recall@N of a list of ranked items.
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param n the cutoff position in the list
   * @return the recall@N for the given data
   */
  public static double recallAt(int[] ranked_items, Collection<Integer> correct_items, int n) {
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
  public static double recallAt(int[] ranked_items, Collection<Integer> correct_items,
      Collection<Integer> ignore_items, int n) {

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
  public static int hitsAt(int[] ranked_items, Collection<Integer> correct_items,
      Collection<Integer> ignore_items, int n) {

    if (n < 1) throw new IllegalArgumentException("n must be at least 1.");

    int hit_count = 0;
    int left_out  = 0;

    for (int i = 0; i < ranked_items.length; i++) {
      int item_id = ranked_items[i];
      if (ignore_items.contains(item_id)) {
        left_out++;
        continue;
      }
      if (!correct_items.contains(item_id)) continue;
      if (i < n + left_out) {
        hit_count++;
      } else {
        break;
      }
    }

    return hit_count;
  }

  /**
   * Computes the ideal DCG given the number of positive items.
   * @param n the number of positive items
   * @return the ideal DCG
   */
  static double ComputeIDCG(int n) {
    double idcg = 0;
    for (int i = 0; i < n; i++)
      idcg += Math.log(2) / Math.log(i + 2);
    return idcg;
  }

  static <T> Collection<T> intersect(Collection<T> a, Collection<T> b) {
    Set<T> intersection = new HashSet<T>(a);
    intersection.retainAll(new HashSet<T>(b));
    return intersection;    
  }

  static Collection<Integer> intersect(Collection<Integer> a, int[] b) {
    Set<Integer> intersection = new HashSet<Integer>();
    for(int i : b) {
      intersection.add(i);
    }
    intersection.retainAll(new HashSet<Integer>(a));
    return intersection;
  }

}