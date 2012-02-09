//Copyright (C) 2010, 2011 Zeno Gantner, Chris
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
import java.util.List;

/**
 * Per-user chronological split for rating prediction.
 * 
 * Chronological splits (splits according to the time of the rating) treat all ratings before
 * a certain time as training ratings, and the ones after that time as test/validation ratings.
 *
 * Here, the split date may differ from user to user.
 * In the constructor, you can either specify which part (ratio) or how many of a user's rating
 * are supposed to be used for validation.
 *
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class RatingsPerUserChronologicalSplit implements ISplit<ITimedRatings> {

  /**
   * 
   */
  public int numberOfFolds() {
    return 1;
  }

  /**
   * 
   */
  public List<ITimedRatings> train; 

  /**
   * 
   */
  public List<ITimedRatings> test;

  @Override
  public List<ITimedRatings> train() {
    return train;
  }

  @Override
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
   * @param ratio the ratio of ratings to use for validation (per user)
   */
  public RatingsPerUserChronologicalSplit(ITimedRatings ratings, double ratio) {

    if (ratio <= 0 && ratio >= 1)
      throw new IllegalArgumentException("ratio must be between 0 and 1");

    IntList train_indices = new IntArrayList();
    IntList test_indices  = new IntArrayList();

    // For every user, perform the split and assign the ratings accordingly
    for (int u : ratings.allUsers()) {

      List<Integer> chronological_index = ratings.byUser().get(u);
      Collections.sort(chronological_index, ratings);

      int num_test_ratings  = (int) Math.round(ratings.byUser().get(u).size() * ratio);
      int num_train_ratings = ratings.byUser().get(u).size() - num_test_ratings;

      // Assign indices to training part
      for (int i = 0; i < num_train_ratings; i++)
        train_indices.add(chronological_index.get(i));

      // Assign indices to test part
      for (int i = 0; i < num_test_ratings; i++)
        test_indices.add(chronological_index.get(i + num_train_ratings));
    }

    // Create split data structures
    train.add(new TimedRatingsProxy(ratings, train_indices));
    test.add(new TimedRatingsProxy(ratings, test_indices));
  }

  /**
   * Create a chronological split of rating prediction data.
   * 
   * If ratings have exactly the same date and time, and they are close to the threshold between
   * train and test, there is no guaranteed order between them (ties are broken according to how the
   * sorting procedure sorts the ratings).
   * 
   * @param ratings the dataset
   * @param num_test_ratings_per_user the number of test ratings (per user)
   */
  public RatingsPerUserChronologicalSplit(ITimedRatings ratings, int num_test_ratings_per_user) {
    IntList train_indices = new IntArrayList();
    IntList test_indices  = new IntArrayList();

    // For every user, perform the split and assign the ratings accordingly
    for (int u : ratings.allUsers()) {

      List<Integer> chronological_index = ratings.byUser().get(u);
      Collections.sort(chronological_index, ratings);

      int num_test_ratings  = Math.min(num_test_ratings_per_user, ratings.byUser().get(u).size());
      int num_train_ratings = ratings.byUser().get(u).size() - num_test_ratings;

      // Assign indices to training part
      for (int i = 0; i < num_train_ratings; i++)
        train_indices.add(chronological_index.get(i));

      // Assign indices to test part
      for (int i = 0; i < num_test_ratings; i++)
        test_indices.add(chronological_index.get(i + num_train_ratings));
    }

    // Create split data structures
    train.add(new TimedRatingsProxy(ratings, train_indices));
    test.add(new TimedRatingsProxy(ratings, test_indices));
  }

}

