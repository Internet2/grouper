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
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action which, assuming user has optin privilege, 
 * causes user to join a group.  
 * 
 <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">groupId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the group to join</font></td>
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
      from groups.message.join-success key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
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
 * @version $Id: JoinGroupAction.java,v 1.5 2008-07-21 04:43:47 mchyzer Exp $
 */
public class JoinGroupAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(JoinGroupAction.class);
	//------------------------------------------------------------ Local
	// Forwards

	static final private String FORWARD_JoinGroups = "JoinGroups";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {

		NavExceptionHelper neh=getExceptionHelper(session);
		String groupId = request.getParameter("groupId");
		if(isEmpty(groupId)) {
			String msg = neh.missingParameters(groupId,"groupId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.join-group.missing-parameter");
		}
		Group group = null;
		try{
			group=GrouperHelper.groupLoadById(grouperSession,
				groupId);
		}catch(GroupNotFoundException e) {
			LOG.error("Could not find group with id=" + groupId,e);
			throw new UnrecoverableErrorException("error.join-group.bad-id",groupId);
		}
		
		if(!group.hasImmediateMember(grouperSession.getSubject())) {
			try{
				group.addMember(grouperSession.getSubject());
			}catch(Exception e) {
				LOG.error(e);
				throw new UnrecoverableErrorException("error.join-group.add-error",e);
			}
		}
			
		request.setAttribute("message", new Message(
				"groups.message.join-success", group.getDisplayExtension()));
		
		return mapping.findForward(FORWARD_JoinGroups);

	}

}
