/*--
$Id: ConfirmProxyAction.java,v 1.14 2006-02-09 10:30:26 lmcrae Exp $
$Date: 2006-02-09 10:30:26 $

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
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
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
public final class ConfirmProxyAction extends BaseAction
{
  // ---------------------------------------------------- Public Methods
  // See superclass for Javadoc
  public ActionForward execute
    (ActionMapping        mapping,
     ActionForm           form,
     HttpServletRequest   request,
     HttpServletResponse response)
  throws Exception
  {
    // Setup message array in case there are errors
    ArrayList messages = new ArrayList();
    ActionMessages actionMessages = null;

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
    Date    effectiveDate   = null;
    Date    expirationDate  = null;
  
    PrivilegedSubject loggedInUser
      = (PrivilegedSubject)
          (session.getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
  
    // currentProxy is present in the session only if we are editing
    // an existing Proxy. Otherwise, we're attempting to create a new one.
    Proxy proxy
      = (Proxy)(session.getAttribute(Constants.PROXY_ATTRNAME));
  
    Signet signet = (Signet)(session.getAttribute("signet"));
  
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }
  
    Common.showHttpParams
      ("ConfirmProxyAction.execute()", signet.getLogger(), request);
    
    PrivilegedSubject currentGrantee = null;
    Subsystem subsystem = null;
    
    if (proxy == null)
    {
      currentGrantee
        = (PrivilegedSubject)
            (session.getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
    
      subsystem
        = Common.getSubsystem
            (signet,
             request,
             Constants.SUBSYSTEM_HTTPPARAMNAME,
             Constants.SUBSYSTEM_ATTRNAME);

      if ((currentGrantee == null) || (subsystem == null))
      {
        // Let's send this user back to square one.
        return (mapping.findForward("notInitialized"));
      }
    }
    
    Date defaultEffectiveDate;
    if ((proxy != null) && (proxy.getStatus().equals(Status.ACTIVE)))
    {
      // You can't change the effective date of an active Proxy.
      defaultEffectiveDate = proxy.getEffectiveDate();
    }
    else
    {
      defaultEffectiveDate = new Date();
    }

    try
    {
      effectiveDate
        = Common.getDateParam
            (request, Constants.EFFECTIVE_DATE_PREFIX, defaultEffectiveDate);
    }
    catch (DataEntryException dee)
    {
      actionMessages
        = addActionMessage
            (request, actionMessages, Constants.EFFECTIVE_DATE_PREFIX);
    }
  
    try
    {
      expirationDate
        = Common.getDateParam(request, Constants.EXPIRATION_DATE_PREFIX);
    }
    catch (DataEntryException dee)
    {
      actionMessages
        = addActionMessage
            (request, actionMessages, Constants.EXPIRATION_DATE_PREFIX);
    }
  
    // If we've detected any data-entry errors, let's bail out here, before we
    // try to use that bad data.
    if (actionMessages != null)
    {
      return findDataEntryErrors(mapping);
    }
  
    if (proxy != null)
    {
      // We're editing an existing Proxy.
      proxy.setEffectiveDate(loggedInUser, effectiveDate);
      proxy.setExpirationDate(loggedInUser, expirationDate);
      proxy.evaluate();
    }
    else
    {
      // We're creating a new Proxy.
      proxy
        = loggedInUser.grantProxy
            (currentGrantee,
             subsystem,
             true,  // canUse
             false, // canExtend
             effectiveDate,
             expirationDate);
    }
    
    session.setAttribute
      (Constants.CURRENTPSUBJECT_ATTRNAME, proxy.getGrantee());
  
    // Let's see whether or not the Proxy we want to save has any
    // duplicates. If it does, we'll sidetrack the user with a warning
    // screen.
  
    Set duplicateProxies = proxy.findDuplicates();
    if (duplicateProxies.size() > 0)
    {
      session.setAttribute(Constants.PROXY_ATTRNAME, proxy);
      session.setAttribute(Constants.DUP_PROXIES_ATTRNAME, duplicateProxies);
      return findDuplicateAssignments(mapping);
    }
  
    // If we've gotten this far, there must be no duplicate Proxies.
    // Let's save this Proxy in the database.
  
    signet.beginTransaction();
    proxy.save();
    signet.commit();
  
    session.setAttribute(Constants.PROXY_ATTRNAME, proxy);

    // Forward to our success page
    return findSuccess(mapping);
  }

  private ActionMessages addActionMessage
    (HttpServletRequest request,
     ActionMessages     msgs,
     String             msgKey)
  {
    if (msgs == null)
    {
      msgs = new ActionMessages();
    }
    
    ActionMessage msg = new ActionMessage("dateformat");
    msgs.add(msgKey, msg);
    saveMessages(request, msgs);
    
    return msgs;
  }
}
