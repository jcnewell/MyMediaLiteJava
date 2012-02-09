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
import java.util.HashMap;
import java.util.List;
import org.mymedialite.IRecommender;
import org.mymedialite.data.WeightedItem;
import org.mymedialite.itemrec.ItemRecommender;

/**
 * Group recommender that averages user scores weighted by the rating frequency of the individual users.
 * @version 2.03
 */
public class WeightedAverage extends GroupRecommender {

  public WeightedAverage(IRecommender recommender) {
    super(recommender);
  }

  /**
   *
   */
  public List<Integer> rankItems(Collection<Integer> users, Collection<Integer> items) {
    ItemRecommender item_recommender = (ItemRecommender) recommender;
    HashMap<Integer, Integer> user_weights = new HashMap<Integer, Integer>();
    for (int user_id : users) {
      user_weights.put(user_id, item_recommender.getFeedback().userMatrix().getEntriesByRow(user_id).size());
    }  
    
    List<WeightedItem> average_scores = new ArrayList<WeightedItem>(items.size());

    for (int item_id : items) {
      WeightedItem weightedItem = new WeightedItem(item_id, 0.0);
      for (int user_id : users) {
        // TODO consider taking CanPredict into account
        weightedItem.weight += user_weights.get(user_id) * recommender.predict(user_id, item_id);
      }
      weightedItem.weight = weightedItem.weight / users.size();
      average_scores.add(item_id, weightedItem);
    }
    
    Collections.sort(average_scores, Collections.reverseOrder());
    List<Integer> ranked_items = new ArrayList<Integer>(average_scores.size());
    for (int i=0; i<ranked_items.size(); i++) {
      ranked_items.add(i, average_scores.get(i).item_id);
    }
    
    return ranked_items;
  }
}

