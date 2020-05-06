package edu.internet2.middleware.grouper.app.daemon;

import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;


public class GrouperDaemonConfigurationTest extends GrouperTest {

  public GrouperDaemonConfigurationTest(String name) {
    super(name);
  }

  public void testRetrieveAttributes() {
    
    setupOtherFindBadMembershipDaemonConfiguration();
    
    GrouperDaemonConfiguration configuration = new GrouperDaemonFindBadMembershipsConfiguration();
    
    Map<String, GrouperDaemonConfigAttribute> attributes = configuration.retrieveAttributes();
    
    assertEquals(2, attributes.size());
    
    configuration = new GrouperDaemonOtherJobConfiguration();
    attributes = configuration.retrieveAttributes();
    
    assertEquals(3, attributes.size());
    
    configuration = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
    attributes = configuration.retrieveAttributes();
    
    assertEquals(3, attributes.size());
    
    
  }
  
  
  private static void setupOtherFindBadMembershipDaemonConfiguration() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.findBadMemberships.class", "edu.internet2.middleware.grouper.misc.FindBadMembershipsDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.findBadMemberships.quartzCron", "0 0 1 * * ?");

  }
 
}
