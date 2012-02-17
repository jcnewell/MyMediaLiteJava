//Copyright (C) 2010, 2011 Zeno Gantner
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
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Chronological split for rating prediction.
 * 
 * Chronological splits (splits according to the time of the rating) treat all ratings before
 * a certain time as training ratings, and the ones after that time as test/validation ratings.
 * This kind of split is the most realistic kind of split, because in a real application
 * you also can only use past data to make predictions for the future.
 *
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class RatingsChronologicalSplit implements ISplit<ITimedRatings> {

  private List<ITimedRatings> train;
  private List<ITimedRatings> test;
  
  /**
   */
  public int numberOfFolds() { 
    return 1; 
  }

  /**
   */
  public List<ITimedRatings> train() {
    return train;
  }
  
  /**
   */
  public List<ITimedRatings> test() { 
    return test;
   }

  /**
   * Create a chronological split of rating prediction data.
   * 
   * If ratings have exactly the same date and time, and they are close to the threshold between
   * train and test, there is no guaranteed order between them (ties are broken according to how the
   * sorting procedure sorts the ratings).
   * 
   * @param ratings the dataset
   * @param ratio the ratio of ratings to use for validation
   */
  public RatingsChronologicalSplit(ITimedRatings ratings, double ratio) {
    
    if (ratio <= 0 && ratio >= 1)
      throw new IllegalArgumentException("ratio must be between 0 and 1");

    List<Integer> chronological_index = new ArrayList<Integer>();
    for(int i=0; i < ratings.size(); i++) chronological_index.add(i);
    
    Collections.sort(chronological_index, ratings);
    int num_test_ratings  = (int) Math.round(ratings.size() * ratio);
    int num_train_ratings = ratings.size() - num_test_ratings;
    
    // Assign indices to training part

    IntList train_indices = new IntArrayList(num_train_ratings);
    for (int i = 0; i < num_train_ratings; i++)
      train_indices.add(chronological_index.get(i));

    // Assign indices to test part
    IntList test_indices  = new IntArrayList(num_test_ratings);
    for (int i = 0; i < num_test_ratings; i++)
        test_indices.add(chronological_index.get(i + num_train_ratings));
    
    // Create split data structures
    train = new ArrayList<ITimedRatings>();
    train.add(new TimedRatingsProxy(ratings, train_indices));
    test = new ArrayList<ITimedRatings>(); 
    test.add(new TimedRatingsProxy(ratings, test_indices));
  }

  /**
   * Create a chronological split of rating prediction data.
   * @param ratings the dataset
   * @param split_time 
   * the point in time to use for splitting the data set;
   * everything from that point on will be used for validation
   * 
   */
  public RatingsChronologicalSplit(ITimedRatings ratings, Date split_time) {
    
    if (split_time.before(ratings.earliestTime()))
      throw new IllegalArgumentException("split_time must be after the earliest event : the data set");
    if (split_time.after(ratings.latestTime()))
      throw new IllegalArgumentException("split_time must be before the latest event : the data set");

    // Determine size of split
    int count = 0;
    for (int i = 0; i < ratings.size(); i++)
      if (ratings.times().get(i).before(split_time))    
        count++;
      
    // Create indices
    IntList train_indices = new IntArrayList(count);
    IntList test_indices  = new IntArrayList(ratings.size() - count);

    // Assign ratings to where they belong
    int trainCount = 0;
    int testCount = 0;    
    for (int i = 0; i < ratings.size(); i++)
      if (ratings.times().get(i).before(split_time))
        train_indices.set(trainCount++, i);
      else
        test_indices.set(testCount++, i);

    // Create split data structures
    train = new ArrayList<ITimedRatings>();
    train.add(new TimedRatingsProxy(ratings, train_indices));
    test = new ArrayList<ITimedRatings>(); 
    test.add(new TimedRatingsProxy(ratings, test_indices));
  }

}

