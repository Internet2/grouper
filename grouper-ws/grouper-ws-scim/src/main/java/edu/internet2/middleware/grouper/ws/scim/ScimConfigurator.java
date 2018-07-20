package edu.internet2.middleware.grouper.ws.scim;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import edu.internet2.middleware.grouper.ws.scim.group.TierGroupService;
import edu.internet2.middleware.grouper.ws.scim.membership.MembershipResource;
import edu.internet2.middleware.grouper.ws.scim.membership.TierMembershipService;
import edu.internet2.middleware.grouper.ws.scim.user.TierUserService;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

@WebListener
public class ScimConfigurator implements ServletContextListener {

  public static final Logger LOG = LoggerFactory.getLogger(ScimConfigurator.class);

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject
  private Instance<TierGroupService> groupProviderInstance;
  
  @Inject
  private Instance<TierUserService> userProviderInstance;
  
  @Inject
  private Instance<TierMembershipService> membershipProviderInstance;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      Json.mapper().registerModule(new JaxbAnnotationModule());
      Json.mapper().registerModule(new JavaTimeModule());
      Json.mapper().registerModule(new Jdk8Module());
      Json.mapper().findAndRegisterModules();
      
      Yaml.mapper().registerModule(new JaxbAnnotationModule());
      Yaml.mapper().registerModule(new JavaTimeModule());
      Yaml.mapper().registerModule(new Jdk8Module());

      providerRegistry.registerProvider(ScimGroup.class, groupProviderInstance);
      providerRegistry.registerProvider(ScimUser.class, userProviderInstance);
      providerRegistry.registerProvider(MembershipResource.class, membershipProviderInstance);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }

}
