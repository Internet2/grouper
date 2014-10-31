package edu.internet2.middleware.grouperVoot.beans;

import java.lang.reflect.Field;
import java.util.Comparator;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * response for get groups request
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancin@gmail.com>
 */
public class VootGetGroupsResponse extends VootResponse {
  
  /**
   * result body
   */
  private VootGroup[] entry;

  /**
   * results
   * @return the results
   */
  public VootGroup[] getEntry() {
    return this.entry;
  }
  
  /**
   * results
   * @param entry
   */
  public void setEntry(VootGroup[] entry) {
    this.entry = entry;
  }

  /**
   * results
   * @param resultArray
   */
  public void setEntry(VootGroup[] resultArray, int start, int count) {
	int remaining = count;
	if (remaining < 0 || (resultArray.length - start) < count)
	  remaining = resultArray.length - start;  
	
	VootGroup[] pageArray = new VootGroup[remaining];
    for (int i = 0; i < remaining; ++i) {
      pageArray[i] = resultArray[i + start];
    }
    
    this.entry = pageArray;
  }
  
  /**
   * Method that sorts one array of VOOT persons.
   * @param entries the array to sort
   * @param sortBy the field to be used for sorting
   * @return the sorted array
   */
  public static VootGroup[] sort(VootGroup[] entries, final String sortBy) {
	if (sortBy != null) {
      Arrays.sort(entries, new Comparator<VootGroup>() {
	    @Override
	    public int compare(VootGroup group1, VootGroup group2) {
	      try {
	    	Field f = VootGroup.class.getDeclaredField(sortBy);
            f.setAccessible(true);
					
            String value1 = (String) f.get(group1);
            String value2 = (String) f.get(group2);
					
            return value1.compareTo(value2);
          } catch (Exception e) {
            return 0;
          }
        }
      });
	}
    return entries;
  }
}
