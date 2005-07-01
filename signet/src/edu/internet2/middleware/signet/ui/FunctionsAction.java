/*--
  $Id: FunctionsAction.java,v 1.3 2005-07-01 23:06:52 acohen Exp $
  $Date: 2005-07-01 23:06:52 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;

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
public final class FunctionsAction extends BaseAction
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
        
    // Find the Subsystem specified by the "select" parameter, and
    // stash it in the Session.
    HttpSession session = request.getSession(); 
    Subsystem currentSubsystem =null;   
    String currentSubsystemId = request.getParameter("select");
    
    Signet signet = (Signet)(session.getAttribute("signet"));
      
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }
      
    currentSubsystem = signet.getSubsystem(currentSubsystemId);
    session.setAttribute("currentSubsystem", currentSubsystem);
    
    // We're creating a new Assignment, not editing an existing one. Let's
    // make that clear by clearing out any "currentAssignment" attribute from
    // the session.
    session.setAttribute("currentAssignment", null);

    // Forward to our success page
    return findSuccess(mapping);
  }
}