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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
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
 * @version $Id: SaveGroupAction.java,v 1.22 2009-08-12 04:52:14 mchyzer Exp $
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
			Stem root = StemFinder.findByName(grouperSession, defaultStem, true);
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
		
		String extension = (String) groupForm.get("groupName");
		String displayExtension = (String) groupForm.get("groupDisplayName");
		String alternateName = (String) groupForm.get("groupAlternateName");

		if(isEmpty(displayExtension))displayExtension=extension;
		//TODO: should be transactional - so add map or List of attributes
		Map assignedPrivs=null;
		Subject grouperAll = SubjectFinder.findById("GrouperAll", true);
		if (groupExists) {
			group = GroupFinder.findByUuid(grouperSession, curNode, true);
			Set<GroupType> removableTypes = group.getRemovableTypes();
			group.setDisplayExtension(displayExtension);
			group.setExtension(extension);

			String oldAlternateName = null;
			Iterator<String> alternateNames = group.getAlternateNames().iterator();
			if (alternateNames.hasNext()) {
				oldAlternateName = alternateNames.next();
			}

			if (isEmpty(alternateName) && oldAlternateName != null) {
				group.deleteAlternateName(oldAlternateName);
			} else if (!isEmpty(alternateName) && (oldAlternateName == null || !oldAlternateName.equals(alternateName))) {
				try {
					group.addAlternateName(alternateName);
				} catch (GroupModifyException e) {
				  setTransactionRollback(true);
					request.setAttribute("message", new Message(
						"groups.message.error.alternate-name-problem", true));
					return mapping.findForward(FORWARD_EditAgain);
				}
			}

                        try {
			group.store();
                        } catch (GroupModifyAlreadyExistsException e) {
                          setTransactionRollback(true);
                          request.setAttribute("message", new Message("groups.message.error.update-problem-already-exists", true));
                          return mapping.findForward(FORWARD_EditAgain);
                        }

			//do this after the store, in case there were types added in the hook...
			doTypes(group,request, removableTypes);

			Map selectedPrivs = GrouperHelper.getImmediateHas(grouperSession,GroupOrStem.findByGroup(grouperSession,group),MemberFinder.findBySubject(grouperSession,grouperAll, true));
			assignedPrivs=new HashMap();
			Map.Entry entry;
			String key;
			Iterator it = selectedPrivs.entrySet().iterator();
			while(it.hasNext()) {
				entry = (Map.Entry)it.next();
				key = (String)entry.getKey();
				assignedPrivs.put(key,entry.getValue());
			}
		} else {
			GroupOrStem curGos = GroupOrStem.findByID(grouperSession,curNode);
			Stem parent=null;
			if(curGos.isStem()) {
				parent = curGos.getStem();
			}else{
				parent = curGos.getGroup().getParentStem();
			}
			try{
				group = parent.addChildGroup(extension,displayExtension );

			}catch(HookVeto hookVeto) {
			  setTransactionRollback(true);
			  //this action was vetoed, put explanation on screen, and go back
			  Message.addVetoMessageToScreen(request, hookVeto);
		    return mapping.findForward(FORWARD_CreateAgain);
			  
			}catch(GroupAddException e) {
			  setTransactionRollback(true);
				String name = parent.getName() + GrouperHelper.HIER_DELIM + extension;
				request.setAttribute("message", new Message(
							"groups.message.error.add-problem",new String[] {e.getMessage()}, true));
				return mapping.findForward(FORWARD_CreateAgain);
			}
			doTypes(group,request, new HashSet<GroupType>());
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
		//if(0==0) throw new RuntimeException("Forced error for transaction testing");
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
			if("true".equals(GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("put.in.session.updated.groups"))) {
				addSavedSubject(session,group.toSubject());
			}
		}catch(Exception e){}
		String val  =(String) groupForm.get("groupDescription");
		if("".equals(val)) val=null;

		if(val!=null) {
		  group.setDescription(val);
		  group.store();
		}

		request.setAttribute("message", new Message(
				"groups.message.group-saved", group.getDisplayExtension()));
		
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
	
	private void doTypes(Group group,HttpServletRequest request, Set<GroupType> curGroupTypes) throws Exception {
		
		String[] selectedGroupTypes = request.getParameterValues("groupTypes");
		Set selected = new HashSet();
		GroupType type;
		if(selectedGroupTypes!=null) {
			for(int i=0;i<selectedGroupTypes.length;i++) {
				type = GroupTypeFinder.find(selectedGroupTypes[i], true);
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
