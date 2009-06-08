package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogEntryDAO.java,v 1.5 2009-06-08 12:16:18 mchyzer Exp $
 */
public class Hib3ChangeLogEntryDAO extends Hib3DAO implements ChangeLogEntryDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ChangeLogEntryDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#save(edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  public void save(ChangeLogEntry changeLogEntry) {
    if (changeLogEntry.isTempObject()) {
      HibernateSession.byObjectStatic().setEntityName(ChangeLogEntry.CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME).save(changeLogEntry);
    } else {
      HibernateSession.byObjectStatic().setEntityName(ChangeLogEntry.CHANGE_LOG_ENTRY_ENTITY_NAME).save(changeLogEntry);
    }

  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    hibernateSession.byHql().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
  }

  /**
   * 
   */
  public void delete(ChangeLogEntry changeLogEntry) {
    if (changeLogEntry.isTempObject()) {
      HibernateSession.byObjectStatic()
        .setEntityName(ChangeLogEntry.CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME).delete(changeLogEntry);
    } else {
      HibernateSession.byObjectStatic()
        .setEntityName(ChangeLogEntry.CHANGE_LOG_ENTRY_ENTITY_NAME).delete(changeLogEntry);
    }
  }

} 

