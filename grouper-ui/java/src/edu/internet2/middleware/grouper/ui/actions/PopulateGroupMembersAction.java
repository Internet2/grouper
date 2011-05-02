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


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

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
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * Top level Strut's action which retrieves and makes available group members.  
 
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
      see members for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">asMemberOf</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if groupId and findForNode are 
      empty, asMemberOf identifies group</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">membershipListScope=all or 
        imm or eff</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates whether to show only 
      immediate or effective members, or both. If not set, set to imm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.addMembers</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates user really wants 
      to add members rather than display members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubject</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates we got here from SubjectSummary</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Custom list field we should 
      display 'members' for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.import</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that user has clicked 
      'Import members' button</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.export</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that user has clicked 
      'Export members' button</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">selectedSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Filters members by source. Retrieved from session if not present</font></td>
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
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;isCompositeGroup</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Indicates whether group 
      is composite</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">membership</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map used by Strut's &lt;html:link&gt; 
      tags when generating parameters for &lt;a&gt; tags</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">contextSubject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Makes request parameter available 
      to JSTL</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Current list field in scope 
      (if any). Default is membership list</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">canWriteField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Can the current user add members 
      to the current list</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">removableMembers</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Can the current user remove 
      members from the current list. Only true if there are members to be removed 
      and immediate members are being viewed</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">exportMembers</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Collection of members to 
      be exported</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">sources</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of source ids - display 
      names. If &gt;1 then let user filter</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">sourcesSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Number of sources represented 
      in result set</font></td>
  </tr>
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
    <td><font face="Arial, Helvetica, sans-serif">membershipListScope</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif"> SET if present as request parameter 
      or does not exist, otherwise READ to use as default</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.show-members</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if groupId not set</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">selectedSource</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">See Request parameter of same name</font></td>
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
 * @version $Id: PopulateGroupMembersAction.java,v 1.26 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PopulateGroupMembersAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateGroupMembersAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";
	static final private String FORWARD_AddGroupMembers = "AddGroupMembers"; 
	static final private String FORWARD_ExportMembers = "ExportMembers";
	static final private String FORWARD_ImportMembers = "ImportMembers";
	static final private String FORWARD_StemMembers = "StemMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		if(!isEmpty(request.getParameter("submit.addMembers"))) return mapping.findForward(FORWARD_AddGroupMembers);
		if(!isEmpty(request.getParameter("submit.import"))) {
			return mapping.findForward(FORWARD_ImportMembers);
		}
		String selectedSource=null;
		session.setAttribute("subtitle","groups.action.show-members");
		String noResultsKey="groups.list-members.none";
		DynaActionForm groupForm = (DynaActionForm) form;
		if(isEmpty(request.getParameter("submit.export"))) {
			saveAsCallerPage(request,groupForm,"findForNode membershipListScope");
		}
		request.setAttribute("contextSubject",groupForm.get("contextSubject"));
		//Identify the group whose membership we are showing
		String groupId = (String)groupForm.get("groupId");
		
		//TODO: check following - shouldn't I always pass parameter
		if (groupId == null || groupId.length() == 0)
			groupId = (String) session.getAttribute("findForNode");
		if (groupId == null)
			groupId = request.getParameter("asMemberOf");
		
		if(isEmpty(groupId)) {
			String msg = neh.missingAlternativeParameters(groupId,"groupId",groupId,"asMemberOf",groupId,"findForNode");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.group-members.missing-grouporstem-id");
		}
		Group group = null;
		String listField = request.getParameter("listField");
		String membershipField = "members";
		
		selectedSource = (String)groupForm.get("selectedSource");
		
		if(isEmpty(selectedSource)) {
			selectedSource=(String)session.getAttribute("selectedSource");
			groupForm.set("selectedSource",selectedSource);
		}else{
			session.setAttribute("selectedSource",selectedSource);
		}
		
		Set<Source> sourceFilter = new HashSet<Source>();
		if(selectedSource!=null && !"_void_".equals(selectedSource)) {
			sourceFilter.add(SourceManager.getInstance().getSource(selectedSource));
		}
		
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = null;
		
		try {
			mField=FieldFinder.find(membershipField, true);
		}catch(SchemaException e) {
			LOG.error("Error retrieving " + membershipField,e);
			throw new UnrecoverableErrorException("error.group-members.bad-field",e,membershipField);
		}
		
		//Determine whether to show immediate, effective only, or all memberships
		String membershipListScope = (String) groupForm
				.get("membershipListScope");
		if (":all:imm:eff:".indexOf(":" + membershipListScope + ":") == -1) {
			membershipListScope = (String) session
					.getAttribute("membershipListScope");
		}
		if (membershipListScope == null)
			membershipListScope = "imm";
		session.setAttribute("membershipListScope", membershipListScope);
		groupForm.set("membershipListScope", membershipListScope);
		
		//Retrieve the membership according to scope selected by user
		try{
			group=GroupFinder.findByUuid(grouperSession,groupId, true);
		}catch(GroupNotFoundException e) {
			LOG.error("Error retirving group with id=" + groupId,e);
			throw new UnrecoverableErrorException("error.group-members.bad-id",groupId);
		}
		
		UIGroupPrivilegeResolver resolver = 
			UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
			    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
					                                    group, grouperSession.getSubject());
		request.setAttribute("groupPrivResolver", resolver.asMap());
		if(resolver.canManageField(mField.getName())) request.setAttribute("canWriteField",Boolean.TRUE);
		
		List listFields = GrouperHelper.getListFieldsForGroup(grouperSession,group);
		request.setAttribute("listFields",listFields);
		request.setAttribute("listFieldsSize",new Integer(listFields.size()));
		
		ResourceBundle mediaResource = GrouperUiFilter.retrieveSessionMediaResourceBundle();
		ResourceBundle navResource = GrouperUiFilter.retrieveSessionNavResourceBundle();
		
		// get member sort and search parameters from the request
		String memberSortIndex = request.getParameter("memberSortIndex");
		String memberSearchValue = GrouperUtil.isEmpty(request.getParameter("memberSearchValue")) ? null : request.getParameter("memberSearchValue");
		
		// get member sort and search properties
    String memberSortEnabled = mediaResource.containsKey("member.sort.enabled") ? mediaResource.getString("member.sort.enabled") : null;
    String memberSearchEnabled = mediaResource.containsKey("member.search.enabled") ? mediaResource.getString("member.search.enabled") : null;
    
    String memberSortDefaultOnly = mediaResource.containsKey("member.sort.defaultOnly") ? mediaResource.getString("member.sort.defaultOnly") : null;

    // figure out if and how member sorting and searching should be done
    SortStringEnum sortStringEnum = null;
    SearchStringEnum searchStringEnum = null;
    Map<Integer, String> memberSortSelections = new TreeMap<Integer, String>();

    if ("true".equals(memberSortEnabled)) {
      if (!"true".equals(memberSortDefaultOnly)) {
        for (int i = 0; i < 5; i++) {
          String sortDisplayName = navResource.containsKey("member.sort.string" + i) ? navResource.getString("member.sort.string" + i) : null;
          if (sortDisplayName != null && SortStringEnum.newInstance(i).hasAccess()) {
            memberSortSelections.put(i, sortDisplayName);
          }
        }
        
        if (memberSortSelections.size() > 0) {
          request.setAttribute("memberSortSelections", memberSortSelections);
        }
      }
      
      if ("true".equals(memberSortDefaultOnly)) {
        sortStringEnum = SortStringEnum.getDefaultSortString();
      } else if (GrouperUtil.isEmpty(memberSortIndex)) {
        sortStringEnum = SortStringEnum.getDefaultSortString();
        if (sortStringEnum == null && memberSortSelections.size() > 0) {
          // if the user doesn't have access to the default sort strings but has access to other sort strings, sort using a non-default sort string.
          sortStringEnum = SortStringEnum.newInstance(memberSortSelections.keySet().iterator().next());
        }
        
        if (sortStringEnum != null) {
          request.setAttribute("memberSortIndex", sortStringEnum.getIndex());
          memberSortIndex = "" + sortStringEnum.getIndex();
        }
      } else {
        sortStringEnum = SortStringEnum.newInstance(Integer.parseInt(memberSortIndex));
        request.setAttribute("memberSortIndex", memberSortIndex);        
      }
    }
    
    if ("true".equals(memberSearchEnabled) && SearchStringEnum.getDefaultSearchString() != null) {
      request.setAttribute("showMemberSearch", true);
      
      if (!GrouperUtil.isEmpty(memberSearchValue)) {
        request.setAttribute("memberSearchValue", memberSearchValue);
        searchStringEnum = SearchStringEnum.getDefaultSearchString();
      }
    } else {
      memberSearchValue = null;
    }
    
    
		Set<Member> nMembers = null;
		
    //Set up CollectionPager for view
    String startStr = request.getParameter("start");
    if (startStr == null || "".equals(startStr))
      startStr = "0";

    int start = Integer.parseInt(startStr);
    int pageSize = getPageSize(session);
    int end = start + pageSize;
    
    MembershipDAO membershipDao = GrouperDAOFactory.getFactory().getMembership();

	QueryOptions queryOptions = new QueryOptions();
	QueryPaging queryPaging = new QueryPaging();
	if(!isEmpty(request.getParameter("submit.export"))) {
		//Exporting - so we want to export everything
		queryPaging.setPageSize(10000000);
		queryPaging.setPageNumber(1);
	}else{
		queryPaging.setPageSize(pageSize);
		queryPaging.setPageNumber((start / pageSize) + 1);
	}
	
	queryOptions.paging(queryPaging);
	queryOptions.retrieveCount(true);
	Set<String> sourceIds = null;
		if ("imm".equals(membershipListScope)) {
			if(group.hasComposite()&& "members".equals(membershipField)) {
				nMembers = new HashSet<Member>();
				sourceIds = new HashSet<String>();
			}else{
				nMembers=membershipDao.findAllMembersByOwnerAndFieldAndType(group.getUuid(),mField,MembershipType.IMMEDIATE.getTypeString(),sourceFilter,queryOptions,true, sortStringEnum, searchStringEnum, memberSearchValue);
				sourceIds = membershipDao.findSourceIdsByGroupOwnerOptions(group.getUuid(),MembershipType.IMMEDIATE,mField,true);
			}
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.imm.none";
			}else{
				noResultsKey="groups.list-members.custom.imm.none";
			}
		} else if ("eff".equals(membershipListScope)) {
			if(group.hasComposite()&& membershipField.equals("members")) {
				nMembers=membershipDao.findAllMembersByOwnerAndFieldAndType(group.getUuid(),mField,MembershipType.COMPOSITE.getTypeString(),sourceFilter,queryOptions,true, sortStringEnum, searchStringEnum, memberSearchValue);
				sourceIds = membershipDao.findSourceIdsByGroupOwnerOptions(group.getUuid(),MembershipType.COMPOSITE,mField,true);
			}else{
				nMembers=membershipDao.findAllMembersByOwnerAndFieldAndType(group.getUuid(),mField,MembershipType.EFFECTIVE.getTypeString(),sourceFilter,queryOptions,true, sortStringEnum, searchStringEnum, memberSearchValue);
				sourceIds = membershipDao.findSourceIdsByGroupOwnerOptions(group.getUuid(),MembershipType.EFFECTIVE,mField,true);
			}
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.eff.none";
			}else{
				noResultsKey="groups.list-members.custom.eff.none";
			}
		} else {
			nMembers=membershipDao.findAllMembersByOwnerAndFieldAndType(group.getUuid(),mField,null,sourceFilter,queryOptions,true, sortStringEnum, searchStringEnum, memberSearchValue);
			sourceIds = membershipDao.findSourceIdsByGroupOwnerOptions(group.getUuid(),null,mField,true);
			if("members".equals(membershipField)) {
				noResultsKey="groups.list-members.all.none";
			}else{
				noResultsKey="groups.list-members.custom.all.none";
			}
		}
		Map compMap = null;
		if(membershipField.equals("members")) {
			if(group.hasComposite()) {
				if(!"eff".equals(membershipListScope)) {
					Composite comp = CompositeFinder.findAsOwner(group, true);
					compMap = GrouperHelper.getCompositeMap(grouperSession,comp);
				}
				request.setAttribute("isCompositeGroup",Boolean.TRUE);
			}
		}
		
		Map sources = new HashMap();
		for(String sourceId : sourceIds) {
			sources.put(sourceId,SourceManager.getInstance().getSource(sourceId).getName());
		}
				
		Map.Entry entry = null;
		Iterator sIterator = sources.entrySet().iterator();
		String lookupKey=null;
		while(sIterator.hasNext()) {
			entry=(Map.Entry) sIterator.next();
			try {
				lookupKey="subject-source."+ entry.getKey()+".display-name";
				entry.setValue(GrouperUiFilter.retrieveSessionNavResourceBundle().getString(lookupKey));
			}catch(Exception e){}
		}
		
		int nMemberCount = queryPaging.getTotalRecordCount();
		
		if("_void_".equals(selectedSource)) selectedSource=null;
		
		request.setAttribute("sources",sources);
		request.setAttribute("sourcesSize", sources.size());
		request.setAttribute("browseParent", GrouperHelper.group2Map(grouperSession, group));
		if(!isEmpty(request.getParameter("submit.export"))) {
			request.setAttribute("exportMembers",nMembers);
			return mapping.findForward(FORWARD_ExportMembers);
		}
		
		List<String> memberIds = new ArrayList<String>();
		List<String> empty = new ArrayList<String>();
		List<String> groupIds = new ArrayList<String>();
		groupIds.add(group.getUuid());
		
		List<Membership> nMemberships = new ArrayList<Membership>();
		Map<String, Integer> pathCount = new HashMap<String, Integer>();
		Map<String, Membership> nMembershipMap = new HashMap<String, Membership>(); 
		for(Member m : nMembers) {
			memberIds.add(m.getUuid());
		}
		
		if (memberIds.size() > 0) {
  		Set<Object[]> res = MembershipFinder.findMemberships(groupIds, memberIds, empty, null, mField, null, null, null, null, true);
  		for(Object[] objects : res) {
  			String memberId = ((Member)objects[2]).getUuid();
  			nMembershipMap.put(memberId, (Membership)objects[0]);
  			Integer count = pathCount.get(memberId);
  			int newCount=1;
  			if(count !=null) {
  				newCount = count.intValue() + 1;
  			}
  			pathCount.put(memberId, newCount);
  		}
		}
		
		for(Member m : nMembers) {
			String memberId = m.getUuid();
			Membership ms = nMembershipMap.get(memberId);
			nMemberships.add(ms);
		}
		
		if (end > nMemberCount)
			end = nMemberCount;
		
		List membershipMaps = GrouperHelper.memberships2Maps(grouperSession, nMemberships);
		GrouperHelper.setMembershipCountPerSubjectOrGroup(membershipMaps,"group",pathCount);
		if(compMap!=null) {
			membershipMaps.add(0,compMap);
			nMemberCount++;
		}
		if(nMemberCount <= pageSize && sortStringEnum == null) {
			membershipMaps=sort(membershipMaps,request,"members", nMemberCount);
		}
		CollectionPager pager = new CollectionPager(null, membershipMaps,nMemberCount,
				null, start, null, pageSize);
		pager.setParam("groupId", groupId);
		
		if (!GrouperUtil.isEmpty(memberSearchValue)) {
		  pager.setParam("memberSearchValue", URLEncoder.encode(memberSearchValue, "UTF-8"));
		}
		
    if (!GrouperUtil.isEmpty(memberSortIndex)) {
      pager.setParam("memberSortIndex", memberSortIndex);
    }
		
		pager.setTarget(mapping.getPath());
		if(!isEmpty(listField))pager.setParam("listField", listField);
		request.setAttribute("pager", pager);
		request.setAttribute("listField",listField);
		request.setAttribute("linkParams", pager.getParams().clone());
		Map membership = new HashMap();
		
		membership.put("groupId", groupId);
		membership.put("callerPageId",request.getAttribute("thisPageId"));
		membership.put("memberSortIndex", memberSortIndex);
		if(!isEmpty(listField)) membership.put("listField",listField);
		//TODO: some of this looks familar  - look at refactoring
		Map privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByGroup(grouperSession, group));
		request.setAttribute("groupPrivs", privs);
		
		
		request.setAttribute("groupMembership", membership);
		request.setAttribute("noResultsKey", noResultsKey);
		request.setAttribute("removableMembers",new Boolean("imm".equals(membershipListScope) && resolver.canManageField(mField.getName()) && !group.isComposite() && nMemberCount>0));
		
		return mapping.findForward(FORWARD_GroupMembers);

	}

}