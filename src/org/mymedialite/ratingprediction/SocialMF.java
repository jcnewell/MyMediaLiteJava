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

import java.util.List;

import org.mymedialite.IUserRelationAwareRecommender;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.datatype.VectorExtensions;

/**
 * Social-network-aware matrix factorization.
 * 
 * This implementation assumes a binary and symmetrical trust network.
 *
 * Mohsen Jamali, Martin Ester:
 * A matrix factorization technique with trust propagation for recommendation in social networks
 * RecSys '10: Proceedings of the Fourth ACM Conference on Recommender Systems, 2010
 * @version 2.03
 */
public class SocialMF extends BiasedMatrixFactorization implements IUserRelationAwareRecommender {
  // TODO
  //  - implement MAE optimization or throw Exception
  //  - implement bold-driver support or throw Exception

  /**
   * Social network regularization constant.
   */
  private double socialRegularization = 1;

  //  /**
  //   * Use stochastic gradient descent instead of batch gradient descent.
  //   */
  //  public boolean stochasticLearning;

  /**
   * 
   */
  public SparseBooleanMatrix getUserRelation() {
    return user_neighbors;
  }

  /**
   * 
   */
  public void setUserRelation(SparseBooleanMatrix user_neighbors) {
    this.user_neighbors = user_neighbors;
  }

  private SparseBooleanMatrix user_neighbors;

  /**
   * The number of users.
   */
  public int numUsers() {
    return maxUserID + 1;
  }

  /**
   * 
   */
  protected void initModel() {
    super.initModel();
    this.maxUserID = Math.max(maxUserID, user_neighbors.numberOfRows() - 1);
    this.maxUserID = Math.max(maxUserID, user_neighbors.numberOfColumns() - 1);

    // Init latent factor matrices
    userFactors = new Matrix<Double>(numUsers(), numFactors);
    itemFactors = new Matrix<Double>(ratings.maxItemID() + 1, numFactors);
    MatrixExtensions.initNormal(userFactors, initMean, initStDev);
    MatrixExtensions.initNormal(itemFactors, initMean, initStDev);

    // Init biases
    userBias = new double[numUsers()];
    itemBias = new double[ratings.maxItemID() + 1];
  }

  /**
   * 
   */
  public void train() {
    initModel();

    System.err.println("numUsers=" + numUsers() + " numItems=" + itemBias.length);

    // Compute global average
    double global_average = 0;
    global_average = ratings.average();

    // Learn model parameters
    globalBias = Math.log((global_average - minRating) / (maxRating - global_average));
    for (int current_iter = 0; current_iter < numIter; current_iter++)
      iterate(ratings.randomIndex(), true, true);
  }

  /**
   */
  protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    // We ignore the method's arguments. FIXME
    iterateBatch();
  }

  private void iterateBatch() {
    // I. compute gradients
    Matrix<Double> userFactors_gradient = new Matrix<Double>(userFactors.dim1, userFactors.dim2);
    Matrix<Double> itemFactors_gradient = new Matrix<Double>(itemFactors.dim1, itemFactors.dim2);
    double[] user_bias_gradient    = new double[userFactors.dim1];
    double[] item_bias_gradient    = new double[itemFactors.dim1];

    // I.1 prediction error
    double rating_range_size = maxRating - minRating;
    for (int index = 0; index < ratings.size(); index++) {
      int u = ratings.users().get(index);
      int i = ratings.items().get(index);

      // Prediction
      double score = globalBias;
      score += userBias[u];
      score += itemBias[i];
      for (int f = 0; f < numFactors; f++)
        score += userFactors.get(u, f) * itemFactors.get(i, f);
      double sig_score = 1 / (1 + Math.exp(-score));

      double prediction = minRating + sig_score * rating_range_size;
      double error      = ratings.get(index) - prediction;

      double gradient_common = error * sig_score * (1 - sig_score) * rating_range_size;

      // Add up error gradient
      for (int f = 0; f < numFactors; f++) {
        double u_f = userFactors.get(u, f);
        double i_f = itemFactors.get(i, f);

        if (f != 0)
          MatrixExtensions.inc(userFactors_gradient, u, f, gradient_common * i_f);
        if (f != 1)
          MatrixExtensions.inc(itemFactors_gradient, i, f, gradient_common * u_f);
      }
    }

    // I.2 L2 regularization
    //        biases
    for (int u = 0; u < user_bias_gradient.length; u++)
      user_bias_gradient[u] += userBias[u] * regularization;

    for (int i = 0; i < item_bias_gradient.length; i++)
      item_bias_gradient[i] += itemBias[i] * regularization;

    //        latent factors
    for (int u = 0; u < userFactors_gradient.dim1; u++)
      for (int f = 2; f < numFactors; f++)
        MatrixExtensions.inc(userFactors_gradient, u, f, userFactors.get(u, f) * regularization);

    for (int i = 0; i < itemFactors_gradient.dim1; i++)
      for (int f = 2; f < numFactors; f++)
        MatrixExtensions.inc(itemFactors_gradient, i, f, itemFactors.get(i, f) * regularization);

    // I.3 social network regularization
    for (int u = 0; u < userFactors_gradient.dim1; u++) {
      // see eq. (13) in the paper
      double[] sum_neighbors    = new double[numFactors];
      double bias_sum_neighbors = 0;
      int num_neighbors = user_neighbors.get(u).size();

      // User bias part
      for (int v : user_neighbors.get(u))
        bias_sum_neighbors += userBias[v];

      if (num_neighbors != 0)
        user_bias_gradient[u] += socialRegularization * (userBias[u] - bias_sum_neighbors / num_neighbors);

      for (int v : user_neighbors.get(u))
        if (user_neighbors.get(v).size() != 0) {
          double trust_v = (double) 1 / user_neighbors.get(v).size();
          double diff = 0;
          for (int w : user_neighbors.get(v))
            diff -= userBias[w];

          diff = diff * trust_v;
          diff += userBias[v];

          if (num_neighbors != 0)
            user_bias_gradient[u] -= socialRegularization * trust_v * diff / num_neighbors;
        }

      // Latent factor part
      for (int v : user_neighbors.get(u))
        for (int f = 0; f < numFactors; f++)
          sum_neighbors[f] += userFactors.get(v, f);

      if (num_neighbors != 0)
        for (int f = 0; f < numFactors; f++)
          MatrixExtensions.inc(userFactors_gradient, u, f, socialRegularization * (userFactors.get(u, f) - sum_neighbors[f] / num_neighbors));

      for (int v : user_neighbors.get(u))
        if (user_neighbors.get(v).size() != 0) {
          double trust_v = (double) 1 / user_neighbors.get(v).size();
          for (int f = 0; f < numFactors; f++) {
            double diff = 0;
            for (int w : user_neighbors.get(v))
              diff -= userFactors.get(w, f);
            
            diff = diff * trust_v;
            diff += userFactors.get(v, f);
            if (num_neighbors != 0)
              MatrixExtensions.inc(userFactors_gradient, u, f, -socialRegularization * trust_v * diff / num_neighbors);
          }
        }
    }

    // II. apply gradient descent step
    for (int u = 0; u < userFactors_gradient.dim1; u++) {
      userBias[u] += user_bias_gradient[u] * learnRate;
      for (int f = 2; f < numFactors; f++)
        MatrixExtensions.inc(userFactors, u, f, userFactors_gradient.get(u, f) * learnRate);
    }
    for (int i = 0; i < itemFactors_gradient.dim1; i++) {
      itemBias[i] += item_bias_gradient[i] * learnRate;
      for (int f = 2; f < numFactors; f++)
        MatrixExtensions.inc(itemFactors, i, f, itemFactors_gradient.get(i, f) * learnRate);
    }
  }

  /**
   * 
   */
  public double computeLoss() {
    double loss = 0;

    for (int i = 0; i < ratings.size(); i++) {
      int user_id = ratings.users().get(i);
      int item_id = ratings.items().get(i);
      loss += Math.pow(predict(user_id, item_id) - ratings.get(i), 2);
    }

    double complexity = 0;
    for (int u = 0; u <= maxUserID; u++)
      if (ratings.countByUser().size() > u) {
        complexity += ratings.countByUser().get(u) * regU * Math.pow(VectorExtensions.euclideanNorm(userFactors.getRow(u)), 2);
        complexity += ratings.countByUser().get(u) * biasReg * Math.pow(userBias[u], 2);
      }
    for (int i = 0; i <= maxItemID; i++) {
      if (ratings.countByItem().size() > i) {
        complexity += ratings.countByItem().get(i) * regI * Math.pow(VectorExtensions.euclideanNorm(itemFactors.getRow(i)), 2);
        complexity += ratings.countByItem().get(i) * biasReg * Math.pow(itemBias[i], 2);
      }
    }
    
    // TODO add penality term for neighborhood regularization

    return loss + complexity;
  }

  /** {@inheritDoc} */
  public String toString() {
    return 
        this.getClass().getName()
        + " numFactors=" + numFactors
        + " regularization=" + regularization
        + " socialRegularization=" + socialRegularization
        + " learnRate=" + learnRate
        + " numIter=" + numIter
        + " initMean=" + initMean
        + " initStDev=" + initStDev;   
  }

}
