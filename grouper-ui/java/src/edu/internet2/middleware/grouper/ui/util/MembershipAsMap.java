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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Wraps a Subject - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MembershipAsMap.java,v 1.2 2006-07-14 11:04:11 isgwb Exp $
 */
public class MembershipAsMap extends ObjectAsMap {

	protected String objType = "Membership";

	private Membership membership = null;
	private boolean withParents=false;
	
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
		super.objType = objType;
		if (membership == null)
			throw new NullPointerException(
					"Cannot create MembershipAsMap with a null Membership");
		this.membership = membership;
		this.withParents=withParents;
		wrappedObject = membership;
		try {
			put("subject",GrouperHelper.subject2Map(membership.getMember().getSubject()));
			put("group",GrouperHelper.group2Map(null,membership.getGroup()));
			put("field",membership.getList());
			try{
				put("viaGroup",GrouperHelper.group2Map(null,membership.getViaGroup()));
			}catch(Exception igex){}
			try{
				put("parentMembership",new MembershipAsMap(membership.getParentMembership()));
			}catch(Exception igex){}
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	
}