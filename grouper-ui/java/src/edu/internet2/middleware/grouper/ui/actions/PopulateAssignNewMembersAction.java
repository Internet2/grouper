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
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.MissingGroupOrStemException;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which may be called directly or indirectly 
 * (following a search). It displays a list of potential members / privilegees 
 * for selection. Potential new members may come from a search, 
 * or may have been submitted from a form whilst browsing existing group memberships. 
 * 
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">alreadyChecked</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates that 
      potential members / privilegees be pre-checked</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">stems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates that 
      Naming privileges are to be assigned to a stem</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">members</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Subject ids submitted 
      from a previous page</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectType:&lt;subjectId&gt;</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">One for each Subject in members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used for paging of lists</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">stemId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present, same indication 
      as stems</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">alreadyChecked</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Based on request parameter - 
      becomes available to JSTL</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectResults</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates we should list results 
      of a search</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectResultsSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates number of search results</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchedPeople</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates people 
      were searched</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchedGroups</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true </em>indicates groups 
      were searched</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">forStems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates whether findForNodeis 
      a stem or group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectResultsCount</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">How many results in total</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">An instance of CollectionPager 
      which is used as the source of data when displaying lists in the UI</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">targetPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map indicating what privileges 
      user has over current group or stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Arguments for subtitle</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">thisPageId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Allows callerPageId to be added 
      to links/forms so this page can be returned to. Only sets new one if there 
      is not an existing one i.e. we got here from another action</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current group 
      or stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Group or stem id indicating 
      entity to which privileges will be assigned</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td height="28"><font face="Arial, Helvetica, sans-serif">searchObj</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map which 'remembers' search 
      parameters. Can be used to populate search screen next time</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td height="28"><font face="Arial, Helvetica, sans-serif">default.pagesize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used in CollectionPager constructor</font></td>
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
 * @version $Id: PopulateAssignNewMembersAction.java,v 1.13 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PopulateAssignNewMembersAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(PopulateAssignNewMembersAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_AssignNewMembers = "AssignNewMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm searchForm = (DynaActionForm) form;
		saveAsCallerPage(request,(DynaActionForm)form);
		//Set to true if selected from browse list rather than search
		String alreadyChecked = request.getParameter("alreadyChecked");
		boolean checked = false;
		Map searchObj = new HashMap();
		if ("true".equals(alreadyChecked)) {
			request.setAttribute("alreadyChecked", Boolean.TRUE);

			searchObj.put("trueSearch", Boolean.FALSE);
			searchObj.put("start", new Integer(0));
			checked = true;

		} else {
			request.setAttribute("alreadyChecked", Boolean.FALSE);
		}
		//Set if from search
		List subjectRes = (List) request.getAttribute("subjectResults");
		Integer resultSize = (Integer) request
				.getAttribute("subjectResultsSize");
		int total = 0;
		if (resultSize != null)
			total = resultSize.intValue();

		Boolean searchedPeople = (Boolean) request
				.getAttribute("searchedPeople");
		Boolean searchedGroups = (Boolean) request
				.getAttribute("searchedGroups");
		String stems = request.getParameter("stems");
		
		GroupOrStem groupOrStem = null;
		Group group = null;
		Stem stem = null;
		String findForNode = (String) request.getSession().getAttribute(
				"findForNode");
		if(isEmpty(findForNode)) {
			String msg = "No stem or group or findForNode available";
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.populate-assign-new-members.missing-id");
		}
		try{
			groupOrStem= GroupOrStem.findByID(grouperSession,findForNode);
		}catch(MissingGroupOrStemException e) {
			Throwable t=NavExceptionHelper.fillInStacktrace(e);
			LOG.error(NavExceptionHelper.toLog(t));
			throw new UnrecoverableErrorException("error.populate-assign-new-members.bad-id",t,findForNode);
		}
		if(groupOrStem.isStem()) {
			request.setAttribute("forStems", Boolean.TRUE);
		}else{
			request.setAttribute("forStems", Boolean.FALSE);
			group=groupOrStem.getGroup();
			UIGroupPrivilegeResolver resolver = 
				UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
				    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
					                                    	group, grouperSession.getSubject());
			request.setAttribute("groupPrivResolver", resolver.asMap());
		}
		

		Map findMap = GrouperHelper.group2Map(grouperSession, groupOrStem);
		boolean fromSearch = true;
		if (searchedPeople == null && searchedGroups == null) {
			//Form submission so must process request parameters
			fromSearch = false;
			if (isEmpty(findMap.get("isGroup"))) {
				request.setAttribute("forStems", Boolean.TRUE);
			} else {
				request.setAttribute("forStems", Boolean.FALSE);
			}
			//List of subjectIds
			String[] members = request.getParameterValues("members");
			subjectRes = new ArrayList();

			String subjectTypeId;
			Subject subject = null;
			String subjectId;
			String sourceId;
			Group subjectGroup;
			Map subjectMap = null;
			if(members!=null) {
				for (int i = 0; i < members.length; i++) {
					subjectId = members[i];
					//Type specified for each subjectId since there could be 
					//a mixture
					subjectTypeId = request.getParameter("subjectType:"
							+ members[i]);
					sourceId = request.getParameter("sourceId:"
							+ members[i]);
					try {
						if ("group".equals(subjectTypeId)) {
							subjectGroup = GroupFinder.findByUuid(grouperSession,
									subjectId);
							subjectMap = GrouperHelper.group2Map(grouperSession,
									subjectGroup);
						} else {
							
							subjectMap = GrouperHelper.subject2Map(grouperSession,
									subjectId, subjectTypeId,sourceId);
						}
					}catch(Exception e) {
						Throwable t=NavExceptionHelper.fillInStacktrace(e);
						LOG.error("Error retrieving subject: " + subjectId + "," + subjectTypeId + ","+sourceId + ": " + NavExceptionHelper.toLog(t));
						throw new UnrecoverableErrorException("error.populate-assign-new-members.bad-subject",t,subjectId);
					
					}
					subjectRes.add(subjectMap);
				}
			}else{
				addMessage(new Message("error.assign-members.none-selected",true), request);
			}
			total = subjectRes.size();

		}
		if (subjectRes == null) {
			subjectRes = new ArrayList();
			total = 0;
		}

		
		//working with user selection so no need to page
		if (checked) {
			searchObj.put("pageSize", new Integer(total));
			session.setAttribute("searchObj", searchObj);
		}
		request.setAttribute("subjectResultsCount", new Integer(subjectRes
				.size()));

		//Set up CollectionPager for view 
		//TODO: make more generic?
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		CollectionPager pager = new CollectionPager(null, subjectRes, total, null,
				start, null, pageSize);
		Map searchFieldParams = filterParameters(request,"searchField.");
		if(!searchFieldParams.isEmpty()){
			pager.setParams(searchFieldParams);
			session.setAttribute("advancedSearchFieldParams",searchFieldParams);
			pager.setParam("advSearch", "Y");
			pager.setParam("callerPageId", searchForm.get("callerPageId"));
			pager.setParam("maxFields", searchForm.get("maxFields"));
		}
		
		if (fromSearch) {
			pager.setTarget("/searchNewMembers");
		} else {
			pager.setTarget(mapping.getPath());
		}
		String listField=(String) session.getAttribute("findForListField");
		request.setAttribute("pager", pager);
		request.setAttribute("searchedPeople", searchedPeople);
		request.setAttribute("searchedGroups", searchedGroups);
		String stemId = request.getParameter("stemId");
		if (stemId != null && stemId.length() > 0)
			request.setAttribute("forStems", Boolean.TRUE);

		Map privs = GrouperHelper.hasAsMap(grouperSession, groupOrStem);
		request.setAttribute("targetPrivs", privs);
		request.setAttribute("subtitleArgs", new Object[] { findMap
				.get("displayExtension") ,listField});

		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, groupOrStem));
		return mapping.findForward(FORWARD_AssignNewMembers);
	}
}
