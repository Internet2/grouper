package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.Set;

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
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class GdgTypeStemFinderTest extends GrouperTest {
  
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
  public GdgTypeStemFinderTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GdgTypeStemFinderTest("testFindGdgTypeStemSubjectDoesNotHaveProperPermissions"));
  }

  public void testFindGdgTypeStemLookupByStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStem(stem).assignType("ref").findGdgTypeStemAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeStemLookupByStemId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStemId(stem.getId()).assignType("ref").findGdgTypeStemAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeStemLookupByStemName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStemName(stem.getName()).assignType("ref").findGdgTypeStemAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeStemReturnsNull() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStemName(stem.getName()).assignType("ref").findGdgTypeStemAssignment();
    
    Assert.assertNull(attributeValue);
    
  }
  
  public void testFindGdgTypeStemReturnsMultiple() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("app")
        .save();
    
    Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeStemFinder().assignStem(stem).findGdgTypeStemAssignments();
    
    Assert.assertEquals(2, attributeValues.size());
    
  }
  
  public void testFindGdgTypeStemDirectOptions() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(false));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "app");
    
    attributeAssign.saveOrUpdate();
    
    Set<GrouperObjectTypesAttributeValue> attributeValues = new GdgTypeStemFinder().assignStem(stem).assignDirectAssignment(true).findGdgTypeStemAssignments();
    
    Assert.assertEquals(1, attributeValues.size());
    
    Assert.assertTrue(attributeValues.iterator().next().isDirectAssignment());
    
    attributeValues = new GdgTypeStemFinder().assignStem(stem).assignDirectAssignment(null).findGdgTypeStemAssignments();
    
    Assert.assertEquals(2, attributeValues.size());
    
    attributeValues = new GdgTypeStemFinder().assignStem(stem).assignDirectAssignment(false).findGdgTypeStemAssignments();
    
    Assert.assertEquals(1, attributeValues.size());
    Assert.assertFalse(attributeValues.iterator().next().isDirectAssignment());
  }
  
  
  public void testFindGdgTypeStemInvalidType() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    boolean exceptionThrown = false;
    try {
      new GdgTypeStemFinder().assignStem(stem).assignType("invalid_type").findGdgTypeStemAssignment();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid types"));
    }

    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testFindGdgTypeStemSubjectHasAdminAccessOnTheStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN);
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStem(stem).assignType("ref").findGdgTypeStemAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeStemRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    GrouperObjectTypesAttributeValue attributeValue = new GdgTypeStemFinder().assignStem(stem).assignType("ref").assignRunAsRoot(true).findGdgTypeStemAssignment();
    
    Assert.assertEquals("ref", attributeValue.getObjectTypeName());
    Assert.assertEquals("do", attributeValue.getObjectTypeDataOwner());
    Assert.assertEquals("md", attributeValue.getObjectTypeMemberDescription());
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindGdgTypeStemSubjectDoesNotHaveProperPermissions() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
    gdgTypeStemSave
        .assignStem(stem)
        .assignType("ref")
        .assignDataOwner("do")
        .assignMemberDescription("md")
        .save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      new GdgTypeStemFinder().assignStem(stem).assignType("ref").findGdgTypeStemAssignment();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);
    
  }

}
