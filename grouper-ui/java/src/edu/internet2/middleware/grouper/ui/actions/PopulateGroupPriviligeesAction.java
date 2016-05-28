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

import java.util.HashMap;
import java.util.List;
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
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;

/**
 * Top level Strut's action which retrieves and makes available subjects with 
 * the requested privilege. 
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
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      see members for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">asMemberOf</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if groupId and findForNode are 
      empty, asMemberOf identifies group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privilege</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Access privilege for which we 
      are listing privilegees</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CollectionPager instance</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pagerParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of params set on pager</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">groupMembership</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map used by Strut's &lt;html:link&gt; 
      tags when generating parameters for &lt;a&gt; tags</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to give context to UI</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.show-privilegees</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if groupId not set</font></td>
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
    <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">memberLinkMode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to privilege so that can 
      be distinguished from list of group members</font></td>
  </tr>
  </table>

  

  * @author Gary Brown.
 * @version $Id: PopulateGroupPriviligeesAction.java,v 1.12 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PopulateGroupPriviligeesAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupPriviligees = "GroupPriviligees";

	static final private String FORWARD_StemPriviligees = "StemPriviligees";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm groupForm = (DynaActionForm) form;
		String groupId = (String) groupForm.get("groupId");
		saveAsCallerPage(request,groupForm);
		if(isEmpty(groupId))groupId = (String) groupForm.get("asMemberOf");
		String privilege = (String)groupForm.get("privilege");
		if(isEmpty(privilege)) privilege = (String)session.getAttribute("findForPriv");
		
		session.setAttribute("subtitle", "groups.action.show-privilegees");
		request.setAttribute("subtitleArgs", new Object[] { privilege });
		
		if (isEmpty(groupId))
			groupId = (String) session.getAttribute("findForNode");
		Group group = null;
				
		//Determine who has privilege selected by user
		group = GroupFinder.findByUuid(grouperSession, groupId, true);
		Set subjects = GrouperHelper.getSubjectsWithPriv(group,privilege);
		List subjectPrivilegeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(grouperSession,sort(subjects,request,"privilegees", -1, null),group,privilege);
		//Set up CollectionPager for the view
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		CollectionPager pager = new CollectionPager(null, subjectPrivilegeMaps,
				subjectPrivilegeMaps.size(), null, start, null, pageSize);
		pager.setParam("groupId", groupId);
		pager.setParam("privilege", privilege);
		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		request.setAttribute("linkParams", pager.getParams().clone());
		

		Map membership = new HashMap();
		
		membership.put("groupId", groupId);
		membership.put("privilege", privilege);
		
		UIGroupPrivilegeResolver resolver = 
				UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
				    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
						                                    group, grouperSession.getSubject());
		
		//Set up view info
		Map privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByGroup(grouperSession,group));
		request.setAttribute("groupPrivs", privs);

		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		request.setAttribute("groupMembership", membership);
		request.setAttribute("memberLinkMode", "privilege");
		
		request.setAttribute("allGroupPrivs", GrouperHelper
				.getGroupPrivsWithLabels(GrouperUiFilter.retrieveSessionNavResourceBundle()));
		
		request.setAttribute("groupPrivResolver", resolver.asMap());
		
		return mapping.findForward(FORWARD_GroupPriviligees);
	}

}
