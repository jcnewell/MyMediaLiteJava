// Copyright (C) 2011 Zeno Gantner
// Copyright (C) 2012 Zeno Gantner, Chris Newell
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

package org.mymedialite.datatype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Utilities to work with matrices.
 * @version 2.03
 */
public class SparseMatrixExtensions {
  
  // Prevent instantiation.
  private SparseMatrixExtensions() {}
  
  /**
   * Return the maximum value contained in a matrix.
   * @param m the matrix
   */
  public static int maxInteger(SparseMatrix<Integer> m) {
    int max = 0;
    for(HashMap<Integer, Integer> row : m.row_list)
       Math.max(max, Collections.max(row.values()));
    return max;
  }

  /**
   * Return the maximum value contained in a matrix.
   * @param m the matrix
   */
  public static double maxDouble(SparseMatrix<Double> m) {
    double max = 0.0D;
    for(HashMap<Integer, Double> row : m.row_list)
       Math.max(max, Collections.max(row.values()));
    return max;
  }

  /**
   * Return the maximum value contained in a matrix.
   * @param m the matrix
   */
  public static float maxFloat(SparseMatrix<Float> m) {
    float max = 0.0F;
    for(HashMap<Integer, Float> row : m.row_list)
      Math.max(max, Collections.max(row.values()));
    return max;
  }

  /**
   * Compute the Frobenius norm (square root of the sum of squared entries) of a matrix.
   * 
   * See http://en.wikipedia.org/wiki/Matrix_norm
   * 
   * @param matrix the matrix
   * @return the Frobenius norm of the matrix
   */
  public static double frobeniusNorm(SparseMatrix<Double> matrix) {
    double squared_entry_sum = 0;
    for (Pair<Integer, Integer> entry : matrix.nonEmptyEntryIDs())
      squared_entry_sum += Math.pow(matrix.row_list.get(entry.first).get(entry.second), 2);
    return Math.sqrt(squared_entry_sum);
  }
}
