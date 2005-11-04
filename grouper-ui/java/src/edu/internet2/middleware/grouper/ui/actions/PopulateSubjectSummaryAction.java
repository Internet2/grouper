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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Grouper;
import edu.internet2.middleware.grouper.GrouperAccess;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperMember;
import edu.internet2.middleware.grouper.GrouperNaming;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFactory;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which retrieves and makes available a Subject 
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
    <td><p><font face="Arial, Helvetica, sans-serif">pagerParams</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map which is copy of pager parameters 
      - can be used when generating links</font></td>
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
 * @version $Id: PopulateSubjectSummaryAction.java,v 1.1 2005-11-04 16:46:52 isgwb Exp $
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
		
		session.setAttribute("subtitle", "subject.action.show-summary");
		DynaActionForm subjectForm = (DynaActionForm) form;
		if(isEmpty(subjectForm.get("callerPageId"))) {
			if(isEmpty(subjectForm.get("subjectId"))) {
				restoreDynaFormBean(session,subjectForm,"lastSubjectSummaryForm");
			}else{
				saveDynaFormBean(session,subjectForm,"lastSubjectSummaryForm");
			}
		}
		subjectForm.set("contextSubject","true");
		String subjectId = (String)subjectForm.get("subjectId");
		String subjectType = (String)subjectForm.get("subjectType");
		Subject subject = SubjectFactory.getSubject(subjectId,subjectType);
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
		if(isEmpty(accessPriv)) accessPriv = Grouper.PRIV_READ;
		session.setAttribute("subjectSummaryAccessPriv",accessPriv);
		subjectForm.set("accessPriv",accessPriv);
		
		String namingPriv = (String) subjectForm.get("namingPriv");
		if(isEmpty(namingPriv)) namingPriv = (String)session.getAttribute("subjectSummaryNamingPriv");
		if(isEmpty(namingPriv)) namingPriv = Grouper.PRIV_CREATE;
		session.setAttribute("subjectSummaryNamingPriv",namingPriv);
		subjectForm.set("namingPriv",namingPriv);
		//Retrieve the membership according to scope selected by user
		GrouperMember member = GrouperMember.load(grouperSession,subject);
		List subjectScopes = null;
		List subjectScopeMaps = null;
		Map listViews = new HashMap();
		listViews.put("titleKey","subject.summary.memberships");
		listViews.put("noResultsKey","subject.list-membership.none");
		listViews.put("view","subjectMemberships");
		listViews.put("itemView","subjectMembership");
		listViews.put("headerView","genericListHeader");
		listViews.put("footerView","genericListFooter");
		
		if ("imm".equals(membershipListScope)) {
			subjectScopes = member.listImmVals();
		} else if ("eff".equals(membershipListScope)) {
			subjectScopes = member.listEffVals();
			listViews.put("noResultsKey","subject.list-membership.eff.none");
		} else if ("all".equals(membershipListScope)){
			subjectScopes = member.listVals();
		}else if("access".equals(membershipListScope)) {
			GrouperAccess accessImpl = grouperSession.access();
			subjectScopes = accessImpl.has(grouperSession,member,accessPriv);
			listViews.put("titleKey","subject.summary.access-privs");
			listViews.put("noResultsKey","subject.list-access.none");
			listViews.put("view","subjectAccessPrivs");
			listViews.put("itemView","subjectAccessPriv");
		}else {
			GrouperNaming namingImpl = grouperSession.naming();
			subjectScopes = namingImpl.has(grouperSession,member,namingPriv);
			listViews.put("titleKey","subject.summary.naming-privs");
			listViews.put("noResultsKey","subject.list-naming.none");
			listViews.put("view","subjectNamingPrivs");
			listViews.put("itemView","subjectNamingPriv");
		}
		request.setAttribute("scopeListData",listViews);
		subjectScopeMaps = GrouperHelper.groupList2SubjectsMaps(grouperSession,subjectScopes);
		
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

		pager.setParam("subjectId", subjectId);
		pager.setParam("subjectType", subjectType);
		pager.setParam("returnTo", subjectForm.get("returnTo"));
		pager.setParam("returnToLinkKey", subjectForm.get("returnToLinkKey"));
		pager.setTarget(mapping.getPath());
		request.setAttribute("pager", pager);
		request.setAttribute("pagerParams", pager.getParams().clone());
		String[] accessPrivs = GrouperHelper.getGroupPrivs(grouperSession);
		String[] namingPrivs = GrouperHelper.getStemPrivs(grouperSession);
		request.setAttribute("allAccessPrivs",accessPrivs);
		request.setAttribute("allNamingPrivs",namingPrivs);
		
		return mapping.findForward(FORWARD_SubjectSummary);
	}
}