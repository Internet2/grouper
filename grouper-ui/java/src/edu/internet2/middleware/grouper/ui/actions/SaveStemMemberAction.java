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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which saves any changes to the Naming privileges held by 
 * a subject. 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">asMemberOf</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies stem which is focus 
      of action</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privileges</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Naming privileges selected 
      by user</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject we are 
      making assignments to</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubject</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies that we started out 
      looking at a subject</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">callerPageId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the page to which 
      we should return</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">message instance: text derived 
      <br>
      from priv.action.assigned key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
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
 * @version $Id: SaveStemMemberAction.java,v 1.7 2009-03-15 06:37:51 mchyzer Exp $
 */
public class SaveStemMemberAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_StemPriviligees = "StemPriviligees";
	static final private String FORWARD_SubjectSummary = "SubjectSummary";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm groupOrStemMemberForm = (DynaActionForm) form;
		String asMemberOf = (String) groupOrStemMemberForm.get("asMemberOf");
		String[] privileges = (String[]) groupOrStemMemberForm.get("privileges");
		String subjectId = (String) groupOrStemMemberForm.get("subjectId");
		String subjectType = (String) groupOrStemMemberForm.get("subjectType");
		String sourceId = (String) groupOrStemMemberForm.get("sourceId");
		boolean forStems = true;


		Stem curStem = StemFinder.findByUuid(grouperSession,
				asMemberOf, true);
		Map subjectMap = GrouperHelper.subject2Map(grouperSession, subjectId,
				subjectType,sourceId);
		Subject subj = null;
		try {
			subj=SubjectFinder.findByIdentifierAndSource(subjectId, sourceId, true);
		}catch(Exception e) {
			subj = new UnresolvableSubject(subjectId,subjectType,sourceId);
		}
		
		Member member = MemberFinder.findBySubject(grouperSession,subj, true);
		
		//Get new privileges as a Map
		Map privs = GrouperHelper.getImmediateHas(grouperSession, GroupOrStem.findByStem(grouperSession,curStem), member);
		Map newPrivs = new HashMap();
		int newPrivsCount = 0;
		for (int i = 0; i < privileges.length; i++) {
			newPrivs.put(privileges[i], Boolean.TRUE);
			if (!privs.containsKey(privileges[i])) {
				newPrivsCount++;
			}
		}
		//And as an array
		String[] newPrivileges = new String[newPrivsCount];
		int newCount = 0;
		for (int i = 0; i < privileges.length; i++) {
			if (!privs.containsKey(privileges[i])) {
				newPrivileges[newCount++] = privileges[i];
			}
		}
		//TODO: should be transactional
		//revoke any not selected that were there:

		Iterator it = privs.keySet().iterator();
		String key;
		while (it.hasNext()) {
			key = (String) it.next();
			if (!newPrivs.containsKey(key)) {
				curStem.revokePriv(member.getSubject(),Privilege.getInstance(key.toLowerCase()));
			}
		}
		//Assign privileges
		
		GrouperHelper.assignPrivileges(grouperSession, curStem.getUuid(),
				new Subject[] { member.getSubject() }, 
				newPrivileges, forStems);

		request.setAttribute("message", new Message("priv.action.assigned"));
		
		if(doRedirectToCaller(groupOrStemMemberForm))return redirectToCaller(groupOrStemMemberForm);
		if(!isEmpty(groupOrStemMemberForm.get("contextSubject"))) return mapping.findForward(FORWARD_SubjectSummary);
		return mapping.findForward(FORWARD_StemPriviligees);

	}

}