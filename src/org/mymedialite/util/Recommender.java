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
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.util;

import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.reflect.Field;
import org.mymedialite.IItemAttributeAwareRecommender;
import org.mymedialite.IItemRelationAwareRecommender;
import org.mymedialite.IRecommender;
import org.mymedialite.IUserAttributeAwareRecommender;
import org.mymedialite.IUserRelationAwareRecommender;
import org.mymedialite.itemrec.ItemRecommender;
import org.mymedialite.ratingprediction.RatingPredictor;

/**
 * Helper class with utility methods for handling recommenders.
 * 
 * Contains methods for creating and configuring recommender objects, as well as listing recommender classes.
 * @version 2.03
 */
public class Recommender {
  
  // Prevent instantiation.
  private Recommender() {}
  
  static String normalizeName(String s) {
    s = s.replaceAll("_", "");
    return s.toUpperCase();
  }
  
  public interface ErrorHandler {
    void reportError(String error);
  }
  
  public static class DefaultErrorHandler implements ErrorHandler {
    public void reportError(String message) {
      System.err.println(message);
    }
  }
  
  /**
   * Configure a recommender.
   * @param recommender the recommender to configure
   * @param parameters a string containing the parameters as key-value pairs
   * @param errorHandler interface for error reporting
   * @return the configured recommender
   */
  public static <T> T configure(T recommender, String parameters, ErrorHandler errorHandler)  throws IllegalAccessException {
    RecommenderParameters parameters_dictionary = new RecommenderParameters(parameters);
    return configure(recommender, parameters_dictionary, errorHandler);
  }

  /**
   * Configure a recommender.
   * @param recommender the recommender to configure
   * @param parameters a string containing the parameters as key-value pairs
   */
  public static <T> T configure(T recommender, String parameters)  throws IllegalAccessException {
    return configure(recommender, parameters, new DefaultErrorHandler());
  }

  /**
   * Configure a recommender.
   * @param recommender the recommender to configure
   * @param parameters a dictionary containing the parameters as key-value pairs
   * @param errorHandler interface for error reporting
   * @return the configured recommender
   */
  public static <T> T configure(T recommender, HashMap<String, String> parameters, ErrorHandler errorHandler) throws IllegalAccessException {

    for (Entry<String, String> entry : parameters.entrySet()) {
      String key = normalizeName(entry.getKey()); 
      for (Field field : recommender.getClass().getFields()) {
        if (field.getName().equalsIgnoreCase(key)) {
          if (field.getType().getName() == "double") field.set(recommender, Double.parseDouble(entry.getValue()));
        } else if (field.getType().getName() == "float") {
          if (field.getType().getName() == "float") field.set(recommender, Float.parseFloat(entry.getValue()));
        } else if (field.getType().getName() == "int") {
          if (field.getType().getName() == "int") field.set(recommender, Integer.parseInt(entry.getValue()));
        } else if (field.getType().getName() == "boolean") {
          if (field.getType().getName() == "boolean") field.set(recommender, Boolean.parseBoolean(entry.getValue()));
        } else {
          errorHandler.reportError("Parameter " + key + " has unknown type " + field.getType());  
        }
      }
    }
    return recommender;
  }

  /**
   * Sets a property of a MyMediaLite recommender.
   * @param recommender An <see cref="IRecommender"/>
   * @param key the name of the property (case insensitive)
   * @param val the string representation of the value
   */
  public static void setProperty(IRecommender recommender, String key, String val) throws IllegalAccessException {
    key = normalizeName(key); 
    for (Field field : recommender.getClass().getFields()) {
      if (field.getName().equalsIgnoreCase(key)) {
        if (field.getType().getName() == "double") field.set(recommender, Double.parseDouble(val));
      } else if (field.getType().getName() == "float") {
        if (field.getType().getName() == "float") field.set(recommender, Float.parseFloat(val));
      } else if (field.getType().getName() == "int") {
        if (field.getType().getName() == "int") field.set(recommender, Integer.parseInt(val));
      } else if (field.getType().getName() == "boolean") {
        if (field.getType().getName() == "boolean") field.set(recommender, Boolean.parseBoolean(val));
      } else {
        throw new IllegalArgumentException("Parameter " + key + " has unknown type " + field.getType());  
      }
    }
  }

  /**
   * Create a rating predictor from the type name.
   * @param typename a string containing the type name
   * @return an item recommender object of type typename if the recommender type is found, null otherwise
   */
  public static RatingPredictor createRatingPredictor(String typename) {
    if (!typename.startsWith("org.mymedialite.ratingprediction.")) typename = "org.mymedialite.ratingprediction." + typename;
    try {
      return (RatingPredictor) Class.forName(typename).getConstructor().newInstance();
    } catch (ClassCastException e) {
      System.err.println(typename + " is not a subclass of org.mymedialite.ratingprediction.RatingPredictor");
    } catch (Exception e) {
      System.err.println("Unable to instantiate " + typename);
    }
    return null;
  }

  /**
   * Create an item recommender from the type name.
   * @param typename a string containing the type name
   * @return an item recommender object of type typename if the recommender type is found, null otherwise
   */
  public static ItemRecommender createItemRecommender(String typename) {
    if (!typename.startsWith("org.mymedialite.itemrec.")) typename = "org.mymedialite.itemrec." + typename;
    try {
      return (ItemRecommender) Class.forName(typename).getConstructor().newInstance();
    } catch (ClassCastException e) {
      System.err.println(typename + " is not a subclass of org.mymedialite.itemrec.ItemRecommender");
      return null;
    } catch (Exception e) {
      System.err.println("Unable to instantiate " + typename);
    }
    return null;
  }

  /**
   * Describes the kind of data needed by this recommender.
   * @param recommender a recommender
   * @return a string containing the additional datafiles needed for training this recommender
   */
  public static String needs(IRecommender recommender) {
    // determine necessary data
    String needs = "";
    if (recommender instanceof IUserRelationAwareRecommender)
      needs += "--user-relations=FILE, ";
    if (recommender instanceof IItemRelationAwareRecommender)
      needs += "--item-relations=FILE, ";
    if (recommender instanceof IUserAttributeAwareRecommender)
      needs += "--user-attributes=FILE, ";
    if (recommender instanceof IItemAttributeAwareRecommender)
      needs += "--item-attributes=FILE, ";

    return needs.substring(0, Math.max(0, needs.length() - 2));
  }

}
