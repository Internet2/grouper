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

package edu.internet2.middleware.grouper.ui;

import java.util.*;

/**
 * Convenience class which allows disparate parts of code keep track of goings
 * on within a thread
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: UIThreadLocal.java,v 1.1.1.1 2005-08-23 13:04:14 isgwb Exp $
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
		if (obj == null)
			obj = new HashMap();
		threadLocal.set(obj);
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
		if (current instanceof List) {
			((List) current).add(value);
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

}