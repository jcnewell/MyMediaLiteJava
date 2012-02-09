// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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

package org.mymedialite.itemrec;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.mymedialite.IRecommender;
import org.mymedialite.data.IEntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.IdentityMapping;
import org.mymedialite.data.WeightedItem;

/**
 * Class that contains static methods for item prediction.
 * @version 2.03
 */
public class Extensions {

  // Prevent instantiation.
  private Extensions() {}

  /**
   * Write item predictions (scores) to a file.
   * @param recommender the IRecommender to use for making the predictions
   * @param train a user-wise IPosOnlyFeedback containing the items already observed
   * @param candidate_items the list of candidate items
   * @param num_predictions the number of items to return per user, -1 if there should be no limit
   * @param filename the name of the file to write to
   * @param users a list of users to make recommendations for
   * @param user_mapping an IEntityMapping object for the user IDs
   * @param item_mapping an IEntityMapping object for the item IDs
   * @throws IOException 
   */
  public static void writePredictions(
      IRecommender recommender,
      IPosOnlyFeedback train,
      Collection<Integer> candidate_items,
      int num_predictions,
      String filename,
      List<Integer> users,
      IEntityMapping user_mapping,
      IEntityMapping item_mapping) throws IOException {
    
    PrintWriter writer = new PrintWriter(filename);
    writePredictions(recommender, train, candidate_items, num_predictions, writer, users, user_mapping, item_mapping);
  }

  /**
   * Write item predictions (scores) to a TextWriter object.
   * @param recommender the IRecommender to use for making the predictions
   * @param train a user-wise IPosOnlyFeedback containing the items already observed
   * @param candidate_items the list of candidate items
   * @param num_predictions the number of items to return per user, -1 if there should be no limit
   * @param writer the TextWriter to write to
   * @param users a list of users to make recommendations for; if null, all users in train will be provided with recommendations
   * @param user_mapping an IEntityMapping object for the user IDs
   * @param item_mapping an IEntityMapping object for the item IDs
   */
  public static void writePredictions(
      IRecommender recommender,
      IPosOnlyFeedback train,
      Collection<Integer> candidate_items,
      int num_predictions,
      PrintWriter writer,
      List<Integer> users,
      IEntityMapping user_mapping,
      IEntityMapping item_mapping) {
    
    // TODO check why the supplied users are ignored. Should there be a null check?
    users = new ArrayList<Integer>(train.allUsers());
    
    for (int user_id : users) {
      IntCollection ignore_items = train.userMatrix().get(user_id);
      writePredictions(recommender, user_id, candidate_items, ignore_items, num_predictions, writer, user_mapping, item_mapping);
    }
  }

  /**
   * Write item predictions (scores) to a TextWriter object.
   * @param recommender the <see cref="IRecommender"/> to use for making the predictions
   * @param user_id the ID of the user to make recommendations for
   * @param candidate_items the list of candidate items
   * @param ignore_items a list of items for which no predictions should be made
   * @param num_predictions the number of items to return per user, -1 if there should be no limit
   * @param writer the <see cref="TextWriter"/> to write to
   * @param user_mapping an <see cref="IEntityMapping"/> object for the user IDs
   * @param item_mapping an <see cref="IEntityMapping"/> object for the item IDs
   */
  public static void writePredictions(
      IRecommender recommender,
      int user_id,
      Collection<Integer> candidate_items,
      Collection<Integer> ignore_items,
      int num_predictions,
      PrintWriter writer,
      IEntityMapping user_mapping,
      IEntityMapping item_mapping) {
    
    if (user_mapping == null)
      user_mapping = new IdentityMapping();
    if (item_mapping == null)
      item_mapping = new IdentityMapping();

    List<WeightedItem> score_list = new ArrayList<WeightedItem>();
    for (int item_id : candidate_items)
      score_list.add( new WeightedItem(item_id, recommender.predict(user_id, item_id)));
  
    Collections.sort(score_list, Collections.reverseOrder());
    int prediction_count = 0;

    writer.print(user_mapping.toOriginalID(user_id) + "\t[");
    for (WeightedItem wi : score_list) {
      if (!ignore_items.contains(wi.item_id) && wi.weight > Double.MIN_VALUE) {
        if (prediction_count == 0)
          writer.print(item_mapping.toOriginalID(wi.item_id) + ":" + wi.weight.toString());
        else
          writer.print("," + item_mapping.toOriginalID(wi.item_id) + ":" + wi.weight.toString());

        prediction_count++;
      }

      if (prediction_count == num_predictions)
        break;
    }
    writer.println("]");
  }

  /**
   * predict items for a specific users.
   * @param recommender the <see cref="IRecommender"/> object to use for the predictions
   * @param user_id the user ID
   * @param max_item_id the maximum item ID
   * @return a list sorted list of item IDs
   */
  public static List<Integer> predictItems(IRecommender recommender, int user_id, int max_item_id) {
    List<Integer> items = new ArrayList<Integer>(max_item_id + 1);
    for (int i = 0; i < max_item_id; i++)
      items.add(i);
    return predictItems(recommender, user_id, items);
  }

  /**
   * Predict items for a given user.
   * @param recommender the recommender to use
   * @param user_id the numerical ID of the user
   * @param candidate_items a collection of numerical IDs of candidate items
   * @return an ordered list of items, the most likely item first
   */
  public static List<Integer> predictItems(IRecommender recommender, int user_id, Collection<Integer> candidate_items) {
    ArrayList<WeightedItem> result = new ArrayList<WeightedItem>(candidate_items.size());
    //for (Iterator<Integer> iterator = candidate_items.iterator(); iterator.hasNext();) {
    //int item_id = iterator.next();
    for (int item_id : candidate_items) {
      result.add(new WeightedItem(item_id, recommender.predict(user_id, item_id)));
    }
    Collections.sort(result, Collections.reverseOrder());
    
    List<Integer> return_array = new ArrayList<Integer>(result.size());
    for (int i = 0; i < result.size(); i++)  return_array.add(i, result.get(i).item_id);
    
    return return_array;
  }
  
//  /**
//   * Predict items for a specific user
//   * <param name="recommender">the <see cref="IRecommender"/> object to use for the predictions</param>
//   * <param name="user_id">the user ID</param>
//   * <param name="max_item_id">the maximum item ID</param>
//   * <returns>a list sorted list of item IDs</returns>
//   */
//  public static int[] predictItems(IRecommender recommender, int user_id, int max_item_id) {
//    ArrayList<WeightedItem> result = new ArrayList<WeightedItem>();
//    for (int item_id = 0; item_id <= max_item_id; item_id++) {
//      result.add( new WeightedItem(item_id, recommender.predict(user_id, item_id)));
//    }
//    Collections.sort(result, Collections.reverseOrder());
//
//    int[] return_array = new int[max_item_id + 1];
//    for (int i=0; i<return_array.length; i++) {
//      return_array[i] = result.get(i).item_id;
//    }
//    return return_array;
//  }
//
//  /**
//   * Predict items for a given user.
//   * @param recommender the recommender to use
//   * @param user_id the numerical ID of the user
//   * @param relevant_items a collection of numerical IDs of relevant items
//   * @return an ordered list of items, the most likely item first
//   */
//  public static int[] predictItems(IRecommender recommender, int user_id, Collection<Integer> relevant_items) {
//    ArrayList<WeightedItem> result = new ArrayList<WeightedItem>();
//    for (int item_id : relevant_items) {
//      result.add( new WeightedItem(item_id, recommender.predict(user_id, item_id)));
//    }
//    Collections.sort(result, Collections.reverseOrder());
//
//    int[] return_array = new int[result.size()];
//    for (int i=0; i<return_array.length; i++) {
//      return_array[i] = result.get(i).item_id;
//    }
//    return return_array;
//  }
  
}
