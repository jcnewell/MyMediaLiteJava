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
 * Simple split for rating prediction.
 * 
 * Please note that simple splits are not the best/most realistic way of evaluating
 * recommender system algorithms.
 * In particular, chronological splits (see RatingsChronologicalSplit) are more realistic.
 * 
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class RatingsSimpleSplit implements ISplit<IRatings> {
  
  private List<IRatings> train;
  
  private List<IRatings> test;
  
  /**
   * 
   */
  public int numberOfFolds() { 
    return 1;
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
   * Create a simple split of rating prediction data.
   * @param ratings the dataset
   * @param ratio the ratio of ratings to use for validation
   */
  public RatingsSimpleSplit(IRatings ratings, double ratio) {
    if (ratio <= 0 && ratio >= 1) throw new IllegalArgumentException("ratio must be between 0 and 1");

    List<Integer> random_index = ratings.randomIndex();

    int num_test_ratings = (int) Math.round(ratings.size() * ratio);
    int num_train_ratings = ratings.size() - num_test_ratings;
    
    // Assign indices to training part
    IntList train_indices = new IntArrayList(num_train_ratings);
    for (int i = 0; i < num_train_ratings; i++)
      train_indices.add(i, random_index.get(i));

    // Assign indices to test part
    IntList test_indices  = new IntArrayList(num_test_ratings);
    for (int i = 0; i < num_test_ratings; i++)
      test_indices.add(i, random_index.get(i + num_train_ratings));
    
    train = new ArrayList<IRatings>();
    test  = new ArrayList<IRatings>();
    
    // Create split data structures
    if (ratings instanceof ITimedRatings) {
      train.add(new TimedRatingsProxy((ITimedRatings) ratings, train_indices));
      test.add(new TimedRatingsProxy((ITimedRatings) ratings, test_indices));
    } else {
      train.add(new RatingsProxy(ratings, train_indices));
      test.add(new RatingsProxy(ratings, test_indices));
    }
  }
  
}
