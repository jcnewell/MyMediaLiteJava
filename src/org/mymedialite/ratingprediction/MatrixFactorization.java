// Copyright (C) 2010 Zeno Gantner, Steffen Rendle, Christoph Freudenthaler
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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.ratingprediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.IMatrixUtils;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixUtils;
import org.mymedialite.datatype.VectorUtils;
import org.mymedialite.util.Recommender;

/** 
 * Simple matrix factorization class
 * 
 * Factorizing the observed rating values using a factor matrix for users and one for items.
 * This class can update the factorization online.
 * 
 * After training, an ArithmeticException is thrown if there are NaN values in the model.
 * NaN values occur if values become too large or too small to be represented by the type double.
 * If you encounter such problems, there are three ways to fix them:
 * (1) (preferred) Use the BiasedMatrixFactorization engine, which is more stable.
 * (2) Change the range of rating values (1 to 5 works generally well with the default settings).
 * (3) Change the learn_rate (decrease it if your range is larger than 1 to 5).
 */
//public class MatrixFactorization extends RatingPredictor implements IIterativeModel {
public class MatrixFactorization extends RatingPredictor implements IIterativeModel /* , IIncrementalRatingPredictor */ { // TODO make incremental
  
	/** Matrix containing the latent user factors */
	protected Matrix<Double> userFactors;

	/** Matrix containing the latent item factors */
	protected Matrix<Double> itemFactors;

	/** The bias (global average) */
	protected double globalBias;

	/** Mean of the normal distribution used to initialize the factors */
	public double initMean;

	/** Standard deviation of the normal distribution used to initialize the factors */
	public double initStdev;

	/** Number of latent factors */
	public int numFactors;

	/** Learn rate */
	public double learnRate;

	/** Regularization parameter */
	public double regularization;

	/** Number of iterations over the training data */
	public int numIter;

	/** Default constructor */
	public MatrixFactorization() {
		// set default values
		regularization = 0.015;
		learnRate = 0.01;
		numIter = 30;
		initStdev = 0.1;
		numFactors = 10;
	}
  
	/** Initialize the model data structure */
	protected void initModel() {
		super.initModel();

		// init factor matrices
		userFactors = new Matrix<Double>(ratings.getMaxUserID() + 1, numFactors);
		itemFactors = new Matrix<Double>(ratings.getMaxItemID() + 1, numFactors);
		MatrixUtils.initNormal(userFactors, initMean, initStdev);
		MatrixUtils.initNormal(itemFactors, initMean, initStdev);
	}
  
	/** @override */
	public void train() {
		initModel();

		// learn model parameters
		globalBias = ratings.getAverage();
		learnFactors(ratings.getRandomIndex(), true, true);
	}

	/**   */
	public void iterate() {
		iterate(ratings.getRandomIndex(), true, true);
	}

	/** 
	 * Updates the latent factors on a user
	 * @param user_id the user ID 
	 */
	public void retrainUser(int user_id) {
		if (updateUsers) {
			MatrixUtils.rowInitNormal(userFactors, initMean, initStdev, user_id);
			learnFactors(ratings.getByUser().get(user_id), true, false);
		}
	}

	/**
	 * Updates the latent factors of an item
	 * @param item_id the item ID
	 */
	public void retrainItem(int item_id) {
		if (updateItems) {
			MatrixUtils.rowInitNormal(itemFactors, initMean, initStdev, item_id);
			learnFactors(ratings.getByItem().get(item_id), false, true);
		}
	}

	/**
	 * Iterate once over rating data and adjust corresponding factors (stochastic gradient descent)
	 * @param rating_indices a list of indices pointing to the ratings to iterate over
	 * @param update_user true if user factors to be updated
	 * @param update_item true if item factors to be updated
	 */
	protected void iterate(List<Integer> rating_indices, boolean update_user, boolean update_item) {
		for (int index : rating_indices) {
			int u = ratings.getUsers().get(index);
			int i = ratings.getItems().get(index);

			double p = predict(u, i, false);
			double err = ratings.get(index) - p;

			// Adjust factors
			for (int f = 0; f < numFactors; f++) {
				double u_f = userFactors.get(u, f);
				double i_f = itemFactors.get(i, f);

				// compute factor updates
				double delta_u = err * i_f - regularization * u_f;
				double delta_i = err * u_f - regularization * i_f;

				// if necessary, apply updates
				if (update_user)
					MatrixUtils.inc(userFactors, u, f, learnRate * delta_u);
				if (update_item)
					MatrixUtils.inc(itemFactors, i, f, learnRate * delta_i);
			}
		}
	}

	private void learnFactors(List<Integer> rating_indices, boolean update_user, boolean update_item) {
		for (int current_iter = 0; current_iter < numIter; current_iter++) {
			iterate(rating_indices, update_user, update_item);
		}
	}

	/**
	 */
	protected double predict(int user_id, int item_id, boolean bound) {
		double result = globalBias + MatrixUtils.rowScalarProduct(userFactors, user_id, itemFactors, item_id);

		if (bound) {
			if (result > getMaxRating()) return getMaxRating();
			if (result < getMinRating()) return getMinRating();
		}
		return result;
	}

	  /**
	   * Predict the rating of a given user for a given item
	   * If the user or the item are not known to the engine, the global average is returned.
	   * To avoid this behavior for unknown entities, use CanPredict() to check before.
	   * @param user_id the user ID
	   * @param item_id the item ID
	   * @returnthe predicted rating</returns>
	   */
	  public double predict(int user_id, int item_id) {
		    if (user_id >= userFactors.dim1)
		    	return globalBias;
		    if (item_id >= itemFactors.dim1)
		    	return globalBias;
		    return predict(user_id, item_id, true);
	  }

	  /**   */
	  public void add(int user_id, int item_id, double rating) {
	    super.add(user_id, item_id, rating);
	    retrainUser(user_id);
	    retrainItem(item_id);
	  }
	
	  /**   */
	  public void updateRating(int user_id, int item_id, double rating) {
	    super.updateRating(user_id, item_id, rating);
	    retrainUser(user_id);
	    retrainItem(item_id);
	  }
	
	  /**   */
	  public void addUser(int user_id) {
	    if (user_id > maxUserID) {
	      super.addUser(user_id);
	      userFactors.addRows(user_id + 1);
	    }
	  }
	
	  /**   */
	  public void addItem(int item_id) {
	    if (item_id > maxItemID) {
	      super.addItem(item_id);
	      itemFactors.addRows(item_id + 1);
	    }
	  }
	
	  /**   */
	  public void removeUser(int user_id) {
	    super.removeUser(user_id);
	
	    // set user factors to zero
	    userFactors.setRowToOneValue(user_id, 0.0D);
	  }
	
	  /**   */
	  public void removeItem(int item_id) {
	    super.removeItem(item_id);
	
	    // set item factors to zero
	    itemFactors.setRowToOneValue(item_id, 0.0D);
	  }
	
	   
	  public void saveModel(String filename)  throws IOException {
		    PrintWriter writer = Recommender.getWriter(filename, this.getClass());
		    writer.println(Double.toString(globalBias));
		    IMatrixUtils.writeMatrix(writer, userFactors);
		    IMatrixUtils.writeMatrix(writer, itemFactors);
		    boolean error = writer.checkError();
		    if(error) System.out.println("Error writing file.");
		    writer.flush();
		    writer.close();
	  }
	
	   
	  public void loadModel(String filename) throws IOException  {
		    BufferedReader reader = Recommender.getReader(filename, this.getClass());
		    double bias = Double.parseDouble(reader.readLine());
		
		    Matrix<Double> user_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
		    Matrix<Double> item_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
		    reader.close();
		
		    // Assign new model
		    this.globalBias = bias;
		    // Assign new model
		    if (this.numFactors != user_factors.getNumberOfColumns()) {
		    	System.err.println("Set num_factors to " + user_factors.getNumberOfColumns());
		      	this.numFactors = user_factors.getNumberOfColumns();
		    }
		    this.userFactors = user_factors;
		    this.itemFactors = item_factors;
		      
		    if (user_factors.getNumberOfColumns() != item_factors.getNumberOfColumns()) {
		    	throw new IOException("Number of user and item factors must match: " + user_factors.getNumberOfColumns() + " != " + item_factors.getNumberOfColumns());
		    }
		    this.maxUserID = user_factors.getNumberOfRows() - 1;
		    this.maxItemID = item_factors.getNumberOfRows() - 1;  
	  }
	
	  /** Compute fit (RMSE) on the training data.
	   * @return the root mean square error (RMSE) on the training data
	   */
	  public double computeFit() {
		    double rmse_sum = 0;
		    for (int i = 0; i < ratings.size(); i++) {
		    	rmse_sum += Math.pow(predict(ratings.getUsers().get(i), ratings.getItems().get(i)) - ratings.get(i), 2);
		    }
		    double fit = Math.sqrt(rmse_sum / ratings.size());
		    //System.out.println("computeFit: " + fit);
		    return fit;
	  }
	  
	
	  /**
	   * Compute the regularized loss
	   * @return the regularized loss
	   */
	  public double computeLoss() {
		    double loss = 0;
		    for (int i = 0; i < ratings.size(); i++) {
		    	int user_id = ratings.getUsers().get(i);
		       	int item_id = ratings.getItems().get(i);
		       	loss += Math.pow(predict(user_id, item_id) - ratings.get(i), 2);
		    }
		
		    for (int u = 0; u <= maxUserID; u++) {
		    	loss += ratings.getCountByUser().get(u) * regularization * Math.pow(VectorUtils.euclideanNorm(userFactors.getRow(u)), 2);
		    }
		      
		    for (int i = 0; i <= maxItemID; i++) {
		    	loss += ratings.getCountByUser().get(i) * regularization * Math.pow(VectorUtils.euclideanNorm(itemFactors.getRow(i)), 2);
		    }
		    
		    return loss;
	  }
	
	   
	  public String toString() {
	      return "MatrixFactorization num_factors=" + numFactors + " regularization=" + regularization + " learn_rate=" + learnRate + " num_iter=" + numIter + " init_mean=" + initMean + " init_stdev=" + initStdev;
	  }
	
	  @Override
	  public void setNumIter(int num_iter) {
		  this.numIter = num_iter;
	  }
	
	  @Override
	  public int getNumIter() {
		  return numIter;
	  }
}