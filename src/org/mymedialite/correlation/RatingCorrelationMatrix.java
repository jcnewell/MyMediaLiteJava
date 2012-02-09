// Copyright (C) 2011 Zeno Gantner, Chris Newell
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
// You should have received a copy of the GNU General Public License
// along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.correlation;

import org.mymedialite.data.IRatings;

/**
 * CorrelationMatrix that computes correlations over rating data.
 * @version 2.03
 */
public class RatingCorrelationMatrix extends CorrelationMatrix {

	/**
	 * Constructor.
	 * @param num_entities the number of entities
	 */
	public RatingCorrelationMatrix(int num_entities) {
		super(num_entities);
	}

	/**
	 * Compute the correlations for a given entity type from a rating dataset.
	 * @param ratings the rating data
	 * @param entity_type EntityType.USER or EntityType.ITEM
	 */
  public void computeCorrelations(IRatings ratings, int entity_type) {
		throw new UnsupportedOperationException();
  }

}