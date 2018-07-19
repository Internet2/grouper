/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class GrouperDeprovisioningLogicTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
//    TestRunner.run(new GrouperDeprovisioningLogicTest("testUpdateDeprovisioningMetadata"));
    new GrouperDeprovisioningLogicTest().testUpdateDeprovisioningMetadata();
  }
  
  /**
   * 
   */
  public GrouperDeprovisioningLogicTest() {
    super();
    
  }

  /**
   * add config stuff
   */
  @Override
  protected void setupConfigs() {
    //  # if deprovisioning should be enabled
    //  deprovisioning.enable = true
    //
    //  # comma separated affiliations for deprovisioning e.g. employee, student, etc
    //  # these need to be alphanumeric suitable for properties keys for further config or for group extensions
    //  deprovisioning.affiliations = 
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student");
  }

  /**
   * @param name
   */
  public GrouperDeprovisioningLogicTest(String name) {
    super(name);
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningLogic#updateDeprovisioningMetadata(edu.internet2.middleware.grouper.Stem)}.
   */
  public void testUpdateDeprovisioningMetadata() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem deprovisioningStem = new StemSave(grouperSession).assignName("deprovisioning").save();
//    
//    for (int i=0;i<10;i++) {
//      System.out.println("i: " + i);
//      for (int j=0;j<10;j++) {
//        
//        System.out.println("j: " + j);
//        Stem stem = new StemSave(grouperSession).assignName("deprovisioning:grandParent_" + i + ": parent_" + j).assignCreateParentStemsIfNotExist(true).save();
//        
//        for (int k=0; k<10; k++) {
//          Group group = new GroupSave(grouperSession).assignName(stem.getName() + ":group_" + k).assignCreateParentStemsIfNotExist(true).save();
//        }
//      }
//    }

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(deprovisioningStem);
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

    grouperDeprovisioningAttributeValue.setDeprovisionString("true");

    grouperDeprovisioningAttributeValue.setAffiliationString("employee");

    grouperDeprovisioningConfiguration.storeConfiguration();

    GrouperDeprovisioningLogic.updateDeprovisioningMetadata(deprovisioningStem);

    //make sure its there
    Group group = GroupFinder.findByName(grouperSession, "deprovisioning:grandParent_0: parent_0:group_0", true);
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
  }

}
