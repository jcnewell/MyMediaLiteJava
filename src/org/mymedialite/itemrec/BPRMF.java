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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MyMediaLite. If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.itemrec;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.*;
import java.util.*;
import org.mymedialite.datatype.*;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;
import org.mymedialite.util.*;
import org.mymedialite.util.Random;

/**
 * Matrix factorization model for item prediction (ranking) optimized using BPR.
 * 
 * BPR reduces ranking to pairwise classification.
 *
 * Literature:
 *    Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars Schmidt-Thieme:
 *    BPR: Bayesian Personalized Ranking from Implicit Feedback.
 *    UAI 2009.
 *    http://www.ismll.uni-hildesheim.de/pub/pdfs/Rendle_et_al2009-Bayesian_Personalized_Ranking.pdf
 *
 * Different sampling strategies are configurable by setting the uniformUserSampling and withReplacement accordingly.
 * To get the strategy from the original paper, set uniformUserSampling=false and withReplacement=false.
 * withReplacement=true (default) gives you usually a slightly faster convergence, and uniformUserSampling=true (default)
 * (approximately) optimizes the average AUC over all users.
 * 
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class BPRMF extends MF {

  private static final String VERSION = "2.03";

  /** Fast, but memory-intensive sampling */
  protected boolean fastSampling = false;

  /** Item bias terms */
  protected double[] itemBias;

  /** 
   * Fast sampling memory limit, in MiB
   * 
   * TODO find out why fast sampling does not improve performance
   */
  //public int fastSamplingMemoryLimit = 1024;
  public int fastSamplingMemoryLimit = 0; // 1024;

  /** Sample positive observations with (true) or without (false) replacement */
  public boolean withReplacement = false;

  /** Sample uniformly from users */
  public boolean uniformUserSampling;

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

  /** If set (default), update factors for negative sampled items during learning */
  protected boolean updateJ = true;

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
  public boolean boldDriver = false;

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

  /**
   * Default constructor.
   */
  public BPRMF() {
    uniformUserSampling = true;
  }

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
      int num_sample_triples = (int) Math.sqrt(maxUserID) * 100;
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

    for (int i = 0; i < numIter; i++)
      iterate();
  }

  /**
   * Perform one iteration of stochastic gradient ascent over the training data.
   * One iteration is iteration_length * number of entries in the training matrix
   */
  public void iterate() {
    int num_pos_events = feedback.size();

    int user_id, pos_item_id, neg_item_id;

    if (uniformUserSampling) {
      if (withReplacement) {
        // Case 1: uniform user sampling, with replacement
        IBooleanMatrix user_matrix = feedback.getUserMatrixCopy();

        for (int i = 0; i < num_pos_events; i++) {
          while (true) {
            // Sampling with replacement
            user_id = sampleUser();
            IntCollection user_items = user_matrix.get(user_id);

            // Reset user if already exhausted
            if (user_items.size() == 0)
              for (int item_id : feedback.userMatrix().get(user_id))
                user_matrix.set(user_id, item_id, true);

            pos_item_id = user_items.toIntArray()[random.nextInt(user_items.size())];
            user_matrix.set(user_id, pos_item_id, false); // temporarily forget positive observation
            do
              neg_item_id = random.nextInt(maxItemID + 1);
            while (feedback.userMatrix().get(user_id).contains(neg_item_id));
            break;
          }
          SampleTriple triple = new SampleTriple(user_id, pos_item_id, neg_item_id);
          updateFactors(triple, true, true, updateJ);
        }

      } else {
        // Case 2: uniform user sampling, without replacement
        for (int i = 0; i < num_pos_events; i++) {
          SampleTriple triple = sampleTriple();
          updateFactors(triple, true, true, true);
        }
      }

    } else {
      if (withReplacement) {
        // Case 3: uniform pair sampling, with replacement
        for (int i = 0; i < num_pos_events; i++) {
          int index = random.nextInt(num_pos_events);
          user_id = feedback.users().get(index);
          pos_item_id = feedback.items().get(index);
          neg_item_id = -1;
          SampleTriple triple = new SampleTriple(user_id, pos_item_id, neg_item_id);
          sampleOtherItem(triple);
          updateFactors(triple, true, true, updateJ);
        }

      } else {
        // Case 4: uniform pair sampling, without replacement
        for (int index : feedback.randomIndex()) {
          user_id = feedback.users().get(index);
          pos_item_id = feedback.items().get(index);
          neg_item_id = -1;
          SampleTriple triple = new SampleTriple(user_id, pos_item_id, neg_item_id);
          sampleOtherItem(triple);
          updateFactors(triple, true, true, updateJ);
        }
      }

    }

    if (boldDriver) {
      double loss = computeLoss();

      if (loss > lastLoss)
        learnRate *= 0.5;
      else if (loss < lastLoss)
        learnRate *= 1.1;
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
    boolean itemIsPositive = feedback.userMatrix().get(triple.u, triple.i);
    if (fastSampling) {
      if (itemIsPositive) {
        int rindex = random.nextInt(userNegItems.get(triple.u).length);
        triple.j = userNegItems.get(triple.u)[rindex];
      } else {
        int rindex = random.nextInt(userPosItems.get(triple.u).length);
        triple.j = userPosItems.get(triple.u)[rindex];
      }
    } else {
      do
        triple.j = random.nextInt(maxItemID + 1);
      while (feedback.userMatrix().get(triple.u, triple.j) != itemIsPositive);
    }
    return itemIsPositive;
  }

  /**
   * Sample a pair of items, given a user
   * @param triple a SampleTriple consisting of a user ID and two item IDs
   */
  protected void sampleItemPair(SampleTriple triple) {
    if (fastSampling) {
      int rindex = random.nextInt(userPosItems.get(triple.u).length);
      triple.i = userPosItems.get(triple.u)[rindex];

      rindex = random.nextInt (userNegItems.get(triple.u).length);
      triple.j = userNegItems.get(triple.u)[rindex];
    } else {
      IntCollection user_items = feedback.userMatrix().get(triple.u);
      triple.i = user_items.toIntArray()[random.nextInt(user_items.size())];
      do
        triple.j = random.nextInt (maxItemID + 1);
      //while (feedback.userMatrix().get(triple.u, triple.j));
      while (user_items.contains(triple.j));
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
   * @param t a SampleTriple specifying the user ID and the first and second itemIDs
   * @param updateU if true, update the user features
   * @param updateI if true, update the features of the first item
   * @param updateJ if true, update the features of the second item
   */
  protected void updateFactors(SampleTriple t, boolean updateU, boolean updateI, boolean updateJ) {
    double x_uij = itemBias[t.i] - itemBias[t.j] + MatrixExtensions.rowScalarProductWithRowDifference(userFactors, t.u, itemFactors,t.i, itemFactors, t.j);
    double one_over_one_plus_ex = 1 / (1 + Math.exp(x_uij));

    // Adjust bias terms.
    if (updateI) {
      double bias_update = one_over_one_plus_ex - biasReg * itemBias[t.i];
      itemBias[t.i] += learnRate * bias_update;
    }

    if (updateJ) {
      double bias_update = -one_over_one_plus_ex - biasReg * itemBias[t.j];
      itemBias[t.j] += learnRate * bias_update;
    }

    // Adjust factors.
    for (int f = 0; f < numFactors; f++) {
      double w_uf = userFactors.get(t.u, f);
      double h_if = itemFactors.get(t.i, f);
      double h_jf = itemFactors.get(t.j, f);

      if (updateU) {
        double uf_update = (h_if - h_jf) * one_over_one_plus_ex - regU * w_uf;
        userFactors.set(t.u, f, w_uf + learnRate * uf_update);
      }

      if (updateI) {
        double if_update = w_uf * one_over_one_plus_ex - regI * h_if;
        itemFactors.set(t.i, f, h_if + learnRate * if_update);
      }

      if (updateJ) {
        double jf_update = -w_uf * one_over_one_plus_ex - regJ * h_jf;
        itemFactors.set(t.j, f, h_jf + learnRate * jf_update);
      }
    }
  }

  /** {@inheritDoc} */
  public void addFeedback(int user_id, int item_id) {
    super.addFeedback(user_id, item_id);
    if (fastSampling)
      createFastSamplingData(user_id);

    // retrain
    retrainUser(user_id);
    retrainItem(item_id);
  }

  /** {@inheritDoc} */
  public void removeFeedback(int user_id, int item_id) {
    super.removeFeedback(user_id, item_id);
    if (fastSampling)
      createFastSamplingData(user_id);

    // retrain
    retrainUser(user_id);
    retrainItem(item_id);
  }

  /** {@inheritDoc} */
  public void addUser(int user_id) {
    super.addUser(user_id);

    userFactors.addRows(user_id + 1);
    MatrixExtensions.rowInitNormal(userFactors, user_id, initMean, initStDev);
  }

  /** {@inheritDoc} */
  public void addItem(int item_id) {
    super.addItem(item_id);
    itemFactors.addRows(item_id + 1);
    MatrixExtensions.rowInitNormal(itemFactors, item_id, initMean, initStDev);

    // Create new item bias array
    double[] itemBias = Arrays.copyOf(this.itemBias, item_id + 1);
    this.itemBias = itemBias;
  }

  /** {@inheritDoc} */
  public void removeUser(int user_id) {
    super.removeUser(user_id);
    if (fastSampling) {
      userPosItems.set(user_id, null);
      userNegItems.set(user_id, null);
    }
    // set user latent factors to zero
    userFactors.setRowToOneValue(user_id, 0.0);
  }

  /** {@inheritDoc} */
  public void removeItem(int item_id) {
    super.removeItem(item_id);
    // TODO remove from fast sampling data structures
    // (however: not needed if all feedback events have been removed properly before)

    // set item latent factors to zero
    itemFactors.setRowToOneValue(item_id, 0.0);
  }

  /**
   * Retrain the latent factors of a given user</summary>
   * @param user_id the user ID
   */
  protected void retrainUser(int user_id) {
    MatrixExtensions.rowInitNormal(userFactors, user_id, initMean, initStDev);
    IntCollection user_items = feedback.userMatrix().get(user_id);

    for (int i = 0; i < userFactors.data.length; i++)
      if (userFactors.data[i] == null)
        System.err.println("uf: " + i);

    for (int i = 0; i < itemFactors.data.length; i++)
      if (itemFactors.data[i] == null) {
        System.err.print( "if: " + i);
        System.err.print( " x " + (i / itemFactors.dim1));
        System.err.println(" y " + (i % itemFactors.dim1));
      }
    System.err.flush();

    for (int i = 0; i < user_items.size(); i++) {
      SampleTriple triple = new SampleTriple();
      triple.u = user_id;
      sampleItemPair(triple);
      updateFactors(triple, true, false, false);
    }
  }

  /**
   * Retrain the latent factors of a given item</summary>
   * @param item_id the item ID
   */
  protected void retrainItem(int item_id) {
    MatrixExtensions.rowInitNormal(itemFactors, item_id, initMean, initStDev);
    int num_pos_events = feedback.userMatrix().numberOfEntries();
    int num_item_iterations = num_pos_events / (maxItemID + 1);
    for (int i = 0; i < num_item_iterations; i++) {
      // remark: the item may be updated more or less frequently than in the normal from-scratch training
      SampleTriple triple = new SampleTriple();
      triple.u = sampleUser();
      triple.i = item_id;
      boolean item_is_positive = sampleOtherItem(triple);

      if (item_is_positive) {
        int j = triple.j;
        triple.j = triple.i;
        triple.i = j;
      }
      updateFactors(triple, false, false, true);
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
      complexity += regU * Math.pow(VectorExtensions.euclideanNorm(userFactors.getRow(lossSampleU[c])), 2);
      complexity += regI * Math.pow(VectorExtensions.euclideanNorm(itemFactors.getRow(lossSampleI[c])), 2);
      complexity += regJ * Math.pow(VectorExtensions.euclideanNorm(itemFactors.getRow(lossSampleJ[c])), 2);
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
      int num_test_items = feedback.userMatrix().get(user_id).size();
      if (num_test_items == 0) continue;

      List<Integer> prediction = Extensions.predictItems(this, user_id, maxItemID);

      int num_eval_items = maxItemID + 1;
      int num_eval_pairs = (num_eval_items - num_test_items) * num_test_items;

      int num_correct_pairs = 0;
      int num_pos_above = 0;
      // start with the highest weighting item...
      for (int i = 0; i < prediction.size(); i++) {
        int item_id = prediction.get(i);

        if (feedback.userMatrix().get(user_id, item_id))
          num_pos_above++;
        else
          num_correct_pairs += num_pos_above;
      }
      double user_auc = ((double)num_correct_pairs) / num_eval_pairs;
      sum_auc += user_auc;
      num_user++;
    }

    double auc = sum_auc / num_user;
    return auc;
  }

  protected void createFastSamplingData(int u) {
    while (u >= userPosItems.size())
      userPosItems.add(null);
    while (u >= userNegItems.size())
      userNegItems.add(null);

    userPosItems.set(u, feedback.userMatrix().get(u).toIntArray());

    IntSet neg_list = new IntArraySet();
    for (int i=0; i < maxItemID; i++)
      if (!feedback.userMatrix().get(u).contains(i))
        neg_list.add(i);
    userNegItems.set(u, neg_list.toIntArray());
  }

  protected void checkSampling() {
    try {
      int fast_sampling_memory_size = ((maxUserID + 1) * (maxItemID + 1) * 4) / (1024 * 1024);
      System.out.println("fast_sampling_memory_size=" + fast_sampling_memory_size);

      if (fast_sampling_memory_size <= fastSamplingMemoryLimit) {
        fastSampling = true;
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
    return itemBias[item_id] + MatrixExtensions.rowScalarProduct(userFactors, user_id, itemFactors, item_id);
  }

  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }

  public void saveModel(PrintWriter writer) {
    IMatrixExtensions.writeMatrix(writer, userFactors);
    org.mymedialite.io.VectorExtensions.writeVectorArray(writer, itemBias);
    IMatrixExtensions.writeMatrix(writer, itemFactors);
  }

  /** { @inheritDoc } */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }

  /** { @inheritDoc } */
  public void loadModel(BufferedReader reader) throws IOException {
    Matrix<Double> user_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0, null));
    double[] item_bias = org.mymedialite.io.VectorExtensions.readVectorArray(reader);
    Matrix<Double> item_factors = (Matrix<Double>) IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0, null));

    if (user_factors.numberOfColumns() != item_factors.numberOfColumns())
      throw new IOException("Number of user and item factors must match: " + user_factors.numberOfColumns() + " != " + item_factors.numberOfColumns());
    if (item_bias.length != item_factors.dim1)
      throw new IOException("Number of items must be the same for biases and factors: " + item_bias.length + " != " + item_factors.dim1);

    this.maxUserID = user_factors.numberOfRows() - 1;
    this.maxItemID = item_factors.numberOfRows() - 1;

    // Assign new model.
    if (this.numFactors != user_factors.numberOfColumns()) {
      System.err.println("Set num_factors to " + user_factors.numberOfColumns());
      this.numFactors = user_factors.numberOfColumns();
    }
    this.userFactors = user_factors;
    this.itemBias = item_bias;
    this.itemFactors = item_factors;
    random = Random.getInstance();
  }

  public String toString() {
    return
        this.getClass().getName()
        + " numFactors=" + numFactors
        + " biasReg=" + biasReg
        + " regU=" + regU
        + " regI=" + regI
        + " regJ=" + regJ
        + " numIter=" + numIter
        + " learnRate=" + learnRate
        + " boldDriver=" + boldDriver
        + " fastSamplingMemoryLimit=" + fastSamplingMemoryLimit
        + " initMean=" + initMean
        + " initStDev=" + initStDev ;
  }

  private class SampleTriple {
    int u; // user_id
    int i; // item_id positive item
    int j; // item_id negative item

    SampleTriple() { }

    SampleTriple(int u, int i, int j) {
      this.u = u;
      this.i = i;
      this.j = j;
    }
  }

}
