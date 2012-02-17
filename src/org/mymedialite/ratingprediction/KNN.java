// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.IRatings;
import org.mymedialite.io.Model;

/**
 * Base class for rating predictors that use some kind of kNN.
 * 
 * The method is described in section 2.2 of the paper below.
 * One difference is that we support several iterations of alternating optimization,
 * instead of just one.
 *
 * Literature:
 *
 *   Yehuda Koren: Factor in the Neighbors: Scalable and Accurate Collaborative Filtering,
 *   Transactions on Knowledge Discovery from Data (TKDD), 2009.
 *   http://public.research.att.com/~volinsky/netflix/factorizedNeighborhood.pdf
 *         
 * This recommender supports incremental updates.
 * 
 * See also org.mymedialite.itemrec.KNN
 * @version 2.03
 */
public abstract class KNN extends IncrementalRatingPredictor {

  private static final String VERSION = "2.03";
  protected UserItemBaseline baseline_predictor = new UserItemBaseline();
  
  /**
   * Number of neighbors to take into account for predictions.
   */
  public int k = Integer.MAX_VALUE;

  /**
   * Default constructor
   */
  public KNN() {
    baseline_predictor.regU = 12; // 10;
    baseline_predictor.regI = 1; // 5;
  }
  
  /**
   * 
   */
  @Override
  public void setRatings(IRatings ratings) {
    super.setRatings(ratings);
    baseline_predictor.setRatings(ratings);
  }

  /**
   * Get the regularization constant for the user bias of the underlying baseline predictor.
   * @return the regularization parameter
   */
  public double getRegU() {
    return baseline_predictor.regU;
  }

  /**
   * Set the regularization constant for the user bias of the underlying baseline predictor.
   * @param regU the regularization parameter
   */
  public void setRegU(double regU) {
    baseline_predictor.regU = regU;
  }

  /**
   * Get the regularization constant for the user bias of the underlying baseline predictor
   * @return the regularization parameter
   */
  public double getRegI() {
    return baseline_predictor.regI;
  }

  /**
   * Set the regularization constant for the item bias of the underlying baseline predictor.
   * @param regI the regularization parameter
   */
  public void setRegI(double regI) {
    baseline_predictor.regI = regI;
  }

  /**
   * Correlation matrix over some kind of entity.
   */
  protected CorrelationMatrix correlation;

  // TODO Check whether k value should be preserved k in the load/save methods below.
  
  /**
   * 
   */
  @Override
  public void saveModel(String filename) throws IOException {
    baseline_predictor.saveModel(filename + "-global-effects");
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    correlation.write(writer);
    writer.flush();
    writer.close();
  }

  /**
   * 
   */
  @Override
  public void loadModel(String filename) throws IOException {
    baseline_predictor.loadModel(filename + "-global-effects");
    if (ratings != null)
      baseline_predictor.setRatings(ratings);

    BufferedReader reader = Model.getReader(filename, this.getClass());    
    CorrelationMatrix correlation = CorrelationMatrix.readCorrelationMatrix(reader);
    this.correlation = correlation;
    reader.close();
  }
  
}
