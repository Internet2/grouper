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

import java.util.HashMap;

import org.apache.commons.beanutils.DynaBean;


/**
 * Base class for using a Map as a wrapper to an object
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ObjectAsMap.java,v 1.2 2005-12-08 15:31:42 isgwb Exp $
 */
public class ObjectAsMap extends HashMap {
	protected String objType = null;

	protected Object wrappedObject = null;
	protected DynaBean dynaBean=null;

	/**
	 * @return notional type of object
	 */
	public String getObjectType() {
		return objType;
	}

	/**
	 * @return object that was wrapped
	 */
	public Object getWrappedObject() {
		return this.wrappedObject;
	}

	/**
	 * @param key
	 *            to get
	 * @return value assumed to be String
	 */
	public String getString(String key) {
		return (String) get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		if ("wrappedObject".equals(key)) {
			return this.getWrappedObject();
		}
		return super.get(key);
	}
	protected Object getByIntrospection(Object key) {
		try {
			return dynaBean.get(key.toString());
		}catch(Exception e){return null;}
	}
}