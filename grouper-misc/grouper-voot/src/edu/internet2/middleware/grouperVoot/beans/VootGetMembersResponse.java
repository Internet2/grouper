package edu.internet2.middleware.grouperVoot.beans;

import java.lang.reflect.Field;
import java.util.Comparator;

import edu.emory.mathcs.backport.java.util.Arrays;


/**
 * response to get members request
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public class VootGetMembersResponse extends VootResponse {
  
  /**
   * result body
   */
  private VootPerson[] entry;

  /**
   * results
   * @return the results
   */
  public VootPerson[] getEntry() {
    return this.entry;
  }
  
  /**
   * results
   * @param entry
   */
  public void setEntry(VootPerson[] entry) {
    this.entry = entry;
  }

  /**
   * results
   * @param resultArray
   */
  public void setEntry(VootPerson[] resultArray, int start, int count) {
    int remaining = count;
    if (remaining < 0 || (resultArray.length - start) < count)
      remaining = resultArray.length - start;
	  
	VootPerson[] pageArray = new VootPerson[remaining];
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
  public static VootPerson[] sort(VootPerson[] entries, final String sortBy) {
	if (sortBy != null) {
	  Arrays.sort(entries, new Comparator<VootPerson>() {
		@Override
		public int compare(VootPerson person1, VootPerson person2) {
		    Field f;
			try {
				f = VootPerson.class.getDeclaredField(sortBy);
				f.setAccessible(true);
				
				String value1 = (String) f.get(person1);
				String value2 = (String) f.get(person2);
				
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
