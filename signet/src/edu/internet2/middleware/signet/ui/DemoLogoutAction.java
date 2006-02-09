/*--
$Id: DemoLogoutAction.java,v 1.2 2006-02-09 10:31:04 lmcrae Exp $
$Date: 2006-02-09 10:31:04 $
  
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
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;

/**
 * Signet demo-logout action - this action exists only for Signet demo
 * installations. In the case of a normal production system, user
 * authentication would occur before the "Start" action is accessed,
 * and this action would need to be altered in order to perform some
 * installation-specific logout operation..
 */
public final class DemoLogoutAction extends BaseAction
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
   *   none
   * 
   * This method updates the followiing attributes in the Session:
   * 
   *   Name: "loggedInPrivilegedSubject"
   *   Type: PrivilegedSubject
   *   Use:  The PrivilegedSubject specified by the received username and
   *         password.
   */
  
  // ---------------------------------------------------- Public Methods
  // See superclass for Javadoc
  public ActionForward   execute
    (ActionMapping       mapping,
     ActionForm          form,
     HttpServletRequest  request,
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
    
    session.removeAttribute(Constants.LOGGEDINUSER_ATTRNAME);
    session.removeAttribute(Constants.CURRENTPSUBJECT_ATTRNAME);
    session.removeAttribute(Constants.SUBSYSTEM_ATTRNAME);
    session.removeAttribute(Constants.PROXY_ATTRNAME);
    session.removeAttribute(Constants.DUP_PROXIES_ATTRNAME);
    session.removeAttribute(Constants.PRIVDISPLAYTYPE_ATTRNAME);
    session.removeAttribute(Constants.SUBSYSTEM_OWNER_ATTRNAME);
    
    
    // Forward to our success page, which should ultimately lead back so
    // another "login" screen.
    return findSuccess(mapping);
  }
}

