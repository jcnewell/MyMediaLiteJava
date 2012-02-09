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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;
import org.mymedialite.io.VectorExtensions;
import org.mymedialite.util.Recommender;


/**
 * Matrix factorization engine with explicit user and item bias.
 * 
 * @version 2.03
 */
public class BiasedMatrixFactorization extends MatrixFactorization {
  
  private static final String VERSION = "2.03";
  
  /** Regularization constant for the bias terms. */
  public double biasReg = 0;

  /** Regularization constant for the user factors. */
  public double regU;

  /** Regularization constant for the item factors. */
  public double regI;
  
  /** The user biases */
  protected double[] userBias;

  /** The item biases */
  protected double[] itemBias;
  
  /**
   * Set the regularization parameters.
   * @param regularization
   * @param regU
   * @param regI
   */
  public void setRegularization(double regularization, double regU, double regI) {
    super.regularization = regularization;
    this.regU = regU;
    this.regI = regI;
  }
  
  /** If set to true, optimize model for MAE instead of RMSE. */
  public boolean optimizeMAE;

  /**
   * Use bold driver heuristics for learning rate adaption.
   *
   * Literature:
   *     Rainer Gemulla, Peter J. Haas, Erik Nijkamp, Yannis Sismanis:
   *     Large-Scale Matrix Factorization with Distributed Stochastic Gradient Descent.
   *     KDD 2011.
   *     http://www.mpi-inf.mpg.de/~rgemulla/publications/gemulla11dsgd.pdf
   */
  public boolean boldDriver;
  
  /** {@inheritDoc} */
  public void train() {
    // Init factor matrices
    userFactors = new Matrix<Double>(maxUserID + 1, numFactors);
    itemFactors = new Matrix<Double>(maxItemID + 1, numFactors);
    MatrixExtensions.initNormal(userFactors, initMean, initStdDev);
    MatrixExtensions.initNormal(itemFactors, initMean, initStdDev);

    userBias = new double[maxUserID + 1];
    for (int u = 0; u <= maxUserID; u++)  userBias[u] = 0;
    itemBias = new double[maxItemID + 1];
    for (int i = 0; i <= maxItemID; i++)  itemBias[i] = 0;

    // learn model parameters

    // compute global average
    double global_average = ratings.average();

    // TODO also learn global bias?
    globalBias = Math.log( (global_average - getMinRating()) / (getMaxRating() - global_average) );
    for (int current_iter = 0; current_iter < numIter; current_iter++) {
      iterate();
    }
  }

  /** {@inheritDoc} */
  protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    double rating_range_size = getMaxRating() - getMinRating();

    for (int index : rating_indices) {
      int u = ratings.users().get(index);
      int i = ratings.items().get(index);

      double dot_product = globalBias + userBias[u] + itemBias[i];
      for (int f = 0; f < numFactors; f++) {
        dot_product += userFactors.get(u, f) * itemFactors.get(i, f);
      }
      double sig_dot = 1 / (1 + Math.exp(-dot_product));

      double p = getMinRating() + sig_dot * rating_range_size;
      double err = getRatings().get(index) - p;

      double gradient_common = err * sig_dot * (1 - sig_dot) * rating_range_size;

      // Adjust biases
      if (update_user)
    	  userBias[u] += learnRate * (gradient_common - biasReg * userBias[u]);
      if (update_item)
    	  itemBias[i] += learnRate * (gradient_common - biasReg * itemBias[i]);

      // Adjust latent factors
      for (int f = 0; f < numFactors; f++) {
        double u_f = userFactors.get(u, f);
        double i_f = itemFactors.get(i, f);

        if (update_user) {
          double delta_u = gradient_common * i_f - regularization * u_f;
          MatrixExtensions.inc(userFactors, u, f, learnRate * delta_u);
          // this is faster (190 vs. 260 seconds per iteration on Netflix w/ k=30) than
          //    user_factors[u, f] += learn_rate * delta_u;
        }

        if (update_item) {
              double delta_i = gradient_common * u_f - regularization * i_f;
              MatrixExtensions.inc(itemFactors, i, f, learnRate * delta_i);
              // item_factors[i, f] += learn_rate * delta_i;
        }
      }
    }
  }

  /** {@inheritDoc} */
  public double predict(int user_id, int item_id) {
    if (user_id >= userFactors.dim1 || item_id >= itemFactors.dim1) {
      return getMinRating() + ( 1 / (1 + Math.exp(-globalBias)) ) * (getMaxRating() - getMinRating());
    }
    
    double score = globalBias + userBias[user_id] + itemBias[item_id];

    // U*V
    for (int f = 0; f < numFactors; f++) {
      score += userFactors.get(user_id, f) * itemFactors.get(item_id, f);
    }
    
    return getMinRating() + ( 1 / (1 + Math.exp(-score)) ) * (getMaxRating() - getMinRating());
  }

  /** {@inheritDoc} */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    writer.println(Double.toString(globalBias));
    VectorExtensions.writeVectorArray(writer, userBias);
    IMatrixExtensions.writeMatrix(writer, userFactors);
    VectorExtensions.writeVectorArray(writer, itemBias);
    IMatrixExtensions.writeMatrix(writer, itemFactors);
    boolean error = writer.checkError();
    if(error) System.out.println("Error writing file.");
    writer.close();
  }

  /** {@inheritDoc} */
  public void loadModel(String filename) throws IOException  {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    double bias = Double.parseDouble(reader.readLine());
    double[] user_bias = VectorExtensions.readVectorArray(reader);
    Matrix<Double> user_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    double[] item_bias = VectorExtensions.readVectorArray(reader);
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.numberOfColumns() != item_factors.numberOfColumns()) {
      throw new IOException("Number of user and item factors must match: " + user_factors.numberOfColumns() + " != " + item_factors.numberOfColumns());
    }

    if (user_bias.length != user_factors.dim1) {
      throw new IOException("Number of users must be the same for biases and factors: " + user_bias.length + " != " +  user_factors.dim1);
    }
    if (item_bias.length != item_factors.dim1) {
      throw new IOException("Number of items must be the same for biases and factors: " + item_bias.length + " != " + item_factors.dim1);
    }
    
    this.maxUserID = user_factors.dim1 - 1;
    this.maxItemID = item_factors.dim1 - 1;

    // assign new model
    this.globalBias = bias;
    if (this.numFactors != user_factors.dim2) {
      System.out.println("Set numFactors to " + user_factors.dim1);
      this.numFactors = user_factors.dim2;
    }
    this.userFactors = user_factors;
    this.itemFactors = item_factors;
    this.userBias = user_bias;
    this.itemBias = item_bias;
  }

  /** {@inheritDoc} */
  public void addUser(int user_id) {
    if (user_id > maxUserID) {
      super.addUser(user_id);

      // create new user bias array
      double[] user_bias = Arrays.copyOf(this.userBias, user_id + 1);
      this.userBias = user_bias;
    }
  }

  /** {@inheritDoc} */
  public void addItem(int item_id) {
    if (item_id > maxItemID) {
      super.addItem(item_id);

      // create new item bias array
      double[] item_bias = Arrays.copyOf(this.itemBias, item_id + 1);
      this.itemBias = item_bias;
    }
  }

  /** {@inheritDoc} */
  public void retrainUser(int user_id) {
    userBias[user_id] = 0;
    super.retrainUser(user_id);
  }

  /** {@inheritDoc} */
  public void retrainItem(int item_id) {
      itemBias[item_id] = 0;
      super.retrainItem(item_id);
  }

  /** {@inheritDoc} */
  public void removeUser(int user_id) {
      super.removeUser(user_id);
      userBias[user_id] = 0;
  }

  /** {@inheritDoc} */
  public void removeItem(int item_id) {
      super.removeItem(item_id);
      itemBias[item_id] = 0;
  }

  /** {@inheritDoc} */
  public String toString() {
    return 
        this.getClass().getName()
        + " numFactors=" + numFactors
        + " biasReg=" + biasReg
        + " regularization=" + regularization
        + " learnRate=" + learnRate
        + " numIter=" + numIter
        + " initMean=" + initMean
        + " initStdDev=" + initStdDev;   
  }
  
}
