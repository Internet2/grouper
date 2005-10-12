/*--
  $Id: RevokeAction.java,v 1.5 2005-10-12 18:11:16 acohen Exp $
  $Date: 2005-10-12 18:11:16 $
  
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

import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.PrivilegedSubject;
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
public final class RevokeAction extends BaseAction
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
    
    PrivilegedSubject loggedInPrivilegedSubject
      = (PrivilegedSubject)
          (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
        
    // Find the Grantable objects specified by the multi-valued "revoke" parameter,
    // and revoke them.
    String[] assignmentIDs = request.getParameterValues("revoke");
    
    signet.beginTransaction();
    
    for (int i = 0; i < assignmentIDs.length; i++)
    {
      Grantable grantableInstance
      	= Common.getGrantableFromParamStr(signet, assignmentIDs[i]);
      grantableInstance.revoke(loggedInPrivilegedSubject);
      grantableInstance.save();
    }
    
    signet.commit();

    // Forward to our success page
    return findSuccess(mapping);
  }
}