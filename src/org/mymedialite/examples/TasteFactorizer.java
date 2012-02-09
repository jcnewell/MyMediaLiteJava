// Copyright (C) Chris Newell
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

package org.mymedialite.examples;

import java.util.Iterator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.svd.AbstractFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.mymedialite.data.PosOnlyFeedback;
import org.mymedialite.datatype.SparseBooleanMatrix;
import org.mymedialite.itemrec.MF;

/**
 * Adaptor class between MyMediaLite and Mahout.
 * Allows a subclass of org.mymedialite.itemrec.MF to be used to create a
 * org.apache.mahout.cf.taste.impl.recommender.svd.Factorization
 * @version 2.03
 */
public class TasteFactorizer extends AbstractFactorizer {

  private final DataModel dataModel;
  private final MF mf;
  
  /**
   * Create a factorizer from the supplied data model and MyMediaLite matrix factorization.
   * 
   * See also org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender
   * 
   * @param dataModel the data model
   * @param mf the matrix factorization
   * @throws TasteException
   */
  public TasteFactorizer(DataModel dataModel, MF mf) throws TasteException {
    super(dataModel);
    this.dataModel = dataModel;
    this.mf = mf;
  }

  @Override
  public Factorization factorize() throws TasteException {

    //System.out.println("TasteFactorizer.factorize(): Load data");
    PosOnlyFeedback<SparseBooleanMatrix> posOnlyFeedback = null;
    try {
      posOnlyFeedback = new PosOnlyFeedback<SparseBooleanMatrix>(SparseBooleanMatrix.class);
    } catch (Exception e) { }
    
    if(dataModel == null) System.out.println("dataModel is null.");
    LongPrimitiveIterator itemIterator = dataModel.getItemIDs();
    if(itemIterator == null) System.out.println("itemIterator model is null.");
    while (itemIterator.hasNext()) {
      long itemID = itemIterator.nextLong();
      System.out.println("itemID = " + itemID);
      PreferenceArray preferenceArray = dataModel.getPreferencesForItem(itemID);
      if(preferenceArray == null) System.out.println("preferenceArray is null.");
      Iterator<Preference> preferenceIterator = preferenceArray.iterator();
      while (preferenceIterator.hasNext()) {
        int userIndex = userIndex(preferenceIterator.next().getUserID());
        int itemIndex = itemIndex(itemID);
        posOnlyFeedback.add(userIndex, itemIndex);
      }
    }
    mf.setFeedback(posOnlyFeedback);
    
    //System.out.println("TasteFactorizer.factorize(): train");
    mf.train();
    
    //System.out.println("TasteFactorizer.factorize(): Create user and item features arrays");
    double[][] userFeatures = new double[mf.getUserFactors().numberOfRows()][mf.getUserFactors().numberOfColumns()]; 
    for (int i = 0; i < mf.getUserFactors().numberOfRows(); i++) {
      for (int j = 0; j < mf.getUserFactors().numberOfColumns(); j++) { 
      userFeatures[i][j] = mf.getUserFactors().get(i, j);
      }
    }

    double[][] itemFeatures = new double[mf.getItemFactors().numberOfRows()][mf.getItemFactors().numberOfColumns()]; 
    for (int i = 0; i < mf.getItemFactors().numberOfRows(); i++) {
      for (int j = 0; j < mf.getItemFactors().numberOfColumns(); j++) { 
        itemFeatures[i][j] = mf.getItemFactors().get(i, j);
      }
    }
       
    //System.out.println("TasteFactorizer: TasteFactorizer.factorize(): Create factorization");
    return createFactorization(userFeatures, itemFeatures);
    
  }

}
