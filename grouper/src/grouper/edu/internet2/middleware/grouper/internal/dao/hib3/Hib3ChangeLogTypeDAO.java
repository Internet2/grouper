package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * Data Access Object for audit type
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogTypeDAO.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
public class Hib3ChangeLogTypeDAO extends Hib3DAO implements ChangeLogTypeDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ChangeLogTypeDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO#saveOrUpdate(edu.internet2.middleware.grouper.changeLog.ChangeLogType)
   */
  public void saveOrUpdate(ChangeLogType changeLogType) {
    
    //assign id if not there
    if (StringUtils.isBlank(changeLogType.getId())) {
      changeLogType.setId(GrouperUuid.getUuid());
    }

    changeLogType.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(changeLogType);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    //i think we dont want to delete these in a reset...
    //hibernateSession.byHql().createQuery("delete from ChangeLogType").executeUpdate();
    //tell the cache it is empty...
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO#deleteEntriesAndTypesByCategoryAndAction(java.lang.String, java.lang.String)
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action) {
    
    //delete entries
    HibernateSession.byHqlStatic()
      .createQuery("delete from ChangeLogEntry as auditEntry where auditEntry.auditTypeId = " +
      		"(select auditType.id from ChangeLogType auditType " +
      		"where auditType.auditCategory = :theChangeLogCategory and auditType.actionName = :theActionName)")
      		.setString("theChangeLogCategory", category).setString("theActionName", action).executeUpdate();

    //delete types
    HibernateSession.byHqlStatic()
      .createQuery("delete from ChangeLogType where auditCategory = :theChangeLogCategory and actionName = :theActionName")
      .setString("theChangeLogCategory", category).setString("theActionName", action).executeUpdate();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO#findAll()
   */
  public Set<ChangeLogType> findAll() {
    return HibernateSession.byHqlStatic().createQuery("from ChangeLogType").listSet(ChangeLogType.class);
  }
  
} 

