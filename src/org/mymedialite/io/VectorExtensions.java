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

package org.mymedialite.io;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mymedialite.util.Random;

/**
 * Tools for vector-like data
 * @version 2.03
 */
public class VectorExtensions {
  
  // Prevent instantiation.
  private VectorExtensions() {}
  
  /**
   * Write a Collection of doubles to a PrintWriter
   * @param writer a <see cref="StreamWriter"/>
   * @param vector a collection of double values
   */
  public static void writeVector(PrintWriter writer, Collection<?> vector) {
    writer.println(vector.size());
    for (Object v : vector) {
      writer.println(v);
    }
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
  }

  /**
   * Read a List of doubles from a BufferedReader object
   * @param reader the <see cref="TextReader"/> to read from      
   * @return a collection of double values
   */
  static public DoubleList readVector(BufferedReader reader) throws IOException {
    int dim = Integer.parseInt(reader.readLine());
    DoubleList vector = new DoubleArrayList(dim);
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
   * Read a collection of ints from a TextReader object.
   * @param reader the BufferedReader to read from
   * @return a list of int values
   * @throws IOException 
   * @throws NumberFormatException 
   */
  public static IntList readIntVector(BufferedReader reader) throws IOException {
    int dim = Integer.parseInt(reader.readLine());
    IntList vector = new IntArrayList(dim);
    for (int i = 0; i < dim; i++)
      vector.add(i, Integer.parseInt(reader.readLine()));

    return vector;
  }

}