/*--
  $Id: PersonViewAction.java,v 1.3 2005-04-05 23:11:38 acohen Exp $
  $Date: 2005-04-05 23:11:38 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.ArrayList;
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

import edu.internet2.middleware.signet.Assignment;
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
public final class PersonViewAction extends BaseAction
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
        
    // Find the PrivilegedSubject specified by the "grantee" parameters, and
    // stash it in the Session. If those parameters are not present, then
    // it must already be stashed in the Session by some prior action.
    String granteeSubjectTypeId = request.getParameter("granteeSubjectTypeId");
    String granteeSubjectId = request.getParameter("granteeSubjectId");
    PrivilegedSubject currentGrantee;
    if (granteeSubjectId != null)
    {
      currentGrantee
      	= signet.getPrivilegedSubject(granteeSubjectTypeId, granteeSubjectId);
      session.setAttribute("currentGranteePrivilegedSubject", currentGrantee);
    }
    else
    {
      currentGrantee
      	= (PrivilegedSubject)
      			(session.getAttribute("currentGranteePrivilegedSubject"));
    }
    
    String currentSubsystemId = request.getParameter("subsystemId");
    Subsystem currentSubsystem;
    if (currentSubsystemId == null)
    {
      currentSubsystem
      	= getFirstSubsystemOfReceivedAssignments(currentGrantee);
    }
    else
    {
      currentSubsystem = signet.getSubsystem(currentSubsystemId);
    }
    session.setAttribute("currentSubsystem", currentSubsystem);
    

    // Forward to our success page
    return findSuccess(mapping);
  }

  /**
   * @param currentGrantee
   * @return
   * @throws ObjectNotFoundException
   */
  private Subsystem getFirstSubsystemOfReceivedAssignments
    (PrivilegedSubject grantee) throws ObjectNotFoundException
  {
    Subsystem firstSubsystem = null;
    Set assignmentsReceived = grantee.getAssignmentsReceived(null, null, null);
    Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
    if (assignmentsReceivedIterator.hasNext())
    {
      firstSubsystem
      	= ((Assignment)(assignmentsReceivedIterator.next()))
      			.getFunction().getSubsystem();
    }
    
    return firstSubsystem;
  }
}