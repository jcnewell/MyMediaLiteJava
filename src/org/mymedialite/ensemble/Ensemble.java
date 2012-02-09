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

package org.mymedialite.ensemble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mymedialite.IRecommender;
import org.mymedialite.ratingprediction.RatingPredictor;

/**
 * Abstract class for combining several prediction methods.
 * @version 2.03
 */
public abstract class Ensemble implements IRecommender {

  /**
   * List of recommenders.
   */
  public List<IRecommender> recommenders = new ArrayList<IRecommender>();

  private double max_rating_value = 5;
  private double min_rating_value = 1;

  /**
   * Create a shallow copy of the object.
   */
  public Object clone() {
    return this.clone();
  }

  /**
   * The max rating value.
   * @return The max rating value
   */
  public double getMaxRatingValue() {
    return max_rating_value;
  }

  public void setMaxRatingValue(double value) {
    this.max_rating_value = value;
    for (IRecommender recommender : recommenders)
      if (recommender instanceof RatingPredictor)
        ((RatingPredictor)recommender).setMaxRating(value);
  }


  /**
   * The min rating value.
   * @return The min rating value
   */
  public double getMinRatingValue() {
    return this.min_rating_value;  
  }

  public void setMinRatingValue(double value) {
    this.min_rating_value = value;
    for (IRecommender recommender : recommenders)
      if (recommender instanceof RatingPredictor)
        ((RatingPredictor)recommender).setMinRating(value);
  }

  /**
   * 
   */
  public abstract double predict(int user_id, int item_id);

  /**
   * 
   */
  public boolean canPredict(int user_id, int item_id) {
    for (IRecommender recommender : recommenders)
      if (!recommender.canPredict(user_id, item_id))
        return false;
    
    return true;
  }

  /**
   * 
   */
  public abstract void saveModel(String filename) throws IOException;

  /**
   * 
   */
  public abstract void loadModel(String filename) throws IOException;

  /**
   * 
   */
  public void train() {
    for (IRecommender recommender : recommenders)
      recommender.train();
  }
  
}
