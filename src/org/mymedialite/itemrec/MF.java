// Copyright (C) 2010 Steffen Rendle, Zeno Gantner, Christoph Freudenthaler
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixExtensions;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;

/**
 * Abstract class for Matrix Factorization based item predictors.
 * @version 2.03
 */
public abstract class MF extends IncrementalItemRecommender implements IIterativeModel {
    
  private static final String VERSION = "2.03";
  
  /** Latent user factor matrix */
  protected Matrix<Double> userFactors;  // [user index] [feature index]
  
  /** Latent item factor matrix */
  protected Matrix<Double> itemFactors;  // [item index] [feature index]

  /** Mean of the normal distribution used to initialize the latent factors */
  public double initMean;

  /** Standard deviation of the normal distribution used to initialize the latent factors */
  public double initStDev;

  /** Number of latent factors per user/item */
  public int numFactors = 10;

  /** Number of iterations over the training data */
  public int numIter;
  
  public MF() {
    this.numIter    = 30;
    this.initMean   = 0;
    this.initStDev = 0.1;
  }

  /** Get the latent user factor matrix */
  public Matrix<Double> getUserFactors() { return userFactors; }
  
  /** Get the latent item factor matrix */
  public Matrix<Double> getItemFactors() { return itemFactors; }
  
  /** { @inheritDoc } */
  public int getNumIter() { return numIter; }
  
  /** { @inheritDoc } */
  public void setNumIter(int num_iter) { this.numIter = num_iter; }
  
  protected void initModel() {
    userFactors = new Matrix<Double>(maxUserID + 1, numFactors);
    itemFactors = new Matrix<Double>(maxItemID + 1, numFactors);

    MatrixExtensions.initNormal(userFactors, initMean, initStDev);
    MatrixExtensions.initNormal(itemFactors, initMean, initStDev);
  }
  
  /** { @inheritDoc } */
  public void train() {
    initModel();
    for (int i=0; i<numIter; i++) {
      iterate();
    }
  }

  /** Iterate once over the data */
  public abstract void iterate();

  /** 
   * Computes the fit (optimization criterion) on the training data
   * @return a double representing the fit, lower is better
   */
  public abstract double computeLoss();

  /**
   * Predict the weight for a given user-item combination.
   * If the user or the item are not known to the recommender, zero is returned.
   * To avoid this behavior for unknown entities, use CanPredict() to check before.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted weight
   */  
  public double predict(int user_id, int item_id) {
    if ((user_id < 0) || (user_id >= userFactors.dim1)) {
      System.err.println("user is unknown: " + user_id);
      return 0;
    }
    if ((item_id < 0) || (item_id >= itemFactors.dim1)) {
      System.err.println("item is unknown: " + item_id);
      return 0;
    }

    return MatrixExtensions.rowScalarProduct(userFactors, user_id, itemFactors, item_id);
  }
  
  /** { @inheritDoc } */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }
  
  /** { @inheritDoc } */
  public void saveModel(PrintWriter writer) {
    IMatrixExtensions.writeMatrix(writer, userFactors);
    IMatrixExtensions.writeMatrix(writer, itemFactors);
    boolean error = writer.checkError();
    if(error) System.out.println("Error writing file.");
  }
  
  /** { @inheritDoc } */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }
  
  /** { @inheritDoc } */
  public void loadModel(BufferedReader reader) throws IOException {

    Matrix<Double> user_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.numberOfColumns() != item_factors.numberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.numberOfColumns() + " != " + item_factors.numberOfColumns());

    this.maxUserID = user_factors.numberOfRows() - 1;
    this.maxItemID = item_factors.numberOfRows() - 1;

    // Assign new model
    if (this.numFactors != user_factors.numberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.numberOfColumns());
      this.numFactors = user_factors.numberOfColumns();
    }
    this.userFactors = user_factors;
    this.itemFactors = item_factors;
  }
  
}