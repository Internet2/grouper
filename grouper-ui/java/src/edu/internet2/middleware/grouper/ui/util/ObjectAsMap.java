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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.beanutils.*;



/**
 * Base class for using a Map as a wrapper to an object
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ObjectAsMap.java,v 1.6 2008-01-09 13:54:31 isgwb Exp $
 */
public class ObjectAsMap extends HashMap {
	protected String objType = null;

	protected Object wrappedObject = null;
	protected DynaBean dynaBean=null;
	public ObjectAsMap() {
		
	}
	public ObjectAsMap(Object obj, String type) {
		wrappedObject=obj;
		objType=type;
	}
	
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
	
	public Set keySet() {
		// TODO Auto-generated method stub
		Set keys = new LinkedHashSet();
		keys.addAll(super.keySet());
		keys.addAll(getExtraKeys());
		if (dynaBean==null) dynaBean=new WrapDynaBean(wrappedObject);
		DynaProperty[] props = dynaBean.getDynaClass().getDynaProperties();
		for(int i=0;i<props.length;i++) {
			if(isValidDynaProperty(props[i])) keys.add(props[i].getName());
		}
		return keys;
	}
	
	private boolean isValidDynaProperty(DynaProperty prop) {
		if(prop.isIndexed() || prop.isMapped()) return false;
		Class type = prop.getType();
		if(type.equals(String.class)) return true;
		if(type.equals(Boolean.class)) return true;
		if(type.equals(Integer.class)) return true;
		return false;
	}
	
	protected java.util.Set getExtraKeys() {
		return new HashSet();
	}
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return keySet().size()==0;
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return keySet().size();
	}
	@Override
	public Set entrySet() {
		// TODO Auto-generated method stub
		HashMap map = new HashMap();
		
		Iterator it = keySet().iterator();
		Object key;
		while(it.hasNext()) {
			key = it.next();
			map.put(key, get(key));
		}
		return map.entrySet();
	}
	
	
}