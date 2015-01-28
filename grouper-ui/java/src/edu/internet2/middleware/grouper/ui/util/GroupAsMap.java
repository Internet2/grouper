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

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.subject.Source;

/**
 * Wraps a GrouperGroup - allows non persistent values to be stored for the UI
 * and works well with JSTL <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupAsMap.java,v 1.17 2009-11-07 15:34:54 isgwb Exp $
 */
public class GroupAsMap extends ObjectAsMap {
	//
	protected Group group = null;
	protected String objType="GrouperGroup";
	private GrouperSession grouperSession = null;
	private static Source source;
	
	/**
	 * @param group GrouperGroup to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public GroupAsMap(Group group,GrouperSession s) {
		super();
		super.objType = objType;
		if(group==null) throw new NullPointerException("Cannot create as GroupAsMap with a null group");
		this.group = group;
		wrappedObject=group;
		dynaBean = new WrapDynaBean(group);
		put("subjectType","group");
		put("isGroup",Boolean.TRUE);
		put("id",group.getUuid());
		
		put("groupId",group.getUuid());
		put("subjectId",group.getUuid());
    put("group",group);
		
		put("desc",get("displayExtension"));
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		if (key.equals("alternateName")) {
			Iterator<String> alternateNamesIterator = group.getAlternateNames().iterator();
			if (alternateNamesIterator.hasNext()) {
				return alternateNamesIterator.next();
			} else {
				return null;
			}
		}

		Object obj = getByIntrospection(key);
		if(obj!=null) return obj;
		//Map would override GrouperGroup values
		obj=super.get(key);
		
		if(obj==null) {
			//No value, so check the wrapped group
			if("hasComposite".equals(key)) {
				obj = new Boolean(group.hasComposite());
				put(key,obj);
			}else if("stem".equals(key)) {
				obj=group.getParentStem().getName();
				put(key,obj);
			}else if("source".equals(key)) {
				if(source==null) {
					try {
						source=group.toSubject().getSource();
					}catch(Exception e) {
						
					}
					put("source",source);
				}
				return source;
			}
			if(obj!=null) return obj;
			try{
			  AttributeDefName legacyAttribute = AttributeDefNameFinder.findByName((String)key, true);
				obj = group.getAttributeValue(legacyAttribute.getLegacyAttributeName(true), true, false);
			}catch(Exception e){}
			
		}
		
		if(obj==null&& "description".equals(key)) obj = get("displayExtension");
		if(obj==null&& "types".equals(key)) {
			obj = group.getTypes();
			Set set = (Set)obj;
			try {
			GroupType gt = GroupTypeFinder.find("base", true);
				set.remove(gt);
			}catch(Exception e) {}
			if(set.isEmpty()) return null;
		}
		if(obj==null) obj="";
		return obj;
	}
	
	protected Set getExtraKeys() {
		Set keys  = new HashSet();
		//keys.add("types");
		return keys;
	}

}
