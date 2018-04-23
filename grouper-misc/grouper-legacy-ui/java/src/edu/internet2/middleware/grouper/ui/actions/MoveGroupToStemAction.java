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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.ui.Message;

/**
 * @author shilen
 * @version $Id: MoveGroupToStemAction.java,v 1.1 2009-05-08 12:03:37 shilen Exp $
 */
public class MoveGroupToStemAction extends GrouperCapableAction {

  static final private String FORWARD_MoveGroup = "MoveGroupToStem";

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
      HttpSession session, GrouperSession grouperSession)
      throws Exception {
    
    DynaActionForm groupForm = (DynaActionForm) form;
    
    String curNode = (String)groupForm.get("stemId");
    Stem destinationStem = StemFinder.findByUuid(grouperSession, curNode, true);

    // get the options selected by the user for the group move
    String[] selections = request.getParameterValues("selections");
    
    // find the group to move
    String groupSelection = request.getParameter("groupSelection");
    if (groupSelection.equals("other")) {
      groupSelection = request.getParameter("otherGroupSelection");
    }
    
    if (groupSelection == null || groupSelection.equals("")) {
      request.setAttribute("message", new Message(
          "groups.message.error.invalid-group", true));
      return mapping.findForward(FORWARD_MoveGroup);
    }

    Group group = GroupFinder.findByName(grouperSession, groupSelection, false);
    
    if (group == null) {
      request.setAttribute("message", new Message(
          "groups.message.error.invalid-group", true));
      return mapping.findForward(FORWARD_MoveGroup);
    }

    GrouperHelper.moveGroup(group, destinationStem, selections);

    request.setAttribute("message", new Message(
        "groups.message.group-moved", group.getName()));
    
    return new ActionForward("/populate" + getBrowseMode(session) + "Groups.do");

  }

}
