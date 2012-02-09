// Copyright (C) 2011 Zeno Gantner, CHris Newell
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

package org.mymedialite.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Routines to read lists of numbers from text files.
 * @version 2.03
 */
public class NumberFile {

  // Prevent instantiation.
  private NumberFile() {}

  /**
   * Read a list of longs from a BufferedReader.
   * @param reader the BufferedReader to be read from
   * @return a list of longs
   */
  public static List<Long> readLongs(BufferedReader reader) throws IOException {
    List<Long> numbers = new ArrayList<Long>();
    String line = "";
    try {
      while ((line = reader.readLine()) != null)
        numbers.add(Long.parseLong(line));
    } catch (IOException e) {
      throw new IOException("Could not read line " + line);
    }
    return numbers;
  }

  /**
   * Read a list of longs from a file.
   * @param filename the name of the file to be read from
   * @return a list of longs
   */
  public static List<Long> readLongs(String filename) throws IOException {
    if (filename == null) throw new IllegalArgumentException("filename");
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return readLongs(reader);
  }

  /**
   * Read a list of integers from a BufferedReader.
   * @param reader the BufferedReader to be read from
   * @return a list of integers
   */
  public static List<Integer> readIntegers(BufferedReader reader) throws IOException {
    List<Integer> numbers = new ArrayList<Integer>();
    String line = "";
    try {
      while ((line = reader.readLine()) != null)
        numbers.add(Integer.parseInt(line));
    } catch (IOException e) {
      throw new IOException("Could not read line " + line);
    }
    return numbers;
  }

  /**
   * Read a list of integers from a file.
   * @param filename the name of the file to be read from
   * @return a list of integers
   */
  public static List<Integer> readIntegers(String filename) throws IOException {
    if (filename == null) throw new IllegalArgumentException("filename");
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return readIntegers(reader);
  }
  
  /**
   * Read a list of integers from a BufferedReader.
   * @param reader the BufferedReader to be read from
   * @return a list of integers
   */
  public static List<String> readStrings(BufferedReader reader) throws IOException {
    List<String> numbers = new ArrayList<String>();
    String line = "";
    try {
      while ((line = reader.readLine()) != null)
        numbers.add(line.trim());
    } catch (IOException e) {
      throw new IOException("Could not read line " + line);
    }
    return numbers;
  }

  /**
   * Read a list of integers from a file.
   * @param filename the name of the file to be read from
   * @return a list of integers
   */
  public static List<String> readStrings(String filename) throws IOException {
    if (filename == null) throw new IllegalArgumentException("filename");
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return readStrings(reader);
  }
  
}

