/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
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
    boolean error = false;
    try {
      StringBuilder jobMessage = new StringBuilder();
      try {
        deleteOldGrouperLoaderLogs(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldGrouperLoaderLogs", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInGrouperLoaderDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldGrouperLoaderLogs: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
      }
      try {
        deleteOldChangeLogEntries(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldChangeLogEntries", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInChangeLogEntryDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldChangeLogEntries: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
        
      }
      try {
        deleteOldInstrumentationData(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldInstrumentation", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInInstrumentationDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldInstrumentation: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
      }
      
      try {
        deleteOldAuditEntryNoLoggedInUser(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldAuditEntryNoLoggedInUser", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInAuditEntryNoLoggedInUserDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldAuditEntryNoLoggedInUser: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
        
      }
  
      try {
        deleteOldAuditEntry(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldAuditEntry", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInAuditEntryDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldAuditEntry: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
        
      }

      try {
        deleteOldDeletedPointInTimeObjects(jobMessage);
      } catch (Exception e) {
        LOG.error("Error in deleteOldDeletedPointInTime", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInDeletedPointInTimeDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldDeletedPointInTime: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
      }

      try {
        boolean[] errorArray = new boolean[]{false};
        obliterateOldStemsDirectlyInStem(jobMessage, errorArray);
        if (errorArray[0]) {
          error = true;
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems.error", true);
        }
      } catch (Exception e) {
        LOG.error("Error in deleteOldDeletedPointInTime", e);
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "errorInDeletedPointInTimeDelete", ExceptionUtils.getFullStackTrace(e));
        jobMessage.append("\nError in deleteOldDeletedPointInTime: " +ExceptionUtils.getFullStackTrace(e)  + "\n");
        error = true;
      }
  
      if (error) {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "status", "error");
      } else {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "status", "success");
      }
      hib3GrouploaderLog.setJobMessage(jobMessage.toString());
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }

    }
  }

  /**
   * @param jobMessage
   */
  private static void deleteOldInstrumentationData(StringBuilder jobMessage) {
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {

      int daysToKeepLogs = GrouperConfig.retrieveConfig().propertyValueInt("instrumentation.retainData.days", 30);
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldInstrumentationData", daysToKeepLogs);
      if (daysToKeepLogs != -1) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
   
        String attributeDefNameName = InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR;
   
        long records = HibernateSession.byHqlStatic().createQuery(
              "select aav.id from AttributeAssignValue aav, AttributeAssign aa, AttributeDefName adn where aav.attributeAssignId = aa.id and adn.id = aa.attributeDefNameId and adn.nameDb = :attributeDefNameName and aav.createdOnDb < :createdOn ")
              .setLong("createdOn", calendar.getTimeInMillis())
              .setString("attributeDefNameName", attributeDefNameName)
              .deleteInBatches(String.class, "AttributeAssignValue", "id");
   
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldInstrumentationDataCount", records);
   
        jobMessage.append("Deleted " + records + " instrumentation records older than " + daysToKeepLogs + " days old. (" + calendar.getTimeInMillis() + ")  ");
      } else {
        jobMessage.append("Configured to not delete old instrumentation data.  ");
      }
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }

  }

  /**
   * @param jobMessage
   */
  private static void deleteOldChangeLogEntries(StringBuilder jobMessage) {
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);

    try {
      int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.change_log_entry.days", 14);
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldChangeLogEntriesDays", daysToKeepLogs);
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
      
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldChangeLogEntriesCount", records);
        jobMessage.append("Deleted " + records + " records from grouper_change_log_entry older than " + daysToKeepLogs + " days old. (" + time + ")  ");
      } else {
        jobMessage.append("Configured to not delete records from grouper_change_log_entry table.  ");
      }
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }
  }

  /**
   * @param jobMessage
   */
  private static void deleteOldGrouperLoaderLogs(StringBuilder jobMessage) {
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {

      int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldGrouperLoaderLogsDays", daysToKeepLogs);
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
   
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldGrouperLoaderLogsCount", records);

        jobMessage.append("Deleted " + records + " records from grouper_loader_log older than " + daysToKeepLogs + " days old.  ");
        
      } else {
        jobMessage.append("Configured to not delete records from grouper_loader_log table.  ");
      }
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
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
   * @return records deleted
   */
  public static long deleteOldDeletedPointInTimeObjects(StringBuilder jobMessage,
      int daysToKeepLogs) {
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteDeletedPointInTimeObjectsDays", daysToKeepLogs);
      
      long records = -1; 
  
      if (daysToKeepLogs != -1) {
        //lets get a date
        Calendar calendar = GregorianCalendar.getInstance();
        //get however many days in the past
        calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
        //note, this is *1000 so that we can differentiate conflicting records
        long time = calendar.getTimeInMillis();

        records = edu.internet2.middleware.grouper.pit.PITUtils.deleteInactiveRecords(new Date(time), false);
      
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteDeletedPointInTimeObjectsCount", records);
  
        if (jobMessage != null) {
          jobMessage.append("Deleted " + records + " records from DeletedPointInTimeObjects older than " + daysToKeepLogs + " days old. (" + time + ")  ");
        }
      } else {
        if (jobMessage != null) {
          jobMessage.append("Configured to not delete records from DeletedPointInTimeObjects");
        }
      }
      
      return records;
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }
  }

  /**
   * @param daysToKeepLogs
   * @return records deleted
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
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
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
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }
  }

  /**
   * @return records deleted
   */
  public static long deleteOldAuditEntry() {
    return deleteOldAuditEntry(null);
  }

  /**
   * @param daysToKeepLogs
   * @return records deleted
   */
  public static long deleteOldAuditEntry(int daysToKeepLogs) {
    return deleteOldAuditEntry(null, daysToKeepLogs);
  }

  /**
   * @param jobMessage
   * @return recordsDeleted
   */
  public static long deleteOldAuditEntry(StringBuilder jobMessage) {
    int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.audit_entry.days", -1);
    return deleteOldAuditEntry(jobMessage, daysToKeepLogs);
  }

  /**
   * @param jobMessage
   * @param daysToKeepLogs
   * @return records deleted
   */
  public static long deleteOldAuditEntry(StringBuilder jobMessage,
      int daysToKeepLogs) {
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    try {
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldAuditDays", daysToKeepLogs);
      
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
            "select ae.id from AuditEntry ae where ae.createdOnDb < :createdOn").setLong("createdOn", time)
            .deleteInBatches(String.class, "AuditEntry", "id");
      
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "deleteOldAuditCount", records);
  
        if (jobMessage != null) {
          jobMessage.append("Deleted " + records + " records from audit_entry older than " + daysToKeepLogs + " days old. (" + time + ")  ");
        }
      } else {
        if (jobMessage != null) {
          jobMessage.append("Configured to not delete records from audit_entry table");
        }
      }
      
      return records;
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }
  }

  /**
   * @return records deleted
   */
  public static long deleteOldDeletedPointInTimeObjects() {
    return deleteOldDeletedPointInTimeObjects(null);
  }

  /**
   * @param daysToKeepLogs
   * @return records deleted
   */
  public static long deleteOldDeletedPointInTimeObjects(int daysToKeepLogs) {
    return deleteOldDeletedPointInTimeObjects(null, daysToKeepLogs);
  }

  /**
   * @param jobMessage
   * @return recordsDeleted
   */
  public static long deleteOldDeletedPointInTimeObjects(StringBuilder jobMessage) {
    int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.point_in_time_deleted_objects.days", -1);
    return deleteOldDeletedPointInTimeObjects(jobMessage, daysToKeepLogs);
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonDeleteOldRecords.class);


  /**
   * @return records deleted
   */
  public static long obliterateOldStemsDirectlyInStem() {
    return obliterateOldStemsDirectlyInStem((StringBuilder)null, (boolean[])null);
  }

  /**
   * @param deleteOldStems
   * @return records deleted
   */
  public static long obliterateOldStemsDirectlyInStem(Set<DeleteOldStems> deleteOldStems) {
    return obliterateOldStemsDirectlyInStem(null, deleteOldStems);
  }

  /**
   * 
   */
  public static class DeleteOldStems {
    
    /**
     * 
     */
    public DeleteOldStems() {
      super();
      
    }

    /**
     * @param configKey
     * @param folderName
     * @param days
     * @param deletePointInTime
     */
    public DeleteOldStems(String configKey, String folderName, Integer days,
        Boolean deletePointInTime) {
      super();
      this.configKey = configKey;
      this.folderName = folderName;
      this.days = days;
      this.deletePointInTime = deletePointInTime;
    }

    /**
     * 
     */
    private String configKey;
    
    /**
     * 
     */
    private Integer days;

    /**
     * 
     */
    private String folderName;
    
    /**
     * 
     */
    private Boolean deletePointInTime;
    
    /**
     * @return the configKey
     */
    public String getConfigKey() {
      return this.configKey;
    }
    
    /**
     * @param configKey the configKey to set
     */
    public void setConfigKey(String configKey) {
      this.configKey = configKey;
    }
    
    /**
     * @return the days
     */
    public Integer getDays() {
      return this.days;
    }
    
    /**
     * @param days the days to set
     */
    public void setDays(Integer days) {
      this.days = days;
    }
    
    /**
     * @return the folder
     */
    public String getFolderName() {
      return this.folderName;
    }
    
    /**
     * @param folderName1 the folder to set
     */
    public void setFolderName(String folderName1) {
      this.folderName = folderName1;
    }
    
    /**
     * @return the deletePointInTime
     */
    public Boolean getDeletePointInTime() {
      return this.deletePointInTime;
    }
    
    /**
     * @param deletePointInTime the deletePointInTime to set
     */
    public void setDeletePointInTime(Boolean deletePointInTime) {
      this.deletePointInTime = deletePointInTime;
    }
    
    
    
  }
  
  /**
   * @param jobMessage
   * @param error 
   * @return recordsDeleted
   */
  public static long obliterateOldStemsDirectlyInStem(StringBuilder jobMessage, boolean[] error) {
    
    Pattern daysPattern = Pattern.compile("^loader\\.retain\\.db\\.folder\\.(.*)\\.days$");

    //  loader.retain.db.folder.courses.days=1825
    //  loader.retain.db.folder.courses.parentFolderName=my:folder:for:courses
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    Map<String, String> daysMap = grouperLoaderConfig.propertiesMap( daysPattern );
    
    Set<DeleteOldStems> deleteOldStemsSet = new LinkedHashSet<DeleteOldStems>();
    int index = 0;
    for(String daysKey: daysMap.keySet()) {

      try {
        Matcher matcher = daysPattern.matcher(daysKey);
        matcher.matches();
        String variableName = matcher.group(1);
        
        int days = grouperLoaderConfig.propertyValueInt(daysKey);
        String folder = grouperLoaderConfig.propertyValueStringRequired("loader.retain.db.folder." + variableName + ".parentFolderName");
        boolean deletePointInTime = grouperLoaderConfig.propertyValueBoolean("loader.retain.db.folder." + variableName + ".deletePointInTime");
  
        DeleteOldStems deleteOldStems = new DeleteOldStems();
        deleteOldStems.setConfigKey(variableName);
        deleteOldStems.setDays(days);
        deleteOldStems.setFolderName(folder);
        deleteOldStems.setDeletePointInTime(deletePointInTime);
        
        deleteOldStemsSet.add(deleteOldStems);
      } catch (Exception e) {
        LOG.error("Error with obliterate folder key: " + daysKey, e);
        if (jobMessage != null) {
          jobMessage.append("Error in folder: " + daysKey + ", " + ExceptionUtils.getFullStackTrace(e));
        }
        if (GrouperUtil.length(error) == 1) {
          error[0] = true;
        }
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.error", ExceptionUtils.getFullStackTrace(e));

      }
      index++;
    }
    
    return obliterateOldStemsDirectlyInStem(jobMessage, deleteOldStemsSet, error);
  }

  /**
   * @param jobMessage
   * @param deleteOldStemsSet
   * @return records deleted
   */
  public static long obliterateOldStemsDirectlyInStem(StringBuilder jobMessage,
      Set<DeleteOldStems> deleteOldStemsSet) {
    return obliterateOldStemsDirectlyInStem(jobMessage,
        deleteOldStemsSet, null);
  }

  /**
   * @param jobMessage
   * @param deleteOldStemsSet
   * @param error put if error if something there
   * @return folders deleted
   */
  public static long obliterateOldStemsDirectlyInStem(StringBuilder jobMessage,
      Set<DeleteOldStems> deleteOldStemsSet, boolean[] error) {
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(LOG_LABEL);
    
    int foldersDeleted = 0;
    
    try {
      
      GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStemsCount", GrouperUtil.length(deleteOldStemsSet));

      int index = 0;
      
      for (DeleteOldStems deleteOldStems : GrouperUtil.nonNull(deleteOldStemsSet)) {

        if (deleteOldStems == null) {
          
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.deleteOldStemsCorrupt", true);
          continue;
        }
        
        String folderName = deleteOldStems.getFolderName();

        if (StringUtils.isBlank(folderName)) {
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.deleteOldStemsFolderCorrupt", true);
          continue;
          
        }
        
        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem", folderName);
                
        if (deleteOldStems.getDays() == null || deleteOldStems.getDays() < 1) {
          
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.daysToKeepCorrupt", true);
          continue;
        }

        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".days", deleteOldStems.getDays());

        if (deleteOldStems.getDeletePointInTime() == null) {

          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".deletePointInTimeCorrupt", true);
          continue;
        }

        GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".deletePointInTime", deleteOldStems.getDeletePointInTime());

        try {

          Stem parentStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderName, true);
          
          //get child stems
          Set<Stem> stems = new StemFinder().assignParentStemId(parentStem.getId()).assignStemScope(Scope.ONE).findStems();
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.subFolderCount", GrouperUtil.length(stems));
          
          int count=-1;
          for (Stem stem : GrouperUtil.nonNull(stems)) {
            count++;
            try {


              long stemCreateTime = stem.getCreateTimeLong();
              if (stemCreateTime < 1) {
                GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem." + stem.getExtension() + ".createTimeCorrupt", true);
                continue;
              }
  
              //lets get a date
              Calendar calendar = GregorianCalendar.getInstance();
              //get however many days in the past
              calendar.add(Calendar.DAY_OF_YEAR, -1 * deleteOldStems.getDays());
              if (stemCreateTime < calendar.getTimeInMillis()) {
  
                GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem." + stem.getExtension() + ".deleting", true);
                GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem." + stem.getExtension() + ".folderCreatedOn", new Timestamp(stemCreateTime));
  
                stem.obliterate(false, false, deleteOldStems.getDeletePointInTime());
  
                foldersDeleted++;
              } else {
                if (count < 30) {
                  GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem." + stem.getExtension() + ".notOldEnoughToDelete", true);
                } else {
                  GrouperLoaderLogger.addLogEntry(LOG_LABEL, "onlyLogging30stems", true);
                }
              }
            } catch (Exception e) {
              LOG.error("Error obliterating: " + stem.getName(), e);
              if (GrouperUtil.length(error) == 1) {
                error[0] = true;
              }
              if (jobMessage != null) {
                jobMessage.append("Error in folder: " + stem.getName() + ", " + ExceptionUtils.getFullStackTrace(e));
              }
              GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem." + stem.getExtension() + ".error", ExceptionUtils.getFullStackTrace(e));
            }
          }
          
        } catch (Exception e) {
          LOG.error("Error obliterating: " + folderName, e);
          if (GrouperUtil.length(error) == 1) {
            error[0] = true;
          }
          if (jobMessage != null) {
            jobMessage.append("Error in folder: " + folderName + ", " + ExceptionUtils.getFullStackTrace(e));
          }
          GrouperLoaderLogger.addLogEntry(LOG_LABEL, "obliterateOldStems." + index + ".stem.error", ExceptionUtils.getFullStackTrace(e));
          
        }
        
        index++;
      }
      
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(LOG_LABEL);
      }
    }
    return foldersDeleted;
  }


}
