/*--
$Id: RevokeAndGrantProxyAction.java,v 1.7 2006-02-09 10:33:32 lmcrae Exp $
$Date: 2006-02-09 10:33:32 $
  
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;

import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Proxy;
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
public final class RevokeAndGrantProxyAction extends BaseAction
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
    Proxy proxy
    = (Proxy)(session.getAttribute(Constants.PROXY_ATTRNAME));
  
    Signet signet = (Signet)(session.getAttribute("signet"));
  
    if ((signet == null) || (proxy == null))
    {
      return (mapping.findForward("notInitialized"));
    }
    
    PrivilegedSubject loggedInPrivilegedSubject
      = (PrivilegedSubject)
          (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
        
    // Find the Proxies specified by the multi-valued "revoke" parameter,
    // and revoke them.
    String[] proxyIDs = request.getParameterValues("revoke");
    if (proxyIDs == null)
    {
      proxyIDs = new String[0];
    }
    
    signet.beginTransaction();
    
    for (int i = 0; i < proxyIDs.length; i++)
    {
      Proxy proxyToRevoke
      	= (Proxy)(Common.getGrantableFromParamStr(signet, proxyIDs[i]));
      proxyToRevoke.revoke(loggedInPrivilegedSubject);
      proxyToRevoke.save();
    }
    
    // Now, save the not-yet-persisted Proxy.
    
    proxy.save();
    signet.commit();

    // Forward to our success page
    return findSuccess(mapping);
  }
}
