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
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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
import java.util.*;

import edu.internet2.middleware.grouper.ui.UIThreadLocal;

/**
 * Given a list of bundles, looks for values in each individually until it finds
 * one
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ChainedResourceBundle.java,v 1.7 2008-11-04 07:17:59 mchyzer Exp $
 */

public class ChainedResourceBundle extends ResourceBundle implements
		Serializable {
	private ArrayList chain = new ArrayList();

	private String name = null;

	private String mapName = null;
	
	
	private HashMap cache = new HashMap();

	/**
	 * add to cache some extra params
	 * @param key
	 * @param value
	 */
	public void addToCache(String key, String value) {
	  this.cache.put(key, value);
	}
	
	/**
	 * Constructor - ensures atleast one bundle!
	 * 
	 * @param bundle
	 *            Resource Bundle
	 * @param name
	 *            of bundle which can be referred to elsewhere
	 */
	public ChainedResourceBundle(ResourceBundle bundle, String name) {
		if (bundle == null)
			throw new NullPointerException();
		if (name == null)
			throw new NullPointerException();
		this.name = name;
		this.mapName = name + "Map";
		chain.add(bundle);
	}

	/**
	 * Extend the chain
	 * 
	 * @param bundle
	 *            to add
	 */
	public synchronized void addBundle(ResourceBundle bundle) {
		if (bundle == null)
			throw new NullPointerException();
		if (chain.contains(bundle))
			throw new IllegalArgumentException(
					"This bundle has already been added");
		chain.add(bundle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
	 */
	protected Object handleGetObject(String key) {
		if(debug()) UIThreadLocal.put(name, key);
		Object obj = null;
		Boolean doShowResourcesInSitu = false;
		if(debug()) doShowResourcesInSitu=(Boolean) UIThreadLocal
		.get("doShowResourcesInSitu");
		if (doShowResourcesInSitu != null
				&& doShowResourcesInSitu.booleanValue()
				&& name.startsWith("nav")) {
			obj= "???" + key + "???";
			return obj;
		}
		
		ResourceBundle bundle = null;
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		obj = cache.get(key);
		if(obj != null) {
			if(debug())UIThreadLocal.put(mapName, key, obj);
			return obj;
		}
		for (int i = 0; i < chain.size(); i++) {
			bundle = (ResourceBundle) chain.get(i);
			try {
				obj = bundle.getString(key);
				if (obj != null) {
					if(debug())UIThreadLocal.put(mapName, key, obj);
					break;
				}
			} catch (Exception e) {
			}
		}
		
		if (doShowResourcesInSitu != null
				&& doShowResourcesInSitu.booleanValue()
				&& name.startsWith("nav"))
			obj= "???" + key + "???";
		
		cache.put(key, obj);
		return obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle#getKeys()
	 */
	public Enumeration getKeys() {
		Set keys = new HashSet();
		ResourceBundle bundle = null;
		Enumeration bundleKeys;
		for (int i = 0; i < chain.size(); i++) {
			bundle = (ResourceBundle) chain.get(i);
			bundleKeys = bundle.getKeys();
			while (bundleKeys.hasMoreElements()) {
				keys.add(bundleKeys.nextElement());
			}

		}
		Vector v = new Vector(keys);
		return v.elements();

	}
	
	private boolean debug() {
		return UIThreadLocal.isDebug();
	}
}
