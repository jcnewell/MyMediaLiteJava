// Copyright (C) 2010 Steffen Rendle, Zeno Gantner, Chris Newell
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

package org.mymedialite.data;

/**
 * Weighted items class.
 * @version 2.03
 */
public final class WeightedItem implements Comparable<WeightedItem> {
  
  /** Item ID */
  public int item_id;

  /** Weight */
  public Double weight;

  /** Default constructor */
  public WeightedItem() {}

  /** 
   * Constructor
   * @param item_id the item ID
   * @param weight the weight
   */
  public WeightedItem(int item_id, double weight) {
    this.item_id = item_id;
    this.weight = weight;
  }

  public int compareTo(WeightedItem otherItem) {
    return this.weight.compareTo(otherItem.weight);
  }

  public boolean equals(WeightedItem otherItem) {
    if (otherItem == null) return false;
    return Math.abs(this.weight - otherItem.weight) < 0.000001;
  }

  public int getHashCode() {
    return weight.hashCode();
  }

}