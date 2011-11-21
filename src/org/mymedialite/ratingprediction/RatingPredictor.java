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

package org.mymedialite.ratingprediction;

import java.io.IOException;

import org.mymedialite.data.IRatings;

/** Abstract class for rating predictors that keep the rating data in memory for training (and possibly prediction) */
public abstract class RatingPredictor implements IRatingPredictor, Cloneable {

	/** Maximum user ID */
	public int maxUserID;

	/** Maximum item ID */
	public int maxItemID;

	/** The maximum rating value */
	public double maxRating;

	/** The minimum rating value */
	public double minRating;

	// TODO find clearer name for this
	/** true if users shall be updated when doing online updates */
	/// <value>true if users shall be updated when doing online updates</value>
	public boolean updateUsers = true;

	/** true if items shall be updated when doing online updates */
	/// <value>true if items shall be updated when doing online updates</value>
	public boolean updateItems = true;

	/** The rating data */
	protected IRatings ratings;

	public RatingPredictor clone() throws CloneNotSupportedException {
		return (RatingPredictor) super.clone();
	}

	public IRatings getRatings() { 
		return this.ratings;
	}

	public void setRatings(IRatings ratings) {
		this.ratings = ratings;
	}

	@Override
	public double getMaxRating() {
		return maxRating;
	}

	@Override
	public void setMaxRating(double max_rating) {
		this.maxRating = max_rating;
	}

	@Override
	public double getMinRating() {
		return minRating;
	}

	@Override
	public void setMinRating(double min_rating) {
		this.minRating = min_rating;
	}

	/// <inheritdoc/>
	public abstract double predict(int user_id, int item_id);

	/** 
	 * Inits the recommender model.
	 * This method is called by the Train() method.
	 * When overriding, please call base.InitModel() to get the functions performed in the base class.
	 */
	protected void initModel() {
		maxUserID = ratings.getMaxUserID();
		maxItemID = ratings.getMaxItemID();
	}

	/// <inheritdoc/>
	public abstract void train();

	/// <inheritdoc/>
	public abstract void saveModel(String filename) throws IOException ;

	/// <inheritdoc/>
	public abstract void loadModel(String filename) throws IOException ;    

	/// <inheritdoc/>
	public boolean canPredict(int user_id, int item_id) {
		return (user_id <= maxUserID && user_id >= 0 && item_id <= maxItemID && item_id >= 0);
	}

	/// <inheritdoc/>
	public void add(int user_id, int item_id, double rating) {
		/// Added JCN to override exiting value if it exists.
		//ratings.add(user_id, item_id, rating);
		ratings.addOrUpdate(user_id, item_id, rating);
	}

	/// <inheritdoc/>
	public void updateRating(int user_id, int item_id, double rating) {

	}

	/// <inheritdoc/>
	public void removeRating(int user_id, int item_id) {

	}

	/// <inheritdoc/>
	public void addUser(int user_id) {
		maxUserID = Math.max(maxUserID, user_id);
	}

	/// <inheritdoc/>
	public void addItem(int item_id) {
		maxItemID = Math.max(maxItemID, item_id);
	}

	/// <inheritdoc/>
	public void removeUser(int user_id) {
		if (user_id == maxUserID)
			maxUserID--;
	}

	/// <inheritdoc/>
	public void removeItem(int item_id) {
		if (item_id == maxItemID)
			maxItemID--;
	}

}