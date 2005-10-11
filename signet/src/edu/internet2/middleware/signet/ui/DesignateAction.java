/*--
  $Id: DesignateAction.java,v 1.3 2005-10-11 03:40:00 acohen Exp $
  $Date: 2005-10-11 03:40:00 $
  
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
public final class DesignateAction extends BaseAction
{
  // ---------------------------------------------------- Public Methods
  // See superclass for Javadoc
  public ActionForward execute
  	(ActionMapping				mapping,
     ActionForm 					form,
     HttpServletRequest 	request,
     HttpServletResponse  response)
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
    
    // Wipe out the "currentProxy" attribute if we're explicitly intending to
    // create a new Proxy.
    String newProxyVal = request.getParameter(Constants.NEW_PROXY_HTTPPARAMNAME);
    if ((newProxyVal != null) && (!newProxyVal.equals("")))
    {
      session.removeAttribute(Constants.PROXY_ATTRNAME);
    }
    
    // Set the "currentProxy" attribute if we're editing an existing Proxy.
    String proxyIdStr = request.getParameter(Constants.PROXYID_HTTPPARAMNAME);
    if ((proxyIdStr != null) && (!proxyIdStr.equals("")))
    {
      session.setAttribute
        (Constants.PROXY_ATTRNAME,
         signet.getProxy(Integer.parseInt(proxyIdStr)));
    }

    // Forward to our success page
    return findSuccess(mapping);
  }
}