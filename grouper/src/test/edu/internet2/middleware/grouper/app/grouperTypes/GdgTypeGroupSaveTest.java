package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DATA_OWNER;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class GdgTypeGroupSaveTest extends GrouperTest {
  
  
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
  public GdgTypeGroupSaveTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GdgTypeGroupSaveTest("testSaveGdgTypeGroupNoChange"));
  }

  
  public void testSaveGdgTypeGroupLookupByGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupNoChange() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    gdgTypeGroupSave = new GdgTypeGroupSave();
    grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignReplaceAllSettings(false)
        .assignDataOwner("do")
        .save();
    
    Assert.assertEquals(SaveResultType.NO_CHANGE, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupLookupByGroupName() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroupName(group.getName())
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupLookupByGroupId() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroupId(group.getId())
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupButGroupNotFound() {
    
    boolean exceptionThrown = false;
    try {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave
          .assignGroupId("non_existent_group_id")
          .assignType("ref")
          .assignDataOwner("do")
          .assignMemberDescription("md")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveGdgTypeGroupRunAsRoot() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignRunAsRoot(true)
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupSubjectHasAdminAccessOnGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeGroupSubjectDoesNotHaveAdminAccessOnGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave
          .assignGroup(group)
          .assignType("ref")
          .assignDataOwner("do")
          .assignMemberDescription("md")
          .save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveGdgTypeGroupDataOwnerOrMemberDescriptionCanNotBeAssigned() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave
          .assignGroup(group)
          .assignType("app")
          .assignDataOwner("do")
          .assignMemberDescription("md")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("dataOwner and memberDescription cannot be assigned"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveGdgTypeGroupInvalidType() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave
          .assignGroup(group)
          .assignType("random_type")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid inputs"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveGdgTypeGroupTypeNotAssigned() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave
          .assignGroup(group)
          .assignType(null)
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("type is required"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testDeleteGdgTypeGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeGroupSave = new GdgTypeGroupSave();
    grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignSaveMode(SaveMode.DELETE)
        .save();
    
    Assert.assertEquals(SaveResultType.DELETE, gdgTypeGroupSave.getSaveResultType());
    Assert.assertNull(grouperObjectTypesAttributeValue);
    
  }
  
  public void testCopyConfigFromParentOnAGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ref");
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "do");
    
    attributeAssign.saveOrUpdate();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeGroupSave = new GdgTypeGroupSave();
    grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignSaveMode(SaveMode.DELETE)
        .save();
    
    Assert.assertEquals(SaveResultType.DELETE, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals(stem.getId(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    Assert.assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testUpdateGdgTypeGroupOnlyOneAttribute() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeGroupSave = new GdgTypeGroupSave();
    grouperObjectTypesAttributeValue = gdgTypeGroupSave
        .assignGroup(group)
        .assignType("ref")
        .assignReplaceAllSettings(false)
        .assignDataOwner("do1")
        .save();
    
    Assert.assertEquals(SaveResultType.UPDATE, gdgTypeGroupSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do1", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
}
