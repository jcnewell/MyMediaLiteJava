// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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

package org.mymedialite.ratingprediction;

import java.io.IOException;

import org.mymedialite.data.IRatings;

/**
 * Abstract class for rating predictors that keep the rating data in memory for training (and possibly prediction)
 * @version 2.03
 */
public abstract class RatingPredictor implements IRatingPredictor, Cloneable {

  /** Maximum user ID */
  public int maxUserID;

  /** Maximum item ID */
  public int maxItemID;

  /** The maximum rating value */
  public double maxRating;

  /** The minimum rating value */
  public double minRating;

//  // TODO find clearer name for this
//  /** true if users shall be updated when doing online updates */
//  public boolean updateUsers = true;
//
//  /** true if items shall be updated when doing online updates */
//  public boolean updateItems = true;

  /** The rating data */
  protected IRatings ratings;

  @Override
  public double getMaxRating() {
    return maxRating;
  }

  @Override
  public void setMaxRating(double max_rating) {
    this.maxRating = max_rating;
  }

  @Override
  public double getMinRating() {
    return minRating;
  }

  @Override
  public void setMinRating(double min_rating) {
    this.minRating = min_rating;
  }

  public IRatings getRatings() { 
    return this.ratings;
  }

  public void setRatings(IRatings ratings) {
    this.ratings = ratings;
    maxUserID = Math.max(ratings.maxUserID(), maxUserID);
    maxItemID = Math.max(ratings.maxItemID(), maxItemID);
    minRating = ratings.minRating();
    maxRating = ratings.maxRating();
  }
  
  public RatingPredictor clone() throws CloneNotSupportedException {
    return (RatingPredictor) super.clone();
  }
  
  /// <inheritdoc/>
  public abstract double predict(int user_id, int item_id);

//  /** 
//   * Initializes the recommender model.
//   * This method is called by the train() method.
//   * When overriding, please call super.initModel() to get the functions performed in the base class.
//   */
//  protected void initModel() {
//    maxUserID = ratings.maxUserID();
//    maxItemID = ratings.maxItemID();
//  }

  /// <inheritdoc/>
  public abstract void train();

  /// <inheritdoc/>
  public abstract void saveModel(String filename) throws IOException ;

  /// <inheritdoc/>
  public abstract void loadModel(String filename) throws IOException ;    

  /// <inheritdoc/>
  public boolean canPredict(int user_id, int item_id) {
    return (user_id <= maxUserID && user_id >= 0 && item_id <= maxItemID && item_id >= 0);
  }
  
  public String toString() {
    return this.getClass().getName();

  }


}