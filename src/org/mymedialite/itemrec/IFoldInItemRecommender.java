//Copyright (C) 2012 Zeno Gantner
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
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite. If not, see <http://www.gnu.org/licenses/>.
//

package org.mymedialite.itemrec;

import java.util.List;
import org.mymedialite.IRecommender;
import org.mymedialite.data.WeightedItem;

/**
 * Rating predictor that allows folding in new users.
 * 
 * The process of folding in is computing a predictive model for a new user based on their ratings
 * and the existing recommender, without modifying the parameters of the existing recommender.
 * 
 * Literature:
 *    Badrul Sarwar and George Karypis, Joseph Konstan, John Riedl:
 *    Incremental singular value decomposition algorithms for highly scalable recommender systems.
 *    Fifth International Conference on Computer and Information Science, 2002.
 *    http://grouplens.org/papers/pdf/sarwar_SVD.pdf
 */
public interface IFoldInItemRecommender extends IRecommender {

  /**
   * Score a list of items given a list of items that represent a new user.
   * @return a list of WeightedItems, representing item IDs and predicted scores
   * @param accessed_items the ratings (item IDs and rating values) representing the new user
   * @param candidate_items the items to be rated
   */
  List<WeightedItem> scoreItems(List<Integer> accessed_items, List<Integer> candidate_items);

}