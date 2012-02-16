// Copyright (C) 2010 Zeno Gantner, Andreas Hoffmann
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

import java.util.Arrays;
import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.VectorExtensions;
import org.mymedialite.eval.Ratings;

/**
 * Baseline method for rating prediction
 *
 * Uses the average rating value, plus a regularized user and item bias for prediction.
 *
 * The method is described in section 2.1 of
 * Yehuda Koren: Factor in the Neighbors: Scalable and Accurate Collaborative Filtering,
 * Transactions on Knowledge Discovery from Data (TKDD), 2009.
 *
 * One difference is that we support several iterations of alternating optimization, instead of just one.
 *
 * This recommender supports incremental updates.
 *
 * @author Zeno Gantner, Andreas Hoffmann
 * @version 2.03
 */
public class UserItemBaseline extends IncrementalRatingPredictor implements IIterativeModel {

  /**
   * Regularization parameter for the user biases
   */
  public double regU = 25;

  /**
   * Regularization parameter for the item biases
   */	
  public double regI = 10;

  /**
   * The number of iterations
   */	
  public int numIter = 10;

  private double globalAverage;
  private double userBiases[];
  private double itemBiases[];

  /**
   * @return The number of iterations
   */
  public int getNumIter() {
    return numIter;
  }

  /**
   * @param numIter The number of iterations
   */
  public void setNumIter(int numIter) {
    this.numIter = numIter;
  }

  protected void retrainUser(int userID) {
    if (getUpdateUsers()) {
      for (int index : ratings.byUser().get(userID))
        userBiases[userID] += ratings.get(index) - globalAverage - itemBiases[ratings.items().get(index)];
      if (ratings.byUser().get(userID).size() != 0)
        userBiases[userID] = userBiases[userID] / (regU + ratings.byUser().get(userID).size());
    }
  }

  protected void retrainItem(int itemID) {
    if (getUpdateItems()) {
      for (int index : ratings.byItem().get(itemID))
        itemBiases[itemID] += ratings.get(index) - globalAverage;
      if (ratings.byItem().get(itemID).size() != 0)
        itemBiases[itemID] = itemBiases[itemID] / (regI + ratings.byItem().get(itemID).size());
    }
  }

  @Override
  public double predict(int userID, int itemID) {
    double user_bias = (userID <= maxUserID && userID >= 0) ? userBiases[userID] : 0;
    double item_bias = (itemID <= maxItemID && itemID >= 0) ? itemBiases[itemID] : 0;
    double result = globalAverage + user_bias + item_bias;

    if (result > maxRating)
      result = maxRating;
    if (result < minRating)
      result = minRating;

    return result;
  }

  public void train() {
    userBiases = new double[maxUserID + 1];
    itemBiases = new double[maxItemID + 1];

    globalAverage = ratings.average();

    for (int i = 0; i < numIter; i++)
      iterate();
  }	

  public void iterate() {
    optimizeItemBiases();
    optimizeUserBiases();
  }

  void optimizeUserBiases() {
    int[] userRatingsCount = new int[maxUserID + 1];

    for (int index = 0; index < ratings.size(); index++) {
      userBiases[ratings.users().get(index)] += ratings.get(index) - globalAverage - itemBiases[ratings.items().get(index)];
      userRatingsCount[ratings.users().get(index)]++;
    }
    for (int u = 0; u < userBiases.length; u++)
      if (userRatingsCount[u] != 0)
        userBiases[u] = userBiases[u] / (regU + userRatingsCount[u]);
  }

  void optimizeItemBiases() {
    int[] item_ratings_count = new int[maxItemID + 1];

    for (int index = 0; index < ratings.size(); index++) {
      itemBiases[ratings.items().get(index)] += ratings.get(index) - globalAverage - userBiases[ratings.users().get(index)];
      item_ratings_count[ratings.items().get(index)]++;
    }

    for (int i = 0; i < itemBiases.length; i++)
      if (item_ratings_count[i] != 0)
        itemBiases[i] = itemBiases[i] / (regI + item_ratings_count[i]);
  }

  @Override
  public void saveModel(String filename) {
    // TODO Auto-generated method stub
  }

  @Override
  public void loadModel(String filename) {
    // TODO Auto-generated method stub
  }

  @Override
  public void addRating(int userID, int itemID, double rating) {

    super.addRating(userID, itemID, rating);
    this.retrainItem(itemID);
    this.retrainUser(userID);
  }

  @Override
  public void updateRating(int userID, int itemID, double rating) {
    super.updateRating(userID, itemID, rating);
    this.retrainItem(itemID);
    this.retrainUser(userID);
  }

  @Override
  public void removeRating(int userID, int itemID) {
    super.removeRating(userID, itemID);
    this.retrainItem(itemID);
    this.retrainUser(userID);
  }

  @Override
  public void addUser(int userID) {
    super.addUser(userID);
    if (userID >= this.userBiases.length) {
      double[] userBiases = Arrays.copyOf(this.userBiases, this.userBiases.length);
      this.userBiases = userBiases;
    }
  }

  @Override
  public void addItem(int itemID) {
    super.addItem(itemID);
    if (itemID >= this.itemBiases.length) {
      double[] itemBiases = Arrays.copyOf(this.itemBiases, this.itemBiases.length);
      this.itemBiases = itemBiases;

    }
  }

  @Override
  public double computeLoss() {
    return
        Ratings.evaluate(this, ratings).get("RMSE")
        + regU * Math.pow(VectorExtensions.euclideanNorm(userBiases), 2)
        + regI * Math.pow(VectorExtensions.euclideanNorm(itemBiases), 2);
  }

  @Override
  public String toString() {
    return "user-item-baseline regU=" + regU + " regI=" + regI+ " numIter=" + numIter;
  }
}