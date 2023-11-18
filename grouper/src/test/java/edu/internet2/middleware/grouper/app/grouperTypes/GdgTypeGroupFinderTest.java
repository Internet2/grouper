package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class GdgTypeGroupFinderTest extends GrouperTest {
  
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
  public GdgTypeGroupFinderTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GdgTypeGroupFinderTest("testFindGdgTypeGroupSubjectDoesNotHaveProperPermissions"));
  }

  public void testFindGdgTypeGroupLookupByGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroup(group).assignType("ref").findGdgTypeGroupAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeGroupLookupByGroupId() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroupId(group.getId()).assignType("ref").findGdgTypeGroupAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeGroupLookupByGroupName() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroupName(group.getName()).assignType("ref").findGdgTypeGroupAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeGroupReturnsNull() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroupName(group.getName()).assignType("ref").findGdgTypeGroupAssignment();
    
    Assert.assertNull(attributeValue);
    
  }
  
  public void testFindGdgTypeGroupReturnsMultiple() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("app")
        .save();
    
    Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeGroupFinder().assignGroup(group).findGdgTypeGroupAssignments();
    
    Assert.assertEquals(2, attributeValues.size());
    
  }
  
  public void testFindGdgTypeGroupDirectOptions() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(false));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "app");
    
    attributeAssign.saveOrUpdate();
    
    Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeGroupFinder().assignGroup(group).assignDirectAssignment(true).findGdgTypeGroupAssignments();
    
    Assert.assertEquals(1, attributeValues.size());
    
    Assert.assertTrue(attributeValues.iterator().next().isDirectAssignment());
    
    attributeValues = new GdgTypeGroupFinder().assignGroup(group).assignDirectAssignment(null).findGdgTypeGroupAssignments();
    
    Assert.assertEquals(2, attributeValues.size());
    
    attributeValues = new GdgTypeGroupFinder().assignGroup(group).assignDirectAssignment(false).findGdgTypeGroupAssignments();
    
    Assert.assertEquals(1, attributeValues.size());
    Assert.assertFalse(attributeValues.iterator().next().isDirectAssignment());
  }
  
  
  public void testFindGdgTypeGroupInvalidType() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    boolean exceptionThrown = false;
    try {
      new GdgTypeGroupFinder().assignGroup(group).assignType("invalid_type").findGdgTypeGroupAssignment();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid types"));
    }

    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testFindGdgTypeGroupSubjectHasReadAccessOnTheGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroup(group).assignType("ref").findGdgTypeGroupAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeGroupRunAsRoot() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeGroupFinder().assignGroup(group).assignType("ref").assignRunAsRoot(true).findGdgTypeGroupAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeGroupSubjectDoesNotHaveProperPermissions() {
    
    Group group = new GroupSave().assignName("test:test-group1").assignCreateParentStemsIfNotExist(true).save();
    
    group.revokePriv(SubjectTestHelper.SUBJA, AccessPrivilege.READ);
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      new GdgTypeGroupFinder().assignGroup(group).assignType("ref").findGdgTypeGroupAssignment();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);
    
  }

}
