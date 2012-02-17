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
 * Group recommender that takes the maximum user score as the group score.
 * @version 2.03
 */
public class Maximum extends GroupRecommender {

  /**
   * 
   */
  public Maximum(IRecommender recommender) {
    super(recommender);
  }

  /**
   */
  public List<Integer> rankItems(Collection<Integer> users, Collection<Integer> items) {
    List<WeightedItem> maximum_scores = new ArrayList<WeightedItem>(items.size());

    for (int i : items) {
      WeightedItem weightedItem = new WeightedItem(i, Double.MIN_VALUE);
      for (int u : users) {
        // TODO consider taking CanPredict into account
        weightedItem.weight = Math.max(weightedItem.weight, recommender.predict(u, i));
      }
      weightedItem.weight = weightedItem.weight / users.size();
      maximum_scores.add(i, weightedItem);
    }

    Collections.sort(maximum_scores, Collections.reverseOrder());
    List<Integer> ranked_items = new ArrayList<Integer>(maximum_scores.size());
    for (int i=0; i<maximum_scores.size(); i++) {
      ranked_items.add(i, maximum_scores.get(i).item_id);
    }

    return ranked_items;
  }
}

