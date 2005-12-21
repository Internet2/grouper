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

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.RepositoryBrowserFactory;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Top level search for people or groups which can be made members or
 * privilegees of the context group. 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to obtain sublist of total 
      search results i.e. subjectResults, in conjunction wit hdefault page size</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchTerm</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The actual query</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchFrom</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies stem which scopes 
      search results</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchInNameOrExtension=name 
        or extension</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which group attribute 
      to search </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchInDisplayNameOrExtension=name 
        or extension</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which group attribute 
      to search </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">newSearch</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates if we have carried 
      out a new search vs paged a previous search</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which sourceAdapter 
      to search</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">forStems</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates if we are searching 
      for new privilegees for a stem</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId,stemId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates which stem or group 
      we are searching 'on behalf of'</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectResults</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Sublist of search results</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectResultsSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Size of total list of search 
      results</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The id by which this page was 
      saved and can be returned to. Used on links and forms</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchObj</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If not new search retrieve search 
      criteria, otherwise save current criteria</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if groupId or stemId not 
      input </font></td>
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
 * @version $Id: SearchNewMembersAction.java,v 1.4 2005-12-21 15:37:08 isgwb Exp $
 */
public class SearchNewMembersAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_AssignNewMembers = "AssignNewMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm searchForm = (DynaActionForm)form;
		saveAsCallerPage(request,searchForm,"findForNode searchObj");
		String browseMode = getBrowseMode(session);
		if (browseMode == null)
			browseMode = "";
		Map searchObj = new HashMap();
		//Determine search parameters
		String query = (String) searchForm.get("searchTerm");

		String searchFrom = (String) searchForm.get("searchFrom");
		String searchStart = (String) searchForm.get("start");
		String newSearch = (String) searchForm.get("newSearch");
		String subjectSource = (String) searchForm.get("subjectSource");
		String searchInNameOrExtension = (String) searchForm.get("searchInNameOrExtension");
		String searchInDisplayNameOrExtension = (String) searchForm.get("searchInDisplayNameOrExtension");
		boolean forStem = "true".equals(searchForm.get("stems"));
		if (searchStart == null || searchStart.length() == 0)
			searchStart = "0";
		if ("Y".equals(newSearch)){
			//Initialise searchObj with search parameters
			searchObj.put("searchTerm", query);

			searchObj.put("searchFrom", searchFrom);
			searchObj.put("subjectSource", subjectSource);
			searchObj.put("searchInNameOrExtension", searchInNameOrExtension);
			searchObj.put("searchInDisplayNameOrExtension", searchInDisplayNameOrExtension);
		} else {
			//Retrieve last search
			searchObj = (Map) session.getAttribute("searchObj");
			query = (String) searchObj.get("searchTerm");

			searchFrom = (String) searchObj.get("searchFrom");
			subjectSource = (String) searchObj.get("subjectSource");
			searchInNameOrExtension = (String) searchObj.get("searchInNameOrExtension");
			searchInDisplayNameOrExtension = (String) searchObj.get("searchInDisplayNameOrExtension");
		}
		//Deal with paging
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		int resultSize = 0;
		searchObj.put("trueSearch", Boolean.TRUE);
		searchObj.put("start", new Integer(Integer.parseInt(searchStart)));
		List subjectRes = null;

		Boolean searchedPeople = Boolean.FALSE;
		String targetId = null;
		//Determine stem or group that is target
		if(forStem) {
			targetId=(String)searchForm.get("groupId");
		}else{
			targetId=(String)searchForm.get("stemId");
		}
		if (targetId == null || targetId.length() == 0)
			targetId = (String) session.getAttribute("findForNode");
		
		//Did we search for people?
		if (!"g:gsa".equals(subjectSource)) {
			searchedPeople = Boolean.TRUE;
			StringBuffer tmp = new StringBuffer();
			//TODO: implement true subject interface when available
			//Do search  + get page worth of results
			Set results = null;
			if("all".equals(subjectSource)) {
				results = SubjectFinder.findAll(query);
			}else{
				SourceManager sm= SourceManager.getInstance();
				Source personSourceImpl = sm.getSource(subjectSource);
				
				results = personSourceImpl.search(query);
			}
			subjectRes = GrouperHelper.subjects2Maps(results.toArray());
			resultSize = results.size();
		}
		Boolean searchedGroups = Boolean.FALSE;
		
		//Did we search for groups
		if ("g:gsa".equals(subjectSource)) {
			searchedGroups = Boolean.TRUE;
			
			//Do search + get page worth of results
			RepositoryBrowser repositoryBrowser = RepositoryBrowserFactory.getInstance("all",grouperSession,getMediaResources(request));
			Map attr = new HashMap();
			attr.put("searchInDisplayNameOrExtension",searchInDisplayNameOrExtension);
			attr.put("searchInNameOrExtension",searchInNameOrExtension);
			
			subjectRes = repositoryBrowser.search(grouperSession, query,
					searchFrom, attr);
			
			resultSize = subjectRes.size();
			int end = start + pageSize;
			if (end > resultSize)
				end = resultSize;
			subjectRes = GrouperHelper.groups2Maps(grouperSession, subjectRes
					.subList(start, end));

		}
		//Make results and search criteria available
		request.setAttribute("subjectResults", subjectRes);
		request.setAttribute("subjectResultsSize", new Integer(resultSize));
		request.setAttribute("searchedPeople", searchedPeople);
		request.setAttribute("searchedGroups", searchedGroups);
		session.setAttribute("searchObj", searchObj);
		
		//Another action processes results to make available for display
		return mapping.findForward(FORWARD_AssignNewMembers);

	}

}