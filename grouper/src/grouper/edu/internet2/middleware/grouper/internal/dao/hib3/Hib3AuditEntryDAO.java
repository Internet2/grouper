package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.AuditEntryNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3AuditEntryDAO.java,v 1.4 2009-06-28 19:02:17 mchyzer Exp $
 */
public class Hib3AuditEntryDAO extends Hib3DAO implements AuditEntryDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AuditEntryDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO#saveOrUpdate(edu.internet2.middleware.grouper.audit.AuditEntry)
   */
  public void saveOrUpdate(AuditEntry auditEntry) {
    auditEntry.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(auditEntry);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AuditEntry").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO#findById(java.lang.String, boolean)
   */
  public AuditEntry findById(String id, boolean exceptionIfNotFound) {
    AuditEntry auditEntry = HibernateSession.byHqlStatic().createQuery("from AuditEntry where id = :theId")
      .setString("theId", id).uniqueResult(AuditEntry.class);
    if (auditEntry == null && exceptionIfNotFound) {
      throw new AuditEntryNotFoundException("Cant find audit entry by id: " + id);
    }
    return auditEntry;
  }

} 

