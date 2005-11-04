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

import uk.ac.bris.is.xml.DOMHelper;
import edu.internet2.middleware.grouper.GrouperSession;

/**
 * Controller for menu that reads files configured through media.resources:menu.resource.files.
 * Order of menu items configured through key 'menu.order'
 * menu.cache determines if menu is cached for users - use false for development
 * if changing source xml files
 * 
 * Subclass this action to process i.e. include / exclude items based on context (including user) 
 * menuItems -> Session attribute -> List of Maps
 * 

 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PrepareMenuAction.java,v 1.1 2005-11-04 16:50:08 isgwb Exp $
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
			if(orderedItem!=null) {
				menu.add(orderedItem);
			}
		}
		request.setAttribute("menuItems",menu);
		if(useCache) session.setAttribute("cachedMenu",menu);
		return null;
	}
}