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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action which retrieves and makes available a GrouperGroup 
 * object. 
 * 
 
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      show summary for. If empty SET</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupName</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The group extension</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextGroup</font></p>
      <p>&nbsp;</p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If set then assume this is a 
      diversion and don`t reset the browsePath to this group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">changeMode</font></p>
      <p>&nbsp;</p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If true then change browse mode 
      to All</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if not set as request parameter</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicate if we are in flatMode 
      - if so don't show hierarchy</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of privileges the Subject 
      identified by request parameters has for this group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">AllGroupPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of all Access privileges</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of available list fields 
      for group - to enable user to change view</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFieldsSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Number of list fields available</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to give context to UI</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFactor</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Boolean which indicates if this 
      group is a factor in a composite - used to provide a link, where relevant, 
      to those groups</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">allowedFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map keyed on fields which the 
      authenticated Subject can read</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">saveParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Link parameters for saving Group 
      to list of saved Groups</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">factorParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Link parameters to supply to 
      populateGroupAsFactor</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">IsComposite</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Boolean indicating if Group 
      has a CompositeMember</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">userCanEditACustomAttribute</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that the UI should 
      show the Edit Attributes button</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupPrivilegeResolver</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Instance of UIGroupPrivilegeResolver</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.show-summary</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">group</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map wrapping current group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to set request attribute 
      <em>isFlat</em> </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseNode&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to groupId </font></td>
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
 * @version $Id: PopulateGroupSummaryAction.java,v 1.16 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PopulateGroupSummaryAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateGroupSummaryAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupSummary = "GroupSummary";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		if("true".equals(request.getParameter("changeMode"))) PopulateAllGroupsAction.initMode(session);
		session.setAttribute("subtitle", "groups.action.show-summary");
		
		DynaActionForm groupForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupForm);
		//Determine group we are showing summary for
		String groupId = (String) groupForm.get("groupId");
		if (groupId == null || "".equals(groupId))
			groupId = (String) request.getAttribute("groupId");
		
		if(isEmpty(groupId)) {
			String msg = neh.missingParameters(groupId,"groupId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.group-summary.missing-id");
		}
		Group group = null;
		try{
			group=GroupFinder.findByUuid(grouperSession,
				groupId, true);
		}catch(GroupNotFoundException e) {
			LOG.error("Error retirving group with id=" + groupId,e);
			throw new UnrecoverableErrorException("error.group-summary.bad-id",groupId);
		}
		if(isEmpty(groupForm.get("groupId"))) {
			augmentCallerPageRequest(request,"groupId",group.getUuid());
		}
		Map groupMap = GrouperHelper.group2Map(grouperSession, group);
		groupMap.put("groupId", groupId);
		groupMap.put("groupName", group.getName().substring(
				group.getName().lastIndexOf(HIER_DELIM) + 1));

		//TODO: check this
		session.setAttribute("group", groupMap);
		
		UIGroupPrivilegeResolver resolver = 
			UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
			    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
					                                    group, grouperSession.getSubject());
		request.setAttribute("groupPrivResolver", resolver.asMap());
		boolean userCanEditACustomAttribute = resolver.canManageAnyCustomField();

		//Only reset if we are not here on a diversion
		if(isEmpty(groupForm.get("contextGroup"))) setBrowseNode(groupId,session);
		Set compOwners = CompositeFinder.findAsFactor(group);
		if(!compOwners.isEmpty()) {
			request.setAttribute("isFactor",Boolean.TRUE);
		}
		//Determine privileges for group so user only gets the buttons they
		// should get
		Map privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByGroup(grouperSession,group));
		request.setAttribute("groupPrivs", privs);

		Collection allGroupPrivs = GrouperHelper.getGroupPrivsWithLabels(GrouperUiFilter.retrieveSessionNavResourceBundle());
		request.setAttribute("allGroupPrivs", allGroupPrivs);
		List listFields = GrouperHelper.getReadableListFieldsForGroup(grouperSession,group);
		request.setAttribute("listFields",listFields);
		request.setAttribute("listFieldsSize",new Integer(listFields.size()));
		Map allowedFields = GrouperHelper.getFieldsForGroup(grouperSession,group,"read");
		request.setAttribute("allowedFields",allowedFields);

		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		Map saveParams = new HashMap();
		saveParams.put("subjectId",group.getUuid());
		saveParams.put("subjectType","group");
		saveParams.put("sourceId",group.toSubject().getSource().getId());
		saveParams.put("callerPageId",request.getAttribute("thisPageId"));
		saveParams.put("groupId",group.getUuid());
		request.setAttribute("saveParams",saveParams);
		request.setAttribute("factorParams",saveParams);
		request.setAttribute("userCanEditACustomAttribute",new Boolean(userCanEditACustomAttribute));
		if(group.hasComposite()) request.setAttribute("isCompositeGroup",Boolean.TRUE);
		
		boolean isFlat = processFlatForMode(getBrowseMode(session), request, session);
		if(isFlat) {
			request.setAttribute("isFlat",Boolean.TRUE);
		}else{
			request.setAttribute("isFlat",Boolean.FALSE);
		}

		return mapping.findForward(FORWARD_GroupSummary);
	}
}
