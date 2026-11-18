package edu.internet2.middleware.grouper.ws.scim;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;


public class GrouperScimServlet extends ServletContainer {

  public GrouperScimServlet() {
    
  }

  public GrouperScimServlet(ResourceConfig resourceConfig) {
    super(resourceConfig);
    
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
      throws ServletException, IOException {
    GrouperStartup.startup();

    GrouperServiceJ2ee.assignHttpServlet(this);

    // SECURITY (issue 34, 2026-11-10): admit the caller the same way every other WS entry point does, so
    // SCIM honors ws.client.user.group.name.  The SCIM providers only call retrieveSubjectLoggedIn, which
    // skips the WS client group.  A null act-as lookup returns the logged in subject after the admission
    // check, and throws for a subject outside the group (or when no one is logged in): answer 403 before
    // any SCIM handler runs
    try {
      GrouperServiceJ2ee.retrieveSubjectActAs(null);
    } catch (RuntimeException re) {
      if (res instanceof HttpServletResponse) {
        ((HttpServletResponse)res).sendError(HttpServletResponse.SC_FORBIDDEN, "User is not authorized");
        return;
      }
      throw re;
    }

    super.service(req, res);
  }

  
  
}
