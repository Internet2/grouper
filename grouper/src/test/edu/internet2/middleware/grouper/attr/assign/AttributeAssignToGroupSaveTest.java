package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;

public class AttributeAssignToGroupSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignToGroupSaveTest("testDeleteAttributeAssignFromGroup"));
  }
  
  /**
   * 
   */
  public AttributeAssignToGroupSaveTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeAssignToGroupSaveTest(String name) {
    super(name);
  }
  
  
  public void testInsertAttributeAssignToGroup() {
    
    //given
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();

    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    
    AttributeAssignToGroupSave attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroup(group0);
    
    //when
    AttributeAssign attributeAssign = attributeAssignToGroupSave.save();
    
    //then
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
  }
  
  public void testDeleteAttributeAssignFromGroup() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();

    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    
    AttributeAssignToGroupSave attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroup(group0);
    
    AttributeAssign attributeAssign = attributeAssignToGroupSave.save();
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    
    attributeAssignToGroupSave.assignSaveMode(SaveMode.DELETE);
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertFalse(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
  }

}
