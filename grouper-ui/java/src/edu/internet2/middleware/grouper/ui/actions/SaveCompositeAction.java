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



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;

import edu.internet2.middleware.grouper.GrouperSession;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.ui.Message;


/**
 * Top level Strut's action which Saves a CompositeMember for a Group.  
 * <p/>

<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id of the group we are adding 
      a Composite to</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">leftId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id of the left group of the 
      composite </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">rightId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id of the right group of the 
      composite </font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">compositeType</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The type of composite i.e. union,intersection,complement</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><p><font face="Arial, Helvetica, sans-serif">message</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">key in nav ResourceBundle = 
      groups.composite.save.duplicate or groups.composite.save.self-reference 
      or groups.composite.save.success</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
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
 * @version $Id: SaveCompositeAction.java,v 1.6 2009-03-23 17:40:13 mchyzer Exp $
 */
public class SaveCompositeAction extends GrouperCapableAction {

	
	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		DynaActionForm compositeForm = (DynaActionForm) form;
		String groupId = compositeForm.getString("groupId");
		String leftId = compositeForm.getString("leftGroup");
		String rightId = compositeForm.getString("rightGroup");
		String type = compositeForm.getString("compositeType");
		if(leftId.equals(rightId)) {
			request.setAttribute("message", new Message(
					"groups.composite.save.duplicate",true));
			return mapping.findForward("self");
		}
		if(leftId.equals(groupId)|| rightId.equals(groupId)) {
			request.setAttribute("message", new Message(
					"groups.composite.save.self-reference",true));
			return mapping.findForward("self");
		}
		Group owner = GroupFinder.findByUuid(grouperSession,groupId, true);
		Group leftGroup = GroupFinder.findByUuid(grouperSession,leftId, true);
		Group rightGroup = GroupFinder.findByUuid(grouperSession,rightId, true);
		CompositeType cType = CompositeType.valueOfIgnoreCase(type);
		owner.addCompositeMember(cType,leftGroup,rightGroup);
		request.setAttribute("message", new Message(
						"groups.composite.save.success"));
		return mapping.findForward("GroupMembers");
	}
}
