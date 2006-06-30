/*--
$Id: PermissionsXMLServlet.java,v 1.3 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

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

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.PermissionsXML;
import edu.internet2.middleware.signet.Signet;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PermissionsXMLServlet implements Servlet {

  /* (non-Javadoc)
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig arg0) throws ServletException {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see javax.servlet.Servlet#getServletConfig()
   */
  public ServletConfig getServletConfig() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
   */
  public void service(ServletRequest request, ServletResponse response)
      throws ServletException, IOException
  {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    
    Signet signet
    = (Signet)
        (httpRequest.getSession().getAttribute("signet"));
  
    PrivilegedSubject currentGranteePrivilegedSubject
      = (PrivilegedSubject)
          (httpRequest
            .getSession()
              .getAttribute
                (Constants.CURRENTPSUBJECT_ATTRNAME));
        
    PermissionsXML permissionsXML;
    
    try
    {
      permissionsXML = new PermissionsXML(signet);
      response.setContentType("text/xml");
      permissionsXML.generateXML
        (currentGranteePrivilegedSubject, response.getOutputStream());
    }
    catch (Exception e)
    {
      throw new ServletException(e);
    }
  }

  /* (non-Javadoc)
   * @see javax.servlet.Servlet#getServletInfo()
   */
  public String getServletInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see javax.servlet.Servlet#destroy()
   */
  public void destroy() {
    // TODO Auto-generated method stub

  }

}
