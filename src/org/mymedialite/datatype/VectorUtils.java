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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mymedialite.util.Random;

/** Tools for vector-like data */
public class VectorUtils {
  
  /**
   * Write a Collection of doubles to a PrintWriter
   * @param writer a <see cref="StreamWriter"/>
   * @param vector a collection of double values
   */
  static public void writeVector(PrintWriter writer, Collection<Double> vector) {
    writer.println(vector.size());
    for (double v : vector) {
      writer.println(Double.toString(v));
    }
    writer.println();
  }  
  
  /**
   * Write an array of doubles to a PrintWriter
   * @param writer a <see cref="StreamWriter"/>
   * @param vector a collection of double values
   */
  static public void writeVectorArray(PrintWriter writer, double[] vector) {
    writer.println(vector.length);
    for (double v : vector) {
      writer.println(Double.toString(v));
    }
    writer.println();
  }

  /**
   * Read a List of doubles from a BufferedReader object
   * @param reader the <see cref="TextReader"/> to read from      
   * @return a collection of double values
   */
  static public List<Double> readVector(BufferedReader reader) throws IOException {
    int dim = Integer.parseInt(reader.readLine());
    ArrayList<Double> vector = new ArrayList<Double>(dim);
    for (int i = 0; i < dim; i++) {
      double v = Double.parseDouble(reader.readLine());
      vector.add(v);
    }
    return vector;
  }       
    
  /**
   * Read an array of doubles from a BufferedReader object
   * @param reader the <see cref="TextReader"/> to read from      
   * @return a collection of double values
   */
  static public double[] readVectorArray(BufferedReader reader) throws IOException {
    int dim = Integer.parseInt(reader.readLine());
    double[] vector = new double[dim];
    for (int i = 0; i < dim; i++) {
      double v = Double.parseDouble(reader.readLine());
      vector[i] = v;
    }
    return vector;
  }   
  
  /**
   * Compute the Euclidean norm of a collection of doubles
   * @param vector the vector to compute the norm for
   * @return the Euclidean norm of the vector
   */
  public static Double euclideanNorm(Double[] vector) {
      double sum = 0;
      for (double v : vector) {
        sum += Math.pow(v, 2);
      }
      return Math.sqrt(sum);
  }
  
  /**
   * Compute the Euclidean norm of a collection of doubles
   * @param vector the vector to compute the norm for
   * @return the Euclidean norm of the vector
   */
  public static Double euclideanNorm(Collection<Double> vector) {
    double sum = 0;
    for (double v : vector) {
      sum += Math.pow(v, 2);
    }
    return Math.sqrt(sum);
  }

  /**
   * Compute the L1 norm of a collection of doubles.
   * @param vector the vector to compute the norm for
   * @return the L1 norm of the vector
   */
  public static double L1Norm(Collection<Double> vector) {
    double sum = 0;
    for (double v : vector) sum += Math.abs(v);
    return sum;
  }
  
  /**
   * Initialize a collection of doubles with values from a normal distribution
   * @param vector the vector to initialize
   * @param mean the mean of the normal distribution
   * @param stdev the standard deviation of the normal distribution
   */
  public static void initNormal(List<Double> vector, double mean, double stdev) {
    Random rnd = Random.getInstance();
    for (int i = 0; i < vector.size(); i++) vector.set(i, rnd.nextNormal(mean, stdev));
  }
  
}