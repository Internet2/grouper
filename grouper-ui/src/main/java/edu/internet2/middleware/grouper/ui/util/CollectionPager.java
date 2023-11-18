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
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Convenience class for use in UI to encapsulate paging logic, and enable
 * creation of HTML links which maintain context
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CollectionPager.java,v 1.6 2009-08-12 04:52:14 mchyzer Exp $
 */

public class CollectionPager implements Serializable {

  private Collection alwaysShowCollection;
  
	private Collection collection;

	private HashMap params = new HashMap();

	private int count;

	private int pageSize;

	private int start;

	private boolean complete = false;

	private String startStr = "start";

	private String pageSizeStr = "pageSize";

	private String target = "";

	/**
	 * @return Returns the target (where links are directed).
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            The target to set.
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Constructor. If collection.size() < count it is assumed that only the
	 * items to display are in collection. start and pageSize can be set as int
	 * or String
	 * 
	 * @param collection
	 *            to page
	 * @param count
	 *            total number of elements in collection
	 * @param startStr
	 * @param start
	 * @param pageSizeStr
	 * @param pageSize
	 */
	public CollectionPager(Collection theAlwaysShowCollection, Collection collection, int count, String startStr,
			int start, String pageSizeStr, int pageSize) {
	  this.alwaysShowCollection = theAlwaysShowCollection;
		if (startStr != null)
			this.startStr = startStr;
		if (pageSizeStr != null)
			this.pageSizeStr = pageSizeStr;
		this.collection = collection;
		this.count = count;
		this.start = start;
		this.pageSize = pageSize;
		params.put(this.pageSizeStr, new Integer(pageSize));
		params.put(this.startStr, new Integer(start));
		if (count == collection.size())
			complete = true;
	}

	/**
	 * @return whether there is another page of results after current one
	 */
	public boolean isNext() {
		if (start + pageSize >= count)
			return false;
		return true;
	}

	/**
	 * @return whther there was a previous page before current one
	 */
	public boolean isPrev() {
		if (start <= 1)
			return false;
		return true;
	}

	/**
	 * @return start position for next page
	 */
	public int getNextStart() {
		if (isNext()) {
			return start + pageSize;
		}
		return start;
	}

	/**
	 * @return start position for previous page
	 */
	public int getPrevStart() {
		if (isPrev()) {
			return start - pageSize;
		}
		return 1;
	}

	/**
	 * @return position of last item to be returned on this page
	 */
	public int getLast() {
		int last = start + pageSize;
		if (last > count) {
			return count;
		}
		return last;
	}

	/**
	 * @return Map representing key/value pairs for a link to display the next
	 *         page
	 */
	public Map getNextParams() {
		params.put(startStr, new Integer(getNextStart()));
		return Collections.unmodifiableMap((Map) params.clone());
	}

	/**
	 * @return Map representing key/value pairs for a link to display the
	 *         previous page
	 */
	public Map getPrevParams() {
		params.put(startStr, new Integer(getPrevStart()));
		return Collections.unmodifiableMap((Map) params.clone());
	}

	/**
	 * @return Map representing key/value pairs for a link to display the first
	 *         page
	 */
	public Map getRestartParams() {
		params.put(startStr, new Integer(0));
		return Collections.unmodifiableMap((Map) params.clone());
	}

	/**
	 * Returns the collection.
	 * 
	 * @return Collection
	 */
	public Collection getCollection() {
		if (GrouperUtil.length(this.alwaysShowCollection) == 0) {
		  return this.getCollectionHelper();
		}
		ArrayList arrayList = new ArrayList();
		arrayList.addAll(this.alwaysShowCollection);
		arrayList.addAll(this.getCollectionHelper());
		return arrayList;
	}

  /**
   * Returns the collection.
   * 
   * @return Collection
   */
  private Collection getCollectionHelper() {
		if (!complete)
			return collection;
		Object[] arr = collection.toArray();
		return new ArrayList(Arrays.asList(arr).subList(start, getLast()));
	}

	/**
	 * Returns the count.
	 * 
	 * @return int
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Returns the pageSize.
	 * 
	 * @return int
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the params.
	 * 
	 * @return HashMap
	 */
	public HashMap getParams() {
		return params;
	}

	/**
	 * Sets the collection.
	 * 
	 * @param collection
	 *            The collection to set
	 */
	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	/**
	 * Sets the count.
	 * 
	 * @param count
	 *            The count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Sets the pageSize.
	 * 
	 * @param pageSize
	 *            The pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	
	/**
	 * @param key
	 * @param value
	 */
	public void setParam(String key, Object value) {
		this.params.put(key, value);
	}
	
	/**
	 * Adds to any existing params - used for generating links
	 * 
	 * @param map
	 *            The params to set
	 */
	public void setParams(Map map) {
		this.params.putAll(map);
	}

	/**
	 * Returns the start.
	 * 
	 * @return int
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Returns the start.
	 * 
	 * @return int
	 */
	public int getStart1() {
		return start + 1;
	}

	/**
	 * @param pageSizeStr
	 *            The pageSizeStr to set.
	 */
	public void setPageSizeStr(String pageSizeStr) {
		Object obj = params.get(this.pageSizeStr);
		params.remove(this.pageSizeStr);
		this.pageSizeStr = pageSizeStr;
		params.put(pageSizeStr, obj);
	}

	/**
	 * @param startStr
	 *            The startStr to set.
	 */
	public void setStartStr(String startStr) {
		Object obj = params.get(this.startStr);
		params.remove(this.startStr);
		this.startStr = startStr;
		params.put(startStr, obj);
	}
}
