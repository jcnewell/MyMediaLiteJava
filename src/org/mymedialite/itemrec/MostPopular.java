//Copyright (C) 2010 Steffen Rendle, Zeno Gantner
//Copyright (C) 2011 Zeno Gantner
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

package org.mymedialite.itemrec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.mymedialite.io.Model;

/** 
 * Most-popular item recommender
 * Items are weighted by how often they have been seen in the past. 
 * This method is not personalized.
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class MostPopular extends IncrementalItemRecommender {

  private static final String VERSION = "2.03";
  
  /** View count */
  protected List<Integer> view_count;

  public void train() {
    view_count = new ArrayList<Integer>(maxItemID + 1);
    for (int i = 0; i <= maxItemID; i++)
      view_count.add(0);

     for(int i : feedback.items())
       view_count.set(i, view_count.get(i) + 1);
  }
  
  public double predict(int user_id, int item_id) {
    if (item_id <= maxItemID) {
      return view_count.get(item_id);
    } else {
        return 0;
    }
  }
   
  protected void addItem(int item_id) {
    super.addItem(item_id);
    while (view_count.size() <= maxItemID) view_count.add(0);
  }
  
  public void removeItem (int item_id) {
    super.removeItem(item_id);
    view_count.set(item_id,  0);
  }

  public void addFeedback(int user_id, int item_id) {
    super.addFeedback(user_id, item_id);
    view_count.set(item_id, view_count.get(item_id) + 1);
  }
   
  public void removeFeedback(int user_id, int item_id) {
    super.removeFeedback(user_id, item_id);
    view_count.set(item_id, view_count.get(item_id) - 1);
  }

  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }
  
  public void saveModel(PrintWriter writer) {
    writer.println(maxItemID + 1);
    for (int i = 0; i <= maxItemID; i++) {
      writer.println(i + " " + view_count.get(i));
    }
  }

  public void loadModel(String filename) throws IOException {
    System.out.println("MostPopular.loadModel()");
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }
  
  public void loadModel(BufferedReader reader) throws IOException {
    int size = Integer.parseInt(reader.readLine());
    System.out.println("MostPopular size: " + size);
    List<Integer> view_count = new ArrayList<Integer>(size);
    
    String line;
    while ((line = reader.readLine()) != null) {
      String[] numbers = line.split(" ");
      int item_id = Integer.parseInt(numbers[0]);
      int count   = Integer.parseInt(numbers[1]);
      view_count.add(item_id, count);
    }
    this.view_count = view_count;
    maxItemID = view_count.size() - 1;
  }

  public String toString() {
      return this.getClass().getName();
  }

}