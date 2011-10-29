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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.ProcessSearchTerm;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Top level search for subjects - gives subject centred approach.  
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
    <td><p><font face="Arial, Helvetica, sans-serif">subjectSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which source adapter 
      was chosen</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupSearchResultField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies which group field 
      should be used for the results page</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">used to render results</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">lastSubjectSearch</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If not new search retrieve search 
      criteria, otherwise save current criteria</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupSearchResultField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Remember user selection</font></td>
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
 * @version $Id: DoSearchSubjectsAction.java,v 1.14 2009-10-30 15:06:34 isgwb Exp $
 */
public class DoSearchSubjectsAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_SearchResults = "SearchResults";

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
		SourceManager sm = SourceManager.getInstance();
		String sourceId = (String)searchForm.get("subjectSource");
		if(isEmpty(sourceId)){
			Map lastSearch = (Map)session.getAttribute("lastSubjectSearch");
			if(!isEmpty(lastSearch)) {
				Iterator entrySetIterator = lastSearch.entrySet().iterator();
				Map.Entry entry;
				while(entrySetIterator.hasNext()) {
					entry = (Map.Entry)entrySetIterator.next();
					searchForm.set((String)entry.getKey(),entry.getValue());
				}
			}
			sourceId = (String)searchForm.get("subjectSource");
		}
		String groupSearchResultField = (String) searchForm.get("groupSearchResultField");
		if(!isEmpty(groupSearchResultField)) {
			session.setAttribute("groupSearchResultField",groupSearchResultField);
		}

		Map lastSearch = new HashMap();
		lastSearch.putAll(searchForm.getMap());
		session.setAttribute("lastSubjectSearch",lastSearch);
		Set results = null;
		session.setAttribute("lastSubjectSource",sourceId);
		
		String searchTerm = (String) searchForm.get("searchTerm");
		
		results = new LinkedHashSet();
		
		try {
  		if ((searchTerm != null) && (!searchTerm.equals(""))) {
  			if("all".equals(sourceId)) {
  				results=SubjectFinder.findPage(searchTerm).getResults();
  			}else{
  				
  					Source source = sm.getSource(sourceId);
  					ProcessSearchTerm processSearchTerm = new ProcessSearchTerm();
  					String processedSearchTerm = processSearchTerm.processSearchTerm(source, searchTerm, request);
  					results = source.search(processedSearchTerm);
  				
  			}
  		}
		} catch (SubjectTooManyResults stmr) {
		  session.setAttribute("sessionMessage",new Message("error.too.many.subject.results",true));
		  return redirectToCaller((DynaActionForm)form);
		}
		
		Iterator it = results.iterator();
		Subject subj;
		
		while(it.hasNext()) {
			subj=(Subject)it.next();
			if(subj.getSource().getId().equals("g:gsa")) {
				try {
					Group g = GroupFinder.findByUuid(grouperSession, subj.getId(), true);
				}catch(Exception e) {
					it.remove();
				}
			}
		}
		
		Map addAttr = new HashMap();
		addAttr.put("returnTo","/doSearchSubjects.do");
		addAttr.put("returnToLinkKey","subject.action.return-results");
		List mapResults = GrouperHelper.subjects2Maps(sort(results,request,"search:"  +groupSearchResultField, -1).toArray(),addAttr);
		
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		int end = start + pageSize;
		if (end > mapResults.size())
			end = mapResults.size();
		CollectionPager pager = new CollectionPager(null, mapResults, mapResults
				.size(), null, start, null, pageSize);

		pager.setParam("searchTerm", searchTerm);
		//pager.setParam("searchFrom", searchFrom);
		//pager.setParam("searchInNameOrExtension", searchInNameOrExtension);
		//pager.setParam("searchInDisplayNameOrExtension", searchInDisplayNameOrExtension);
		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		
		return mapping.findForward(FORWARD_SearchResults);

	}

}