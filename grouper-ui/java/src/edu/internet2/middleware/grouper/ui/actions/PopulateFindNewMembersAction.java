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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;


/**
 * Top level Strut's action which does any setup required for browsing / searching 
 * for new members / privilegees. 
 * 
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
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Custom list field for which 
      we are finfing 'members'</font></td>
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
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;Allows callerPageId to 
      be added to links/forms so this page can be returned to</font></td>
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
      or groups.action.find-new-list-members or stems.action.find-new-members</font></td>
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
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForListField</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">READ if present, otherwise SET</font></td>
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
 * @version $Id: PopulateFindNewMembersAction.java,v 1.11 2008-07-21 04:43:47 mchyzer Exp $
 */
public class PopulateFindNewMembersAction extends GrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PopulateFindNewMembersAction.class);
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_FindNewMembers = "FindNewMembers";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		NavExceptionHelper neh=getExceptionHelper(session);
		session.setAttribute("subtitle","groups.action.find-new-members");
		
		DynaActionForm groupOrStemForm = (DynaActionForm) form;
		saveAsCallerPage(request,groupOrStemForm,"findForNode");
		Group group = null;
		Stem stem = null;
		String param = mapping.getParameter();
		boolean forStem = "stems".equals(param);
		String privilege = request.getParameter("privilege");
		if(!isEmpty(privilege)) session.setAttribute("findForPriv",privilege);
		//Determine which stem/group we are finding members for
		String targetId = null;
		String listField = (String) groupOrStemForm.get("listField");
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
		if(isEmpty(listField))listField=(String) session.getAttribute("findForListField");
		if(isEmpty(targetId)) {
			targetId = (String) session.getAttribute("findForNode");
			if(isEmpty(targetId)) {
				String msg = neh.missingAlternativeParameters(targetId,"groupId",targetId,"stemId",targetId,"findForNode");
				LOG.error(msg);
				throw new UnrecoverableErrorException("error.populate-find-new-members.missing-parameter");
			
			}
			
		}else{
			if (session.getAttribute("findForNode") == null)
				session.setAttribute("findForNode", targetId);
				if(!isEmpty(listField)) session.setAttribute("findForListField", listField);
		}
		
		
		//We will allow user to search from any stem that is parent 
		//of this stem / group. If target is a stem this can be searched from also
		
		
		Map tmp;
		Map tmpMap;
		StringBuffer sb = new StringBuffer();
		GroupOrStem groupOrStem = null;
		try{
			groupOrStem=GroupOrStem.findByID(grouperSession,(String)session.getAttribute("findForNode"));
		}catch(Exception e) {
			LOG.error("Problem loading " + (String)session.getAttribute("findForNode"),e);
			throw new UnrecoverableErrorException("error.populate-find-new-members.bad-id",e,(String)session.getAttribute("findForNode"));
		}
		RepositoryBrowser repositoryBrowser = getRepositoryBrowser(grouperSession,session);
		List parentStems = null;
		try {
			parentStems=repositoryBrowser.getParentStems(groupOrStem);
		}catch(Exception e) {
				LOG.error("Problem loading parent stems for " + groupOrStem.getId(),e);
				throw new UnrecoverableErrorException("error.populate-find-new-members.bad-parent-stems",e,groupOrStem.getId());
		}	
		Map[] searchFromArray = new HashMap[parentStems.size()];
		Map stemMap = null;
		for (int i = 0; i < parentStems.size(); i++) {
			stemMap = (Map)parentStems.get(i);
			tmpMap = new HashMap();
			//Set keys that Struts understands
			tmpMap.put("id", stemMap.get("name"));
			tmpMap.put("label", stemMap.get("displayExtension"));
			searchFromArray[i] = tmpMap;
		}
		request.setAttribute("searchFromArray", searchFromArray);
		Map nodeMap = null;
		
		if(forStem) {
			stem = groupOrStem.getStem();
			nodeMap = GrouperHelper.stem2Map(grouperSession,stem);
		}else{
			group = groupOrStem.getGroup();
			nodeMap = GrouperHelper.group2Map(grouperSession,group);
			//List listFields = GrouperHelper.getListFieldsForGroup(grouperSession,group);
			//request.setAttribute("listFields",listFields);
			//request.setAttribute("listFieldsSize",new Integer(listFields.size()));
		}
		Object[] subtitleParams = null;
		
		if(isEmpty(listField)) {
			subtitleParams=new Object[] { nodeMap
				.get("displayExtension") };	
		}else{
			subtitleParams=new Object[] { nodeMap
					.get("displayExtension"),listField };
			session.setAttribute("subtitle","groups.action.find-new-list-members");
		}
		
		request.setAttribute("subtitleArgs", subtitleParams);
		
		//Make path to current stem/group available for navigation

		request.setAttribute("browseParent", nodeMap);
		makeSavedSubjectsAvailable(request);
		
		return mapping.findForward(FORWARD_FindNewMembers);

	}

}
