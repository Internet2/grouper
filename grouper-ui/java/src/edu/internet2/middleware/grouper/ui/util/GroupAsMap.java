/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouper.GrouperAttribute;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFactory;
import edu.internet2.middleware.subject.Subject;

/**
 * Wraps a GrouperGroup - allows non persistent values to be stored for the UI
 * and works well with JSTL <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupAsMap.java,v 1.2 2005-10-20 14:40:56 isgwb Exp $
 */
public class GroupAsMap extends ObjectAsMap {
	//
	protected GrouperGroup group = null;
	protected String objType="GrouperGroup";
	private GrouperSession grouperSession = null;
	
	/**
	 * @param group GrouperGroup to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public GroupAsMap(GrouperGroup group,GrouperSession s) {
		super();
		super.objType = objType;
		if(group==null) throw new NullPointerException("Cannot create as GroupAsMap with a null group");
		this.group = group;
		wrappedObject=group;
		put("subjectType","group");
		put("isGroup",Boolean.TRUE);
		put("id",group.id());
		put("groupId",group.id());
		put("subjectId",group.id());
		put("desc",get("displayExtension"));
		try {
			Subject subj = SubjectFactory.getSubject(group.id(),"group");
			put("source",subj.getSource().getName());
		}catch(Exception e) {
			
		}
		put("name",group.name());

	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		//Map would override GrouperGroup values
		Object obj=super.get(key);
		
		if(obj==null) {
			//No value, so check the wrapped group
			GrouperAttribute ga = group.attribute((String)key);
			obj=ga.value();
		}
		if(obj==null&& "description".equals(key)) obj = get("displayExtension");
		if(obj==null) obj="";
		return obj;
	}
}
