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

/**
 * Weighted BPR-MF with frequency-adjusted sampling.
 * 
 * Literature:
 *    Zeno Gantner, Lucas Drumond, Christoph Freudenthaler, Lars Schmidt-Thieme:
 *    Bayesian Personalized Ranking for Non-Uniformly Sampled Items.
 *    KDD Cup Workshop 2011
 * 
 * @version 2.03
 */
public class WeightedBPRMF extends BPRMF {

  // TODO offer this data structure from Feedback
  /**
   * array of user IDs of positive user-item pairs.
   */
  protected int[] users;
  
  /**
   * array of item IDs of positive user-item pairs.
   */
  protected int[] items;

  /**
   * Default constructor.
   */
  public WeightedBPRMF() { }

  /**
   */
  public void train() {
    // Deactivate until supported
    withReplacement = false;
    
    // Deactivate until false is supported
    uniformUserSampling = true;

    // prepare helper data structures for training
    users = new int[feedback.size()];
    items = new int[feedback.size()];

    int index = 0;
    for (int user_id : feedback.userMatrix().nonEmptyRowIDs())
      for (int item_id : feedback.userMatrix().get(user_id)) {
        users[index] = user_id;
        items[index] = item_id;

        index++;
      }

    // Suppress using user_neg_items in BPRMF
    fastSamplingMemoryLimit = 0;

    super.train();
  }

  /**
   * 
   */
  protected void sampleTriple(SampleTriple t) {
    // ample user from positive user-item pairs
    int index = random.nextInt(items.length - 1);
    t.u = users[index];
    t.i = items[index];

    // Sample negative item
    do
      t.j = items[random.nextInt(items.length - 1)];
    while (feedback.userMatrix().get(t.u, t.j));
  }

  /**
   * 
   */
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
        + " initMean=" + initMean
        + " initStdev=" + initStDev ;
  }
  
  private class SampleTriple {
    int u;  // user_id
    int i;  // item_id positive item
    int j;  // item_id negative item
  } 

}

