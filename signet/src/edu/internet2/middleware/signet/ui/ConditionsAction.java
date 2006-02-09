/*--
$Id: ConditionsAction.java,v 1.7 2006-02-09 10:30:10 lmcrae Exp $
$Date: 2006-02-09 10:30:10 $
  
Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet.ui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * <p>
 * Confirm required resources are available. If a resource is missing,
 * forward to "failure". Otherwise, forward to "success", where
 * success is usually the "welcome" page.
 * </p>
 * <p>
 * Since "required resources" includes the application MessageResources
 * the failure page must not use the standard error or message tags.
 * Instead, it display the Strings stored in an ArrayList stored under
 * the request attribute "ERROR".
 * </p>
 *
 */
public final class ConditionsAction extends BaseAction
{

  /**
   * This method expects to find the following attributes in the Session:
   * 
   *   Name: "signet"
   *   Type: Signet
   *   Use:  A handle to the current Signet environment.
   * 
   * This method expects to receive the following HTTP parameters:
   * 
   *   Name: "scope" (optional)
   *   Type: The String representation of a Scope ID.
   *   Use:  If present, this parameter indicates that we are trying to
   *         create a new Assignment.
   * 
   *   Name: "assignmentId" (optional)
   *   Type: The String representation of an Assignment ID.
   *   Use:  Present only if we are trying to edit an existing Assignment.
   * 
   * This method updates the followiing attributes in the Session:
   * 
   *   Name: "currentScope"
   *   Type: TreeNode
   *   Use:  The Scope of the Assignment which is being either created or
   *         edited.
   * 
   *   Name: "currentAssignment" (optional)
   *   Type: Assignment
   *   Use: Is updated only if we are trying to edit an existing Assignment.
   * 
   *   Name: "currentGranteePrivilegedSubject" (optional)
   *   Type: PrivilegedSubject
   *   Use:  Is updated only if we are trying to edit an existing Assignment.
   * 
   *   Name: Constants.SUBSYSTEM_ATTRNAME (optional)
   *   Type: Subsystem
   *   Use:  Is updated only if we are trying to edit an existing Assignment.
   * 
   *   Name: "currentCategory" (optional)
   *   Type: Category
   *   Use:  Is updated only if we are trying to edit an existing Assignment.
   * 
   *   Name: "currentFunction" (optional)
   *   Type: Function
   *   Use:  Is updated only if we are trying to edit an existing Assignment.
   */
  public ActionForward execute
  	(ActionMapping				mapping,
     ActionForm 					form,
     HttpServletRequest 	request,
     HttpServletResponse response)
  throws Exception
  {
    // Setup message array in case there are errors
    ArrayList messages = new ArrayList();

    // Confirm message resources loaded
    MessageResources resources = getResources(request);
    if (resources==null)
    {
      messages.add(Constants.ERROR_MESSAGES_NOT_LOADED);
    }

    // If there were errors, forward to our failure page
    if (messages.size()>0)
    {
      request.setAttribute(Constants.ERROR_KEY,messages);
      return findFailure(mapping);
    }
    
    HttpSession session = request.getSession();
    Signet signet = (Signet)(session.getAttribute("signet"));
    
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }
    
    // If we've received a "scope" parameter, then it means we're attempting
    // to create a new Assignment. If we receive an "assignmentID" parameter
    // instead, then it means we're editing an existing Assignment. In either
    // case, we'll stash the received parameter in the Session.
    
    if (request.getParameter("scope") != null)
    {
      TreeNode currentScope = signet.getTreeNode(request.getParameter("scope"));
      session.setAttribute("currentScope", currentScope);
    }
    else
    {
      Assignment assignment
        = signet.getAssignment
            (Integer.parseInt
              (request.getParameter
                ("assignmentId")));
      session.setAttribute("currentAssignment", assignment);

      session.setAttribute
        (Constants.CURRENTPSUBJECT_ATTRNAME, assignment.getGrantee());
      session.setAttribute
        (Constants.SUBSYSTEM_ATTRNAME, assignment.getFunction().getSubsystem());
      session.setAttribute
        ("currentCategory", assignment.getFunction().getCategory());
      session.setAttribute
        ("currentFunction", assignment.getFunction());
      session.setAttribute
        ("currentScope", assignment.getScope());
    }

    // Forward to our success page
    return findSuccess(mapping);
  }
}
