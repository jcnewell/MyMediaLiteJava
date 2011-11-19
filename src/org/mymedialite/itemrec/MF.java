// Copyright (C) 2010 Steffen Rendle, Zeno Gantner, Christoph Freudenthaler
// Copyright (C) 2011 Zeno Gantner, Chris
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
import org.mymedialite.datatype.Matrix;
import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.IMatrixUtils;
import org.mymedialite.datatype.MatrixUtils;
import org.mymedialite.util.Recommender;

/** Abstract class for matrix factorization based item predictors */
public abstract class MF extends ItemRecommender implements IIterativeModel {
    
  /** Latent user factor matrix */
  protected Matrix<Double> user_factors;  // [user index] [feature index]
  
  /** Latent item factor matrix */
  protected Matrix<Double> item_factors;  // [item index] [feature index]

  /** Mean of the normal distribution used to initialize the latent factors */
  public double initMean;

  /** Standard deviation of the normal distribution used to initialize the latent factors */
  public double initStdev;

  /** Number of latent factors per user/item */
  public int numFactors = 10;

  /** Number of iterations over the training data */
  public int numIter;
  
  public MF() {
    numIter = 30;
    initMean = 0;
    initStdev = 0.1;
  }

  /** Get the latent user factor matrix */
  public Matrix<Double> getUserFactors() { return user_factors; }
  
  /** Get the latent item factor matrix */
  public Matrix<Double> getItemFactors() { return item_factors; }

  /** { @inheritDoc } */
  public int getNumIter() { return numIter; }
  
  /** { @inheritDoc } */
  public void setNumIter(int num_iter) { this.numIter = num_iter; }
  
  protected void initModel() {
    user_factors = new Matrix<Double>(maxUserID + 1, numFactors);
    item_factors = new Matrix<Double>(maxItemID + 1, numFactors);

    MatrixUtils.initNormal(user_factors, initMean, initStdev);
    MatrixUtils.initNormal(item_factors, initMean, initStdev);
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
  public abstract double computeFit();

  /**
   * Predict the weight for a given user-item combination.
   * If the user or the item are not known to the recommender, zero is returned.
   * To avoid this behavior for unknown entities, use CanPredict() to check before.
   * @param user_id the user ID
   * @param item_id the item ID
   * @return the predicted weight
   */  
  public double predict(int user_id, int item_id) {
    if ((user_id < 0) || (user_id >= user_factors.dim1)) {
      System.out.println("user is unknown: " + user_id);
      return 0;
    }
    if ((item_id < 0) || (item_id >= item_factors.dim1)) {
      System.err.println("item is unknown: " + item_id);
      return 0;
    }

    return MatrixUtils.rowScalarProduct(user_factors, user_id, item_factors, item_id);
  }
  
  /** { @inheritDoc } */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Recommender.getWriter(filename, this.getClass());
    saveModel(writer);
  }
  
  /** { @inheritDoc } */
  public void saveModel(PrintWriter writer) {
    IMatrixUtils.writeMatrix(writer, user_factors);
    IMatrixUtils.writeMatrix(writer, item_factors);
    boolean error = writer.checkError();
    if(error) System.out.println("Error writing file.");
    writer.flush();
    writer.close();
  }
  
  /** { @inheritDoc } */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Recommender.getReader(filename, this.getClass());
    loadModel(reader);
  }
  
  /** { @inheritDoc } */
  public void loadModel(BufferedReader reader) throws IOException {

    Matrix<Double> user_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.getNumberOfColumns() != item_factors.getNumberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.getNumberOfColumns() + " != " + item_factors.getNumberOfColumns());

    this.maxUserID = user_factors.getNumberOfRows() - 1;
    this.maxItemID = item_factors.getNumberOfRows() - 1;

    // Assign new model
    if (this.numFactors != user_factors.getNumberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.getNumberOfColumns());
      this.numFactors = user_factors.getNumberOfColumns();
    }
    this.user_factors = user_factors;
    this.item_factors = item_factors;
    reader.close();
  }
  
}