// Copyright (C) 2010 Zeno Gantner
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

package org.mymedialite.diversification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.WeightedItem;

/**
 * Sequential Diversification.
 * 
 * Literature:
 *   Cai-Nicolas Ziegler, Sean McNee, Joseph A. Konstan, Georg Lausen:
 *   Improving Recommendation Lists Through Topic Diversification.
 *   WWW 2005
 *   
 * @version 2.03
 */
public class SequentialDiversification {

  public CorrelationMatrix itemCorrelations;

  /**
   * Constructor.
   * @param item_correlation the similarity measure to use for diversification
   */
  public SequentialDiversification(CorrelationMatrix itemCorrelation) {
    itemCorrelations = itemCorrelation;
  }

  /**
   * Diversify an item list.
   * @param item_list a list of items
   * @param diversification_parameter the diversification parameter (higher means more diverse)
   * @return a list re-ordered to ensure maximum diversity at the top of the list
   */
  public List<Integer> diversifySequential(List<Integer> item_list, double diversification_parameter) {
    if(item_list.size() == 0) throw new IllegalArgumentException();

    Map<Integer, Integer> item_rank_by_rating = new HashMap<Integer, Integer>();
    for (int i = 0; i < item_list.size(); i++)
      item_rank_by_rating.put(item_list.get(i), i);

    List<Integer> diversified_item_list = new ArrayList<Integer>();
    int top_item = item_list.get(0);
    diversified_item_list.add(top_item);

    Set<Integer> item_set = new HashSet<Integer>(item_list);
    item_set.remove(top_item);
    while (item_set.size() > 0) {
      // Rank remaining items by diversity
      List<WeightedItem> items_by_diversity = new ArrayList<WeightedItem>();
      for (int item_id : item_set) {
        double similarity = similarity(item_id, diversified_item_list, itemCorrelations);
        items_by_diversity.add(new WeightedItem(item_id, similarity));
      }
      Collections.sort(items_by_diversity);

      List<WeightedItem> items_by_merged_rank = new ArrayList<WeightedItem>();
      for (int i = 0; i < items_by_diversity.size(); i++) {
        int item_id = items_by_diversity.get(i).item_id;
        // i is the dissimilarity rank
        // TODO adjust for ties
        double score = item_rank_by_rating.get(item_id) * (1 - diversification_parameter) + i * diversification_parameter;

        items_by_merged_rank.add(new WeightedItem(item_id, score));
      }
      Collections.sort(items_by_merged_rank);

      int next_item_id = items_by_merged_rank.get(0).item_id;
      diversified_item_list.add(next_item_id);
      item_set.remove(next_item_id);
    }
    return diversified_item_list;
  }

  /**
   * Compute similarity between one item and a collection of items.
   * @param item_id the item ID
   * @param items a collection of items
   * @param item_correlation the similarity measure to use
   * @return the similarity between the item and the collection
   */
  public static double similarity(int item_id, Collection<Integer> items, CorrelationMatrix item_correlation) {
    double similarity = 0;
    for (int other_item_id : items)
      similarity += item_correlation.get(item_id, other_item_id);
    return similarity;
  }

  /**
   * Compute the intra-set similarity of an item collection.
   * @param items a collection of items
   * @param item_correlation the similarity measure to use
   * @return the intra-set similarity of the collection
   */
  public static double similarity(Collection<Integer> items, CorrelationMatrix item_correlation) {
    double similarity = 0;
    for (int i = 0; i < items.size(); i++)
      for (int j = i + 1; j < items.size(); j++)
        similarity += item_correlation.get(i, j);

    return similarity;
  }

}
