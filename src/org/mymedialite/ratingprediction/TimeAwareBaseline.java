//Copyright (C) 2011 Zeno Gantner, Chris Newell
//
//This file is part of MyMediaLite.
//
//MyMediaLite is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//MyMediaLite is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.
//

package org.mymedialite.ratingprediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.datatype.SparseMatrix;
import org.mymedialite.datatype.SparseMatrixExtensions;
import org.mymedialite.datatype.VectorExtensions;
import org.mymedialite.eval.Ratings;

/**
 * Time-aware bias model.
 * 
 * Model described in equation (10) of BellKor Grand Prize documentation for the Netflix Prize (see below).
 * The optimization problem is described in equation (12).
 *
 * The default hyper-parameter values are set to the ones shown in the report.
 * For datasets other than Netflix, you may want to find better parameters.
 *
 * Literature:

 *   Yehuda Koren: The BellKor Solution to the Netflix Grand Prize
 *
 * This recommender does currently NOT support incremental updates.
 * @version 2.03
 */
public class TimeAwareBaseline extends TimeAwareRatingPredictor implements IIterativeModel {
  
  // Parameters
  
  double global_average;
  List<Double> user_bias;
  List<Double> item_bias;
  List<Double> alpha;
  Matrix<Double> item_bias_by_time_bin;  // items : rows, bins : columns
  SparseMatrix<Double> user_bias_by_day; // users : rows, days : columns
  List<Double> user_scaling;               // c_u
  SparseMatrix<Double> user_scaling_by_day; // c_ut

  // Hyperparameters

  /**
   * Number of iterations over the dataset to perform.
   */
  public int numIter;
  
  /**
   * Bin size in days for modeling the time-dependent item bias.
   */
  public int binSize;

  /**
   * Beta parameter for modeling the drift in the user bias.
   */
  public double beta;

  // Parameter-specific learn rates

  /**
   * Learn rate for the user bias.
   */
  public double userBiasLearnRate;

  /**
   * Learn rate for the item bias.
   */
  public double itemBiasLearnRate;

  /**
   * Learn rate for the user-wise alphas.
   */
  public double alphaLearnRate;
  
  /**
   * Learn rate for the bin-wise item bias.
   */
  public double itemBiasByTimeBinLearnRate;

  /**
   * Learn rate for the day-wise user bias.
   */
  public double userBiasByDayLearnRate;

  /**
   * Learn rate for the user-wise scaling factor.
   */
  public double userScalingLearnRate;

  /**
   * Learn rate for the day-wise user scaling factor.
   */
  public double userScalingByDayLearnRate;

  // Parameter-specific regularization constants

  /**
   * Regularization for the user bias.
   */
  public double regU;

  /**
   * Regularization for the item bias.
   */
  public double regI;

  /**
   * Regularization for the user-wise alphas.
   */
  public double regAlpha;

  /**
   * Regularization for the bin-wise item bias.
   */
  public double regItemBiasByTimeBin;

  /**
   * Regularization for the day-wise user bias.
   */
  public double regUserBiasByDay;

  /**
   * Regularization for the user scaling factor.
   */
  public double regUserScaling;

  /**
   * Regularization for the day-wise user scaling factor.
   */
  public double regUserScalingByDay;

  // Helper data structures
  List<Double> userMeanDay;

  /**
   * Default constructor.
   */
  public TimeAwareBaseline() {
    numIter = 30;
    binSize = 70;
    beta = 0.4;

    userBiasLearnRate = 0.003;
    itemBiasLearnRate = 0.002;
    alphaLearnRate = 0.00001;
    itemBiasByTimeBinLearnRate = 0.000005;
    userBiasByDayLearnRate = 0.0025;
    userScalingLearnRate = 0.008;
    userScalingByDayLearnRate = 0.002;

    regU = 0.03;
    regI = 0.03;
    regAlpha = 50;
    regItemBiasByTimeBin = 0.1;
    regUserBiasByDay = 0.005;
    regUserScaling = 0.01;
    regUserScalingByDay = 0.005;
  }
  
  @Override
  public void setNumIter(int numIter) {
    this.numIter = numIter;
  }

  @Override
  public int getNumIter() {
    return numIter;
  }

  /**
   */
  public void train() {
    initModel();
    global_average = ratings.average();
    
    // Compute mean day of rating by user
    userMeanDay = new ArrayList<Double>(maxUserID + 1);
    for(int i = 0; i <= maxUserID; i++)
      userMeanDay.add(0.0);

    for (int i = 0; i < timed_ratings.size(); i++)
      userMeanDay.set(ratings.users().get(i), userMeanDay.get(ratings.users().get(i)) + relativeDay(timed_ratings.times().get(i)));  
    
    for (int u = 0; u <= maxUserID; u++)
      if (ratings.countByUser().get(u) != 0)
        userMeanDay.set(u, userMeanDay.get(u) / ratings.countByUser().get(u));
      else // no ratings yet?
        userMeanDay.set(u, new Double(relativeDay(timed_ratings.latestTime()))); // set to latest day

    for (int i = 0; i < numIter; i++)
      iterate();
  }

  /**
   * Given a Date object, return the day relative to the first rating day in the dataset.
   * @return the day relative to the first rating day in the dataset
   * @param date the date/time of the rating event
   */
  protected int relativeDay(Date date) {
   return (int)((date.getTime() - timed_ratings.earliestTime().getTime()) / (24 * 3600000));
  }

  /**
   * Initialize the model parameters.
   */
  protected void initModel() {
    
    int number_of_days = (int)((timed_ratings.latestTime().getTime() - timed_ratings.earliestTime().getTime()) / (24 * 3600000));

    int number_of_bins = number_of_days / binSize + 1;
    System.out.println(number_of_days + " days, " + number_of_bins + " bins");

    // Initialize parameters
    user_bias = new ArrayList<Double>(maxUserID + 1);    
    item_bias = new ArrayList<Double>(maxItemID + 1);
    alpha = new ArrayList<Double>(maxUserID + 1);

    item_bias_by_time_bin = new Matrix<Double>(maxItemID + 1, number_of_bins, 0.0);
    user_bias_by_day = new SparseMatrix<Double>(maxUserID + 1, number_of_days, 0.0);
    user_scaling = new ArrayList<Double>(maxUserID + 1);
    user_scaling_by_day = new SparseMatrix<Double>(maxUserID + 1, number_of_days, 0.0);

    for (int i = 0; i <= maxUserID; i++) {
      user_bias.add(0.0);
      item_bias.add(0.0);
      alpha.add(0.0);
      user_scaling.add(0.0);
    }
  }

  /**
   */
  public void iterate() {
    for (int index : timed_ratings.randomIndex()) {
      int u = timed_ratings.users().get(index);
      int i = timed_ratings.items().get(index);
      int day = relativeDay(timed_ratings.times().get(index));
      int bin = day / binSize;
      
      // Compute error
      double err = timed_ratings.get(index) - predict(u, i, day, bin);
      updateParameters(u, i, day, bin, err);
    }
  }

  /**
   * Single SGD step: update the parameter values for one user and one item.
   * <param name='u'>the user ID
   * <param name='i'>the item ID
   * <param name='day'>the day of the rating
   * <param name='bin'>the day bin of the rating
   * <param name='err'>the current error made for this rating
   */
  protected void updateParameters(int u, int i, int day, int bin, double err) {
    // Update user biases
    double dev_u = Math.signum(day - userMeanDay.get(u)) * Math.pow(Math.abs(day - userMeanDay.get(u)), beta);
    alpha.set(u, alpha.get(u)                                 + 2 * alphaLearnRate         * (err                    * dev_u - regAlpha * alpha.get(u)));
    user_bias.set(u, user_bias.get(u)                         + 2 * userBiasLearnRate      * (err - regU             * user_bias.get(u)));
    user_bias_by_day.set(u, day, user_bias_by_day.get(u, day) + 2 * userBiasByDayLearnRate * (err - regUserBiasByDay * user_bias_by_day.get(u, day)));

    // Update item biases and user scalings
    double b_i  = item_bias.get(i);
    double b_ib = item_bias_by_time_bin.get(i, bin);
    double c_u  = user_scaling.get(u);
    double c_ud = user_scaling_by_day.get(u, day);
    item_bias.set(i,                   item_bias.get(i)                 + 2 * itemBiasLearnRate          * (err * (c_u + c_ud) - regI                 * b_i));
    item_bias_by_time_bin.set(i, bin, item_bias_by_time_bin.get(i, bin) + 2 * itemBiasByTimeBinLearnRate * (err * (c_u + c_ud) - regItemBiasByTimeBin * b_ib));
    user_scaling.set(u,               user_scaling.get(u)               + 2 * userScalingLearnRate       * (err * (b_i + b_ib) - regUserScaling       * (c_u - 1)));
    user_scaling_by_day.set(u, day,   user_scaling_by_day.get(u, day)   + 2 * userScalingByDayLearnRate  * (err * (b_i + b_ib) - regUserScalingByDay  * c_ud));
  }

  /**
   */
  public double predict(int user_id, int item_id) {
    double result = global_average;
    if (user_id <= maxUserID)
      result += user_bias.get(user_id);
    if (item_id <= maxItemID)
      result += item_bias.get(item_id);

    return result;
  }

  /**
   * Predict the specified user_id, item_id, day and bin.
   * 
   * Assumes user and item IDs are valid.
   * 
   * <param name='user_id'>the user ID
   * <param name='item_id'>the item ID
   * <param name='day'>the day of the rating
   * <param name='bin'>the day bin of the rating
   */
  protected double predict(int user_id, int item_id, int day, int bin) {
    double result = global_average;

    double dev_u = Math.signum(day - userMeanDay.get(user_id)) * Math.pow(Math.abs(day - userMeanDay.get(user_id)), beta);
    result += user_bias.get(user_id) + alpha.get(user_id) * dev_u + user_bias_by_day.get(user_id, day);    
    result += (item_bias.get(item_id) + item_bias_by_time_bin.get(item_id, bin)) ;  //  * (user_scaling.get(user_id) + user_scaling_by_day.get(user_id, day));

    return result;
  }

  /**
   * 
   */
  @SuppressWarnings("deprecation")
  public double predict(int user_id, int item_id, Date time) {
    int day = relativeDay(time);
    int bin = day / binSize;

    // Use latest day bin if the rating time is after the training time period
    if (bin >= item_bias_by_time_bin.numberOfColumns())
      bin = item_bias_by_time_bin.numberOfColumns() - 1;

    double result = global_average;
    if (user_id <= maxUserID) {
      double dev_u = Math.signum(day - userMeanDay.get(user_id)) * Math.pow(Math.abs(day - userMeanDay.get(user_id)), beta);
      result += user_bias.get(user_id) + alpha.get(user_id) * dev_u;
      if (day <= timed_ratings.latestTime().getDay())
        result += user_bias_by_day.get(user_id, day);
    }

    if (item_id <= maxItemID && user_id > maxUserID)
      result += item_bias.get(item_id) + item_bias_by_time_bin.get(item_id, bin);
    if (item_id <= maxItemID && user_id <= maxUserID && day < user_scaling_by_day.numberOfColumns())
      result += (item_bias.get(item_id) + item_bias_by_time_bin.get(item_id, bin)) * (user_scaling.get(user_id) + user_scaling_by_day.get(user_id, day));

    return result;
  }

  /**
   * 
   */
  public double computeLoss() {
    double loss =
        2 * Ratings.evaluate(this, ratings).get("RMSE")
            + regU                 * Math.pow(VectorExtensions.euclideanNorm(user_bias),                 2)
            + regI                 * Math.pow(VectorExtensions.euclideanNorm(item_bias),                 2)
            + regAlpha             * Math.pow(VectorExtensions.euclideanNorm(alpha),                     2)
            + regUserBiasByDay     * Math.pow(SparseMatrixExtensions.frobeniusNorm(user_bias_by_day),    2)
            + regItemBiasByTimeBin * Math.pow(MatrixExtensions.frobeniusNorm(item_bias_by_time_bin)    , 2)
            + regUserScalingByDay  * Math.pow(SparseMatrixExtensions.frobeniusNorm(user_scaling_by_day), 2);

    double user_scaling_reg_term = 0;
    for (double e : user_scaling)
      user_scaling_reg_term += Math.pow(1 - e, 2);

    user_scaling_reg_term = user_scaling_reg_term * regUserScaling;
    loss += user_scaling_reg_term;
    return loss;
  }

  /**
   * 
   */
  public String toString() {
    return "TimeAwareBaseline"
        + " num_iter="                         + numIter
        + " bin_size="                         + binSize
        + " beta="                             + beta
        + " user_bias_learn_rate="             + userBiasLearnRate
        + " item_bias_learn_rate="             + itemBiasLearnRate
        + " alpha_learn_rate="                 + alphaLearnRate
        + " item_bias_by_time_bin_learn_rate=" + itemBiasByTimeBinLearnRate
        + " user_bias_by_day_learn_rate="      + userBiasByDayLearnRate
        + " user_scaling_learn_rate="          + userScalingLearnRate
        + " user_scaling_by_day_learn_rate="   + userScalingByDayLearnRate
        + " reg_u="                            + regU
        + " reg_i="                            + regI
        + " reg_alpha="                        + regAlpha
        + " reg_item_bias_by_time_bin="        + regItemBiasByTimeBin
        + " reg_user_bias_by_day="             + regUserBiasByDay
        + " reg_user_scaling="                 + regUserScaling
        + " reg_user_scaling_by_day="          + regUserScalingByDay;
  }

  @Override
  public void saveModel(String filename) throws IOException {
    throw new UnsupportedOperationException(); 
  }

  @Override
  public void saveModel(PrintWriter writer) throws IOException {
    throw new UnsupportedOperationException(); 
  }
  
  @Override
  public void loadModel(String filename) throws IOException {
    throw new UnsupportedOperationException(); 
  }

  @Override
  public void loadModel(BufferedReader reader) throws IOException {
    throw new UnsupportedOperationException(); 
  }
  
}
