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

package org.mymedialite.ensemble;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.mymedialite.IRecommender;
import org.mymedialite.io.Model;

/**
 * Combining several predictors with a weighted ensemble.
 * 
 * This recommender does NOT support incremental updates.
 * @version 2.03
 */
public class WeightedEnsemble extends Ensemble {

  private static final String VERSION = "2.03";
  
  /**
   * List of component weights.
   */
  public List<Double> weights = new ArrayList<Double>();

  /**
   * Sum of the component weights.
   */
  protected double weight_sum;

  /**
   * 
   */
  public void train() {
    for (IRecommender recommender : recommenders)
      recommender.train();

    for(double weight : weights)
      weight_sum += weight;
  }

  /**
   * 
   */
  public double predict(int user_id, int item_id) {
    double result = 0;
    for (int i = 0; i < recommenders.size(); i++)
      result += weights.get(i) * recommenders.get(i).predict(user_id, item_id);

    return result / weight_sum;
  }

  /**
   * @throws IOException 
   */
  public void saveModel(String filename) throws IOException {
      PrintWriter writer = Model.getWriter(filename, this.getClass(), VERSION); 
      writer.println(recommenders.size());
      for (int i=0; i < recommenders.size(); i++) {
        recommenders.get(i).saveModel("model-" + i + ".txt");
        writer.println(recommenders.get(i).getClass().getName() + " " + weights.get(i).toString());
      }
      writer.flush();
      writer.close();
    }
  

  /**
   */
  public void loadModel(String filename) throws IOException {
    BufferedReader reader = Model.getReader(filename, this.getClass());
      int numberOfComponents = Integer.parseInt(reader.readLine());

      List<Double> weights = new ArrayList<Double>();
      List<IRecommender> recommenders = new ArrayList<IRecommender>();

      for (int i = 0; i < numberOfComponents; i++) {
        String[] data = reader.readLine().split(" ");

        try {
          Class<?> c = Class.forName(data[0]);
          recommenders.add((IRecommender) c.newInstance());
        } catch (Exception e) {
          System.err.println("Unable to create recommender " + data[0]);
          throw new IOException();
        }
        recommenders.get(i).loadModel("model-" + i + ".txt");

        // TODO make sure the recommenders get their data?
       
        weights.add(Double.parseDouble(data[1]));
      }
      reader.close();

      this.weights = weights;
      this.recommenders = recommenders;
    }
  
}
