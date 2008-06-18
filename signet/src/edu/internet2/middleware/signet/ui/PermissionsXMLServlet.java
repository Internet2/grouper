/*--
$Id: PermissionsXMLServlet.java,v 1.7 2008-06-18 01:21:39 ddonn Exp $
$Date: 2008-06-18 01:21:39 $

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
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.PermissionXml;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;

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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void service(ServletRequest request, ServletResponse response) throws /* ServletException, */IOException
	{
		HttpSession session = ((HttpServletRequest)request).getSession();

		Signet signet = (Signet)session.getAttribute(Constants.SIGNET_ATTRNAME);
		SignetSubject grantee = (SignetSubject)session.getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME);

		response.setContentType("text/xml");
		ServletOutputStream sos = response.getOutputStream();

		SignetXa signetXmlAdapter = new SignetXa(signet);
		PermissionXml pXml = new PermissionXml(signetXmlAdapter);
		pXml.exportPermission(grantee, Status.ACTIVE, sos);

		sos.flush();
	}

  /*
	 * (non-Javadoc)
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
