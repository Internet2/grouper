/*
 * @author mchyzer
 * $Id: GrouperServiceAxisServlet.java,v 1.3 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis2.transport.http.AxisServlet;

import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ws.security.GrouperWssecAuthentication;
import edu.internet2.middleware.grouper.ws.security.RampartHandlerServer;
import edu.internet2.middleware.grouper.ws.status.GrouperStatusServlet;


/**
 * axis servlet
 */
@SuppressWarnings("serial")
public class GrouperServiceAxisServlet extends AxisServlet {

  static {
    GrouperStatusServlet.registerStartup();
  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
   */
  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException,
      IOException {
    
    GrouperStartup.startup();
    
    GrouperStatusServlet.incrementNumberOfRequest();

    //stash in threadlocal, make sure this is first in this method!
    GrouperServiceJ2ee.assignHttpServlet(this);

    //if rampart, then, then make sure configured
    if (GrouperServiceJ2ee.wssecServlet()) {
      Class<? extends GrouperWssecAuthentication> wssecClass  = RampartHandlerServer.retrieveRampartCallbackClass();
      if (wssecClass == null) {
        //do a 404 if not configured, this is not found!
        HttpServletResponse httpServletResponse = (HttpServletResponse)res;
        httpServletResponse.setStatus(404);
        return;
      }
    }
    
    //else pass to axis
    super.service(req, res);
    
    HttpSession httpSession = ((HttpServletRequest)req).getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
    }
  }

  
  
  
}
