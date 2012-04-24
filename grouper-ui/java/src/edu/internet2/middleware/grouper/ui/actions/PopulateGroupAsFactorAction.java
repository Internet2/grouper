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

import java.util.ArrayList;

import java.util.Iterator;
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

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

/**
 * Top level Strut's action which retrieves and makes available Composites
 * where this Group is a Factor.  
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
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      show as a factor</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for current group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">composites</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of Composites as Maps where 
      focus group is a Factor</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.as-factor</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
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
 * @version $Id: PopulateGroupAsFactorAction.java,v 1.6 2009-03-15 06:37:51 mchyzer Exp $
 */
public class PopulateGroupAsFactorAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateGroupAsFactorAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupAsFactor = "GroupAsFactor";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		session.setAttribute("subtitle", "groups.action.as-factor");
		
		DynaActionForm groupForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupForm);
		//Determine group we are showing summary for
		String groupId = (String) groupForm.get("groupId");
		if (groupId == null || "".equals(groupId))
			groupId = (String) request.getAttribute("groupId");
		if(isEmpty(groupId)) {
			String msg = neh.missingParameters(groupId,"groupId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.group-as-factor.missing-group-id");
		}
		Group group = null;
		try{
			group=GroupFinder.findByUuid(grouperSession,groupId, true);
		}catch(GroupNotFoundException e) {
			LOG.error("No group with id=" + groupId,e);
			throw new UnrecoverableErrorException("error.group-as-factor.bad-id",groupId);
		}
		Set compOwners = CompositeFinder.findAsFactor(group);
		Map compMap = null;
		List composites = new ArrayList();
		Composite comp;
		Iterator it = compOwners.iterator();
		while(it.hasNext()) {
			comp = (Composite)it.next();
			composites.add(GrouperHelper.getCompositeMap(grouperSession,comp));
		}
		/*Map groupMap = GrouperHelper.group2Map(grouperSession, group);
		groupMap.put("groupId", groupId);
		groupMap.put("groupName", group.getName().substring(
				group.getName().lastIndexOf(HIER_DELIM) + 1));

		//TODO: check this
		session.setAttribute("group", groupMap);
		*/
				


		request.setAttribute("composites",composites);
		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		/*Map saveParams = new HashMap();
		saveParams.put("subjectId",group.getUuid());
		saveParams.put("subjectType","group");
		saveParams.put("callerPageId",request.getAttribute("thisPageId"));
		request.setAttribute("saveParams",saveParams);*/


		return mapping.findForward(FORWARD_GroupAsFactor);
	}
}
