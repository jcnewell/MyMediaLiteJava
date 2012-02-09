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

package org.mymedialite.hyperparameter;

import java.util.List;

import org.mymedialite.IRecommender;

/*
 * Delegate type for the evaluation task: a closure that returns a dictionary containing the results.
 * @version 2.03
 */
//public delegate HashMap<String, Double> eval_task(IRecommender recommender);

/**
 * Interface for classes that perform hyper-parameter search.
 */
public interface IHyperParameterSearch {

  // Configuration properties

  /**
   * The delegate used to compute.
   */
  // TODO define interface?
  void evalJob();

  /**
   * The recommender to find the hyperparameters for.
   */
  IRecommender recommender();

  /**
   * List of (hyper-)parameters to optimize.
   */
  List<String> parameters();

  /**
   * The evaluation measure to optimize.
   */
  String measure();

  /**
   * true if evaluation measure is to be maximized, false if it is to be minimized.
   */
  boolean maximize();

  // Status properties

  /**
   * Size of the current epoch of the hyper-parameter search.
   */
  int epochSize();

  /**
   * The number of steps computed so far in this hyper-parameter search.
   */
  int numberOfStepsComputed();

  /**
   * The best result so far.
   */
  double bestResult();

  /**
   * The (hyper-)parameter values of the best result so far.
   */
  List<Object> bestParameterValues();

  // Methods

  /**
   * Compute the next step in the current epoch.
   */
  void computeNextStep();

  /**
   * Complete the current epoch.
   */
  void computeNextEpoch();

}

