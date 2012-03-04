//Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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

package org.mymedialite.itemrec;

import java.util.List;

/**
 * Base class for item recommenders that support incremental updates.
 */
public abstract class IncrementalItemRecommender extends ItemRecommender implements IIncrementalItemRecommender {

  @Override
  public void addFeedback(int user_id, int item_id) {
    if (user_id > maxUserID)
      addUser(user_id);
    if (item_id > maxItemID)
      addItem(item_id);

    feedback.add(user_id, item_id);
  }

  @Override
  public void addFeedback(int user_id, List<Integer> item_ids) {
    for(int item_id : item_ids)
      addFeedback(user_id, item_id); 
  }
  
  @Override
  public void removeFeedback(int user_id, int item_id) {
    if (user_id > maxUserID)
      throw new IllegalArgumentException("Unknown user " + user_id);
    if (item_id > maxItemID)
      throw new IllegalArgumentException("Unknown item " + item_id);

    feedback.remove(user_id, item_id);
  }

  protected void addUser(int user_id) {
    if (user_id > maxUserID)
      maxUserID = user_id;
  }

  protected void addItem(int item_id) {
    if (item_id > maxItemID)
      maxItemID = item_id;
  }

  @Override
  public void removeUser(int user_id) {
    feedback.removeUser(user_id);
    if (user_id == maxUserID)
      maxUserID--;
  }

  @Override
  public void removeItem(int item_id) {
    feedback.removeItem(item_id);
    if (item_id == maxItemID)
      maxItemID--;
  }
  
}
