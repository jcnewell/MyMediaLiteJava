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

import org.mymedialite.datatype.IMatrixUtils;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixUtils;
import org.mymedialite.datatype.VectorUtils;
import org.mymedialite.util.Recommender;

/** Matrix factorization engine with explicit user and item bias */
public class BiasedMatrixFactorization extends MatrixFactorization {
  
  /** Regularization constant for the bias terms */
  public double bias_regularization = 0;

  /** the user biases */
  protected double[] user_bias;

  /** the item biases */
  protected double[] item_bias;
  
  /// <inheritdoc/>
  public void train() {
    // init factor matrices
    user_factors = new Matrix<Double>(maxUserID + 1, numFactors);
    item_factors = new Matrix<Double>(maxItemID + 1, numFactors);
    MatrixUtils.initNormal(user_factors, initMean, initStdev);
    MatrixUtils.initNormal(item_factors, initMean, initStdev);

    user_bias = new double[maxUserID + 1];
    for (int u = 0; u <= maxUserID; u++)  user_bias[u] = 0;
    item_bias = new double[maxItemID + 1];
    for (int i = 0; i <= maxItemID; i++)  item_bias[i] = 0;

    // learn model parameters

    // compute global average
    double global_average = ratings.getAverage();

    // TODO also learn global bias?
    global_bias = Math.log( (global_average - getMinRating()) / (getMaxRating() - global_average) );
    for (int current_iter = 0; current_iter < num_iter; current_iter++) {
      iterate();
    }
  }

  /// <inheritdoc/>
  protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
    double rating_range_size = getMaxRating() - getMinRating();

    for (int index : rating_indices) {
      int u = ratings.getUsers().get(index);
      int i = ratings.getItems().get(index);

      double dot_product = global_bias + user_bias[u] + item_bias[i];
      for (int f = 0; f < numFactors; f++) {
        dot_product += user_factors.get(u, f) * item_factors.get(i, f);
      }
      double sig_dot = 1 / (1 + Math.exp(-dot_product));

      double p = getMinRating() + sig_dot * rating_range_size;
      double err = getRatings().get(index) - p;

      double gradient_common = err * sig_dot * (1 - sig_dot) * rating_range_size;

      // Adjust biases
      if (update_user)  user_bias[u] += learn_rate * (gradient_common - bias_regularization * user_bias[u]);
      if (update_item)  item_bias[i] += learn_rate * (gradient_common - bias_regularization * item_bias[i]);

      // Adjust latent factors
      for (int f = 0; f < numFactors; f++) {
        double u_f = user_factors.get(u, f);
        double i_f = item_factors.get(i, f);

        if (update_user) {
          double delta_u = gradient_common * i_f - regularization * u_f;
          MatrixUtils.inc(user_factors, u, f, learn_rate * delta_u);
          // this is faster (190 vs. 260 seconds per iteration on Netflix w/ k=30) than
          //    user_factors[u, f] += learn_rate * delta_u;
        }

        if (update_item) {
              double delta_i = gradient_common * u_f - regularization * i_f;
              MatrixUtils.inc(item_factors, i, f, learn_rate * delta_i);
              // item_factors[i, f] += learn_rate * delta_i;
        }
      }
    }
  }

  /// <inheritdoc/>
  public double predict(int user_id, int item_id) {
    if (user_id >= user_factors.dim1 || item_id >= item_factors.dim1) {
      return getMinRating() + ( 1 / (1 + Math.exp(-global_bias)) ) * (getMaxRating() - getMinRating());
    }
    
    double score = global_bias + user_bias[user_id] + item_bias[item_id];

    // U*V
    for (int f = 0; f < numFactors; f++) {
      score += user_factors.get(user_id, f) * item_factors.get(item_id, f);
    }
    
    return getMinRating() + ( 1 / (1 + Math.exp(-score)) ) * (getMaxRating() - getMinRating());
  }

  /// <inheritdoc/>
  public void SaveModel(String filename) throws IOException {
    PrintWriter writer = Recommender.getWriter(filename, this.getClass());
    writer.println(Double.toString(global_bias));
    VectorUtils.writeVectorArray(writer, user_bias);
    IMatrixUtils.writeMatrix(writer, user_factors);
    VectorUtils.writeVectorArray(writer, item_bias);
    IMatrixUtils.writeMatrix(writer, item_factors);
    boolean error = writer.checkError();
    if(error) System.out.println("Error writing file.");
    writer.close();
  }

  /// <inheritdoc/>
  public void loadModel(String filename) throws IOException  {
    BufferedReader reader = Recommender.getReader(filename, this.getClass());
    double bias = Double.parseDouble(reader.readLine());
    double[] user_bias = VectorUtils.readVectorArray(reader);
    Matrix<Double> user_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    double[] item_bias = VectorUtils.readVectorArray(reader);
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.getNumberOfColumns() != item_factors.getNumberOfColumns()) {
      throw new IOException("Number of user and item factors must match: " + user_factors.getNumberOfColumns() + " != " + item_factors.getNumberOfColumns());
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
    this.global_bias = bias;
    if (this.numFactors != user_factors.dim2) {
      System.out.println("Set numFactors to " + user_factors.dim1);
      this.numFactors = user_factors.dim2;
    }
    this.user_factors = user_factors;
    this.item_factors = item_factors;
    this.user_bias = user_bias;
    this.item_bias = item_bias;
  }

  /// <inheritdoc/>
  public void addUser(int user_id) {
    if (user_id > maxUserID) {
      super.addUser(user_id);

      // create new user bias array
      double[] user_bias = Arrays.copyOf(this.user_bias, user_id + 1);
      this.user_bias = user_bias;
    }
  }

  /// <inheritdoc/>
  public void addItem(int item_id) {
    if (item_id > maxItemID) {
      super.addItem(item_id);

      // create new item bias array
      double[] item_bias = Arrays.copyOf(this.item_bias, item_id + 1);
      this.item_bias = item_bias;
    }
  }

  /// <inheritdoc/>
  public void retrainUser(int user_id) {
    user_bias[user_id] = 0;
    super.retrainUser(user_id);
  }

  /// <inheritdoc/>
  public void retrainItem(int item_id) {
      item_bias[item_id] = 0;
      super.retrainItem(item_id);
  }

  /// <inheritdoc/>
  public void RemoveUser(int user_id) {
      super.removeUser(user_id);
      user_bias[user_id] = 0;
  }

  /// <inheritdoc/>
  public void RemoveItem(int item_id) {
      super.removeItem(item_id);
      item_bias[item_id] = 0;
  }

  /// <inheritdoc/>
  public String ToString() {
    return "BiasedMatrixFactorization num_factors=" + numFactors +
           " bias_regularization=" + bias_regularization +
           " regularization=" + regularization +
           " learn_rate=" + learn_rate+
           " num_iter=" + num_iter +
           " init_mean=" + initMean +
           " init_stdev=" + initStdev;   
  }
}
