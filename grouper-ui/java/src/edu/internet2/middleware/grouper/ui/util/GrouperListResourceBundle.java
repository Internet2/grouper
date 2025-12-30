/*******************************************************************************
 * Copyright 2025 Internet2
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

package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouper.ui.UIThreadLocal;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListResourceBundle;
import java.util.Set;
import java.util.Vector;

/**
 * Populate a bundle by adding key/value pairs
 */

public class GrouperListResourceBundle extends ListResourceBundle implements
		Serializable {
	private String name = null;

	private String mapName = null;


	private HashMap cache = new HashMap();

	/**
	 * add to cache some extra params
	 * @param key
	 * @param value
	 */
	public void addToCache(String key, String value) {
	  this.cache.put(key, value == null ? "" : value); // ListResourceBundle can't have null values
	}

	/**
	 * Constructor - with empty cache
	 *
	 * @param name bundle which can be referred to elsewhere
	 */
	public GrouperListResourceBundle(String name) {
		if (name == null)
			throw new NullPointerException();
		this.name = name;
		this.mapName = name + "Map";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ListResourceBundle#getKeys()
	 */
	public Enumeration getKeys() {
		Set keys = new HashSet(cache.keySet());
		Vector v = new Vector(keys);
		return v.elements();
	}

  @Override
  protected Object[][] getContents() {
    Object[][] contents = new Object[cache.size()][2];
    int index = 0;
    for (Object key : cache.keySet()) {
      contents[index][0] = key;
      contents[index][1] = cache.get(key);
      index++;
    }
    return contents;
  }

  private boolean debug() {
		return UIThreadLocal.isDebug();
	}
}
