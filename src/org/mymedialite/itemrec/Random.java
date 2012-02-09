//Copyright (C) 2011 Chris Newell
//
//This file is part of MyMediaLite.
//
//MyMediaLite is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//MyMediaLite is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.itemrec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.mymedialite.itemrec.ItemRecommender;

/**
 * An Item Recommender which returns random prediction values uniformly distributed between 0.0 and 1.0.
 * For use as experimental baseline.
 * @author newell
 * @version 2.03
 */
public class Random extends ItemRecommender {

  java.util.Random random;
  
  public Random() { 
    random = new java.util.Random();
  }

  @Override
  public double predict(int userId, int itemId) {
    return random.nextDouble();
  }

  @Override
  public void train() {
  }

  @Override
  public void loadModel(String filename) throws IOException {
  }
  
  @Override
  public void loadModel(BufferedReader reader) throws IOException {
  }

  @Override
  public void saveModel(String filename) throws IOException {
  }
  
  @Override
  public void saveModel(PrintWriter writer) throws IOException {
  }

}
