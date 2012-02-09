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

package org.mymedialite.ratingprediction;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.util.Memoizer;

/**
 * Weighted item-based kNN with cosine similarity.
 * 
 * This recommender supports incremental updates.
 * @version 2.03
 */
public class ItemKNNCosine extends ItemKNN {

  /**
   * 
   */
  public void train() {
    baseline_predictor.train();
    correlation = BinaryCosine.create(data_item);
    memoizer = new Memoizer<Integer, List<Integer>>(correlation, "getPositivelyCorrelatedEntities", Integer.TYPE);
  }

  /**
   * 
   */
  protected void retrainItem(int item_id) {
    baseline_predictor.retrainItem(item_id);
    if (updateItems)
      for (int i = 0; i <= maxItemID; i++)
        correlation.set(item_id, i, BinaryCosine.computeCorrelation(new HashSet<Integer>(data_item.get(item_id)), new HashSet<Integer>(data_item.get(i))));
  }
  
  /**
   * 
   */
  public void loadModel(String filename) throws IOException {
    super.loadModel(filename);
    correlation = BinaryCosine.create(data_item);
    memoizer = new Memoizer<Integer, List<Integer>>(correlation, "getPositivelyCorrelatedEntities", Integer.TYPE);
  }
  
  /**
   * 
   */
  public String toString() {
    return "ItemKNNCosine k=" + (k == Integer.MAX_VALUE ? "inf" : k) + " reg_u=" + getRegU() + " reg_i=" + getRegI();
  }

}
