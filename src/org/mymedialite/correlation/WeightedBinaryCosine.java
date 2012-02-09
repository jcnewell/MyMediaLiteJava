// Copyright (C) 2010 Steffen Rendle, Zeno Gantner
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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.correlation;

import java.util.HashSet;

import it.unimi.dsi.fastutil.ints.IntList;

import org.mymedialite.datatype.IBooleanMatrix;
import org.mymedialite.datatype.IMatrix;
import org.mymedialite.datatype.SymmetricMatrix;

/**
 * Class for weighted cosine similarities.
 * http://kddcup.yahoo.com/pdf/Track2-TheCoreTeam-Paper.pdf
 * http://en.wikipedia.org/wiki/Cosine_similarity
 * @version 2.03
 */
public final class WeightedBinaryCosine extends BinaryDataCorrelationMatrix {

  /**
   * Creates an object of type Cosine.
   * @param num_entities the number of entities
   */
  public WeightedBinaryCosine(int num_entities) {
    super(num_entities);
  }

  /**
   * Creates a Cosine similarity matrix from given data.
   * @param vectors the boolean data
   * @return the similarity matrix based on the data
   */
  public static CorrelationMatrix create(IBooleanMatrix vectors) {
    BinaryDataCorrelationMatrix cm;
    int num_entities = vectors.numberOfRows();
    try {
      cm = new WeightedBinaryCosine(num_entities);
    } catch (OutOfMemoryError e) {
      System.err.println("Too many entities: " + num_entities);
      throw e;
    }
    cm.computeCorrelations(vectors);
    return cm;
  }

  /**
   * 
   */
  public void computeCorrelations(IBooleanMatrix entity_data) {
    IBooleanMatrix transpose = (IBooleanMatrix) entity_data.transpose();

    float[] other_entity_weights = new float[transpose.numberOfRows()];
    for (int row_id = 0; row_id < transpose.numberOfRows(); row_id++) {
      int freq = transpose.getEntriesByRow(row_id).size();
      other_entity_weights[row_id] = 1f / (float) (Math.log(3 + freq) / Math.log(2));   ; // TODO make configurable
    }

    IMatrix<Float> weighted_overlap = new SymmetricMatrix<Float>(entity_data.numberOfRows());
    float[] entity_weights = new float[entity_data.numberOfRows()];

    // Go over all (other) entities
    for (int row_id = 0; row_id < transpose.numberOfRows(); row_id++) {
      IntList row = transpose.getEntriesByRow(row_id);
      for (int i = 0; i < row.size(); i++) {
        int x = row.getInt(i);
        entity_weights[x] += other_entity_weights[row_id];
        for (int j = i + 1; j < row.size(); j++) {
          int y = row.getInt(j);
          weighted_overlap.set(x, y, weighted_overlap.get(x, y) + other_entity_weights[row_id] * other_entity_weights[row_id]);
        }
      }
    }

    // The diagonal of the correlation matrix
    for (int i = 0; i < numEntities; i++)
      set(i, i, 1.0F);

    // Compute cosine
    for (int x = 0; x < numEntities; x++)
      for (int y = 0; y < x; y++)
        set(x, y, (float) (weighted_overlap.get(x, y) / Math.sqrt(entity_weights[x] * entity_weights[y])));
  }

  /**
   * Computes the cosine similarity of two binary vectors.
   * @param vector_i the first vector
   * @param vector_j the second vector
   * @return the cosine similarity between the two vectors
   */
  public static float computeCorrelation(HashSet<Integer> vector_i, HashSet<Integer> vector_j) {
    int cntr = 0;
    for (int k : vector_j)
      if (vector_i.contains(k))
        cntr++;
    return cntr / (float) Math.sqrt(vector_i.size() * vector_j.size());
  }
  
}
