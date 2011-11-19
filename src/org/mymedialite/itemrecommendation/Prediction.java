// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011 Zeno Gantner
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

package org.mymedialite.itemrecommendation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.mymedialite.IRecommender;
import org.mymedialite.data.WeightedItem;

/** <summary>Class that contains static methods for item prediction</summary>*/
public class Prediction {
				
  /**
   * Predict items for a specific user
   * <param name="recommender">the <see cref="IRecommender"/> object to use for the predictions</param>
   * <param name="user_id">the user ID</param>
   * <param name="max_item_id">the maximum item ID</param>
   * <returns>a list sorted list of item IDs</returns>
   */
  public static int[] predictItems(IRecommender recommender, int user_id, int max_item_id) {
    ArrayList<WeightedItem> result = new ArrayList<WeightedItem>();
    for (int item_id = 0; item_id <= max_item_id; item_id++) {
      result.add( new WeightedItem(item_id, recommender.predict(user_id, item_id)));
    }
    Collections.sort(result, Collections.reverseOrder());

    int[] return_array = new int[max_item_id + 1];
    for (int i=0; i<return_array.length; i++) {
      return_array[i] = result.get(i).item_id;
    }
    return return_array;
  }

  /**
   * Predict items for a given user.
   * @param recommender the recommender to use
   * @param user_id the numerical ID of the user
   * @param relevant_items a collection of numerical IDs of relevant items
   * @return an ordered list of items, the most likely item first
   */
  public static int[] predictItems(IRecommender recommender, int user_id, Collection<Integer> relevant_items) {
    ArrayList<WeightedItem> result = new ArrayList<WeightedItem>();
    for (int item_id : relevant_items) {
      result.add( new WeightedItem(item_id, recommender.predict(user_id, item_id)));
    }
    Collections.sort(result, Collections.reverseOrder());

    int[] return_array = new int[result.size()];
    for (int i=0; i<return_array.length; i++) {
      return_array[i] = result.get(i).item_id;
    }
    return return_array;
  }
  
}