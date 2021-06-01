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
    TestRunner.run(new AttributeAssignToStemSaveTest("testMultiAssignabeAttributeDef"));
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
  
  public void testMultiAssignabeAttributeDef() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // multiassignable attribute def
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
        .assignToStem(true).assignMultiAssignable(true)
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
      
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();

    Stem stem0 = new StemSave().assignName("test").save();
    
    AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStem(stem0);
    
    AttributeAssign attributeAssign = attributeAssignToStemSave.save();
    
    assertTrue(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStem(stem0);
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertEquals(2, stem0.getAttributeDelegate().getAttributeAssigns().size());
    
    // non multiassignable attribute def
    attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1:attrDefMarker")
        .assignToStem(true).assignMultiAssignable(false)
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
      
    AttributeDefName attributeDefName2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test1:attributeDefName2").save();
    
    Stem stem1 = new StemSave().assignName("test1").save();
    
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName2).assignStem(stem1);
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertTrue(stem1.getAttributeDelegate().hasAttribute(attributeDefName2));
    assertEquals(attributeDefName2.getId(), attributeAssign.getAttributeDefNameId());
    
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName2).assignStem(stem1);
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertEquals(1, stem1.getAttributeDelegate().getAttributeAssigns().size());
    
  }
  
  public void testInsertAttributeAssignToStem() {
    
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
    
    stem0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    //only stem id is given
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStemId(stem0.getId());
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertTrue(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    stem0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    //only stem name is given
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStemName(stem0.getName());
    
    attributeAssign = attributeAssignToStemSave.save();
    
    assertTrue(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    stem0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    // stem not found
    attributeAssignToStemSave = new AttributeAssignToStemSave();
    attributeAssignToStemSave.assignAttributeDefName(attributeDefName0).assignStemName("not_existent_stem");
    
    try {      
      attributeAssign = attributeAssignToStemSave.save();
      fail();
    } catch(Exception e) {
      assertTrue(true);
    }
    
    
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
    
    AttributeAssignToStemSave attributeAssignToStemSave1 = new AttributeAssignToStemSave();
    attributeAssignToStemSave1.assignAttributeDefName(attributeDefName0).assignStem(stem0);
    attributeAssignToStemSave1.assignSaveMode(SaveMode.DELETE);
    
    attributeAssign = attributeAssignToStemSave1.save();
    
    assertFalse(stem0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
  }

}
