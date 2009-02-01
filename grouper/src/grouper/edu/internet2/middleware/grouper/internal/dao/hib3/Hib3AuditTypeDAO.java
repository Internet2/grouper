package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.AuditType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;

/**
 * Data Access Object for audit type
 * @author  mchyzer
 * @version $Id: Hib3AuditTypeDAO.java,v 1.1 2009-02-01 22:38:48 mchyzer Exp $
 */
public class Hib3AuditTypeDAO extends Hib3DAO implements AuditTypeDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AuditTypeDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#saveOrUpdate(edu.internet2.middleware.grouper.AuditType)
   */
  public void saveOrUpdate(AuditType auditType) {
    auditType.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(auditType);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AuditType").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#deleteEntriesAndTypesByCategoryAndAction(java.lang.String, java.lang.String)
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action) {
    
    //delete entries
    HibernateSession.byHqlStatic()
      .createQuery("delete from AuditEntry as auditEntry where auditEntry.auditTypeId = " +
      		"(select auditType.id from AuditType auditType " +
      		"where auditType.auditCategory = :theAuditCategory and auditType.actionName = :theActionName)")
      		.setString("theAuditCategory", category).setString("theActionName", action).executeUpdate();

    //delete types
    HibernateSession.byHqlStatic()
      .createQuery("delete from AuditType where auditCategory = :theAuditCategory and actionName = :theActionName")
      .setString("theAuditCategory", category).setString("theActionName", action).executeUpdate();
    
    
    
  }
  
} 

