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

import java.util.List;

import org.mymedialite.IUserSimilarityProvider;
import org.mymedialite.data.IRatings;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Weighted user-based kNN.
 * @version 2.03
 */
public abstract class UserKNN extends KNN implements IUserSimilarityProvider {

  /**
   * boolean matrix indicating which user rated which item.
   */
  protected SparseBooleanMatrix data_user;
  protected IRatings ratings;

  /**
   */
  public void setRatings(IRatings ratings) {
    super.ratings = ratings;
    data_user = new SparseBooleanMatrix();
    for (int index = 0; index < ratings.size(); index++)
      data_user.set(ratings.users().get(index), ratings.items().get(index), true);
  }

  /**
   * Predict the rating of a given user for a given item.
   * 
   * If the user or the item are not known to the recommender, a suitable average rating is returned.
   * To avoid this behavior for unknown entities, use CanPredict() to check before.
   * 
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted rating
   */
  public double predict(int user_id, int item_id) {
    if ((user_id > correlation.numberOfRows() - 1) || (item_id > maxItemID))
      return baseline_predictor.predict(user_id, item_id);

    List<Integer> relevant_users = correlation.getPositivelyCorrelatedEntities(user_id);

    double sum = 0;
    double weight_sum = 0;
    int neighbors = k;
    for (int user_id2 : relevant_users) {
      if (data_user.get(user_id2, item_id)) {
        double rating = ratings.get(user_id2, item_id, ratings.byUser().get(user_id2));

        double weight = correlation.get(user_id, user_id2);
        weight_sum += weight;
        sum += weight * (rating - baseline_predictor.predict(user_id2, item_id));

        if (--neighbors == 0)
          break;
      }
    }

    double result = baseline_predictor.predict(user_id, item_id);
    if (weight_sum != 0) {
      double modification = sum / weight_sum;
      result += modification;
    }

    if (result > maxRating)
      result = maxRating;
    if (result < minRating)
      result = minRating;
    return result;
  }

  /**
   * Retrain model for a given user.
   * @param user_id the user ID
   */
  abstract protected void retrainUser(int user_id);

  /**
   */
  public void addRating(int user_id, int item_id, double rating) {
    baseline_predictor.addRating(user_id, item_id, rating);
    data_user.set(user_id, item_id, true);
    retrainUser(user_id);
  }

  /**
   */
  public void updateRating(int user_id, int item_id, double rating) {
    baseline_predictor.updateRating(user_id, item_id, rating);
    retrainUser(user_id);
  }

  /**
   */
  public void removeRating(int user_id, int item_id) {
    baseline_predictor.removeRating(user_id, item_id);
    data_user.set(user_id, item_id, false);
    retrainUser(user_id);
  }

  /**
   */
  public void addUser(int user_id) {
    correlation.addEntity(user_id);
  }

  /**
   */
  public float getUserSimilarity(int user_id1, int user_id2) {
    return correlation.get(user_id1, user_id2);
  }

  /**
   */
  public int[] getMostSimilarUsers(int user_id, int n) {
    return correlation.getNearestNeighbors(user_id, n);
  }
  
}
