package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogEntryDAO.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
public class Hib3ChangeLogEntryDAO extends Hib3DAO implements ChangeLogEntryDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ChangeLogEntryDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#saveOrUpdate(edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  public void saveOrUpdate(ChangeLogEntry changeLogEntry) {
    changeLogEntry.truncate();
    HibernateSession.byObjectStatic().saveOrUpdate(changeLogEntry);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ChangeLogEntry").executeUpdate();
  }

} 

