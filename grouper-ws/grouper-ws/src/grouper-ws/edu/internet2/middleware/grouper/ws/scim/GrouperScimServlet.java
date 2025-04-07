package edu.internet2.middleware.grouper.ws.scim;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
    super.service(req, res);
  }

  
  
}
