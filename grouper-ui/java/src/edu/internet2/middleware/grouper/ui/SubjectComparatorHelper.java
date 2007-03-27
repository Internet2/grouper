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
package edu.internet2.middleware.grouper.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import edu.internet2.middleware.grouper.GrouperSubject;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Implementation of ComparatorHelper used to sort Subjects. Can also sort
 * SubjectAsMaps, Members, Memberships, MembershipAsMaps which can all be
 * shown as Subjects. The context affects sorting thus:
 * <ul>
 * <li>If it starts 'search:', then context is changed to 'search' </li>
 * <li>Otherwise '&lt;type&gt;.sort.&lt;context&gt;', '&lt;type&gt;.sort.&lt;default&gt;', 'subject.sort.&lt;context&gt;',
 * 'subject.sort.default', 'subject.display.&lt;sourceId&gt;' and 'subject.display.default' are looked up until a value is found</li>
 * </ul>
 * If the sort field contains spaces, the field is 'split' and each sub part is assumed to be separate attribute to sort on.
 
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SubjectComparatorHelper.java,v 1.3 2007-03-27 14:16:04 isgwb Exp $
 */

public class SubjectComparatorHelper implements GrouperComparatorHelper{
	private Map partsCache = new HashMap();
	/**
	 * 
	 */
	public SubjectComparatorHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparatorHelper#getComparisonString(java.lang.Object, java.util.ResourceBundle, java.lang.String)
	 */
	public String getComparisonString(Object obj, ResourceBundle config,
			String context) {
		String attrStr=null;
		if(context.startsWith("search:")) {
			if(obj instanceof GrouperSubject) {
				attrStr=context.substring(7);
			}else{
				context="search";
			}
		}
		Subject subject=null;
		String type = null;
		if(obj instanceof Subject) {
			subject=(Subject)obj;
			type="subject";
		}else if(obj instanceof Member) {
			try {
				subject=((Member)obj).getSubject();
				type="member";
			}catch (SubjectNotFoundException e) {
				return "";
			}
		}else if(obj instanceof Membership) {
			try {
			subject=((Membership)obj).getMember().getSubject();
			type="membership";
			}catch(Exception e) {
				return "";
			}
		}else if(obj instanceof SubjectAsMap) {
			subject = (Subject)((SubjectAsMap)obj).getWrappedObject();
		}else if(obj instanceof MembershipAsMap) {
			try {
				subject = ((Membership)((MembershipAsMap)obj).getWrappedObject()).getMember().getSubject();
			}catch(Exception e) {
				return "";
			}
			}else{
			throw new IllegalStateException("Invalid object type");
		}
		String[] parts=(String[])partsCache.get(type + ".sort." + context + "." + subject.getSource().getId());
		
		if(parts==null) {
			
			if(attrStr==null) {
				try {
					attrStr=config.getString(type + ".sort." + context);
				}catch(Exception e){}
				}
			if(attrStr==null) {
				try {
					attrStr=config.getString(type + ".sort.default");
				}catch(Exception e){}
				
			}
			if(attrStr==null) {
				try {
					attrStr=config.getString("subject.sort." + context);
				}catch(Exception e){}
				
			}
			if(attrStr==null) {
				try {
					attrStr=config.getString("subject.sort.default");
				}catch(Exception e){}
				
			}
			if(attrStr==null) {
				try {
					attrStr=config.getString("subject.display." + subject.getSource().getId());
				}catch(Exception e){}
				
			}
			if(attrStr==null) {
				try {
					attrStr=config.getString("subject.display.default");
				}catch(Exception e){}
				
			}
			parts=attrStr.split(" ");
			partsCache.put(type + ".sort." + context+ "." + subject.getSource().getId(),parts);
		}
		StringBuffer sb=new StringBuffer();
		String val="";
		for(int i=0;i<parts.length;i++) {
			val="";
			if("name".equals(parts[i])) {
				val=subject.getName();
			}else if("description".equals(parts[i])) {
				val=subject.getDescription();
			}else {
				try {
					val=subject.getAttributeValue(parts[i]);
				}catch(Exception e){}
			}
			sb.append(val);
		}
		return sb.toString().toLowerCase();
	}
}
