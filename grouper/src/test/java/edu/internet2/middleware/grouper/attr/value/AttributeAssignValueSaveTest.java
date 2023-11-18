package edu.internet2.middleware.grouper.attr.value;

import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import junit.textui.TestRunner;

public class AttributeAssignValueSaveTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueSaveTest("testAttributeAssignValueAddValueMultivalueDef"));
  }
  
  /**
   * 
   */
  public AttributeAssignValueSaveTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignValueSaveTest(String name) {
    super(name);
  }
  
  public void testAttributeAssignValueLookupByAttributeAssign() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignValueSave attributeAssignValueSave = new AttributeAssignValueSave();
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign)
      .assignValue("hello").save();
    
    Assert.assertEquals("hello", attributeAssignValueResult.getAttributeAssignValue().getValue());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignValueSave.getSaveResultType());
    
  }
  
  public void testAttributeAssignValueLookupByAttributeAssignId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignValueResult attributeAssignValueResult = new AttributeAssignValueSave().assignAttributeAssignId(attributeAssign.getId())
      .assignValue("hello").save();

    
    Assert.assertEquals("hello", attributeAssignValueResult.getAttributeAssignValue().getValue());
    
  }
  
  public void testAttributeAssignValueRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    AttributeAssignValueResult attributeAssignValueResult = new AttributeAssignValueSave()
        .assignAttributeAssignId(attributeAssign.getId())
        .assignValue("hello")
        .assignRunAsRoot(true)
        .save();
    
    Assert.assertEquals("hello", attributeAssignValueResult.getAttributeAssignValue().getValue());
    
  }
  
  public void testAttributeAssignValueDoNotRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      new AttributeAssignValueSave()
          .assignAttributeAssignId(attributeAssign.getId())
          .assignValue("hello")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testAttributeAssignValueNullValue() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    boolean exceptionThrown = false;
    try {
      new AttributeAssignValueSave()
          .assignAttributeAssignId(attributeAssign.getId())
          .assignValue(null)
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testAttributeAssignValueNotAValidAttributeAssignId() {
    
    boolean exceptionThrown = false;
    try {
      new AttributeAssignValueSave()
          .assignAttributeAssignId("non_existent_attribute_assign_id")
          .assignValue("hello")
          .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testAttributeAssignValueRemoveValue() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignValueSave attributeAssignValueSave = new AttributeAssignValueSave();
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign)
      .assignValue("hello").save();
    
    Assert.assertEquals("hello", attributeAssignValueResult.getAttributeAssignValue().getValue());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignValueSave.getSaveResultType());
    
    // now remove the same attribute assign
    attributeAssignValueSave = new AttributeAssignValueSave();
    attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign)
       .assignAttributeAssignValueOperation(AttributeAssignValueOperation.remove_value)
      .assignValue("hello").save();
    
    Assert.assertEquals(SaveResultType.DELETE, attributeAssignValueSave.getSaveResultType());
    
  }
  
  public void testAttributeAssignValueAddValueMultivalueDef() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    AttributeDef attributeDef = stem.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.setAssignToStem(true);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(stem, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignValueSave attributeAssignValueSave = new AttributeAssignValueSave();
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign)
      .assignValue("hello").save();
    
    Assert.assertEquals("hello", attributeAssignValueResult.getAttributeAssignValue().getValue());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignValueSave.getSaveResultType());
    
    attributeAssignValueSave = new AttributeAssignValueSave();
    attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign)
      .assignValue("world").save();
    
    Assert.assertEquals("world", attributeAssignValueResult.getAttributeAssignValue().getValue());
    Assert.assertEquals(SaveResultType.INSERT, attributeAssignValueSave.getSaveResultType());
    
    Set<AttributeAssignValue> attributeAssignValues = stem.getAttributeDelegate().getAttributeAssigns().iterator().next().getValueDelegate().getAttributeAssignValues();
    Assert.assertEquals(2, attributeAssignValues.size());
    
  }
  
  
}
