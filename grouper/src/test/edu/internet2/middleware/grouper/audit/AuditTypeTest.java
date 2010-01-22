/*
 * @author mchyzer
 * $Id: AuditTypeTest.java,v 1.5 2009-05-30 05:49:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import org.hibernate.Query;
import org.hibernate.Session;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


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
    TestRunner.run(new AuditTypeTest("testPersistence"));
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
//            System.out.println(auditType2);
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
  
}
