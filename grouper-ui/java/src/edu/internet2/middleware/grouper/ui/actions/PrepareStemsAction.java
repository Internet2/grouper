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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import edu.internet2.middleware.grouper.GrouperSession;




/**
 * Low level Strut's action which controls browsing. Determines what groups / stems 
 * to display according to browse mode. Much code here should be factored out so 
 * that new modes can be easily added. 
 * 
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">start</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">pageSize</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used by CollectionPager. If 
      not set, get from HttpSession</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">currentNode</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Group id or stem id of position 
      to show in hierarchy</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">resetBrowse</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates if flat mode should 
      be cancelled</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">hideQuickLinks</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates if quick links should 
      be hidden</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">flat</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates hierarchy should be 
      hidden </font></td>
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
    <td><font face="Arial, Helvetica, sans-serif">initialStems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of 'quick links'</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicate if we are in flatMode 
      - if so don't show hierarchy</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browsePrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map of privileges the authenticated 
      user has for current group or stem</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CollectionPager</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">allStemPrivs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of Naming privileges</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">currentLocation</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map wrapping current stem or 
      group </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemHasChildren=true/false</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If current node is a stem indicates 
      if there are children - if there are children cannot be deleted</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">subtitle=groups.action.show-summary</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">group</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Map wrapping current group</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Remove if resetBrowse=Y</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Determine if we are browsing 
      to find members / privilegees for a group or stem</font></td>
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
 * @version $Id: PrepareStemsAction.java,v 1.4 2005-12-08 15:30:52 isgwb Exp $
 */

public class PrepareStemsAction extends LowLevelGrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_JoinGroups = "JoinGroups";

	static final private String FORWARD_ManageGroups = "ManageGroups";

	static final private String FORWARD_CreateGroups = "CreateGroups";

	static final private String FORWARD_MyGroups = "MyGroups";

	static final private String FORWARD_FindNewMembers = "FindNewMembers";

	static final private String FORWARD_FindNewMembersForStems = "FindNewMembersForStems";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		/*DynaActionForm browseForm = (DynaActionForm) form;
		String browseMode = getBrowseMode(session);
		GrouperGroup curNodeGroup = null;
		GrouperStem curNodeStem = null;
		Group curGroupOrStem =null;
		Map curGroupOrStemMap = null;
		boolean currentNodeIsGroup = false;
		int resultSize = 0;
		boolean isFlat = processFlatForMode(browseMode, request, session);
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		if (!isEmpty(browseForm.get("currentNode")))
			setBrowseNode((String)browseForm.get("currentNode"),session);
		
		GrouperAccess accessImpl = grouperSession.access();
		//Determine the current node
		//TODO: differentiate between stem and group ids
		String currentNodeId = (String)browseForm.get("currentNode");
		if ("Y".equals(browseForm.get("resetBrowse"))) {
			session.removeAttribute("browseNodeId" + browseMode);
			if (isFlat) {
				session.removeAttribute("isFlat" + browseMode);
				isFlat = false;
			}
		}
		if (!isEmpty(currentNodeId)) {
			isFlat = false;
		}else{
			currentNodeId = getBrowseNode(session);
		}
		//GrouperGroup curNode = null;

		ResourceBundle mediaResources = getMediaResources(session);
		String pluginInitialStems = null;
		try {
			pluginInitialStems = mediaResources
					.getString("plugin.initialstems");
		} catch (Exception e) {
			//TODO: something
		}
		String defaultStem = getDefaultRootStemName(session); 
		Map parent = null;
		if (isFlat || ((isEmpty(currentNodeId)||"ROOT".equals(currentNodeId)) && pluginInitialStems == null)
				|| ("All".equals(browseMode) && isEmpty(currentNodeId ))
				|| !isEmpty(browseForm.get("hideQuickLinks"))
				|| "false".equals(browseForm.get("flat"))) {
			//Don't have a node or are not in browse mode
			//so use default
			curNodeStem = GrouperStem.loadByName(grouperSession, defaultStem);
			if(curNodeStem!=null && !"ROOT".equals(currentNodeId)) {
				currentNodeId = curNodeStem.id();
				parent = GrouperHelper.stem2Map(grouperSession, curNodeStem);
				curGroupOrStem=curNodeStem;
				curGroupOrStemMap = GrouperHelper.stem2Map(grouperSession,curNodeStem);	
			}else{
				parent = new HashMap();
				curGroupOrStemMap = new HashMap();
				currentNodeId=Grouper.NS_ROOT;
				defaultStem=Grouper.NS_ROOT;
				curNodeStem=null;
			}

			
		} else if (isEmpty(currentNodeId)) {
			//Quicklinks
			InitialStems initialStems = (InitialStems) Class.forName(
					pluginInitialStems).newInstance();
			List stems = initialStems.getInitialStems(grouperSession);
			List stemMaps = GrouperHelper.stems2Maps(grouperSession, stems);
			request.setAttribute("initialStems", stemMaps);
			request.setAttribute("browsePath", stemMaps);
			return null;

		} else {
			//we have a node
			
			try {
				curNodeGroup = (GrouperGroup)GrouperGroup.loadByID(grouperSession, currentNodeId);
				currentNodeIsGroup=true;
				parent = GrouperHelper.group2Map(grouperSession, curNodeGroup);
				curGroupOrStem = curNodeGroup;
				curGroupOrStemMap = GrouperHelper.group2Map(grouperSession,curNodeGroup);
			}catch(Exception e) {
				curNodeStem = (GrouperStem)GrouperStem.loadByID(grouperSession, currentNodeId);
				parent = GrouperHelper.stem2Map(grouperSession, curNodeStem);
				curGroupOrStem=curNodeStem;
				curGroupOrStemMap = GrouperHelper.stem2Map(grouperSession,curNodeStem);
			}	
		}

		 
		List children = new ArrayList();
		List allChildren = new ArrayList();
		Map child;
		String name;

		if (isEmpty(parent.get("isGroup")) && isFlat) {
			//We don't have a group and we are in flat mode
			//so we are going to page groups
			//TODO: allow filtering so only happens from a specified stem?
			Set childrenSet = null;
			StringBuffer totalCount = new StringBuffer();
			if ("".equals(browseMode)) {
				childrenSet = GrouperHelper.getMembershipsSet(grouperSession,
						start, pageSize, totalCount);
			} else if ("Create".equals(browseMode)) {
				childrenSet = GrouperHelper.getStemsForPrivileges(
						grouperSession, new String[] { Grouper.PRIV_CREATE,
								Grouper.PRIV_STEM }, start, pageSize,
						totalCount);
			} else if ("Manage".equals(browseMode)) {
				childrenSet = GrouperHelper.getGroupsForPrivileges(
						grouperSession, new String[] { Grouper.PRIV_ADMIN,
								Grouper.PRIV_UPDATE, Grouper.PRIV_READ },
						start, pageSize, totalCount);
			} else if ("Join".equals(browseMode)) {
				childrenSet = GrouperHelper.getGroupsForPrivileges(
						grouperSession, new String[] { Grouper.PRIV_OPTIN },
						start, pageSize, totalCount);
			}
			resultSize = Integer.parseInt(totalCount.toString());
			List tmpList = new ArrayList(childrenSet);
			int end = start + pageSize;
			if (end > resultSize)
			end = resultSize;
			children = GrouperHelper.groups2Maps(grouperSession, tmpList);
			GrouperGroup group;

		} else {
			//OK it isn't flat mode - or we actually have a group
			
			//Determine if we are browsing to find members/privilegees
			//for a group or stem
			String findForNode = (String) session.getAttribute("findForNode");
			if (!isEmpty(parent.get("isGroup"))) {

				if (findForNode !=null && !curNodeGroup.id().equals(findForNode)) {
					//We've got a group and it isn't one we are finding
					//members or privilegees for, so we will list the membership
					//TODO: do we have to do effective / all as well
					allChildren = curNodeGroup.listImmVals();
					resultSize = allChildren.size();
					allChildren = GrouperHelper.groupList2SubjectsMaps(
							grouperSession, allChildren, start, pageSize);
				}
			} else {
				//we have a stem so we will page the children (stems / groups)
				if(curNodeStem!=null) {
					allChildren = GrouperHelper.getChildrenAsMaps(grouperSession, curNodeStem.name());
				}
				else allChildren = GrouperHelper.getChildrenAsMaps(grouperSession,defaultStem);
				request.setAttribute("stemHasChildren",new Boolean(allChildren.size()>0));
			}
			if (GrouperHelper.isSuperUser(grouperSession)
					|| findForNode != null) {
				//We aren't going to filter according to the mode we arein
				//since we are browsing to add members / privilegees
				children = allChildren;
				//resultSize = children.size();
			} else {
				Map validStems = null;
				//We want to restrict dead ends
				validStems = GrouperHelper.getValidStems(grouperSession,browseMode);
				
				boolean addChild = false;
				int end = start + pageSize;
				//TODO: ideally these would be pluggable to some extent so different modes
				//could easily be added
				for (int i = 0; i < allChildren.size(); i++) {
					addChild = false;

					child = (Map) allChildren.get(i);
					name = (String) child.get("name");
					if (validStems.get(name) != null
							&& !Boolean.TRUE.equals(child.get("isGroup"))) {
						addChild = true;
					} else {
						GrouperGroup childGroup = null;
						GrouperStem childStem = null;
						try {
							childGroup = (GrouperGroup)GrouperGroup.loadByID(
								grouperSession, (String) child.get("id"));
						}catch(Exception e) {
							childStem = (GrouperStem)GrouperStem.loadByID(
									grouperSession, (String) child.get("id"));
						}
						
						if ("".equals(browseMode) && childGroup!=null 
								&& childGroup.hasMember(GrouperMember.load(grouperSession,grouperSession.subject().getId(),grouperSession.subject().getType().getName()))) {
							addChild = true;
						} else if ("Create".equals(browseMode) && childGroup!=null
								&& (accessImpl.has(grouperSession, childGroup,
										Grouper.PRIV_ADMIN) ||
										accessImpl.has(grouperSession, childGroup,
												Grouper.PRIV_UPDATE)||
												accessImpl.has(grouperSession, childGroup,
														Grouper.PRIV_READ))) {
							addChild = true;
						} else if ("Manage".equals(browseMode)
								&& validStems.containsKey(name)) {
							addChild = true;
						} else if ("Join".equals(browseMode)
								&& validStems.containsKey(name)) {
							addChild = true;
						} else if ("All".equals(browseMode)) {
							String personalStem = null;
							try {
								getMediaResources(session).getString("personal.browse.stem");
							}catch(Exception e){}

							if ((personalStem==null || !name.startsWith(personalStem))&&!GrouperHelper.isSuperUser(grouperSession))
								addChild = true;
						}
					}
					if (addChild) {
						resultSize++;
						if (resultSize >= start && resultSize < end)
							children.add(child);

					}
				}
			}
		}
		//Skip empty stems
		//should only skip where no stem / create privilege
		if(children.size()==1) {
			Map m = (Map)children.get(0);
			String stemId = (String)m.get("stemId");
			if(!isEmpty(stemId)) {
				GrouperStem wStem = (GrouperStem)m.get("wrappedObject");
				GrouperNaming namingImpl = grouperSession.naming();
				if(!namingImpl.has(grouperSession,wStem,Grouper.PRIV_STEM)
						&&!namingImpl.has(grouperSession,wStem,Grouper.PRIV_CREATE)) {
					((DynaActionForm)form).set("currentNode",stemId);
					return grouperExecute(mapping,form,request,response,session,grouperSession);
				}
			}
		}		
		//Determine hierarchy to current node
		List path = GrouperHelper.parentStemsAsMaps(grouperSession, curGroupOrStem);
		Map privs = null;
		//Determine privileges 
		if ("All".equals(browseMode)) {
			privs = GrouperHelper.hasAsMap(grouperSession, curGroupOrStem, false);
		} else {
			privs = GrouperHelper.hasAsMap(grouperSession, curGroupOrStem, true);
		}
		request.setAttribute("browsePrivs", privs);
		request.setAttribute("browseParent", parent);
	

		browseForm.set("pageSize", "" + pageSize);
		//Set up CollectionPager for view
		if(resultSize<children.size()) resultSize=children.size();
		CollectionPager pager = new CollectionPager(children, resultSize, null,
				start, null, pageSize);
		if (!isFlat)
			pager.setParam("currentNode", currentNodeId);
		if (session.getAttribute("findForNode") != null) {
			pager.setTarget("/browseStemsFind");
		} else {
			pager.setTarget("/browseStems");
		}
		request.setAttribute("pager", pager);
		request.setAttribute("browsePath", path);

		request.setAttribute("allStemPrivs", GrouperHelper
				.getStemPrivs(grouperSession));
		request.setAttribute("currentLocation", curGroupOrStemMap);*/
		return null;
	}
}
