package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * Data Access Object for audit type
 * @author  mchyzer
 * @version $Id: Hib3AuditTypeDAO.java,v 1.2 2009-02-06 16:33:18 mchyzer Exp $
 */
public class Hib3AuditTypeDAO extends Hib3DAO implements AuditTypeDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AuditTypeDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#saveOrUpdate(edu.internet2.middleware.grouper.audit.AuditType)
   */
  public void saveOrUpdate(AuditType auditType) {
    
    //assign id if not there
    if (StringUtils.isBlank(auditType.getId())) {
      auditType.setId(GrouperUuid.getUuid());
    }

    auditType.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(auditType);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    //i think we dont want to delete these in a reset...
    //hibernateSession.byHql().createQuery("delete from AuditType").executeUpdate();
    //tell the cache it is empty...
    
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

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO#findAll()
   */
  public Set<AuditType> findAll() {
    return HibernateSession.byHqlStatic().createQuery("from AuditType").listSet(AuditType.class);
  }
  
} 

