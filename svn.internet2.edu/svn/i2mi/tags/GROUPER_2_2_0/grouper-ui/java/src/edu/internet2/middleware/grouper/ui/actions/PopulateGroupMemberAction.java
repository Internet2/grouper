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

import java.util.Iterator;
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
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.MissingGroupOrStemException;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;



/**
 * Top level Strut's action which retrieves privileges for subject, 
 * with respect to a group or stem. This page does not differentiate privileges
 * assigned directly from those derived from group memberships - may 
 * give unexpected behaviour. 
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
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Context for the Subject - the 
      group are we dealing with. If not set, set based on findForNode</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies the member / privilegee 
      we are dealing with </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Required to ensure no conflicts 
      in subjectIds between Subject types</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privileges</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The privileges the user currently 
      has </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">displayExtension</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used in subtitleArgs to give 
      UI context</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">contextGroupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used when we are on a diversion 
      and the group we are modifying is not the browseParent</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Custom list field we should 
      display 'members' for</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Subject obtained from id and 
      type wrapped as a Map</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">authUserPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of privileges the authenticated 
      user has for this group or stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subjectPri</font>v</td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of direct privileges the 
      Subject identified by request parameters has for this group or stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">possiblePrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">All privileges (Naming or Access) 
      which can be assigned</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">possibleEffectivePrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">All privileges (Naming or Access) 
      which can be assigned + MEMBER as psuedo Access priv</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Provides context for UI</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">USed in links and forms so this 
      page can be returned to</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">extendedSubjectPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Effective privileges and how 
      they are derived</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Maintain correct list field</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">findForPriv</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set to privilege request parameter</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=stems.action.edit-member 
      or groups.action.edit-member</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if asMemberOf not set</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">stems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates we are dealing with 
      a stem</font></td>
  </tr>
</table>

 * 
 * 
 * @author Gary Brown.
 * @version $Id: PopulateGroupMemberAction.java,v 1.14 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PopulateGroupMemberAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateGroupMemberAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";

	static final private String FORWARD_GroupMember = "GroupMember";

	static final private String FORWARD_StemMembers = "StemMembers";

	static final private String FORWARD_StemMember = "StemMember";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		//We are using same class for stems and groups, but action is different
		//we distinguish by checking parameter
		
		
		boolean forStems = "stems".equals(mapping.getParameter());
		DynaActionForm groupOrStemMemberForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupOrStemMemberForm,"findForNode");
		String asMemberOf = (String) groupOrStemMemberForm.get("asMemberOf");
		String contextGroupId = (String) groupOrStemMemberForm.get("contextGroup");
		GroupOrStem contextGroup=null;
		
		String listField = request.getParameter("listField");
		String membershipField = "members";
		
		if(!isEmpty(listField)) {
			membershipField=listField;
			request.setAttribute("listField",listField);
		}
		Field mField = null;
		try {
			mField=FieldFinder.find(membershipField, true);
		}catch(SchemaException e) {
			LOG.error("Error retrieving " + membershipField,e);
			throw new UnrecoverableErrorException("error.group-member.bad-field",e,membershipField);
		}
		if(!isEmpty(contextGroupId)) {
			contextGroup = GroupOrStem.findByID(grouperSession,contextGroupId);
		}
		if(isEmpty(asMemberOf) && !isEmpty(contextGroupId)) asMemberOf = contextGroupId;
		
//		If not set explicitly, default to findForNode
		if (isEmpty(asMemberOf)) {
			asMemberOf = (String) session.getAttribute("findForNode");
			groupOrStemMemberForm.set("asMemberOf",asMemberOf);
		}
		//If not set explicitly, default to current browse location
		if (isEmpty(asMemberOf)) {
			asMemberOf = getBrowseNode(session);
			groupOrStemMemberForm.set("asMemberOf",asMemberOf);
		}
		
		if(isEmpty(asMemberOf)) {
			String msg = neh.missingAlternativeParameters(contextGroupId,"contextGroupId",asMemberOf,"asMemberOf",asMemberOf,"findForNode");
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.group-member.missing-grouporstem-id");
		}
		Group group = null;
		Stem stem = null;
		GroupOrStem groupOrStem = null;
		try{
			groupOrStem=GroupOrStem.findByID(grouperSession,asMemberOf);
		}catch(MissingGroupOrStemException e) {
			LOG.error(e);
			throw new UnrecoverableErrorException("error.group-member.bad-id",asMemberOf);
		}
		Map groupOrStemMap = null;
		//In UI we need to display set of possible privileges - with effective ones
		//pre-selected
		String[] possiblePrivs = null;
		String[] possibleEffPrivs = null;
		
		if(forStems) {
			stem= groupOrStem.getStem();
			groupOrStemMap=GrouperHelper.stem2Map(grouperSession,stem);
			session.setAttribute("subtitle","stems.action.edit-member");
			possiblePrivs = GrouperHelper.getStemPrivs(grouperSession);
			possibleEffPrivs=possiblePrivs;
		}else{
			group=groupOrStem.getGroup();
			groupOrStemMap=GrouperHelper.group2Map(grouperSession,group);
			session.setAttribute("subtitle","groups.action.edit-member");
			possiblePrivs = GrouperHelper.getGroupPrivs(grouperSession);
			possibleEffPrivs = GrouperHelper.getGroupPrivsWithMember(grouperSession);
		}
		
		
		request.setAttribute("subtitleArgs", new Object[] { groupOrStemMap.get("displayExtension")});

		
		String subjectId = (String) groupOrStemMemberForm.get("subjectId");
		String subjectType = (String) groupOrStemMemberForm.get("subjectType");
		String sourceId = (String) groupOrStemMemberForm.get("sourceId");
		String msg = neh.missingParameters(subjectId,"subjectId",subjectType,"subjectTypeId",sourceId,"sourceId");
		if(msg!=null) {
			LOG.error(msg);
			throw new UnrecoverableErrorException("error.group-member.missing-subject-parameter");
		}
		//Retrieve subject whose privileges we are displaying and make
		//available to view
		Map subjectMap =null;
		try {
			subjectMap=GrouperHelper.subject2Map(grouperSession, subjectId,
					subjectType,sourceId);
		}catch(Exception e) {
			LOG.error("Error retrieving subject id=" + subjectId,e);
			throw new UnrecoverableErrorException("error.group-member.retrieve-subject",e,subjectId);
		}
		 
		
		//Retrieve privileges current user, and selected subject have over
		//current group/stem
				Member member = null;
		try {
			member=MemberFinder.findBySubject(grouperSession,SubjectFinder.findById(subjectId,subjectType,sourceId, true), true);
		}catch(SubjectNotFoundException e) {
			Subject unresolvable = new UnresolvableSubject(subjectId,subjectType,sourceId);
			member = MemberFinder.findBySubject(grouperSession,unresolvable, true);
		}
    Map authUserPrivs = GrouperHelper.hasAsMap(grouperSession, groupOrStem,false);
		Map privs = GrouperHelper.hasAsMap(grouperSession, groupOrStem, member,mField); 
		Map extendedPrivs = GrouperHelper.getExtendedHas(grouperSession,groupOrStem,member,mField);
		Map immediatePrivs = GrouperHelper.getImmediateHas(grouperSession,groupOrStem,member,mField);
		if(!forStems) {
			UIGroupPrivilegeResolver resolver = 
				UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
				    GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
					                                    	group, grouperSession.getSubject());
			request.setAttribute("groupPrivResolver", resolver.asMap());
		}
		
		//The actual privileges the subject has
		String privileges[] = new String[immediatePrivs.size()];
		Iterator it = immediatePrivs.keySet().iterator();
		String privilege;
		int pos = 0;
		while (it.hasNext()) {
			privilege = (String) it.next();
			privileges[pos++] = privilege;
		}
		groupOrStemMemberForm.set("privileges", privileges);
		//Set up attributes required by view
		request.setAttribute("subject", subjectMap);
		request.setAttribute("authUserPriv", authUserPrivs);
		request.setAttribute("subjectPriv", privs);
		request.setAttribute("extendedSubjectPriv", extendedPrivs);
		
		request.setAttribute("possiblePrivs", possiblePrivs);
		request.setAttribute("possibleEffectivePrivs", possibleEffPrivs);
		
		if(contextGroup==null) {
			request.setAttribute("browseParent", groupOrStemMap);
		}else{
			request.setAttribute("browseParent", GrouperHelper.group2Map(grouperSession,contextGroup));
		}
		
		if (forStems) {
			return mapping.findForward(FORWARD_StemMember);
		}

		return mapping.findForward(FORWARD_GroupMember);
	}

}
