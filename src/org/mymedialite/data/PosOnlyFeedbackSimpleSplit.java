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

package org.mymedialite.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * simple split for item prediction from implicit feedback.
 * 
 * The dataset must not be modified after the split - this would lead to undefined behavior.
 * @version 2.03
 */
public class PosOnlyFeedbackSimpleSplit<T extends IPosOnlyFeedback> implements ISplit<IPosOnlyFeedback> {
  
  private List<IPosOnlyFeedback> train;
  private List<IPosOnlyFeedback> test;
  
  /**
   * 
   */
  public int numberOfFolds() {
    return 1;
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
   * Create a simple split of positive-only item prediction data.
   * @param feedback the dataset
   * @param ratio the ratio of positive events to use for validation
   */
  public PosOnlyFeedbackSimpleSplit(IPosOnlyFeedback feedback, double ratio, T train, T test) {
    if (ratio <= 0) throw new IllegalArgumentException("ratio must be greater than 0");

    // assign indices to training or validation part
    Random random = org.mymedialite.util.Random.getInstance();
    for (int index : feedback.randomIndex())
      if (random.nextDouble() < ratio)
        test.add(feedback.users().get(index), feedback.items().get(index));
      else
        train.add(feedback.users().get(index), feedback.items().get(index));

    this.train = new ArrayList<IPosOnlyFeedback>();
    this.train.add(train);
    this.test  = new ArrayList<IPosOnlyFeedback>();
    this.test.add(test);
  }
}
