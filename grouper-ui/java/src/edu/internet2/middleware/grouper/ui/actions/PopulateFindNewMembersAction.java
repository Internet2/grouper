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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;

/**
 * Top level Strut's action which does any setup required for browsing / searching 
 * for new members / privilegees. 
 * <p/>
 <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">privilege</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies if and which privilege 
      was in scope prior to current action</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">stemId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present identifies stem we 
      are finding new members / privilegees for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present identifies group 
      we are finding new members / privilegees for</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browsePath</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of stem ancestors as Maps 
      <br>
      for current stem - not including immediate parent</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map for stem of current stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">forStems=true/false</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates to JSP whether we 
      are dealing with a stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchFromArray</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Maps indicating stem 
      ids and labels which will be used to render a select list which allows a 
      user to scope their search</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">personSources</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of Sources which can be 
      searched to find people. If &gt;1 user can select one to search</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">personSourcesSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Size of personSources - allows 
      UI to determine if it should display select for element</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitleArgs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Provides context for UI</font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.find-new-members 
      or stems.action.find-new-members</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">READ if present (used if no 
      stem or group id), otherwise SET</font></td>
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
 * @author Gary Brown.
 * @version $Id: PopulateFindNewMembersAction.java,v 1.1.1.1 2005-08-23 13:04:15 isgwb Exp $
 */
public class PopulateFindNewMembersAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_FindNewMembers = "FindNewMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		session.setAttribute("subtitle","groups.action.find-new-members");
		
		DynaActionForm groupOrStemForm = (DynaActionForm) form;
		GrouperGroup group = null;
		GrouperStem stem = null;
		String param = mapping.getParameter();
		boolean forStem = "stems".equals(param);
		String privilege = request.getParameter("privilege");
		if(!isEmpty(privilege)) session.setAttribute("findForPriv",privilege);
		//Determine which stem/group we are finding members for
		String targetId = null;
		if(forStem) {
			session.setAttribute("subtitle","stems.action.find-new-members");
			targetId=(String)groupOrStemForm.get("stemId");
			request.setAttribute("forStems", Boolean.TRUE);
		}else{
			session.setAttribute("subtitle","groups.action.find-new-members");
			targetId=(String)groupOrStemForm.get("groupId");
			request.setAttribute("forStems", Boolean.FALSE);
		}
		//TODO: What should I do about forStems?
		if(targetId==null) {
			targetId = (String) session.getAttribute("findForNode");
		}else{
			if (session.getAttribute("findForNode") == null)
				session.setAttribute("findForNode", targetId);
		}
		
		
		//We will allow user to search from any stem that is parent 
		//of this stem / group. If target is a stem this can be searched from also
		String[] searchFrom = targetId.split(HIER_DELIM);
		Map[] searchFromArray = new HashMap[searchFrom.length];
		Map tmp;
		Map tmpMap;
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < searchFrom.length - 1; i++) {
			if (i > 0)
				sb.append(HIER_DELIM);
			sb.append(searchFrom[i]);
			stem = GrouperStem.loadByName(grouperSession, sb.toString()
					);
			tmp = GrouperHelper.stem2Map(grouperSession, stem);
			tmpMap = new HashMap();
			//Set keys that Struts understands
			tmpMap.put("id", sb.toString());
			tmpMap.put("label", tmp.get("displayExtension"));
			searchFromArray[i] = tmpMap;
		}
		request.setAttribute("searchFromArray", searchFromArray);
		Map nodeMap = null;
		Group groupOrStem = null;
		if(forStem) {
			stem = (GrouperStem)GrouperStem.loadByID(grouperSession,
					(String)session.getAttribute("findForNode"));
			nodeMap = GrouperHelper.stem2Map(grouperSession,stem);
			groupOrStem = stem;
		}else{
			group = (GrouperGroup)GrouperGroup.loadByID(grouperSession,
					(String)session.getAttribute("findForNode"));
			nodeMap = GrouperHelper.group2Map(grouperSession,group);
			groupOrStem=group;
		}
				
		request.setAttribute("subtitleArgs", new Object[] { nodeMap
				.get("displayExtension") });
		
		//Make path to current stem/group available for navigation
		List path = GrouperHelper.parentStemsAsMaps(grouperSession, groupOrStem);
		request.setAttribute("browsePath", path);
		request.setAttribute("browseParent", nodeMap);
		
		List personSources = GrouperHelper.getPersonSources();
		request.setAttribute("personSources",personSources);
		request.setAttribute("personSourcesSize",new Integer(personSources.size()));
		
		return mapping.findForward(FORWARD_FindNewMembers);

	}

}