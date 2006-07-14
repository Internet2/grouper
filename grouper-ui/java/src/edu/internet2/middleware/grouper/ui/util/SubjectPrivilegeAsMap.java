/*
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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

import java.util.Map;

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.subject.Subject;

/**
 * Wraps a Subject a group/stem and privilege - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SubjectPrivilegeAsMap.java,v 1.2 2006-07-14 11:04:11 isgwb Exp $
 */
public class SubjectPrivilegeAsMap extends ObjectAsMap {

	protected String objType = "SubjectPrivilege";

	private Subject subject = null;
	private GroupOrStem groupOrStem = null;
	private String privilege = null;
	private GrouperSession s=null;

	/**
	 * @param subject
	 *            to wrap
	 */
	public SubjectPrivilegeAsMap(GrouperSession s,Subject subject,GroupOrStem groupOrStem,String privilege) {
		super();
		super.objType = objType;
		if (subject == null)
			throw new NullPointerException(
					"Cannot create SubjectPrivilegeAsMap with a null Subject");
		if (groupOrStem == null)
			throw new NullPointerException(
					"Cannot create SubjectPrivilegeAsMap with a null GroupOrStem");
		if (privilege == null)
			throw new NullPointerException(
					"Cannot create SubjectPrivilegeAsMap with a null privilege");
		
		this.subject = subject;
		this.groupOrStem=groupOrStem;
		this.privilege=privilege;
		this.s=s;
		wrappedObject = subject;
		try {
			if(groupOrStem.isGroup())
				put("type","access");
			else
				put("type","naming");
			
			put("subject",GrouperHelper.subject2Map(subject));
			put("groupOrStem",GrouperHelper.group2Map(null,groupOrStem));
			put("privilege",privilege);
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		//Map would override GrouperGroup values
		Object obj = super.get(key);
		if (obj == null) {
			//No value, so check the wrapped stem
			if("isDirect".equals(key)) {
				try {
					Map tmpPrivMap = GrouperHelper.getImmediateHas(s,groupOrStem,MemberFinder.findBySubject(s,subject));
					Boolean answer = new Boolean(tmpPrivMap.containsKey(privilege.toUpperCase()));
					put("isDirect",answer);
					return answer;
				}catch(Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}	
		}
		if (obj == null)
			obj = "";
		return obj;
	}
	
}