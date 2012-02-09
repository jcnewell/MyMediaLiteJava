// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011, 2012 Zeno Gantner, Chris Newell
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

import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.datatype.VectorExtensions;

/**
 * Matrix factorization with explicit user and item bias, learning is performed by 
 * stochastic gradient descent, optimized for the log likelihood.
 * 
 * Implements a simple version Menon and Elkan's LFL model:
 * Predicts binary labels, no advanced regUlarization, no side information.
 *
 * Literature:
 *
 *     Aditya Krishna Menon, Charles Elkan:
 *     A log-linear model with latent features for dyadic prediction.
 *     ICDM 2010.
 *     http://cseweb.ucsd.edu/~akmenon/LFL-ICDM10.pdf
 *
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class LogisticRegressionMatrixFactorization extends BiasedMatrixFactorization {

  /**
   * 
   */
  protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    double rating_range_size = maxRating - minRating;

    for (int index : rating_indices) {
      int u = ratings.users().get(index);
      int i = ratings.items().get(index);

      double dot_product = userBias[u] + itemBias[i] + MatrixExtensions.rowScalarProduct(userFactors, u, itemFactors, i);
      double sig_dot = 1 / (1 + Math.exp(-dot_product));

      double prediction = minRating + sig_dot * rating_range_size;
      double gradient_common = ratings.get(index) - prediction;

      // Adjust biases
      if (update_user)
        userBias[u] = userBias[u] + learnRate * (gradient_common - biasReg * userBias[u]);
      if (update_item)
        itemBias[i] = learnRate * (gradient_common - biasReg * itemBias[i]);

      // Adjust latent factors
      for (int f = 0; f < numFactors; f++) {
        double u_f = userFactors.get(u, f);
        double i_f = itemFactors.get(i, f);

        if (update_user) {
          double delta_u = gradient_common * i_f - regU * u_f;
          MatrixExtensions.inc(userFactors, u, f, learnRate * delta_u);
          // this is faster (190 vs. 260 seconds per iteration on Netflix w/ k=30) than
          //    userFactorsu, f) += learn_rate * delta_u;
        }
        if (update_item) {
          double delta_i = gradient_common * u_f - regI * i_f;
          MatrixExtensions.inc(itemFactors, i, f, learnRate * delta_i);
        }
      }
    }
  }

  /**
   * 
   */
  public double computeLoss() {
    double rating_range_size = maxRating - minRating;

    double loss = 0;
    for (int i = 0; i < ratings.size(); i++) {
      double prediction = predict(ratings.users().get(i), ratings.items().get(i));

      // Map into [0, 1] interval
      prediction = (prediction - minRating) / rating_range_size;
      if (prediction < 0.0)
        prediction = 0.0;
      if (prediction > 1.0)
        prediction = 1.0;
      double actual_rating = (ratings.get(i) - minRating) / rating_range_size;

      loss -= (actual_rating) * Math.log(prediction);
      loss -= (1 - actual_rating) * Math.log(1 - prediction);
    }

    double complexity = 0;
    for (int u = 0; u <= maxUserID; u++) {
      complexity += ratings.countByUser().get(u) * regU * Math.pow(VectorExtensions.euclideanNorm(userFactors.getRow(u)), 2);
      complexity += ratings.countByUser().get(u) * biasReg * Math.pow(userBias[u], 2);
    }
    
    for (int i = 0; i <= maxItemID; i++) {
      complexity += ratings.countByItem().get(i) * regI * Math.pow(VectorExtensions.euclideanNorm(itemFactors.getRow(i)), 2);
      complexity += ratings.countByItem().get(i) * biasReg * Math.pow(itemBias[i], 2);
    }

    return loss + complexity;
  }
  
  public String toString() {
    return 
        this.getClass().getName()
    	+ " numFactors=" + numFactors
    	+ " biasReg=" + biasReg
    	+ " regI=" + regI
    	+ " regU=" + regU
    	+ " learnRate=" + learnRate
    	+ " numIter=" + numIter
    	+ " boldDriver=" + boldDriver
    	+ " initMean=" + initMean
    	+ " initStdDev=" + initStdDev;
  }
  
}
