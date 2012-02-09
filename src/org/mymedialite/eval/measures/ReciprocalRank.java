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

package org.mymedialite.eval.measures;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * The reciprocal rank of a list of ranked items.
 * 
 * See http://en.wikipedia.org/wiki/Mean_reciprocal_rank
 *
 * Literature:
 *
 *     E.M. Voorhees "Proceedings of the 8th Text Retrieval Conference". TREC-8 Question Answering Track Report. 1999.
 *     http://gate.ac.uk/sale/dd/related-work/qa/TREC+1999+TREC-8+QA+Report.pdf
 *     
 * @version 2.03
 */
public class ReciprocalRank {

  // Prevent instantiation.
  private ReciprocalRank() {}

  /**
   * Compute the reciprocal rank of a list of ranked items.
   * 
   * See http://en.wikipedia.org/wiki/Mean_reciprocal_rank
   * 
   * @param ranked_items a list of ranked item IDs, the highest-ranking item first
   * @param correct_items a collection of positive/correct item IDs
   * @param ignore_items a collection of item IDs which should be ignored for the evaluation
   * @return the mean reciprocal rank for the given data
   */
  public static double compute(List<Integer> ranked_items, Collection<Integer> correct_items, Collection<Integer> ignore_items) {
    if (ignore_items == null)
      ignore_items = new HashSet<Integer>();

    int pos = 0;

    for (int item_id : ranked_items) {
      if (ignore_items.contains(item_id))
        continue;

      if (correct_items.contains(ranked_items.get(pos++)))
        return (double) 1 / (pos);
    }

    return 0;
  }
}
