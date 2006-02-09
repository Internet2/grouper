/*--
$Id: FunctionsAction.java,v 1.7 2006-02-09 10:31:26 lmcrae Exp $
$Date: 2006-02-09 10:31:26 $
  
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
    
    // First, check to see if we have a Signet instance or not. If we don't,
    // we'll need to redirect this session to the Signet start page.
    HttpSession session = request.getSession(); 
    Signet signet = (Signet)(session.getAttribute("signet"));
    
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }
    
    // There are two ways we could have gotten to this action servlet: From the
    // "choose Subsystem" page (in which case we should now have an HTTP
    // parameter which describes the Subsystem of interest), or via some "go
    // back and change your Function selection" link (in which case we should
    // now have a Subsystem stored as an HTTP Session parameter).
    
    Subsystem currentSubsystem
      = Common.getSubsystem
          (signet,
           request,
           "grantableSubsystems", // paramName
           Constants.SUBSYSTEM_ATTRNAME);   // attributeName

    if (currentSubsystem == null)
    {
      // We don't have a Subsystem either via a Session attribute or via an
      // HTTP parameter. Let's send this user back to square one.
      return (mapping.findForward("notInitialized"));
    }
    
    // We're creating a new Assignment, not editing an existing one. Let's
    // make that clear by clearing out any "currentAssignment" attribute from
    // the session.
    session.setAttribute("currentAssignment", null);

    // Forward to our success page
    return findSuccess(mapping);
  }
}
