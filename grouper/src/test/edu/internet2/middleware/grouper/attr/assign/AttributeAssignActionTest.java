/**
 * @author mchyzer
 * $Id: AttributeAssignActionTest.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;


/**
 *
 */
public class AttributeAssignActionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignActionTest("testXmlInsert"));
  }
  
  /**
   * 
   * @param name
   */
  public AttributeAssignActionTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testHibernate() {

    GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top display name");
    AttributeDef attributeDef = top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setId(GrouperUuid.getUuid());
    attributeAssignAction.setAttributeDefId(attributeDef.getId());

    attributeAssignAction.save();
    
    attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignAction.getId(), true);
    
    attributeAssignAction.delete();

    attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignAction.getId(), false);
    
    assertNull(attributeAssignAction);
  }
  
  /**
   * make an example AttributeAssignAction for testing
   * @return an example AttributeAssignAction
   */
  public static AttributeAssignAction exampleAttributeAssignAction() {
    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setAttributeDefId("attributeDefId");
    attributeAssignAction.setContextId("contextId");
    attributeAssignAction.setCreatedOnDb(new Long(4L));
    attributeAssignAction.setHibernateVersionNumber(3L);
    attributeAssignAction.setId("id");
    attributeAssignAction.setLastUpdatedDb(new Long(7L));
    attributeAssignAction.setNameDb("name");
    
    return attributeAssignAction;
  }
  
  /**
   * make an example attributeAssignAction from db for testing
   * @return an example attributeAssignAction
   */
  public static AttributeAssignAction exampleAttributeAssignActionDb() {
    return exampleAttributeAssignActionDb("testAction");
  }
  
  /**
   * make an example attributeAssignAction from db for testing
   * @param actionName 
   * @return an example attributeAssignAction
   */
  public static AttributeAssignAction exampleAttributeAssignActionDb(String actionName) {
    
    AttributeDef attributeDef = AttributeDefFinder.findByName("test:attributeDefAction", false);

    if (attributeDef == null) {
      Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test").assignName("test").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
      attributeDef = stem.addChildAttributeDef("attributeDefAction", AttributeDefType.attr);
    }
    
    attributeDef.getAttributeDefActionDelegate().addAction(actionName);
    
    return GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(null, attributeDef.getId(), actionName, true);
    
  }

  /**
   * retrieve example AttributeAssignAction from db for testing
   * @return an example AttributeAssignAction
   */
  public static AttributeAssignAction exampleRetrieveAttributeAssignActionDb() {
    return exampleRetrieveAttributeAssignActionDb("testAction");
  }
  
  /**
   * retrieve example AttributeAssignAction from db for testing
   * @param actionName 
   * @return an example AttributeAssignAction
   */
  public static AttributeAssignAction exampleRetrieveAttributeAssignActionDb(String actionName) {
    AttributeDef attributeDef = AttributeDefFinder.findByName("test:attributeDefAction", true);
    return GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(null, attributeDef.getId(), actionName, true);
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeAssignAction attributeAssignActionOriginal =  exampleAttributeAssignActionDb("testInsert");
    
    //do this because last membership update isnt there, only in db
    attributeAssignActionOriginal = exampleRetrieveAttributeAssignActionDb("testInsert");
    AttributeAssignAction attributeAssignActionCopy = exampleRetrieveAttributeAssignActionDb("testInsert");
    AttributeAssignAction attributeAssignActionCopy2 = exampleRetrieveAttributeAssignActionDb("testInsert");
    attributeAssignActionCopy.delete();
    
    //lets insert the original
    attributeAssignActionCopy2.xmlSaveBusinessProperties(null);
    attributeAssignActionCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignActionCopy = exampleRetrieveAttributeAssignActionDb("testInsert");
    
    assertFalse(attributeAssignActionCopy == attributeAssignActionOriginal);
    assertFalse(attributeAssignActionCopy.xmlDifferentBusinessProperties(attributeAssignActionOriginal));
    assertFalse(attributeAssignActionCopy.xmlDifferentUpdateProperties(attributeAssignActionOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssignAction attributeAssignAction = null;
    AttributeAssignAction exampleAttributeAssignAction = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      attributeAssignAction.setContextId("abc");
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setContextId(exampleAttributeAssignAction.getContextId());
      attributeAssignAction.xmlSaveUpdateProperties();

      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
      
    }
    
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setCreatedOnDb(99L);
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setCreatedOnDb(exampleAttributeAssignAction.getCreatedOnDb());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    }
    
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setLastUpdatedDb(exampleAttributeAssignAction.getLastUpdatedDb());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

    }

    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setHibernateVersionNumber(exampleAttributeAssignAction.getHibernateVersionNumber());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setAttributeDefId("abc");
      
      assertTrue(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setAttributeDefId(exampleAttributeAssignAction.getAttributeDefId());
      attributeAssignAction.xmlSaveBusinessProperties(exampleAttributeAssignAction.clone());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setId("abc");
      
      assertTrue(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setId(exampleAttributeAssignAction.getId());
      attributeAssignAction.xmlSaveBusinessProperties(exampleAttributeAssignAction.clone());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignAction = exampleAttributeAssignActionDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionDb();

      attributeAssignAction.setNameDb("abc");
      
      assertTrue(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignAction.setNameDb(exampleAttributeAssignAction.getNameDb());
      attributeAssignAction.xmlSaveBusinessProperties(exampleAttributeAssignAction.clone());
      attributeAssignAction.xmlSaveUpdateProperties();
      
      attributeAssignAction = exampleRetrieveAttributeAssignActionDb();
      
      assertFalse(attributeAssignAction.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignAction.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
  }


}
