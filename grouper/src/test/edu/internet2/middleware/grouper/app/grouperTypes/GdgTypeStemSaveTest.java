package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DATA_OWNER;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import org.junit.Assert;

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
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class GdgTypeStemSaveTest extends GrouperTest {
  
  
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
  public GdgTypeStemSaveTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GdgTypeStemSaveTest("testSaveGdgTypeStemNoChange"));
  }

  
  public void testSaveGdgTypeStemLookupByStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemNoChange() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeStemSave = new GdgTypeStemSave();
    grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .save();
    
    Assert.assertEquals(SaveResultType.NO_CHANGE, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemLookupByStemName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStemName(stem.getName())
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemLookupByStemId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStemId(stem.getId())
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemButStemNotFound() {
    
    boolean exceptionThrown = false;
    try {
      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave
          .assignStemId("non_existent_stem_id")
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
  
  public void testSaveGdgTypeStemRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignRunAsRoot(true)
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemSubjectHasAdminAccessOnStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveGdgTypeStemSubjectDoesNotHaveAdminAccessOnStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave
          .assignStem(stem)
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
  
  public void testSaveGdgTypeStemDataOwnerOrMemberDescriptionCanNotBeAssigned() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave
          .assignStem(stem)
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
  
  public void testSaveGdgTypeStemInvalidType() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave
          .assignStem(stem)
          .assignType("random_type")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid inputs"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveGdgTypeStemTypeNotAssigned() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    
    try {
      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave
          .assignStem(stem)
          .assignType(null)
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("type is required"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testDeleteGdgTypeStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeStemSave = new GdgTypeStemSave();
    grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignSaveMode(SaveMode.DELETE)
        .save();
    
    Assert.assertEquals(SaveResultType.DELETE, gdgTypeStemSave.getSaveResultType());
    Assert.assertNull(grouperObjectTypesAttributeValue);
    
  }
  
  public void testCopyConfigFromParentOnAStem() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ref");
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "do");
    
    attributeAssign.saveOrUpdate();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem1)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeStemSave = new GdgTypeStemSave();
    grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem1)
        .assignType("ref")
        .assignSaveMode(SaveMode.DELETE)
        .save();
    
    Assert.assertEquals(SaveResultType.DELETE, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals(stem.getId(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    Assert.assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
  public void testUpdateGdgTypeStemOnlyOneAttribute() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    gdgTypeStemSave = new GdgTypeStemSave();
    grouperObjectTypesAttributeValue = gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignReplaceAllSettings(false)
        .assignDataOwner("do1")
        .save();
    
    Assert.assertEquals(SaveResultType.UPDATE, gdgTypeStemSave.getSaveResultType());
    Assert.assertEquals("ref", grouperObjectTypesAttributeValue.getObjectTypeName());
    Assert.assertEquals("do1", grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(grouperObjectTypesAttributeValue.isDirectAssignment());
    
  }
  
}
