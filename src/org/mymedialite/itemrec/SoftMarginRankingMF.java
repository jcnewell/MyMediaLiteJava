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

import org.mymedialite.datatype.MatrixExtensions;

/**
 * Matrix Factorization model for item prediction optimized for a soft margin (hinge) ranking loss,
 * using stochastic gradient descent (as in BPR-MF).
 *
 * Literature:
 * 
 *     Steffen Rendle:
 *     Context-Aware Ranking with Factorization Models.
 *     Studies in Computational Intelligence. Springer 2011.
 *     http://www.springer.com/engineering/computational+intelligence+and+complexity/book/978-3-642-16897-0
 *
 *     Markus Weimer, Alexandros Karatzoglou, Alex Smola:
 *     Improving Maximum Margin Matrix Factorization.
 *     Machine Learning Journal 2008.
 *
 *     Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars Schmidt-Thieme:
 *     BPR: Bayesian Personalized Ranking from Implicit Feedback.
 *     UAI 2009.
 *     http://www.ismll.uni-hildesheim.de/pub/pdfs/Rendle_et_al2009-Bayesian_Personalized_Ranking.pdf
 *
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class SoftMarginRankingMF extends BPRMF {
  
  public SoftMarginRankingMF() {
    learnRate = 0.1;
  }

  /**
   * Update latent factors according to the stochastic gradient descent update rule.
   * @param u the user ID
   * @param i the ID of the first item
   * @param j the ID of the second item
   * @param update_u if true, update the user latent factors
   * @param update_i if true, update the latent factors of the first item
   * @param update_j if true, update the latent factors of the second item
   */
  protected void updateFactors(int u, int i, int j, boolean update_u, boolean update_i, boolean update_j) {
    double x_uij = itemBias[i] - itemBias[j] + MatrixExtensions.rowScalarProductWithRowDifference(userFactors, u, itemFactors, i, itemFactors, j);

    double common_part = x_uij < 0 ? 1 : 0;

    // Adjust bias terms
    if (update_i) {
      double biasUpdate = common_part - biasReg * itemBias[i];
      itemBias[i] += learnRate * biasUpdate;
    }

    if (update_j) {
      double biasUpdate = -common_part - biasReg * itemBias[j];
      itemBias[j] += learnRate * biasUpdate;
    }

    // Adjust factors
    for (int f = 0; f < numFactors; f++) {
      double w_uf = userFactors.get(u, f);
      double h_if = itemFactors.get(i, f);
      double h_jf = itemFactors.get(j, f);

      if (update_u) {
        double uf_update = (h_if - h_jf) * common_part - regU * w_uf;
        userFactors.set(u, f, w_uf + learnRate * uf_update);
      }

      if (update_i) {
        double if_update = w_uf * common_part - regI * h_if;
        itemFactors.set(i, f, h_if + learnRate * if_update);
      }

      if (update_j) {
        double jf_update = -w_uf  * common_part - regJ * h_jf;
        itemFactors.set(j, f, h_jf + learnRate * jf_update);
      }
      
    }
  }

  /**
   * Compute approximate loss.
   * @return the approximate loss
   */
  public double computeLoss() {
    throw new UnsupportedOperationException();
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
    + " fastSamplingMemoryLimit=" + fastSamplingMemoryLimit
    + " initMean=" + initMean
    + " initStdev=" + initStdDev ;
  }
  
}
