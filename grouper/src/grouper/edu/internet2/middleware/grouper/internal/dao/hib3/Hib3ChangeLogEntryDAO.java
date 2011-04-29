package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;

/**
 * Data Access Object for audit entry
 * @author  mchyzer
 * @version $Id: Hib3ChangeLogEntryDAO.java,v 1.7 2009-06-10 05:31:35 mchyzer Exp $
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
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME).save(changeLogEntry);
    } else {
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_ENTITY_NAME).save(changeLogEntry);
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#saveBatch(java.util.Set, boolean)
   */
  public void saveBatch(Set<ChangeLogEntry> changeLogEntries, boolean isTempBatch) {
    if (isTempBatch) {
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME).saveBatch(changeLogEntries);
    } else {
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_ENTITY_NAME).saveBatch(changeLogEntries);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#update(edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  public void update(ChangeLogEntry changeLogEntry) {
    if (changeLogEntry.isTempObject()) {
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME).update(changeLogEntry);
    } else {
      HibernateSession.byObjectStatic().setEntityName(
          ChangeLogEntry.CHANGE_LOG_ENTRY_ENTITY_NAME).update(changeLogEntry);
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
   * @param changeLogEntry 
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#retrieveBatch(long, int)
   */
  public List<ChangeLogEntry> retrieveBatch(long afterSequenceNumber, int batchSize) {
    
    List<ChangeLogEntry> changeLogEntryList = HibernateSession.byHqlStatic().createQuery(
        "from ChangeLogEntryEntity theEntity where theEntity.sequenceNumber > :afterSequenceNumber")
        .options(new QueryOptions().paging(batchSize, 1, false))
        .setLong("afterSequenceNumber", afterSequenceNumber).list(ChangeLogEntry.class);
    
    return changeLogEntryList;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO#findBySequenceNumber(long, boolean)
   */
  public ChangeLogEntry findBySequenceNumber(long sequenceNumber, boolean exceptionIfNotFound) {
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindBySequenceNumber")
      .createQuery(
        "from ChangeLogEntryEntity where sequenceNumber = :theSequenceNumber")
      .setLong("theSequenceNumber", sequenceNumber).uniqueResult(ChangeLogEntry.class);
  
    if (changeLogEntry == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find changeLogEntry by sequenceNumber: " + sequenceNumber);
    }
    
    return changeLogEntry;
  }

} 

