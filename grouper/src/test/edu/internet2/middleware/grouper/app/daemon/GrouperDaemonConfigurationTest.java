package edu.internet2.middleware.grouper.app.daemon;

import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderIncrementalJob;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class GrouperDaemonConfigurationTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonConfigurationTest("testRetrieveAttributes"));
  }
  
  public GrouperDaemonConfigurationTest(String name) {
    super(name);
  }

  public void testRetrieveAttributes() {
    
    setupOtherFindBadMembershipDaemonConfiguration();
    
    GrouperDaemonConfiguration configuration = new GrouperDaemonFindBadMembershipsConfiguration();
    
    Map<String, GrouperDaemonConfigAttribute> attributes = configuration.retrieveAttributes();
    
    assertEquals(GrouperUtil.toStringForLog(attributes), 3, attributes.size());
    
    configuration = new GrouperDaemonOtherJobConfiguration();
    configuration.setConfigId("notExist");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 3, attributes.size());

    configuration = new GrouperDaemonOtherJobConfiguration();
    configuration.setConfigId("xyz");
    attributes = configuration.retrieveAttributes();
    
    assertEquals(GrouperUtil.toStringForLog(attributes), 4, attributes.size());
    
    configuration = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
    configuration.setConfigId("notExist");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 6, attributes.size());
    
    configuration = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
    configuration.setConfigId("abc");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 7, attributes.size());
    
    
  }
  
  
  private static void setupOtherFindBadMembershipDaemonConfiguration() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.findBadMemberships.somethingElse", "whatever");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.class", "a.b.c.MyOtherJob");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.quartzCron", "123");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.somethingElse2", "345");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.class", GrouperLoaderIncrementalJob.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.quartzCron", "qwe");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.databaseName", "ert");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.tableName", "rty");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.fullSyncThreshold", "tyu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.somethingElse3", "vbn");

  }
 
}
