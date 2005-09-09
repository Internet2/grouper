/*--
$Id: ConfirmAction.java,v 1.11 2005-09-09 20:49:46 acohen Exp $
$Date: 2005-09-09 20:49:46 $

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

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
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
  boolean canUse;
  boolean canGrant;
  Date    effectiveDate   = null;
  Date    expirationDate  = null;

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

  try
  {
    effectiveDate
      = Common.getDateParam(request, Constants.EFFECTIVE_DATE_PREFIX, new Date());
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

  if (Common.paramIsPresent(canUseString))
  {
    canUse = true;
  }
  else
  {
    canUse = false;
  }
  
  if (Common.paramIsPresent(canGrantString))
  {
    canGrant = true;
  }
  else
  {
    canGrant = false;
  }
  
  Set limitValues = LimitRenderer.getAllLimitValues(signet, request);
  
  // If we've detected any data-entry errors, let's bail out here, before we
  // try to use that bad data.
  if (actionMessages != null)
  {
    return findDataEntryErrors(mapping);
  }
  
  if (assignment != null)
  {
    // We're editing an existing Assignment.
    assignment.setLimitValues(grantor, limitValues);
    assignment.setCanGrant(grantor, canGrant);
    assignment.setCanUse(grantor, canUse);
    assignment.setEffectiveDate(grantor, effectiveDate);
    assignment.setExpirationDate(grantor, expirationDate);
  }
  else
  {
    // We're creating a new Assignment.
    assignment
      = grantor.grant
          (null,  // actingAs
           grantee,
           scope,
           function,
           limitValues,
           canUse,
           canGrant,
           effectiveDate,
           expirationDate);
  }
  
  // Let's see whether or not the Assignment we want to save has any
  // duplicates. If it does, we'll sidetrack the user with a warning
  // screen.
  
  Set duplicateAssignments = assignment.findDuplicates();
  if (duplicateAssignments.size() > 0)
  {
    session.setAttribute("currentAssignment", assignment);
    session.setAttribute("currentLimitValues", limitValues);
    session.setAttribute("duplicateAssignments", duplicateAssignments);
    return findDuplicateAssignments(mapping);
  }
  
  // If we've gotten this far, there must be no duplicate Assignments.
  // Let's save this Assignment in the database.
  
  signet.beginTransaction();
  assignment.save();
  signet.commit();
  
  session.setAttribute("currentAssignment", assignment);

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