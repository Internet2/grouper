/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDaemonDeleteOldRecords {

  /** delete old records log */
  private static final String LOG_LABEL = "maintenanceDeleteOldRecords";

  
  /**
   * @param hib3GrouploaderLog
   */
  public static void maintenanceDeleteOldRecords(Hib3GrouperLoaderLog hib3GrouploaderLog) {

    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {
      StringBuilder jobMessage = new StringBuilder();
      {
        int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
        if (daysToKeepLogs != -1) {
          //lets get a date
          Calendar calendar = GregorianCalendar.getInstance();
          //get however many days in the past
          calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
          //run a query to delete (note, dont retrieve records to java, just delete)
          long records = -1;
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteRecordsInBatches", true)) {
            records = HibernateSession.byHqlStatic().createQuery(
                "select gll.id from Hib3GrouperLoaderLog gll where gll.lastUpdated < :lastUpdated ").setTimestamp("lastUpdated", new Timestamp(calendar.getTimeInMillis()))
                .deleteInBatches(String.class, "Hib3GrouperLoaderLog", "id");
          } else {
            //this is the old way that we can get rid of at some point
            records = HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where last_updated < ?", 
                (List<Object>)(Object)GrouperUtil.toList(new Timestamp(calendar.getTimeInMillis())));
          }
  
          jobMessage.append("Deleted " + records + " records from grouper_loader_log older than " + daysToKeepLogs + " days old.  ");
          
        } else {
          jobMessage.append("Configured to not delete records from grouper_loader_log table.  ");
        }
      }
      {
        int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.change_log_entry.days", 14);
        if (daysToKeepLogs != -1) {
          //lets get a date
          Calendar calendar = GregorianCalendar.getInstance();
          //get however many days in the past
          calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
          //note, this is *1000 so that we can differentiate conflicting records
          long time = calendar.getTimeInMillis()*1000L;
          //run a query to delete (note, dont retrieve records to java, just delete)
          long records = -1; 
          
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeleteRecordsInBatches", true)) {
  
            records = HibernateSession.byHqlStatic().createQuery(
                "select cle.sequenceNumber from ChangeLogEntryEntity cle where cle.createdOnDb < :createdOn ").setLong("createdOn", time)
                .deleteInBatches(long.class, "ChangeLogEntryEntity", "sequenceNumber");
            
          } else {
  
            // this is the old way that can go away at some point
            records = HibernateSession.bySqlStatic().executeSql("delete from grouper_change_log_entry where created_on < ?", 
                (List<Object>)(Object)GrouperUtil.toList(new Long(time)));
            
          }
        
          jobMessage.append("Deleted " + records + " records from grouper_change_log_entry older than " + daysToKeepLogs + " days old. (" + time + ")  ");
        } else {
          jobMessage.append("Configured to not delete records from grouper_change_log_entry table.  ");
        }
        
      }
      {
        int daysToKeepLogs = GrouperConfig.retrieveConfig().propertyValueInt("instrumentation.retainData.days", 30);
        if (daysToKeepLogs != -1) {
          Calendar calendar = GregorianCalendar.getInstance();
          calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
  
          String attributeDefNameName = InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR;
  
          long records = HibernateSession.byHqlStatic().createQuery(
                "select aav.id from AttributeAssignValue aav, AttributeAssign aa, AttributeDefName adn where aav.attributeAssignId = aa.id and adn.id = aa.attributeDefNameId and adn.nameDb = :attributeDefNameName and aav.createdOnDb < :createdOn ")
                .setLong("createdOn", calendar.getTimeInMillis())
                .setString("attributeDefNameName", attributeDefNameName)
                .deleteInBatches(long.class, "AttributeAssignValue", "id");
  
  
          jobMessage.append("Deleted " + records + " instrumentation records older than " + daysToKeepLogs + " days old. (" + calendar.getTimeInMillis() + ")  ");
        } else {
          jobMessage.append("Configured to not delete old instrumentation data.  ");
        }
      }
      
      {
        deleteOldAuditEntryNoLoggedInUser(jobMessage);
        
      }
  
      {
        int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.audit_entry.days", -1);
        deleteOldAuditEntry(jobMessage, daysToKeepLogs);
        
      }
  
      {
        int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.point_in_time_deleted_object.days", -1);
        //deleteOldDeletedPointInTimeObjects(jobMessage, daysToKeepLogs);
        
      }
  
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      hib3GrouploaderLog.setJobMessage(jobMessage.toString());
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging("maintenanceDeleteOldRecords");
      }

    }
  }

  /**
   * @return records deleted
   */
  public static long deleteOldAuditEntryNoLoggedInUser() {
    return deleteOldAuditEntryNoLoggedInUser(null);
  }
  
  /**
   * @param jobMessage
   * @return recordsDeleted
   */
  public static long deleteOldAuditEntryNoLoggedInUser(StringBuilder jobMessage) {
    int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.audit_entry_no_logged_in_user.days", -1);
    return deleteOldAuditEntryNoLoggedInUser(jobMessage, daysToKeepLogs);
  }

  /**
   * @param jobMessage
   * @param daysToKeepLogs
   */
  public static void deleteOldAuditEntry(StringBuilder jobMessage, int daysToKeepLogs) {
    
    //GrouperLoaderLogger.addLogEntry(LOG_LABEL, "success", false);
    
    if (daysToKeepLogs != -1) {
      //lets get a date
      Calendar calendar = GregorianCalendar.getInstance();
      //get however many days in the past
      calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
      //note, this is *1000 so that we can differentiate conflicting records
      long time = calendar.getTimeInMillis();
      //run a query to delete (note, dont retrieve records to java, just delete)
      long records = -1; 
      
      records = HibernateSession.byHqlStatic().createQuery(
          "select ae.id from AuditEntry ae where cle.createdOnDb < :createdOn").setLong("createdOn", time)
          .deleteInBatches(String.class, "AuditEntry", "id");
    
      if (jobMessage != null) {
        jobMessage.append("Deleted " + records + " records from audit_entry older than " + daysToKeepLogs + " days old. (" + time + ")  ");
      }
    } else {
      if (jobMessage != null) {
        jobMessage.append("Configured to not delete records from audit_entry table older than a certain number of days");
      }
    }
  }

  /**
   * @param jobMessage
   * @param daysToKeepLogs
   */
  public static void deleteOldDeletedPointInTimeObjects(StringBuilder jobMessage,
      int daysToKeepLogs) {
    if (daysToKeepLogs != -1) {
      //lets get a date
      Calendar calendar = GregorianCalendar.getInstance();
      //get however many days in the past
      calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
      //note, this is *1000 so that we can differentiate conflicting records
      long time = calendar.getTimeInMillis();
      //run a query to delete (note, dont retrieve records to java, just delete)
      long records = -1; 
      
      records = edu.internet2.middleware.grouper.pit.PITUtils.deleteInactiveRecords(new Date(), false);
      
      jobMessage.append("Deleted " + records + " deleted point in time records older than " + daysToKeepLogs + " days old. (" + time + ")  ");
    } else {
      jobMessage.append("Configured to not remove deleted point in time records older than a certain number of days");
    }
  }

  /**
   * @param daysToKeepLogs
   */
  public static long deleteOldAuditEntryNoLoggedInUser(int daysToKeepLogs) {
    return deleteOldAuditEntryNoLoggedInUser(null, daysToKeepLogs);
  }

  /**
   * @param jobMessage
   * @param daysToKeepLogs
   * @return records deleted
   */
  public static long deleteOldAuditEntryNoLoggedInUser(StringBuilder jobMessage,
      int daysToKeepLogs) {
    
    boolean initted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldAuditNotLoggedInDays", daysToKeepLogs);
      
      long records = -1; 
  
      if (daysToKeepLogs != -1) {
        //lets get a date
        Calendar calendar = GregorianCalendar.getInstance();
        //get however many days in the past
        calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
        //note, this is *1000 so that we can differentiate conflicting records
        long time = calendar.getTimeInMillis();
        //run a query to delete (note, dont retrieve records to java, just delete)
        
        records = HibernateSession.byHqlStatic().createQuery(
            "select ae.id from AuditEntry ae where ae.createdOnDb < :createdOn and ae.loggedInMemberId is null").setLong("createdOn", time)
            .deleteInBatches(String.class, "AuditEntry", "id");
      
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldAuditNotLoggedInCount", records);
  
        if (jobMessage != null) {
          jobMessage.append("Deleted " + records + " records from audit_entry with null logged in member id and older than " + daysToKeepLogs + " days old. (" + time + ")  ");
        }
      } else {
        if (jobMessage != null) {
          jobMessage.append("Configured to not delete records from audit_entry table with null logged in member id");
        }
      }
      
      return records;
    } finally {
      if (initted) {
        GrouperLoaderLogger.skipLogging(LOG_LABEL);
      }
    }
  }


}
