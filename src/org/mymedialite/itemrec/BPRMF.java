// Copyright (C) 2010 Zeno Gantner, Christoph Freudenthaler
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
import java.util.ArrayList;
import java.util.List;
import org.mymedialite.datatype.IMatrixUtils;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.MatrixUtils;
import org.mymedialite.datatype.VectorUtils;
import org.mymedialite.util.IntHashSet;
import org.mymedialite.util.Random;
import org.mymedialite.util.Recommender;

/**
 * Matrix factorization model for item prediction (ranking) optimized using BPR-Opt.
 * BPR reduces ranking to pairwise classification.
 *
 * Literature:
 *    Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars Schmidt-Thieme:
 *    BPR: Bayesian Personalized Ranking from Implicit Feedback.
 *    UAI 2009.
 *    http://www.ismll.uni-hildesheim.de/pub/pdfs/Rendle_et_al2009-Bayesian_Personalized_Ranking.pdf
 *
 * This recommender supports incremental updates.
 */
public class BPRMF extends MF {
  
  /** Fast, but memory-intensive sampling */
  protected boolean fast_sampling = false;

  /** Item bias terms */
  protected double[] itemBias;
  
  /** Fast sampling memory limit, in MiB */
  public int fastSamplingMemoryLimit = 128;
  
  /** Regularization parameter for the bias term */
  public double biasReg;

  /** Learning rate alpha */
  public double learnRate = 0.05;

  /** Regularization parameter for user factors */
  public double regU = 0.0025;
  
  /** Regularization parameter for positive item factors */
  public double regI = 0.0025;

  /** Regularization parameter for negative item factors */
  public double regJ = 0.00025;

  /** Support data structure for fast sampling */
  protected ArrayList<int[]> userPosItems;
  
  /** Support data structure for fast sampling */
  protected ArrayList<int[]> userNegItems;

  /**
   * Use bold driver heuristics for learning rate adaption.
   * See
   * Rainer Gemulla, Peter J. Haas, Erik Nijkamp, Yannis Sismanis:
   * Large-Scale Matrix Factorization with Distributed Stochastic Gradient Descent
   * 2011
   */
  public boolean boldDriver;
  
  /**
   * Loss for the last iteration, used by bold driver heuristics.
   */
  double lastLoss = Double.NEGATIVE_INFINITY;

  /**
   * Array of user components of triples to use for approximate loss computation.
   */
  int[] lossSampleU;
  
  /**
   * Array of positive item components of triples to use for approximate loss computation.
   */
  int[] lossSampleI;
  
  /**
   * Array of negative item components of triples to use for approximate loss computation.
   */
  int[] lossSampleJ;
  
  /** Random number generator */
  protected org.mymedialite.util.Random random = Random.getInstance();
  
  protected void initModel() {
    super.initModel();
    itemBias = new double[maxItemID + 1];
  }
  
  /** {@inheritDoc} */
  public void train() {

    initModel();

    checkSampling();

    random = Random.getInstance();

    if (boldDriver) {
      int num_sample_triples = (int) Math.sqrt(feedback.getMaxUserID()) * 100;         // TODO make configurable
      System.err.println("loss_num_sample_triples=" + num_sample_triples);

      // create the sample to estimate loss from
      lossSampleU = new int[num_sample_triples];
      lossSampleI = new int[num_sample_triples];
      lossSampleJ = new int[num_sample_triples];

      for (int c = 0; c < num_sample_triples; c++) {
        SampleTriple triple = sampleTriple();  
        lossSampleU[c] = triple.u;
        lossSampleI[c] = triple.i;
        lossSampleJ[c] = triple.j;
      }

      lastLoss = computeLoss();
    }

    for (int i = 0; i < numIter; i++) iterate();
  }
  
  /** 
   * Perform one iteration of stochastic gradient ascent over the training data.
   * One iteration is iteration_length * number of entries in the training matrix
   */
  public void iterate() {
    int num_pos_events = feedback.size();
  
    for (int i=0; i<num_pos_events; i++) {
      SampleTriple triple = sampleTriple();      
      updateFactors(triple.u, triple.i, triple.j, true, true, true);
    }
    
    if (boldDriver) {
      double loss = computeLoss();

      if (loss > lastLoss) {
        learnRate *= 0.5;
      } else if (loss < lastLoss) {
        learnRate *= 1.1;
      }
      lastLoss = loss;

      System.err.println("loss: " + loss + " learnRate: " + learnRate);
    }
    
  }
 
  /** 
   * Sample another item, given the first one and the user
   * @param triple a SampleTriple consisting of a user ID and two item IDs
   * @return true if the given item was already seen by the user
   */  
  protected boolean sampleOtherItem(SampleTriple triple) {
    boolean itemIsPositive = feedback.getUserMatrix().get(triple.u, triple.i);
    if (fast_sampling) {
      if (itemIsPositive) {
        int rindex = random.nextInt(userNegItems.get(triple.u).length);
        triple.j = userNegItems.get(triple.u)[rindex];
      } else {
        int rindex = random.nextInt(userPosItems.get(triple.u).length);
        triple.j = userPosItems.get(triple.u)[rindex];
      }
    } else {
      do {
        triple.j = random.nextInt(maxItemID + 1);
      } while (feedback.getUserMatrix().get(triple.u, triple.j) != itemIsPositive);
    }
    return itemIsPositive;
  }
 
  /**  
   * Sample a pair of items, given a user
   * @param triple a SampleTriple consisting of a user ID and two item IDs
   */
  protected void sampleItemPair(SampleTriple triple) {
    if (fast_sampling) {
      int rindex = random.nextInt(userPosItems.get(triple.u).length);
      triple.i = userPosItems.get(triple.u)[rindex];

      rindex = random.nextInt (userNegItems.get(triple.u).length);
      triple.j = userNegItems.get(triple.u)[rindex];
    } else {
      IntHashSet user_items = feedback.getUserMatrix().getRow(triple.u);
      triple.i = user_items.get(random.nextInt (user_items.size()));
      do {
        triple.j = random.nextInt (maxItemID + 1);
      } while (feedback.getUserMatrix().get(triple.u, triple.j));
    }
  }
  
  /**  
   * Sample a user that has viewed at least one and not all items.
   * @return the user ID
   */
  protected int sampleUser() {
    while (true) {
      int u = random.nextInt(maxUserID + 1);
      IntHashSet user_items = feedback.getUserMatrix().getRow(u);
      if (user_items.size() == 0 || user_items.size() == maxItemID + 1) continue;
      return u;
    }
  }
  
  /**
   * Sample a triple for BPR learning.
   * @return a SampleTriple consisting of a user ID and two item IDs
   */
  protected SampleTriple sampleTriple() {
    SampleTriple triple = new SampleTriple();
    triple.u = sampleUser();
    sampleItemPair(triple);
    return triple;
  }
  
  /** 
   * Update features according to the stochastic gradient descent update rule.
   * @param u the user ID
   * @param i the ID of the first item
   * @param j the ID of the second item
   * @param update_u if true, update the user features
   * @param update_i if true, update the features of the first item
   * @param update_j if true, update the features of the second item
   */
  protected void updateFactors(int u, int i, int j, boolean update_u, boolean update_i, boolean update_j) {

    double x_uij = itemBias[i] - itemBias[j] + MatrixUtils.rowScalarProductWithRowDifference(user_factors, u, item_factors, i, item_factors, j);
    double one_over_one_plus_ex = 1 / (1 + Math.exp(x_uij));

    // Adjust bias terms.
    if (update_i) {
      double bias_update = one_over_one_plus_ex - biasReg * itemBias[i];
      itemBias[i] += learnRate * bias_update;
    }

    if (update_j) {
      double bias_update = -one_over_one_plus_ex - biasReg * itemBias[j];
      itemBias[j] += learnRate * bias_update;
    }

    // Adjust factors.
    for (int f = 0; f < numFactors; f++) {
      
      double w_uf = user_factors.get(u, f);
      double h_if = item_factors.get(i, f);
      double h_jf = item_factors.get(j, f);

      if (update_u) {
        double uf_update = (h_if - h_jf) * one_over_one_plus_ex - regU * w_uf;
        user_factors.set(u, f, w_uf + learnRate * uf_update);
      }
      
      if (update_i) {
        double if_update = w_uf * one_over_one_plus_ex - regI * h_if;
        item_factors.set(i, f, h_if + learnRate * if_update);
      }

      if (update_j) {
        double jf_update = -w_uf  * one_over_one_plus_ex - regJ * h_jf;
        item_factors.set(j, f, h_jf + learnRate * jf_update);
      }
    }
  }

  /** {@inheritDoc} */
  public void addFeedback(int user_id, int item_id) {
    super.addFeedback(user_id, item_id);
    if (fast_sampling)  createFastSamplingData(user_id);
    // retrain
    retrainUser(user_id);
    //retrainItem(item_id);
  }

  /** {@inheritDoc} */
  public void addFeedback(int user_id, List<Integer> item_ids) {
    for(int item_id : item_ids) { 
      super.addFeedback(user_id, item_id);
    }
    if (fast_sampling)  createFastSamplingData(user_id);
    // retrain
    retrainUser(user_id);
    //retrainItem(item_id);
  }
  
  /** {@inheritDoc} */
  public void removeFeedback(int user_id, int item_id) {
    super.removeFeedback(user_id, item_id);
    if (fast_sampling)  createFastSamplingData(user_id);
    // retrain
    retrainUser(user_id);
    //retrainItem(item_id);
  }

  /** {@inheritDoc} */
  public void addUser(int user_id) {
    super.addUser(user_id);
    user_factors.addRows(user_id + 1);
    MatrixUtils.rowInitNormal(user_factors, initMean, initStdev, user_id);
  }

  /** {@inheritDoc} */
  public void addItem(int item_id) {
    super.addItem(item_id);
    item_factors.addRows(item_id + 1);
    MatrixUtils.rowInitNormal(item_factors, initMean, initStdev, item_id);
  }

  /** {@inheritDoc} */
  public void removeUser(int user_id) {
    super.removeUser(user_id);
    if (fast_sampling) {
      userPosItems.set(user_id, null);
      userNegItems.set(user_id, null);
    }
    // set user latent factors to zero
    user_factors.setRowToOneValue(user_id, 0.0);
  }

  /** {@inheritDoc} */
  public void removeItem(int item_id) {
    super.removeItem(item_id);
    // TODO remove from fast sampling data structures
    //   (however: not needed if all feedback events have been removed properly before)

    // set item latent factors to zero
    item_factors.setRowToOneValue(item_id, 0.0);
  }

  /**
   * Retrain the latent factors of a given user</summary>
   * @param user_id the user ID
   */
  protected void retrainUser(int user_id) {
    MatrixUtils.rowInitNormal(user_factors, initMean, initStdev, user_id);
    IntHashSet user_items = feedback.getUserMatrix().getRow(user_id);
    for (int i = 0; i < user_items.size(); i++) {
      SampleTriple triple = new SampleTriple();
      triple.u = user_id;
      sampleItemPair(triple);
      updateFactors(triple.u, triple.i, triple.j, true, false, false);
    }
  }

  /**
   * Retrain the latent factors of a given item</summary>
   * @param item_id the item ID
   */
  protected void retrainItem(int item_id) {
    MatrixUtils.rowInitNormal(item_factors, initMean, initStdev, item_id);
    int num_pos_events = feedback.getUserMatrix().getNumberOfEntries();
    int num_item_iterations =  num_pos_events  / (maxItemID + 1);
    for (int i = 0; i < num_item_iterations; i++) {
      // remark: the item may be updated more or less frequently than in the normal from-scratch training
      int user_id = sampleUser();
      SampleTriple triple = new SampleTriple();
      triple.u = sampleUser();
      triple.i = item_id;
      boolean item_is_positive = sampleOtherItem(triple);

      if (item_is_positive) {
        updateFactors(user_id, item_id, triple.j, false, true, false);
      } else {
        updateFactors(user_id, triple.j, item_id, false, false, true);
      }
    }
  }
  
  /**
   * Compute approximate loss.
   * @return the approximate loss
   */
  public double computeLoss() {
    double ranking_loss = 0;
    for (int c = 0; c < lossSampleU.length; c++) {
      double x_uij = predict(lossSampleU[c], lossSampleI[c]) - predict(lossSampleU[c], lossSampleJ[c]);
      ranking_loss += 1 / (1 + Math.exp(x_uij));
    }

    double complexity = 0;
    for (int c = 0; c < lossSampleU.length; c++) {
      complexity += regU * Math.pow(VectorUtils.euclideanNorm(user_factors.getRow(lossSampleU[c])), 2);
      complexity += regI * Math.pow(VectorUtils.euclideanNorm(item_factors.getRow(lossSampleI[c])), 2);
      complexity += regJ * Math.pow(VectorUtils.euclideanNorm(item_factors.getRow(lossSampleJ[c])), 2);
      complexity += biasReg * Math.pow(itemBias[lossSampleI[c]], 2);
      complexity += biasReg * Math.pow(itemBias[lossSampleJ[c]], 2);
    }

    return ranking_loss + 0.5 * complexity;
  }
  
  /**  
   * Compute the fit (AUC on training data)
   * @return the fit
   */
  public double computeFit() {
    double sum_auc = 0;
    int num_user = 0;

    for (int user_id = 0; user_id < maxUserID + 1; user_id++) {
 
      int num_test_items = feedback.getUserMatrix().getRow(user_id).size();
      if (num_test_items == 0) continue;
      
      int[] prediction = Prediction.predictItems(this, user_id, maxItemID);

      int num_eval_items = maxItemID + 1;
      int num_eval_pairs = (num_eval_items - num_test_items) * num_test_items;

      int num_correct_pairs = 0;
      int num_pos_above = 0;
      // start with the highest weighting item...
      for (int i = 0; i < prediction.length; i++) {
        int item_id = prediction[i];

        if (feedback.getUserMatrix().get(user_id, item_id)) {
          num_pos_above++;
        } else {
          num_correct_pairs += num_pos_above;
        }
      }
      double user_auc = ((double)num_correct_pairs) / num_eval_pairs;
      sum_auc += user_auc;
      num_user++;
    }

    double auc = sum_auc / num_user;
    return auc;
  }

  protected void createFastSamplingData(int u) {
    while (u >= userPosItems.size()) userPosItems.add(null);
    while (u >= userNegItems.size()) userNegItems.add(null);
      
    userPosItems.set(u, feedback.getUserMatrix().getRow(u).values());
    
    IntHashSet neg_list = new IntHashSet();    
    for (int i=0; i < maxItemID; i++) {
      if (!feedback.getUserMatrix().getRow(u).contains(i)) neg_list.add(i);
    }
    userNegItems.set(u, neg_list.values());
  }

  protected void checkSampling() {
    try {
      int fast_sampling_memory_size = ((maxUserID + 1) * (maxItemID + 1) * 4) / (1024 * 1024);
      System.out.println("fast_sampling_memory_size=" + fast_sampling_memory_size);
      
      if (fast_sampling_memory_size <= fastSamplingMemoryLimit) {
        fast_sampling = true;
        this.userPosItems = new ArrayList<int[]>(maxUserID + 1);
        this.userNegItems = new ArrayList<int[]>(maxUserID + 1);
        for (int u = 0; u < maxUserID + 1; u++)
          createFastSamplingData(u);
      }
    } catch (Exception e) {
      System.out.println("fast_sampling_memory_size=TOO_MUCH");
      // Do nothing - don't use fast sampling
    }
  }
  
  public double predict(int user_id, int item_id) {
    return itemBias[item_id] + MatrixUtils.rowScalarProduct(user_factors, user_id, item_factors, item_id);
  }

  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Recommender.getWriter(filename, this.getClass());
    saveModel(writer);
  }
  
  public void saveModel(PrintWriter writer) {
      IMatrixUtils.writeMatrix(writer, user_factors);
      VectorUtils.writeVectorArray(writer, itemBias);
      IMatrixUtils.writeMatrix(writer, item_factors);
  }

  /** { @inheritDoc } */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Recommender.getReader(filename, this.getClass());
    loadModel(reader);
  }
  
  /** { @inheritDoc } */
  public void loadModel(BufferedReader reader) throws IOException {
    Matrix<Double> user_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    double[] item_bias = VectorUtils.readVectorArray(reader);
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixUtils.readDoubleMatrix(reader, new Matrix<Double>(0, 0));

    if (user_factors.getNumberOfColumns() != item_factors.getNumberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.getNumberOfColumns() + " != " + item_factors.getNumberOfColumns());
    if (item_bias.length != item_factors.dim1)
      throw new IOException("Number of items must be the same for biases and factors: " + item_bias.length + " != " + item_factors.dim1);

    this.maxUserID = user_factors.getNumberOfRows() - 1;
    this.maxItemID = item_factors.getNumberOfRows() - 1;

    // Assign new model.
    if (this.numFactors != user_factors.getNumberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.getNumberOfColumns());
      this.numFactors = user_factors.getNumberOfColumns();
    }
    this.user_factors = user_factors;
    this.itemBias    = item_bias;
    this.item_factors = item_factors;
    random = Random.getInstance();
  }

  public String toString() {
    return this.getClass().getName() +
    " numFactors=" + numFactors +
    " biasReg=" + biasReg + 
    " regU=" + regU +
    " regI=" + regI +
    " regJ=" + regJ +
    " numIter=" + numIter +
    " learnRate=" + learnRate +
    " boldDriver=" + boldDriver +
    " fastSamplingMemoryLimit=" + fastSamplingMemoryLimit +
    " initMean=" + initMean +
    " initStdev=" + initStdev ;
  }
  
  private class SampleTriple {
    int u;  // user_id
    int i;  // item_id_1
    int j;  // item_id_2
  }

}