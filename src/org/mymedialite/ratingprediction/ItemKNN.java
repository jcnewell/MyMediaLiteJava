// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.ratingprediction;

import java.io.IOException;
import java.util.List;

import org.mymedialite.IItemSimilarityProvider;
import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.IRatings;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.util.Memoizer;

/**
 * Weighted item-based kNN.
 * @version 2.03
 */
public abstract class ItemKNN extends KNN implements IItemSimilarityProvider {

  /**
   * Matrix indicating which item was rated by which user.
   */
  protected SparseBooleanMatrix data_item;
  
  /**
   * Source of positively correlated entities.
   */
  protected Memoizer<Integer, List<Integer>> memoizer;
  
  /**
   * 
   */
  public void setRatings(IRatings ratings) {
    super.ratings = ratings;

    data_item = new SparseBooleanMatrix();
    for (int index = 0; index < ratings.size(); index++)
      data_item.set(ratings.items().get(index), ratings.users().get(index), true);
  }
    
  /**
   * Predict the rating of a given user for a given item.
   * 
   * If the user or the item are not known to the recommender, a suitable average is returned.
   * To avoid this behavior for unknown entities, use canPredict() to check before.
   * 
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted rating
   */
  public double predict(int user_id, int item_id) {
    if ((user_id > maxUserID) || (item_id > correlation.numberOfRows() - 1))
      return baseline_predictor.predict(user_id, item_id);

    List<Integer> relevant_items = memoizer.get(item_id);

    double sum = 0;
    double weight_sum = 0;
    int neighbors = k;
    for (int item_id2 : relevant_items)
      if (data_item.get(item_id2, user_id)) {
        double rating = ratings.get(user_id, item_id2, ratings.byItem().get(item_id2));
        double weight = correlation.get(item_id, item_id2);
        weight_sum += weight;
        sum += weight * (rating - baseline_predictor.predict(user_id, item_id2));

        if (--neighbors == 0)
          break;
      }

    double result = baseline_predictor.predict(user_id, item_id);
    if (weight_sum != 0)
      result += sum / weight_sum;

    if (result > maxRating)
      result = maxRating;
    if (result < minRating)
      result = minRating;
    return result;
  }

  /**
   * Retrain model for a given item.
   * @param item_id the item ID
   */
  abstract protected void retrainItem(int item_id);

  /**
   */
  public void addRating(int user_id, int item_id, double rating) {
    baseline_predictor.addRating(user_id, item_id, rating);
    data_item.set(item_id, user_id, true);
    retrainItem(item_id);
  }

  /**
   */
  public void updateRating(int user_id, int item_id, double rating) {
    baseline_predictor.updateRating(user_id, item_id, rating);
    retrainItem(item_id);
  }

  /**
   */
  public void removeRating(int user_id, int item_id) {
    baseline_predictor.removeRating(user_id, item_id);
    data_item.set(item_id, user_id, false);
    retrainItem(user_id);
  }

  /**
   */
  public void addItem(int item_id) {
    correlation.addEntity(item_id);
  }

  /**
   */
  public float getItemSimilarity(int item_id1, int item_id2) {
    return correlation.get(item_id1, item_id2);
  }

  /**
   */
  public int[] getMostSimilarItems(int item_id, int n) {
    // TODO default n = 10
    return correlation.getNearestNeighbors(item_id, n);
  }
  
}
