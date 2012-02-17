// Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.mymedialite.io.Model;

/**
 * Uses the average rating value over all ratings for prediction.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class GlobalAverage extends IncrementalRatingPredictor {
  
  private static final String VERSION = "2.03";
  private double global_average = 0;

  /**
   */
  @Override
  public void train() {
    global_average = ratings.average();
  }

  /**
   */
  @Override
  public boolean canPredict(int user_id, int item_id) {
    return true;
  }

  /**
   */
  @Override
  public double predict(int user_id, int item_id) {
    return global_average;
  }

  /**
   */
  @Override
  public void addRating(int user_id, int item_id, double rating) {
    super.addRating(user_id, item_id, rating);
    train();
  }

  /**
   */
  @Override
  public void updateRating(int user_id, int item_id, double rating) {
    super.updateRating(user_id, item_id, rating);
    train();
  }

  /**
   */
  @Override
  public void removeRating(int user_id, int item_id) {
    super.removeRating(user_id, item_id);
    train();
  }

  /**
   * @throws IOException 
   */
  @Override
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(global_average);
    writer.flush();
    writer.close();
  }

  /**
   * @throws IOException 
   */
  @Override
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    this.global_average = Double.parseDouble(reader.readLine());
    reader.close();
  }

}
