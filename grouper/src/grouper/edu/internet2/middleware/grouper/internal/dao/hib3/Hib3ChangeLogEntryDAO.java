package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogEntryDAO.java,v 1.2 2009-05-12 06:35:26 mchyzer Exp $
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
    changeLogEntry.truncate();
    HibernateSession.byObjectStatic().save(changeLogEntry);
  }

  /**
   * reset the audit types
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ChangeLogEntry").executeUpdate();
  }

} 

