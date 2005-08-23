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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.GrouperAttribute;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;
import edu.internet2.middleware.grouper.ui.Message;

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
    <td><p><font face="Arial, Helvetica, sans-serif">groupName,groupType,groupDisplayName,<br>
        groupDescription</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Values retrieved from DynaActionForm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user wants to save 
      group but not assign membership or privileges</font></td>
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
 * @version $Id: SaveGroupAction.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
 */
public class SaveGroupAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupSummary = "GroupSummary";

	static final private String FORWARD_EditGroupAttributes = "EditGroupAttributes";

	static final private String FORWARD_EditAgain = "EditAgain";

	static final private String FORWARD_CreateAgain = "CreateAgain";

	static final private String FORWARD_GroupMembers = "GroupMembers";

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
			GrouperStem root = (GrouperStem)GrouperStem.loadByName(grouperSession, defaultStem);
			curNode = root.id();
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
		GrouperGroup group = null;
		String id = curNode;
		
		//TODO: should be transactional - so add map or List of attributes
		if (groupExists) {
			group = (GrouperGroup)GrouperGroup.loadByID(grouperSession, curNode);
		} else {
			GrouperStem parent = (GrouperStem)GrouperStem.loadByID(grouperSession,
					curNode);
			group = GrouperGroup.create(grouperSession, parent.name(),
					(String) groupForm.get("groupName"), (String) groupForm
							.get("groupType"));
			groupForm.set("groupId", group.id());
		}
		//TODO: are both these necessary?
		request.setAttribute("groupId", group.id());
		groupForm.set("groupId",group.id());
		group.attribute("displayExtension", (String) groupForm
				.get("groupDisplayName"));
		String val  =(String) groupForm.get("groupDescription");
		if("".equals(val)) val=null;
		//try {
		GrouperAttribute ga = group.attribute("description");
		if(!(ga==null && val==null))	group.attribute("description",val);
			
		//}catch(Exception e) {
			
		//}
		

		request.setAttribute("message", new Message(
				"groups.message.group-saved", (String) groupForm
						.get("groupDisplayName")));
		
		//TODO: more sophistication in determining where to go?
		String submit = request.getParameter("submit.save");

		if (submit != null) {
			return mapping.findForward(FORWARD_GroupSummary);
		}
		session.setAttribute("findForNode", group.id());
		return mapping.findForward(FORWARD_FindMembers);

	}

}