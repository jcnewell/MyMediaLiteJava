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

package org.mymedialite.itemrec;

import it.unimi.dsi.fastutil.ints.IntList;
import org.mymedialite.datatype.IBooleanMatrix;
import org.mymedialite.datatype.Matrix;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * Weighted matrix factorization method proposed by Hu et al. and Pan et al..
 * 
 * We use the fast learning method proposed by Hu et al. (alternating least squares),
 * and we use a global weight to penalize observed/unobserved values.
 *
 * Literature:
 *
 *     Y. Hu, Y. Koren, C. Volinsky: Collaborative filtering for implicit feedback datasets.
 *     ICDM 2008.
 *     http://research.yahoo.net/files/HuKorenVolinsky-ICDM08.pdf
 *
 *     R. Pan, Y. Zhou, B. Cao, N. N. Liu, R. M. Lukose, M. Scholz, Q. Yang:
 *     One-class collaborative filtering,
 *     ICDM 2008.
 *     http://www.hpl.hp.com/techreports/2008/HPL-2008-48R1.pdf
 *
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class WRMF extends MF {

  /**
   * C position: the weight/confidence that is put on positive observations.
   * The alpha value in Hu et al.
   */
  public double cPos = 1;

  /**
   * Regularization parameter.
   */
  public double regularization = 0.015;

  /**
   * 
   */
  public WRMF() {
    numIter = 15;
  }

  /**
   * 
   */
  public void iterate() {
    // Perform alternating parameter fitting
    optimize(feedback.userMatrix(), userFactors, itemFactors);
    optimize(feedback.itemMatrix(), itemFactors, userFactors);
  }

  /**
   * Optimizes the specified data.
   * @param data data
   * @param W W
   * @param H H
   */
  protected void optimize(IBooleanMatrix data, Matrix<Double> W, Matrix<Double> H) {
    Matrix<Double> HH          = new Matrix<Double>(numFactors, numFactors);
    Matrix<Double> HC_minus_IH = new Matrix<Double>(numFactors, numFactors);
    double[] HCp         = new double[numFactors];

    DenseDoubleMatrix2D m = new DenseDoubleMatrix2D(numFactors, numFactors);

    // Source code comments are in terms of computing the user factors
    // Works the same with users and items exchanged

    // (1) Create HH in O(f^2|Items|)
    // HH is symmetric
    for (int f_1 = 0; f_1 < numFactors; f_1++)
      for (int f_2 = 0; f_2 < numFactors; f_2++) {
        double d = 0;
        for (int i = 0; i < H.dim1; i++)
          d += H.get(i, f_1) * H.get(i, f_2);
        
        HH.set(f_1, f_2, d);
      }
    
    // (2) Optimize all U
    // HC_minus_IH is symmetric
    for (int u = 0; u < W.dim1; u++) {
      IntList row = data.getEntriesByRow(u);
      
      // Create HC_minus_IH in O(f^2|S_u|)
      for (int f_1 = 0; f_1 < numFactors; f_1++)
        for (int f_2 = 0; f_2 < numFactors; f_2++) {
          double d = 0;
          for (int i : row)
            //d += H.get(i, f_1) * H.get(i, f_2) * (c_pos - 1);
            d += H.get(i, f_1) * H.get(i, f_2) * cPos;
        
          HC_minus_IH.set(f_1, f_2, d);
        }
      
      // Create HCp in O(f|S_u|)
      for (int f = 0; f < numFactors; f++) {
        double d = 0;
        for (int i : row)
          //d += H.get(i, f) * c_pos;
          d += H.get(i, f) * (1 + cPos);
        
        HCp[f] = d;
      }
      
      // Create m = HH + HC_minus_IH + reg*I
      // m is symmetric
      // The inverse m_inv is symmetric
      for (int f_1 = 0; f_1 < numFactors; f_1++)
        for (int f_2 = 0; f_2 < numFactors; f_2++) {
          double d = HH.get(f_1, f_2) + HC_minus_IH.get(f_1, f_2);
          if (f_1 == f_2)
            d += regularization;
        
          m.set(f_1, f_2, d);
        }
      
      DoubleMatrix2D m_inv = Algebra.DEFAULT.inverse(m);
      // Write back optimal W
      for (int f = 0; f < numFactors; f++) {
        double d = 0;
        for (int f_2 = 0; f_2 < numFactors; f_2++)
          d += m_inv.get(f, f_2) * HCp[f_2];
       
        W.set(u, f, d);
      }
    }
  }

  /**
   * 
   */
  public double computeLoss() {
    return -1;
  }

  /**
   * 
   */
  public String toString() {
    return
        "WRMF numFactors=" + numFactors +
        " regularization=" + regularization +
        " cPos="           + cPos +
        " numIter="        + numIter +
        " initMean="       + initMean +
        " initStdDev="     + initStdDev;
  }
  
}
