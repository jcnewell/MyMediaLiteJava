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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.ratingprediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.mymedialite.IIterativeModel;
import org.mymedialite.data.IRatings;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;

/**
 * Matrix factorization with factor-wise learning.
 * 
 * Similar to the approach described in Simon Funk's seminal blog post: http://sifter.org/~simon/journal/20061211.html
 *     
 * Literature:
 *     Robert Bell, Yehuda Koren, Chris Volinsky:
 *     Modeling Relationships at Multiple Scales to Improve Accuracy of Large Recommender Systems,
 *     ACM Int. Conference on Knowledge Discovery and Data Mining (KDD'07), 2007.
 *
 * This recommender does NOT support incremental updates.
 */
public class FactorWiseMatrixFactorization extends RatingPredictor implements IIterativeModel {

  private static final String VERSION = "2.03";

  UserItemBaseline global_effects = new UserItemBaseline();
  int num_learned_factors;

  @Override
  public void setRatings(IRatings ratings) {
    super.setRatings(ratings);
    global_effects.setRatings(ratings);
  }

  /**
   * Regularization constant for the user bias of the underlying baseline predictor.
   */
  public double getRegU() {
    return global_effects.regU;
  } 

  public void setRegU(double regU) {
    global_effects.regU = regU; 
  }

  /**
   * Regularization constant for the item bias of the underlying baseline predictor.
   */
  public double getRegI() {
    return global_effects.regI;
  }

  public void setRegI(double regI) { 
    global_effects.regI = regI;
  }

  /**
   * Matrix containing the latent user factors.
   */
  Matrix<Double> userFactors;

  /**
   * Matrix containing the latent item factors.
   */
  Matrix<Double> itemFactors;

  /**
   * Number of latent factors.
   */
  public int numFactors;

  /**
   * Number of iterations (in this case: number of latent factors).
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
   * Shrinkage parameter.
   * 
   * alpha in the Bell et al. paper
   */
  public double shrinkage;

  /**
   * Sensibility parameter (stopping criterion for parameter fitting).
   * 
   * epsilon in the Bell et al. paper
   */
  public double sensibility;

  /**
   * Mean of the normal distribution used to initialize the factors.
   */
  public double initMean;

  /**
   * Standard deviation of the normal distribution used to initialize the factors.
   */
  public double initStDev;

  /**
   * Default constructor.
   */
  public FactorWiseMatrixFactorization() {
    super();
    // Set default values
    shrinkage = 25;
    numFactors = 10;
    numIter = 10;
    sensibility = 0.00001;
    initStDev = 0.1;
  }

  /**
   */
  public void train() {
    // Init factor matrices
    userFactors = new Matrix<Double>(maxUserID + 1, numFactors, 0.0);
    itemFactors = new Matrix<Double>(maxItemID + 1, numFactors, 0.0);

    // Init+train global effects model
    global_effects.setRatings(ratings);
    global_effects.train();

    // Learn model parameters
    num_learned_factors = 0;
    for (int i = 0; i < numIter; i++)
      iterate();
  }

  /**
   */
  public void iterate() {
    if (num_learned_factors >= numFactors)
      return;

    // Compute residuals
    double[] residuals = new double[ratings.size()];
    for (int index = 0; index < ratings.size(); index++) {
      int u = ratings.users().getInt(index);
      int i = ratings.items().getInt(index);
      residuals[index] = ratings.get(index) - predict(u, i);
      int n_ui = Math.min(ratings.byUser().get(u).size(), ratings.byItem().get(i).size());
      residuals[index] *= n_ui / (n_ui + shrinkage);
    }

    // Initialize new latent factors
    MatrixExtensions.columnInitNormal(userFactors, num_learned_factors, initMean, initStDev);
    MatrixExtensions.columnInitNormal(itemFactors, num_learned_factors, initMean, initStDev);

    // Compute the next factor by solving many least squares problems with one variable each
    double err     = Double.MAX_VALUE / 2;
    double err_old = Double.MAX_VALUE;
    while (err / err_old < 1 - sensibility) {
      double[] user_factors_update_numerator   = new double[maxUserID + 1];
      double[] user_factors_update_denominator = new double[maxUserID + 1];

      // Compute updates in one pass over the data
      for (int index = 0; index < ratings.size(); index++) {
        int u = ratings.users().getInt(index);
        int i = ratings.items().getInt(index);

        user_factors_update_numerator[u]   += residuals[index] * itemFactors.get(i, num_learned_factors);
        user_factors_update_denominator[u] += itemFactors.get(i, num_learned_factors) * itemFactors.get(i, num_learned_factors);
      }

      // Update user factors
      for (int u = 0; u <= maxUserID; u++)
        if (user_factors_update_numerator[u] != 0)
          userFactors.set(u, num_learned_factors, user_factors_update_numerator[u] / user_factors_update_denominator[u]);

      double[] item_factors_update_numerator   = new double[maxItemID + 1];
      double[] item_factors_update_denominator = new double[maxItemID + 1];

      // Compute updates in one pass over the data
      for (int index = 0; index < ratings.size(); index++) {
        int u = ratings.users().getInt(index);
        int i = ratings.items().getInt(index);

        item_factors_update_numerator[i]   += residuals[index] * userFactors.get(u, num_learned_factors);
        item_factors_update_denominator[i] += userFactors.get(u, num_learned_factors) * userFactors.get(u, num_learned_factors);
      }

      // Update item factors
      for (int i = 0; i <= maxItemID; i++)
        if (item_factors_update_numerator[i] != 0)
          itemFactors.set(i, num_learned_factors, item_factors_update_numerator[i] / item_factors_update_denominator[i]);

      err_old = err;
      err = org.mymedialite.eval.Ratings.computeFit(this);
    }

    num_learned_factors++;
  }

  /**
   * Predict the rating of a given user for a given item.
   * 
   * If the user or the item are not known to the recommender, the global effects prediction is returned.
   * To avoid this behavior for unknown entities, use canPredict() to check before.
   * 
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted rating
   */
  public double predict(int user_id, int item_id) {
    if (user_id >= userFactors.dim1 || item_id >= itemFactors.dim1)
      return global_effects.predict(user_id, item_id);

    double result = global_effects.predict(user_id, item_id) + MatrixExtensions.rowScalarProduct(userFactors, user_id, itemFactors, item_id);

    if (result > maxRating)
      return maxRating;
    if (result < minRating)
      return minRating;

    return result;
  }

  /**
   * @throws IOException 
   */
  public void saveModel(String filename) throws IOException {
    global_effects.saveModel(filename + "-global-effects");
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(num_learned_factors);
    IMatrixExtensions.writeMatrix(writer, userFactors);
    IMatrixExtensions.writeMatrix(writer, itemFactors);
  }

  /**
   * @throws IOException 
   */
  public void loadModel(String filename) throws IOException {
    global_effects.loadModel(filename + "-global-effects");
    if (ratings != null)
      global_effects.setRatings(ratings);

    BufferedReader reader = Model.getReader(filename, this.getClass());
    int num_learned_factors = Integer.parseInt(reader.readLine());

    Matrix<Double> user_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.numberOfColumns() != item_factors.numberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.numberOfColumns() + " != " + item_factors.numberOfColumns());

    this.maxUserID = user_factors.numberOfRows() - 1;
    this.maxItemID = item_factors.numberOfRows() - 1;

    // Assign new model
    this.num_learned_factors = num_learned_factors;
    if (this.numFactors != user_factors.numberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.numberOfColumns());
      this.numFactors = user_factors.numberOfColumns();
    }
    this.userFactors = user_factors;
    this.itemFactors = item_factors;
  }

  /**
   */
  public double computeLoss() {
    return -1;
  }

  public String toString() {
    return 
        this.getClass().getName()
        + " numFactors=" + numFactors
        + " shrinkage=" + shrinkage
        + " sensibility=" + sensibility
        + " initMean=" + initMean
        + " initStDev=" + initStDev
        + " numIter=" + numIter
        + " regU" + getRegU()
        + " regI" + getRegI();
  }

}

