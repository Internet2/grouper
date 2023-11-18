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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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

import java.util.Map;
/**
 * GrouperGroup and GrouperStem used to be related. This is a convenience 
 * class for dealing with an id which can be for a group or a stem
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupOrStem.java,v 1.7 2009-03-15 06:37:51 mchyzer Exp $
 */

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;

public class GroupOrStem {
	private Group group = null;
	private Stem stem = null;
	private GrouperSession s = null;
	
	/**
	 * Already have a stem but a method needs GroupOrStem
	 * @param s
	 * @param stem
	 * @return a GroupOrStem based on the stem provided
	 */
	public static GroupOrStem findByStem(GrouperSession s,Stem stem) {
		GroupOrStem groupOrStem = new GroupOrStem();
		groupOrStem.s=s;
		groupOrStem.stem=stem;
		return groupOrStem;
	}
	
	/**
	 * Already have a group but a method needs GroupOrStem
	 * @param s
	 * @param group
	 * @return a GroupOrStem based on the group provided
	 */
	public static GroupOrStem findByGroup(GrouperSession s,Group group) {
		GroupOrStem groupOrStem = new GroupOrStem();
		groupOrStem.s=s;
		groupOrStem.group=group;
		return groupOrStem;
	}
	
	/**
	 * Only have and id ...
	 * @param s
	 * @param id
	 * @return a GroupOrStem based on the id provided
	 */
	public static GroupOrStem findByID(GrouperSession s,String id) {
		GroupOrStem groupOrStem = new GroupOrStem();
		groupOrStem.s = s;
		if(GrouperHelper.NS_ROOT.equals(id)) {
			groupOrStem.stem=StemFinder.findRootStem(s);
			return groupOrStem;
		}
		try {
			Group group = GroupFinder.findByUuid(s,id, true);
			groupOrStem.group = group;
			
		}catch(Exception e) {
			try {
				Stem stem = StemFinder.findByUuid(s,id, true);
				groupOrStem.stem = stem;
			}catch(Exception se) {
				throw new MissingGroupOrStemException("Unable to instantiate a group or stem with ID=" + id);
			}
		}
		return groupOrStem;
	}
	
	/**
	 * Only have a name...
	 * @param s
	 * @param name
	 * @return a GroupOrStem based on the name provided
	 */
	public static GroupOrStem findByName(GrouperSession s,String name) {
		GroupOrStem groupOrStem = new GroupOrStem();
		groupOrStem.s = s;
		try {
			Group group = GroupFinder.findByName(s,name, true);
			groupOrStem.group = group;
			
		}catch(Exception e) {
			try {
				Stem stem = StemFinder.findByName(s,name, true);
				groupOrStem.stem = stem;
			}catch(Exception se) {
				throw new MissingGroupOrStemException("Unable to instatiate a group or stem with name=" + name);
			}
		}
		return groupOrStem;
	}
	
	/**
	 * Don't just let any one make a GroupOrStem
	 */
	private GroupOrStem() {
		
	}
	
	/**
	 * 
	 * @return id of 'masked' group or stem
	 */
	public String getId() {
		if(group!=null) return group.getUuid();
		return stem.getUuid();
	}
	
	/**
	 * @return if 'masked' object is a Group
	 */
	public boolean isGroup() {
		return (group != null);
	}
	
	/**
	 * @return if 'masked' object is a Stem
	 */
	public boolean isStem() {
		return (stem != null);
	}
	
	/**
	 * @return 'masked' object as a Map
	 */
	public Map getAsMap() {
		if(isGroup())
			return GrouperHelper.group2Map(s,getGroup());
		return GrouperHelper.stem2Map(s,getStem());
	}
	
	/**
	 * @return masked Group (or null if is a Stem)
	 */
	public Group getGroup() {
		return group;
	}
	
	/**
	 * @return masked Stem (or null if is a Group)
	 */
	public Stem getStem() {
		return stem;
	}
	
	/**
	 * @return display extension for wrapped group or stem
	 */
	public String getDisplayExtension() {
		if(group!=null) return group.getDisplayExtension();
		if(stem!=null) return stem.getDisplayExtension();
		throw new IllegalStateException("GroupOrStem is not initialised");
	}
	
	/**
	 * @return display name for wrapped group or stem
	 */
	public String getDisplayName() {
		if(group!=null) return group.getDisplayName();
		if(stem!=null) return stem.getDisplayName();
		throw new IllegalStateException("GroupOrStem is not initialised");
	}
	
	/**
	 * @return name for wrapped group or stem
	 */
	public String getName() {
		if(group!=null) return group.getName();
		if(stem!=null) return stem.getName();
		throw new IllegalStateException("GroupOrStem is not initialised");
	}
	
	/**
	 * @return 'group' or 'stem' depending on the wrapped object
	 */
	public String getType() {
		if(group!=null) return "group";
		if(stem!=null) return "stem";
		throw new IllegalStateException("GroupOrStem is not initialised");
	}
}
