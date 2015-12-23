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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.MissingGroupOrStemException;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;


/**
 * Top level Strut's Action which maintains the current node (stem or group) 
 * for the different browseModes (My,Create,Manage,Join,All). 
 * 
 * <p />
  <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">currentNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">New location in hierarchy</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">advancedSearch</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user has requested 
      the advanced search; currentNode is not expected</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Allows callerPageId to 
      be added to links/forms so this page can be returned to</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Group or stem for which 'new' 
      members/privilegees are being sought</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseNodeId&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to currentNode (unless advancedSearch)</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">find.browse - key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">MyGroups, CreateGroups, ManageGroups, 
      JoinGroups, AllGroups</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates the current browsing 
      mode which determines which JSP is displayed</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">FindNewMembers</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that purpose of browsing 
      is to find Subjects which can be added as members of a group, or can have 
      Access/Naming privileges granted to them.</font></td>
  </tr>
</table>
 * @author Gary Brown.
 * @version $Id: BrowseStemsAction.java,v 1.11 2008-04-15 08:28:23 isgwb Exp $
 */
public class BrowseStemsAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(BrowseStemsAction.class);

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_JoinGroups = "JoinGroups";

	static final private String FORWARD_ManageGroups = "ManageGroups";

	static final private String FORWARD_CreateGroups = "CreateGroups";

	static final private String FORWARD_MyGroups = "MyGroups";

	static final private String FORWARD_AllGroups = "AllGroups";

	static final private String FORWARD_FindNewMembers = "FindNewMembers";

	static final private String FORWARD_FindNewMembersForStems = "FindNewMembersForStems";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		//Default subtitle
		session.setAttribute("subtitle", "find.browse");
		
		DynaActionForm browseForm = (DynaActionForm)form;
		String currentNodeId = (String)browseForm.get("currentNode");
		if(isEmpty(currentNodeId)) currentNodeId=getBrowseNode(session);
		
		if(!isEmpty(currentNodeId)) {
			try {
				GroupOrStem gos = GroupOrStem.findByID(grouperSession,currentNodeId);
				if(gos.isStem()) browseForm.set("stemId",currentNodeId);
			}catch(MissingGroupOrStemException e) {
				Throwable t=NavExceptionHelper.fillInStacktrace(e);
				LOG.error("Error retrieving id=" + currentNodeId + ": " + NavExceptionHelper.toLog(t));
				throw new UnrecoverableErrorException("error.browse-stems.bad-node",t,currentNodeId);
			}
		}
		if(isEmpty(request.getParameter("advancedSearch")))
			setBrowseNode(currentNodeId,session);
		//Multiple Struts actions map to this class. Param 
		//indicates the appropriate view
		String param = mapping.getParameter();
		String mode = param.replaceAll("Groups", "").replaceAll("My", "");
		if(mode.equals("") || mode.equals("All") || mode.equals("Create")
				|| mode.equals("Manage")|| mode.equals("Join"))	setBrowseMode(mode, session);
		
		//Check if browsing to find members
		if ("FindNewMembers".equals(param)) {
			saveAsCallerPage(request,browseForm,"findForNode");
			//The node we are finding members for
			String findForNode = (String) session.getAttribute(
					"findForNode");
			try {
				GroupOrStem groupOrStem = GroupOrStem.findByID(grouperSession,findForNode);
				if(groupOrStem.isStem()) return mapping.findForward(param + "ForStems");
			}catch(MissingGroupOrStemException e) {
				Throwable t=NavExceptionHelper.fillInStacktrace(e);
				LOG.error("Could not retrieve findForNode=" + findForNode + ": " + NavExceptionHelper.toLog(t));
				throw new UnrecoverableErrorException("error.browse-stems.bad-find-node",t,findForNode);
			}
		}
		saveAsCallerPage(request,browseForm,"");
		return mapping.findForward(param);
	}
}
