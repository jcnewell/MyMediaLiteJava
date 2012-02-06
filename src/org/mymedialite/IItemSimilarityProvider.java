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

package org.mymedialite;

import java.util.List;

/**
 * Interface for classes that provide item similarities.
 * @version 2.03
 */
public interface IItemSimilarityProvider {

  /**
   * Get the similarity between two items.
   * @return the item similarity; higher means more similar
   * @param item_id1 the ID of the first item
   * @param item_id2 the ID of the second item
   */
  float getItemSimilarity(int item_id1, int item_id2);

  /**
   * Get the most similar items.
   * @return the items most similar to a given item
   * @param item_id the ID of the item
   * @paramvn the number of similar items to return
   */
  int[] getMostSimilarItems(int item_id, int n);

}
