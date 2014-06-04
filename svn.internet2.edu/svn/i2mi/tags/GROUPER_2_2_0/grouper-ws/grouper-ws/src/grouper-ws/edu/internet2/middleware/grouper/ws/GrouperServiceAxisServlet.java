/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

import edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ws.security.GrouperWssecAuthentication;
import edu.internet2.middleware.grouper.ws.security.RampartHandlerServer;


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
