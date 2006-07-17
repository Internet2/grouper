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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
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
    <td><font face="Arial, Helvetica, sans-serif">if request parameter for membershipListScope 
      is not present, read this session attribute. Save current value to session</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectSummaryAccessPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if request parameter for accessPriv 
      is not present, read this session attribute. Save current value to session</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectSummaryNamingPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">if request parameter for namingPriv 
      is not present, read this session attribute. Save current value to session</font></td>
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
 * @version $Id: PopulateSubjectSummaryAction.java,v 1.10 2006-07-17 12:52:57 isgwb Exp $
 */
public class PopulateSubjectSummaryAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_SubjectSummary = "SubjectSummary";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		
		DynaActionForm subjectForm = (DynaActionForm) form;
		if("true".equals(request.getParameter("changeMode"))) PopulateSearchSubjectsAction.initMode(session);
		session.setAttribute("subtitle", "subject.action.show-summary");
		if(isEmpty(subjectForm.get("callerPageId"))) {
			if(isEmpty(subjectForm.get("subjectId"))) {
				restoreDynaFormBean(session,subjectForm,"lastSubjectSummaryForm");
			}else{
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
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		subjectForm.set("contextSubject","true");
		String subjectId = (String)subjectForm.get("subjectId");
		String subjectType = (String)subjectForm.get("subjectType");
		Subject subject = SubjectFinder.findById(subjectId,subjectType);
		Map subjectMap = GrouperHelper.subject2Map(subject);
		
		request.setAttribute("subject",subjectMap);
		request.setAttribute("subjectAttributeNames",subject.getAttributes().keySet());
		
		String membershipListScope = (String) subjectForm.get("membershipListScope");
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
		Member member = MemberFinder.findBySubject(grouperSession,subject);
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
		} else if ("eff".equals(membershipListScope)) {
			if(membershipField.equals("members")) {
				subjectScopes = member.getMemberships();
				subjectScopes.removeAll(member.getImmediateMemberships());
			}else{
				subjectScopes = member.getEffectiveMemberships(mField);
			}
			
			listViews.put("noResultsKey","subject.list-membership.eff.none");
		} else if ("all".equals(membershipListScope)){
			subjectScopes = member.getMemberships(mField);
		}else if("access".equals(membershipListScope)) {
			
			subjectScopes = GrouperHelper.getGroupsOrStemsWhereMemberHasPriv(member,accessPriv);
			subjectScopeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(grouperSession,subjectScopes,subject,accessPriv);
			listViews.put("titleKey","subject.summary.access-privs");
			listViews.put("noResultsKey","subject.list-access.none");
			listViews.put("view","subjectSummaryPrivileges");
			listViews.put("itemView","subjectSummaryPrivilege");
		}else {
			subjectScopes = GrouperHelper.getGroupsOrStemsWhereMemberHasPriv(member,namingPriv);
			subjectScopeMaps = GrouperHelper.subjects2SubjectPrivilegeMaps(grouperSession,subjectScopes,subject,namingPriv);
			listViews.put("titleKey","subject.summary.naming-privs");
			listViews.put("noResultsKey","subject.list-naming.none");
			listViews.put("view","subjectSummaryPrivileges");
			listViews.put("itemView","subjectSummaryPrivilege");
		}
		request.setAttribute("scopeListData",listViews);
		if(subjectScopeMaps==null) {
			Map countMap = new HashMap();
			List uniqueSubjectScopes = GrouperHelper.getOneMembershipPerSubjectOrGroup(subjectScopes,"subject",countMap);
			subjectScopeMaps = GrouperHelper.memberships2Maps(grouperSession,uniqueSubjectScopes);
			GrouperHelper.setMembershipCountPerSubjectOrGroup(subjectScopeMaps,"subject",countMap);
		}
		
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
		pager.setParam("returnTo", subjectForm.get("returnTo"));
		pager.setParam("returnToLinkKey", subjectForm.get("returnToLinkKey"));
		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		request.setAttribute("linkParams", pager.getParams().clone());
		request.setAttribute("listFieldParams", pager.getParams().clone());
		
		Map saveParams = new HashMap();
		saveParams.put("subjectId",subject.getId());
		saveParams.put("subjectType",subject.getType().getName());
		saveParams.put("callerPageId",request.getAttribute("thisPageId"));
		request.setAttribute("saveParams",saveParams);
		
		if(subjectType.equals("group")) {
			List lists = GrouperHelper.getListFieldsForGroup(grouperSession,subjectId);
			if(!lists.isEmpty()) request.setAttribute("listFields",lists);
		}
		
		List memberOfListFields = GrouperHelper.getListFieldsForSubject(grouperSession,subject);
		if(memberOfListFields.size()>0) {
			request.setAttribute("memberOfListFields",memberOfListFields);
		}
		
		String[] accessPrivs = GrouperHelper.getGroupPrivs(grouperSession);
		String[] namingPrivs = GrouperHelper.getStemPrivs(grouperSession);
		request.setAttribute("allAccessPrivs",accessPrivs);
		request.setAttribute("allNamingPrivs",namingPrivs);
		
		return mapping.findForward(FORWARD_SubjectSummary);
	}
}