// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.ratingprediction;

/**
 * Interface for rating predictors which support incremental training
 * @author Zeno Gantner
 *
 * By incremental training we mean that after each update, the recommender does not
 * perform a complete re-training using all data, but only a brief update procedure
 * taking into account the update and only a subset of the existing training data.
 *
 * This interface does not prevent you from doing a complete re-training when implementing
 * a new class. This makes sense e.g. for simple average-based models.
 *
 * This interface assumes that every user can rate every item only once.
 * @version 2.03
 */
public interface IIncrementalRatingPredictor extends IRatingPredictor {

     /**
	  * Add a new rating and perform incremental training
 	  * @param userId the ID of the user who performed the rating
 	  * @param itemId the ID of the rated item
	  * @param rating the rating value
	  */
	void addRating(int userId, int itemId, double rating);

	 /**
	  * Update an existing rating and perform incremental training
	  * @param userId the ID of the user who performed the rating
	  * @param itemId the ID of the rated item
	  * @param rating the rating value
	  */
	void updateRating(int userId, int itemId, double rating) throws IllegalArgumentException;

	 /**
	  * Remove an existing rating and perform "incremental" training
	  * @param userId the ID of the user who performed the rating
	  * @param itemId the ID of the rated item
	  */
	void removeRating(int userId, int itemId);

	/**
	 * Remove a user from the recommender model, and delete all their ratings
	 * 
	 * It is up to the recommender implementor whether there should be model updates after this
	 * action, both options are valid.
	 * 
	 * @param userId the ID of the user to be removed
	 */
	void removeUser(int userId);

	/**
	 * Remove an item from the recommender model, and delete all ratings of this item
	 * 
	 * It is up to the recommender implementor whether there should be model updates after this
	 * action, both options are valid.
	 * 
	 * @param itemId the ID of the user to be removed
	 */
	void removeItem(int itemId);
	
    /**
     * true if users shall be updated when doing incremental updates.
     */
	// TODO this name feels wrong
    boolean getUpdateUsers();
    
    /**
     * Set to true if users shall be updated when doing incremental updates.
     * Set to false if you do not want any updates to the user model parameters when doing incremental updates.
     * Default should be true.
     * 
     */
    void setUpdateUsers(boolean updateUsers);

    /**
     * true if items shall be updated when doing incremental updates.
     */
    // TODO this name feels wrong
    boolean getUpdateItems();
    
    /**
     * Set to true if items shall be updated when doing incremental updates.
     * Set to false if you do not want any updates to the item model parameters when doing incremental updates.
     * Default should true.
     */
    void setUpdateItems(boolean updateItems);
    
}