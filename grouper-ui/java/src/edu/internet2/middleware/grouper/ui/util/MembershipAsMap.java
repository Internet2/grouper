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

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;

/**
 * Wraps a Subject - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MembershipAsMap.java,v 1.6 2008-03-03 13:54:52 isgwb Exp $
 */
public class MembershipAsMap extends ObjectAsMap {

	protected String objType = "Membership";

	private Membership membership = null;
	private boolean withParents=false;
	
	protected MembershipAsMap() {}
	/**
	 * @param membership to wrap

	 */
	public MembershipAsMap(Membership membership) {
		this(membership,false);
	}
	/**
	 * TODO check if withParents is redundant
	 * @param membership to wrap
	 * @param withParents parentMemberships?
	 */
	public MembershipAsMap(Membership membership,boolean withParents) {
		super();
		init(membership,withParents);
	}
	
	protected void init(Membership membership, boolean withParents) {
		super.objType = objType;
		if (membership == null)
			throw new NullPointerException(
					"Cannot create MembershipAsMap with a null Membership");
		this.membership = membership;
		this.withParents=withParents;
		wrappedObject = membership;
		try {
			put("memberUuid",GrouperHelper.getMemberUuid(membership));
			put("field",membership.getList());
		}catch(Exception e) {
			throw new RuntimeException(membershipToString(membership) + e.getMessage());
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
			try {
			if("subject".equals(key)) {
				obj = GrouperHelper.subject2Map(membership.getMember().getSubject());
				put(key,obj);
			}else if("group".equals(key)) {
				obj=GrouperHelper.group2Map(null,membership.getGroup());
				put(key,obj);
			}else if("viaGroup".equals(key)) {
				obj=GrouperHelper.group2Map(null,membership.getViaGroup());
				put(key,obj);
			}else if("parentMembership".equals(key)) {
				obj=ObjectAsMap.getInstance("MembershipAsMap",membership.getParentMembership());
				put(key,obj);
			}
			}catch(Exception e){}
			if(obj!=null) return obj;			
		}
		
		if(obj==null) obj="";
		return obj;
	}
	
	
	public String membershipToString(Membership mship) {
		Member m=null;
		try {
			m=mship.getMember();
		}catch(Exception e) {
			throw new RuntimeException("Problem getting member for Membership: " + mship.toString());
		}
		StringBuffer sb=new StringBuffer("Member Uuid: ");
		sb.append(m.getUuid());
		sb.append("\nSubject Id: ");
		sb.append(m.getSubjectId());
		sb.append("\nSubject Source: ");
		sb.append(m.getSubjectSourceId());
		sb.append("\nSubject type: ");
		sb.append(m.getSubjectTypeId());
		sb.append("\n");
		return sb.toString();
	}

	
}
