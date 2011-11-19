//Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mymedialite.datatype.Matrix;

/**
 * Class for computing and storing correlations and similarities.
 */
public class CorrelationMatrix extends Matrix<Float> {

  /**
   * Number of entities, e.g. users or items.
   */
  protected int num_entities;

  /**
   * @return returns true if the matrix is symmetric, which is generally the case for similarity matrices
   */
  public boolean isSymmetric() {
    return true;
  }

  /**
   * 
   * @param i
   * @param j
   * @return
   */
  public Float get(int i, int j) {
    return (Float)data[i * dim2 + j];
  }

  /**
   * 
   * @param i
   * @param j
   * @param value
   */
  public void set(int i, int j, Float value) {
    data[i * dim2 + j] = value;
    data[j * dim2 + i] = value;
  }

  /**
   * Creates a CorrelationMatrix object for a given number of entities.
   * @param num_entities number of entities
   */
  public CorrelationMatrix(int num_entities) {
    super(num_entities, num_entities);
    this.num_entities = num_entities;
  }

  /**
   * Creates a correlation matrix.
   * Gives out a useful warning if there is not enough memory
   * @param num_entities the number of entities
   * @return the correlation matrix
   */
  public static CorrelationMatrix create(int num_entities) {
    CorrelationMatrix cm;
    try {
      cm = new CorrelationMatrix(num_entities);
    } catch (OutOfMemoryError e) {
      System.err.println("Too many entities: " + num_entities);
      throw e;
    }
    return cm;
  }

  /**
   * Creates a CorrelationMatrix from the lines of a StreamReader.
   * In the first line, we expect to be the number of entities.
   * All the other lines have the format
   * <pre>
   *   EntityID1 EntityID2 Correlation
   * </pre>
   * where EntityID1 and EntityID2 are non-negative integers and Correlation is a floating point number.
   * @param reader the StreamReader to read from
   */
  public static CorrelationMatrix readCorrelationMatrix(BufferedReader reader) throws IOException {
    int num_entities = Integer.parseInt(reader.readLine());

    CorrelationMatrix cm = create(num_entities);

    // Diagonal values.
    for (int i = 0; i < num_entities; i++) cm.set(i, i, 1.0F);

    String regex = "[\t ,]";  // tab, space or commma.

    String line;
    while ((line = reader.readLine()) != null) {
      String[] numbers = line.split(regex);
      int i = Integer.parseInt(numbers[0]);
      int j = Integer.parseInt(numbers[1]);
      float c = Float.parseFloat(numbers[2]);

      if (i >= num_entities) throw new IOException("Entity ID is too big: i = " + i);
      if (j >= num_entities) throw new IOException("Entity ID is too big: j = " + j);

      cm.set(i, j, c);
    }
    return cm;
  }

  /**
   * Write out the correlations to a StreamWriter.
   * @param writer 
   * A <see cref="StreamWriter"/>
   * 
   */
  public void write(PrintWriter writer) {
    writer.println(num_entities);
    for (int i = 0; i < num_entities; i++) {
      for (int j = i + 1; j < num_entities; j++) {
        Float val = get(i, j);
        if (val != 0f) writer.println(i + " " + j + " " + val.toString());
      }
    }
  }

  /**
   * Add an entity to the CorrelationMatrix by growing it to the requested size..
   * Note that you still have to correctly compute and set the entity's correlation values
   * @param entity_id the numerical ID of the entity
   */
  public void addEntity(int entity_id)
  {
    this.grow(entity_id + 1, entity_id + 1);
  }

  /**
   * Sum up the correlations between a given entity and the entities in a collection.
   * @param entity_id the numerical ID of the entity
   * @param entities a collection containing the numerical IDs of the entities to compare to
   * @return the correlation sum
   */
  public double sumUp(int entity_id, Collection<Integer> entities) {
    if (entity_id < 0 || entity_id >= num_entities) throw new IllegalArgumentException("Invalid entity ID: " + entity_id);
    double result = 0;
    for (int entity_id2 : entities) {
      if (entity_id2 >= 0 && entity_id2 < num_entities) result += get(entity_id, entity_id2);
    }
    return result;
  }

  /**
   * Get all entities that are positively correlated to an entity, sorted by correlation.
   * @param entity_id the entity ID
   * @return a sorted list of all entities that are positively correlated to entity_id
   */
  public List<Integer> getPositivelyCorrelatedEntities(int entity_id) {
    List<Neighbor> result = new ArrayList<Neighbor>();
    for (int i = 0; i < num_entities; i++) {
      if(i != entity_id) {
        Neighbor neighbor = new Neighbor(i, get(i, entity_id));
        result.add(neighbor);
      }
    }
    Collections.sort(result);    
    List<Integer> ids = new ArrayList<Integer>(result.size());
    for(int i = 0; i <result.size() ; i++) {
      ids.add(result.get(i).id);
    }
    return ids;
  }

  /**
   * Get the k nearest neighbors of a given entity.
   * @param entity_id the numerical ID of the entity
   * @param k the neighborhood size
   * @return an array containing the numerical IDs of the k nearest neighbors
   */
  public int[] getNearestNeighbors(int entity_id, int k) {
    List<Neighbor> entities = new ArrayList<Neighbor>();
    for (int i = 0; i < num_entities; i++) {
      if(i != entity_id) {
        Neighbor neighbor = new Neighbor(i, get(i, entity_id));
        entities.add(neighbor);
      }
    }
    Collections.sort(entities);    
    int[] ids = new int[k];
    for(int i = 0; i < k; i++) {
      ids[i] = entities.get(entities.size() - 1 - i).id;
    }
    return ids;
  }
  
  final class Neighbor implements Comparable<Neighbor> {
    int id;
    Float value;
 
    Neighbor(int id, Float value) {
      this.id = id;
      this.value = value;
    }

    @Override
    public int compareTo(Neighbor o) {
      return value.compareTo(o.value);
    }
  }
  
}


