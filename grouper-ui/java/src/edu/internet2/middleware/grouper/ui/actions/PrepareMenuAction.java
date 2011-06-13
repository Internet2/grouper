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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.MenuFilter;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.util.DOMHelper;

/**
 * Controller for menu that reads files configured through the media.resources key menu.resource.files.
 * See resources/grouper/menu-items.xml for the xml format.
 * The order of menu items configured through key 'menu.order'
 * menu.cache determines if menu is cached for users - use false for development
 * if changing source xml files
 * 
 * Since 1.2.1 a new mechanism has been put in place to control which users get which menu items. The
 * media.resources key menu.filters defines a space separated list of MenuFilter. Each filter has a chance
 * to veto a menu item. Currently, two MenuFilter implementations are provided:
 * <ul><li>edu.internet2.middleware.grouper.ui.RootMenuFilter</li>
 * <li>edu.internet2.middleware.grouper.ui.GroupMembershipMenuFilter</li></ul>
 * The latter is configured through an XML configuration file - 
@see edu.internet2.middleware.grouper.ui.GroupMembershipMenuFilter
 * 

 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PrepareMenuAction.java,v 1.7 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PrepareMenuAction extends LowLevelGrouperCapableAction {
	protected static final Log LOG = LogFactory.getLog(PrepareMenuAction.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.ui.actions.LowLevelGrouperCapableAction#grouperExecute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession, edu.internet2.middleware.grouper.GrouperSession)
   */
	@Override
  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		ResourceBundle mediaResources = GrouperUiFilter.retrieveSessionMediaResourceBundle();
		String menuFiles = mediaResources.getString("menu.resource.files");
		String menuOrder = mediaResources.getString("menu.order");
		String menuCache = mediaResources.getString("menu.cache");
		if(LOG.isDebugEnabled()) {
			LOG.debug("menu.resource.files=" + menuFiles);
			LOG.debug("menu.order=" + menuOrder);
			LOG.debug("menu.cache=" + menuCache);
		}
		boolean useCache=false;
		if("true".equals(menuCache)) {
			Object cachedMenu = session.getAttribute("cachedMenu");
			if(cachedMenu!=null) {
				request.setAttribute("menuItems",cachedMenu);
				return null;
			}
			useCache=true;
		}
		List<Map<String, String>> menu = new ArrayList<Map<String, String>>();
		Map<String, Map<String, String>> menuItems = new LinkedHashMap<String, Map<String, String>>();
		String[] inputResources = menuFiles.split(" ");
		Document menuDom = null;
		Element itemElement = null;
		Map<String, String> itemMap = null;
		Iterator menuIterator = null;
		Collection menuItemElements = null;
		Attr attribute = null;
		for(int i=0;i<inputResources.length;i++) {
			LOG.debug("Reading menu file: " + inputResources[i]);
			menuDom = DOMHelper.getDomFromResourceOnClassPath(inputResources[i]);
			menuItemElements = DOMHelper.getImmediateElements(menuDom.getDocumentElement(),"item");
			menuIterator= menuItemElements.iterator();
			while(menuIterator.hasNext()) {
				itemElement = (Element)menuIterator.next();
				itemMap = new HashMap<String, String>();
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
		Map<String, String> orderedItem = null;
		for(int i=0;i<order.length;i++) {
			orderedItem = menuItems.get(order[i]);
			if(orderedItem!=null && isValidMenuItem(orderedItem,grouperSession,request)) {
				LOG.debug(order[i] + " added to menu");
				menu.add(orderedItem);
			}
		}
		request.setAttribute("menuItems",menu);
		if(useCache) {
		  session.setAttribute("cachedMenu",menu);
		}
		//store in session whether cached or not
    session.setAttribute("menuMetaBean",new MenuMetaBean(menu));
		return null;
	}
	
	/**
	 * 
	 * @param item
	 * @param grouperSession
	 * @param request
	 * @return if
	 */
	protected boolean isValidMenuItem(Map<String, String> item,GrouperSession grouperSession,HttpServletRequest request) {
		if(item.containsKey("mediaKeyMustBeTrue")) {
			Properties mp = GrouperUiFilter.retrieveMediaProperties();
			String val = mp.getProperty(item.get("mediaKeyMustBeTrue"));
			if(!"true".equals(val)) {
				LOG.debug("Discarding " + item.get("functionalArea") + " since " + item.get("mediaKeyMustBeTrue") + "is not 'true'");
				return false;
			}
		}
		
		Set menuFilters = SessionInitialiser.getMenuFilters(request.getSession());
		if(menuFilters.isEmpty()) return true;
		Iterator it = menuFilters.iterator();
		MenuFilter filter;
		while(it.hasNext()) {
			filter=(MenuFilter)it.next();
			if(!filter.isValid(grouperSession, item, request)) {
				LOG.debug("Discarding " + item.get("functionalArea") + " - rejected by " + filter.getClass().getSimpleName());
				return false;
			}
		}
		//if((isActiveWheelGroupMember(request.getSession())||"GrouperSystem".equals(SessionInitialiser.getGrouperSession(request.getSession()).getSubject().getId())) && "false".equals(item.get("forAdmin"))) return false;
		return true;
	}
}