/**
 * 
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeStemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypeObjectAttributes;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class GrouperDeprovisioningDaemonLogicTest extends GrouperTest {

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  /**
   * @param name
   */
  public GrouperDeprovisioningDaemonLogicTest(String name) {
    super(name);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student, employee");
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperDeprovisioningDaemonLogicTest("testRetrieveDeprovisioningAttributesByStem"));
  }
  
  public void testRetrieveAllFoldersOfInterestForDeprovisioning() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.ONE);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(true);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(true);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    
    //When
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> allFoldersOfInterestForDeprovisioning = 
        GrouperDeprovisioningDaemonLogic.retrieveAllFoldersOfInterestForDeprovisioning();
    
    //Then
    assertEquals(2, allFoldersOfInterestForDeprovisioning.size());
    assertEquals(2, allFoldersOfInterestForDeprovisioning.get("employee").size());
    assertEquals(2, allFoldersOfInterestForDeprovisioning.get("student").size());
    
    {
      Map<String, GrouperDeprovisioningObjectAttributes> stemToObjectDeprovisioningAttributes = allFoldersOfInterestForDeprovisioning.get("employee");
      assertTrue(stemToObjectDeprovisioningAttributes.containsKey("test"));
      assertTrue(stemToObjectDeprovisioningAttributes.containsKey("test:test1"));
      
      GrouperDeprovisioningObjectAttributes testStemAttributes = stemToObjectDeprovisioningAttributes.get("test");
      assertEquals("false", testStemAttributes.getDeprovision());
      assertEquals("true", testStemAttributes.getDirectAssign());
      assertEquals("employee", testStemAttributes.getAffiliation());
      assertEquals("true", testStemAttributes.getAllowAddsWhileDeprovisioned());
      assertEquals("false", testStemAttributes.getAutoChangeLoader());
      assertEquals("false", testStemAttributes.getAutoSelectForRemoval());
      assertEquals("test@example.com", testStemAttributes.getEmailAddresses());
      assertEquals("test email body", testStemAttributes.getEmailBody());
      assertEquals("true", testStemAttributes.getSendEmail());
      assertEquals("false", testStemAttributes.getShowForRemoval());
      assertEquals("test", testStemAttributes.getName());
      assertEquals(stem0.getId(), testStemAttributes.getId());
      assertEquals("ONE", testStemAttributes.getStemScope());
      
      GrouperDeprovisioningObjectAttributes test1StemAttributes = stemToObjectDeprovisioningAttributes.get("test:test1");
      assertEquals(stem1.getId(), test1StemAttributes.getId());
      assertEquals("test:test1", test1StemAttributes.getName());
    }
    
    {
      Map<String, GrouperDeprovisioningObjectAttributes> stemToObjectDeprovisioningAttributes = allFoldersOfInterestForDeprovisioning.get("student");
      assertTrue(stemToObjectDeprovisioningAttributes.containsKey("test"));
      assertTrue(stemToObjectDeprovisioningAttributes.containsKey("test:test1"));
      
      GrouperDeprovisioningObjectAttributes testStemAttributes = stemToObjectDeprovisioningAttributes.get("test");
      assertEquals("true", testStemAttributes.getDirectAssign());
      assertEquals("student", testStemAttributes.getAffiliation());
      assertEquals("true", testStemAttributes.getAllowAddsWhileDeprovisioned());
      assertEquals("false", testStemAttributes.getShowForRemoval());
      assertEquals("test", testStemAttributes.getName());
      assertEquals(stem0.getId(), testStemAttributes.getId());
      
      GrouperDeprovisioningObjectAttributes test1StemAttributes = stemToObjectDeprovisioningAttributes.get("test:test1");
      assertEquals(stem1.getId(), test1StemAttributes.getId());
      assertEquals("test:test1", test1StemAttributes.getName());
    }
    
  }
  
  public void testRetrieveAllGroupsOfInterestForDeprovisioning() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.ONE);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(true);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(true);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> allFoldersOfInterestForDeprovisioning = 
        GrouperDeprovisioningDaemonLogic.retrieveAllFoldersOfInterestForDeprovisioning();
    
    //When
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> allGroupsOfInterestForDeprovisioning = GrouperDeprovisioningDaemonLogic.retrieveAllGroupsOfInterestForDeprovisioning(allFoldersOfInterestForDeprovisioning);
    
    //Then
    assertEquals(2, allGroupsOfInterestForDeprovisioning.size());
    assertEquals(1, allGroupsOfInterestForDeprovisioning.get("employee").size());
    assertEquals(1, allGroupsOfInterestForDeprovisioning.get("student").size());
    
    {
      Map<String, GrouperDeprovisioningObjectAttributes> groupToObjectDeprovisioningAttributes = allGroupsOfInterestForDeprovisioning.get("employee");
      assertTrue(groupToObjectDeprovisioningAttributes.containsKey("test:test1-group"));
      
      GrouperDeprovisioningObjectAttributes testStemAttributes = groupToObjectDeprovisioningAttributes.get("test:test1-group");
      assertEquals("false", testStemAttributes.getDeprovision());
      assertEquals("true", testStemAttributes.getDirectAssign());
      assertEquals("employee", testStemAttributes.getAffiliation());
      assertEquals("true", testStemAttributes.getAllowAddsWhileDeprovisioned());
      assertEquals("false", testStemAttributes.getAutoChangeLoader());
      assertEquals("false", testStemAttributes.getAutoSelectForRemoval());
      assertEquals("test@example.com", testStemAttributes.getEmailAddresses());
      assertEquals("test email body", testStemAttributes.getEmailBody());
      assertEquals("true", testStemAttributes.getSendEmail());
      assertEquals("false", testStemAttributes.getShowForRemoval());
      assertEquals("test:test1-group", testStemAttributes.getName());
      assertEquals(group0.getId(), testStemAttributes.getId());
      assertEquals("ONE", testStemAttributes.getStemScope());
      
    }
    
    {
      Map<String, GrouperDeprovisioningObjectAttributes> groupToObjectDeprovisioningAttributes = allGroupsOfInterestForDeprovisioning.get("student");
      assertTrue(groupToObjectDeprovisioningAttributes.containsKey("test:test1-group"));
      
      GrouperDeprovisioningObjectAttributes testStemAttributes = groupToObjectDeprovisioningAttributes.get("test:test1-group");
      assertEquals("true", testStemAttributes.getDirectAssign());
      assertEquals("student", testStemAttributes.getAffiliation());
      assertEquals("true", testStemAttributes.getAllowAddsWhileDeprovisioned());
      assertEquals("false", testStemAttributes.getShowForRemoval());
      assertEquals("test:test1-group", testStemAttributes.getName());
      assertEquals(group0.getId(), testStemAttributes.getId());
    }
  }

  public void testFullSyncLogic_CopyFromStemToChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:test2-group").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.ONE);
      grouperDeprovisioningConfiguration.storeConfiguration();
    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(true);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(true);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test1@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body1");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    //When
    GrouperDeprovisioningDaemonLogic.fullSyncLogic();
    
    //Then
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertFalse(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertNull(grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    assertEquals("test1@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body1", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    
  }
  
  public void testFullSyncLogic_DeleteAttributesFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:test2-group").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);
      grouperDeprovisioningConfiguration.storeConfiguration();
    }

    //Run full sync logic first so that attributes can be propagated to children
    GrouperDeprovisioningDaemonLogic.fullSyncLogic();
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    //now delete attributes from the test folder
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningConfiguration.clearOutConfigurationButLeaveMetadata();
    grouperDeprovisioningConfiguration.storeConfiguration();
    Set<AttributeAssign> assignments = stem0.getAttributeDelegate().retrieveAssignments(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase());
    for (AttributeAssign attributeAssign: assignments) {
      attributeAssign.delete();
    }
    
    // now run full sync and that should delete all the deprovisioning attributes from all the children
    GrouperDeprovisioningDaemonLogic.fullSyncLogic();
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
  }
  
  public void testIncrementalSyncLogic_copyFromStemToChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.ONE);
      grouperDeprovisioningConfiguration.storeConfiguration();
    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(true);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(true);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    runJobs(true, true);
    
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    
    //When
    runJobs(true, true);
    
    //Then
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("test@example.com", grouperDeprovisioningAttributeValue.getEmailAddressesString());
    assertEquals("test email body", grouperDeprovisioningAttributeValue.getEmailBodyString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getSendEmailString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals("ONE", grouperDeprovisioningAttributeValue.getStemScopeString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("student");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("student", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString());
    assertNull(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString());
    assertEquals("false", grouperDeprovisioningAttributeValue.getShowForRemovalString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
  }
  
  
  public void testIncrementalSyncLogic_deleteFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    runJobs(true, true);
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:test2-group").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("true");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");
      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
      grouperDeprovisioningAttributeValue.setStemScope(Scope.SUB);
      grouperDeprovisioningConfiguration.storeConfiguration();
    }
    
    //Run full sync logic first so that attributes can be propagated to children
    GrouperDeprovisioningDaemonLogic.fullSyncLogic();
    
    runJobs(true, true);
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getOriginalConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getDirectAssignmentString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertTrue(grouperDeprovisioningAttributeValue.isDeprovision());
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertEquals("true", grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    assertEquals(stem0.getId(), grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString());
    
    //now delete attributes from the test folder
    Set<AttributeAssign> assignments = stem0.getAttributeDelegate().retrieveAssignments(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase());
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            for (AttributeAssign attributeAssign: assignments) {
              attributeAssign.delete();
            }
            return null;
          }
        });
      };
    });
    
    // now run the incremental sync; it should delete attributes from children
    runJobs(true, true);
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group0, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group1, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
    grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group2, false);
    grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get("employee");
    grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
    assertEquals("employee", grouperDeprovisioningAttributeValue.getAffiliationString());
    assertNull(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString());
    
  }
  
  public void testRetrieveDeprovisioningAttributesByGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test1@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body1");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    //When
    Map<String, GrouperDeprovisioningObjectAttributes> deprovisioningAttributesAssignedToGroup = GrouperDeprovisioningDaemonLogic.retrieveDeprovisioningAttributesByGroup(group.getId());
    
    //Then
    assertEquals(2, deprovisioningAttributesAssignedToGroup.size());
    
    GrouperDeprovisioningObjectAttributes employeeAttributes = deprovisioningAttributesAssignedToGroup.get("employee");
    assertEquals("true", employeeAttributes.getDirectAssign());
    assertNull(employeeAttributes.getSendEmail());
    
    GrouperDeprovisioningObjectAttributes studentAttributes = deprovisioningAttributesAssignedToGroup.get("student");
    assertEquals("true", studentAttributes.getDirectAssign());
    assertEquals("true", studentAttributes.getSendEmail());
    
  }
  
  public void testRetrieveDeprovisioningAttributesByStem() {

    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = null;
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = null;
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = null;
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("student");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setEmailAddressesString("test1@example.com");
      grouperDeprovisioningAttributeValue.setEmailBodyString("test email body1");
      grouperDeprovisioningAttributeValue.setSendEmail(true);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    {
      grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0);
      
      grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
      grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
      grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

      grouperDeprovisioningAttributeValue.setDeprovisionString("false");
      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
      grouperDeprovisioningAttributeValue.setAffiliationString("employee");

      grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(true);
      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
      grouperDeprovisioningAttributeValue.setSendEmail(false);
      grouperDeprovisioningAttributeValue.setShowForRemoval(false);

      grouperDeprovisioningConfiguration.storeConfiguration();

    }
    
    //When
    Map<String, GrouperDeprovisioningObjectAttributes> deprovisioningAttributesAssignedToStem = GrouperDeprovisioningDaemonLogic.retrieveDeprovisioningAttributesByStem(stem0.getId());
    
    //Then
    assertEquals(2, deprovisioningAttributesAssignedToStem.size());
    
    GrouperDeprovisioningObjectAttributes employeeAttributes = deprovisioningAttributesAssignedToStem.get("employee");
    assertEquals("true", employeeAttributes.getDirectAssign());
    assertNull(employeeAttributes.getSendEmail());
    
    GrouperDeprovisioningObjectAttributes studentAttributes = deprovisioningAttributesAssignedToStem.get("student");
    assertEquals("true", studentAttributes.getDirectAssign());
    assertEquals("true", studentAttributes.getSendEmail());
  }
  
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
    // wait for message cache to clear
    GrouperUtil.sleep(10000);
    
    if (runChangeLog) {
      ChangeLogTempToEntity.convertRecords();
    }
    
    if (runConsumer) {
      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_grouperDeprovisioningIncremental");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("grouperDeprovisioningIncremental", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }
  
}
