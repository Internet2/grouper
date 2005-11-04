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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.ForwardAction;

import edu.internet2.middleware.grouper.GrouperAccess;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperMember;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFactory;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.subject.Subject;


/**
 * Top level Strut's action which saves privileges for a subject 
 * for a group and / or makes the subject a member of the group. 
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
    <td><font face="Arial, Helvetica, sans-serif">Identifies group which is focus 
      of action</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privileges</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Access privileges selected 
      by user</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privilege</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that we were finding 
      in the context of this privilege - so we should return to list of privilegees 
      and not members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">callerPageId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies page to which we 
      should return</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject we are 
      making assignments to</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject we are 
      making assignments to</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the subject where 
      we started out</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubjectType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the type of the Subject 
      where we started out</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextSubject</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies that there is a contextSubject</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextGroup</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the group where we 
      started out</font></td>
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
 * @version $Id: SaveGroupMemberAction.java,v 1.2 2005-11-04 14:17:12 isgwb Exp $
 */
public class SaveGroupMemberAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";
	static final private String FORWARD_GroupMember = "GroupMember";
	static final private String FORWARD_GroupPrivilegees = "GroupPrivilegees";
	static final private String FORWARD_SubjectSummary = "SubjectSummary";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		DynaActionForm groupOrStemMemberForm = (DynaActionForm) form;
		String contextGroup = (String) groupOrStemMemberForm.get("contextGroup");
		String asMemberOf = (String) groupOrStemMemberForm.get("asMemberOf");
		String[] privileges = (String[]) groupOrStemMemberForm.get("privileges");
		String privilege = (String) groupOrStemMemberForm.get("privilege");
		String subjectId = (String) groupOrStemMemberForm.get("subjectId");
		String subjectType = (String) groupOrStemMemberForm.get("subjectType");
		if(isEmpty(asMemberOf)&& !isEmpty(contextGroup))asMemberOf=contextGroup;
		GrouperAccess accessImpl = grouperSession.access();
		GrouperGroup curGroup = (GrouperGroup)GrouperGroup.loadByID(grouperSession,
				asMemberOf);
		Map subjectMap = GrouperHelper.subject2Map(grouperSession, subjectId,
				subjectType);
		GrouperMember member = GrouperMember.load(grouperSession,subjectId, subjectType);
		List failedRevocations=new ArrayList();
		//Get privileges as they existed and determine new ones as Map
		Map privs = GrouperHelper.getImmediateHas(grouperSession, curGroup, member);
		Map newPrivs = new HashMap();
		int newPrivsCount = 0;
		for (int i = 0; i < privileges.length; i++) {
			newPrivs.put(privileges[i], Boolean.TRUE);
			if (!privs.containsKey(privileges[i])) {
				newPrivsCount++;
			}
		}
		
		//Also get as String[]
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
				if (key.equals("MEMBER")) {
					curGroup.listDelVal(member);
				} else {
					try {
						accessImpl.revoke(grouperSession, curGroup, member, key);
					}catch(Exception e) {
						if(e.getMessage().indexOf("List value does not exist")>-1) {
							failedRevocations.add(key);
						}
					}
				}
			}
		}
		//Assign new privileges
		
		if(failedRevocations.size()==0) {
			GrouperHelper.assignPrivileges(grouperSession, curGroup.id(),
				new Subject[] { SubjectFactory.getSubject(subjectId,subjectType) },
				newPrivileges, false);
			request.setAttribute("message", new Message("priv.action.assigned"));
		}else{
			request.setAttribute("message", new Message("priv.action.assigned-failed",true));
			request.setAttribute("failedRevocations", failedRevocations);
			return mapping.findForward(FORWARD_GroupMember);
		}
		if(doRedirectToCaller(groupOrStemMemberForm))return redirectToCaller(groupOrStemMemberForm);
		if(!isEmpty(groupOrStemMemberForm.get("contextSubject"))) return mapping.findForward(FORWARD_SubjectSummary);
		
		if(isEmpty(privilege)) {
			if(!isEmpty(contextGroup)) {
				return new ActionForward("populateChains.do?subjectId=" + groupOrStemMemberForm.get("contextSubjectId") +
						"&subjectType=" + groupOrStemMemberForm.get("contextSubjectType") +
						"&groupId=" + groupOrStemMemberForm.get("contextGroup")+
						"&contextSubject=" + groupOrStemMemberForm.get("contextSubject"),true);
			}
			return mapping.findForward(FORWARD_GroupMembers);
		}
		return mapping.findForward(FORWARD_GroupPrivilegees);
	}
}