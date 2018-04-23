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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * Top level Struts Action which shows all the ways in which a member is 
 * a member of a group - used so users can follow effective memberships.   
 * <p/>
 * <table width="75%" border="1">
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
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the Subject we are 
      viewing chains for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the type of the Subject 
      we are viewing chains for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifiesgroup which is focus 
      of action</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies list field we are 
      interested in</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">subtitle</font><font face="Arial, Helvetica, sans-serif">: 
      text derived <br>
      from groups.action.show-members key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">chainPaths</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of objects representing 
      the chains</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The current group we are showing 
      chains for</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">composite</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If the group has a composite 
      member, make the Composite available as a Map</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The subject we are showing chains 
      for </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">requestParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">request.getParameterMap</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">linkParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Empty Map for use in templates 
      to build parameters for a link</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">groupMemberParams</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map with parameters for link 
      to populateGroupMember. Allows direct assignment of privileges to the subject</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">privs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set of directly assigned privileges 
      for the subject on this group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">privsSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Number of items in privs</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group which is focus 
      of action</font></td>
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
 * 
 * @author Gary Brown.
 * @version $Id: PopulateChainsAction.java,v 1.14 2009-03-15 06:37:51 mchyzer Exp $
 */
public class PopulateChainsAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateChainsAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Chain = "Chain";

	

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		session.setAttribute("subtitle","groups.action.show-members");
		String subjectId = request.getParameter("subjectId");
		String subjectType = request.getParameter("subjectType");
		String sourceId = request.getParameter("sourceId");
		if(isEmpty(subjectId) || isEmpty(subjectType) || isEmpty(sourceId)) {
			String msg = neh.missingParameters(subjectId,"subjectId",subjectType,"subjectType",sourceId,"sourceId");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.chain.missing-parameter");
		}
		Subject subject = null;
		
		try{
			subject=SubjectFinder.findById(subjectId,subjectType,sourceId, true);
		}catch (Exception e) {
			LOG.error("Unable to retrieve subject: " + subjectId + "," + sourceId,e);
			String contextError="error.chain.subject.exception";
			throw new UnrecoverableErrorException(contextError,e,subjectId);
		}
		DynaActionForm groupForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupForm,"findForNode");
		
		//Identify the group whose membership we are showing
		String groupId = (String)groupForm.get("groupId");
		
		String listField = (String) groupForm.get("listField");
		String membershipField = "members";
		
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = FieldFinder.find(membershipField, true);
		//TODO: check following - shouldn't I always pass parameter
		if (groupId == null || groupId.length() == 0)
			groupId = (String) session.getAttribute("findForNode");
		if (groupId == null)
			groupId = request.getParameter("asMemberOf");
		if(isEmpty(groupId)) {
			String msg = neh.missingAlternativeParameters(groupId,"groupId",groupId,"asMemberOf",groupId,"findForNode");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.chain.missing-group-id");
		}
		Group group = null;
		try{
			group=GroupFinder.findByUuid(grouperSession,groupId, true);
		}catch(GroupNotFoundException e) {
			LOG.error("No group with id=" + groupId,e);
			throw new UnrecoverableErrorException("error.chain.bad-id",groupId);
		}
		Map gMap = null;
		if(group.hasComposite()) {
			Composite comp = CompositeFinder.findAsOwner(group, true);
			gMap = GrouperHelper.getCompositeMap(grouperSession,comp,subject);
			request.setAttribute("composite",gMap);
			request.setAttribute("linkParams",new HashMap());
		}else{
			List ways = null;
			try {
				ways=GrouperHelper.getAllWaysInWhichSubjectIsMemberOFGroup(grouperSession,subject,group,mField);
			}catch(Exception e) {
				Throwable t=NavExceptionHelper.fillInStacktrace(e);
				LOG.error("Error populating chains: " + NavExceptionHelper.toLog(t));
				throw new UnrecoverableErrorException("error.chains.determine",t,subject.getId(),group.getUuid(),mField.getName());
			
			}
			Group g = null;
			Membership m = null;
			
			List chains=new ArrayList();
			List chain;
			for(int i=0;i<ways.size();i++) {
				m = (Membership)ways.get(i);
				g=m.getGroup();
	
				gMap = GrouperHelper.group2Map(grouperSession,g);
				chain=GrouperHelper.getChain(grouperSession,m);
				gMap.put("chainPath",chain);
				gMap.put("chainPathSize",new Integer(chain.size()));
				
				chains.add(gMap);
			}
			
			
			request.setAttribute("chainPaths", chains);
		}
		request.setAttribute("browseParent", GrouperHelper.group2Map(
				grouperSession, group));
		request.setAttribute("requestParams",request.getParameterMap());
		Map groupMemberParams = new HashMap(request.getParameterMap());
		groupMemberParams.put("callerPageId",request.getAttribute("thisPageId"));
		request.setAttribute("groupMemberParams",groupMemberParams);
		request.setAttribute("subject",GrouperHelper.subject2Map(subject));
		Map privs = GrouperHelper.hasAsMap(grouperSession,GroupOrStem.findByGroup(grouperSession,group),MemberFinder.findBySubject(grouperSession,subject, true),FieldFinder.find("members", true));
		privs.remove("member");
		request.setAttribute("privs",privs.keySet());
		request.setAttribute("privsSize",new Integer(privs.size()));
		
		return mapping.findForward(FORWARD_Chain);

	}

}
