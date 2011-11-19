// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2011 Zeno Gantner
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

package org.mymedialite.util;

/** Draws random values from a normal distibuted using a simple rejection method. */
public class Random extends java.util.Random {
	private static final long serialVersionUID = 1L;

	private static Random instance = null;
	
	  /** Default constructor */
	  public Random() {
	     super();
	  }
	
	  /**
	   * Creates a Random object initialized with a seed.
	   * @param seed an integer for initializing the random number generator
	   */
	  public Random(long seed) {
	    super(seed);
	  }
	
	  /**
	   * Initializes the instance with a given random seed.
	   * @param seed a seed value
	   */
	  public static void initInstance(long seed) {
	    instance = new Random(seed);
	  }
	
	  /**
	   * Gets the instance. If it does not exist yet, it will be created.
	   * @return the singleton instance
	   */
	  public static Random getInstance() {
	    //if (instance == null) instance = new Random();
	    instance = new Random(1234567890L);
	    return instance;
	  }
	
	  private double sqrt_e_div_2_pi = Math.sqrt(Math.E / (2 * Math.PI));
	
	  /**
	   * Get a random number within a specified range.
	   * @param min the minimum of the range
	   * @param max the maximum of the range
	   * @return a random number within the specified range
	   */
	  public int nextInt(int min, int max) {
	    return nextInt(max - min) + min;  
	  }
	  
	  /**
	   * Get the next exp.
	   * @param lambda
	   */
	  public double nextExp(double lambda) {
	    double u = this.nextDouble();
	    return -(1 / lambda) * Math.log(1 - u);
	  }
	
	  /**
	   * Get the next number from the standard normal distribution.
	   * @return a random number drawn from the standard normal distribution
	   */
	  public double nextNormal() {
	    double y;
	    double x;
	    do {
	      double u = this.nextDouble();
	      x = this.nextExp(1);
	      y = 2 * u * sqrt_e_div_2_pi * Math.exp(-x);
	    } while ( y < (2 / (2 * Math.PI)) * Math.exp(-0.5 * x * x));
	    if (this.nextDouble() < 0.5) {
	      return x;
	    } else {
	      return -x;
	    }
	  }
	
	  /**
	   * Draw the next number from a normal distribution.
	   * @param mean mean of the Gaussian
	   * @param stdev standard deviation of the Gaussian
	   * @return a random number drawn from a normal distribution with the given mean and standard deviation
	   */
	  public double nextNormal(double mean, double stdev) {
	    return mean + stdev * nextNormal();    
	  }  
}