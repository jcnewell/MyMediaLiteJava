// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
// Copyright (C) 2012 Chris Newell
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

package org.mymedialite.correlation;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.HashSet;

import org.mymedialite.datatype.IBooleanMatrix;
import org.mymedialite.datatype.IMatrix;
import org.mymedialite.datatype.SymmetricMatrix;

/**
 * Class for storing and computing the Jaccard index (Tanimoto coefficient).
 * The Jaccard index is often also called the Tanimoto coefficient.
 * http://en.wikipedia.org/wiki/Jaccard_index
 * @version 2.03
 */
public final class Jaccard extends BinaryDataCorrelationMatrix {

  /**
   * Creates an object of type Jaccard.
   * @param numEntities the number of entities
   */
  public Jaccard(int numEntities) {
    super(numEntities);
  }

  /**
   * Creates a Jaccard index matrix from given data.
   * @param vectors the boolean data
   * @return the similarity matrix based on the data
   */
  public static CorrelationMatrix create(IBooleanMatrix vectors) {
    BinaryDataCorrelationMatrix cm;
    int numEntities = vectors.numberOfRows();
    try {
      cm = new Jaccard(numEntities);
    } catch (OutOfMemoryError e) {
      System.err.println("Too many entities: " + numEntities);
      throw e;
    }
    cm.computeCorrelations(vectors);
    return cm;
  }

  /**
   */
  public void computeCorrelations(IBooleanMatrix entityData) {
    IBooleanMatrix transpose = (IBooleanMatrix)entityData.transpose();
    IMatrix<Integer> overlap = new SymmetricMatrix<Integer>(entityData.numberOfRows());

    // Go over all (other) entities
    for (int row_id = 0; row_id < transpose.numberOfRows(); row_id++) {
      IntList row = transpose.getEntriesByRow(row_id);
      for (int i = 0; i < row.size(); i++) {
        int x = row.getInt(i);
        for (int j = i + 1; j < row.size(); j++) {
          int y = row.getInt(j);
          overlap.set(x, y, overlap.get(x, y) + 1);
        }
      }
    }

    // The diagonal of the correlation matrix
    for (int i = 0; i < numEntities; i++)
      set(i, i, 1.0F);

    // Compute the Jaccard index
    for (int x = 0; x < numEntities; x++)
      for (int y = 0; y < x; y++)
        set(x, y,  (float)(overlap.get(x, y) / (entityData.numEntriesByRow(x) + entityData.numEntriesByRow(y) - overlap.get(x, y))));
  }

  /**
   * Computes the Jaccard index of two binary vectors.
   * @param vectorI the first vector
   * @param vectorJ the second vector
   * @return the cosine similarity between the two vectors
   */
  public static float computeCorrelation(HashSet<Integer> vectorI, HashSet<Integer> vectorJ) {
    int cntr = 0;
    for (int k : vectorJ)
      if (vectorI.contains(k))
        cntr++;
    return  cntr / (vectorI.size() + vectorJ.size() - cntr);
  }
  
}
