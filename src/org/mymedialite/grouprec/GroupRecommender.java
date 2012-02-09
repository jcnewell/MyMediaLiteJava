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

package org.mymedialite.grouprec;

import java.util.Collection;
import java.util.List;
import org.mymedialite.IRecommender;

/**
 * Base class for group recommenders.
 * @version 2.03
 */
public abstract class GroupRecommender implements IGroupRecommender {

  /**
   * The underlying recommender that produces the user-wise item scores.
   */
  protected IRecommender recommender;

  /**
   * Constructor that takes the underlying recommender that will be used.
   * @param recommender the underlying recommender
   */
  public GroupRecommender(IRecommender recommender) {
    this.recommender = recommender;
  }

  /**
   */
  public abstract List<Integer> rankItems(Collection<Integer> users, Collection<Integer> items);

}