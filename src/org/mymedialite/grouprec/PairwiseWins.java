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

package org.mymedialite.grouprec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mymedialite.IRecommender;
import org.mymedialite.data.WeightedItem;

/**
 * A simple Condorcet-style voting mechanism.
 * runtime complexity O(|U| |I|^2)
 * @version 2.03
 */
public class PairwiseWins extends GroupRecommender {

  /**
   * 
   */
  public PairwiseWins(IRecommender recommender) {
    super(recommender);
  }

  /**
   * 
   */
  public List<Integer> rankItems(Collection<Integer> users, Collection<Integer> items) {
    Double[][] scores_by_user = new Double[users.size()][items.size()];

    Integer[] users_array = users.toArray(new Integer[users.size()]);
    Integer[] items_array = items.toArray(new Integer[items.size()]);

    for (int u = 0; u < users.size(); u++) {
      for (int i = 0; i < items.size(); i++) {
        int user_id = users_array[u];
        int item_id = items_array[i];
        scores_by_user[u][i] = recommender.predict(user_id, item_id);
      }
    }
    
    List<WeightedItem> wins_by_item = new ArrayList<WeightedItem>(items.size());
    
    for (int u = 0; u < users.size(); u++) {
      for (int i = 0; i < items.size(); i++) {
        WeightedItem weightedItem = new WeightedItem(i, Double.MIN_VALUE);
        for (int j = 0; j < items.size(); j++) {
          if (scores_by_user[u][i] > scores_by_user[u][j]) {
            weightedItem.weight += 1.0;
          }
        }
        wins_by_item.add(i, weightedItem);
      }
    }

    Collections.sort(wins_by_item, Collections.reverseOrder());
    List<Integer> ranked_items = new ArrayList<Integer>(wins_by_item.size());
    for (int i=0; i<wins_by_item.size(); i++) {
      ranked_items.add(i, wins_by_item.get(i).item_id);
    }

    return ranked_items;
  }
}
