/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
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
    <td><p><font face="Arial, Helvetica, sans-serif">contextSourceId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the source of the Subject 
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
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List field for which member 
      is being saved</font></td>
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
 * @version $Id: SaveGroupMemberAction.java,v 1.12 2009-08-12 04:52:14 mchyzer Exp $
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
		String sourceId = (String) groupOrStemMemberForm.get("sourceId");
		String listField = (String) groupOrStemMemberForm.get("listField");
		String membershipField = "members";
		
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = FieldFinder.find(membershipField, true);
		if(isEmpty(asMemberOf)&& !isEmpty(contextGroup))asMemberOf=contextGroup;
		
		Group curGroup = GroupFinder.findByUuid(grouperSession,
				asMemberOf, true);
		Map subjectMap = GrouperHelper.subject2Map(grouperSession, subjectId,
				subjectType,sourceId);
		Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findById(subjectId,subjectType,sourceId, true), true);
		List failedRevocations=new ArrayList();
		//Get privileges as they existed and determine new ones as Map
		Map privs = GrouperHelper.getImmediateHas(grouperSession, GroupOrStem.findByGroup(grouperSession,curGroup), member,mField);
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
		
		UIGroupPrivilegeResolver resolver = 
			UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
			    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
					                                    curGroup, grouperSession.getSubject());
		request.setAttribute("groupPrivResolver", resolver.asMap());

		//TODO: should be transactional
		//revoke any not selected that were there:
		boolean hasAdmin = curGroup.hasAdmin(grouperSession.getSubject());
		Iterator it = privs.keySet().iterator();
		String key;
		while (it.hasNext()) {
			key = ((String) it.next());
			if (!newPrivs.containsKey(key)) {
				if (key.toLowerCase().equals("member")) {
					if(!membershipField.equals("members") || !curGroup.hasComposite()) {
						if(resolver.canManageField(mField.getName()))
								curGroup.deleteMember(member.getSubject(),mField);
					}
				} else if(resolver.canManagePrivileges()) {
					try {
						curGroup.revokePriv(member.getSubject(),Privilege.getInstance(key));
						
					}catch(Exception e) {
						if(e.getMessage().indexOf("List value does not exist")>-1) {
							failedRevocations.add(key);
						}
					}
				}
			}
		}
		//Assign new privileges
		if(resolver.canManagePrivileges()) {
			if(failedRevocations.size()==0) {
				GrouperHelper.assignPrivileges(grouperSession, curGroup.getUuid(),
					new Subject[] { member.getSubject() },
					newPrivileges, false,mField); 
				request.setAttribute("message", new Message("priv.action.assigned"));
			}else{
				request.setAttribute("message", new Message("priv.action.assigned-failed",true));
				request.setAttribute("failedRevocations", failedRevocations);
				return mapping.findForward(FORWARD_GroupMember);
			}
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
