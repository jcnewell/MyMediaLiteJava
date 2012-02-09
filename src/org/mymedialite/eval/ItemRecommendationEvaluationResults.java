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

package org.mymedialite.eval;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Item recommendation evaluation results.
 * This class is basically a HashMap with a custom-made toString() method.
 * @version 2.03
 */
public class ItemRecommendationEvaluationResults extends HashMap<String, Double> {

  private static DecimalFormat decimalFormat = new DecimalFormat("0.00000");
  private static DecimalFormat integerFormat = new DecimalFormat("0");
  
  /**
   * default constructor.
   */
  public ItemRecommendationEvaluationResults() {
    for (String method : Items.getMeasures()) put(method, 0.0);
  }

  /**
   * Format item prediction results.
   * @return a string containing the results
   */
  public String toString() {
    return "AUC "           + decimalFormat.format(get("AUC"))
        + " prec@5 "    + decimalFormat.format(get("prec@5"))
        + " prec@10 "   + decimalFormat.format(get("prec@10"))
        + " MAP "       + decimalFormat.format(get("MAP"))
        + " recall@5 "  + decimalFormat.format(get("recall@5"))
        + " recall@10 " + decimalFormat.format(get("recall@10"))
        + " NDCG "      + decimalFormat.format(get("NDCG"))
        + " MRR "       + decimalFormat.format(get("MRR"))
        + " num_users " + integerFormat.format(get("num_users"))
        + " num_items " + integerFormat.format(get("num_items"))
        + " num_lists " + integerFormat.format(get("num_lists"));
  }
}

