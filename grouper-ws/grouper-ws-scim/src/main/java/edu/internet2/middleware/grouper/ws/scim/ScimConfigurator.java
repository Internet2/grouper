package edu.internet2.middleware.grouper.ws.scim;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.ws.scim.group.GrouperGroupService;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimGroup;

@WebListener
public class ScimConfigurator implements ServletContextListener {

  public static final Logger LOG = LoggerFactory.getLogger(ScimConfigurator.class);

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject
  private Instance<GrouperGroupService> groupProviderInstance;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      providerRegistry.registerProvider(ScimGroup.class, groupProviderInstance);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }

}
