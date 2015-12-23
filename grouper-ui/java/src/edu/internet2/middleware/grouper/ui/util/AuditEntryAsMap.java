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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;

/**
 * Wraps a GrouperStem- allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: AuditEntryAsMap.java,v 1.4 2009-10-01 13:43:13 isgwb Exp $
 */
public class AuditEntryAsMap extends ObjectAsMap {
	
	
	protected AuditEntry entry = null;

	protected final static String objType = "AuditEntry";

	private GrouperSession grouperSession = null;
	
	private static Set<String> groupIdLabels = new HashSet<String>();
	private static Set<String> stemIdLabels = new HashSet<String>();
	private static Set<String> memberIdLabels = new HashSet<String>();
	private static Set<String> idLabels = new HashSet<String>();
	static {
		groupIdLabels.add("groupId");
		groupIdLabels.add("groupUuid");		
		groupIdLabels.add("oldGroupId");
		groupIdLabels.add("newGroupId");
		groupIdLabels.add("leftFactorId");
		groupIdLabels.add("rightFactorId");
		groupIdLabels.add("ownerId");
		stemIdLabels.add("stemId");
		stemIdLabels.add("oldStemId");
		stemIdLabels.add("newStemId");
		stemIdLabels.add("stemUuid");
		stemIdLabels.add("newStemUuid");
		memberIdLabels.add("memberId");
		memberIdLabels.add("newMemberId");
		memberIdLabels.add("oldMemberId");
		idLabels.add("group");
		idLabels.add("stem");
		idLabels.add("member");
		
	}
	
	protected AuditEntryAsMap() {}

	/**
	 * @param stem Stem to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public AuditEntryAsMap(AuditEntry entry, GrouperSession s) {
		super();
		grouperSession=s;
		init(entry);
	}
	
	protected void init(AuditEntry entry) {
		dynaBean = new WrapDynaBean(entry);
		super.objType = objType;
		if (entry == null)
			throw new NullPointerException(
					"Cannot create AuditEntryAsMap with a null AuditEntry");
		this.entry = entry;
		wrappedObject = entry;

		Map<String, Object> fields = new HashMap<String, Object>();
		Map<String, Object> fieldObjects = new HashMap<String, Object>();
		for (String label :  entry.getAuditType().labels()) {
			String fieldName = entry.getAuditType().retrieveAuditEntryFieldForLabel(label);
	        //Object value = GrouperUtil.fieldValue(this, fieldName);
			fields.put(label,entry.fieldValue(fieldName));
			Object obj = instantiateFieldObject(label,entry.fieldValue(fieldName));
			if(obj!=null) {
				fieldObjects.put(label, obj);
			}
		}
		this.put("fields", fields);
		this.put("fieldObjects", fieldObjects);
	}
	
	protected Object instantiateFieldObject(String label,Object value) {
		if(value==null) {
			return null;
		}
		if(groupIdLabels.contains(label) || 
				("id".equals(label) 
						&& ("group".equals(entry.getAuditType().getAuditCategory()) 
								))) {
			Group g = GroupFinder.findByUuid(grouperSession, (String)value, false);
			if(g==null) {
				return g;
			}
			return ObjectAsMap.getInstance("GroupAsMap", g,grouperSession);
		}
		
		if(stemIdLabels.contains(label) || ("id".equals(label) && "stem".equals(entry.getAuditType().getAuditCategory()))) {
			Stem s = StemFinder.findByUuid(grouperSession, (String)value, false);
			if(s==null) {
				return s;
			}
			return ObjectAsMap.getInstance("StemAsMap", s, grouperSession);
		}
		
		if(memberIdLabels.contains(label) || ("id".equals(label) && "member".equals(entry.getAuditType().getAuditCategory()))) {
			Member m = MemberFinder.findByUuid(grouperSession, (String)value, false);
			if(m==null) {
				return m;
			}
			return ObjectAsMap.getInstance("SubjectAsMap", m.getSubject());
		}
		
		if(("id".equals(label) && "groupType".equals(entry.getAuditType().getAuditCategory())) ||
				("groupTypeId".equals(label) && "groupField".equals(entry.getAuditType().getAuditCategory()))) {
			return GroupTypeFinder.findByUuid((String)value, false);
		}
		
		return null;
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
			if("duration".equals(key)) {
				double l = Math.round(((AuditEntry)wrappedObject).getDurationMicroseconds()/10000000)/10.0;
				return l * 10.0;
				
			}
			if("loggedInMember".equals(key)) {
				Member m = MemberFinder.findByUuid(grouperSession, entry.getLoggedInMemberId(), false);
				if(m==null) {
					return m;
				}
				return ObjectAsMap.getInstance("SubjectAsMap", m.getSubject());
			}
			if("formatLastUpdated".equals(key)) {
				Date date = entry.getLastUpdated();
				String df = getDateFormat();
				SimpleDateFormat sdf = new SimpleDateFormat(df);
				return sdf.format(date);
			}
			if("actAsMember".equals(key)) {
				String actAs = entry.getActAsMemberId();
				if(actAs==null) {
					return null;
				}
				Member m = MemberFinder.findByUuid(grouperSession, actAs, false);
				if(m==null) {
					return m;
				}
				return ObjectAsMap.getInstance("SubjectAsMap", m.getSubject());
			}
			obj=getByIntrospection(key);
		}
		if (obj == null)
			obj = "";
		return obj;
	}
	

}
