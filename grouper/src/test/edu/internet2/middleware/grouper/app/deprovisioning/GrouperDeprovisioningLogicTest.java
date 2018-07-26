/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;


/**
 *
 */
public class GrouperDeprovisioningLogicTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningLogicTest("testUpdateDeprovisioningMetadata"));
    //new GrouperDeprovisioningLogicTest().testUpdateDeprovisioningMetadata();
  }
  
  /**
   * 
   */
  public GrouperDeprovisioningLogicTest() {
    super();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student, employee");
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
    
    for (int i=0;i<10;i++) {

      for (int j=0;j<10;j++) {
        
        Stem stem = new StemSave(grouperSession).assignName(deprovisioningStem.getName() + ":grandParent_" + i + ":parent_" + j).assignCreateParentStemsIfNotExist(true).save();
        
        for (int k=0; k<10; k++) {
          Group group = new GroupSave(grouperSession).assignName(stem.getName() + ":group_" + k).assignCreateParentStemsIfNotExist(true).save();
          AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName(stem.getName() + ":attributeDef_" + k).assignCreateParentStemsIfNotExist(true).save();
        }
      }
    }

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(deprovisioningStem);
    
    grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

    grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

    grouperDeprovisioningAttributeValue.setDeprovisionString("true");
    grouperDeprovisioningAttributeValue.setDirectAssignment(true);

    grouperDeprovisioningAttributeValue.setAffiliationString("employee");

    grouperDeprovisioningConfiguration.storeConfiguration();

    GrouperDeprovisioningLogic.updateDeprovisioningMetadata(deprovisioningStem);

    for (int i=0;i<10;i++) {
      for (int j=0;j<10;j++) {
        
        Stem stem = new StemSave(grouperSession).assignName(deprovisioningStem.getName() + ":grandParent_" + i + ":parent_" + j).assignCreateParentStemsIfNotExist(true).save();

        grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem, false);
        grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
        grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
        assertTrue(stem.getName(), grouperDeprovisioningAttributeValue.isDeprovision());

        for (int k=0; k<10; k++) {
          Group group = new GroupSave(grouperSession).assignName(stem.getName() + ":group_" + k).assignCreateParentStemsIfNotExist(true).save();
          //make sure its there
          grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
          grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
          grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
          assertTrue(group.getName(), grouperDeprovisioningAttributeValue.isDeprovision());
          
          
          AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName(stem.getName() + ":attributeDef_" + k).assignCreateParentStemsIfNotExist(true).save();
          grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(attributeDef, false);
          grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
          grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
          assertNotNull(attributeDef.getName(), grouperDeprovisioningAttributeValue);
          assertTrue(attributeDef.getName(), grouperDeprovisioningAttributeValue.isDeprovision());
          
        }
      }
    }

  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningLogic#updateDeprovisioningMetadataSmall(edu.internet2.middleware.grouper.Stem)}.
   */
  public void testUpdateDeprovisioningMetadataSmall() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem deprovisioningStem = new StemSave(grouperSession).assignName("deprovisioningSmall").save();
    
    for (int i=0;i<2;i++) {

      for (int j=0;j<2;j++) {
        
        Stem stem = new StemSave(grouperSession).assignName(deprovisioningStem.getName() + ":grandParent_" + i + ":parent_" + j).assignCreateParentStemsIfNotExist(true).save();
        
        for (int k=0; k<2; k++) {
          Group group = new GroupSave(grouperSession).assignName(stem.getName() + ":group_" + k).assignCreateParentStemsIfNotExist(true).save();
        }
      }
    }

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(deprovisioningStem, false);
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

    grouperDeprovisioningAttributeValue.setDeprovisionString("true");
    grouperDeprovisioningAttributeValue.setDirectAssignment(true);

    grouperDeprovisioningAttributeValue.setAffiliationString("employee");

    grouperDeprovisioningConfiguration.storeConfiguration();

    GrouperDeprovisioningLogic.updateDeprovisioningMetadata(deprovisioningStem);

    //make sure its there
    Group group = GroupFinder.findByName(grouperSession, deprovisioningStem.getName() + ":grandParent_0:parent_0:group_0", true);
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
  }

}
