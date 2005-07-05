/*--
  $Id: ConditionsAction.java,v 1.3 2005-07-05 17:03:27 acohen Exp $
  $Date: 2005-07-05 17:03:27 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
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
  // ---------------------------------------------------- Public Methods
  // See superclass for Javadoc
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
        ("currentGranteePrivilegedSubject", assignment.getGrantee());
      session.setAttribute
        ("currentSubsystem", assignment.getFunction().getSubsystem());
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