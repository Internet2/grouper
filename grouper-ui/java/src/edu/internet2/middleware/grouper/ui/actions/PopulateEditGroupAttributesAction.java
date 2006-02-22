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
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;


/**
 * Deal with attributes from custom types 
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: PopulateEditGroupAttributesAction.java,v 1.3 2006-02-22 12:49:11 isgwb Exp $
 */
public class PopulateEditGroupAttributesAction extends GrouperCapableAction {


  //------------------------------------------------------------ Local Forwards
  static final private String FORWARD_EditGroupAttributes = "EditGroupAttributes";

  //------------------------------------------------------------ Action Methods

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,HttpSession session,GrouperSession grouperSession)
      throws Exception {
  		DynaActionForm groupForm = (DynaActionForm)form;
  		String groupId = request.getParameter("groupId");
  		if(isEmpty(groupId)) {
  			groupId=(String)request.getAttribute("groupId");
  			groupForm.set("groupId",groupId);
  		}else {
  			groupForm.set("groupId",groupId);
  		}
  		Group group = GroupFinder.findByUuid(grouperSession,groupId);
  		request.setAttribute("group",GrouperHelper.group2Map(grouperSession,group));
  		request.setAttribute("browseParent",GrouperHelper.group2Map(grouperSession,group));
		session.setAttribute("subtitle","groups.action.edit-attr");
    return mapping.findForward(FORWARD_EditGroupAttributes);

    
  }

}