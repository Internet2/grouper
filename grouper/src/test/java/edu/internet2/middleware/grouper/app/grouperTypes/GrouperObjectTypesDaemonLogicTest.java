package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class GrouperObjectTypesDaemonLogicTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("objectTypes.enable", "true");
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  /**
   * @param name
   */
  public GrouperObjectTypesDaemonLogicTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperObjectTypesDaemonLogicTest("testIncrementalSyncLogic_copyFromStemToChildren"));
  }
  
  
  public void testRetrieveAllFoldersOfInterestForTypes_SingleStemAssigned() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allFoldersOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllFoldersOfInterestForTypes();
    
    //Then
    assertEquals(1, allFoldersOfInterestForTypes.size());
    assertEquals(2, allFoldersOfInterestForTypes.get("ref").size());
    
    Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = allFoldersOfInterestForTypes.get("ref");
    assertTrue(stemToObjectTypeAttributes.containsKey("test"));
    assertTrue(stemToObjectTypeAttributes.containsKey("test:test1"));
    
  }
  
  public void testRetrieveAllFoldersOfInterestForTypes_TwoStemsAssigned() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem1).assignType("basis").save();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allFoldersOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllFoldersOfInterestForTypes();
    
    //Then
    assertEquals(2, allFoldersOfInterestForTypes.size());
    assertEquals(3, allFoldersOfInterestForTypes.get("ref").size());
    assertEquals(2, allFoldersOfInterestForTypes.get("basis").size());
    
    Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = allFoldersOfInterestForTypes.get("ref");
    assertTrue(stemToObjectTypeAttributes.containsKey("test"));
    assertTrue(stemToObjectTypeAttributes.containsKey("test:test1"));
    assertTrue(stemToObjectTypeAttributes.containsKey("test:test1:test2"));
    stemToObjectTypeAttributes = allFoldersOfInterestForTypes.get("basis");
    assertTrue(stemToObjectTypeAttributes.containsKey("test:test1"));
    assertTrue(stemToObjectTypeAttributes.containsKey("test:test1:test2"));
    
  }
  
  public void testRetrieveAllGroupsOfInterestForTypes_SingleGroupAssigned() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allStemsOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllFoldersOfInterestForTypes();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allGroupsOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllGroupsOfInterestForTypes(allStemsOfInterestForTypes);
    
    //Then
    assertEquals(1, allGroupsOfInterestForTypes.size());
    assertEquals(1, allGroupsOfInterestForTypes.get("ref").size());
    
    Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = allGroupsOfInterestForTypes.get("ref");
    assertTrue(groupToObjectTypeAttributes.containsKey("test:test1-group"));
    
  }
  
  public void testRetrieveAllGroupsOfInterestForTypes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("basis").save();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do2").assignMemberDescription("md2").assignGroup(group2).assignType("ref").save();
    
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do3").assignMemberDescription("md3").assignGroup(group3).assignType("basis").save();
    
    Group group4 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1:test4-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do4").assignMemberDescription("md4").assignGroup(group4).assignType("ref").save();
    
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allStemsOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllFoldersOfInterestForTypes();

    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allGroupsOfInterestForTypes = GrouperObjectTypesDaemonLogic.retrieveAllGroupsOfInterestForTypes(allStemsOfInterestForTypes);
    
    //Then
    assertEquals(2, allGroupsOfInterestForTypes.size());
    assertEquals(3, allGroupsOfInterestForTypes.get("ref").size());
    assertEquals(3, allGroupsOfInterestForTypes.get("basis").size());
    
    Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = allGroupsOfInterestForTypes.get("ref");
    assertEquals(3, groupToObjectTypeAttributes.size());
    
    GrouperObjectTypeObjectAttributes grouperObjectTypeObjectAttributes = groupToObjectTypeAttributes.get("test:test1-group");
    assertEquals("do", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    
    grouperObjectTypeObjectAttributes = groupToObjectTypeAttributes.get("test:test2-group");
    assertEquals("do2", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md2", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    grouperObjectTypeObjectAttributes = groupToObjectTypeAttributes.get("test1:test4-group");
    assertEquals("do4", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md4", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    groupToObjectTypeAttributes = allGroupsOfInterestForTypes.get("basis");
    assertEquals(3, groupToObjectTypeAttributes.size());
    assertTrue(groupToObjectTypeAttributes.containsKey("test:test1-group"));
    assertTrue(groupToObjectTypeAttributes.containsKey("test:test3-group"));
    assertTrue(groupToObjectTypeAttributes.containsKey("test:test2-group"));
    
  }
  
  public void testFullSyncLogic_copyFromStemToChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    
    //When
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    GrouperObjectTypesAttributeValue stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    GrouperObjectTypesAttributeValue groupObjectTypeAttributeValue = gdgTypeGroupAssignments.iterator().next();
    
    assertEquals("do", groupObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", groupObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", groupObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), groupObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, groupObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(groupObjectTypeAttributeValue.isDirectAssignment());
    
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem2).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
  }
  
  public void testFullSyncLogic_deleteFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    
    //When
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    GrouperObjectTypesAttributeValue stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    GrouperObjectTypesAttributeValue groupObjectTypeAttributeValue = gdgTypeGroupAssignments.iterator().next();
    
    assertEquals("do", groupObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", groupObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", groupObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), groupObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, groupObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(groupObjectTypeAttributeValue.isDirectAssignment());
    
    // now delete object types from "test" folder
    new GdgTypeStemSave().assignSaveMode(SaveMode.DELETE).assignStem(stem0).assignType("ref").save();
    
    // object types should still be there on the children
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    // now run the full sync; it should delete object types from children
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(0, gdgTypeStemAssignments.size());
    gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(0, gdgTypeGroupAssignments.size());
    
  }
  
  public void testIncrementalSyncLogic_copyFromStemToChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    
    runJobs(true, true);
    
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    
    //When
    runJobs(true, true);
    
    // Then
    Set<GrouperObjectTypesAttributeValue> gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    GrouperObjectTypesAttributeValue stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    GrouperObjectTypesAttributeValue groupObjectTypeAttributeValue = gdgTypeGroupAssignments.iterator().next();
    
    assertEquals("do", groupObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", groupObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", groupObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), groupObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, groupObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(groupObjectTypeAttributeValue.isDirectAssignment());
    
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem2).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    //modify the values and confirm they are reflected on the children
    new GdgTypeStemSave().assignDataOwner("do1").assignMemberDescription("md1").assignStem(stem0).assignType("ref").save();
    
    runJobs(true, true);
    
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do1", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md1", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    groupObjectTypeAttributeValue = gdgTypeGroupAssignments.iterator().next();
    
    assertEquals("do1", groupObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md1", groupObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", groupObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), groupObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, groupObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(groupObjectTypeAttributeValue.isDirectAssignment());
    
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem2).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do1", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md1", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    
  }
  
  
  public void testIncrementalSyncLogic_deleteFromChildren() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    
    runJobs(true, true);
    
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1-group").save();
    
    //When
    runJobs(true, true);
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    
    GrouperObjectTypesAttributeValue stemObjectTypeAttributeValue = gdgTypeStemAssignments.iterator().next();
    
    assertEquals("do", stemObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", stemObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", stemObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), stemObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, stemObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(stemObjectTypeAttributeValue.isDirectAssignment());
    
    Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    GrouperObjectTypesAttributeValue groupObjectTypeAttributeValue = gdgTypeGroupAssignments.iterator().next();
    
    assertEquals("do", groupObjectTypeAttributeValue.getObjectTypeDataOwner());
    assertEquals("md", groupObjectTypeAttributeValue.getObjectTypeMemberDescription());
    assertEquals("ref", groupObjectTypeAttributeValue.getObjectTypeName());
    assertEquals(stem0.getId(), groupObjectTypeAttributeValue.getObjectTypeOwnerStemId());
    assertEquals(null, groupObjectTypeAttributeValue.getObjectTypeServiceName());
    assertFalse(groupObjectTypeAttributeValue.isDirectAssignment());
    
    // now delete object types from "test" folder
    new GdgTypeStemSave().assignSaveMode(SaveMode.DELETE).assignStem(stem0).assignType("ref").save();
    
    // object types should still be there on the children
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(1, gdgTypeStemAssignments.size());
    gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(1, gdgTypeGroupAssignments.size());
    
    // now run the incremental sync; it should delete object types from children
    runJobs(true, true);
    gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(stem1).findGdgTypeStemAssignments();
    assertEquals(0, gdgTypeStemAssignments.size());
    gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(group0).findGdgTypeGroupAssignments();
    assertEquals(0, gdgTypeGroupAssignments.size());
    
  }
  
  public void testRetrieveObjectTypeAttributesByGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    new GdgTypeGroupSave().assignGroup(group0).assignType("app").save();
    
    //When
    Map<String, GrouperObjectTypeObjectAttributes> objectTypesAssignedToGroup = GrouperObjectTypesDaemonLogic.retrieveObjectTypeAttributesByGroup(group0.getId());
    
    //Then
    assertEquals(2, objectTypesAssignedToGroup.size());
    
    GrouperObjectTypeObjectAttributes refObjectTypeAttributes = objectTypesAssignedToGroup.get("ref");
    assertEquals("true", refObjectTypeAttributes.getObjectTypeDirectAssign());
    assertEquals("do", refObjectTypeAttributes.getObjectTypeDataOwner());
    assertEquals("md", refObjectTypeAttributes.getObjectTypeMemberDescription());
    
    GrouperObjectTypeObjectAttributes appObjectTypeAttributes = objectTypesAssignedToGroup.get("app");
    assertEquals("true", appObjectTypeAttributes.getObjectTypeDirectAssign());
    assertNull(appObjectTypeAttributes.getObjectTypeDataOwner());
    assertNull(appObjectTypeAttributes.getObjectTypeMemberDescription());
    
  }
  
  public void testRetrieveObjectTypeAttributesByStem() {

    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Stem stem3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test3").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    new GdgTypeStemSave().assignDataOwner("do1").assignMemberDescription("md1").assignStem(stem0).assignType("basis").save();
    new GdgTypeStemSave().assignDataOwner("do2").assignMemberDescription("md2").assignStem(stem2).assignType("ref").save();
    new GdgTypeStemSave().assignDataOwner("do3").assignMemberDescription("md3").assignStem(stem3).assignType("ref").save();
    
    //When
    Map<String, GrouperObjectTypeObjectAttributes> objectTypesAssignedToStem = GrouperObjectTypesDaemonLogic.retrieveObjectTypeAttributesByStem(stem0.getId());
    
    //Then
    assertEquals(2, objectTypesAssignedToStem.size());
    
    GrouperObjectTypeObjectAttributes refObjectTypeAttributes = objectTypesAssignedToStem.get("ref");
    assertEquals("true", refObjectTypeAttributes.getObjectTypeDirectAssign());
    assertEquals("do", refObjectTypeAttributes.getObjectTypeDataOwner());
    assertEquals("md", refObjectTypeAttributes.getObjectTypeMemberDescription());
    
    GrouperObjectTypeObjectAttributes basisObjectTypeAttributes = objectTypesAssignedToStem.get("basis");
    assertEquals("true", basisObjectTypeAttributes.getObjectTypeDirectAssign());
    assertEquals("do1", basisObjectTypeAttributes.getObjectTypeDataOwner());
    assertEquals("md1", basisObjectTypeAttributes.getObjectTypeMemberDescription());
    
  }
  
  public void testRetrieveFolderAndAncestorObjectTypesAttributesByFolder() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    Stem stem3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:test3").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    new GdgTypeStemSave().assignDataOwner("do1").assignMemberDescription("md1").assignStem(stem0).assignType("basis").save();
    new GdgTypeStemSave().assignStem(stem2).assignType("app").save();
    
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> objectTypesToStemsToObjectAttributes = GrouperObjectTypesDaemonLogic.retrieveFolderAndAncestorObjectTypesAttributesByFolder(stem2.getName());
    
    assertEquals(3, objectTypesToStemsToObjectAttributes.size());
    
    Map<String, GrouperObjectTypeObjectAttributes> appToStemsObjectAttributes = objectTypesToStemsToObjectAttributes.get("app");
    assertEquals(1, appToStemsObjectAttributes.size());
    
    GrouperObjectTypeObjectAttributes grouperObjectTypeObjectAttributes = appToStemsObjectAttributes.get("test:test1:test2");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("true", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertNull(grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    
    
    Map<String, GrouperObjectTypeObjectAttributes> basisToStemsObjectAttributes = objectTypesToStemsToObjectAttributes.get("basis");
    assertEquals(3, basisToStemsObjectAttributes.size());
    
    grouperObjectTypeObjectAttributes = basisToStemsObjectAttributes.get("test:test1:test2");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("false", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do1", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md1", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    grouperObjectTypeObjectAttributes = basisToStemsObjectAttributes.get("test:test1");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("false", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do1", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md1", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    grouperObjectTypeObjectAttributes = basisToStemsObjectAttributes.get("test");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("true", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do1", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md1", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    Map<String, GrouperObjectTypeObjectAttributes> refToStemsObjectAttributes = objectTypesToStemsToObjectAttributes.get("ref");
    assertEquals(3, refToStemsObjectAttributes.size());
    
    grouperObjectTypeObjectAttributes = refToStemsObjectAttributes.get("test:test1:test2");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("false", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    grouperObjectTypeObjectAttributes = refToStemsObjectAttributes.get("test:test1");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("false", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
    grouperObjectTypeObjectAttributes = refToStemsObjectAttributes.get("test");
    assertTrue(grouperObjectTypeObjectAttributes.isOwnedByStem());
    assertEquals("true", grouperObjectTypeObjectAttributes.getObjectTypeDirectAssign());
    assertEquals("do", grouperObjectTypeObjectAttributes.getObjectTypeDataOwner());
    assertEquals("md", grouperObjectTypeObjectAttributes.getObjectTypeMemberDescription());
    
  }
  
  public void testGetStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md")
        .assignStem(stem0).assignType("ref").save();
    
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerStemId(stem0.getId()).assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    //When
    String stemId = GrouperObjectTypesDaemonLogic.retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(stemId, stem0.getId());
    
  } 
  
  public void testGetStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignIdUnassign() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md")
        .assignStem(stem0).assignType("ref").save();
    
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerStemId(stem0.getId()).assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    new GdgTypeStemSave().assignSaveMode(SaveMode.DELETE).assignStem(stem0).assignType("ref").save();
    
    runJobs(true, false);
    
    //When
    String stemId =  GrouperObjectTypesDaemonLogic.retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(stemId, stem0.getId());
    
    
  } 
  
  public void testGetStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignIdDelete() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md")
        .assignStem(stem0).assignType("ref").save();
    
    //When
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerStemId(stem0.getId()).assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    stem0.obliterate(false, false);
    
    runJobs(true, false);
    
    //When
    String stemId = GrouperObjectTypesDaemonLogic.retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(stemId, stem0.getId());
    
  } 
  
  
  
  public void testRetrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerGroupId(group0.getId())
        .assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    //When
    String groupId = GrouperObjectTypesDaemonLogic.retrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(groupId, group0.getId());
    
  } 
  
  public void testRetrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignIdUnassign() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerGroupId(group0.getId())
        .assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    new GdgTypeGroupSave().assignSaveMode(SaveMode.DELETE).assignGroup(group0).assignType("ref").save();
    
    runJobs(true, false);
    
    //When
    String groupId =  GrouperObjectTypesDaemonLogic.retrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(groupId, group0.getId());
    
  } 
  
  public void testRetrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignIdDelete() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    //When
    String markerAttributeAssignId = new AttributeAssignFinder().addOwnerGroupId(group0.getId())
        .assignAttributeDefNameIds(GrouperUtil.toSet(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getId()))
      .findAttributeAssigns().iterator().next().getId();
    
    runJobs(true, false);
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(markerAttributeAssignId, false);
    
    group0.delete();
    
    runJobs(true, false);
    
    //When
    String groupId = GrouperObjectTypesDaemonLogic.retrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getId());
    
    assertEquals(groupId, group0.getId());
    
  } 
  
  
  public void testRetrieveChildObjectTypesFolderAttributesByFolder() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    //Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
    //Stem stem3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:test3").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("ref").save();
    new GdgTypeStemSave().assignDataOwner("do1").assignMemberDescription("md1").assignStem(stem0).assignType("basis").save();
    new GdgTypeStemSave().assignStem(stem2).assignType("app").save();
    
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> childObjectTypesFolderAttributesByFolder = GrouperObjectTypesDaemonLogic.retrieveChildObjectTypesFolderAttributesByFolder(stem0.getId());
    
    //Then
    assertEquals(3, childObjectTypesFolderAttributesByFolder.size());
    
    assertTrue(childObjectTypesFolderAttributesByFolder.containsKey("ref"));
    assertTrue(childObjectTypesFolderAttributesByFolder.containsKey("basis"));
    assertTrue(childObjectTypesFolderAttributesByFolder.containsKey("app"));
    
  }
  
  
  public void testRetrieveChildObjectTypesGroupAttributesByFolder() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    new GdgTypeStemSave().assignDataOwner("do").assignMemberDescription("md").assignStem(stem0).assignType("basis").save();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do").assignMemberDescription("md").assignGroup(group0).assignType("ref").save();
    
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do2").assignMemberDescription("md2").assignGroup(group2).assignType("ref").save();
    
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do3").assignMemberDescription("md3").assignGroup(group3).assignType("basis").save();
    
    Group group4 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1:test4-group").save();
    
    new GdgTypeGroupSave().assignDataOwner("do4").assignMemberDescription("md4").assignGroup(group4).assignType("ref").save();
    
    GrouperObjectTypesDaemonLogic.fullSyncLogic();
    
    //When
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> childObjectTypesFolderAttributesByFolder = GrouperObjectTypesDaemonLogic.retrieveChildObjectTypesGroupAttributesByFolder(stem0.getId());
    
    //Then
    assertEquals(2, childObjectTypesFolderAttributesByFolder.size());
    
    assertTrue(childObjectTypesFolderAttributesByFolder.containsKey("ref"));
    assertTrue(childObjectTypesFolderAttributesByFolder.containsKey("basis"));
    
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
          
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_grouperObjectTypeIncremental");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("grouperObjectTypeIncremental", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }
  
}
