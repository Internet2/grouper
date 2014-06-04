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
package edu.internet2.middleware.grouper.ui;

import java.util.ResourceBundle;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;

/**
 * Implementation of ComparatorHelper used to sort Stems. The context affects sorting thus:
 * <ul>
 * <li>If it starts 'search:', then anything following : is used as the sort field </li>
 * <li>Otherwise 'stem.sort.&lt;context&gt;', 'stem.sort.&lt;default&gt;' and 'stem.display' are looked up until a value is found</li>
 * </ul>
 * If the sort field contains spaces, the field is 'split' and each sub part is assumed to be separate attribute to sort on.
 *
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: StemComparatorHelper.java,v 1.2 2008-03-03 13:58:25 isgwb Exp $
 */

public class StemComparatorHelper implements GrouperComparatorHelper{

	/**
	 * 
	 */
	public StemComparatorHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	/**
	 * 
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparatorHelper#getComparisonString(java.lang.Object, java.util.ResourceBundle, java.lang.String)
	 */
	public String getComparisonString(Object obj, ResourceBundle config,
			String context) {
		Stem stem = null;
		if(obj instanceof Stem) {
			stem=(Stem)obj;
		}else if(obj instanceof StemAsMap) {
			stem = (Stem)((StemAsMap)obj).getWrappedObject();
		}else if(obj instanceof Membership) {
			stem = ((Membership)obj).getStem();
		}else if(obj instanceof MembershipAsMap) {
			stem = ((Membership)((MembershipAsMap)obj).getWrappedObject()).getStem();
		
		}else{
			throw new IllegalArgumentException(obj + " is not a Stem");
		}
		
		String attrStr=null;
		if(context.startsWith("search:")) {
			attrStr=context.substring(7);
		}else{
			try {
				attrStr=config.getString("stem.sort." + context);
			}catch(Exception e){}
			if(attrStr==null) {
				try {
					attrStr=config.getString("stem.sort.default");
				}catch(Exception e){}
				
			}
			if(attrStr==null) {
				try {
					attrStr=config.getString("stem.display");
				}catch(Exception e){}
				
			}
		}
		String[] parts=attrStr.split(" ");
		StringBuffer sb=new StringBuffer();
		String val="";
		for(int i=0;i<parts.length;i++) {
			val="";
			try {
				val=getStemAttribute(stem,parts[i]);
			}catch(Exception e){}
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
	
	/**
	 * 
	 * @param stem
	 * @param attr
	 * @return string
	 */
	private String getStemAttribute(Stem stem, String attr) {
		if("extension".equals(attr)) {
			return stem.getExtension();
		}else if("displayExtension".equals(attr)) {
			return stem.getDisplayExtension();
		}else if("name".equals(attr)) {
			return stem.getName();
		}else if("displayName".equals(attr)) {
			return stem.getDisplayName();
		}else if("deascription".equals(attr)) {
			return stem.getDescription();
		}
		return "";
		
	}
}
