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


import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;


/**
 * Low level Strut's action which acts as a controller to do any necessary setup 
 * group or stem search forms. 
 * 
 * <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchInDisplayNameOrExtension=true 
        or false</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Derived from media ResourceBundle 
      and set on DynaActionForm</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchInNameOrExtension</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Derived from media ResourceBundle 
      and set on DynaActionForm </font></td>
  </tr>
    <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">searchIn</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Derived from media ResourceBundle 
      and set on DynaActionForm - unless in session as searchGroupDefault</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseParent</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Checks if browsePath iis present 
      - if not derives it from current browseNode and SETs it</font></td>
  </tr>
    <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">stemFields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of fields that stems have. Note, Grouper
    does not model Stem fields so Maps are used to emulate Group fields</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">currentLocation</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Checks if browsePath iis present 
      - if not derives it from current browseNode and SETs it</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">fields</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">List of fields (attributes) 
      which can be searched </font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">mediaMap</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">To obtain search defaults from 
      media ResourceBundle</font></td>
  </tr>
    <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchGroupDefault</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">= 'any' or 'name' (if anything). Stored in session
    if selected in a previous search</font></td>
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
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PrepareGroupSearchFormAction.java,v 1.11 2009-08-12 04:52:14 mchyzer Exp $
 */
public class PrepareGroupSearchFormAction extends LowLevelGrouperCapableAction {


	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		DynaActionForm searchForm = (DynaActionForm)form;
		List browsePath = (List) request.getAttribute("browsePath");
		String searchDisplayNameOrExtension = GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("search.default.search-in-display-name-or-extension");
		String searchNameOrExtension = GrouperUiFilter.retrieveSessionMediaNullMapResourceBundle().get("search.default.search-in-name-or-extension");
		String searchInAny=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("search.default.any");
		String searchDefault=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("search.default");
		searchForm.set("searchInDisplayNameOrExtension",searchDisplayNameOrExtension);
		searchForm.set("searchInNameOrExtension",searchNameOrExtension);
		if(session.getAttribute("searchGroupDefault")!=null) {
			searchDefault=(String)session.getAttribute("searchGroupDefault");
		}
		if("true".equals(searchInAny)) {
			searchForm.set("searchIn",searchDefault);
		}
		
		
		request.setAttribute("fields",GrouperHelper.getSearchableFields(GrouperUiFilter.retrieveSessionNavResourceBundle()));
		Set groupTypes=GroupTypeFinder.findAllAssignable();
		List stemFields = GrouperHelper.getSearchableStemFields(GrouperUiFilter.retrieveSessionNavResourceBundle());
		request.setAttribute("stemFields",stemFields);
		request.setAttribute("types",groupTypes);
		request.setAttribute("typesSize",new Integer(groupTypes.size()));
		if(browsePath==null) {
			GroupOrStem curGroupOrStem = getCurrentGroupOrStem(grouperSession,session);
			if(curGroupOrStem!=null) {

				request.setAttribute("browseParent",GrouperHelper.group2Map(grouperSession,curGroupOrStem));
				request.setAttribute("currentLocation",GrouperHelper.group2Map(grouperSession,curGroupOrStem));
			}
		}
		return null;
	}
}
