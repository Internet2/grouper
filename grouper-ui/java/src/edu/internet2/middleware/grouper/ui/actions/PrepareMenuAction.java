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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.util.DOMHelper;
import edu.internet2.middleware.grouper.GrouperSession;

/**
 * Controller for menu that reads files configured through the media.resources key menu.resource.files.
 * See resources/grouper/menu-items.xml for the xml format.
 * The order of menu items configured through key 'menu.order'
 * menu.cache determines if menu is cached for users - use false for development
 * if changing source xml files
 * 
 * Subclass this action (and override the Struts config definition) to process i.e. 
 * include / exclude items based on context (including user
 * since GrouperSession - and consequently the current subject - is available) 
 * menuItems -> Session attribute -> List of Maps
 * 

 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PrepareMenuAction.java,v 1.3 2006-02-24 13:36:52 isgwb Exp $
 */
public class PrepareMenuAction extends LowLevelGrouperCapableAction {


	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		ResourceBundle mediaResources = getMediaResources(request);
		String menuFiles = mediaResources.getString("menu.resource.files");
		String menuOrder = mediaResources.getString("menu.order");
		String menuCache = mediaResources.getString("menu.cache");
		boolean useCache=false;
		if("true".equals(menuCache)) {
			Object cachedMenu = session.getAttribute("cachedMenu");
			if(cachedMenu!=null) {
				request.setAttribute("menuItems",cachedMenu);
				return null;
			}
			useCache=true;
		}
		List menu = new ArrayList();
		Map menuItems = new HashMap();
		String[] inputResources = menuFiles.split(" ");
		Document menuDom = null;
		Element itemElement = null;
		Map itemMap = null;
		Iterator menuIterator = null;
		Collection menuItemElements = null;
		Attr attribute = null;
		for(int i=0;i<inputResources.length;i++) {
			menuDom = DOMHelper.getDomFromResourceOnClassPath(inputResources[i]);
			menuItemElements = DOMHelper.getImmediateElements(menuDom.getDocumentElement(),"item");
			menuIterator= menuItemElements.iterator();
			while(menuIterator.hasNext()) {
				itemElement = (Element)menuIterator.next();
				itemMap = new HashMap();
				NamedNodeMap attributes = itemElement.getAttributes();
				for(int j=0;j<attributes.getLength();j++) {
					attribute = (Attr)attributes.item(j);
					if(attribute.getName().equals("functionalArea")) {
						menuItems.put(attribute.getValue(),itemMap);
					}
					itemMap.put(attribute.getName(),attribute.getValue());
				}
			}
		}
		String[] order = menuOrder.split(" ");
		Object orderedItem = null;
		for(int i=0;i<order.length;i++) {
			orderedItem = menuItems.get(order[i]);
			if(orderedItem!=null && isValidMenuItem((Map)orderedItem,grouperSession,request)) {
				menu.add(orderedItem);
			}
		}
		request.setAttribute("menuItems",menu);
		if(useCache) session.setAttribute("cachedMenu",menu);
		return null;
	}
	
	protected boolean isValidMenuItem(Map item,GrouperSession grouperSession,HttpServletRequest request) {
		if((isActiveWheelGroupMember(request.getSession())||"GrouperSystem".equals(SessionInitialiser.getGrouperSession(request.getSession()).getSubject().getId())) && "false".equals(item.get("forAdmin"))) return false;
		return true;
	}
}