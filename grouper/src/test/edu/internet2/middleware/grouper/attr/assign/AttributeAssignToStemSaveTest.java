package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;

public class AttributeAssignToStemSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignToStemSaveTest("testDeleteAttributeAssignFromStem"));
  }
  
  /**
   * 
   */
  public AttributeAssignToStemSaveTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeAssignToStemSaveTest(String name) {
    super(name);
  }
  
  
  public void testInsertAttributeAssignToStem() {
    
    //given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToStem(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();

    Stem stem0 = new StemSave().assignName("test").save();
    
    AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStem(stem0);
    
    //when
    AttributeAssign attributeAssign = attributeAssignToStemSave.save();
    
    //then
    assertTrue(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
  }
  
  public void testDeleteAttributeAssignFromStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToStem(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();

    Stem stem0 = new StemSave().assignName("test").save();
    
    AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStem(stem0);
    
    AttributeAssign attributeAssign = attributeAssignToStemSave.save();
    assertTrue(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    
    attributeAssignToStemSave.assignSaveMode(SaveMode.DELETE);
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertFalse(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
  }

}
