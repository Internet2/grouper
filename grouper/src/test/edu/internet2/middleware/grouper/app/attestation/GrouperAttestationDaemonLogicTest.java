package edu.internet2.middleware.grouper.app.attestation;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class GrouperAttestationDaemonLogicTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  /**
   * @param name
   */
  public GrouperAttestationDaemonLogicTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperAttestationDaemonLogicTest("testIncrementalSyncLogic_deleteFromChildren"));
  }
  
  
  public void testRetrieveAllFoldersOfInterestForAttestation_SingleStemAssigned() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();

    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();

    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.report).assignStem(stem2)
    .addEmailAddress("test1@example.com").assignDaysUntilRecertify(17).assignSendEmail(true)
    .assignGroupCanAttest(group)
    .save();
    
    //When
    Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation = GrouperAttestationDaemonLogic.retrieveAllFoldersOfInterestForAttestation();
    
    //Then
    assertEquals(1, allFoldersOfInterestForAttestation.size());
    
    GrouperAttestationObjectAttributes objectAttributes = allFoldersOfInterestForAttestation.get("test");
    assertEquals("true", objectAttributes.getAttestationDirectAssign());
    assertEquals("15", objectAttributes.getDaysUntilRecertify());
   
  }
  
  public void testRetrieveAllFoldersOfInterestForAttestation_MultipleStemsAssigned() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();

    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();
    
    Stem stem3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test3").save();

    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem3)
      .assignDaysUntilRecertify(16).assignSendEmail(false).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.report).assignStem(stem2)
      .addEmailAddress("test1@example.com").assignDaysUntilRecertify(17).assignSendEmail(true)
      .assignGroupCanAttest(group)
      .save();
    
    //When
    Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation = GrouperAttestationDaemonLogic.retrieveAllFoldersOfInterestForAttestation();
    
    //Then
    assertEquals(2, allFoldersOfInterestForAttestation.size());
    
    GrouperAttestationObjectAttributes objectAttributes = allFoldersOfInterestForAttestation.get("test");
    assertEquals("true", objectAttributes.getAttestationDirectAssign());
    assertEquals("15", objectAttributes.getDaysUntilRecertify());
    
    objectAttributes = allFoldersOfInterestForAttestation.get("test3");
    assertEquals("true", objectAttributes.getAttestationDirectAssign());
    assertEquals("16", objectAttributes.getDaysUntilRecertify());
    
  }
  
  public void testRetrieveAllGroupsOfInterestForAttestation() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test-group1").save();
    
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test21:test-group2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test22:test-group3").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem2)
      .addEmailAddress("test2@example.com").assignDaysUntilRecertify(16).assignSendEmail(true)
      .assignStemScope(Scope.ONE)
      .save();
    
    new AttestationGroupSave().assignGroup(group3).assignDaysUntilRecertify(17).assignSendEmail(false).save();

    Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation = GrouperAttestationDaemonLogic.retrieveAllFoldersOfInterestForAttestation();
    
    //When
    Map<String, GrouperAttestationObjectAttributes> allGroupsOfInterestForAttestation = GrouperAttestationDaemonLogic.retrieveAllGroupsOfInterestForAttestation(allFoldersOfInterestForAttestation);
    
    //Then
    assertEquals(3, allGroupsOfInterestForAttestation.size());
    
    GrouperAttestationObjectAttributes objectAttributes = allGroupsOfInterestForAttestation.get("test:test-group");
    assertNull(objectAttributes.getAttestationDirectAssign());
    assertNull(objectAttributes.getDaysUntilRecertify());
    
    objectAttributes = allGroupsOfInterestForAttestation.get("test:test1:test-group1");
    assertNull(objectAttributes.getAttestationDirectAssign());
    assertNull(objectAttributes.getDaysUntilRecertify());
    
    objectAttributes = allGroupsOfInterestForAttestation.get("test2:test22:test-group3");
    assertEquals("true", objectAttributes.getAttestationDirectAssign());
    assertNull(objectAttributes.getDaysUntilRecertify());
    
  }
  
  
  public void testFullSyncLogic_copyFromStemToGroups() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test-group1").save();

    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test-group2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test22:test-group3").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem2)
      .addEmailAddress("test2@example.com").assignDaysUntilRecertify(16)
      .assignSendEmail(true)
      .assignStemScope(Scope.ONE)
      .save();
    
    //When
    GrouperAttestationDaemonLogic.fullSyncLogic();
    
    //Then
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    assertEquals("false", attestationDirectAssignment);
    
    String attestationHasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    assertEquals("true", attestationHasAttestation);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    assertEquals("false", attestationDirectAssignment);
    
    attestationHasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    assertEquals("true", attestationHasAttestation);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    assertEquals("false", attestationDirectAssignment);
    
    attestationHasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    assertEquals("true", attestationHasAttestation);
    
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
  }
  
  public void testFullSyncLogic_deleteFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test-group1").save();

    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test-group2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test22:test-group3").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem2)
      .addEmailAddress("test2@example.com").assignDaysUntilRecertify(16)
      .assignSendEmail(true)
      .assignStemScope(Scope.ONE)
      .save();
    
    //Run full sync so that children groups are populated
    GrouperAttestationDaemonLogic.fullSyncLogic();
    
    // verify children groups have attributes
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    // group3 shouldn't have attributes because scope was ONE for parent stem
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    // now delete attributes from parent folder directly
    new AttestationStemSave().assignStem(stem0).assignSaveMode(SaveMode.DELETE).save();
    new AttestationStemSave().assignStem(stem2).assignSaveMode(SaveMode.DELETE).save();
    
    // attributes on children groups should still be there
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    // group3 no difference should be made here
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    //now run full sync logic and it should delete indirect attributes from all children groups
    GrouperAttestationDaemonLogic.fullSyncLogic();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    // group3 no difference should be made here again - it was always null
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
  }
  
  public void testIncrementalSyncLogic_copyFromStemToGroups() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
    .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    runJobs(true, true);
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test-group1").save();

    //When
    runJobs(true, true);
    
    //Then
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    assertEquals("false", attestationDirectAssignment);
    
    String attestationHasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    assertEquals("true", attestationHasAttestation);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    assertEquals("false", attestationDirectAssignment);
    
    attestationHasAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
    assertEquals("true", attestationHasAttestation);
    
  }
  
  public void testIncrementalSyncLogic_deleteFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2").save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem0)
      .addEmailAddress("test@example.com").assignDaysUntilRecertify(15).assignSendEmail(true).save();
    
    new AttestationStemSave().assignAttestationType(AttestationType.group).assignStem(stem2)
      .addEmailAddress("test2@example.com").assignDaysUntilRecertify(16)
      .assignSendEmail(true)
      .assignStemScope(Scope.ONE)
      .save();
    
    runJobs(true, true);
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test-group").save();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test-group1").save();

    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test-group2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test2:test22:test-group3").save();
    
    runJobs(true, true);
    
    // verify children groups have attributes
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    // group3 shouldn't have attributes because scope was ONE for parent stem
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    // now delete attributes from parent folder directly
    new AttestationStemSave().assignStem(stem0).assignSaveMode(SaveMode.DELETE).save();
    new AttestationStemSave().assignStem(stem2).assignSaveMode(SaveMode.DELETE).save();
    
    // attributes on children groups should still be there
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNotNull(attributeAssign);
    
    // group3 no difference should be made here
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    //now run incremental sync logic and it should delete indirect attributes from all children groups
    runJobs(true, true);
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    attributeAssign = group1.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    attributeAssign = group2.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
    // group3 no difference should be made here again - it was always null
    attributeAssign = group3.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    assertNull(attributeAssign);
    
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
          
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_grouperAttestationIncremental");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("grouperAttestationIncremental", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }
  
}
