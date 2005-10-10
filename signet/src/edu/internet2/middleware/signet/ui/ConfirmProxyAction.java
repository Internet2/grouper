/*--
$Id: ConfirmProxyAction.java,v 1.8 2005-10-10 02:17:08 acohen Exp $
$Date: 2005-10-10 02:17:08 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
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
    
    PrivilegedSubject currentGrantee
      = Common.getSubjectFromSelectList
          (signet,
           request,
           Constants.SUBJECT_SELECTLIST_ID,
           Constants.CURRENTPSUBJECT_ATTRNAME);
    
    Subsystem subsystem
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

    try
    {
      effectiveDate
        = Common.getDateParam
            (request, Constants.EFFECTIVE_DATE_PREFIX, new Date());
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
    
    // Clear the currentProxy out of the HTTP session. We're done with it.
    session.removeAttribute(Constants.PROXY_ATTRNAME);

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