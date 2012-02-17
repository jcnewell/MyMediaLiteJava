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

import java.io.*;
import org.mymedialite.IRecommender;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Abstract item recommender class that loads the (positive-only implicit feedback) training data into memory
 * and provides flexible access to it. 
 * @version 2.03
 */
public abstract class ItemRecommender implements IRecommender {

  /** The maximum user ID */
  protected int maxUserID;

  /** The maximum item ID */
  protected int maxItemID;

  /** The feedback data to be used for training */
  protected IPosOnlyFeedback feedback;

  protected ItemRecommender() {
    try {
      feedback = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public IPosOnlyFeedback getFeedback() {
    return feedback;
  }

  public void setFeedback(IPosOnlyFeedback feedback) {
    this.feedback = feedback;
    maxUserID = feedback.maxUserID();
    maxItemID = feedback.maxItemID();
  }

  /** Create a shallow copy of the object. */
  public ItemRecommender clone() {
    ItemRecommender clone = null;
    try {
      clone =  (ItemRecommender) super.clone();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return clone;
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

  @Override
  public String toString() {
    return this.getClass().getName();
  }
  
}