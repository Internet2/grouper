/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: AuditTypeTest.java,v 1.5 2009-05-30 05:49:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AuditTypeTest extends GrouperTest {

  /**
   * @param name
   */
  public AuditTypeTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuditTypeTest("testXmlDifferentUpdateProperties"));
  }
  
  /**
   * 
   */
  public void testPersistence() {
    
    //clear out
    AuditTypeDAO auditTypeDao = GrouperDAOFactory.getFactory().getAuditType();
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    final AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
    auditType.setId(GrouperUuid.getUuid());
    
//    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT,
//        new HibernateHandler() {
//
//          public Object callback(HibernateHandlerBean hibernateHandlerBean)
//              throws GrouperDAOException {
//            
//            Session session = hibernateHandlerBean.getHibernateSession().getSession();
//            session.save("AuditType2", auditType);
//            hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
//            
//            Query query =  session.createQuery("from AuditType2 where labelString01 = 's1'");
//
//            AuditType auditType2 = (AuditType)query.uniqueResult();
//            return null;
//          }
//    });
    
    auditTypeDao.saveOrUpdate(auditType);
    
    //update and save again
    auditType.setLabelString03("s3");
    auditTypeDao.saveOrUpdate(auditType);

    AuditEntry auditEntry = new AuditEntry();
    auditEntry.setAuditTypeId(auditType.getId());
    auditEntry.setDescription("whatever");
    auditEntry.setId(GrouperUuid.getUuid());
    auditEntry.setString01("something");
    
    AuditEntryDAO auditEntryDao = GrouperDAOFactory.getFactory().getAuditEntry();
    auditEntryDao.saveOrUpdate(auditEntry);
    
    //edit and save again
    auditEntry.setEnvName("hey");
    auditEntryDao.saveOrUpdate(auditEntry);
    
    
    //clear out
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
  }
  
  /**
   * make an example audit type for testing
   * @return an example audit type
   */
  public static AuditType exampleAuditType() {
    AuditType auditType = new AuditType();
    auditType.setActionName("actionName");
    auditType.setAuditCategory("auditCategory");
    auditType.setContextId("contextId");
    auditType.setCreatedOnDb(3L);
    auditType.setHibernateVersionNumber(4L);
    auditType.setId("id");
    auditType.setLabelInt01("labelInt01");
    auditType.setLabelInt02("labelInt02");
    auditType.setLabelInt03("labelInt03");
    auditType.setLabelInt04("labelInt04");
    auditType.setLabelInt05("labelInt05");
    auditType.setLabelString01("labelString01");
    auditType.setLabelString02("labelString02");
    auditType.setLabelString03("labelString03");
    auditType.setLabelString04("labelString04");
    auditType.setLabelString05("labelString05");
    auditType.setLabelString06("labelString06");
    auditType.setLabelString07("labelString07");
    auditType.setLabelString08("labelString08");
    auditType.setLastUpdatedDb(5L);
    return auditType;
  }
  
  /**
   * make an example audit type from db for testing
   * @return an example audit type
   */
  public static AuditType exampleAuditTypeDb() {
    return exampleAuditTypeDb("testCategory", "testAction");
  }
  
  /**
   * make an example audit type from db for testing
   * @param category 
   * @param action 
   * @return an example audit type
   */
  public static AuditType exampleAuditTypeDb(String category, String action) {
    
    AuditType auditType = exampleRetrieveAuditTypeDb(category, action, false);
    if (auditType == null) {
      auditType = new AuditType(category, action, null, "labelString01", "labelString02", "labelString03");
      GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditType);
      AuditTypeFinder.clearCache();
    }
    return auditType;
  }

  /**
   * retrieve example audit type from db for testing
   * @return an example audit type
   */
  public static AuditType exampleRetrieveAuditTypeDb() {
    return exampleRetrieveAuditTypeDb("testCategory", "testAction", true);
  }
  
  /**
   * retrieve example audit type from db for testing
   * @param category 
   * @param action 
   * @param exceptionIfNull 
   * @return an example audit type
   */
  public static AuditType exampleRetrieveAuditTypeDb(String category, String action, boolean exceptionIfNull) {
    AuditType auditType = GrouperDAOFactory.getFactory().getAuditType()
      .findByUuidOrName(null, category, action, exceptionIfNull);
    return auditType;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AuditType auditTypeOriginal = exampleAuditTypeDb("exampleInsert", "exampleInsertAction");
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    auditTypeOriginal = exampleRetrieveAuditTypeDb("exampleInsert", "exampleInsertAction", true);
    AuditType auditTypeCopy = exampleRetrieveAuditTypeDb("exampleInsert", "exampleInsertAction", true);
    AuditType auditTypeCopy2 = exampleRetrieveAuditTypeDb("exampleInsert", "exampleInsertAction", true);
    HibernateSession.byObjectStatic().delete(auditTypeCopy);
    
    //lets insert the original
    auditTypeCopy2.xmlSaveBusinessProperties(null);
    auditTypeCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    auditTypeCopy = exampleRetrieveAuditTypeDb("exampleInsert", "exampleInsertAction", true);
    
    assertFalse(auditTypeCopy == auditTypeOriginal);
    assertFalse(auditTypeCopy.xmlDifferentBusinessProperties(auditTypeOriginal));
    assertFalse(auditTypeCopy.xmlDifferentUpdateProperties(auditTypeOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AuditType auditType = null;
    AuditType exampleAuditType = null;

    
    //TEST UPDATE PROPERTIES
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();
      
      auditType.setContextId("abc");
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertTrue(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setContextId(exampleAuditType.getContextId());
      auditType.xmlSaveUpdateProperties();

      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
      
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setCreatedOnDb(99L);
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertTrue(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setCreatedOnDb(exampleAuditType.getCreatedOnDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLastUpdatedDb(99L);
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertTrue(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLastUpdatedDb(exampleAuditType.getLastUpdatedDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setHibernateVersionNumber(99L);
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertTrue(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setHibernateVersionNumber(exampleAuditType.getHibernateVersionNumber());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setActionName("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setActionName(exampleAuditType.getActionName());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setAuditCategory("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setAuditCategory(exampleAuditType.getAuditCategory());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setAuditCategory("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setAuditCategory(exampleAuditType.getAuditCategory());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setId("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setId(exampleAuditType.getId());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelInt01("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelInt01(exampleAuditType.getLabelInt01());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelInt02("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelInt02(exampleAuditType.getLabelInt02());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelInt03("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelInt03(exampleAuditType.getLabelInt03());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelInt04("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelInt04(exampleAuditType.getLabelInt04());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelInt05("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelInt05(exampleAuditType.getLabelInt05());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString01("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString01(exampleAuditType.getLabelString01());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString02("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString02(exampleAuditType.getLabelString02());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString03("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString03(exampleAuditType.getLabelString03());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString04("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString04(exampleAuditType.getLabelString04());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString05("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString05(exampleAuditType.getLabelString05());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString06("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString06(exampleAuditType.getLabelString06());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString07("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString07(exampleAuditType.getLabelString07());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
    {
      auditType = exampleAuditTypeDb();
      exampleAuditType = exampleRetrieveAuditTypeDb();

      auditType.setLabelString08("abc");
      
      assertTrue(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));

      auditType.setLabelString08(exampleAuditType.getLabelString08());
      auditType.xmlSaveBusinessProperties(exampleRetrieveAuditTypeDb());
      auditType.xmlSaveUpdateProperties();
      
      auditType = exampleRetrieveAuditTypeDb();
      
      assertFalse(auditType.xmlDifferentBusinessProperties(exampleAuditType));
      assertFalse(auditType.xmlDifferentUpdateProperties(exampleAuditType));
    
    }
    
  }

}
