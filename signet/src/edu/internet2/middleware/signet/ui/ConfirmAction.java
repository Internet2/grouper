/*--
  $Id: ConfirmAction.java,v 1.5 2005-07-01 23:06:52 acohen Exp $
  $Date: 2005-07-01 23:06:52 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.TreeNode;

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
public final class ConfirmAction extends BaseAction
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
    boolean grantOnly;
    boolean canGrant;

    PrivilegedSubject grantor
      = (PrivilegedSubject)
          (session.getAttribute("loggedInPrivilegedSubject"));
    PrivilegedSubject grantee
    	= (PrivilegedSubject)
    	    (session.getAttribute("currentGranteePrivilegedSubject"));
    TreeNode scope = (TreeNode)(session.getAttribute("currentScope"));
    Function function = (Function)(session.getAttribute("currentFunction"));
    
    // currentAssignment is present in the session only if we are editing
    // an existing Assignment. Otherwise, we're attempting to create a new one.
    Assignment assignment
      = (Assignment)(session.getAttribute("currentAssignment"));
    
    Signet signet = (Signet)(session.getAttribute("signet"));
    
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }
    
    Common.showHttpParams
    	("ConfirmAction.execute()", signet.getLogger(), request);
    
    String canUseString = request.getParameter("can_use");
    String canGrantString = request.getParameter("can_grant");

    if (paramIsPresent(canUseString))
    {
      grantOnly = false;
    }
    else
    {
      grantOnly = true;
    }
    
    if (paramIsPresent(canGrantString))
    {
      canGrant = true;
    }
    else
    {
      canGrant = false;
    }
    Set limitValues = LimitRenderer.getAllLimitValues(signet, request);
    
    if (assignment != null)
    {
      // We're editing an existing Assignment.
      assignment.setLimitValues(grantor, limitValues);
      assignment.setGrantable(grantor, canGrant);
      assignment.setGrantOnly(grantor, grantOnly);
    }
    else
    {
      // We're creating a new Assignment.
      assignment
        = grantor.grant
            (grantee,
             scope,
             function,
             limitValues,
             canGrant,
             grantOnly,
             new Date(), // effective immediately
             null);      // no expiration date
    }
    
    signet.beginTransaction();
    assignment.save();
    signet.commit();
    
    session.setAttribute("currentAssignment", assignment);

    // Forward to our success page
    return findSuccess(mapping);
  }
  
  private boolean paramIsPresent(String param)
  {
    if ((param != null) && (param != ""))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
}