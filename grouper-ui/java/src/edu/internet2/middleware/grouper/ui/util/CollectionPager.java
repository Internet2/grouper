/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.internet2.middleware.grouper.ui.util;

import java.util.*;

/**
 * Convenience class for use in UI to encapsulate paging logic, and enable
 * creation of HTML links which maintain context
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CollectionPager.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
 */

public class CollectionPager {
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
	public CollectionPager(Collection collection, int count, String startStr,
			int start, String pageSizeStr, int pageSize) {
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
	 * Sets the params - used for generating links
	 * 
	 * @param params
	 *            The params to set
	 */
	public void setParam(String key, Object value) {
		this.params.put(key, value);
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