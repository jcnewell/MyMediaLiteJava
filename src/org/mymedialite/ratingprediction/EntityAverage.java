package org.mymedialite.ratingprediction;

//Copyright (C) 2010 Zeno Gantner, Steffen Rendle
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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.mymedialite.io.Model;
import org.mymedialite.io.VectorExtensions;
import org.mymedialite.util.Recommender;

/**
 * Abstract class that uses an average (by entity) rating value for predictions.
 * This engine does NOT support online updates.
 * @version 2.03
 */
public abstract class EntityAverage extends IncrementalRatingPredictor {

  private static final String VERSION = "2.03";

  /** The average rating for each entity */
  protected DoubleList entity_averages = new DoubleArrayList();

  /** The global average rating (default prediction if there is no data about an entity) */
  protected double global_average = 0;

  /** 
   * Return the average rating for a given entity
   * @param index the entity index
   */
  public double get(int index) {
    if (index < entity_averages.size()) {
      return entity_averages.getDouble(index);
    } else {
      return global_average;
    }
  }

  /**
   * Train the recommender according to the given entity type
   * @param entity_ids a list of the relevant entity IDs in the training data
   * @param max_entity_id the maximum entity ID
   */
  protected void train(IntList entity_ids, int max_entity_id) {
    IntList rating_counts = new IntArrayList();
    entity_averages = new DoubleArrayList();
    
    for (int i = 0; i <= max_entity_id; i++) {
      rating_counts.add(0);
      entity_averages.add(0.0D);
    }

    for (int i = 0; i < ratings.size(); i++) {
      int entity_id = entity_ids.getInt(i);
      rating_counts.set(entity_id, rating_counts.getInt(entity_id) + 1);
      entity_averages.set(entity_id, entity_averages.getDouble(entity_id) + ratings.get(i));
    }

    global_average = ratings.average();
    
    for (int i = 0; i <= max_entity_id; i++) {
      if (rating_counts.getInt(i) != 0) {
        entity_averages.set(i, entity_averages.getDouble(i) / rating_counts.getInt(i));
      } else {
        entity_averages.set(i, global_average);
      }
    }
  }

  /**
   * Retrain the recommender according to the given entity type.
   * @param entity_id the ID of the entity to update
   * @param indices list of indices to use for retraining
   * @param entity_ids list of all entity IDs in the training data (per rating)
   */
  protected void retrain(int entity_id, IntList indices, IntList entity_ids) {
    double sum = 0;
    int count = 0;

    for (int i : indices) {
      count++;
      sum += ratings.get(i);
    }

    if (count > 0)
      entity_averages.set(entity_id,  sum / count);
    else
      entity_averages.set(entity_id, global_average);
  }

  @Override
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    saveModel(writer);
    writer.flush();
    writer.close();
  }

  @Override
  public void saveModel(PrintWriter writer)  throws IOException {
    writer.println(global_average);
    VectorExtensions.writeVector(writer, entity_averages);
    writer.flush();
    writer.close();
  }

  @Override
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    loadModel(reader);
    reader.close();
  }
  
  @Override
  public void loadModel(BufferedReader reader) throws IOException {
    this.global_average = Double.parseDouble(reader.readLine());   
    this.entity_averages = VectorExtensions.readVector(reader);
    reader.close();
  }

}
