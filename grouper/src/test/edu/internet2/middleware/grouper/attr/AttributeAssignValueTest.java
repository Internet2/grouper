/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import junit.textui.TestRunner;

import org.hibernate.exception.SQLGrammarException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeAssignValueTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueTest("testHibernate"));
  }
  
  /**
   * 
   */
  public AttributeAssignValueTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignValueTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate();
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue.setValueString("hello");
    attributeAssignValue.setId(GrouperUuid.getUuid());
    try {
      attributeAssignValue.saveOrUpdate();
    } catch (RuntimeException e) {
      
      Throwable cause = e.getCause();
      if (cause instanceof SQLGrammarException) {
        cause = cause.getCause();
      }
      if (cause instanceof BatchUpdateException) {
        SQLException sqlException = ((BatchUpdateException)cause).getNextException();
        if (sqlException != null) {
          sqlException.printStackTrace();
        }
      }
      
      throw e;
      
    }
  }

  /**
   * make an example AttributeDefScope for testing
   * @return an example AttributeDefScope
   */
  public static AttributeAssignValue exampleAttributeAssignValue() {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId("attributeAssignId");
    attributeAssignValue.setContextId("contextId");
    attributeAssignValue.setCreatedOnDb(5L);
    attributeAssignValue.setHibernateVersionNumber(3L);
    attributeAssignValue.setLastUpdatedDb(6L);
    attributeAssignValue.setId("uuid");
    attributeAssignValue.setValueInteger(7L);
    attributeAssignValue.setValueMemberId("valueMemberId");
    attributeAssignValue.setValueString("valueString");
    return attributeAssignValue;
  }
  
  /**
   * make an example AttributeAssignValue from db for testing
   * @return an example AttributeAssignValue
   */
  public static AttributeAssignValue exampleAttributeAssignValueDb() {
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory()
      .getAttributeAssignValue().findByUuidOrKey(null, null, attributeAssign.getId(), false, null, null, null);
    if (attributeAssignValue == null) {
      //create a new one
      attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setId(GrouperUuid.getUuid());
      attributeAssignValue.setAttributeAssignId(attributeAssign.getId());
      attributeAssignValue.saveOrUpdate();
    }
    return attributeAssignValue;
  }

  
  /**
   * retrieve example AttributeAssignValue from db for testing
   * @return an example AttributeAssignValue
   */
  public static AttributeAssignValue exampleRetrieveAttributeAssignValueDb() {
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory()
      .getAttributeAssignValue().findByUuidOrKey(null, null, attributeAssign.getId(), true, null, null, null);
    return attributeAssignValue;
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testRetrieveMultiple() {
    
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    
    AttributeAssignValue attributeAssignValue1 = new AttributeAssignValue();
    attributeAssignValue1.setId(GrouperUuid.getUuid());
    attributeAssignValue1.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue1.setValueInteger(55L);
    attributeAssignValue1.saveOrUpdate();

    AttributeAssignValue attributeAssignValue2 = new AttributeAssignValue();
    attributeAssignValue2.setId(GrouperUuid.getUuid());
    attributeAssignValue2.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue2.setValueString("abc");
    attributeAssignValue2.saveOrUpdate();

    AttributeAssignValue attributeAssignValue3 = new AttributeAssignValue();
    attributeAssignValue3.setId(GrouperUuid.getUuid());
    attributeAssignValue3.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue3.setValueMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true).getUuid());
    attributeAssignValue3.saveOrUpdate();
    
    //get by id
    AttributeAssignValue attributeAssignValue = attributeAssignValue1.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue1.getId(), attributeAssignValue.getId());

    attributeAssignValue = attributeAssignValue2.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue2.getId(), attributeAssignValue.getId());

    attributeAssignValue = attributeAssignValue3.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue3.getId(), attributeAssignValue.getId());
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeAssignValue attributeAssignValueOriginal = exampleAttributeAssignValueDb();
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    attributeAssignValueOriginal = exampleRetrieveAttributeAssignValueDb();
    AttributeAssignValue attributeAssignValueCopy = exampleRetrieveAttributeAssignValueDb();
    AttributeAssignValue attributeAssignValueCopy2 = exampleRetrieveAttributeAssignValueDb();
    attributeAssignValueCopy.delete();
    
    //lets insert the original
    attributeAssignValueCopy2.xmlSaveBusinessProperties(null);
    attributeAssignValueCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignValueCopy = exampleRetrieveAttributeAssignValueDb();
    
    assertFalse(attributeAssignValueCopy == attributeAssignValueOriginal);
    assertFalse(attributeAssignValueCopy.xmlDifferentBusinessProperties(attributeAssignValueOriginal));
    assertFalse(attributeAssignValueCopy.xmlDifferentUpdateProperties(attributeAssignValueOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssignValue attributeAssignValue = null;
    AttributeAssignValue exampleAttributeAssignValue = null;
    
    //TEST UPDATE PROPERTIES
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      attributeAssignValue.setContextId("abc");
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setContextId(exampleAttributeAssignValue.getContextId());
      attributeAssignValue.xmlSaveUpdateProperties();

      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
      
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setCreatedOnDb(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setCreatedOnDb(exampleAttributeAssignValue.getCreatedOnDb());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setLastUpdatedDb(exampleAttributeAssignValue.getLastUpdatedDb());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

    }

    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setHibernateVersionNumber(exampleAttributeAssignValue.getHibernateVersionNumber());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setAttributeAssignId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setAttributeAssignId(exampleAttributeAssignValue.getAttributeAssignId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setId(exampleAttributeAssignValue.getId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueInteger(99L);
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueInteger(exampleAttributeAssignValue.getValueInteger());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueMemberId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueMemberId(exampleAttributeAssignValue.getValueMemberId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueString("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueString(exampleAttributeAssignValue.getValueString());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
  }


  
}
