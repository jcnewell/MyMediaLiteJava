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

package org.mymedialite;

import java.util.List;

/**
 * Interface for classes that provide user similarities.
 * @version 2.03
 */
public interface IUserSimilarityProvider {

  /**
   * get the similarity between two users.
   * @return the user similarity; higher means more similar
   * @param user_id1 the ID of the first user
   * @param user_id2 the ID of the second user
   */
  float getUserSimilarity(int user_id1, int user_id2);

  /**
   * get the most similar users.
   * @return the users most similar to a given user
   * @param user_id the ID of the user
   * @param n the number of similar users to return
   */
  int[] getMostSimilarUsers(int user_id, int n);

}

