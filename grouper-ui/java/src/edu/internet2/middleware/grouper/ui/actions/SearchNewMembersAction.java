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
import edu.internet2.middleware.grouper.GrouperStem;
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
    <td><p><font face="Arial, Helvetica, sans-serif">searchFor</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies Subject type we are 
      searching for - currently limited to person or group</font></td>
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
    <td><p><font face="Arial, Helvetica, sans-serif">personSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which personSource 
      to search if searching for people</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">searchedPeople</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that people were searched</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchedGroups</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that groups were searched</font></td>
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
 * @version $Id: SearchNewMembersAction.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
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
		String browseMode = getBrowseMode(session);
		if (browseMode == null)
			browseMode = "";
		Map searchObj = new HashMap();
		//Determine search parameters
		String query = (String) searchForm.get("searchTerm");
		String searchFor = (String) searchForm.get("searchFor");
		String searchFrom = (String) searchForm.get("searchFrom");
		String searchStart = (String) searchForm.get("start");
		String newSearch = (String) searchForm.get("newSearch");
		String personSource = (String) searchForm.get("personSource");
		String searchInNameOrExtension = (String) searchForm.get("searchInNameOrExtension");
		String searchInDisplayNameOrExtension = (String) searchForm.get("searchInDisplayNameOrExtension");
		boolean forStem = "true".equals(searchForm.get("stems"));
		if (searchStart == null || searchStart.length() == 0)
			searchStart = "0";
		if ("Y".equals(newSearch)){
			//Initialise searchObj with search parameters
			searchObj.put("searchTerm", query);
			searchObj.put("searchFor", searchFor);
			searchObj.put("searchFrom", searchFrom);
			searchObj.put("personSource", personSource);
			searchObj.put("searchInNameOrExtension", searchInNameOrExtension);
			searchObj.put("searchInDisplayNameOrExtension", searchInDisplayNameOrExtension);
		} else {
			//Retrieve last search
			searchObj = (Map) session.getAttribute("searchObj");
			query = (String) searchObj.get("searchTerm");
			searchFor = (String) searchObj.get("searchFor");
			searchFrom = (String) searchObj.get("searchFrom");
			personSource = (String) searchObj.get("personSource");
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
		if (searchFor.indexOf("people") > -1) {
			searchedPeople = Boolean.TRUE;
			StringBuffer tmp = new StringBuffer();
			//TODO: implement true subject interface when available
			//Do search  + get page worth of results
			SourceManager sm= SourceManager.getInstance();
			Source personSourceImpl = sm.getSource(personSource);
			Set results = personSourceImpl.search(query);
			subjectRes = GrouperHelper.subjects2Maps(results.toArray());
			resultSize = results.size();
		}
		Boolean searchedGroups = Boolean.FALSE;
		
		//Did we search for groups
		if (searchFor.indexOf("groups") > -1) {
			searchedGroups = Boolean.TRUE;
			GrouperStem searchFromStem = (GrouperStem)GrouperStem.loadByID(
					grouperSession, searchFrom);
			//Do search + get page worth of results
			subjectRes = GrouperHelper.searchGroups(grouperSession, query,
					searchFrom,searchInDisplayNameOrExtension,searchInNameOrExtension, browseMode);
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