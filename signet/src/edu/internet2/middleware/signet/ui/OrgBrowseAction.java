/*--
$Id: OrgBrowseAction.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

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

import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
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
public final class OrgBrowseAction extends BaseAction
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
      
  // Find the Function specified by the "step3" parameter, and
  // stash it in the Session.
  HttpSession session = request.getSession(); 
  Function currentFunction = null;
  Category currentCategory = null;
  String currentFunctionId = request.getParameter("step3");

  Signet signet = (Signet)(session.getAttribute("signet"));
  Subsystem currentSubsystem
  	= (Subsystem)(session.getAttribute("currentSubsystem"));
    
  if ((signet == null) || (currentSubsystem == null))
  {
    return (mapping.findForward("notInitialized"));
  }
  currentFunction
  	= currentSubsystem.getFunction(currentFunctionId);
  currentCategory = currentFunction.getCategory();

  session.setAttribute("currentFunction", currentFunction);
  session.setAttribute("currentCategory", currentCategory);

  // Forward to our success page
  return findSuccess(mapping);
}
}