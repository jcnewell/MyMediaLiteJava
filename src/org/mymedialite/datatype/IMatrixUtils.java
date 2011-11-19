//Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/** Utilities to work with matrices. */
public class IMatrixUtils {

  /**
   * Write a matrix to a PrintWriter object.
   * @param writer a PrintWriter
   * @param matrix the matrix to write out
   */
  public static void writeMatrix(PrintWriter writer, IMatrix<?> matrix) {
    writer.println(matrix.getNumberOfRows() + " " + matrix.getNumberOfColumns());
    for (int i = 0; i < matrix.getNumberOfRows(); i++) {
      for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
        writer.println(i + " " + j + " " + matrix.get(i, j));
      }
    }
    writer.println();
  }
  
//  /**
//   * Write a matrix of doubles to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of doubles to write out
//   */
//  public static void writeMatrix(PrintWriter writer, IMatrix<Double> matrix) {
//    writer.println(matrix.getNumberOfRows() + " " + matrix.getNumberOfColumns());
//    for (int i = 0; i < matrix.getNumberOfRows(); i++) {
//      for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
//        writer.println(i + " " + j + " " + matrix.get(i, j));
//      }
//    }
//    writer.println();
//  }
//
//  /**
//   * Write a matrix of floats to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of floats to write out
//   */
//  public static void writeMatrix(PrintWriter writer, IMatrix<Float> matrix) {
//    writer.println(matrix.getNumberOfRows() + " " + matrix.getNumberOfColumns());
//    for (int i = 0; i < matrix.getNumberOfRows(); i++) {
//      for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
//        writer.println(i + " " + j + " " + matrix.get(i, j));
//      }
//    }
//    writer.println();
//  }
//
//  /**
//   * Write a matrix of integers to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of doubles to write out
//   */
//  static public void writeMatrix(PrintWriter writer, IMatrix<Integer> matrix)
//    writer.println(matrix.getNumberOfRows() + " " + matrix.getNumberOfColumns());
//    for (int i = 0; i < matrix.getNumberOfRows(); i++) {
//      for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
//        writer.println(i + " " + j + " " + matrix.get(i, j));   // .ToString(ni));
//      }
//    }
//    writer.println();
//  }

//  /**
//   * Write a sparse matrix of doubles to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of doubles to write out
//   */
//  static public void writeSparseMatrix(PrintWriter writer, SparseMatrix<Double> matrix) {
//    var ni = new NumberFormatInfo();
//    ni.numberDecimalDigits = '.';
//
//    writer.writeLine(matrix.numberOfRows + " " + matrix.numberOfColumns);
//    foreach (var index_pair in matrix.nonEmptyEntryIDs)
//    writer.writeLine(index_pair.first + " " + index_pair.second + " " + matrix[index_pair.first, index_pair.second].toString(ni));
//    writer.writeLine();
//  }

//  /**
//   * Write a sparse matrix of floats to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of floats to write out
//   */
//  static public void WriteSparseMatrix(PrintWriter writer, SparseMatrix<float> matrix)
//  {
//    var ni = new NumberFormatInfo();
//    ni.numberDecimalDigits = '.';
//
//    writer.writeLine(matrix.numberOfRows + " " + matrix.numberOfColumns);
//    foreach (var index_pair in matrix.nonEmptyEntryIDs)
//    writer.writeLine(index_pair.first + " " + index_pair.second + " " + matrix[index_pair.first, index_pair.second].toString(ni));
//    writer.writeLine();
//  }

//  /**
//   * Write a sparse matrix of integers to a PrintWriter object.
//   * @param writer a PrintWriter
//   * @param matrix the matrix of doubles to write out
//   */
//  static public void WriteSparseMatrix(PrintWriter writer, SparseMatrix<Integer> matrix)
//  {
//    writer.writeLine(matrix.numberOfRows + " " + matrix.numberOfColumns);
//    foreach (var index_pair in matrix.nonEmptyEntryIDs)
//    writer.writeLine(index_pair.first + " " + index_pair.second + " " + matrix[index_pair.first, index_pair.second].toString());
//    writer.writeLine();
//  }

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

    while ((numbers = reader.readLine().split(" ")).length == 3) {
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

    while ((numbers = reader.readLine().split(" ")).length == 3) {
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

    while ((numbers = reader.readLine().split(" ")).length == 3) {
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