package edu.internet2.middleware.grouper.attr.assign;

import org.junit.Assert;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import junit.textui.TestRunner;

public class AttributeAssignToAssignmentSaveTest extends GrouperTest {
  
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignToAssignmentSaveTest("testSaveAttributeDefNameToAttributeAssignAttributeDefMultiassignable"));
  }
  
  /**
   * 
   */
  public AttributeAssignToAssignmentSaveTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignToAssignmentSaveTest(String name) {
    super(name);
  }
  
  public void testSaveAttributeDefNameToAttributeAssignLookupByAttributeAssign() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .save();
      
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignLookupByAttributeAssignId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave
      .assignAttributeAssignId(attributeAssign.getId())
      .assignAttributeDefName(attributeDefName)
      .save();
      
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignLookupByAttributeDefNameName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave
      .assignAttributeAssignId(attributeAssign.getId())
      .assignAttributeDefNameName(attributeDefName.getName())
      .save();
      
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignLookupByAttributeDefNameId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave
      .assignAttributeAssignId(attributeAssign.getId())
      .assignAttributeDefNameId(attributeDefName.getId())
      .save();
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .assignRunAsRoot(true)
      .save();
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignDoNotRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
      attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
        .assignAttributeDefName(attributeDefName)
        .assignRunAsRoot(false)
        .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
  }
  
  public void testSaveAttributeDefNameToAttributeAssignInvalidAttributeAssign() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    boolean exceptionThrown = false;
    
    try {
      AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
      attributeAssignToAssignmentSave.assignAttributeAssignId("non_existent_attribute_assign_id")
        .assignAttributeDefName(attributeDefName)
        .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
  }
  
  public void testSaveAttributeDefNameToAttributeAssignInvalidAttributeDefName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    boolean exceptionThrown = false;
    
    try {
      AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
      attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
        .assignAttributeDefNameName("non_existent_attribute_def_name")
        .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
  }
  
  public void testRemoveAttributeDefNameFromAttributeAssign() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .save();
      
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
    //now remove attribute def name
    attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .assignAttributeAssignOperation(AttributeAssignOperation.remove_attr)
      .save();
    
    Assert.assertEquals(SaveResultType.DELETE, attributeAssignToAssignmentSave.getSaveResultType());
    
  }
  
  public void testSaveAttributeDefNameToAttributeAssignAttributeDefMultiassignable() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.setMultiAssignable(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .assignAttributeAssignOperation(AttributeAssignOperation.add_attr)
      .save();
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
    attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave();
    attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign)
      .assignAttributeDefName(attributeDefName)
      .assignAttributeAssignOperation(AttributeAssignOperation.add_attr)
      .save();
    
    Assert.assertEquals(attributeDefName.getId(), attributeAssignResult.getAttributeAssign().getAttributeDefNameId());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignToAssignmentSave.getSaveResultType());
    
  }

}
