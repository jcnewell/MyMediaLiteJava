//Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

import java.lang.annotation.Inherited;

import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * k-nearest neighbor item-based collaborative filtering using cosine-similarity over the item attibutes.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class ItemAttributeKNN extends ItemKNN implements IItemAttributeAwareRecommender {

  private SparseBooleanMatrix itemAttributes;

  /**
   * 
   */
  @Override
  public SparseBooleanMatrix getItemAttributes() {
    return itemAttributes;
  }

  @Override
  public void setItemAttributes(SparseBooleanMatrix itemAttributes) {
    this.itemAttributes = itemAttributes;
    this.maxItemID = Math.max(maxItemID, itemAttributes.numberOfRows() - 1);
  }

  /**
   * 
   */
  @Override
  public int numItemAttributes() {
    return itemAttributes.numberOfColumns();
  }

  /**
   * 
   */
  @Override
  public void train() {
    this.correlation = BinaryCosine.create(itemAttributes);

    int num_items = maxItemID + 1;
    this.nearest_neighbors = new int[num_items][];
    for (int i = 0; i < num_items; i++)
      nearest_neighbors[i] = correlation.getNearestNeighbors(i, k);
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return "ItemAttributeKNN k=" + (k == Integer.MAX_VALUE ? "inf" : Integer.toString(k));
  }
  
}

