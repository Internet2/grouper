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

package edu.internet2.middleware.grouper.ui.actions;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import edu.internet2.middleware.grouper.ui.Message;

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
 * @version $Id: DoAssignNewMembersAction.java,v 1.2 2005-11-04 11:41:42 isgwb Exp $
 */
public class DoAssignNewMembersAction extends GrouperCapableAction {

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
		//Cast the form to make it useful
		DynaActionForm assignMembersForm = (DynaActionForm) form;
		String groupId = (String) assignMembersForm.get("groupId");
		String stemId = (String) assignMembersForm.get("stemId");
		String folderId = groupId;
		boolean forStem = "true".equals(assignMembersForm.get("stems"));
		if (forStem)
			folderId = stemId;
		
		//If we haven't got a node yet assume it was set a while back
		//TODO: check this
		if (folderId == null || folderId.length() == 0)
			folderId = (String) session.getAttribute("findForNode");
		
		//subjectIds
		String[] members = (String[])assignMembersForm.get("members");
		Subject[] membersAsSubjects = new Subject[members.length];
		String subjectTypeId;
		Subject subject;
		String subjectId;
		GrouperGroup subjectGroup;
		for (int i = 0; i < members.length; i++) {
			subjectId = members[i];
			subjectTypeId = request.getParameter("subjectType:" + members[i]);
			if ("group".equals(subjectTypeId)) {
				subjectGroup = GrouperHelper.groupLoadById(grouperSession,
						subjectId);
				
				//TODO: is this needed?
				subjectId = subjectGroup.id();
			}
			//Need actual subject instance to assign privileges
			subject = GrouperHelper.getSubjectFromIdAndType(subjectId,
					subjectTypeId);
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
		//TODO: could possibly fail on some - check wit blair as to
		//how and wheter to make one transaction
		GrouperHelper.assignPrivileges(grouperSession, folderId,
				membersAsSubjects, privileges, "true".equals(request
						.getParameter("stems")));
		message = new Message("priv.message.assigned");
		request.setAttribute("message", message);
		//Make sure we don't switch inadvertantly to group context
		if(doRedirectToCaller(assignMembersForm))return redirectToCaller(assignMembersForm);
		if (forStem)
			return mapping.findForward(FORWARD_StemPrivilegees);
		if(!isEmpty(session.getAttribute("findForPriv"))) return mapping.findForward((FORWARD_GroupPrivilegees));
		return mapping.findForward(FORWARD_GroupMembers);

	}

}