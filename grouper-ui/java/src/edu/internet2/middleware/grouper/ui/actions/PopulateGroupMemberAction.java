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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperMember;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;

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
    <td><font face="Arial, Helvetica, sans-serif">possiblePrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">All privileges (Naming or Access) 
      which can be assigned</font></td>
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
 * @version $Id: PopulateGroupMemberAction.java,v 1.3 2005-11-22 10:33:32 isgwb Exp $
 */
public class PopulateGroupMemberAction extends GrouperCapableAction {

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
		
		//We are using same class for stems and groups, but action is different
		//we distinguish by checking parameter
		
		
		boolean forStems = "stems".equals(mapping.getParameter());
		DynaActionForm groupOrStemMemberForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupOrStemMemberForm,"findForNode");
		String asMemberOf = (String) groupOrStemMemberForm.get("asMemberOf");
		String contextGroupId = (String) groupOrStemMemberForm.get("contextGroup");
		Group contextGroup=null;
		if(!isEmpty(contextGroupId)) {
			if(forStems) {
				contextGroup = GrouperStem.loadByID(grouperSession,contextGroupId);
			}else{
				contextGroup = GrouperGroup.loadByID(grouperSession,contextGroupId);
			}
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
		GrouperGroup group = null;
		GrouperStem stem = null;
		Group groupOrStem = null;
		Map groupOrStemMap = null;
		//In UI we need to display set of possible privileges - with effective ones
		//pre-selected
		String[] possiblePrivs = null;
		
		if(forStems) {
			stem= (GrouperStem)GrouperStem.loadByID(grouperSession,asMemberOf);
			groupOrStem = stem;
			groupOrStemMap=GrouperHelper.stem2Map(grouperSession,stem);
			session.setAttribute("subtitle","stems.action.edit-member");
			possiblePrivs = GrouperHelper.getStemPrivs(grouperSession);
		}else{
			group=(GrouperGroup)GrouperGroup.loadByID(grouperSession,
					asMemberOf);
			groupOrStem = group;
			groupOrStemMap=GrouperHelper.group2Map(grouperSession,group);
			session.setAttribute("subtitle","groups.action.edit-member");
			possiblePrivs = GrouperHelper.getGroupPrivs(grouperSession);
		}
		
		
		request.setAttribute("subtitleArgs", new Object[] { groupOrStemMap.get("displayExtension")});

		
		String subjectId = (String) groupOrStemMemberForm.get("subjectId");
		String subjectType = (String) groupOrStemMemberForm.get("subjectType");
		
		//Retrieve subject whose privileges we are displaying and make
		//available to view
		Map subjectMap = GrouperHelper.subject2Map(grouperSession, subjectId,
				subjectType);
		
		//Retrieve privileges current user, and selected subject have over
		//current group/stem
		GrouperMember member = GrouperMember.load(grouperSession,subjectId, subjectType);
		Map authUserPrivs = GrouperHelper.hasAsMap(grouperSession, groupOrStem,false);
		Map privs = GrouperHelper.hasAsMap(grouperSession, groupOrStem, member);
		Map extendedPrivs = GrouperHelper.getExtendedHas(grouperSession,groupOrStem,member);
		Map immediatePrivs = GrouperHelper.getImmediateHas(grouperSession,groupOrStem,member);
		
		
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