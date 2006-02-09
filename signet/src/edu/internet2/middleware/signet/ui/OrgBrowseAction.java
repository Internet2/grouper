/*--
$Id: OrgBrowseAction.java,v 1.6 2006-02-09 10:32:14 lmcrae Exp $
$Date: 2006-02-09 10:32:14 $

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
      
  // Find the Function specified by the "functionSelectList" parameter, and
  // stash it in the Session.
  HttpSession session = request.getSession(); 
  Function currentFunction = null;
  Category currentCategory = null;
  String currentFunctionId = request.getParameter("functionSelectList");

  Signet signet = (Signet)(session.getAttribute("signet"));
  Subsystem currentSubsystem
  	= (Subsystem)(session.getAttribute(Constants.SUBSYSTEM_ATTRNAME));
    
  if ((signet == null) || (currentSubsystem == null))
  {
    return (mapping.findForward("notInitialized"));
  }
  currentFunction = Common.getFunction(currentSubsystem, currentFunctionId);
  currentCategory = currentFunction.getCategory();

  session.setAttribute("currentFunction", currentFunction);
  session.setAttribute("currentCategory", currentCategory);

  // Forward to our success page
  return findSuccess(mapping);
}
}
