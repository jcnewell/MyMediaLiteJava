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

package org.mymedialite;

import java.io.IOException;

/** Generic interface for simple recommenders. */ 
public interface IRecommender {
  
  /**
   * Predict the rating or score for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted score/rating for the given user-item combination
   */
  double predict(int user_id, int item_id);

  /**
   * Check whether a useful prediction can be made for a given user-item combination.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return true if a useful prediction can be made, false otherwise
   */
  boolean canPredict(int user_id, int item_id);

  /** Learn the model parameters of the recommender from the training data */
  void train();

  /**
   * Save the model parameters to a file
   * @param file the file to write to
   */
  void saveModel(String file) throws IOException;

  /**
   * Get the model parameters from a file
   * @param file the file to read from
   */
  void loadModel(String file) throws IOException;

  /** 
   * Return a string representation of the recommender
   * @return the class name and all hyperparameters, separated by space characters.
   */ 
  String toString();
}