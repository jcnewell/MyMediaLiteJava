// Copyright (C) Zeno Gantner, Chris Newell
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

package org.mymedialite.examples;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.eval.Items;
import org.mymedialite.io.ItemData;
import org.mymedialite.itemrec.BPRMF;

/**
 * Example program for ItemRecommenders.
 * @version 2.03
 */
public class ItemRecommenderExample {

  public static final String MODEL_FILE = "online_viewings.dat";
  
  public static void main(String[] args) {
    long start = Calendar.getInstance().getTimeInMillis();

    // Load the data
    EntityMapping user_mapping = new EntityMapping();
    EntityMapping item_mapping = new EntityMapping();

    IPosOnlyFeedback training_data = null;
    try {
      training_data = ItemData.read(args[0], user_mapping, item_mapping, false);
    } catch (Exception e) {
      e.printStackTrace();
    }

    IPosOnlyFeedback test_data = null;
    try {
      test_data = ItemData.read(args[1], user_mapping, item_mapping, false);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Set up the recommender
    BPRMF recommender = new BPRMF();
    //MostPopular recommender = new MostPopular();
    System.out.println(recommender);
    recommender.setFeedback(training_data);
        
    File file = new File(MODEL_FILE);
    try {
      if(file.exists()) {
        recommender.loadModel(MODEL_FILE);
      } else {
        recommender.train();      
        recommender.saveModel(MODEL_FILE);
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    /*
     * Test incremental update.
     */
    //		int internalUserId1 = user_mapping.toInternalID(28221);
    //		recommender.addUser(internalUserId1);
    //
    //		int internalItemId1 = item_mapping.toInternalID(2854);
    //		recommender.addItem(internalItemId1);  
    //		recommender.addFeedback(internalUserId1, internalItemId1);
    //
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2855));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2985));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2071));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(950));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2982));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2857));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2979));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2316));
    //		recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2978));


    // Measure the accuracy on the test data set
    Collection<Integer> candidate_items = training_data.allItems();  // items that will be taken into account in the evaluation
    Collection<Integer> test_users      = training_data.allUsers();  // users that will be taken into account in the evaluation
    try {
      HashMap<String, Double> results = Items.evaluate(recommender, test_data, training_data, test_users, candidate_items);
      System.out.println("AUC       " + results.get("AUC"));
      System.out.println("MAP       " + results.get("MAP"));
      System.out.println("NDCG      " + results.get("NDCG"));
      System.out.println("prec@5    " + results.get("prec@5"));
      System.out.println("prec@10   " + results.get("prec@10"));
      System.out.println("prec@15   " + results.get("prec@15"));
      System.out.println("num_users " + results.get("num_users"));
      System.out.println("num_items " + results.get("num_items"));
      System.out.println();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make a prediction for a certain user and item.
    String userId = "2";
    String itemId = "2716";
    double prediction = recommender.predict(user_mapping.toInternalID(userId), item_mapping.toInternalID(itemId));
    System.out.println("userId: " + userId + " itemId: " + itemId + " prediction: " + prediction); 

    long end = Calendar.getInstance().getTimeInMillis();
    System.out.println("Time taken: " + ((end - start) / 1000F) + " seconds");
    start = end;
  }  
}
