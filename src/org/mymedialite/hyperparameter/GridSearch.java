// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.hyperparameter;

import javax.rmi.CORBA.Util;

import org.mymedialite.data.IRatings;
import org.mymedialite.data.ISplit;
import org.mymedialite.data.RatingCrossValidationSplit;
import org.mymedialite.data.Ratings;
import org.mymedialite.eval.RatingsCrossValidation;
import org.mymedialite.ratingprediction.RatingPredictor;
import org.mymedialite.util.Recommender;

/**
 * Grid search for finding suitable hyperparameters.
 * @version 2.03
 */
public class GridSearch {

  // Prevent instantiation.
  private GridSearch() {}

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure (1D).
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hyperparameter_name the name of the hyperparameter to optimize
   * @param hyperparameter_values the values of the hyperparameter to try out
   * @param recommender the recommender
   * @param split the dataset split to use
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimum(
      String evaluation_measure,
      String hyperparameter_name,
      double[] hyperparameter_values,
      RatingPredictor recommender,
      ISplit<IRatings> split) throws Exception {

    double min_result = Double.MAX_VALUE;
    int min_i = -1;

    for (int i = 0; i < hyperparameter_values.length; i++) {
      Recommender.setProperty(recommender, hyperparameter_name, Double.toString(hyperparameter_values[i]));
      double result = RatingsCrossValidation.doCrossValidation(recommender, split, null, null).get(evaluation_measure);

      if (result < min_result) {
        min_i = i;
        min_result = result;
      }
    }
    Recommender.setProperty(recommender, hyperparameter_name, Double.toString(hyperparameter_values[min_i]));
    return min_result;
  }

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure (2D).
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hp_name1 the name of the first hyperparameter to optimize
   * @param hp_values1 the values of the first hyperparameter to try out
   * @param hp_name2 the name of the second hyperparameter to optimize
   * @param hp_values2 the values of the second hyperparameter to try out
   * @param recommender the recommender
   * @param split the dataset split to use
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimum(
      String evaluation_measure,
      String hp_name1, String hp_name2,
      double[] hp_values1,
      double [] hp_values2,
      RatingPredictor recommender,
      ISplit<IRatings> split) throws Exception {

    double min_result = Double.MAX_VALUE;
    int min_i = -1;
    int min_j = -1;

    for (int i = 0; i < hp_values1.length; i++)
      for (int j = 0; j < hp_values2.length; j++) {
        Recommender.setProperty(recommender, hp_name1, Double.toString(hp_values1[i]));
        Recommender.setProperty(recommender, hp_name2, Double.toString(hp_values2[j]));

        System.err.println("reg_u=" + Double.toString(hp_values1[i]) + "reg_i=" + Double.toString(hp_values2[j])); // TODO this instanceof not generic
        double result = RatingsCrossValidation.doCrossValidation(recommender, split, null, null).get(evaluation_measure);
        if (result < min_result) {
          min_i = i;
          min_j = j;
          min_result = result;
        }
      }

    // Set to best hyperparameter values
    Recommender.setProperty(recommender, hp_name1, Double.toString(hp_values1[min_i]));
    Recommender.setProperty(recommender, hp_name2, Double.toString(hp_values2[min_j]));

    return min_result;
  }

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure (2D).
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hp_name1 the name of the first hyperparameter to optimize
   * @param hp_values1 the logarithm values of the first hyperparameter to try out
   * @param hp_name2 the name of the second hyperparameter to optimize
   * @param hp_values2 the logarithm values of the second hyperparameter to try out
   * @param basis the basis to use for the logarithms
   * @param recommender the recommender
   * @param split the dataset split to use
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimumExponential(
      String evaluation_measure,
      String hp_name1,
      String hp_name2,
      double[] hp_values1,
      double[] hp_values2,
      double basis,
      RatingPredictor recommender,
      ISplit<IRatings> split) throws Exception {

    double[] new_hp_values1 = new double[hp_values1.length];
    double[] new_hp_values2 = new double[hp_values2.length];

    for (int i = 0; i < hp_values1.length; i++)
      new_hp_values1[i] = Math.pow(basis, hp_values1[i]);

    for (int i = 0; i < hp_values2.length; i++)
      new_hp_values2[i] = Math.pow(basis, hp_values2[i]);

    return findMinimum(evaluation_measure, hp_name1, hp_name2, new_hp_values1, new_hp_values2, recommender, split);
  }

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure (1D).
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hp_name the name of the hyperparameter to optimize
   * @param hp_values the logarithms of the values of the hyperparameter to try out
   * @param basis the basis to use for the logarithms
   * @param recommender the recommender
   * @param split the dataset split to use
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimumExponential(
      String evaluation_measure,
      String hp_name,
      double[] hp_values,
      double basis,
      RatingPredictor recommender,
      ISplit<IRatings> split) throws Exception {

    double[] new_hp_values = new double[hp_values.length];

    for (int i = 0; i < hp_values.length; i++)
      new_hp_values[i] = Math.pow(basis, hp_values[i]);

    return findMinimum(evaluation_measure, hp_name, new_hp_values, recommender, split);
  }

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure using k-fold cross-validation.
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hyperparameter_name the name of the hyperparameter to optimize
   * @param hyperparameter_values the values of the hyperparameter to try out
   * @param recommender the recommender
   * @param k the number of folds to be used for cross-validation
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimum(
      String evaluation_measure,
      String hyperparameter_name,
      double[] hyperparameter_values,
      RatingPredictor recommender,
      int k) throws Exception {

    IRatings data = recommender.getRatings();
    RatingCrossValidationSplit split = new RatingCrossValidationSplit(data, k);
    double result = findMinimum(evaluation_measure, hyperparameter_name, hyperparameter_values, recommender, split);
    recommender.setRatings(data);
    return result;
  }

}

