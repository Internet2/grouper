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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;
import edu.internet2.middleware.subject.Subject;


/**
 * Base class for using a Map as a wrapper to an object
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ObjectAsMap.java,v 1.10 2009-10-16 12:16:32 isgwb Exp $
 */
public class ObjectAsMap extends HashMap {
	protected String objType = null;

	protected Object wrappedObject = null;
	protected transient DynaBean dynaBean=null;
	protected String dateFormat="dd MMM yyyy HH:mm:ss";
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
		if (dynaBean==null) dynaBean=new WrapDynaBean(wrappedObject);
		try {
			return dynaBean.get(key.toString());
		}catch(Exception e){return null;}
	}
	
	/**
	 * Rather than use a constructor directly, the UI reads the implementation type
	 * from media.properties. This allows sites to provide alternative implementations
	 * @param type
	 * @param object
	 * @param grouperSession
	 * @return subclass as configured in media.properties
	 */
	public static ObjectAsMap getInstance(String type, Object object, GrouperSession grouperSession) {
		
		ResourceBundle mediaBundle = (ResourceBundle) UIThreadLocal.get("mediaBundle");
		String claz = mediaBundle.getString("objectasmap." + type + ".impl");
		
		try {
			Class impl = Class.forName(claz);
			Constructor c = impl.getConstructor(object.getClass(), GrouperSession.class);
			ObjectAsMap result = (ObjectAsMap) c.newInstance(object, grouperSession);
		
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
/**
 * Rather than use a constructor directly, the UI reads the implementation type
 * from media.properties. This allows sites to provide alternative implementations
 * @param type
 * @param subject
 * @param grouperSession
 * @param groupOrStem
 * @param privilege
 * @return subclass as configured in media.properties
 */
public static ObjectAsMap getInstance(String type, Subject subject, GrouperSession grouperSession,GroupOrStem groupOrStem,String privilege) {
		
		ResourceBundle mediaBundle = (ResourceBundle) UIThreadLocal.get("mediaBundle");
		String claz = mediaBundle.getString("objectasmap." + type + ".impl");
		
		try {
			Class impl = Class.forName(claz);
			Constructor c = impl.getConstructor(GrouperSession.class,Subject.class,groupOrStem.getClass(),privilege.getClass());
			ObjectAsMap result = (ObjectAsMap) c.newInstance( grouperSession,subject,groupOrStem,privilege);
		
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
/**
 * Rather than use a constructor directly, the UI reads the implementation type
 * from media.properties. This allows sites to provide alternative implementations
 * @param type
 * @param object
 * @return subclass as configured in media.properties
 */
public static ObjectAsMap getInstance(String type, Object object) {
	
	ResourceBundle mediaBundle = (ResourceBundle) UIThreadLocal.get("mediaBundle");
	String claz = mediaBundle.getString("objectasmap." + type + ".impl");
	
	try {
		Class impl = Class.forName(claz);
		Constructor c = impl.getConstructor(object.getClass());
		ObjectAsMap result = (ObjectAsMap) c.newInstance(object);
	
		return result;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

/**
 * Rather than use a constructor directly, the UI reads the implementation type
 * from media.properties. This allows sites to provide alternative implementations
 * @param type
 * @param object
 * @return subclass as configured in media.properties
 */
public static ObjectAsMap getInstance(String type, Subject object) {
	
	ResourceBundle mediaBundle = (ResourceBundle) UIThreadLocal.get("mediaBundle");
	String claz = mediaBundle.getString("objectasmap." + type + ".impl");
	
	try {
		Class impl = Class.forName(claz);
		Constructor c = impl.getConstructor(Subject.class);
		ObjectAsMap result = (ObjectAsMap) c.newInstance(object);
	
		return result;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
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
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	
}