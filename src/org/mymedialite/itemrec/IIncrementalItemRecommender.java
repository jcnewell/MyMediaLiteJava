// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011 Zeno Gantner
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

import org.mymedialite.IRecommender;

/**
 * Interface for item recommenders
 * @author Zeno Gantner
 *
 * Item prediction or item recommendation is the task of predicting items (movies, books, products, videos, jokes)
 * that a user may like, based on past user behavior (and possibly other information).
 * See also http://recsyswiki/wiki/Item_prediction
 */
public interface IIncrementalItemRecommender extends IRecommender {
	/**
	 * add a new positive feedback event
	 * @param user_id the user ID
	 * @param item_id the item ID
	 */
	void addFeedback(int user_id, int item_id);
	
	/**
	 * remove all positive feedback events with that user-item combination
	 * @param user_id the user ID
	 * @param item_id the item ID
	 */
	void removeFeedback(int user_id, int item_id);
	
	/**
	 * remove all feedback by one user
	 * @param user_id the user ID
	 */
	void removeUser(int user_id);

	/**
	 * remove all feedback for one item
	 * @param item_id the item ID
	 */
	void removeItem(int item_id);
}
