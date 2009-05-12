package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * Data Access Object for changeLog type
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogTypeDAO.java,v 1.2 2009-05-12 06:35:26 mchyzer Exp $
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
   * reset the changeLog types
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
      .createQuery("delete from ChangeLogEntry as changeLogEntry where changeLogEntry.changeLogTypeId = " +
      		"(select changeLogType.id from ChangeLogType changeLogType " +
      		"where changeLogType.changeLogCategory = :theChangeLogCategory and changeLogType.actionName = :theActionName)")
      		.setString("theChangeLogCategory", category).setString("theActionName", action).executeUpdate();

    //delete types
    HibernateSession.byHqlStatic()
      .createQuery("delete from ChangeLogType where changeLogCategory = :theChangeLogCategory and actionName = :theActionName")
      .setString("theChangeLogCategory", category).setString("theActionName", action).executeUpdate();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO#findAll()
   */
  public Set<ChangeLogType> findAll() {
    return HibernateSession.byHqlStatic().createQuery("from ChangeLogType").listSet(ChangeLogType.class);
  }
  
} 

