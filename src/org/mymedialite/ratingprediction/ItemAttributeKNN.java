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

import java.util.List;

import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.util.Memoizer;

/**
 * Attribute-aware weighted item-based kNN recommender.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class ItemAttributeKNN extends ItemKNN implements IItemAttributeAwareRecommender {

  private SparseBooleanMatrix itemAttributes;
  private int numItemAttributes;
  
  public SparseBooleanMatrix getItemAttributes() {
    return itemAttributes;
  }

  public void setItemAttributes(SparseBooleanMatrix matrix) {      
    this.itemAttributes = matrix;
    this.numItemAttributes = itemAttributes.numberOfColumns();
    this.maxItemID = Math.max(maxItemID, itemAttributes.numberOfRows() - 1);
  }

  /**
   * 
   */
  protected void retrainItem(int item_id) {
    baseline_predictor.retrainItem(item_id);
  }

  /**
   * 
   */
  public int numItemAttributes() {
    return numItemAttributes;
  }

  /**
   * 
   */
  public void train() {
    baseline_predictor.train();
    this.correlation = BinaryCosine.create(itemAttributes);
    memoizer = new Memoizer<Integer, List<Integer>>(correlation, "getPositivelyCorrelatedEntities", Integer.TYPE);
  }

  /**
   */
  public String toString() {
    return "ItemAttributeKNN k=" + (k == Integer.MAX_VALUE ? "inf" : k) + " reg_u=" + getRegU() + " reg_i=" + getRegI();
  }
  
}

