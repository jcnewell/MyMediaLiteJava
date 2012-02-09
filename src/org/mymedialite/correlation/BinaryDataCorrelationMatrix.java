//Copyright (C) 2011 Zeno Gantner, Chris Newell
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

import org.mymedialite.datatype.IBooleanMatrix;

/**
 * CorrelationMatrix that computes correlations over binary data.
 * @version 2.03
 */
public class BinaryDataCorrelationMatrix extends CorrelationMatrix {

  /**
   * Constructor.
   * @param num_entities the number of entities
   */
  public BinaryDataCorrelationMatrix(int numEntities) { 
    super(numEntities);
  }

  /**
   * Compute the correlations from an implicit feedback, positive-only dataset.
   * @param entityData the implicit feedback set, rows contain the entities to correlate
   */
  public void computeCorrelations(IBooleanMatrix entityData) {
    throw new UnsupportedOperationException();
  }
}

