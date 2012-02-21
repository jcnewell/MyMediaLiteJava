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

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.mymedialite.datatype.IMatrix;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.datatype.Pair;
import org.mymedialite.datatype.SparseMatrix;

/**
 * Utilities to work with matrices.
 * @version 2.03
 */
public class IMatrixExtensions {

  /**
   * Write a matrix to a PrintWriter object.
   * @param writer a PrintWriter
   * @param matrix the matrix to write out
   */
  public static void writeMatrix(PrintWriter writer, IMatrix<?> matrix) {
    writer.println(matrix.numberOfRows() + " " + matrix.numberOfColumns());
    for (int i = 0; i < matrix.numberOfRows(); i++) {
      for (int j = 0; j < matrix.numberOfColumns(); j++) {
        writer.println(i + " " + j + " " + matrix.get(i, j));
      }
    }
  }
  
  /**
   * Write a sparse matrix of doubles to a PrintWriter object.
   * @param writer a PrintWriter
   * @param matrix the matrix of doubles to write out
   */
  public static void writeSparseMatrix(PrintWriter writer, SparseMatrix<?> matrix) {
    writer.println(matrix.numberOfRows() + " " + matrix.numberOfColumns());
    for (Pair<Integer, Integer> index_pair : matrix.nonEmptyEntryIDs())
      writer.println(index_pair.first + " " + index_pair.second + " " + matrix.get(index_pair.first, index_pair.second));

  }

  /**
   * Read a matrix from a BufferedReader object.
   * @param reader the BufferedReader object to read from
   * @param example_matrix matrix of the type of matrix to create
   * @return a matrix of doubles
   */
  public static IMatrix<Double> readDoubleMatrix(BufferedReader reader, IMatrix<Double> example_matrix) throws IOException {
    String[] numbers = reader.readLine().split(" ");
    int dim1 = Integer.parseInt(numbers[0]);
    int dim2 = Integer.parseInt(numbers[1]);
    IMatrix<Double> matrix = example_matrix.createMatrix(dim1, dim2);

    int length = dim1 * dim2;
    for (int n = 0; n < length; n++) {
      String line = reader.readLine();
      numbers = line.split(" ");
      if(numbers.length != 3)
        throw new IOException("Expected three fields: " + line);
      
      int i = Integer.parseInt(numbers[0]);
      int j = Integer.parseInt(numbers[1]);
      double v = Double.parseDouble(numbers[2]);

      if (i >= dim1)
        throw new IOException("i = " + i + " >= " + dim1);
      if (j >= dim2)
        throw new IOException("j = " + j + " >= " + dim2);

       matrix.set(i, j, v);
    }
    return matrix;
  }

  /**
   * Read a matrix from a BufferedReader object.
   * @param reader the BufferedReader object to read from
   * @param example_matrix matrix of the type of matrix to create
   * @return a matrix of float
   */
  public static IMatrix<Float> readFloatMatrix(BufferedReader reader, IMatrix<Float> example_matrix) throws IOException  {
    String[] numbers = reader.readLine().split(" ");
    int dim1 = Integer.parseInt(numbers[0]);
    int dim2 = Integer.parseInt(numbers[1]);
    IMatrix<Float> matrix = example_matrix.createMatrix(dim1, dim2);

    int length = dim1 * dim2;
    for (int n = 0; n < length; n++) {
      String line = reader.readLine();
      numbers = line.split(" ");
      if(numbers.length != 3)
        throw new IOException("Expected three fields: " + line);
      
      int i = Integer.parseInt(numbers[0]);
      int j = Integer.parseInt(numbers[1]);
      Float v = Float.parseFloat(numbers[2]);

      if (i >= dim1)
        throw new IOException("i = " + i + " >= " + dim1);
      if (j >= dim2)
        throw new IOException("j = " + j + " >= " + dim2);

       matrix.set(i, j, v);
    }
    return matrix;
  }

  /**
   * Read a matrix of integers from a BufferedReader object.
   * @param reader the BufferedReader object to read from
   * @param example_matrix matrix of the type of matrix to create
   * @return a matrix of integers
   */
  static public IMatrix<Integer> readIntegerMatrix(BufferedReader reader, IMatrix<Integer> example_matrix) throws IOException {
    String[] numbers = reader.readLine().split(" ");
    int dim1 = Integer.parseInt(numbers[0]);
    int dim2 = Integer.parseInt(numbers[1]);
    IMatrix<Integer> matrix = example_matrix.createMatrix(dim1, dim2);

    int length = dim1 * dim2;
    for (int n = 0; n < length; n++) {
      String line = reader.readLine();
      numbers = line.split(" ");
      if(numbers.length != 3)
        throw new IOException("Expected three fields: " + line);

      int i = Integer.parseInt(numbers[0]);
      int j = Integer.parseInt(numbers[1]);
      Integer v = Integer.parseInt(numbers[2]);

      if (i >= dim1)
        throw new IOException("i = " + i + " >= " + dim1);
      if (j >= dim2)
        throw new IOException("j = " + j + " >= " + dim2);

       matrix.set(i, j, v);
    }
    return matrix;
  }

}