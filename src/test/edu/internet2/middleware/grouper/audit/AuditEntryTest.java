/*
 * @author mchyzer
 * $Id: AuditEntryTest.java,v 1.3.2.1 2009-12-21 07:42:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 *
 */
public class AuditEntryTest extends GrouperTest {

  /**
   * @param name
   */
  public AuditEntryTest(String name) {
    super(name);
    
  }

  /**
   * 
   */
  public void testLength() {
    //clear out
    AuditTypeDAO auditTypeDao = GrouperDAOFactory.getFactory().getAuditType();
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    final AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
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
    
    auditEntry.setDurationMicroseconds(1000000000000l);
    
    AuditEntryDAO auditEntryDao = GrouperDAOFactory.getFactory().getAuditEntry();
    auditEntryDao.saveOrUpdate(auditEntry);
    
    //edit and save again
    auditEntry.setEnvName("hey");
    auditEntryDao.saveOrUpdate(auditEntry);
    
    
    //clear out
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
  }
}
