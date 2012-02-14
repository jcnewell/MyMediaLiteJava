//Copyright (C) 2011, 2012 Zeno Gantner, Chris Newell
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
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.
//

package org.mymedialite.eval;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Rating prediction evaluation results.
 * This class is basically a Dictionary with a custom-made toString() method.
 * @version 2.03
 */
public class RatingPredictionEvaluationResults extends HashMap<String, Double> {

  private static DecimalFormat decimalFormat = new DecimalFormat("0.00000");

  /**
   * Format rating prediction results.
   * 
   * See http://recsyswiki.com/wiki/Root_mean_square_error and http://recsyswiki.com/wiki/Mean_absolute_error
   * 
   * @return a string containing the results
   */
  @Override
  public String toString() {
    String s = "RMSE="  + decimalFormat.format(get("RMSE"))
        + " MAE="  + decimalFormat.format(get("MAE"))
        + " NMAE=" + decimalFormat.format(get("NMAE"));

    if (this.containsKey("CBD"))
      s+= " CBD="  + decimalFormat.format(get("CBD"));

    if (this.containsKey("fit"))
      s += " fit=" + decimalFormat.format(get("fit"));

    return s;
  }

}

