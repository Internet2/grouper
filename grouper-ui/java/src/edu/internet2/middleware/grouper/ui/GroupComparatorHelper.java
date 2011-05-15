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

import java.util.ResourceBundle;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;


/**
 * Implementation of ComparatorHelper used to sort Groups and GroupAsMaps. The context affects sorting thus:
 * <ul>
 * <li>If it starts 'search:', then anything following : is used as the sort field </li>
 * <li>If it equals 'flat', the media property 'group.sort.flat' is looked up. If that does not exist 'group.display.flat' is looked up</li>
 * <li>Otherwise 'group.sort.&lt;context&gt;', 'group.sort.&lt;default&gt;' and 'group.display' are looked up until a value is found</li>
 * </ul>
 * If the sort field contains spaces, the field is 'split' and each sub part is assumed to be separate attribute to sort on.
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupComparatorHelper.java,v 1.4 2009-11-07 14:46:34 isgwb Exp $
 */

public class GroupComparatorHelper implements GrouperComparatorHelper{

	/**
	 * 
	 */
	public GroupComparatorHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparatorHelper#getComparisonString(java.lang.Object, java.util.ResourceBundle, java.lang.String)
	 */
	public String getComparisonString(Object obj, ResourceBundle config,
			String context) {
		Group group = null;
		if(obj instanceof Group) {
			group=(Group)obj;
		}else if(obj instanceof GroupAsMap) {
			group = (Group)((GroupAsMap)obj).getWrappedObject();
		}else if(obj instanceof Membership) {
			group = ((Membership)obj).getGroup();
		}else if(obj instanceof MembershipAsMap) {
			group = ((Membership)((MembershipAsMap)obj).getWrappedObject()).getGroup();
		}else{
			throw new IllegalArgumentException(obj + " is not a Group");
		}
		String attrStr=null;
		if("flat".equals(context)) {
			try {
				attrStr=config.getString("group.sort.flat");
			}catch(Exception e){}
			if(attrStr==null) {
				try {
					attrStr=config.getString("group.display.flat");
				}catch(Exception e){}
			}
		}else if(context.startsWith("search:")) {
			attrStr=context.substring(7);
		}else{
			try {
				attrStr=config.getString("group.sort." + context);
			}catch(Exception e){}
			
			if(attrStr==null) {
				try {
					attrStr=config.getString("group.sort.default");
				}catch(Exception e){}	
			}
			
			if(attrStr==null) {
				try {
					attrStr=config.getString("group.display");
				}catch(Exception e){}
				
			}
		}
		
		String[] parts=attrStr.split(" ");
		StringBuffer sb=new StringBuffer();
		String val="";
		String attrName;
		for(int i=0;i<parts.length;i++) {
			val="";
			attrName=parts[i];
			if(attrName.equals("extension")) {
				val=group.getExtension();
			}else if(attrName.equals("displayExtension")) {
				val=group.getDisplayExtension();
			}else if(attrName.equals("name")) {
				val=group.getName();
			}else if(attrName.equals("displayName")) {
				val=group.getDisplayName();
			}else {
				try {
					val=group.getAttributeValue(attrName, false, false);
				}catch(Exception e){}
			}
			sb.append(val);
		}
		boolean sortLowercase = true;
		try {
			if("false".equals(config.getString("comparator.sort.lowercase"))) sortLowercase=false;
		}catch(Exception e) {}
		if(sortLowercase) {
			return sb.toString().toLowerCase();
		}
		return sb.toString();
	}
}
