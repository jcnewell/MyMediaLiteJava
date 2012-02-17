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

package org.mymedialite.ratingprediction;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.mymedialite.IIterativeModel;
import org.mymedialite.datatype.IMatrix;
import org.mymedialite.datatype.Matrix;
import org.mymedialite.eval.Ratings;
import org.mymedialite.io.IMatrixExtensions;
import org.mymedialite.io.Model;
import org.mymedialite.io.VectorExtensions;
import org.mymedialite.util.Random;

/**
 * Co-clustering for rating prediction.
 * 
 * Literature:
 *
 *     Thomas George, Srujana Merugu
 *     A Scalable Collaborative Filtering Framework based on Co-clustering.
 *     ICDM 2005.
 *     http://hercules.ece.utexas.edu/~srujana/papers/icdm05.pdf
 *
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class CoClustering extends RatingPredictor implements IIterativeModel {

  private static final String VERSION = "2.03";

  /**
   * Random number generator.
   */
  org.mymedialite.util.Random random;

  IntList user_clustering;
  IntList item_clustering;

  DoubleList user_averages;
  DoubleList item_averages;
  IntList user_counts;
  IntList item_counts;

  DoubleList user_cluster_averages;
  DoubleList item_cluster_averages;
  IMatrix<Double> cocluster_averages;

  double global_average;

  /**
   * The number of user clusters.
   */
  public int numUserClusters;

  /**
   * The number of item clusters.
   */
  public int numItemClusters;

  /**
   * The maximum number of iterations.
   * If the algorithm converges to a stable solution, it will terminate earlier.
   */
  public int numIter;

  /**
   * Default constructor.
   */
  public CoClustering() {
    numUserClusters = 3;
    numItemClusters = 3;
    numIter = 30;
  }

  @Override
  public void setNumIter(int numIter) {
    this.numIter = numIter;
  }

  @Override
  public int getNumIter() {
    return numIter;
  }
  
  public void initModel() {
    this.user_clustering = new IntArrayList();
    user_clustering.size(maxUserID + 1);
    this.item_clustering = new IntArrayList();
    item_clustering.size(maxItemID + 1);

    this.user_cluster_averages = new DoubleArrayList();
    user_cluster_averages.size(numUserClusters);
    this.item_cluster_averages = new DoubleArrayList();
    item_cluster_averages.size(numItemClusters);
    this.cocluster_averages    = new Matrix<Double>(numUserClusters, numItemClusters, 0.0);
  }

  boolean iterateCheckModified() {
    boolean clustering_modified = false;
    computeClusterAverages();

    // Dimension users
    for (int u = 0; u < maxUserID; u++)
      if (findOptimalUserClustering(u))
        clustering_modified = true;

    // Dimension items
    for (int i = 0; i < maxItemID; i++)
      if (findOptimalItemClustering(i))
        clustering_modified = true;

    return clustering_modified;
  }

  /**
   * 
   */
  public void iterate() {
    iterateCheckModified();
  }

  /**
   * 
   */
  public void train() {
    random = Random.getInstance();

    initModel();
    for (int i = 0; i < user_clustering.size(); i++)
      user_clustering.set(i, random.nextInt(numUserClusters));
    for (int i = 0; i < item_clustering.size(); i++)
      item_clustering.set(i, random.nextInt(numItemClusters));

    computeAverages();

    for (int i = 0; i < numIter; i++)
      if (!iterateCheckModified())
        break;
  }

  /**
   * 
   */
  public double predict(int u, int i) {
    if (u > maxUserID && i > maxItemID)
      return global_average;
    if (u > maxUserID)
      return item_cluster_averages.get(item_clustering.get(i));
    if (i > maxItemID)
      return user_cluster_averages.get(user_clustering.get(u));

    double prediction = predict(u, i, user_clustering.get(u), item_clustering.get(i));
    if (prediction < minRating)
      return minRating;
    if (prediction > maxRating)
      return maxRating;
    return prediction;
  }

  double predict(int u, int i, int uc, int ic) {
    return cocluster_averages.get(uc, ic)
        + user_averages.get(u)
        - user_cluster_averages.get(uc)
        + item_averages.get(i)
        - item_cluster_averages.get(ic);
  }

  boolean findOptimalUserClustering(int user_id) {
    boolean modified = false;

    double[] errors = new double[numUserClusters];
    
    for (int uc = 0; uc < numUserClusters; uc++)
      for (int index : ratings.byUser().get(user_id)) {
        int item_id   = ratings.items().get(index);
        double rating = ratings.get(index);

        errors[uc] += Math.pow(rating - predict(user_id, item_id, uc, item_clustering.get(item_id)), 2);
      }

    int minimum_index = getMinimumIndex(errors, user_clustering.get(user_id));
    if (minimum_index != user_clustering.get(user_id)) {
      user_clustering.set(user_id, minimum_index);
      modified = true;
    }

    return modified;
  }

  boolean findOptimalItemClustering(int item_id) {
    boolean modified = false;

    double[] errors = new double[numItemClusters];
    for (int ic = 0; ic < numItemClusters; ic++)
      for (int index : ratings.byItem().get(item_id)) {
        int user_id = ratings.users().get(index);
        double rating = ratings.get(index);

        errors[ic] += Math.pow(rating - predict(user_id, item_id, user_clustering.get(user_id), ic), 2);
      }

    int minimum_index = getMinimumIndex(errors, item_clustering.get(item_id));
    if (minimum_index != item_clustering.get(item_id)) {
      item_clustering.set(item_id, minimum_index);
      modified = true;
    }

    return modified;
  }

  void computeAverages() {
    double[] user_sums = new double[maxUserID + 1];
    double[] item_sums = new double[maxItemID + 1];
    double sum = 0;

    this.user_counts = new IntArrayList();
    user_counts.size(maxUserID + 1);
    
    this.item_counts = new IntArrayList();
    item_counts.size(maxItemID + 1);

    for (int i = 0; i < ratings.size(); i++) {
      int user_id   = ratings.users().get(i);
      int item_id   = ratings.items().get(i);
      double rating = ratings.get(i);

      user_sums[user_id] += rating;
      item_sums[item_id] += rating;
      sum += rating;

      user_counts.set(user_id, user_counts.get(user_id) + 1);
      item_counts.set(item_id, item_counts.get(item_id) + 1);
    }

    this.global_average = sum / ratings.size();

    this.user_averages = new DoubleArrayList();
    user_averages.size(maxUserID + 1);

    for (int u = 0; u <= maxUserID; u++)
      if (user_counts.get(u) > 0)
        user_averages.set(u, user_sums[u] / user_counts.get(u));
      else
        user_averages.set(u, global_average);

    this.item_averages = new DoubleArrayList();
    item_averages.size(maxItemID + 1);
    for (int i = 0; i <= maxItemID; i++)
      if (item_counts.get(i) > 0)
        item_averages.set(i, item_sums[i] / item_counts.get(i));
      else
        item_averages.set(i, global_average);
  }

  void computeClusterAverages() {
    int[] user_cluster_counts = new int[numUserClusters];
    int[] item_cluster_counts = new int[numItemClusters];
    int[][] cocluster_counts  = new int[numUserClusters][numItemClusters];

    for (int i = 0; i < ratings.size(); i++) {
      int user_id = ratings.users().get(i);
      int item_id = ratings.items().get(i);
      double rating = ratings.get(i);

      user_cluster_averages.set(user_clustering.getInt(user_id), user_cluster_averages.getDouble(user_clustering.get(user_id)) + rating);
      item_cluster_averages.set(item_clustering.getInt(item_id), item_cluster_averages.getDouble(item_clustering.get(item_id)) + rating);
      cocluster_averages.set(user_clustering.get(user_id), item_clustering.get(item_id), cocluster_averages.get(user_clustering.get(user_id), item_clustering.get(item_id))  + rating);

      user_cluster_counts[user_clustering.get(user_id)]++;
      item_cluster_counts[item_clustering.get(item_id)]++;
      cocluster_counts[user_clustering.get(user_id)][ item_clustering.get(item_id)]++;
    }

    for (int i = 0; i < numUserClusters; i++)
      if (user_cluster_counts[i] > 0)
        user_cluster_averages.set(i, user_cluster_averages.get(i) / user_cluster_counts[i]);
      else
        user_cluster_averages.set(i, global_average);

    for (int i = 0; i < numItemClusters; i++)
      if (item_cluster_counts[i] > 0)
        item_cluster_averages.set(i, item_cluster_averages.get(i) / item_cluster_counts[i]);
      else
        item_cluster_averages.set(i, global_average);

    for (int i = 0; i < numUserClusters; i++)
      for (int j = 0; j < numItemClusters; j++)
        if (cocluster_counts[i][j] > 0)
          cocluster_averages.set(i, j, cocluster_averages.get(i, j) / cocluster_counts[i][j]);
        else
          cocluster_averages.set(i, j, global_average);
  }

  int getMinimumIndex(double[] array, int default_index) {
    int minimumIndex = default_index;

    for (int i = 0; i < array.length; i++)
      if (array[i] < array[minimumIndex])
        minimumIndex = i;

    return minimumIndex;
  }

  /**
   */
  public void saveModel(String filename) throws IOException {
    PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION);
    VectorExtensions.writeVector(writer, user_clustering);
    VectorExtensions.writeVector(writer, item_clustering);
    writer.println(Double.toString(global_average));
    VectorExtensions.writeVector(writer, user_averages);
    VectorExtensions.writeVector(writer, item_averages);
    VectorExtensions.writeVector(writer, user_cluster_averages);
    VectorExtensions.writeVector(writer, item_cluster_averages);
    IMatrixExtensions.writeMatrix(writer, cocluster_averages);
    writer.flush();
    writer.close();
  }

  /**
   * 
   */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
    IntList user_clustering = VectorExtensions.readIntVector(reader);
    IntList item_clustering = VectorExtensions.readIntVector(reader);
    double global_average = Double.parseDouble(reader.readLine());
    DoubleList user_averages = VectorExtensions.readVector(reader);
    DoubleList item_averages = VectorExtensions.readVector(reader);
    DoubleList user_cluster_averages = VectorExtensions.readVector(reader);
    DoubleList item_cluster_averages = VectorExtensions.readVector(reader);
    IMatrix<Double> cocluster_averages = IMatrixExtensions.readDoubleMatrix(reader, new Matrix<Double>(0, 0));
    reader.close();
    
    int num_user_clusters = user_cluster_averages.size();
    int num_item_clusters = item_cluster_averages.size();

    // Adjust maximum IDs
    this.maxUserID = user_clustering.size() - 1;
    this.maxItemID = item_clustering.size() - 1;

    // Adjust hyperparameters
    if (this.numUserClusters != num_user_clusters) {
      System.err.println("Set num_user_clusters to " + num_user_clusters);
      this.numUserClusters = num_user_clusters;
    }
    
    if (this.numItemClusters != num_item_clusters) {
      System.err.println("Set num_item_clusters to " + num_item_clusters);
      this.numItemClusters = num_item_clusters;
    }
    
    // Assign model
    this.global_average = global_average;
    this.user_cluster_averages = user_cluster_averages;
    this.item_cluster_averages = item_cluster_averages;
    this.cocluster_averages = cocluster_averages;
    this.user_averages = user_averages;
    this.item_averages = item_averages;
    this.user_clustering = user_clustering;
    this.item_clustering = item_clustering;

  }

  /**
   * 
   */
  public double computeLoss() {
    return Ratings.evaluate(this, ratings).get("RMSE");
  }

  /**
   * 
   */
  public String toString() {
    return 
      this.getClass().getName()
      + " numUserClusters=" + numUserClusters
      + " numItemClusters=" + numItemClusters
      + " numIter=" + numIter;
  }

}

