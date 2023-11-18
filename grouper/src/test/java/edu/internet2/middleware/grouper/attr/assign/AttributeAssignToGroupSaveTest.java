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
    TestRunner.run(new AttributeAssignToGroupSaveTest("testMultiAssignabeAttributeDef"));
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
  
  public void testMultiAssignabeAttributeDef() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // multiassignable attribute def
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(true)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();
    
    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    
    AttributeAssignToGroupSave attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroup(group0);
    
    AttributeAssign attributeAssign = attributeAssignToGroupSave.save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroup(group0);
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertEquals(2, group0.getAttributeDelegate().getAttributeAssigns().size());
    
    // non multiassignable attribute def
    attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName2 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test1:attributeDefName2").save();
    
    Group group1 = new GroupSave(grouperSession).assignName("test1:group1").save();
    
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName2).assignGroup(group1);
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertTrue(group1.getAttributeDelegate().hasAttribute(attributeDefName2));
    assertEquals(attributeDefName2.getId(), attributeAssign.getAttributeDefNameId());
    
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName2).assignGroup(group1);
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertEquals(1, group1.getAttributeDelegate().getAttributeAssigns().size());
    
  }
  
  public void testInsertAttributeAssignToGroup() {
    
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
    
    group0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    //only group id is given
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroupId(group0.getId());
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    group0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    //only group name is given
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroupName(group0.getName());
    
    attributeAssign = attributeAssignToGroupSave.save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertEquals(attributeDefName0.getId(), attributeAssign.getAttributeDefNameId());
    
    group0.getAttributeDelegate().removeAttribute(attributeDefName0);
    assertFalse(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
    // group not found
    attributeAssignToGroupSave = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave.assignAttributeDefName(attributeDefName0).assignGroupName("not_existent_group");
    
    try {      
      attributeAssign = attributeAssignToGroupSave.save();
      fail();
    } catch(Exception e) {
      assertTrue(true);
    }
    
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
    
    AttributeAssignToGroupSave attributeAssignToGroupSave1 = new AttributeAssignToGroupSave();
    attributeAssignToGroupSave1.assignAttributeDefName(attributeDefName0).assignGroup(group0);
    attributeAssignToGroupSave1.assignSaveMode(SaveMode.DELETE);
    
    attributeAssign = attributeAssignToGroupSave1.save();
    
    assertFalse(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    
  }

}
