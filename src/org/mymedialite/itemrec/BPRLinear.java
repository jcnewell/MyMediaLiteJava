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

package org.mymedialite.itemrec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.mymedialite.util.Random;
import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;

/**
 * Linear model optimized for BPR.
 * 
 * Literature:
 *     Zeno Gantner, Lucas Drumond, Christoph Freudenthaler, Steffen Rendle, Lars Schmidt-Thieme:
 *     Learning Attribute-to-Feature Mappings for Cold-Start Recommendations.
 *     ICDM 2011.
 *     http://www.ismll.uni-hildesheim.de/pub/pdfs/Gantner_et_al2010Mapping.pdf
 *
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class BPRLinear extends ItemRecommender implements IItemAttributeAwareRecommender, IIterativeModel {

  private static final String VERSION = "2.03";
  
  private SparseBooleanMatrix itemAttributes;
  
  public SparseBooleanMatrix getItemAttributes() {
    return itemAttributes;
  }

  public void setItemAttributes(SparseBooleanMatrix itemAttributes) {
    this.itemAttributes = itemAttributes;
    this.maxItemID = Math.max(maxItemID, itemAttributes.numberOfRows() - 1);
  }

  /**
   * 
   */
  @Override
  public int numItemAttributes() {
    return itemAttributes.numberOfColumns();
  }

  // Item attribute weights
  private Matrix<Double> itemAttributeWeightByUser;

  /**
   * One iteration = iterationLength * number of entries in the training matrix.
   */
  protected int iterationLength = 5;

  private Random random;
  
  // Fast, but memory-intensive sampling
  private boolean fastSampling = false;

  /**
   * Number of iterations over the training data.
   */
  public int numIter = 10;

  /**
   * Fast sampling memory limit, in MiB.
   */
  public int fastSamplingMemoryLimit = 1024;

  /**
   * mean of the Gaussian distribution used to initialize the features.
   */
  public double initMean = 0;

  /**
   * standard deviation of the normal distribution used to initialize the features.
   */
  public double initStdev = 0.1;

  /**
   * Learning rate alpha.
   */
  public double learnRate = 0.05;

  /**
   * Regularization parameter.
   */
  public double regularization = 0.015;

  // Support data structure for fast sampling
  private IntList[] userPosItems;

  // Support data structure for fast sampling
  private IntList[] userNegItems;

  @Override
  public void setNumIter(int numIter) {
    this.numIter = numIter;
  }

  @Override
  public int getNumIter() {
    return numIter;
  } 
  
  /**
   *
   */
  @Override
  public void train() {
    random = org.mymedialite.util.Random.getInstance();

    // Prepare fast sampling, if necessary
    int fast_sampling_memory_size = ((maxUserID + 1) * (maxItemID + 1) * 4) / (1024 * 1024);
    System.err.println("fast_sampling_memory_size=" + fast_sampling_memory_size);
    
    if (fast_sampling_memory_size <= fastSamplingMemoryLimit) {
      fastSampling = true;

      this.userPosItems = new IntArrayList[maxUserID + 1];
      this.userNegItems = new IntArrayList[maxUserID + 1];
      for (int u = 0; u < maxUserID + 1; u++) {
        IntList pos_list = new IntArrayList(feedback.userMatrix().get(u));
        userPosItems[u] = pos_list;
        IntList neg_list = new IntArrayList();
        for (int i = 0; i < maxItemID; i++)
          if (!feedback.userMatrix().get(u).contains(i) && feedback.itemMatrix().get(i).size() != 0)
            neg_list.add(i);
        
          userNegItems[u] = neg_list;
      }
    }

    itemAttributeWeightByUser = new Matrix<Double>(maxUserID + 1, numItemAttributes(), 0.0);
    //itemAttributeWeightByUser.init(0.0);
    for (int i = 0; i < numIter; i++)
      iterate();
  }

  /**
   * 
   * Perform one iteration of stochastic gradient ascent over the training data.
   * One iteration is <see cref="iteration_length"/> * number of entries in the training matrix.
   */
  @Override
  public void iterate() {
    int num_pos_events = feedback.size();

    for (int i = 0; i < num_pos_events * iterationLength; i++) {
      if (i % 1000000 == 999999)
        System.err.print(".");
      if (i % 100000000 == 99999999)
        System.err.println();

      // user_id u, item_id_1 i, item_id_2 j
      SampleTriple triple = new SampleTriple();
      updateFeatures(triple);
      
    }
  }

  /**
   * Sample a pair of items, given a user.
   * @param t a SampleTriple specifying a user ID
   */
  protected  void sampleItemPair(SampleTriple t) {
    if (fastSampling) {
      t.i = userPosItems[t.u].get(random.nextInt(userPosItems[t.u].size()));
      t.j = userNegItems[t.u].get(random.nextInt(userNegItems[t.u].size()));
    } else {
      IntList user_items = new IntArrayList(feedback.userMatrix().get(t.u));
      t.i = user_items.get(random.nextInt(user_items.size()));
      do
        t.j = random.nextInt(0, maxItemID + 1);
      while (feedback.userMatrix().get(t.u, t.j) || feedback.itemMatrix().get(t.j).size() == 0); // don't sample the item if it never has been viewed (maybe unknown item!)
    }
  }

  /**
   * Sample a user that has viewed at least one and not all items.
   * @return the user ID
   */
  protected int sampleUser() {
    while (true) {
      int u = random.nextInt(maxUserID + 1);
      IntCollection user_items = feedback.userMatrix().get(u);
      if (user_items.size() == 0 || user_items.size() == maxItemID + 1)
        continue;
      
      return u;
    }
  }

  /**
   * Sample a triple for BPR learning.
   * @param t the SampleTriple to configure
   */
  protected void sampleTriple(SampleTriple t) {
    t.u = sampleUser();
    sampleItemPair(t);
  }

  /**
   * Modified feature update method that exploits attribute sparsity.
   * @param t a SampleTriple specifying the user ID and the first and second item IDs
   */
  protected void updateFeatures(SampleTriple t) {
    double x_uij = predict(t.u, t.i) - predict(t.u, t.j);

    IntCollection attr_i = itemAttributes.get(t.i);
    IntCollection attr_j = itemAttributes.get(t.j);

    // Assumption: attributes are sparse
    IntSet attr_i_over_j = new IntArraySet(attr_i);
    attr_i_over_j.removeAll(attr_j);

    IntSet attr_j_over_i = new IntArraySet(attr_j);
    attr_j_over_i.removeAll(attr_i);

    double one_over_one_plus_ex = 1 / (1 + Math.exp(x_uij));

    for (int a : attr_i_over_j) {
      double w_uf = itemAttributeWeightByUser.get(t.u, a);
      double uf_update = one_over_one_plus_ex - regularization * w_uf;
      itemAttributeWeightByUser.set(t.u, a, w_uf + learnRate * uf_update);
    }
    
    for (int a : attr_j_over_i) {
      double w_uf = itemAttributeWeightByUser.get(t.u, a);
      double uf_update = -one_over_one_plus_ex - regularization * w_uf;
      itemAttributeWeightByUser.set(t.u, a, w_uf + learnRate * uf_update);
    }
  }

  /**
   * 
   */
  @Override
  public double predict(int user_id, int item_id) {
    if ((user_id < 0) || (user_id >= itemAttributeWeightByUser.dim1))
      return Double.MIN_VALUE;
    if ((item_id < 0) || (item_id > maxItemID))
      return Double.MIN_VALUE;

    double result = 0;
    for (int a : itemAttributes.get(item_id))
      result += itemAttributeWeightByUser.get(user_id, a);
    
    return result;
  }

  /**
   * @throws IOException 
   */
  @Override
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }

  @Override
  public void saveModel(PrintWriter writer) {
    IMatrixExtensions.writeMatrix(writer, itemAttributeWeightByUser);
  }

  /**
   * 
   */
  @Override
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }

  /** 
   *
   */
  @Override
  public void loadModel(BufferedReader reader) throws IOException {
    this.itemAttributeWeightByUser = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0, 0.0));
  }

  /**
   * 
   */
  @Override
  public double computeLoss() {
    return -1;
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return
        this.getClass().getName()
        + " reg=" + regularization
        + " numIter=" + numIter
        + " learnRate=" + learnRate
        + " fastSamplingMemoryLimit=" + fastSamplingMemoryLimit
        + " initMean=" + initMean
        + " initStdev=" + initStdev;
  }
  
  private class SampleTriple {
    int u;  // user_id
    int i;  // item_id positive item
    int j;  // item_id negative item
  }

}

