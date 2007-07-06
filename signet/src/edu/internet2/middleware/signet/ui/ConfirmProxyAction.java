/*--
$Id: ConfirmProxyAction.java,v 1.20 2007-07-06 21:59:20 ddonn Exp $
$Date: 2007-07-06 21:59:20 $

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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

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
  
    Signet signet = (Signet)(session.getAttribute(Constants.SIGNET_ATTRNAME));
  
    if (signet == null)
    {
      return (mapping.findForward("notInitialized"));
    }

    // currentProxy is present in the session only if we are editing
    // an existing Proxy. Otherwise, we're attempting to create a new one.
    Proxy proxy = (Proxy)(session.getAttribute(Constants.PROXY_ATTRNAME));

    SignetSubject grantee = null;
    Subsystem subsystem = null;
    
    if (proxy == null)
    {
      grantee = (SignetSubject)session.getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME);
    
      subsystem = Common.getSubsystem(signet, request,
    		  Constants.SUBSYSTEM_HTTPPARAMNAME, Constants.SUBSYSTEM_ATTRNAME);

      if ((grantee == null) || (subsystem == null))
      {
        // Let's send this user back to square one.
        return (mapping.findForward("notInitialized"));
      }
    }
    
    Date defaultEffectiveDate = Common.getDefaultEffectiveDate(proxy);
    ActionMessages actionMsgs = new ActionMessages();
    Date effectiveDate = Common.getDateParam(request, Constants.EFFECTIVE_DATE_PREFIX,
    		defaultEffectiveDate, actionMsgs);
    Date expirationDate = Common.getDateParam(request, Constants.EXPIRATION_DATE_PREFIX,
    		null, actionMsgs);
	// If we've detected any data-entry errors, bail out now
    if ( !actionMsgs.isEmpty())
    {
    	saveMessages(request, actionMsgs);
		return findDataEntryErrors(mapping);
    }
    actionMsgs = null;

	// note that grantor may have "acting as" set, which would be the actual grantor
    SignetSubject grantor = (SignetSubject)session.getAttribute(Constants.LOGGEDINUSER_ATTRNAME);
  
    if (proxy != null) // We're editing an existing Proxy
    {
      proxy.checkEditAuthority(grantor); // throws on error
      proxy.setEffectiveDate(grantor, effectiveDate, false);
      proxy.setExpirationDate(grantor, expirationDate, false);
      proxy.evaluate();
    }
    else // We're creating a new Proxy
    {
		boolean canUse = true;
		boolean canExtend = false;
		proxy = grantor.grantProxy(grantee, subsystem,
			canUse, canExtend,  effectiveDate, expirationDate);
    }

	session.setAttribute(Constants.CURRENTPSUBJECT_ATTRNAME, proxy.getGrantee());

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
	HibernateDB hibr = null;
	Session hs = null;
	Transaction tx = null;
	try
	{
		hibr = signet.getPersistentDB();
		hs = hibr.openSession();
		tx = hs.beginTransaction();

		hibr.save(hs, proxy.getGrantor());
		hibr.save(hs, proxy.getGrantee());
		hibr.save(hs, proxy.getRevoker());
		hibr.save(hs, proxy.getProxy());
		hibr.save(hs, proxy);

		tx.commit();

		hs.refresh(proxy);
		hs.refresh(proxy.getGrantor());
		if (null != proxy.getProxy())
			hs.refresh(proxy.getProxy());
		if (null != proxy.getRevoker())
			hs.refresh(proxy.getRevoker());
		hs.refresh(grantor);
		hs.refresh(grantee);

		hibr.closeSession(hs);
	}
	catch (Exception e)
	{
		log.error("ConfirmProxyAction.execute: Problem saving Proxy, Id=" + proxy.getId() + ". The exception was: " + e.toString());
		tx.rollback();
		hibr.closeSession(hs);
		return findFailure(mapping);
	}

	session.setAttribute(Constants.PROXY_ATTRNAME, proxy);

	// Forward to our success page
	return findSuccess(mapping);
  }


}
