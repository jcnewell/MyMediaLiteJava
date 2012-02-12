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

import java.util.Date;

import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.datatype.Pair;
import org.mymedialite.datatype.SparseMatrix;
import org.mymedialite.datatype.SparseMatrixExtensions;
import org.mymedialite.datatype.VectorExtensions;

/**
 * Time-aware bias model with frequencies.
 * 
 * Model described in equation (11) of BellKor Grand Prize documentation for the Netflix Prize (see below).
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
public class TimeAwareBaselineWithFrequencies extends TimeAwareBaseline {

  // Additional parameters
  SparseMatrix<Double> item_bias_at_frequency;

  // Additional hyper-parameters

  /**
   * logarithmic base for the frequency counts.
   */
  public double FrequencyLogBase;

  /**
   * Regularization constant for b_{i, f_{ui}}.
   */
  public double RegItemBiasAtFrequency;

  /**
   * Learn rate for b_{i, f_{ui}}.
   */
  public double ItemBiasAtFrequencyLearnRate;

  // Additional helper data structures
  SparseMatrix<Integer> log_frequency_by_day;

  /**
   * Default constructor.
   */
  public TimeAwareBaselineWithFrequencies() {
    numIter = 40;

    FrequencyLogBase = 6.76;

    binSize = 70;

    beta = 0.4;

    userBiasLearnRate = 0.00267;
    itemBiasLearnRate = 0.000488;
    alphaLearnRate = 0.00000311;
    itemBiasByTimeBinLearnRate = 0.00000115;
    userBiasByDayLearnRate = 0.000257;
    userScalingLearnRate = 0.00564;
    userScalingByDayLearnRate = 0.00103;
    ItemBiasAtFrequencyLearnRate = 0.00236;

    regU = 0.0255;
    regI = 0.0255;
    regAlpha = 3.95;
    regItemBiasByTimeBin = 0.0929;
    regUserBiasByDay = 0.00231;
    regUserScaling = 0.0476;
    regUserScalingByDay = 0.019;
    RegItemBiasAtFrequency = 0.000000011;
  }

  /**
   */
  public void train() {
    // TODO check for better way to do this.
    int number_of_days = (int)((timed_ratings.latestTime().getTime() - timed_ratings.earliestTime().getTime()) / 3600000);

    // Compute log rating frequencies
    log_frequency_by_day = new SparseMatrix<Integer>(maxUserID + 1, number_of_days, 0);

    // First count the frequencies ...
    for (int i = 0; i < timed_ratings.size(); i++) {
      int day = relativeDay(timed_ratings.times().get(i));
      log_frequency_by_day.set(timed_ratings.users().get(i), day, log_frequency_by_day.get(timed_ratings.users().get(i), day) + 1);
    }

    // ... then apply (rounded) logarithm
    for (Pair<Integer, Integer> index_pair : log_frequency_by_day.nonEmptyEntryIDs())
      log_frequency_by_day.set(index_pair.first, index_pair.second, 
          (int) Math.ceil(Math.log(log_frequency_by_day.get(index_pair.first, index_pair.second)) / Math.log(FrequencyLogBase)));

    super.train();
  }

  /**
   */
  protected void initModel() {
    super.initModel();
    item_bias_at_frequency = new SparseMatrix<Double>(maxItemID + 1, SparseMatrixExtensions.maxInteger(log_frequency_by_day), 0.0);
  }

  /**
   * 
   */
  protected void updateParameters(int u, int i, int day, int bin, double err) {
    super.updateParameters(u, i, day, bin, err);

    // Update additional bias
    int f = log_frequency_by_day.get(u, day);
    double b_i_f_ui  = item_bias_at_frequency.get(i, f);
    item_bias_at_frequency.set(i, f, item_bias_at_frequency.get(i, f) + 2 * ItemBiasAtFrequencyLearnRate * (err * b_i_f_ui - RegItemBiasAtFrequency * b_i_f_ui));
  }

  /**
   * 
   */
  protected double predict(int user_id, int item_id, int day, int bin) {
    double result = super.predict(user_id, item_id, day, bin);
    // TODO should this be relative day?
    if (day <= timed_ratings.latestTime().getDay())
      result += item_bias_at_frequency.get(item_id, log_frequency_by_day.get(user_id, day));

      return result;
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id, Date time) {
    double result = super.predict(user_id, item_id, time);
    int day = relativeDay(time);
    // TODO should this be relative day?
    if (day <= timed_ratings.latestTime().getDay())
      result += item_bias_at_frequency.get(item_id, log_frequency_by_day.get(user_id, day));

    return result;
  }

  /**
   * 
   */
  public double computeLoss() {
    return super.computeLoss()
        + RegItemBiasAtFrequency * Math.pow(SparseMatrixExtensions.frobeniusNorm(item_bias_at_frequency), 2);
  }

/**
 * 
 */
public String toString() {
  return "TimeAwareBaseline "
      + "num_iter="                         + numIter
      + "bin_size="                         + binSize
      + "beta="                             + beta
      + "user_bias_learn_rate="             + userBiasLearnRate
      + "item_bias_learn_rate="             + itemBiasLearnRate
      + "alpha_learn_rate="                 + alphaLearnRate
      + "item_bias_by_time_bin_learn_rate=" + itemBiasByTimeBinLearnRate
      + "user_bias_by_day_learn_rate="      + userBiasByDayLearnRate
      + "user_scaling_learn_rate="          + userScalingLearnRate
      + "user_scaling_by_day_learn_rate="   + userScalingByDayLearnRate
      + "reg_u="                            + regU
      + "reg_i="                            + regI
      + "reg_alpha="                        + regAlpha
      + "reg_item_bias_by_time_bin="        + regItemBiasByTimeBin
      + "reg_user_bias_by_day="             + regUserBiasByDay
      + "reg_user_scaling="                 + regUserScaling
      + "reg_user_scaling_by_day="          + regUserScalingByDay
      + "frequencyLogBase"                  + FrequencyLogBase
      + "itemBiasAtFrequencyLearnRate"      + ItemBiasAtFrequencyLearnRate
      + "regItemBiasAtFrequency"            + RegItemBiasAtFrequency;
  }

}
