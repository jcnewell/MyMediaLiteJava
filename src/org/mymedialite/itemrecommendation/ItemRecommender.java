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

package org.mymedialite.itemrecommendation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.mymedialite.data.IPosOnlyFeedback;

/** 
 * Abstract item recommender class that loads the (positive-only implicit feedback) 
 * training data into memory.
 * The data is stored in two sparse matrices: one column-wise and one row-wise.
 */
public abstract class ItemRecommender implements IItemRecommender, Cloneable {
	  
  /** The maximum user ID */
  protected int maxUserID;

  /** The maximum item ID */
  protected int maxItemID;

  /** The feedback data to be used for training */
  protected IPosOnlyFeedback feedback;

  public void setFeedback(IPosOnlyFeedback feedback) {
    this.feedback = feedback;
    maxUserID = feedback.getMaxUserID();
    maxItemID = feedback.getMaxItemID();
  }
  
  /** Create a shallow copy of the object. */
  public Object clone() {
    return this.clone();
  }
  
  public abstract double predict(int userId, int itemId);

  public boolean canPredict(int user_id, int item_id) {
      return (user_id <= maxUserID && user_id >= 0 && item_id <= maxItemID && item_id >= 0);
  }
  
  public abstract void train();
  
  public abstract void loadModel(String filename) throws IOException;

  public abstract void loadModel(BufferedReader reader) throws IOException;
  
  public abstract void saveModel(String filename) throws IOException;

  public abstract void saveModel(PrintWriter writer) throws IOException;

  public void addFeedback(int user_id, int item_id) throws IllegalArgumentException {
    if (user_id > maxUserID) addUser(user_id);
    if (item_id > maxItemID) addItem(item_id);
    feedback.add(user_id, item_id);
  }

  public void removeFeedback(int user_id, int item_id) {
    if (user_id > maxUserID) throw new IllegalArgumentException("Unknown user " + user_id);
    if (item_id > maxItemID) throw new IllegalArgumentException("Unknown item " + item_id);
    feedback.remove(user_id, item_id);
  }

  protected void addUser(int user_id) {
    if (user_id > maxUserID)  maxUserID = user_id;
  }

  protected void addItem(int item_id)  {
      if (item_id > maxItemID)  maxItemID = item_id;
  }

  public void removeUser(int user_id) {
    feedback.removeUser(user_id);
    if (user_id == maxUserID)  maxUserID--;
  }

  public void removeItem(int item_id) {
    feedback.removeItem(item_id);
    if (item_id == maxItemID)  maxItemID--;
  }
  
  @Override
  public String toString() {
    return this.getClass().getName();
  }
  
}