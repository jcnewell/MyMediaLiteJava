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
    PosOnlyFeedback<SparseBooleanMatrix> posOnlyFeedback = new PosOnlyFeedback<SparseBooleanMatrix>(new SparseBooleanMatrix());
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
    double[][] userFeatures = new double[mf.getUserFactors().getNumberOfRows()][mf.getUserFactors().getNumberOfColumns()]; 
    for (int i = 0; i < mf.getUserFactors().getNumberOfRows(); i++) {
      for (int j = 0; j < mf.getUserFactors().getNumberOfColumns(); j++) { 
      userFeatures[i][j] = mf.getUserFactors().get(i, j);
      }
    }

    double[][] itemFeatures = new double[mf.getItemFactors().getNumberOfRows()][mf.getItemFactors().getNumberOfColumns()]; 
    for (int i = 0; i < mf.getItemFactors().getNumberOfRows(); i++) {
      for (int j = 0; j < mf.getItemFactors().getNumberOfColumns(); j++) { 
        itemFeatures[i][j] = mf.getItemFactors().get(i, j);
      }
    }
       
    //System.out.println("TasteFactorizer: TasteFactorizer.factorize(): Create factorization");
    return createFactorization(userFeatures, itemFeatures);
    
  }

}
