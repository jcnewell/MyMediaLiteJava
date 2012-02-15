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

package org.mymedialite.ratingprediction;

/**
 * Base class for rating predictors that support incremental training
 * @author Zeno Gantner
 * @version 2.03
 */
public abstract class IncrementalRatingPredictor extends RatingPredictor implements	IIncrementalRatingPredictor {

  protected boolean updateUsers;

  protected boolean updateItems;
  
  /** 
   * Default constructor.
   */
  public IncrementalRatingPredictor() {
      updateUsers = true;
      updateItems = true;
  }
  
  /**  */
  public void addRating(int userId, int itemId, double rating) {
    if (userId > maxUserID)
      addUser(userId);
    if (itemId > maxItemID)
      addItem(itemId);

    ratings.add(userId, itemId, rating);
  }

  /**  */
  public void updateRating(int userId, int itemId, double rating) throws IllegalArgumentException {
    Integer index = ratings.tryGetIndex(userId, itemId);
    if (index != null)
      ratings.set(index.intValue(), rating);
    else
      throw new IllegalArgumentException(String.format("Cannot update rating for user %i and item %i: No such rating exists.", userId, itemId));
  }

  /**  */
  public void removeRating(int userId, int itemId) {
    Integer index = ratings.tryGetIndex(userId, itemId);
    if (index != null)
      ratings.removeAt(index);
  }

  /**
   * 
   */
  public void addUser(int userId) {
    maxUserID = Math.max(maxUserID, userId);
  }

  /**
   * 
   */
  public void addItem(int itemId) {
    maxItemID = Math.max(maxItemID, itemId);
  }

  /**  */
  public void removeUser(int userId) {
    if (userId == maxUserID)
      maxUserID--;
    ratings.removeUser(userId);
  }

  /**  */
  public void removeItem(int itemId) {
    if (itemId == maxItemID)
      maxItemID--;
    ratings.removeItem(itemId);
  }

  /**  */
  public boolean getUpdateUsers() {
    return updateUsers;
  }

  /**  */
  public void setUpdateUsers(boolean updateUsers) {
    this.updateUsers = updateUsers;
  }

  /**  */
  public boolean getUpdateItems() {
    return updateItems;
  }

  /**  */
  public void setUpdateItems(boolean updateItems) {
    this.updateItems = updateItems;
  }

}
