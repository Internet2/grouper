/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.internet2.middleware.grouperVoot.messages;

import java.lang.reflect.Field;
import java.util.Comparator;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;

/**
 * Response for get groups request.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancin@gmail.com>
 */
public class VootGetGroupsResponse extends VootResponse {

  /** result body */
  private VootGroup[] entry;

  /**
   * Get the results as array of VOOT groups.
   * 
   * @return the results as array of VOOT groups.
   */
  public VootGroup[] getEntry() {
    return this.entry;
  }

  /**
   * Set the results passing an array of VOOT groups.
   * 
   * @param entry1 the array of VOOT groups.
   */
  public void setEntry(VootGroup[] entry1) {
    this.entry = entry1;
  }

  /**
   * Set the results by taking a slice of the elements in an array of VOOT groups.
   * 
   * @param entry1 the array of VOOT groups.
   * @param start the first element in the result set (0 means start from beginning).
   * @param count the number of elements in the result set (-1 or 0 means find all).
   */
  public void setEntry(VootGroup[] entry1, int start, int count) {
    int remaining = count;
    if (remaining < 0 || (entry1.length - start) < count) {
      remaining = entry1.length - start;
    }

    VootGroup[] pageArray = new VootGroup[remaining];
    for (int i = 0; i < remaining; ++i) {
      pageArray[i] = entry1[i + start];
    }

    this.entry = pageArray;
  }

  /**
   * Method that sorts one array of VOOT groups.
   * 
   * @param entries the array to sort.
   * @param sortBy the field name to be used for sorting or null of no sorting.
   * @return the sorted array.
   */
  public static VootGroup[] sort(VootGroup[] entries, final String sortBy) {
    if (sortBy != null) {
      Arrays.sort(entries, new Comparator<VootGroup>() {
        @Override
        public int compare(VootGroup group1, VootGroup group2) {
          try {
            // Set up introspection for the field specified by sortBy
            Field f = VootGroup.class.getDeclaredField(sortBy);
            f.setAccessible(true);

            // Retrieve String value for the field obtained by introspection
            String value1 = f.get(group1).toString();
            String value2 = f.get(group2).toString();

            // Compare the two strings
            return value1.compareTo(value2);
          } catch (Exception e) {
            // If any exception return 0 not to influence original sorting.
            return 0;
          }
        }
      });
    }
    return entries;
  }
}
