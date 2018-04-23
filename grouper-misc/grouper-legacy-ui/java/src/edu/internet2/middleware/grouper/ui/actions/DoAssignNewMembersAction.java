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

package edu.internet2.middleware.grouper.ui.actions;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action which assigns membership / privileges for a
 * group / stem.  
 * <p />
 <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">stemId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the stem (if any) 
      to which Naming privileges will be assigned</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">groupId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the group (if any) 
      to which membership / Access privileges will be assigned</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">stems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates that 
      a stemId should be expected</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">members</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Subject Ids</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectType:&lt;subjectId&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Subject type Subject ids derived 
      from 'members'</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">privileges</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of privilege names to 
      be assigned</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">callerPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">pageId of saved page which should 
      be returned to</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies list field we are 
      assigning 'members' to</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Message instance: text derived 
      from priv.message.error.no-priv / priv.message.assigned key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If stemId and groupId are not 
      set default to this</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present display list of privilegees, 
      otherwise display list of members</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">findForListField</font></p>
      <p>&nbsp;</p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If listField not a request parameter 
      use this</font></td>
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
 * 
 * @author Gary Brown.
 * @version $Id: DoAssignNewMembersAction.java,v 1.11 2009-03-15 06:37:51 mchyzer Exp $
 */
public class DoAssignNewMembersAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(DoAssignNewMembersAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";
	static final private String FORWARD_GroupPrivilegees = "GroupPrivilegees";

	static final private String FORWARD_StemPrivilegees = "StemPrivilegees";

	static final private String FORWARD_FindNewMembers = "FindNewMembers";

	static final private String FORWARD_GroupSummary = "GroupSummary";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		NavExceptionHelper neh=getExceptionHelper(session);
		//Cast the form to make it useful
		DynaActionForm assignMembersForm = (DynaActionForm) form;
		String groupId = (String) assignMembersForm.get("groupId");
		String stemId = (String) assignMembersForm.get("stemId");
		String folderId = groupId;
		boolean forStem = "true".equals(assignMembersForm.get("stems"));
		
		String listField = (String) assignMembersForm.get("listField");
		String membershipField = "members";
		if(isEmpty(listField)) {
			listField = (String) session.getAttribute("findForListField");
		}
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = null;
		try {
			mField=FieldFinder.find(membershipField, true);
		}catch(SchemaException e) {
			LOG.error("Error retrieving " + membershipField,e);
			throw new UnrecoverableErrorException("error.assign-new-members.bad-field",e,membershipField);
		}
		if (forStem)
			folderId = stemId;
		
		//If we haven't got a node yet assume it was set a while back
		//TODO: check this
		if (folderId == null || folderId.length() == 0)
			folderId = (String) session.getAttribute("findForNode");
		
		if(isEmpty(folderId)) {
			String msg = "No stem or group or findForNode available";
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.assign-new-members.missing-id");
		}
		//subjectIds
		String[] members = (String[])assignMembersForm.get("members");
		Subject[] membersAsSubjects = new Subject[members.length];
		String subjectTypeId;
		Subject subject;
		String subjectId;
		String sourceId;
		Group subjectGroup;
		for (int i = 0; i < members.length; i++) {
			subjectId = members[i];
			subjectTypeId = request.getParameter("subjectType:" + members[i]);
			sourceId = request.getParameter("sourceId:" + members[i]);
			/*if ("group".equals(subjectTypeId)) {
				subjectGroup = GrouperHelper.groupLoadById(grouperSession,
						subjectId);
				
				//TODO: is this needed?
				subjectId = subjectGroup.getUuid();
			}*/
			//Need actual subject instance to assign privileges
			String msg = neh.missingParameters(subjectId,"subjectId",subjectTypeId,"subjectTypeId",sourceId,"sourceId");
			if(msg!=null) {
				LOG.error(msg);
				throw new UnrecoverableErrorException("error.assign-members.missing-subject-parameter");
			}
			try {
			subject = SubjectFinder.findById(subjectId,
					subjectTypeId,sourceId, true);
			}catch(Exception e) {
				LOG.error(e);
				throw new UnrecoverableErrorException("error.assign-members.retrieve-subject",e,subjectId);
			}
			
			membersAsSubjects[i] = subject;
		}
		//Get the privileges checked in the UI
		String[] privileges = (String[]) assignMembersForm.get("privileges");
		request.setAttribute("privileges", privileges);
		Message message = null;
		
		//Deal with situation when no privileges were set
		if (privileges == null || privileges.length == 0) {
			message = new Message("priv.message.error.no-priv", true);
			request.setAttribute("message", message);
			return mapping.findForward(FORWARD_GroupMembers);
		}
		
		try {
		GrouperHelper.assignPrivileges(grouperSession, folderId,
				membersAsSubjects, privileges, "true".equals(request
						.getParameter("stems")),mField);
			message = new Message("priv.message.assigned");
		}catch(GrantPrivilegeException e) {
			LOG.error("Could not assign all privileges", e);
			throw new UnrecoverableErrorException(e);
		}catch(Exception e) {
			LOG.error("Could not assign all privileges", e);
			throw new UnrecoverableErrorException("error.assign-members.assign-privs",e);
		}
		request.setAttribute("message", message);
		//Make sure we don't switch inadvertantly to group context
		if(doRedirectToCaller(assignMembersForm))return redirectToCaller(assignMembersForm);
		if (forStem)
			return mapping.findForward(FORWARD_StemPrivilegees);
		if(!isEmpty(session.getAttribute("findForPriv"))) return mapping.findForward((FORWARD_GroupPrivilegees));
		return mapping.findForward(FORWARD_GroupMembers);

	}

}
