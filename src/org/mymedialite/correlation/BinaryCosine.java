//Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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

package org.mymedialite.correlation;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.HashSet;

import org.mymedialite.datatype.IBooleanMatrix;
import org.mymedialite.datatype.SymmetricMatrix;
import org.mymedialite.util.Memory;

/**
 * Class for storing cosine similarities.
 * http://en.wikipedia.org/wiki/Cosine_similarity
 * @version 2.03
 */
public final class BinaryCosine extends BinaryDataCorrelationMatrix {

  /**
   * Creates an object of type Cosine.
   * @param numEntities the number of entities
   */
  public BinaryCosine(int numEntities) {
    super(numEntities);
  }

  /**
   * Creates a Cosine similarity matrix from given data.
   * @param vectors the boolean data
   * @return the similarity matrix based on the data
   */
  public static CorrelationMatrix create(IBooleanMatrix vectors) {
    BinaryDataCorrelationMatrix cm;
    int numEntities = vectors.numberOfRows();
    try {
      cm = new BinaryCosine(numEntities);
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
    // If possible, save some memory
    if (entityData.numberOfColumns() > Short.MAX_VALUE)
      computeCorrelationsUIntOverlap(entityData);
    else
      computeCorrelationsUShortOverlap(entityData);
  }

  void computeCorrelationsUIntOverlap(IBooleanMatrix entityData) {
    IBooleanMatrix transpose = (IBooleanMatrix) entityData.transpose();

    SymmetricMatrix<Integer> overlap = new SymmetricMatrix<Integer>(entityData.numberOfRows(), 0);
    overlap.init(new Integer(0));
    
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

    // Compute cosine
    for (int x = 0; x < numEntities; x++)
      for (int y = 0; y < x; y++) {
        if(entityData.numEntriesByRow(x) == 0 || entityData.numEntriesByRow(y) == 0) {
          set(x, y, 0.0F);
        } else {
          set(x, y, (float)(overlap.get(x, y) / Math.sqrt(entityData.numEntriesByRow(x) * entityData.numEntriesByRow(y))));
        }
      }
  }

  void computeCorrelationsUShortOverlap(IBooleanMatrix entity_data) {
    IBooleanMatrix transpose = (IBooleanMatrix) entity_data.transpose();
    
    SymmetricMatrix<Short> overlap = new SymmetricMatrix<Short>(entity_data.numberOfRows(), new Short("0"));
    overlap.init(new Short("0"));
    
    // Go over all (other) entities
    for (int row_id = 0; row_id < transpose.numberOfRows(); row_id++) {
      IntList row = transpose.getEntriesByRow(row_id);
      for (int i = 0; i < row.size(); i++) {
        int x = row.getInt(i);
        for (int j = i + 1; j < row.size(); j++) {
          int y = row.getInt(j);
          overlap.set(x, y, (short)(overlap.get(x, y) + 1));
        }
      }
    }

    // The diagonal of the correlation matrix
    for (int i = 0; i < numEntities; i++)
      set(i, i, 1.0F);

    // Compute cosine
    for (int x = 0; x < numEntities; x++)
      for (int y = 0; y < x; y++) {
        if(entity_data.numEntriesByRow(x) == 0 || entity_data.numEntriesByRow(y) == 0) {
          set(x, y, 0.0F);
        } else {
          set(x, y, (float) (overlap.get(x, y) / Math.sqrt(entity_data.numEntriesByRow(x) * entity_data.numEntriesByRow(y))));
        }
      }
  }

  /**
   * Computes the cosine similarity of two binary vectors.
   * @param vectorI the first vector
   * @param vectorJ the second vector
   * @return the cosine similarity between the two vectors
   */
  public static float computeCorrelation(HashSet<Integer> vectorI, HashSet<Integer> vectorJ) {
    int cntr = 0;
    for (int k : vectorJ)
      if (vectorI.contains(k))
        cntr++;
    return cntr / (float) Math.sqrt(vectorI.size() * vectorJ.size());
  }
}

