// Copyright (C) 2010 Zeno Gantner 
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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.data;

import java.util.Collections;
import java.util.List;

/**
 * K-fold cross-validation split for item prediction from implicit feedback.
 * 
 * Items with less than k events associated are ignored for testing and always assigned to the training set.
 *
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class PosOnlyFeedbackCrossValidationSplit<T extends IPosOnlyFeedback> implements ISplit<IPosOnlyFeedback> {
  
  private int numberOfFolds;
  private List<IPosOnlyFeedback> train;
  private List<IPosOnlyFeedback> test;
  
  /**
   * 
   */
  public int numberOfFolds() {
    return numberOfFolds;
  }

  /**
   * 
   */
  public List<IPosOnlyFeedback> train() {
    return train;
  }

  /**
   * 
   */
  public List<IPosOnlyFeedback> test() { 
    return test;
  }

  /**
   * Create a k-fold split of positive-only item prediction data.
   * See the class description for details.
   * @param feedback the dataset
   * @param num_folds the number of folds
   */
  public PosOnlyFeedbackCrossValidationSplit(IPosOnlyFeedback feedback, int num_folds, List<IPosOnlyFeedback> train, List<IPosOnlyFeedback> test) {
    if (num_folds < 2)
      throw new IllegalArgumentException("num_folds must be at least 2.");
    
    if(train.size() != num_folds) 
      throw new IllegalArgumentException("train.length must be greater than num_folds");
    
    if(test.size() != num_folds) 
      throw new IllegalArgumentException("tes.length must be greater than num_folds");
    
    this.numberOfFolds = num_folds;
    this.train = train;
    this.test  = test;

    // Assign events to folds
    int pos = 0;
    for (int item_id : feedback.allItems()) {
      List<Integer> item_indices = feedback.byItem().get(item_id);

      if (item_indices.size() < num_folds) {
        for (int index : item_indices)
          for (int f = 0; f < num_folds; f++)
            train.get(f).add(feedback.users().get(index), feedback.items().get(index));

      } else {
        
        // Shuffle list for randomness
        Collections.shuffle(item_indices);

        for (int index : item_indices) {
          int user_id = feedback.users().get(index);
          for (int f = 0; f < num_folds; f++)
            if (pos % num_folds == f)
              test.get(f).add(user_id, item_id);
            else
              train.get(f).add(user_id, item_id);
          pos++;
        }
      }
    }
  }
}
