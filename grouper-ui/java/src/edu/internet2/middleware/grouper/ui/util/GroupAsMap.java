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

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;

/**
 * Wraps a GrouperGroup - allows non persistent values to be stored for the UI
 * and works well with JSTL <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupAsMap.java,v 1.4 2006-02-22 10:17:26 isgwb Exp $
 */
public class GroupAsMap extends ObjectAsMap {
	//
	protected Group group = null;
	protected String objType="GrouperGroup";
	private GrouperSession grouperSession = null;
	
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
		put("subjectType","group");
		put("isGroup",Boolean.TRUE);
		put("id",group.getUuid());
		put("stem",group.getParentStem().getName());
		put("groupId",group.getUuid());
		put("subjectId",group.getUuid());
		put("desc",get("displayExtension"));
		try {
			Subject subj = SubjectFinder.findById(group.getUuid(),"group");
			put("source",subj.getSource().getName());
		}catch(Exception e) {
			
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		//Map would override GrouperGroup values
		Object obj=super.get(key);
		
		if(obj==null) {
			//No value, so check the wrapped group
			try{
			obj = group.getAttribute((String)key);
			}catch(Exception e){}
			
		}
		if(obj==null&& "description".equals(key)) obj = get("displayExtension");
		if(obj==null&& "types".equals(key)) {
			obj = group.getTypes();
			Set set = (Set)obj;
			try {
			GroupType gt = GroupTypeFinder.find("base");
				set.remove(gt);
			}catch(Exception e) {}
			if(set.isEmpty()) return null;
		}
		if(obj==null) obj="";
		return obj;
	}
}
