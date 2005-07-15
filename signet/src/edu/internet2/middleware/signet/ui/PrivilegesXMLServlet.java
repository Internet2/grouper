/*--
$Id: PrivilegesXMLServlet.java,v 1.1 2005-07-15 22:31:34 acohen Exp $
$Date: 2005-07-15 22:31:34 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
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
import edu.internet2.middleware.signet.PrivilegesXML;
import edu.internet2.middleware.signet.Signet;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrivilegesXMLServlet implements Servlet {

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
                ("currentGranteePrivilegedSubject"));
        
    PrivilegesXML privilegesXML;
    
    try
    {
      privilegesXML = new PrivilegesXML();
      response.setContentType("text/xml");
      privilegesXML.generateXML
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
