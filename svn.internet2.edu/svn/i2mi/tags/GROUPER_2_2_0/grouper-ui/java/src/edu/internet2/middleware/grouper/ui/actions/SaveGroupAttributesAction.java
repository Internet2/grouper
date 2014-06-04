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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.ui.Message;

/**
 * Saves custom attributes.  
 * <p/>
 * <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we are saving 
      attributes for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">attr.&lt;field_name&gt;</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Input value for field</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.save</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that once saved should 
      go to GroupSummary - rather than add new members</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">key=groups.action.saved-attr</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">findForNode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Use if groupId not set</font></td>
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
 * 
 * @author Gary Brown.
 * @version $Id: SaveGroupAttributesAction.java,v 1.15 2009-08-12 04:52:14 mchyzer Exp $
 */
public class SaveGroupAttributesAction extends GrouperCapableAction {


  //------------------------------------------------------------ Local Forwards
  static final private String FORWARD_GroupMembers = "GroupMembers";
  static final private String FORWARD_FindNewMembers = "FindNewMembers";
  static final private String FORWARD_GroupSummary = "GroupSummary";
  static final private String FORWARD_EditAttributes = "EditAttributes";

  //------------------------------------------------------------ Action Methods

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,HttpSession session,
	  GrouperSession grouperSession)
      throws Exception {
      	DynaActionForm groupFormBean = (DynaActionForm) form;
      	String groupId = (String)groupFormBean.get("groupId");
      	Group group = GroupFinder.findByUuid(grouperSession,groupId, true);
      	List<AttributeDefName> allAttributes = new ArrayList<AttributeDefName>();
      	Set types = group.getTypes();
      	GroupType type;
      	Iterator it = types.iterator();
      	while(it.hasNext()) {
      		type = (GroupType)it.next();
      		Set<AttributeDefName> fs = type.getLegacyAttributes();
      		allAttributes.addAll(fs);
      	}
      	String attr;
      	String groupAttr;
      	for(int i=0;i<allAttributes.size();i++) {
      		AttributeDefName attribute = allAttributes.get(i);
      		attr = request.getParameter("attr." + attribute.getName());
      		groupAttr = group.getAttributeValue(attribute.getLegacyAttributeName(true), false, false);
      		if(attr!=null && !attr.equals(groupAttr)) {
      			if("".equals(attr)) {
      				try {
      					group.deleteAttribute(attribute.getLegacyAttributeName(true));
      				}catch(GroupModifyException e) {
      					Map fieldMap = (Map)GrouperHelper.getFieldsAsMap().get(attribute.getLegacyAttributeName(true));
      					Message message = new Message("error.group.save-attributes.delete",(String)fieldMap.get("displayName"),true);
      					request.setAttribute("message",message);
      					return mapping.findForward(FORWARD_EditAttributes);
      				}
      			}
      			else group.setAttribute(attribute.getLegacyAttributeName(true), attr);
      		}
      	}
      		
      	
      	
      	
		request.setAttribute("message",new Message("groups.action.saved-attr",false));

		String submit = request.getParameter("submit.save");
		
			if(submit!=null) {
				return mapping.findForward(FORWARD_GroupSummary);
			}
			session.setAttribute("findForNode",request.getParameter("groupId"));
   return mapping.findForward(FORWARD_FindNewMembers);

    
  }

}
