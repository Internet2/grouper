/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import org.apache.commons.lang3.StringUtils;


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
    
    int sizeI = 2;
    int sizeJ = 2;
    int sizeK = 2;
    
    for (int i=0;i<sizeI;i++) {

      for (int j=0;j<sizeJ;j++) {
        
        Stem stem = new StemSave(grouperSession).assignName(deprovisioningStem.getName() + ":grandParent_" + i + ":parent_" + j).assignCreateParentStemsIfNotExist(true).save();
        
        for (int k=0; k<sizeK; k++) {
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

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deprovisioningFullSyncDaemon");

    for (int i=0;i<sizeI;i++) {
      for (int j=0;j<sizeJ;j++) {
        
        Stem stem = new StemSave(grouperSession).assignName(deprovisioningStem.getName() + ":grandParent_" + i + ":parent_" + j).assignCreateParentStemsIfNotExist(true).save();

        grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem, false);
        grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
        grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
        assertTrue(stem.getName(), grouperDeprovisioningAttributeValue.isDeprovision());

        for (int k=0; k<sizeK; k++) {
          Group group = new GroupSave(grouperSession).assignName(stem.getName() + ":group_" + k).assignCreateParentStemsIfNotExist(true).save();
          //make sure its there
          grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
          grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
          grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
          assertTrue(group.getName(), grouperDeprovisioningAttributeValue.isDeprovision());
          
          
// https://todos.internet2.edu/browse/GRP-3580
//          AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName(stem.getName() + ":attributeDef_" + k).assignCreateParentStemsIfNotExist(true).save();
//          grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(attributeDef, false);
//          grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
//          grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
//          assertNotNull(attributeDef.getName(), grouperDeprovisioningAttributeValue);
//          assertTrue(attributeDef.getName(), grouperDeprovisioningAttributeValue.isDeprovision());
          
        }
      }
    }

  }

  /**
   * count the metadata assignment-on-assignment rows for a given attributeDefName on a base assign
   * @param attributeAssignBase
   * @param attributeDefName
   * @return the count
   */
  private static int countMetadataAssigns(AttributeAssign attributeAssignBase, AttributeDefName attributeDefName) {
    Set<AttributeAssign> metadataAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findByOwnerAttributeAssignId(attributeAssignBase.getId(), new QueryOptions().secondLevelCache(false));
    int count = 0;
    for (AttributeAssign metadataAttributeAssign : metadataAttributeAssigns) {
      if (StringUtils.equals(attributeDefName.getId(), metadataAttributeAssign.getAttributeDefNameId())) {
        count++;
      }
    }
    return count;
  }

  /**
   * GRP-7038: concurrent deprovisioning config saves can create duplicate metadata
   * assignment-on-assignment rows for the same single-assign attribute, which then break the
   * deprovision-a-user screen.  Verify that storeConfiguration collapses such duplicates so the
   * data self-heals on the next save.
   */
  public void testStoreConfigurationRemovesDuplicateMetadataAssignments() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem deprovisioningStem = new StemSave(grouperSession).assignName("deprovisioningDup").save();

    Group group = new GroupSave(grouperSession).assignName(deprovisioningStem.getName() + ":someGroup")
        .assignCreateParentStemsIfNotExist(true).save();

    // build and store an initial deprovisioning configuration for the employee affiliation
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration =
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);

    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
    grouperDeprovisioningAttributeValue.setAffiliationString("employee");
    grouperDeprovisioningAttributeValue.setDirectAssignment(true);
    grouperDeprovisioningAttributeValue.setAutoChangeLoaderString("false");

    grouperDeprovisioningConfiguration.storeConfiguration();

    AttributeAssign attributeAssignBase = grouperDeprovisioningConfiguration.getAttributeAssignBase();
    assertNotNull(attributeAssignBase);

    AttributeDefName autoChangeLoaderAttributeDefName =
        GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader();

    // sanity: exactly one autoChangeLoader metadata assignment right now
    assertEquals(1, countMetadataAssigns(attributeAssignBase, autoChangeLoaderAttributeDefName));

    // manufacture the post-race corruption: a second metadata assignment for the same single-assign
    // attribute on the same base assign (this is what two concurrent saves produce)
    AttributeAssign duplicateAttributeAssign = new AttributeAssign(attributeAssignBase, null, autoChangeLoaderAttributeDefName, null);
    duplicateAttributeAssign.saveOrUpdate(false);
    duplicateAttributeAssign.getValueDelegate().assignValue("false");

    assertEquals(2, countMetadataAssigns(attributeAssignBase, autoChangeLoaderAttributeDefName));

    // re-store the configuration (unchanged values); storeConfiguration should collapse the duplicate
    GrouperDeprovisioningAttributeValue resaveAttributeValue = new GrouperDeprovisioningAttributeValue();
    resaveAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
    resaveAttributeValue.setAffiliationString("employee");
    resaveAttributeValue.setDirectAssignment(true);
    resaveAttributeValue.setAutoChangeLoaderString("false");
    grouperDeprovisioningConfiguration.setNewConfig(resaveAttributeValue);

    grouperDeprovisioningConfiguration.storeConfiguration();

    // the duplicate is gone - exactly one assignment remains
    assertEquals(1, countMetadataAssigns(attributeAssignBase, autoChangeLoaderAttributeDefName));

    // and the read path that the duplicate used to break now works
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    assertNotNull(grouperDeprovisioningConfiguration);
    assertEquals("false", grouperDeprovisioningConfiguration.getOriginalConfig().getAutoChangeLoaderString());
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

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deprovisioningFullSyncDaemon");

    //make sure its there
    Group group = GroupFinder.findByName(grouperSession, deprovisioningStem.getName() + ":grandParent_0:parent_0:group_0", true);
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
  }

}
