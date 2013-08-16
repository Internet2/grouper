/*
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which retrieves and makes available a Subject.  
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">callerPageId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id of previous page</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject we are 
      viewing the summary for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the type of the Subject 
      we are viewing the summary for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">sourceId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the source of the Subject 
      we are viewing the summary for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubject</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Added to links so that other 
      pages know to return here</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">membershipListScope</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">imm, eff, all, access or naming 
      - determines scope of membership list or whether groups where subject has 
      specified Access privilege, or Stems where subject has specified Naming 
      privilege, are shown</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">returnTo</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Where to return to</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">returnToLinkKey</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Label to use for return link</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">accessPriv</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">selected Access privilege - 
      only used if scope=access</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">namingPriv</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">selected Naming privilege - 
      only used if scope=naming</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">changeMode</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if true then chnage browse mode 
      to Subject Search</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">User selected list field</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">advancedSearch</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If false, cancels group search 
      for privileges</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">subject which we are showing 
      summary for</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectAttributeNames</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">names of all subject atributes 
      to be displayed on page</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">scopeListData</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of keys so correct labels 
      for selected scope can be used</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">pager</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CollectionPager used to render 
      user selection</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">allAccessPrivs</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of privileges user can 
      select from</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">allNamingPrivs</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of privileges user can 
      select from</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of available list fields 
      for group - to enable user to change view</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">listFieldParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map used to make link parameters</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">memberOfListFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of list fields the subject 
      is a member of</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">pagerParams</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map which is copy of pager parameters 
      - can be used when generating links</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">saveParams</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of parameters for link allowing 
      Subject to be saved to list</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">fromSubjectSummary</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Boolean which indicates to group 
      search that user is finding privileges for group search results</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=subject.action.show-summary</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">lastSubjectSummaryForm</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If no subject id passed in retrieve 
      last details. Save details - can link to subject summary without parameters</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectMembershipListScope</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If request parameter for membershipListScope 
      is not present, read this session attribute. Save current value to session</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectSummaryAccessPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If request parameter for accessPriv 
      is not present, read this session attribute. Save current value to session</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectSummaryNamingPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If request parameter for namingPriv 
      is not present, read this session attribute. Save current value to session</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupSearchSubject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">A Subject object so that group 
      search machinery can find appropriate privileges</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupSearchSubjectMap</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">A SubjectAsMap object so that 
      group search UI can indicate in the UI who the privileges belong to</font></td>
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
 * @version $Id: PopulateSubjectSummaryAction.java,v 1.23.2.1 2009-04-07 16:21:04 mchyzer Exp $
 */
public class PopulateSubjectSummaryAction extends GrouperCapableAction {
	
	protected static final Log LOG = LogFactory.getLog(PopulateSubjectSummaryAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_SubjectSummary = "SubjectSummary";
	static final private String FORWARD_GroupSearch = "GroupSearch";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		NavExceptionHelper neh=getExceptionHelper(session);
		DynaActionForm subjectForm = (DynaActionForm) form;
		if("true".equals(request.getParameter("changeMode"))) PopulateSearchSubjectsAction.initMode(session);
		session.setAttribute("subtitle", "subject.action.show-summary");
		if(isEmpty(subjectForm.get("callerPageId"))) {
			if(isEmpty(subjectForm.get("subjectId"))) {
				LOG.info("Restoring lastSubjectSummaryForm");
				restoreDynaFormBean(session,subjectForm,"lastSubjectSummaryForm");
			}else{
				LOG.info("Saving lastSubjectSummaryForm");
				saveDynaFormBean(session,subjectForm,"lastSubjectSummaryForm");
				saveAsCallerPage(request,subjectForm);
			}
		}
		saveAsCallerPage(request,subjectForm);
		
		String listField = (String) subjectForm.get("listField");
		String membershipField = "members";
		
		if(!isEmpty(listField)) {
			membershipField=listField;
		}
		Field mField = null;
		try {
			mField=FieldFinder.find(membershipField);
		}catch(SchemaException e) {
			LOG.error("Could not find Field: " + membershipField,e);
			if("members".equals(membershipField)) {
				LOG.fatal("Built in field: members, missing");
				throw new UnrecoverableErrorException(e);
			}else{
				mField=FieldFinder.find("members");
				request.setAttribute("message",new Message("error.subject-summary.missing-field",listField,true));
			}
		}
		
		subjectForm.set("contextSubject","true");
		String subjectId = (String)subjectForm.get("subjectId");
		String subjectType = (String)subjectForm.get("subjectType");
		String subjectSource = (String)subjectForm.get("sourceId");
		if(isEmpty(subjectId) || isEmpty(subjectType) || isEmpty(subjectSource)) {
			String msg = neh.missingParameters(subjectId,"subjectId",subjectType,"subjectType",subjectSource,"sourceId");
			LOG.error(msg);
			if(doRedirectToCaller(subjectForm)) {
				session.setAttribute("sessionMessage",new Message("error.subject-summary.missing-parameter",true));
				return redirectToCaller(subjectForm);
			}
			throw new UnrecoverableErrorException("error.subject-summary.missing-parameter");
		}
		Subject subject = null;
		try{ 
			subject=SubjectFinder.findById(subjectId,subjectType,subjectSource);
		}catch (Exception e) {
			LOG.error(e);
			String contextError="error.subject-summary.subject.exception";
			session.setAttribute("sessionMessage",new Message(neh.key(e),contextError,true));
			if(doRedirectToCaller(subjectForm)) return redirectToCaller(subjectForm);
			throw new UnrecoverableErrorException(contextError,e);
		}
		Map subjectMap = GrouperHelper.subject2Map(subject);
		request.setAttribute("subject",subjectMap);
		
		String order=null;
		try {
			order=getMediaResources(request).getString("subject.attributes.order." + subject.getSource().getId());
			request.setAttribute("subjectAttributeNames",order.split(","));
		}catch(Exception e){
			//No order specified, so go with all, in whatever order they come
			List extendedAttr  = new ArrayList(subject.getAttributes().keySet());
			extendedAttr.add("subjectType");
			extendedAttr.add("id");
			request.setAttribute("subjectAttributeNames",extendedAttr);
		}
		String membershipListScope = (String) subjectForm.get("membershipListScope");
		
		if("any-access".equals(membershipListScope)) {
			if("false".equals(request.getParameter("advancedSearch"))) {
				membershipListScope=null;
			}else{
				request.setAttribute("fromSubjectSummary",Boolean.TRUE);
				
				session.setAttribute("groupSearchSubject", subject);
				session.setAttribute("groupSearchSubjectMap", subjectMap);
				return mapping.findForward(FORWARD_GroupSearch);
			}
		}
		if (":all:imm:eff:access:naming:".indexOf(":" + membershipListScope + ":") == -1) {
			membershipListScope = (String) session
				.getAttribute("subjectMembershipListScope");
		}
		if (membershipListScope == null)
			membershipListScope = "imm";
		session.setAttribute("subjectMembershipListScope", membershipListScope);
		subjectForm.set("membershipListScope", membershipListScope);
		
		String accessPriv = (String) subjectForm.get("accessPriv");
		if(isEmpty(accessPriv)) accessPriv = (String)session.getAttribute("subjectSummaryAccessPriv");
		if(isEmpty(accessPriv)) accessPriv = "read";
		session.setAttribute("subjectSummaryAccessPriv",accessPriv);
		subjectForm.set("accessPriv",accessPriv);
		
		String namingPriv = (String) subjectForm.get("namingPriv");
		if(isEmpty(namingPriv)) namingPriv = (String)session.getAttribute("subjectSummaryNamingPriv");
		if(isEmpty(namingPriv)) namingPriv = "create";
		session.setAttribute("subjectSummaryNamingPriv",namingPriv);
		subjectForm.set("namingPriv",namingPriv);
		//Retrieve the membership according to scope selected by user
		Member member = null;
		try {
			member=MemberFinder.findBySubject(grouperSession,subject);
		}catch(Exception e) {
			LOG.error(e);
			if(doRedirectToCaller(subjectForm)) {
				session.setAttribute("sessionMessage",new Message("error.subject-summary.member.exception",true));
				return redirectToCaller(subjectForm);
			}
			throw new UnrecoverableErrorException("error.subject-summary.member.exception",e);
		
		}
		Set subjectScopes = null;
		List subjectScopeMaps = null;
		Map listViews = new HashMap();
		listViews.put("titleKey","subject.summary.memberships");
		listViews.put("noResultsKey","subject.list-membership.none");
		listViews.put("view","whereSubjectsAreMembers");
		//listViews.put("itemView","whereIsMemberLink");
		listViews.put("itemView","subjectSummary");
		listViews.put("headerView","genericListHeader");
		listViews.put("footerView","genericListFooter");
		
		if ("imm".equals(membershipListScope)) {
			subjectScopes = member.getImmediateMemberships(mField);
			listViews.put("noResultsKey","subject.list-membership.imm.none");
		} else if ("eff".equals(membershipListScope)) {
			if(membershipField.equals("members")) {
				subjectScopes = member.getMemberships();
				subjectScopes.removeAll(member.getImmediateMemberships());
				
			}else{
				subjectScopes = member.getEffectiveMemberships(mField);
				listViews.put("noResultsKey","subject.list-membership.all.none");
			}
			if("members".equals(membershipField)) {
				listViews.put("noResultsKey","subject.list-membership.eff.none");
			}else{
				listViews.put("noResultsKey","subject.list-membership.custom.eff.none");
			}
			
		} else if ("all".equals(membershipListScope)){
			subjectScopes = member.getMemberships(mField);
			listViews.put("noResultsKey","subject.list-membership.all.none");
		}else if("access".equals(membershipListScope)) {
			
			subjectScopes = GrouperHelper.getGroupsOrStemsWhereMemberHasPriv(member,accessPriv);

			// filter out groups where the subject can't see privs
			removeObjectsNotAllowedToSeePrivs(subjectScopes);

			subjectScopeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(grouperSession,subjectScopes,subject,accessPriv);
			listViews.put("titleKey","subject.summary.access-privs");
			listViews.put("noResultsKey","subject.list-access.none");
			listViews.put("view","subjectSummaryPrivileges");
			listViews.put("itemView","subjectSummaryPrivilege");
		}else {
			subjectScopes = GrouperHelper.getGroupsOrStemsWhereMemberHasPriv(member,namingPriv);

      // filter out stems where the subject can't see privs
			removeObjectsNotAllowedToSeePrivs(subjectScopes);

			subjectScopeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(grouperSession,subjectScopes,subject,namingPriv);
			listViews.put("titleKey","subject.summary.naming-privs");
			listViews.put("noResultsKey","subject.list-naming.none");
			listViews.put("view","subjectSummaryPrivileges");
			listViews.put("itemView","subjectSummaryPrivilege");
		}
		request.setAttribute("scopeListData",listViews);
		if(subjectScopeMaps==null) {
			Map countMap = new HashMap();
			Map sources=new HashMap();
			List uniqueSubjectScopes = GrouperHelper.getOneMembershipPerSubjectOrGroup(subjectScopes,"subject",countMap,sources,0);
			subjectScopeMaps = GrouperHelper.memberships2Maps(grouperSession,uniqueSubjectScopes);
			GrouperHelper.setMembershipCountPerSubjectOrGroup(subjectScopeMaps,"subject",countMap);
		}
		subjectScopeMaps = sort(subjectScopeMaps,request,"subjectSummary", -1);
		String startStr = (String)subjectForm.get("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		int end = start + pageSize;
		if (end > subjectScopeMaps.size())
			end = subjectScopeMaps.size();
		CollectionPager pager = new CollectionPager(subjectScopeMaps, subjectScopeMaps
				.size(), null, start, null, pageSize);
		
		if(!isEmpty(listField))pager.setParam("listField", listField);
		pager.setParam("subjectId", subjectId);
		pager.setParam("subjectType", subjectType);
		pager.setParam("sourceId", subjectSource);
		pager.setParam("returnTo", subjectForm.get("returnTo"));
		pager.setParam("returnToLinkKey", subjectForm.get("returnToLinkKey"));
		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		request.setAttribute("linkParams", pager.getParams().clone());
		request.setAttribute("listFieldParams", pager.getParams().clone());
		
		Map saveParams = new HashMap();
		saveParams.put("subjectId",subject.getId());
		saveParams.put("subjectType",subject.getType().getName());
		saveParams.put("sourceId",subject.getSource().getId());
		saveParams.put("callerPageId",request.getAttribute("thisPageId"));
		request.setAttribute("saveParams",saveParams);
		
		if(subjectType.equals("group")) {
			List lists = GrouperHelper.getReadableListFieldsForGroup(grouperSession,subjectId);
			if(!lists.isEmpty()) request.setAttribute("listFields",lists);
		}
		
		List memberOfListFields = GrouperHelper.getListFieldsForSubject(grouperSession,subject);
		if(memberOfListFields.size()>0) {
			request.setAttribute("memberOfListFields",memberOfListFields);
		}
		
		String[] accessPrivs = GrouperHelper.getGroupPrivs(grouperSession);
		Collection namingPrivs = GrouperHelper.getStemPrivsWithLabels(getNavResources(request));
		request.setAttribute("allAccessPrivs",accessPrivs);
		request.setAttribute("allNamingPrivs",namingPrivs);
		
		return mapping.findForward(FORWARD_SubjectSummary);
	}

  /**
   * remove objects not allowed to see privileges on
   * @param groupsAndStems
   */
  public static void removeObjectsNotAllowedToSeePrivs(Set<?> groupsAndStems) {
    
    if (groupsAndStems == null) {
      return;
    }

    //subject who is making the query
    final Subject grouperSessionSubject = GrouperSession.staticGrouperSession().getSubject();

    Iterator<?> iterator = groupsAndStems.iterator();

    while (iterator.hasNext()) {
      Object groupOrStem = iterator.next();

      if (groupOrStem instanceof Group) {
        
        Group group = (Group)groupOrStem;
        if (!group.hasAdmin(grouperSessionSubject)) {
          iterator.remove();
        }
      } else if (groupOrStem instanceof Stem) {

        Stem stem = (Stem)groupOrStem;
        if (!stem.hasStem(grouperSessionSubject)) {
          iterator.remove();
        }

      } else {
        //this should never happen
        throw new RuntimeException("Not expecting object of type: " + groupOrStem.getClass() + ", " + groupOrStem);
      }
    }   
  }
}
