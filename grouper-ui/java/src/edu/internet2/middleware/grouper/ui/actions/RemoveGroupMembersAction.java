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

import java.util.HashMap;

import java.util.Iterator;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;

/**
 * Top level Strut's action which removes selected, or all members, depending on user choice.  
 
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
    <td><font face="Arial, Helvetica, sans-serif">Identifies group we want to 
      see members for</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">subjectIds</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Array of selected subject ids 
      to remove as members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.remove.all</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Remove all members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">submit.remove.selected</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Remove selected members</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">listField</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Custom list field we should 
      remove 'members' from</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates success or otherwise 
      of request</font></td>
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
 * 
 * @author Gary Brown.
 * @version $Id: RemoveGroupMembersAction.java,v 1.4 2009-03-15 06:37:51 mchyzer Exp $
 */
public class RemoveGroupMembersAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_GroupMembers = "GroupMembers";
	static final private String FORWARD_GroupSummary = "GroupSummary"; 

	

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		
		DynaActionForm groupForm = (DynaActionForm) form;
		
		//Identify the group whose membership we are showing
		String groupId = (String)groupForm.get("groupId");
		
    if (groupId == null || groupId.length() == 0)
      groupId = (String) session.getAttribute("findForNode");
		
    if(isEmpty(groupId)) {
      String msg = "No stem or group or findForNode available";
      LOG.error(msg);
      throw new UnrecoverableErrorException("error.delete-group.missing-parameter");
    }
		
		String listField = request.getParameter("listField");
		String membershipField = "members";
		
    if(isEmpty(listField)) {
      listField = (String) session.getAttribute("findForListField");
    }
		if(!isEmpty(listField)) membershipField=listField;
		Field mField = FieldFinder.find(membershipField, true);
		
		
	
		//Retrieve the membership according to scope selected by user
		Group group = GroupFinder.findByUuid(grouperSession, groupId, true);
		if(group.canWriteField(mField) && (!group.hasComposite() || !mField.getUuid().equals(Group.getDefaultList().getUuid()))) {
			if(!isEmpty(request.getParameter("submit.remove.all"))) {
				Set members = group.getImmediateMembers(mField);
				Iterator it = members.iterator();
				Member member;
				while(it.hasNext()) {
					member = (Member)it.next();
					group.deleteMember(member.getSubject(),mField);
				}
				request.setAttribute("message", new Message(
						"groups.remove.all.success"));
				
			}else if(!isEmpty(request.getParameter("submit.remove.selected"))){
				Map map = new HashMap();
				String[] ids = request.getParameterValues("subjectIds");
				if(ids==null) {
					request.setAttribute("message", new Message(
							"groups.remove.none-selected",true));
				}else{
					for(int i=0;i<ids.length;i++) {
						map.put(ids[i],Boolean.TRUE);
					}
					Set members = group.getImmediateMembers(mField);
					Iterator it = members.iterator();
					Member member;
					int count=0;
					while(it.hasNext()) {
						member = (Member)it.next();
						if(map.get(member.getSubject().getId())!=null) {
							group.deleteMember(member.getSubject(),mField);
							count++;
						}
					}
					request.setAttribute("message", new Message(
					"groups.remove.selected.success",""+count));
				}
				
				
			}else {
				request.setAttribute("message", new Message(
				"groups.remove.unkown.error",true));
				
			}
			
			
		}else if(group.canWriteField(mField) && group.hasComposite() && mField.getUuid().equals(Group.getDefaultList().getUuid())){
			//Message - not applicable for composites
			request.setAttribute("message", new Message(
			"groups.remove.composite.error",true));
			
		}else {
			//message - no privilege
			request.setAttribute("message", new Message(
					"groups.remove.no-privs.error",true));
			
		}
		
		
		
		
		
		return mapping.findForward(FORWARD_GroupMembers);

	}

}
