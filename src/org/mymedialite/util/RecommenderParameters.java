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


/**
 * Class for key-value pair string processing.
 */

package org.mymedialite.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @version 2.03
 */
public class RecommenderParameters extends HashMap<String, String> {

  /**
   * Create a CommandLineParameters object.
   * @param arg_string a string that contains the command line parameters
   */
  public RecommenderParameters(String arg_string) {

    String[] args = arg_string.split("\\s");

    for (int i = 0; i < args.length; i++) {
      if (args[i].length() == 0) continue;

      String[] pair = args[i].split("=");
      if (pair.length != 2)
        throw new IllegalArgumentException("Too many '=' : argument '" + args[i] + "'.");

      String arg_name  = pair[0];
      String arg_value = pair[1];

      if (this.containsKey(arg_name))
        throw new IllegalArgumentException(arg_name + " is used twice as an argument.");

      if (arg_value.length() == 0)
        throw new IllegalArgumentException(arg_name + " has an empty value.");

      this.put(arg_name, arg_value);
    }
  }

  /**
   * Create a CommandLineParameters object.
   * @param args a list of strings that contains the command line parameters
   * @param start ignore all parameters before this position
   */
  public RecommenderParameters(List<String> args, int start) {
    for (int i = start; i < args.size(); i++) {
      if (args.get(i).length() == 0) continue;

      String[] pair = args.get(i).split("=");
      if (pair.length != 2) throw new IllegalArgumentException("Too many '=' : argument '" + args.get(i) + "'.");

      String arg_name  = pair[0];
      String arg_value = pair[1];

      if (this.containsKey(arg_name)) throw new IllegalArgumentException(arg_name + " is used twice as an argument.");

      if (arg_value.length() == 0) throw new IllegalArgumentException(arg_name + " has an empty value.");

      this.put(arg_name, arg_value);
    }
  }

  /**
   * Check for parameters that have not been processed yet.
   * @return true if there are leftovers
   */
  public boolean checkForLeftovers() {
    if (this.size() != 0) {
      System.out.println("Unknown argument " + this.keySet().iterator().next());
      return true;
    }
    return false;
  }

  /**
   * Get the value of an integer parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @return the value of the parameter if it exists, 0 otherwise
   */
  public int getRemoveInt32(String key) {
    return getRemoveInt32(key, 0);
  }

  /**
   * Get the value of an integer parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @param dvalue the default value of the parameter
   * @return the value of the parameter if it exists, the default otherwise
   */
  public int getRemoveInt32(String key, int dvalue) {
    if (this.containsKey(key)) {
      int value = Integer.parseInt(this.get(key));
      this.remove(key);
      return value;
    } else {
      return dvalue;
    }
  }

  /**
   * Get the values of an integer list parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @return the values of the parameter if it exists, an empty list otherwise
   */
  public List<Integer> getRemoveInt32List(String key) {
    return getRemoveInt32List(key, " ");
  }

  /**
   * Get the values of an integer list parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @param sep the separator character used to split the string representation of the list
   * @return the values of the parameter if it exists, the default otherwise
   */
  public List<Integer> getRemoveInt32List(String key, String sep) {
    List<Integer> result_list = new ArrayList<Integer>();
    if (this.containsKey(key)) {
      String[] numbers = this.get(key).split(sep);
      this.remove(key);
      for (String s : numbers)
        result_list.add(Integer.parseInt(s));
    }
    return result_list;
  }

  /**
   * Get a double value from the parameters.
   * @param key the parameter name
   * @return the value of the parameter, 0 if no parameter of the given name found
   */
  public double getRemoveDouble(String key) {
    return getRemoveDouble(key, 0.0);
  }

  /**
   * Get a double value from the parameters.
   * @param key the parameter name
   * @param dvalue the default value if parameter of the given name is not found
   * @return the value of the parameter if it is found, the default value otherwise
   */
  public double getRemoveDouble(String key, double dvalue) {
    if (this.containsKey(key)) {
      try {
        double value = Double.parseDouble(this.get(key));
        this.remove(key);
        return value;
      } catch (NumberFormatException e) {
        throw new NumberFormatException(this.get(key));
      }
    } else {
      return dvalue;
    }
  }

  /**
   * Get a float value from the parameters.
   * @param key the parameter name
   * @return the value of the parameter, 0 if no parameter of the given name found
   */
  public float getRemoveFloat(String key) {
    return getRemoveFloat(key, 0.0f);
  }

  /**
   * Get a float value from the parameters.
   * @param key the parameter name
   * @param dvalue the default value if parameter of the given name is not found
   * @return the value of the parameter if it is found, the default value otherwise
   */
  public float getRemoveFloat(String key, float dvalue) {
    if (this.containsKey(key)) {
      try {
        float value = Float.parseFloat(this.get(key));
        this.remove(key);
        return value;
      } catch (NumberFormatException e) {
        throw new NumberFormatException(this.get(key));
      }
    } else {
      return dvalue;
    }
  }

  /**
   * Get a string parameter.
   * @param key the name of the parameter
   * @return the parameter value related to key, an empty string if it does not exist
   */
  public String getRemoveString(String key) {
    return getRemoveString(key, "");
  }

  /**
   * Get a string parameter.
   * @param key the name of the parameter
   * @param dvalue the default value
   * @return the parameter value related to key, the default value if it does not exist
   */
  public String getRemoveString(String key, String dvalue) {
    if (this.containsKey(key)) {
      String value = this.get(key);
      this.remove(key);
      return value;
    } else {
      return dvalue;
    }
  }

  /**
   * Get the value of a boolean parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @return the value of the parameter if it exists, false otherwise
   */
  public boolean GetRemoveBool(String key) {
    return GetRemoveBool(key, false);
  }

  /**
   * Get the value of a boolean parameter from the collection and remove the corresponding key-value pair.
   * @param key the name of the parameter
   * @param dvalue the default value of the parameter
   * @return the value of the parameter if it exists, the default otherwise
   */
  public boolean GetRemoveBool(String key, boolean dvalue) {
    if (this.containsKey(key)) {
      boolean value = Boolean.parseBoolean(this.get(key));
      this.remove(key);
      return value;
    } else {
      return dvalue;
    }
  }
}
