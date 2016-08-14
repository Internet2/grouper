package edu.internet2.middleware.grouper.ws.scim;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.ws.scim.group.TierGroupService;
import edu.internet2.middleware.grouper.ws.scim.user.TierUserService;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;

@WebListener
public class ScimConfigurator implements ServletContextListener {

  public static final Logger LOG = LoggerFactory.getLogger(ScimConfigurator.class);

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject
  private Instance<TierGroupService> groupProviderInstance;
  
  @Inject
  private Instance<TierUserService> userProviderInstance;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      providerRegistry.registerProvider(ScimGroup.class, groupProviderInstance);
      providerRegistry.registerProvider(ScimUser.class, userProviderInstance);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }

}
