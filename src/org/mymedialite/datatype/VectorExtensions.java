// Copyright (C) 2010 Zeno Gantner
// Copyright (C) 2011 Zeno Gantner, Chris Newell
//
// file is part of MyMediaLite.
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

import java.util.Collection;
import java.util.List;
import org.mymedialite.util.Random;

/**
 * Extensions for vector-like data.
 * @version 2.03
 */
public class VectorExtensions {
  
  // Prevent instantiation.
  private VectorExtensions() {}
  
  /**
   * Compute the Euclidean norm of a collection of doubles.
   * @param vector the vector to compute the norm for
   * @return the Euclidean norm of the vector
   */
  public static double euclideanNorm(Collection<Double> vector) {
    double sum = 0;
    for (double v : vector)
      sum += Math.pow(v, 2);
    return Math.sqrt(sum);
  }

  /**
   * Compute the Euclidean norm of an array of doubles
   * @param vector the vector to compute the norm for
   * @return the Euclidean norm of the vector
   */
  public static double euclideanNorm(double[] vector) {
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
    for (double v : vector)
      sum += Math.abs(v);
    return sum;
  }

  /**
   * Initialize a collection of doubles with values from a normal distribution.
   * @param vector the vector to initialize
   * @param mean the mean of the normal distribution
   * @param stddev the standard deviation of the normal distribution
   */
  public static void initNormal(List<Double> vector, double mean, double stddev) {  
   Random rnd = Random.getInstance();
    for (int i = 0; i < vector.size(); i++) 
      vector.set(i, rnd.nextNormal(mean, stddev));
  }
  
}
