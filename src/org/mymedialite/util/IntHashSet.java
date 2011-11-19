package org.mymedialite.util;

import java.util.HashSet;

public class IntHashSet extends HashSet<Integer> {
	private static final long serialVersionUID = 1L;
	private int[] values;
    
	  public IntHashSet() {
	    super();
	  }
	    
	  public IntHashSet(int initialCapacity) {
	    super(initialCapacity);
	  }
	     
	  public boolean add(Integer value) {
	    values = null;
	    return super.add(value);
	  }
	    
	  public boolean remove(Integer value) {
	    values = null;
	    return super.remove(value);
	  }
	    
	  public int get(int index) {
	    if(values == null) createValues();
	    return values[index];
	  }
	   
	  public void clear() {
	    values = null;
	    super.clear();
	  }
	    
	  public int[] values() {
	    if(values == null) createValues();
	    return values;
	  }
	       
	  private void createValues() {
	    Integer[] values = toArray(new Integer[size()]);
	    this.values = new int[values.length];
	    for (int i=0; i<values.length; i++) {
	      this.values[i] = values[i];
	    } 
	  }
	    
	  public IntHashSet clone() {
	    IntHashSet clone = new IntHashSet();
	    for(Integer i : toArray(new Integer[size()])) {
	      clone.add(i);
	    }
	    return clone; 
	  }
	    
	  public int max() {
	    int max = 0;
	    if(values == null) createValues();
	    for (int i=0; i<values.length; i++) {
	      max = Math.max(max, values[i]);
	    } 
	    return max;
	  }
}