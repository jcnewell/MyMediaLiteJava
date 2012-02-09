//Copyright (C) 2011 Zeno Gantner, Chris Newell
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

/**
 * Constant item recommender for use as experimental baseline. Always predicts a score of zero.
 * This recommender can be used for debugging, e.g. to detect non-random orderings in item lists.
 * @version 2.03
 */
public class Zero extends ItemRecommender {

  @Override
  public void train() { /* do nothing */ }

  @Override
  public double predict(int user_id, int item_id) { 
    return 0;
  }

  @Override
  public void saveModel(String filename) { /* do nothing */ }

  @Override
  public void saveModel(PrintWriter writer) throws IOException { /* do nothing */ }

  @Override
  public void loadModel(String filename) { /* do nothing */ }

  @Override
  public void loadModel(BufferedReader reader) throws IOException { /* do nothing */ }

}