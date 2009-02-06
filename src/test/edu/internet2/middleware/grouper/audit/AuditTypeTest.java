/*
 * @author mchyzer
 * $Id: AuditTypeTest.java,v 1.3 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
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
    
    AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
    auditType.setId(GrouperUuid.getUuid());
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
