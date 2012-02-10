//Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.hyperparameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.ISplit;
import org.mymedialite.data.RatingsSimpleSplit;
import org.mymedialite.eval.Ratings;
import org.mymedialite.eval.RatingsCrossValidation;
import org.mymedialite.ratingprediction.BiasedMatrixFactorization;
import org.mymedialite.ratingprediction.MatrixFactorization;
import org.mymedialite.ratingprediction.RatingPredictor;
import org.mymedialite.ratingprediction.UserItemBaseline;
import org.mymedialite.util.Recommender;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;
import cern.jet.math.PlusMult;

/**
 * Nealder-Mead algorithm for finding suitable hyperparameters.
 * @version 2.03
 */
public class NelderMead {
  
  // Prevent instantiation.
  private NelderMead() {}
  
  // TODO avoid negative values e.g. for regularization ...

  // TODO make configurable
  static double alpha = 1.0;
  static double gamma = 2.0;
  static double rho = 0.5;
  static double sigma = 0.5;
  static double num_it = 50;
  static double split_ratio = 0.2;

  static String createConfigString(List<String> hp_names, double[] hp_values) {
    String hp_String = "";
    for (int i = 0; i < hp_names.size(); i++)
      hp_String += " " + hp_names.get(i) + "=" + hp_values[i];

    return hp_String;
  }

  static double run(RatingPredictor recommender, ISplit<IRatings> split, String hp_String, String evaluation_measure) throws Exception {
    Recommender.configure(recommender, hp_String);

    double result = RatingsCrossValidation.doCrossValidation(recommender, split, null, null).get(evaluation_measure);
    System.err.println("Nelder-Mead: " + hp_String + ": " + result);
    return result;
  }

  static DoubleMatrix1D computeCenter(Map<String, Double> results, Map<String, DoubleMatrix1D> hp_values) {
    if (hp_values.size() == 0)
      throw new IllegalArgumentException("need at least one vector to build center");

    DoubleMatrix1D center = new DenseDoubleMatrix1D(hp_values.values().iterator().next().size());
    for (String key : results.keySet())
      center.assign(hp_values.get(key), Functions.plus);
   
    // TODO check this is right - dividing by size - 1 instead of size?
    center = new DenseDoubleMatrix1D(center.size()).assign(center, PlusMult.plusDiv(hp_values.size() - 1));  
    return center;
  }

  /**
   * Find best hyperparameter (according to an error measure) using Nelder-Mead search.
   * @param error_measure an error measure (lower is better)
   * @param recommender a rating predictor (will be set to best hyperparameter combination)
   * @return the estimated error of the best hyperparameter combination
   * @throws Exception 
   */
  public static double findMinimum(String error_measure, RatingPredictor recommender) throws Exception {
    ISplit<IRatings> split = new RatingsSimpleSplit(recommender.getRatings(), split_ratio);
    //ISplit<IRatings> split = new RatingCrossValidationSplit(recommender.getRatings(), 5);

    List<String> hp_names;
    List<DoubleMatrix1D> initial_hp_values;

    //TODO manage this via reflection?
    if (recommender instanceof UserItemBaseline) {
      hp_names = Arrays.asList(new String[] { "reg_u", "reg_i" } );
      initial_hp_values = new ArrayList<DoubleMatrix1D>();
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 25, 10 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 10, 25 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] {  2,  5 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] {  5,  2 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] {  1,  4 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] {  4,  1 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] {  3,  3 } ));

    } else if (recommender instanceof BiasedMatrixFactorization) {
      hp_names = Arrays.asList(new String[] { "regularization", "bias_reg" } );
      initial_hp_values = new ArrayList<DoubleMatrix1D>();
      // TODO reg_u and reg_i (in a second step?)
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.1,     0      } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.01,    0      } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.0001,  0      } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.00001, 0      } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.1,     0.0001 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.01,    0.0001 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.0001,  0.0001 } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.00001, 0.0001 } ));

    } else if (recommender instanceof MatrixFactorization) {
      // TODO normal interval search could be more efficient
      hp_names = Arrays.asList(new String[] { "regularization" } );
      initial_hp_values = new ArrayList<DoubleMatrix1D>();
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.1     } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.01    } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.0001  } ));
      initial_hp_values.add( new DenseDoubleMatrix1D( new double[] { 0.00001 } ));

    } else {
      //TODO kNN-based methods
      throw new IllegalArgumentException("Not prepared for type " + recommender.getClass().getName());
    }

    return findMinimum(error_measure, hp_names, initial_hp_values, recommender, split);
  }

  /**
   * Find the the parameters resulting in the minimal results for a given evaluation measure.
   * The recommender will be set to the best parameter value after calling this method.
   * @param evaluation_measure the name of the evaluation measure
   * @param hp_names the names of the hyperparameters to optimize
   * @param initial_hp_values the values of the hyperparameters to try out first
   * @param recommender the recommender
   * @param split the dataset split to use
   * @return the best (lowest) average value for the hyperparameter
   * @throws Exception 
   */
  public static double findMinimum(
      String evaluation_measure,
      List<String> hp_names,
      List<DoubleMatrix1D> initial_hp_values,
      RatingPredictor recommender, // TODO make more general?
      ISplit<IRatings> split) throws Exception {
    
    Map<String, Double> results    = new HashMap<String, Double>();
    Map<String, DoubleMatrix1D> hp_vectors = new HashMap<String, DoubleMatrix1D>();

    // Initialize
    for (DoubleMatrix1D hp_values : initial_hp_values) {
      String hp_String = createConfigString(hp_names, hp_values.toArray());
      results.put(hp_String, run(recommender, split, hp_String, evaluation_measure));
      hp_vectors.put(hp_String, hp_values);
    }

    List<String> keys;
    for (int i = 0; i < num_it; i++) {
      if (results.size() != hp_vectors.size())
        throw new Exception(results.size() + " vs. " + hp_vectors.size());

      keys = new ArrayList<String>(results.keySet());     
      Collections.sort(keys, new ResultsComparator(results));
      
      String min_key = keys.get(0);
      String max_key = keys.get(keys.size() - 1);

      System.err.println("Nelder-Mead: iteration " + i + " (" + results.get(min_key) + ")");

      DoubleMatrix1D worst_vector = hp_vectors.get(max_key);
      Double worst_result = results.get(max_key);
      hp_vectors.remove(max_key);
      results.remove(max_key);

      // Compute center
      DoubleMatrix1D center = computeCenter(results, hp_vectors);

      // Reflection
      //Console.Error.WriteLine("ref");
      //DoubleMatrix1D reflection = center + alpha * (center - worst_vector);
      
      DoubleMatrix1D diffr = center.assign(worst_vector, PlusMult.minusMult(1.0));
      DoubleMatrix1D reflection = center.assign(diffr, PlusMult.plusMult(alpha));
      
      String ref_String = createConfigString(hp_names, reflection.toArray());
      double ref_result = run(recommender, split, ref_String, evaluation_measure);
      if (results.get(min_key) <= ref_result && ref_result < Collections.max(results.values())) {
        results.put(ref_String, ref_result);
        hp_vectors.put(ref_String, reflection);
        continue;
      }

      // Expansion
      if (ref_result < results.get(min_key)) {
        //Console.Error.WriteLine("exp");
        DoubleMatrix1D diffe = center.assign(worst_vector, PlusMult.minusMult(1.0));
        DoubleMatrix1D expansion = center.assign(diffe, PlusMult.plusMult(gamma));
        
        String exp_String = createConfigString(hp_names, expansion.toArray());
        double exp_result = run(recommender, split, exp_String, evaluation_measure);
        if (exp_result < ref_result) {
          results.put(exp_String, exp_result);
          hp_vectors.put(exp_String, expansion);
        } else {
          results.put(ref_String, ref_result);
          hp_vectors.put(ref_String, reflection);
        }
        continue;
      }

      // Contraction
      //Console.Error.WriteLine("con");
      DoubleMatrix1D diffc = center.assign(worst_vector, PlusMult.minusMult(1.0));
      DoubleMatrix1D contraction = center.assign(diffc, PlusMult.plusMult(rho));
      
      String con_String = createConfigString(hp_names, contraction.toArray());
      double con_result = run(recommender, split, con_String, evaluation_measure);
      if (con_result < worst_result) {
        results.put(con_String, con_result);
        hp_vectors.put(con_String, contraction);
        continue;
      }

      // Reduction
      //Console.Error.WriteLine("red");
      DoubleMatrix1D best_vector = hp_vectors.get(min_key);
      Double best_result = results.get(min_key);
      hp_vectors.remove(min_key);
      results.remove(min_key);
      for (String key : new ArrayList<String>(results.keySet())) {
        DoubleMatrix1D diffu = hp_vectors.get(key).assign(best_vector, PlusMult.minusMult(1.0));
        DoubleMatrix1D reduction = hp_vectors.get(key).assign(diffu, PlusMult.plusMult(sigma));
        
        String red_String = createConfigString(hp_names, reduction.toArray());
        double red_result = run(recommender, split, red_String, evaluation_measure);

        // Replace by reduced vector
        results.remove(key);
        hp_vectors.remove(key);
        results.put(red_String,  red_result);
        hp_vectors.put(red_String, reduction);
      }
      results.put(min_key, best_result);
      hp_vectors.put(min_key, best_vector);
      results.put(max_key, worst_result);
      hp_vectors.put(max_key, worst_vector);
    }

    keys = new ArrayList<String>(results.keySet());
    Collections.sort(keys, new ResultsComparator(results));

    // Set to best hyperparameter values
    Recommender.configure(recommender, keys.get(0));
    return results.get(keys.get(0));
  }

  static private class ResultsComparator implements Comparator<String> {
    private Map<String, Double> results;
    
    public ResultsComparator(Map<String, Double> results) {
      this.results = results;
    }

    @Override
    public int compare(String k1, String k2) {
      return results.get(k1).compareTo(results.get(k2));
    }
  }
  
}

