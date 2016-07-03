/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.SaveMode;


/**
 *
 */
public class AttributeAssignSaveTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignSaveTest("testMarkerGroupWithMarkersOnAssignment"));
    //TestRunner.run(AttributeAssignSaveTest.class);
  }
  
  /**
   * 
   */
  public AttributeAssignSaveTest() {
    super();
    
  }

  /**
   * @param name
   */
  public AttributeAssignSaveTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testMarkerGroupWithMarkersOnAssignment() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDef attributeDefMeta = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarkerMeta")
        .assignToGroupAssn(true).assignMultiAssignable(false)
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
      
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();
    //AttributeDefName attributeDefName1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName1").save();
    
    AttributeDefName attributeDefNameMeta0 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta0").save();
    AttributeDefName attributeDefNameMeta1 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta1").save();
    AttributeDefName attributeDefNameMeta2 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta2").save();
    AttributeDefName attributeDefNameMeta3 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta3").save();
    AttributeDefName attributeDefNameMeta4 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta4").save();

    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").save();

    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0).save();
    assertNotNull(attributeAssignBase);
    new AttributeAssignSave(grouperSession).assignOwnerGroup(group1).assignAttributeDefName(attributeDefName0).save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    new AttributeAssignSave(grouperSession).assignOwnerAttributeAssign(attributeAssignBase).assignAttributeDefName(attributeDefNameMeta0).save();
    new AttributeAssignSave(grouperSession).assignOwnerAttributeAssign(attributeAssignBase).assignAttributeDefName(attributeDefNameMeta1).save();
    new AttributeAssignSave(grouperSession).assignOwnerAttributeAssign(attributeAssignBase).assignAttributeDefName(attributeDefNameMeta2).save();
    
    EhcacheController.ehcacheController().flushCache();

    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    attributeAssignBase = group0.getAttributeDelegate().retrieveAssignment(null, attributeDefName0, false, true);

    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta0));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta1));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta2));
    
    //assign same attributes
    AttributeAssignSave attributeAssignSaveMeta0 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta0);
    AttributeAssignSave attributeAssignSaveMeta1 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta1);
    AttributeAssignSave attributeAssignSaveMeta2 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta2);
    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta1)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta2)
        .assignAttributeDefName(attributeDefName0).save();
    assertNotNull(attributeAssignBase);

    EhcacheController.ehcacheController().flushCache();

    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    attributeAssignBase = group0.getAttributeDelegate().retrieveAssignment(null, attributeDefName0, false, true);

    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta0));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta1));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta2));


    //assign different metadata
    attributeAssignSaveMeta2 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta2);
    AttributeAssignSave attributeAssignSaveMeta3 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta3);
    AttributeAssignSave attributeAssignSaveMeta4 = new AttributeAssignSave(grouperSession).assignAttributeDefName(attributeDefNameMeta4);
    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta2)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta3)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta4)
        .assignAttributeDefName(attributeDefName0).save();
    assertNotNull(attributeAssignBase);

    EhcacheController.ehcacheController().flushCache();

    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    attributeAssignBase = group0.getAttributeDelegate().retrieveAssignment(null, attributeDefName0, false, true);

    assertFalse(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta0));
    assertFalse(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta1));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta2));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta3));
    assertTrue(attributeAssignBase.getAttributeDelegate().hasAttribute(attributeDefNameMeta4));


  }

  /**
   * 
   */
  public void testGroupWithSingleAssignValuesOnAssignment() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false).assignMultiValued(true)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();
    AttributeDefName attributeDefName1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName1").save();
    
    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").save();

    AttributeAssignValue attributeAssignValueA = new AttributeAssignValue();
    attributeAssignValueA.setValueString("A");
    AttributeAssignValue attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    
    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0)
        .addAttributeAssignValue(attributeAssignValueA).addAttributeAssignValue(attributeAssignValueB).save();

    assertNotNull(attributeAssignBase);
    new AttributeAssignSave(grouperSession).assignOwnerGroup(group1).assignAttributeDefName(attributeDefName0).save();
    new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName1).save();
    new AttributeAssignSave(grouperSession).assignOwnerGroup(group1).assignAttributeDefName(attributeDefName1).save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    List<String> values = group0.getAttributeValueDelegate().retrieveValuesString(attributeDefName0.getName());
    assertEquals(2, values.size());
    assertTrue(values.contains("A"));
    assertTrue(values.contains("B"));

    EhcacheController.ehcacheController().flushCache();
    
    //assign same values

    attributeAssignValueA = new AttributeAssignValue();
    attributeAssignValueA.setValueString("A");
    attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    
    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0)
        .addAttributeAssignValue(attributeAssignValueA).addAttributeAssignValue(attributeAssignValueB).save();

    assertNotNull(attributeAssignBase);
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    values = group0.getAttributeValueDelegate().retrieveValuesString(attributeDefName0.getName());
    assertEquals(2, values.size());
    assertTrue(values.contains("A"));
    assertTrue(values.contains("B"));

    EhcacheController.ehcacheController().flushCache();


    //assign different values

    attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    AttributeAssignValue attributeAssignValueC = new AttributeAssignValue();
    attributeAssignValueC.setValueString("C");
    AttributeAssignValue attributeAssignValueC2 = new AttributeAssignValue();
    attributeAssignValueC2.setValueString("C");
    AttributeAssignValue attributeAssignValueD = new AttributeAssignValue();
    attributeAssignValueD.setValueString("D");
    
    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0)
        .addAttributeAssignValue(attributeAssignValueB).addAttributeAssignValue(attributeAssignValueC).addAttributeAssignValue(attributeAssignValueC2)
        .addAttributeAssignValue(attributeAssignValueD).save();

    assertNotNull(attributeAssignBase);
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    values = group0.getAttributeValueDelegate().retrieveValuesString(attributeDefName0.getName());
    assertEquals(4, values.size());
    assertFalse(values.contains("A"));
    assertTrue(values.contains("B"));
    assertTrue(values.contains("C"));
    assertTrue(values.contains("D"));

    EhcacheController.ehcacheController().flushCache();


  }

  /**
   * 
   */
  public void testGroupWithMultipleAssignValuesOnAssignmentOfAssignment() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(true).assignMultiValued(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();
    
    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();

    AttributeDef attributeDefMeta = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarkerMeta")
        .assignToGroupAssn(true).assignMultiAssignable(false)
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string).save();
      
    AttributeDefName attributeDefNameMeta0 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta0").save();
    AttributeDefName attributeDefNameMeta1 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta1").save();
    AttributeDefName attributeDefNameMeta2 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta2").save();
    AttributeDefName attributeDefNameMeta3 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta3").save();
    //AttributeDefName attributeDefNameMeta4 = new AttributeDefNameSave(grouperSession, attributeDefMeta).assignName("test:attributeDefNameMeta4").save();

    AttributeAssignValue attributeAssignValueA = new AttributeAssignValue();
    attributeAssignValueA.setValueString("A");
    AttributeAssignValue attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    AttributeAssignValue attributeAssignValueC = new AttributeAssignValue();
    attributeAssignValueC.setValueString("C");
    AttributeAssignValue attributeAssignValueD = new AttributeAssignValue();
    attributeAssignValueD.setValueString("D");
    AttributeAssignValue attributeAssignValueE = new AttributeAssignValue();
    attributeAssignValueE.setValueString("E");

    // group->attr(0)->meta0->A
    // group->attr(0)->meta1->B
    // group->attr(1)->meta2->C
    // group->attr(1)->meta3->D
    AttributeAssignSave attributeAssignSaveMeta0 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta0).addAttributeAssignValue(attributeAssignValueA);
    AttributeAssignSave attributeAssignSaveMeta1 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta1).addAttributeAssignValue(attributeAssignValueB);

    AttributeAssign attributeAssignBase0 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta1).save();

    AttributeAssignSave attributeAssignSaveMeta2 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta2).addAttributeAssignValue(attributeAssignValueC);
    AttributeAssignSave attributeAssignSaveMeta3 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta3).addAttributeAssignValue(attributeAssignValueD);

    AttributeAssign attributeAssignBase1 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta2)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta3).assignSaveMode(SaveMode.INSERT).save();
    
    assertNotNull(attributeAssignBase0);
    assertEquals(2, group0.getAttributeDelegate().retrieveAssignments(attributeDefName0).size());

    assertEquals(2, attributeAssignBase0.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("A", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta0.getName()));
    assertEquals("B", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta1.getName()));
    assertEquals(2, attributeAssignBase1.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("C", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta2.getName()));
    assertEquals("D", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta3.getName()));

    EhcacheController.ehcacheController().flushCache();
    
    //assign same values

    attributeAssignValueA = new AttributeAssignValue();
    attributeAssignValueA.setValueString("A");
    attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    attributeAssignValueC = new AttributeAssignValue();
    attributeAssignValueC.setValueString("C");
    attributeAssignValueD = new AttributeAssignValue();
    attributeAssignValueD.setValueString("D");
    attributeAssignValueE = new AttributeAssignValue();
    attributeAssignValueE.setValueString("E");

    // group->attr(0)->meta0->A
    // group->attr(0)->meta1->B
    // group->attr(1)->meta2->C
    // group->attr(1)->meta3->D

    attributeAssignSaveMeta0 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta0).addAttributeAssignValue(attributeAssignValueA);
    attributeAssignSaveMeta1 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta1).addAttributeAssignValue(attributeAssignValueB);

    attributeAssignBase0 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta1).save();

    attributeAssignSaveMeta2 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta2).addAttributeAssignValue(attributeAssignValueC);
    attributeAssignSaveMeta3 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta3).addAttributeAssignValue(attributeAssignValueD);

    attributeAssignBase1 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta2)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta3).save();
    
    assertNotNull(attributeAssignBase0);
    assertEquals(2, group0.getAttributeDelegate().retrieveAssignments(attributeDefName0).size());

    assertEquals(2, attributeAssignBase0.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("A", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta0.getName()));
    assertEquals("B", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta1.getName()));
    assertEquals(2, attributeAssignBase1.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("C", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta2.getName()));
    assertEquals("D", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta3.getName()));

    EhcacheController.ehcacheController().flushCache();
    

    //assign different values

    attributeAssignValueA = new AttributeAssignValue();
    attributeAssignValueA.setValueString("A");
    attributeAssignValueB = new AttributeAssignValue();
    attributeAssignValueB.setValueString("B");
    attributeAssignValueC = new AttributeAssignValue();
    attributeAssignValueC.setValueString("C");
    attributeAssignValueD = new AttributeAssignValue();
    attributeAssignValueD.setValueString("D");
    attributeAssignValueE = new AttributeAssignValue();
    attributeAssignValueE.setValueString("E");
    AttributeAssignValue attributeAssignValueF = new AttributeAssignValue();
    attributeAssignValueF.setValueString("F");

    // group->attr(0)->meta0->B
    // group->attr(0)->meta1->E
    // group->attr(0)->meta2->F
    // group->attr(1)->meta3->D
    // group->attr(1)->meta4->E

    attributeAssignSaveMeta0 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta0).addAttributeAssignValue(attributeAssignValueB);
    attributeAssignSaveMeta1 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta1).addAttributeAssignValue(attributeAssignValueE);
    attributeAssignSaveMeta2 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta2).addAttributeAssignValue(attributeAssignValueF);

    attributeAssignBase0 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta0)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta1)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta2).save();

    attributeAssignSaveMeta3 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta2).addAttributeAssignValue(attributeAssignValueD);
    AttributeAssignSave attributeAssignSaveMeta4 = new AttributeAssignSave(grouperSession)
      .assignAttributeDefName(attributeDefNameMeta3).addAttributeAssignValue(attributeAssignValueE);

    attributeAssignBase1 = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0)
        .assignAttributeDefName(attributeDefName0).addAttributeAssignOnThisAssignment(attributeAssignSaveMeta3)
        .addAttributeAssignOnThisAssignment(attributeAssignSaveMeta4).save();
    
    assertNotNull(attributeAssignBase0);
    assertEquals(2, group0.getAttributeDelegate().retrieveAssignments(attributeDefName0).size());

    assertEquals(3, attributeAssignBase0.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("B", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta0.getName()));
    assertEquals("E", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta1.getName()));
    assertEquals("F", attributeAssignBase0.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta2.getName()));
    assertEquals(2, attributeAssignBase1.getAttributeDelegate().retrieveAssignments().size());
    assertEquals("D", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta2.getName()));
    assertEquals("E", attributeAssignBase1.getAttributeValueDelegate().retrieveValueString(attributeDefNameMeta3.getName()));

    EhcacheController.ehcacheController().flushCache();
    

  }

  
  /**
   * 
   */
  public void testSingleMarkerGroup() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:attrDefMarker")
      .assignToGroup(true).assignMultiAssignable(false)
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
    
    AttributeDefName attributeDefName0 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName0").save();
    AttributeDefName attributeDefName1 = new AttributeDefNameSave(grouperSession, attributeDef).assignName("test:attributeDefName1").save();

    Group group0 = new GroupSave(grouperSession).assignName("test:group0").save();
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").save();
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0).save();
    assertNotNull(attributeAssign);
    new AttributeAssignSave(grouperSession).assignOwnerGroup(group1).assignAttributeDefName(attributeDefName0).save();
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    EhcacheController.ehcacheController().flushCache();

    //assign same attribute
    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName0).save();
    assertNotNull(attributeAssign);
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    EhcacheController.ehcacheController().flushCache();


    //assign a different attribute
    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName1).save();
    assertNotNull(attributeAssign);
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName1));

    EhcacheController.ehcacheController().flushCache();

    //set the disabled date
    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attributeDefName1).assignDisabledTime(30L).save();
    assertNotNull(attributeAssign);

    assertEquals(attributeAssign.getDisabledTime().getTime(), 30L);
    
    assertTrue(group0.getAttributeDelegate().hasAttribute(attributeDefName0));

    EhcacheController.ehcacheController().flushCache();

    

  }
  
}
