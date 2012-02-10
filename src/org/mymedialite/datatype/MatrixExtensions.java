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

package org.mymedialite.datatype;

import java.util.ArrayList;
import java.util.List;

import org.mymedialite.util.Random;

/**
 * Utilities to work with matrices
 *  * @version 2.03
 */
public class MatrixExtensions {

  /**
   * Initializes one row of a double matrix with normal distributed (Gaussian) noise.
   * @param matrix the matrix to initialize
   * @param mean the mean of the normal distribution drawn from
   * @param stdev the standard deviation of the normal distribution
   * @param row the row to be initialized
   * @version 2.03
   */
  public static void rowInitNormal(Matrix<Double> matrix, int row, double mean, double stdev) {
    Random random = Random.getInstance();
    for (int j = 0; j < matrix.dim2; j++)
      matrix.set(row, j, random.nextNormal(mean, stdev));
  }

  /**
   * Initializes one column of a double matrix with normal distributed (Gaussian) noise.
   * @param matrix the matrix to initialize
   * @param mean the mean of the normal distribution drawn from
   * @param stdev the standard deviation of the normal distribution
   * @param column the column to be initialized
   */
  public static void columnInitNormal(Matrix<Double> matrix, double mean, double stdev, int column) {
    Random random = Random.getInstance();
    for (int i = 0; i < matrix.dim1; i++)
      matrix.set(i, column, random.nextNormal(mean, stdev));
  }

  /**
   * Initializes a double matrix with normal distributed (Gaussian) noise.
   * @param matrix the matrix to initialize
   * @param mean the mean of the normal distribution drawn from
   * @param stdev the standard deviation of the normal distribution
   */
  public static void initNormal(Matrix<Double> matrix, double mean, double stdev) {
    Random random = Random.getInstance();
    for (int i = 0; i < matrix.dim1; i++)
      for (int j = 0; j < matrix.dim2; j++)
        matrix.set(i, j, random.nextNormal(mean, stdev));
  }

  /**
   * Increments the specified matrix element by a double value.
   * @param matrix The matrix
   * @param i the row
   * @param j the column
   * @param v the value
   */
  public static void inc(Matrix<Double> matrix, int i, int j, double v) {
    matrix.data[i * matrix.dim2 + j] = (Double)(matrix.data[i * matrix.dim2 + j]) + v;
  }

  /** 
   * Increment the elements in one matrix by the ones in another
   * @param matrix1 the matrix to be incremented
   * @param matrix2 the other matrix
   */
  public static void inc(Matrix<Double> matrix1, Matrix<Double> matrix2) {
    if (matrix1.dim1 != matrix2.dim1 || matrix1.dim2 != matrix2.dim2)
      throw new IllegalArgumentException("Matrix sizes do not match.");

    int dim1 = matrix1.dim1;
    int dim2 = matrix1.dim2;

    for (int x = 0; x < dim1; x++)
      for (int y = 0; y < dim2; y++)
        matrix1.data[x * dim2 + y] = (Double)matrix1.data[x * dim2 + y] + (Double)matrix2.data[x * dim2 + y];
  }

  /**
   * Compute the average value of the entries in a column of a matrix.
   * @param matrix the matrix
   * @param col the column ID
   * @return the average
   */
  public static double columnAverage(Matrix<Double> matrix, int col) {
    if (matrix.dim1 == 0)
      throw new IllegalArgumentException("Cannot compute average of 0 entries.");
    double sum = 0;
    for (int x = 0; x < matrix.dim1; x++)
      sum += (Double)matrix.data[x * matrix.dim2 + col];
    return sum / matrix.dim1;
  }

  /**
   * Compute the average value of the entries in a row of a matrix.
   * @param matrix the matrix
   * @param row the row ID
   * @return the average
   */
  public static double rowAverage(Matrix<Double> matrix, int row) {
    if (matrix.dim2 == 0)
      throw new IllegalArgumentException("Cannot compute average of 0 entries.");
    double sum = 0;
    for (int y = 0; y < matrix.dim2; y++)
      sum += (Double)matrix.data[row * matrix.dim2 + y];
    return sum / matrix.dim2;
  }

  /** 
   * Multiply all entries of a matrix with a scalar.
   * @param matrix the matrix
   * @param d the number to multiply with
   */
  public static void multiply(Matrix<Double> matrix, double d) {
    for (int x = 0; x < matrix.dim1; x++)
      for (int y = 0; y < matrix.dim2; y++)
        matrix.data[x * matrix.dim2 + y]  = (Double)matrix.data[x * matrix.dim2 + y] * d;
  }

  /** 
   * Compute the Frobenius norm (square root of the sum of squared entries) of a matrix.
   * See http://en.wikipedia.org/wiki/Matrix_norm
   * @param matrix the matrix
   * @return the Frobenius norm of the matrix
   */
  public static double frobeniusNorm(Matrix<Double> matrix) {
    double squared_entry_sum = 0;
    for (int x = 0; x < matrix.dim1 * matrix.dim2; x++)
      squared_entry_sum += Math.pow((Double)matrix.data[x], 2);
    return Math.sqrt(squared_entry_sum);
  }

  /**
   * Compute the scalar product between a vector and a row of the matrix.
   * @param matrix the matrix
   * @param i the row ID
   * @param vector the numeric vector
   * @returns the scalar product of row i and the vector
   */
  public static double rowScalarProduct(Matrix<Double> matrix, int i, List<Double> vector) {
    if (i >= matrix.dim1)
      throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + matrix.dim1);
    if (vector.size() != matrix.dim2)
      throw new IllegalArgumentException("wrong vector size: " + vector.size() + ", dim2 is " + matrix.dim2);

    Double result = 0.0;
    for (int j = 0; j < matrix.dim2; j++)
      result += (Double)(matrix.data[i * matrix.dim2 + j]) * vector.get(j);
    return result;
  }

  /**
   * Compute the scalar product between two rows of two matrices.
   * @param matrix1 the first matrix
   * @param i the first row ID
   * @param matrix2 the second matrix
   * @param j the second row ID
   * @return the scalar product of row i of matrix1 and row j of matrix2
   */
  public static Double rowScalarProduct(Matrix<Double> matrix1, int i, Matrix<Double> matrix2, int j) {
    if (i >= matrix1.dim1)
      throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + matrix1.dim1);
    if (j >= matrix2.dim1)
      throw new IllegalArgumentException("j too big: " + j + ", dim1 is " + matrix2.dim1);
    if (matrix1.dim2 != matrix2.dim2)
      throw new IllegalArgumentException("wrong row size: " + matrix1.dim2 + " vs. " + matrix2.dim2);

    Double result = 0.0;
    for (int c = 0; c < matrix1.dim2; c++)
      result += (Double)(matrix1.data[i * matrix1.dim2 + c]) * (Double)(matrix2.data[j * matrix2.dim2 + c]);
    return result;
  }

  /**
   * Compute the difference vector between two rows of two matrices.
   * @param matrix1 the first matrix
   * @param i the first row ID
   * @param matrix2 the second matrix
   * @param j the second row ID
   * @return the difference vector of row i of matrix1 and row j of matrix2
   */
  public static List<Double> rowDifference(Matrix<Double> matrix1, int i, Matrix<Double> matrix2, int j) {
    if (i >= matrix1.dim1)
      throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + matrix1.dim1);
    if (j >= matrix2.dim1)
      throw new IllegalArgumentException("wrong row size: " + matrix1.dim2 + " vs. " + matrix2.dim2);

    List<Double> result = new ArrayList<Double>(matrix1.dim2);
    for (int c = 0; c < matrix1.dim2; c++)
      result.set(c, (Double)matrix1.data[i * matrix1.dim2 + c] - (Double)matrix2.data[j * matrix2.dim2 + c]);
    return result;
  }

  /** 
   * Compute the scalar product of a matrix row with the difference vector of two other matrix rows.
   * @param matrix1 the first matrix
   * @param i the first row ID
   * @param matrix2 the second matrix
   * @param j the second row ID
   * @param matrix3 the third matrix
   * @param k the third row ID
   * @return see summary
   */
  public static double rowScalarProductWithRowDifference(Matrix<Double> matrix1, int i, Matrix<Double> matrix2, int j, Matrix<Double> matrix3, int k) {
    if (i >= matrix1.dim1)
      throw new IllegalArgumentException("i too big: " + i + ", dim1 is " + matrix1.dim1);
    if (j >= matrix2.dim1)
      throw new IllegalArgumentException("j too big: " + j + ", dim1 is " + matrix2.dim1);
    if (j >= matrix3.dim1)
      throw new IllegalArgumentException("j too big: " + k + ", dim1 is " + matrix3.dim1);
    if (matrix1.dim2 != matrix2.dim2)
      throw new IllegalArgumentException("wrong row size: " + matrix1.dim2 + " vs. " + matrix2.dim2);
    if (matrix1.dim2 != matrix3.dim2)
      throw new IllegalArgumentException("wrong row size: " + matrix1.dim2 + " vs. " + matrix3.dim2);

    double result = 0.0;
    for (int c = 0; c < matrix1.dim2; c++)
      result += (Double) matrix1.data[i * matrix1.dim2 + c] * ((Double)matrix2.data[j * matrix2.dim2 + c] - (Double)matrix3.data[k * matrix3.dim2 + c]);
    return result;
  }
}