// Copyright (C) 2010 Zeno Gantner, Steffen Rendle, Christoph Freudenthaler
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.datatype.VectorExtensions;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;

/**
 * Simple matrix factorization class, learning is performed by stochastic gradient descent.
 * 
 * Factorizing the observed rating values using a factor matrix for users and one for items.
 *
 * NaN values in the model occur if values become too large or too small to be represented by the type double.
 * If you encounter such problems, there are three ways to fix them:
 * (1) (preferred) Use BiasedMatrixFactorization, which is more stable.
 * (2) Change the range of rating values (1 to 5 works generally well with the default settings).
 * (3) Change the learn_rate (decrease it if your range is larger than 1 to 5).
 *
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class MatrixFactorization extends IncrementalRatingPredictor implements IIterativeModel {

  private static final String VERSION = "2.03";

  /**
   * Matrix containing the latent user factors.
   */
  protected Matrix<Double> userFactors;

  /**
   * Matrix containing the latent item factors.
   */
  protected Matrix<Double> itemFactors;

  /**
   * The bias (global average).
   */
  protected double globalBias;

  /**
   * Mean of the normal distribution used to initialize the factors.
   */
  public double initMean;

  /**
   * Standard deviation of the normal distribution used to initialize the factors.
   */
  public double initStDev;

  /**
   * Number of latent factors.
   */
  public int numFactors;

  /**
   * Learn rate.
   */
  public double learnRate;

  /**
   * Regularization parameter.
   */
  public double regularization;

  /**
   * Number of iterations over the training data.
   */
  public int numIter;

  @Override
  public void setNumIter(int num_iter) {
    this.numIter = num_iter;
  }

  @Override
  public int getNumIter() {
    return numIter;
  }

  /**
   * Default constructor.
   */
  public MatrixFactorization() {
    super();
    // Set default values
    regularization = 0.015;
    learnRate = 0.01;
    numIter = 30;
    initStDev = 0.1;
    numFactors = 10;
  }

  /**
   * Initialize the model data structure.
   */
  protected void initModel() {

    // Init factor matrices
    userFactors = new Matrix<Double>(maxUserID + 1, numFactors);
    itemFactors = new Matrix<Double>(maxItemID + 1, numFactors);
    MatrixExtensions.initNormal(userFactors, initMean, initStDev);
    MatrixExtensions.initNormal(itemFactors, initMean, initStDev);
  }

  /**
   * 
   */
  public void train() {    
    initModel();

    // Learn model parameters
    globalBias = ratings.average();
    learnFactors(ratings.randomIndex(), true, true);
  }

  /**
   */
  public void iterate() {
    iterate(ratings.randomIndex(), true, true);
  }

  /**
   * Updates the latent factors on a user.
   * @param user_id the user ID
   */
  public void retrainUser(int user_id) {
    if (updateUsers) {
      MatrixExtensions.rowInitNormal(userFactors, user_id, initMean, initStDev);
      learnFactors(ratings.byUser().get(user_id), true, false);
    }
  }

  /**
   * Updates the latent factors of an item.
   * @param item_id the item ID
   */
  public void retrainItem(int item_id) {
    if (updateItems) {
      MatrixExtensions.rowInitNormal(itemFactors, item_id, initMean, initStDev);
      learnFactors(ratings.byItem().get(item_id), false, true);
    }
  }

  /**
   * Iterate once over rating data and adjust corresponding factors (stochastic gradient descent).
   * @param rating_indices a list of indices pointing to the ratings to iterate over
   * @param update_user true if user factors to be updated
   * @param update_item true if item factors to be updated
   */
  protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    for (int index : rating_indices) {
      int u = ratings.users().get(index);
      int i = ratings.items().get(index);

      double p = predict(u, i, false);
      double err = ratings.get(index) - p;

      // Adjust factors
      for (int f = 0; f < numFactors; f++) {
        double u_f = userFactors.get(u, f);
        double i_f = itemFactors.get(i, f);

        // If necessary, compute and apply updates
        if (update_user) {
          double delta_u = err * i_f - regularization * u_f;
          MatrixExtensions.inc(userFactors, u, f, learnRate * delta_u);
        }
        if (update_item) {
          double delta_i = err * u_f - regularization * i_f;
          MatrixExtensions.inc(itemFactors, i, f, learnRate * delta_i);
        }
      }
    }
  }

  private void learnFactors(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    for (int current_iter = 0; current_iter < numIter; current_iter++)
      iterate(rating_indices, update_user, update_item);
  }

  /**
   */
  protected double predict(int user_id, int item_id, boolean bound) {
    double result = globalBias + MatrixExtensions.rowScalarProduct(userFactors, user_id, itemFactors, item_id);

    if (bound) {
      if (result > maxRating)
        return maxRating;
      if (result < minRating)
        return minRating;
    }
    return result;
  }

  /**
   * Predict the rating of a given user for a given item.
   * 
   * If the user or the item are not known to the recommender, the global average is returned.
   * To avoid this behavior for unknown entities, use CanPredict() to check before.
   * 
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted rating
   */
  public double predict(int user_id, int item_id) {
    if (user_id >= userFactors.dim1)
      return globalBias;
    if (item_id >= itemFactors.dim1)
      return globalBias;

    return predict(user_id, item_id, true);
  }

  /**
   * 
   */
  public void addRating(int user_id, int item_id, double rating) {
    super.addRating(user_id, item_id, rating);
    retrainUser(user_id);
    retrainItem(item_id);
  }

  /**
   * 
   */
  public void updateRating(int user_id, int item_id, double rating) {
    super.updateRating(user_id, item_id, rating);
    retrainUser(user_id);
    retrainItem(item_id);
  }

  /**
   */
  public void removeRating(int user_id, int item_id) {
    super.removeRating(user_id, item_id);
    retrainUser(user_id);
    retrainItem(item_id);
  }

  /**
   */
  public void addUser(int user_id) {
    super.addUser(user_id);
    userFactors.addRows(user_id + 1);
  }

  /**
   * 
   */
  public void addItem(int item_id) {
    super.addItem(item_id);
    itemFactors.addRows(item_id + 1);
  }

  /**
   * 
   */
  public void removeUser(int user_id) {
    super.removeUser(user_id);

    // Set user factors to zero
    userFactors.setRowToOneValue(user_id, 0.0);
  }

  /**
   */
  public void removeItem(int item_id) {
    super.removeItem(item_id);

    // Set item factors to zero
    itemFactors.setRowToOneValue(item_id, 0.0);
  }

  /**
   * @throws IOException 
   */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(Double.toString(globalBias));
    IMatrixExtensions.writeMatrix(writer, userFactors);
    IMatrixExtensions.writeMatrix(writer, itemFactors);
  }

  /**
   */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    double bias = Double.parseDouble(reader.readLine());

    Matrix<Double> user_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    reader.close();

    if (user_factors.numberOfColumns() != item_factors.numberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.numberOfColumns() + " != " + item_factors.numberOfColumns());

    this.maxUserID = user_factors.numberOfRows() - 1;
    this.maxItemID = item_factors.numberOfRows() - 1;

    // Assign new model
    this.globalBias = bias;
    if (this.numFactors != user_factors.numberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.numberOfColumns());
      this.numFactors = user_factors.numberOfColumns();
    }
    this.userFactors = user_factors;
    this.itemFactors = item_factors;

  }

  /**
   * Compute the regularized loss.
   * @return the regularized loss
   */
  public double computeLoss() {
    double loss = 0;
    for (int i = 0; i < ratings.size(); i++) {
      int user_id = ratings.users().get(i);
      int item_id = ratings.items().get(i);
      loss += Math.pow(predict(user_id, item_id) - ratings.get(i), 2);
    }

    for (int u = 0; u <= maxUserID; u++)
      loss += ratings.countByUser().get(u) * regularization * Math.pow(VectorExtensions.euclideanNorm(userFactors.getRow(u)), 2);

    for (int i = 0; i <= maxItemID; i++)
      loss += ratings.countByItem().get(i) * regularization * Math.pow(VectorExtensions.euclideanNorm(itemFactors.getRow(i)), 2);

    return loss;
  }

  public String toString() {
    return 
        this.getClass().getName()
        + " numFactors=" + numFactors
        + " regularization=" + regularization
        + " learnRate=" + learnRate
        + " numIter=" + numIter
        + " initMean=" + initMean
        + " initStDev=" + initStDev;
  }

}
