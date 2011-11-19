package org.mymedialite.examples;


import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.eval.Items;
import org.mymedialite.io.ItemRecommendation;
import org.mymedialite.itemrecommendation.BPRMF;
import org.mymedialite.itemrecommendation.MostPopular;

public class ItemRecommenderExample {

  public static void main(String[] args) {
    long start = Calendar.getInstance().getTimeInMillis();

    // Load the data
    EntityMapping user_mapping = new EntityMapping();
    EntityMapping item_mapping = new EntityMapping();
    
    PosOnlyFeedback training_data = null;
    try {
      training_data = ItemRecommendation.read(args[0], user_mapping, item_mapping);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Collection<Integer> relevant_items = training_data.getAllItems();  // items that will be taken into account in the evaluation
  
    PosOnlyFeedback test_data = null;
    try {
      test_data = ItemRecommendation.read(args[1], user_mapping, item_mapping);
    } catch (NumberFormatException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    // Set up the recommender
    BPRMF recommender = new BPRMF();
    //MostPopular recommender = new MostPopular();
    recommender.setFeedback(training_data);    
    recommender.train();  
    
    // Test online update.
    //recommender.addUser(1);
    //recommender.addItem(1);
    //recommender.addFeedback(4, 21);
    //recommender.removeUser(4);
    //recommender.removeItem(5);
    //recommender.removeFeedback(4, 5);
    
    /*
     * Test online update.
     */
    int internalUserId1 = user_mapping.toInternalID(28221);
    recommender.addUser(internalUserId1);
    
    int internalItemId1 = item_mapping.toInternalID(2854);
    recommender.addItem(internalItemId1);  
    recommender.addFeedback(internalUserId1, internalItemId1);
   
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2855));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2985));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2071));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(950));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2982));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2857));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2979));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2316));
    recommender.addFeedback(internalUserId1, item_mapping.toInternalID(2978));
    
    
    // Measure the accuracy on the test data set
    try {
      HashMap<String, Double> results = Items.Evaluate(recommender, test_data, training_data, relevant_items);
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
    int userId = 4;
    int itemId = 21;
    double prediction = recommender.predict(user_mapping.toInternalID(userId), item_mapping.toInternalID(itemId));
    System.out.println("userId: " + userId + " itemId: " + itemId + " prediction: " + prediction); 
  
    userId = 4;
    itemId = 673;
    prediction = recommender.predict(user_mapping.toInternalID(userId), item_mapping.toInternalID(itemId));
    System.out.println("userId: " + userId + " itemId: " + itemId + " prediction: " + prediction); 
    
    userId = 35525;
    itemId = 21;
    prediction = recommender.predict(user_mapping.toInternalID(userId), item_mapping.toInternalID(itemId));
    System.out.println("userId: " + userId + " itemId: " + itemId + " prediction: " + prediction); 
    
    userId = 35525;
    itemId = 673;
    prediction = recommender.predict(user_mapping.toInternalID(userId), item_mapping.toInternalID(itemId));
    System.out.println("userId: " + userId + " itemId: " + itemId + " prediction: " + prediction); 
    
    long end = Calendar.getInstance().getTimeInMillis();
    System.out.println("Time taken: " + ((end - start) / 1000F) + " seconds");
    start = end;
  }
  
}
