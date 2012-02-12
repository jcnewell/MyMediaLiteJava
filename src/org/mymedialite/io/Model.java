// Copyright (C) 2010, 2011 Zeno Gantner, Chris Newell
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

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.mymedialite.IRecommender;
import org.mymedialite.util.Recommender;

/**
 * Class containing static routines for reading and writing recommender models.
 * @version 2.03
 */
public class Model {

  // Prevent instantiation.
  private Model() {}

  /**
   * Save the model parameters of a recommender to a file.
   * 
   * Does not save if filename is an empty string.
   * 
   * @param recommender the recommender to store
   * @param filename the filename (may include relative paths)
   */
  public static void save(IRecommender recommender, String filename) throws IOException {
    if (filename == null) return;
    System.err.println("Save model to " + filename);
    recommender.saveModel(filename);
  }

  /**
   * Save the model parameters of a recommender (in a given iteration of the training) to a file.
   * 
   * Does not save if filename is an empty string.
   * 
   * @param recommender the <see cref="IRecommender"/> to save
   * @param filename the filename template
   * @param iteration the iteration (will be appended to the filename)
   */
  public static void save(IRecommender recommender, String filename, int iteration) throws IOException {
    if (filename == null) return;
    save(recommender, filename + "-it-" + iteration);
  }

  /**
   * Load the model parameters of a recommender from a file.
   * @param recommender the <see cref="IRecommender"/> to load
   * @param filename the filename template
   */
  public static void load(IRecommender recommender, String filename) throws IOException {
    System.err.println("Load model from " + filename);
    recommender.loadModel(filename);
  }

  /**
   * Load a recommender from a file, including object creation.
   * @param filename the name of the model file
   * @return the recommender loaded from the file
   */
  public static IRecommender load(String filename) throws IOException {
    IRecommender recommender;
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String type_name = reader.readLine();

    if (type_name.startsWith("org.mymedialite.ratingprediction.")) {
      recommender = Recommender.createRatingPredictor(type_name);
    } else if (type_name.startsWith("org.mymedialite.itemrec.")) {
      recommender = Recommender.createItemRecommender(type_name);
    } else {
      throw new IOException("Unknown recommender namespace: " + type_name);
    }
    recommender.loadModel(filename);
    return recommender;
  }

  /**
   * Get a reader object to read in model parameters of a recommender</summary>
   * @param filename the filename of the model file
   * @param recommenderType the expected recommender type
   * @return a BufferedReader
   */
  public static BufferedReader getReader(String filename, Class<?> recommenderType) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
    String type_name = reader.readLine();
    if (type_name == null)  throw new IOException("Unexpected end of file " + filename);
    if(!type_name.equals(recommenderType.getCanonicalName()))
      System.err.println("WARNING: Incorrect type name: " + type_name + ", expected: " + recommenderType.getCanonicalName());

    reader.readLine(); // read version line, and ignore it for now
    return reader;
  }

  /**
   * Get a writer object to save the model parameters of a recommender engine.
   * @param filename the filename of the model file
   * @param recommenderType the engine type
   * @param version the version string (for backwards compatibility)
   * @return a PrintWriter
   */
  public static PrintWriter getWriter(String filename, Class<?> recommenderType, String version) throws IOException {
    PrintWriter writer = new PrintWriter(filename);
    writer.println(recommenderType.getCanonicalName());
    writer.println(version);
    return writer;
  }

}
