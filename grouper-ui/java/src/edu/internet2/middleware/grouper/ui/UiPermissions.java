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

package edu.internet2.middleware.grouper.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.subject.Subject;

/**
 * New in 1.2.1, a general means of specifying UI privileges. Initially implemented to allow 
 * selection of menu items, but hopefully will have wider application. Configured through 
 * resources/grouper/ui-permissions.xml (if present)
 * <pre>
&lt;ui-permissions&gt;<br>
  &lt;virtual-groups&gt;<br>
    &lt;virtual-group name=&quot;HighlyPrivileged&quot;&gt;<br>
      &lt;group name=&quot;qsuob:admins&quot;/&gt;<br>
    &lt;/virtual-group&gt;<br>
  &lt;/virtual-groups&gt;<br>
  &lt;permissions&gt;<br>
    &lt;permission can=&quot;view&quot; target=&quot;ManageGroups&quot; if-member-of=&quot;HighlyPrivileged&quot; 
is=&quot;false&quot;/&gt;<br>
  &lt;/permissions&gt;<br>
&lt;/ui-permissions&gt; 
 * </pre>
 * if-member-of can be an actual group name. If using virtual groups, if-member-of  evaluates to true 
 * if the current Subject is a member of any of the nested groups. In the example shown, members of qsuob:admins
 * do not get the <i>Manage Groups</i> menu item. <i>Manage Groups</i> does not currently scale well for a Subject
 * who has privileges over thousands of groups.
 * <p>
 * NB. The format above is illustrative. In the case shown it is not necessary to use a virtual group. 
 * The following would have worked just as well:
 * <pre>
&lt;ui-permissions&gt;<br>
  &lt;permissions&gt;<br>
    &lt;permission can=&quot;view&quot; target=&quot;ManageGroups&quot; if-member-of=&quot;qsuob:admins&quot; 
is=&quot;false&quot;/&gt;<br>
  &lt;/permissions&gt;<br>
&lt;/ui-permissions&gt;
 * </pre>
 * </p>
 * 
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: UiPermissions.java,v 1.4 2009-08-12 04:52:14 mchyzer Exp $
 */

public class UiPermissions implements Serializable {
	Map vGroups = new HashMap();
	Map targets = new HashMap();
	public UiPermissions(GrouperSession s,Document doc) {
		NodeList nl = doc.getElementsByTagName("virtual-group");
		Element el;
		VirtualGroup vGroup = null;
		String group;
		NodeList groups;
		Element elGroup;
		for(int i=0;i<nl.getLength();i++) {
			el=(Element) nl.item(i);
			vGroup=new VirtualGroup(el.getAttribute("name"));
			vGroups.put(el.getAttribute("name"), vGroup);
			groups=el.getElementsByTagName("group");
			for(int j=0;j<groups.getLength();j++) {
				elGroup = (Element)groups.item(j);
				vGroup.addGroup(s,elGroup.getAttribute("name"));
			}
		}
		
		NodeList permissions = doc.getElementsByTagName("permission");
		String target;
		String can;
		String ifMemberOf;
		String is;
		Target t;
		Permission p;
		for(int i=0;i<permissions.getLength();i++) {
			el=(Element) permissions.item(i);
			target=el.getAttribute("target");
			can=el.getAttribute("can");
			ifMemberOf=el.getAttribute("if-member-of");
			is=el.getAttribute("is");
			t=getTarget(target);
			if(t==null) {
				t=new Target(target);
				targets.put(target,t);
			}
			p = new Permission(s,target,can,Boolean.parseBoolean(is),ifMemberOf);
			t.addPermission(p);
		}
	}
	
	protected VirtualGroup getVirtualGroup(String name) {
		return (VirtualGroup)vGroups.get(name);
	}
	
	protected Target getTarget(String name) {
		return (Target)targets.get(name);
	}
	
	/**
	 * @param subj
	 * @param target
	 * @param action
	 * @return whether the subject can carry out the specified action on the specified target
	 */
	public boolean can(Subject subj,String target,String action) {
		Target t = (Target)targets.get(target);
		if(t==null) return true;
		return t.can(subj, action);
	}

	protected class VirtualGroup {
		private String name;
		private Set groups=new LinkedHashSet();	
		
		protected VirtualGroup(String name) {
			this.name=name;
		}
		protected void addGroup(GrouperSession s,String name) {
			VirtualGroup vg = getVirtualGroup(name);
			if(vg!=null) {
				groups.addAll(vg.getGroups());
			}else{
				Group group = null;
				try {
					group=GroupFinder.findByName(GrouperHelper.getRootGrouperSession(s), name, true);
				}catch(GroupNotFoundException e) {}
				if(group!=null) groups.add(group);
			}
		}
		protected boolean hasMember(Subject subj) {
			Iterator it=groups.iterator();
			Group g;
			while(it.hasNext()) {
				g=(Group)it.next();
				if(g.hasMember(subj)) return true;
			}
			return false;
		}
		protected String getName() {
			return name;
		}
		protected Set getGroups() {
			return groups;
		}
		
	}
	
	protected class Target {
		private Map permissions = new HashMap();
		private String name;
		protected Target(String name) {
			this.name=name;
		}
		protected void addPermission(Permission p) {
			permissions.put(p.getAction(), p);
		}
		
		protected boolean can(Subject subj,String action) {
			Permission p = (Permission) permissions.get(action);
			if(p==null)	return true;
			return p.can(subj);
		}
	}
	
	protected class Permission {
		private String target;
		private String action;
		private boolean isMember;
		private VirtualGroup vGroup;
		private String group;
		
		protected Permission(GrouperSession s,String target,String action,boolean isMember,String group) {
			this.target=target;
			this.action=action;
			this.isMember=isMember;
			this.group=group;
			vGroup = getVirtualGroup(group);
			if(vGroup==null) {
				vGroup = new VirtualGroup("_v-" + group);
				vGroup.addGroup(s, group);
			}
		}

		protected String getTarget() {
			return target;
		}

		protected String getAction() {
			return action;
		}

		protected boolean isMember() {
			return isMember;
		}

		protected VirtualGroup getGroup() {
			return vGroup;
		}
		
		protected boolean can(Subject subj) {
			boolean inVGroup=vGroup.hasMember(subj);
			if(inVGroup && isMember ) return true;
			if(!inVGroup && !isMember) return true;
			return false;
		}
		
	}
 }
