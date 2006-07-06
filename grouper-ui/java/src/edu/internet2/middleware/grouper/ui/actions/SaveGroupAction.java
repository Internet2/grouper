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

package edu.internet2.middleware.grouper.ui.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Privilege;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which saves a group - creating it first if it does not 
 * exist. 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group to save</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupName,groupType,groupDisplayName,<br />
        groupDescription</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Values retrieved from DynaActionForm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupTypes</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of group types assigned 
      to the group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      group but not assign membership or privileges</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.saveAndAddComposite</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      group and then add a composite member</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">message instance: text derived 
      <br>
      from groups.message.error.invalid-char or groups.message.group-saved key 
      in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set because may be new id for 
      new group</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to groupId if user indicates 
      they want to find new members / privilegees</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">browseNodeId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If new Group need to set stem 
      to current node</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>
 * @author Gary Brown.
 * @version $Id: SaveGroupAction.java,v 1.9 2006-07-06 10:32:26 isgwb Exp $
 */
public class SaveGroupAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupSummary = "GroupSummary";

	static final private String FORWARD_EditGroupAttributes = "EditGroupAttributes";

	static final private String FORWARD_EditAgain = "EditAgain";

	static final private String FORWARD_CreateAgain = "CreateAgain";

	static final private String FORWARD_GroupMembers = "GroupMembers";
	
	static final private String FORWARD_AddComposite = "AddComposite";

	static final private String FORWARD_FindMembers = "FindMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm groupForm = (DynaActionForm) form;
		boolean groupExists = false;
		String curNode = (String)groupForm.get("groupId");

		if (curNode == null || "".equals(curNode)) {
			//Need to find the stem in which new group wuill be created
			
			curNode = getBrowseNode(session);
		} else {
			groupExists = true;
		}
		if (curNode == null || "".equals(curNode)) {
			String defaultStem = getDefaultRootStemName(session);
			Stem root = StemFinder.findByName(grouperSession, defaultStem);
			curNode = root.getUuid();
		}

		//TODO: should be checked by the API and exception thrown?
		String groupName = (String) groupForm.get("groupName");
		if (!groupExists && !groupName.matches("[^ \"<>\\*]+")) {
			request.setAttribute("message", new Message(
					"groups.message.error.invalid-char", true));
			if (groupExists) {
				return mapping.findForward(FORWARD_EditAgain);
			} else {
				return mapping.findForward(FORWARD_CreateAgain);
			}
		}
		if ("".equals(groupForm.get("groupDisplayName")))
			groupForm.set("groupDisplayName", groupName);
		Group group = null;
		String id = curNode;
		String extension = (String) groupForm.get("groupName");
		String displayExtension = (String) groupForm.get("groupDisplayName");
		if(isEmpty(displayExtension))displayExtension=extension;
		//TODO: should be transactional - so add map or List of attributes
		Map assignedPrivs=null;
		Subject grouperAll = SubjectFinder.findById("GrouperAll");
		if (groupExists) {
			group = GroupFinder.findByUuid(grouperSession, curNode);
			doTypes(group,request);
			group.setDisplayExtension(displayExtension);
			group.setExtension(extension);
			Map selectedPrivs = GrouperHelper.getImmediateHas(grouperSession,GroupOrStem.findByGroup(grouperSession,group),MemberFinder.findBySubject(grouperSession,grouperAll));
			assignedPrivs=new HashMap();
			Map.Entry entry;
			String key;
			Iterator it = selectedPrivs.entrySet().iterator();
			while(it.hasNext()) {
				entry = (Map.Entry)it.next();
				key = (String)entry.getKey();
				assignedPrivs.put(key.toLowerCase(),entry.getValue());
			}
		} else {
			Stem parent = StemFinder.findByUuid(grouperSession,
					curNode);
			group = parent.addChildGroup(extension,displayExtension );
			doTypes(group,request);
			groupForm.set("groupId", group.getUuid());
			assignedPrivs = GrouperHelper.getDefaultAccessPrivsForGrouperAPI();
		}
		String [] privileges = request.getParameterValues("privileges");
		
		Map selectedPrivs = new HashMap();
		if(privileges==null) privileges = new String[]{};
		
			
		for(int i=0;i<privileges.length;i++) {
			selectedPrivs.put(privileges[i],Boolean.TRUE);
			//Check if priv was automatically assigned...
			if(!Boolean.TRUE.equals(assignedPrivs.get(privileges[i]))){
				group.grantPriv(grouperAll,Privilege.getInstance(privileges[i]));
			}
		}
		Map.Entry entry = null;
		Iterator it = assignedPrivs.entrySet().iterator();
		while(it.hasNext()) {
			entry = (Map.Entry)it.next();
			if(!Boolean.TRUE.equals(selectedPrivs.get(entry.getKey()))) {
				group.revokePriv(grouperAll,Privilege.getInstance((String)entry.getKey()));
			}
		}
		
		
		
		
		//TODO: are both these necessary?
		request.setAttribute("groupId", group.getUuid());
		groupForm.set("groupId",group.getUuid());
		try {
			if("true".equals(getMediaResources(request).getString("put.in.session.updated.groups"))) {
				addSavedSubject(session,group.toSubject());
			}
		}catch(Exception e){}
		String val  =(String) groupForm.get("groupDescription");
		if("".equals(val)) val=null;

		if(val!=null)group.setDescription(val);	

		request.setAttribute("message", new Message(
				"groups.message.group-saved", (String) groupForm
						.get("groupDisplayName")));
		
		//TODO: more sophistication in determining where to go?
		String submit = request.getParameter("submit.save");

		if (submit != null) {
			return mapping.findForward(FORWARD_GroupSummary);
		}
		submit=request.getParameter("submit.saveAndAddComposite");
		if (submit != null) {
			return mapping.findForward(FORWARD_AddComposite);
		}
		session.setAttribute("findForNode", group.getUuid());
		return mapping.findForward(FORWARD_FindMembers);

	}
	
	private void doTypes(Group group,HttpServletRequest request) throws Exception {
		Set curGroupTypes = group.getRemovableTypes();
		String[] selectedGroupTypes = request.getParameterValues("groupTypes");
		Set selected = new HashSet();
		GroupType type;
		if(selectedGroupTypes!=null) {
			for(int i=0;i<selectedGroupTypes.length;i++) {
				type = GroupTypeFinder.find(selectedGroupTypes[i]);
				selected.add(type);
				if(!curGroupTypes.contains(type)) group.addType(type);
			}
		}
		Iterator it = curGroupTypes.iterator();
		while(it.hasNext()) {
			type=(GroupType)it.next();
			if(!selected.contains(type) && !"base".equals(type.getName())) group.deleteType(type);
		}
	}

}