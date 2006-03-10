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

import java.util.ArrayList;
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
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.InitialStems;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.RepositoryBrowserFactory;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;


/**
 * Low level Strut's action which controls browsing. Determines what groups / stems 
 * to display according to browse mode. This class replaces PrepareStemsAction. It has
 * been refactored to use RepositoryBrowser instances - loaded according to browse mode.
 * This allows sites to add their own browse modes and to change the behaviour of existing modes.
 * <p/>
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
    <td><font face="Arial, Helvetica, sans-serif">repositoryBrowser</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">RepositoryBrowser instance responsible 
      for listing node children according to browse mode</font></td>
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
 * @version $Id: PrepareRepositoryBrowserStemsAction.java,v 1.3.2.1 2006-03-10 10:07:37 isgwb Exp $
 */

public class PrepareRepositoryBrowserStemsAction extends LowLevelGrouperCapableAction {

	

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		DynaActionForm browseForm = (DynaActionForm) form;
		String browseMode = getBrowseMode(session);
		RepositoryBrowser repositoryBrowser = RepositoryBrowserFactory.getInstance(browseMode,grouperSession,getMediaResources(request));
		Group curNodeGroup = null;
		Stem curNodeStem = null;
		GroupOrStem curGroupOrStem =null;
		Map curGroupOrStemMap = null;
		boolean currentNodeIsGroup = false;
		int resultSize = 0;
		boolean isFlat = false;
		String findForNode = (String) session.getAttribute("findForNode");
		
		if(repositoryBrowser.isFlatCapable()) {
			isFlat = processFlatForMode(browseMode, request, session);
		}
		String startStr = request.getParameter("start");
		if (startStr == null || "".equals(startStr))
			startStr = "0";

		int start = Integer.parseInt(startStr);
		int pageSize = getPageSize(session);
		
		//Remember where we are
		if (!isEmpty(browseForm.get("currentNode")))
			setBrowseNode((String)browseForm.get("currentNode"),session);
		
		//Determine the current node
		String currentNodeId = (String)browseForm.get("currentNode");
		
		//Resume normal browsing 
		if ("Y".equals(browseForm.get("resetBrowse"))) {
			session.removeAttribute("browseNodeId" + browseMode);
			if (isFlat) {
				session.removeAttribute("isFlat" + browseMode);
				isFlat = false;
			}
		}
		
		//We have a node so we souldn't be in flat mode
		//TODO check this logic!
		if (!isEmpty(currentNodeId)) {
			isFlat = false;
		}else{
			currentNodeId = getBrowseNode(session);
		}

		//Can have different implementation for different modes
		String pluginInitialStems = repositoryBrowser.getInitialStems();
		
		//Should we be using initial stems/ If so do it and get out of here
		if(!isFlat && isEmpty(currentNodeId) && pluginInitialStems != null) {
			InitialStems initialStems = (InitialStems) Class.forName(
					pluginInitialStems).newInstance();
			List stems = initialStems.getInitialStems(grouperSession);
			List stemMaps = GrouperHelper.stems2Maps(grouperSession, stems);
			request.setAttribute("initialStems", stemMaps);
			request.setAttribute("browsePath", stemMaps);
			return null;
		}
		
		//Different modes can have different root nodes
		String defaultStem = repositoryBrowser.getRootNode();
		if(isEmpty(defaultStem)) defaultStem=getDefaultRootStemName(session);
		
		Map parent = null;
		if(isEmpty(currentNodeId)){ 
			if(GrouperHelper.NS_ROOT.equals(defaultStem)) {
				curNodeStem = StemFinder.findRootStem(grouperSession);
			}else {
				try{
					curNodeStem = StemFinder.findByName(grouperSession, defaultStem);
				}catch(StemNotFoundException e){}
			}
			
			if(curNodeStem!=null && !"ROOT".equals(currentNodeId)) {
				currentNodeId = curNodeStem.getUuid();
			}
		}
		if(isEmpty(currentNodeId)|| "ROOT".equals(currentNodeId)){
				parent = new HashMap();
				curGroupOrStemMap = new HashMap();
				currentNodeId=GrouperHelper.NS_ROOT;
				defaultStem=GrouperHelper.NS_ROOT;
				curNodeStem=null;
		}else{
			//we have a node
			curGroupOrStem = GroupOrStem.findByID(grouperSession,currentNodeId);
			if(curGroupOrStem.isGroup() && findForNode ==null) {
				curGroupOrStem = GroupOrStem.findByStem(grouperSession,curGroupOrStem.getGroup().getParentStem());
			}else if(curGroupOrStem.isGroup()) {
				curNodeGroup = curGroupOrStem.getGroup();
				currentNodeIsGroup=true;
				parent = GrouperHelper.group2Map(grouperSession, curNodeGroup);
				curGroupOrStemMap = GrouperHelper.group2Map(grouperSession,curNodeGroup);
			}
			if(curGroupOrStem.isStem()) {
				curNodeStem = curGroupOrStem.getStem();
				parent = GrouperHelper.stem2Map(grouperSession, curNodeStem);
				curGroupOrStemMap = GrouperHelper.stem2Map(grouperSession,curNodeStem);
			}	
		}

		List children = new ArrayList();
		List allChildren = new ArrayList();
		Map child;
		String name;


			
			
				//we have a stem so we will page the children (stems / groups)
				StringBuffer totalCount = new StringBuffer();
				String fromId = null;
				if(curNodeStem!=null) {
					fromId = curNodeStem.getUuid();
				}else if(curNodeGroup!=null){
					fromId=curNodeGroup.getUuid();
				}else{
					fromId = defaultStem;
				}
				children = new ArrayList(repositoryBrowser.getChildren(fromId,start,pageSize,totalCount,isFlat,findForNode!=null));
				//children = GrouperHelper.groups2Maps(grouperSession, allChildren);
				request.setAttribute("stemHasChildren",new Boolean(allChildren.size()>0));
				resultSize=Integer.parseInt(totalCount.toString());
			
			
			
		//Skip empty stems
		//should only skip where no stem / create privilege
		if(children.size()==1) {
			Map m = (Map)children.get(0);
			String stemId = (String)m.get("stemId");
			if(!isEmpty(stemId)) {
				Stem wStem = (Stem)m.get("wrappedObject");
				
				if(!wStem.hasStem(grouperSession.getSubject())
						&&!wStem.hasCreate(grouperSession.getSubject())) {
					((DynaActionForm)form).set("currentNode",stemId);
					return grouperExecute(mapping,form,request,response,session,grouperSession);
				}
			}
		}		
		//Determine hierarchy to current node
		List path =  repositoryBrowser.getParentStems(curGroupOrStem);
		Map privs = null;
		//Determine privileges 
		if ("All".equals(browseMode)) {
			privs = GrouperHelper.hasAsMap(grouperSession, curGroupOrStem, false);
		} else {
			privs = GrouperHelper.hasAsMap(grouperSession, curGroupOrStem, true);
		}
		request.setAttribute("browsePrivs", privs);
		request.setAttribute("browseParent", parent);
		if(isEmpty(getBrowseNode(session))) setBrowseNode(curNodeStem.getUuid(),session);
	

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
			pager.setTarget("/browseStems" + getBrowseMode(session));
		}
		request.setAttribute("pager", pager);


		request.setAttribute("allStemPrivs", GrouperHelper
				.getStemPrivs(grouperSession));
		request.setAttribute("currentLocation", curGroupOrStemMap);
		request.setAttribute("repositoryBrowser",repositoryBrowser);
		return null;
	}
}