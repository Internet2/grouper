/*--
$Id: DemoLoginCheckAction.java,v 1.3 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $
  
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;

/**
 * Signet demo-login-check action - this action exists only for Signet demo
 * installations. In the case of a normal production system, user
 * authentication would occur before the "Start" action is accessed.
 */
public final class DemoLoginCheckAction extends BaseAction
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
   *   Name: "username"
   *   Use:  A username typed in by a user, via a login screen.
   *   
   *   Name: "password"
   *   Use:  A password typed in by a user, via a login screen.
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
    
    String username
      = request.getParameter(Constants.DEMO_USERNAME_HTTPPARAMNAME);
    String password
      = request.getParameter(Constants.DEMO_PASSWORD_HTTPPARAMNAME);
    
    // This simple demo-authentication system uses one password for all
    // users. If it's not right, there's no need to go further.
    if (!(Constants.DEMO_PASSWORD.equals(password)))
    {
      return (mapping.findForward("failure"));
    }
    
    // Let's look up the username. If it exists, we've got our user.
    
    Set userMatches = signet.getSubjectSources().getPrivilegedSubjectsByDisplayId(
    		Signet.DEFAULT_SUBJECT_TYPE_ID, username);
    
    if (userMatches.size() != 1)
    {
        messages.add
      ("Found " 
          + userMatches.size()
          + " matches for logged-in user with display-ID '"
          + request.getRemoteUser()
          + "'. We don't know what to do with any number other than one.");
      messages.add("All matches:");
      Iterator userMatchesIterator = userMatches.iterator();
      while (userMatchesIterator.hasNext())
      {
        messages.add
        (((PrivilegedSubject)(userMatchesIterator.next())).toString());
      }
      request.setAttribute(Constants.ERROR_KEY, messages);
      
      return findFailure(mapping);
    }
    
    PrivilegedSubject loggedInUser = null;
    Iterator pSubjectsIterator = userMatches.iterator();
    while (pSubjectsIterator.hasNext())
    {
      loggedInUser = (PrivilegedSubject)(pSubjectsIterator.next());
    }
    
    session.setAttribute(Constants.LOGGEDINUSER_ATTRNAME, loggedInUser);
    
    // Forward to our success page, which is the Signet main page.
    return findSuccess(mapping);
  }
}

