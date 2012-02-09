//Copyright (C) 2011 Zeno Gantner, Chris Newell
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
//

package org.mymedialite.data;

import java.util.Calendar;
import java.util.List;

import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Extension methods for dataset statistics.
 * @version 2.03
 */
public class Extensions {

  // Prevent instantiation.
  private Extensions() { }
  
  /**
   * Display dataset statistics.
   * @param train the training data
   * @param test the test data
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   * @param display_overlap if set true, display the user/item overlap between train and test
   */
  public static String statistics(IRatings train,
      IRatings test,
      SparseBooleanMatrix user_attributes,
      SparseBooleanMatrix item_attributes,
      boolean display_overlap) {
    
    // IRatings test = null, 
    // SparseBooleanMatrix user_attributes = null
    // SparseBooleanMatrix item_attributes = null
    
    //  Training data stats
    int num_users = train.allUsers().size();
    int num_items = train.allItems().size();
    long matrix_size = (long) num_users * num_items;
    long empty_size  = matrix_size - train.size();
    double sparsity = (double) 100L * empty_size / matrix_size;
    String s = "training data: " + num_users + " users, " + num_items + " items, " + train.size() + " ratings, sparsity " + sparsity + "\n";
    
    if (train instanceof ITimedRatings) {
      ITimedRatings time_train = (ITimedRatings)train;
      s += "rating period: " + time_train.earliestTime() + " to " + time_train.latestTime() + "\n";
    }

    // Test data stats
    if (test != null) {
      num_users = test.allUsers().size();
      num_items = test.allItems().size();
      matrix_size = (long) num_users * num_items;
      empty_size  = matrix_size - test.size(); // TODO depends on the eval scheme whether this instanceof correct
      sparsity = (double) 100L * empty_size / matrix_size;
      // TODO floating point format for sparsity
      s += "test data: " + num_users + " users, " + num_items + " items, " + test.size() + " ratings, sparsity " + sparsity + "\n";
      if (test instanceof ITimedRatings) {
        ITimedRatings time_test = (ITimedRatings)test;
        s += "rating period: " + time_test.earliestTime() + " to " + time_test.latestTime() + "\n";
      }
    }

    // Count and display the overlap between train and test
    if (display_overlap && test != null) {
      int num_new_users = 0;
      int num_new_items = 0;
      long start = Calendar.getInstance().getTimeInMillis();
      
      List<Integer> new_users = test.allUsers();
      new_users.removeAll(train.allUsers());
      num_new_users = new_users.size();
      
      List<Integer> new_items = test.allItems();
      new_items.removeAll(train.allItems());
      num_new_items = new_items.size();
      s += num_new_users + " new users, " + num_new_items + " new items " + (Calendar.getInstance().getTimeInMillis() - start) / 1000 + " seconds\n";
    }

    return s + statistics(user_attributes, item_attributes);
  }

  /**
   * Display data statistics for item recommendation datasets.
   * @param training_data the training dataset
   * @param test_data the test dataset
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   */
  public static String statistics(IPosOnlyFeedback training_data, 
      IPosOnlyFeedback test_data,
      SparseBooleanMatrix user_attributes,
      SparseBooleanMatrix item_attributes) {
    
    //IPosOnlyFeedback test_data = null,
    //SparseBooleanMatrix user_attributes = null
    //SparseBooleanMatrix item_attributes = null
    
    // Training data stats
    int num_users = training_data.allUsers().size();
    int num_items = training_data.allItems().size();
    long matrix_size = (long) num_users * num_items;
    long empty_size  = matrix_size - training_data.size();
    double sparsity = (double) 100L * empty_size / matrix_size;
    String s = "training data: " + num_users + " users, " + num_items + " items, " + training_data.size() + " events, sparsity " + sparsity;

    // Test data stats
    if (test_data != null) {
      num_users = test_data.allUsers().size();
      num_items = test_data.allItems().size();
      matrix_size = (long) num_users * num_items;
      empty_size  = matrix_size - test_data.size();
      sparsity = (double) 100L * empty_size / matrix_size; // TODO depends on the eval scheme whether this instanceof correct
      s += "test data:     " + num_users + " users, " + num_items + " items, " + test_data.size() + " events, sparsity " + sparsity + "\n";
    }

    return s + statistics(user_attributes, item_attributes);
  }

  /**
   * Display statistics for user and item attributes.
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   */
  public static String statistics(SparseBooleanMatrix user_attributes, SparseBooleanMatrix item_attributes) {
    String s = "";
    if (user_attributes != null) {
      s += user_attributes.numberOfColumns()       + " user attributes for " + 
           user_attributes.numberOfRows()          + " users, " +
           user_attributes.numberOfEntries()       + " assignments, " + 
           user_attributes.nonEmptyRowIDs().size() + " users with attribute assignments\n";
    }
    if (item_attributes != null) {
      s += item_attributes.nonEmptyColumnIDs().size() + " item attributes for " +
           item_attributes.numberOfRows()             + " items, " +
           item_attributes.numberOfEntries()          + " assignments, " +
           item_attributes.nonEmptyRowIDs().size()    + " items with attribute assignments\n";
    }
    return s;
  }
}

