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

/**
 * Uses a constant rating value for prediction.
 * 
 * This recommender supports incremental updates.
 * Updates are just ignored, because the prediction is always the same.
 * @version 2.03
 */
public class Constant extends IncrementalRatingPredictor {
  
  /**
   * the constant rating.
   */
  public double constantRating;

  /**
   * Default constructor.
   */
  public Constant() {
    constantRating = 1.0;
  }

  /**
   * 
   */
  @Override
  public void train() { }

  /**
   * 
   */
  @Override
  public boolean canPredict(int user_id, int item_id) {
    return true;
  }

  /**
   * 
   */
  @Override
  public double predict(int user_id, int item_id) {
    return constantRating;
  }

  /**
   * 
   */
  @Override
  public void saveModel(String filename) { /* do nothing */ }

  /**
   * 
   */
  @Override
  public void loadModel(String filename) { /* do nothing */ }

  /**
   * 
   */
  @Override
  public String toString() {
    return "Constant constant_rating=" + constantRating;
  }
  
}
