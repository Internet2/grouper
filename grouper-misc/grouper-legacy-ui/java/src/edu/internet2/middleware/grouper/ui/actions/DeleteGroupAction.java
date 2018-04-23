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


import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action which removes a group from the repository.  
 * <p />
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
    <td><font face="Arial, Helvetica, sans-serif">Identifies the group to be deleted</font></td>
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
      from groups.message.group-deleted / groups.message.group-deleted-fail.factor 
      / groups.message.group-fail-delete key in nav ResourceBundle</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">isFactor</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Boolean indicates that this 
      group is a factor in a composite - and so cannot be deleted</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseNodeId&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If the delete was successful 
      set to the id for the stem of the deleted group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseMode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates which browseStem URL 
      to forward to</font></td>
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
 * @version $Id: DeleteGroupAction.java,v 1.11 2008-07-21 04:43:47 mchyzer Exp $
 */
public class DeleteGroupAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(DeleteGroupAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	/** Return to manage screen */
	static final private String FORWARD_ManageGroups = "ManageGroups";

	static final private String FORWARD_CreateGroups = "CreateGroups";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		//Get the id of the group to remove
		NavExceptionHelper neh=getExceptionHelper(session);
		String groupId = request.getParameter("groupId");
		if(isEmpty(groupId)) {
			String msg = neh.missingParameters(groupId,"groupId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.delete-group.missing-parameter");
		}
		
		//Instantiate the group
		Group group = null;
		try {
			group=GrouperHelper.groupLoadById(grouperSession,
				groupId);
		}catch(GroupNotFoundException e) {
			LOG.error(e);
			throw new UnrecoverableErrorException("error.delete-group.bad-id",groupId);
		}
		
		Message message=null;
		String displayExtn = group.getDisplayExtension();
		Set compOwners = CompositeFinder.findAsFactor(group);
		boolean success = false;
		if(!compOwners.isEmpty()) {
			request.setAttribute("isFactor",Boolean.TRUE);
			 message= new Message("groups.message.group-deleted-fail.factor",
					displayExtn,true);
		}else {
		
			//Set up message to display to user
			message = new Message("groups.message.group-deleted",
					displayExtn);
			
			//Obtain the stem for the group we are removing
			
			Stem parent = group.getParentStem();
			//Try and remove the group
			
			try{
				if(group.hasComposite()) group.deleteCompositeMember();
				group.delete();
				setBrowseNode(parent.getUuid(),session);
				success=true;
			}catch(GroupDeleteException e){
				LOG.error(e);
				throw new UnrecoverableErrorException("error.delete-group.unknown-error");
			}
		}
		request.setAttribute("message", message);

		 
		
		ActionForward forward = null;
		if(!success) {
			forward = mapping.findForward("GroupSummary");
		}else {
			//Do redirect else advanced search link breaks
			 forward=new ActionForward("/browseStems" + getBrowseMode(session) + ".do",true);
		}
		return forward;
	}
}
