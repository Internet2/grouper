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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.RepositoryBrowserFactory;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.ProcessSearchTerm;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectTooManyResults;
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
    <td><p><font face="Arial, Helvetica, sans-serif">groupSearchResultField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The group field to display on 
      results page</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">queryOutTerms</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of (query,field,and / or 
      / not) used to display what was searched for</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">groupSearchResultField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Maintain user selection</font></td>
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
 * @version $Id: SearchNewMembersAction.java,v 1.14 2009-10-30 15:06:34 isgwb Exp $
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
	  
    //put this in the request since it is a common inlucde shared by many pagers
    ResourceBundle mediaResources = GrouperUiFilter.retrieveSessionMediaResourceBundle();
    String removeFromSubjectSearch = mediaResources.getString("pager.removeFromSubjectSearch");
    request.setAttribute("pager_removeFromSubjectSearch", removeFromSubjectSearch);
    
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
		String groupSearchResultField = (String) searchForm.get("groupSearchResultField");
		if(!isEmpty(groupSearchResultField)) {
			session.setAttribute("groupSearchResultField",groupSearchResultField);
		}
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
		List<Map<Object,Object>> subjectRes = null;

		Boolean searchedPeople = Boolean.FALSE;
		String targetId = null;
		//Determine stem or group that is target
		if(!forStem) {
			targetId=(String)searchForm.get("groupId");
		}else{
			targetId=(String)searchForm.get("stemId");
		}
		if (targetId == null || targetId.length() == 0)
			targetId = (String) session.getAttribute("findForNode");
		
		String stemName = null;
    Group group = null;
		if (!forStem) {
      try {
        group = GroupFinder.findByUuid(grouperSession, targetId, true);
      }catch(GroupNotFoundException e) {
        LOG.error("Error retrieving group with id=" + targetId,e);
        throw new UnrecoverableErrorException("error.search-new-members.bad-group-id",targetId);
      }
      stemName = group.getParentStemName();
		} else {
      try {
        Stem stem = StemFinder.findByUuid(grouperSession, targetId, true);
        stemName = stem.getName();
      }catch(StemNotFoundException e) {
        LOG.error("Error retrieving stem with id=" + targetId,e);
        throw new UnrecoverableErrorException("error.search-new-members.bad-stem-id",targetId);
      }
		  
		}
		
		//Did we search for people?
		if (!"g:gsa".equals(subjectSource)) {
			searchedPeople = Boolean.TRUE;
			StringBuffer tmp = new StringBuffer();
			//TODO: implement true subject interface when available
			//Do search  + get page worth of results
			Set results = null;
			

      results = new LinkedHashSet();
      try {
  			if ((query != null) && (!query.equals(""))) {
  				SearchPageResult searchPageResult = null;
  				if("all".equals(subjectSource)) {
  					searchPageResult=SubjectFinder.findPageInStem(stemName, query);
  					
  				}else{
  					SourceManager sm= SourceManager.getInstance();
  					Source personSourceImpl = sm.getSource(subjectSource);
  					
  					ProcessSearchTerm processSearchTerm = new ProcessSearchTerm();
  					String processedSearchTerm = processSearchTerm.processSearchTerm(personSourceImpl, query, request);
  					
  					searchPageResult = SubjectFinder.findPage(processedSearchTerm, GrouperUtil.toSet(personSourceImpl));
  				}
  				results = searchPageResult.getResults();
  	  			if(searchPageResult.isTooManyResults()) {
  	  				request.setAttribute("message",new Message("error.too.many.subject.results.for.source",true));
  	  				request.setAttribute("isTruncatedResults",true);
  	  			}
  		  }
      } catch (SubjectTooManyResults stmr) {
        session.setAttribute("sessionMessage",new Message("error.too.many.subject.results",true));
        if(doRedirectToCaller(searchForm)) {
        	return redirectToCaller(searchForm);
        }
      }
      subjectRes = GrouperHelper.subjects2Maps(results.toArray());
      resultSize = subjectRes.size();
    }
		Boolean searchedGroups = Boolean.FALSE;
		
		//Did we search for groups
		List outTerms = new ArrayList();
		if ("g:gsa".equals(subjectSource)) {
			searchedGroups = Boolean.TRUE;
			
			//Do search + get page worth of results
			RepositoryBrowser repositoryBrowser = RepositoryBrowserFactory.getInstance(
			    "all",grouperSession, GrouperUiFilter.retrieveSessionNavResourceBundle(),GrouperUiFilter.retrieveSessionMediaResourceBundle());
			Map attr = new HashMap();
			attr.put("searchInDisplayNameOrExtension",searchInDisplayNameOrExtension);
			attr.put("searchInNameOrExtension",searchInNameOrExtension);
			
			subjectRes = repositoryBrowser.search(grouperSession, query,
					searchFrom, request.getParameterMap(),outTerms);
			
			resultSize = subjectRes.size();
			int end = start + pageSize;
			if (end > resultSize)
				end = resultSize;
			subjectRes = GrouperHelper.groups2Maps(grouperSession, subjectRes
					.subList(start, end));

		}
		
		//loop through and make sure the subject can read the group if it is a group
		Iterator<Map<Object,Object>> iterator = subjectRes.iterator();
		while (iterator.hasNext()) {
		  Map<Object,Object> map = iterator.next();
		  String subjectType = (String)map.get("subjectType");
		  //will this always be accurate??
		  if (StringUtils.equals("group", subjectType)) {
		    Object groupGet = map.get("group");
		    Group theGroup = groupGet instanceof Group ? (Group)groupGet : null;
		    if (theGroup == null) {
	        String subjectId = (String)map.get("subjectId");
		      try {
		        theGroup = GroupFinder.findByUuid(grouperSession,subjectId, true);
		      } catch (Exception e) {
		        //this is probably ok, just cant find it
		        LOG.debug("Cant find group: " + subjectId, e);
		      }
		    }
	      if (theGroup != null) {
	        
	        try {
	          PrivilegeHelper.dispatch( grouperSession, theGroup, 
	              grouperSession.getSubject(), Group.getDefaultList().getReadPriv() );
	        } catch (Exception e) {
            //this is probably ok, just not allowed
	          if (LOG.isDebugEnabled()) {
              LOG.debug("Not allowed to read: " 
                + GrouperUtil.subjectToString(grouperSession.getSubject()) + ", " + theGroup.getName(), e);
	          }
	          iterator.remove();
	        }
	        
	      } else {
	        iterator.remove();
	      }
		  }
		}
		if(!forStem) {
			 UIGroupPrivilegeResolver resolver = 
					UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
					    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
						                                    	group, grouperSession.getSubject());
				request.setAttribute("groupPrivResolver", resolver.asMap());
		}
		resultSize = subjectRes.size();
		
		//Make results and search criteria available
		request.setAttribute("subjectResults", subjectRes);
		request.setAttribute("subjectResultsSize", new Integer(resultSize));
		request.setAttribute("searchedPeople", searchedPeople);
		request.setAttribute("searchedGroups", searchedGroups);
		request.setAttribute("queryOutTerms",outTerms);
		session.setAttribute("searchObj", searchObj);
		
		//Another action processes results to make available for display
		return mapping.findForward(FORWARD_AssignNewMembers);

	}

}