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

package edu.internet2.middleware.grouper.ui;

import java.util.*;

/**
 * Convenience class which allows disparate parts of code keep track of goings
 * on within a thread
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: UIThreadLocal.java,v 1.6 2008-04-04 13:53:13 isgwb Exp $
 */
public class UIThreadLocal {
	private static ThreadLocal threadLocal = new ThreadLocal();

	/**
	 * Ensures there is a clean Map
	 * 
	 * @return Map
	 */
	private static Map init() {
		Object obj = threadLocal.get();
		if (obj == null) {
			obj = new HashMap();
			threadLocal.set(obj);
			((HashMap)obj).put("debugActive", false);
		}
		return (Map) obj;
	}

	/**
	 * Resets any values - use at beginning of Thread use e.g. Filter in servlet
	 * engine
	 *  
	 */
	public static void clear() {
		Map map = init();
		map.clear();
		UIThreadLocal.setDebug(false);
	}

	/**
	 * Stores a value. If there is an existing value for the key which is a
	 * List, then it appends the value to that List
	 * 
	 * @param key
	 *            to set
	 * @param value
	 *            for key
	 */
	public static void put(String key, Object value) {
		Map map = init();
		Object current = map.get(key);
		if (current instanceof Collection) {
			((Collection) current).add(value);
			return;
		}
		map.put(key, value);
	}

	/**
	 * Replaces any current value for the key with the new value - regardless of
	 * type. Use instead of put when appending to existing List should not hapen
	 * 
	 * @param key
	 *            to set
	 * @param value
	 *            for key
	 */
	public static void replace(String key, Object value) {
		Map map = init();
		map.put(key, value);
	}

	/**
	 * If value indicated by key is itself a Map, put(key1,value) If no value
	 * create a new HashMap first
	 * 
	 * @param key
	 *            for nested Map
	 * @param key1
	 *            to set
	 * @param value
	 *            for key1
	 */
	public static void put(String key, String key1, Object value) {
		Map map = init();
		Object current = map.get(key);
		if (current == null) {
			current = new HashMap();
			map.put(key, current);
		}
		if (current instanceof Map) {
			((Map) current).put(key1, value);
			return;
		}

	}

	/**
	 * Retrieve object previously saved
	 * 
	 * @param key
	 *            to retrieve
	 * @return Object previously set
	 */
	public static Object get(String key) {
		Map map = init();
		Object current = map.get(key);
		return current;
	}

	/**
	 * Retrieve nested value from Map defined by key
	 * 
	 * @param key
	 *            for nested Map
	 * @param key1
	 *            for value to retrieve
	 * @return value for key1
	 */
	public static Object get(String key, String key1) {
		Map map = init();
		Object current = map.get(key);
		if (current != null && current instanceof Map) {
			return ((Map) current).get(key1);
		}
		return current;
	}
	
	/**
	 * Set debug mode
	 * @param debug
	 */
	public static void setDebug(boolean debug) {
		UIThreadLocal.put("debugActive", debug);
	}
	
	/**
	 * @return true if debug mode is on
	 */
	public static boolean isDebug() {
		return (Boolean)UIThreadLocal.get("debugActive");
	}
}
