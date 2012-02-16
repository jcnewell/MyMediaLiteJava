// // Copyright (C) 2011 Zeno Gantner, Chris Newell
// //
// // This file is part of MyMediaLite.
// //
// // MyMediaLite is free software: you can redistribute it and/or modify
// // it under the terms of the GNU General Public License as published by
// // the Free Software Foundation, either version 3 of the License, or
// // (at your option) any later version.
// //
// // MyMediaLite is distributed in the hope that it will be useful,
// // but WITHOUT ANY WARRANTY; without even the implied warranty of
// // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// // GNU General Public License for more details.
// //
// //  You should have received a copy of the GNU General Public License
// //  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.io;

/**
 * Static class containing constants used by the MyMediaLite Input/Output routines.
 * @version 2.03
 */
public class Constants {

  // Prevent instantiation.
  private Constants() {}

  /**
   * Regular expression used for splitting tab/whitespace/comma separated fields.
   * 
   * The fields can be divided by either:
   *   - a tab
   *   - a space
   *   - a comma
   *   - a semicolon
   *   - two colons (i.e. "::")
   *   
   * with zero or more additional spaces on either side. 
   */
  public static final String SPLIT_CHARS = "\\s*(\t|\\s|,|;|::)\\s*";

}
