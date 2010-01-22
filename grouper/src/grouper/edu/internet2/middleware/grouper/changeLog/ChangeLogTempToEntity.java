package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;

/**
 * convert the temp objects to regular objects
 * @author mchyzer
 *
 */
public class ChangeLogTempToEntity {

  /**
   * convert the temps to regulars, assign id's
   * @param hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords() {
    return convertRecords(null);
  }

  /**
   * convert the temps to regulars, assign id's
   * @param hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    
    int count = 0;
    
    //first select the temp records
    List<ChangeLogEntry> changeLogEntryList = HibernateSession.byHqlStatic().createQuery("from ChangeLogEntryTemp order by createdOnDb")
      .options(new QueryOptions().paging(1000, 1, false)).list(ChangeLogEntry.class);
    
    //note: this is not in a transaction, though the inner one is
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
      
      final ChangeLogEntry CHANGE_LOG_ENTRY = changeLogEntry;
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              //insert into the non temp table
              CHANGE_LOG_ENTRY.setTempObject(false);
              CHANGE_LOG_ENTRY.save();
              //delete from the temp
              CHANGE_LOG_ENTRY.setTempObject(true);
              CHANGE_LOG_ENTRY.delete();
              return null;
            }
        
      });
    }
    
    count += changeLogEntryList.size();

    if (count > 0 && hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addTotalCount(count);
      hib3GrouperLoaderLog.store();
    }
    
    if (changeLogEntryList.size() == 1000) {
      count += convertRecords(hib3GrouperLoaderLog);
    }
    
    return count;
  }
  
}
