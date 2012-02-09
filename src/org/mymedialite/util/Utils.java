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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.IRatings;
import org.mymedialite.data.ITimedRatings;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Class containing utility functions.
 * @version 2.03
 */
public class Utils {
  
  // Prevent instantiation.
  private Utils() {}
  
  // TODO
//  /**
//   * Shuffle a list in-place.
//   * 
//   * Fisher-Yates shuffle, see
//   * http://en.wikipedia.org/wiki/Fisher–Yates_shuffle
//   * 
//   */
//  public static void shuffle(List<Object> list) {
//    Random random = org.mymedialite.util.Random.getInstance();
//    for (int i = list.size() - 1; i >= 0; i--) {
//      int r = random.nextInt(i + 1);
//
//      // swap position i with position r
//      Object tmp = list.get(i);
//      list.set(i, list.get(r));
//      list.set(r, tmp);
//    }
//  }

  // TODO
//  /**
//   * Get all types of a namespace.
//   * @param name_space a string describing the namespace
//   * @return an array of Type objects
//   */
//  public static Type[] getTypesInNamespace(String name_space) {
//    var types = new Vector<Type>();
//
//    for (Assembly assembly : AppDomain.CurrentDomain.GetAssemblies())
//      types.AddRange( assembly.GetTypes().Where(t => String.Equals(t.Namespace, name_space, StringComparison.Ordinal)) );
//
//    return types.ToArray();
//  }

  /**
   * Display dataset statistics.
   * @param train the training data
   * @param test the test data
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   * @param display_overlap if set true, display the user/item overlap between train and test
   */
  public static void displayDataStats(
      IRatings train, IRatings test,
      SparseBooleanMatrix user_attributes, SparseBooleanMatrix item_attributes,
      boolean display_overlap) {

    // training data stats
    int num_users = train.allUsers().size();
    int num_items = train.allItems().size();
    long matrix_size = (long) num_users * num_items;
    long empty_size  = matrix_size - train.size();
    double sparsity = (double) 100L * empty_size / matrix_size;
    System.out.println("training data: " + num_users + " users, " + num_items + " items, " + train.size() + " ratings, sparsity " + sparsity);
    if (train instanceof ITimedRatings) {
      ITimedRatings time_train = (ITimedRatings)train;
      System.out.println("rating period: " + time_train.earliestTime() + " to " + time_train.latestTime());
    }

    // test data stats
    if (test != null) {
      num_users = test.allUsers().size();
      num_items = test.allItems().size();
      matrix_size = (long) num_users * num_items;
      empty_size  = matrix_size - test.size(); // TODO depends on the eval scheme whether this is correct
      sparsity = (double) 100L * empty_size / matrix_size;
      System.out.println("test data: " + num_users + " users, " + num_items + " items, " + test.size() + " ratings, sparsity " + sparsity);
      if (test instanceof ITimedRatings) {
        ITimedRatings time_test = (ITimedRatings)test;
        System.out.println("rating period: " + time_test.earliestTime() + " to " + time_test.latestTime());
      }
    }

    // count and display the overlap between train and test
    if (display_overlap && test != null) {
      int num_new_users = 0;
      int num_new_items = 0;
      long start = Calendar.getInstance().getTimeInMillis();
   
      for(int u : test.allUsers())
        if(Arrays.asList(train.allUsers()).contains(u)) num_new_users++;
      
      for(int i : test.allItems())
        if(Arrays.asList(train.allItems()).contains(i)) num_new_items++;
      
      System.out.println(num_new_users + " new users, " + num_new_items + " new items, " + (Calendar.getInstance().getTimeInMillis() - start) / 1000 + " seconds");
    }

    displayAttributeStats(user_attributes, item_attributes);
  }

  /**
   * Display data statistics for item recommendation datasets.
   * @param training_data the training dataset
   * @param test_data the test dataset
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   */
  public static void displayDataStats(
      IPosOnlyFeedback training_data, IPosOnlyFeedback test_data,
      SparseBooleanMatrix user_attributes, SparseBooleanMatrix item_attributes) {

    // training data stats
    int num_users = training_data.allUsers().size();
    int num_items = training_data.allItems().size();
    long matrix_size = (long) num_users * num_items;
    long empty_size  = matrix_size - training_data.size();
    double sparsity = (double) 100L * empty_size / matrix_size;
    System.out.println("training data: " + num_users + " users, " + num_items + " items, " + training_data.size() + " events, sparsity " + sparsity);

    // test data stats
    if (test_data != null) {
      num_users = test_data.allUsers().size();
      num_items = test_data.allItems().size();
      matrix_size = (long) num_users * num_items;
      empty_size  = matrix_size - test_data.size();
      sparsity = (double) 100L * empty_size / matrix_size; // TODO depends on the eval scheme whether this is correct
      System.out.println("test data: " + num_users + " users, " + num_items + " items, " + test_data.size() + " events, sparsity " + sparsity);
    }

    displayAttributeStats(user_attributes, item_attributes);
  }

  /**
   * Display statistics for user and item attributes.
   * @param user_attributes the user attributes
   * @param item_attributes the item attributes
   */
  public static void displayAttributeStats(SparseBooleanMatrix user_attributes, SparseBooleanMatrix item_attributes) {
    if (user_attributes != null) {
      System.out.println(
          user_attributes.numberOfColumns()       + " user attributes for " +
        + user_attributes.numberOfRows()          + " users, " +
        + user_attributes.numberOfEntries()       + " assignments, " +
        + user_attributes.nonEmptyRowIDs().size() + " users with attribute assignments"
      );
    }

    if (item_attributes != null) {
      System.out.println(
          item_attributes.nonEmptyColumnIDs().size() + " item attributes for " +
        + item_attributes.numberOfRows()             + " items, " +
        + item_attributes.numberOfEntries()          + " assignments, "
        + item_attributes.nonEmptyRowIDs().size()    + " items with attribute assignments"
      );
    }
  }
  
  public static String combine(String directory, String filename) {
    if(!directory.endsWith("/") && !directory.endsWith("\\")) directory = directory + "/";
    String path = directory + filename;
    return path;
  }
  
  public static <T extends Number> double average(Collection<T> values) {
    double sum = 0;
    for(Number value : values) {
      sum += value.doubleValue();  
    }
    return sum / values.size();
  }
 
  public static <T> Collection<T> intersect(Collection<T> a, Collection<T> b) {
    Set<T> intersection = new HashSet<T>(a);
    intersection.retainAll(b);
    return intersection;    
  }

  public static <T> Collection<T>  union(Collection<T> a, Collection<T> b) {
    Set<T> intersection = new HashSet<T>(a);
    intersection.addAll(b);
    return intersection;
  }
  
  public static Integer parseInteger(String string) {
    try {
      Integer integer = Integer.parseInt(string);
      return integer;
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
}
