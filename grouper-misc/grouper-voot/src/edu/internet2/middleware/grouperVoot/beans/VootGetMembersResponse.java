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

package edu.internet2.middleware.grouperVoot.beans;

import java.lang.reflect.Field;
import java.util.Comparator;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Response to get members request.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public class VootGetMembersResponse extends VootResponse {

	/** result body */
	private VootPerson[] entry;

	/**
	 * Get the results as array of VOOT persons.
	 * 
	 * @return the results as array of VOOT persons.
	 */
	public VootPerson[] getEntry() {
		return this.entry;
	}

	/**
	 * Set the results passing an array of VOOT persons.
	 * 
	 * @param entry the array of VOOT persons.
	 */
	public void setEntry(VootPerson[] entry) {
		this.entry = entry;
	}

	/**
	 * Set the results by taking a slice of the elements in an array of VOOT persons.
	 * 
	 * @param entry the array of VOOT persons.
	 * @param start the first element in the result set (0 means start from beginning).
	 * @param count the number of elements in the result set (-1 or 0 means find all).
	 */
	public void setEntry(VootPerson[] entry, int start, int count) {
		int remaining = count;
		if (remaining < 0 || (entry.length - start) < count)
			remaining = entry.length - start;

		VootPerson[] pageArray = new VootPerson[remaining];
		for (int i = 0; i < remaining; ++i) {
			pageArray[i] = entry[i + start];
		}

		this.entry = pageArray;
	}

	/**
	 * Method that sorts one array of VOOT persons.
	 * 
	 * @param entries the array to sort.
	 * @param sortBy the field name to be used for sorting or null of no sorting.
	 * @return the sorted array.
	 */
	public static VootPerson[] sort(VootPerson[] entries, final String sortBy) {
		if (sortBy != null) {
			Arrays.sort(entries, new Comparator<VootPerson>() {
				@Override
				public int compare(VootPerson person1, VootPerson person2) {
					try {
						// Set up introspection for the field specified by sortBy
						Field f = VootGroup.class.getDeclaredField(sortBy);
						f.setAccessible(true);

						// Retrieve String value for the field obtained by introspection
						String value1 = f.get(person1).toString();
						String value2 = f.get(person2).toString();

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
