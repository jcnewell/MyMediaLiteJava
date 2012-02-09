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

package org.mymedialite.data;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;

/**
 * k-fold  cross-validation split for rating prediction.
 * 
 * Please note that k-fold cross-validation is not the best/most realistic way of evaluating
 * recommender system algorithms.
 * In particular, chronological splits (see RatingsChronologicalSplit) are more realistic.
 *
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class RatingCrossValidationSplit implements ISplit<IRatings> {
  
  private int numberOfFolds;
  private List<IRatings> train;
  private List<IRatings> test;
  
  /**
   * 
   */
  public int numberOfFolds() { 
    return numberOfFolds; 
  }

  /**
   * 
   */
  public List<IRatings> train() {
    return train;
  }

  /**
   * 
   */
  public List<IRatings> test() {
    return test;  
  }

  /**
   * Create a k-fold split of rating prediction data.
   * @param ratings the dataset
   * @param num_folds the number of folds
   */
  public RatingCrossValidationSplit(IRatings ratings, int num_folds) {
    
    if (num_folds < 2)
      throw new IllegalArgumentException("num_folds must be at least 2.");

    this.numberOfFolds = num_folds;

    // Randomize
    List<Integer> random_indices = ratings.randomIndex();

    // Create index lists
    List<IntList> train_indices = new ArrayList<IntList>(num_folds);
    List<IntList> test_indices  = new ArrayList<IntList>(num_folds);

    for (int i = 0; i < num_folds; i++) {
      train_indices.set(i, new IntArrayList());
      test_indices.set(i, new IntArrayList());
    }

    // Assign indices to folds
    for (int i : random_indices)
      for (int j = 0; j < num_folds; j++)
        if (j == i % num_folds)
          test_indices.get(j).add(i);
        else
          train_indices.get(j).add(i);

    // Create split data structures
    train = new ArrayList<IRatings>(num_folds);
    test  = new ArrayList<IRatings>(num_folds);
    
    for (int i = 0; i < num_folds; i++)
      if (ratings instanceof ITimedRatings) {
        train.add(new TimedRatingsProxy((ITimedRatings) ratings, train_indices.get(i)));
        test.add(new TimedRatingsProxy((ITimedRatings) ratings, test_indices.get(i)));
      } else {
        train.add(new RatingsProxy(ratings, train_indices.get(i)));
        test.add(new RatingsProxy(ratings, test_indices.get(i)));
      }
  }
  
}
