/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.2 2008-05-13 07:11:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.loader.db.Hib3GrouploaderLog;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderHibUtils;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * type of loaders (e.g. sql simple)
 */
public enum GrouperLoaderType {

  /** 
   * simple sql query where all results are all members of group.
   * must have a subject_id col, and optionally a subject_source_id col
   */
  SQL_SIMPLE {
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeRequired(java.lang.String)
     */
    @Override
    public boolean attributeRequired(String attributeName) {
      return StringUtils.equals(GrouperLoader.GROUPER_LOADER_DB_NAME, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUERY, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, attributeName);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeOptional(java.lang.String)
     */
    @Override
    public boolean attributeOptional(String attributeName) {
      return StringUtils.equals(GrouperLoader.GROUPER_LOADER_PRIORITY, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, attributeName);
    }
    
    /**
     * sync up a group membership based on query and db
     * @param groupName
     * @param grouperLoaderDb
     * @param query
     * @param hib3GrouploaderLog
     * @param startTime
     */
    @SuppressWarnings("unchecked")
    @Override
    public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
        Hib3GrouploaderLog hib3GrouploaderLog, long startTime) {
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(grouperLoaderDb, query);
      
      hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis()-startTime));

      long startTimeLoadData = System.currentTimeMillis();
      
      //get group
      GrouperSession grouperSession = null;
      
      //assume success
      GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
      
      try {
        grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        );
 
        final Group group = GroupFinder.findByName(grouperSession, groupName);
        
        final Set<Member> currentMembers = group.getImmediateMembers();
        
        //now lets remove data from each since the member is there and is supposed to be there
        Iterator<Member> iterator = currentMembers.iterator();
        
        while (iterator.hasNext()) {
          
          Member member = iterator.next();
          //see if it is in the current list
          if (grouperLoaderResultset.remove(member.getSubjectId(), member.getSubjectSourceId())) {
            //if so, then remove, no need to change
            iterator.remove();
          }
        }
        
        //lets lookup the subjects first
        final Set<Subject> subjectsToAdd = new HashSet<Subject>();
        
        String defaultSubjectSourceId = GrouperLoaderConfig.getPropertyString(GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);
        
        //here are new members
        for (int i=0;i<grouperLoaderResultset.numberOfRows();i++) {
          
          String subjectId = (String)grouperLoaderResultset.getCell(i, GrouperLoaderResultset.SUBJECT_ID_COL, true);
          String subjectSourceId = (String)grouperLoaderResultset.getCell(i, GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);

          //maybe get the sourceId from config file
          subjectSourceId = StringUtils.defaultString(subjectSourceId, defaultSubjectSourceId);
          Subject subject = null;
          try {
            if (!StringUtils.isBlank(subjectSourceId)) {
              subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId);
            } else {
              subject = SubjectFinder.findById(subjectId);
            }
            subjectsToAdd.add(subject);
          } catch (Exception e) {
            GrouperUtil.injectInException(e, "Problem with subjectId: " + subjectId + ", subjectSourceId: " + subjectSourceId);
            LOG.error(e.getMessage(), e);
            if (e instanceof SubjectNotFoundException
                || e instanceof SubjectNotUniqueException
                || e instanceof SourceUnavailableException) {
              
              hib3GrouploaderLog.incrementUnresolvableSubjectCount();
              //put something in log
              hib3GrouploaderLog.appendJobMessage("unresolvable: " + subjectId + (subjectSourceId == null ? "" : (", " + subjectSourceId)) + ", ");
              status = GrouperLoaderStatus.SUBJECT_PROBLEMS;
            } else {
              if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
              }
              //this shouldnt really be possible
              throw new RuntimeException(e);
            }
             
          }
        }
        
        //here are members to remove
        final Set<Subject> subjectsToRemove = new HashSet<Subject>();
        //first remove members
        for (Member member : currentMembers) {
          subjectsToRemove.add(member.getSubject());
        }
        
        //now the currentMembers is full of members to remove, and the grouperLoaderResultset is full
        //of members to add
        //start a transaction
        GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            try {
              //first remove members
              for (Subject subject : subjectsToRemove) {
                try {
                  group.deleteMember(subject);
                } catch (Exception e) {
                  GrouperUtil.injectInException(e, "Problem with " + GrouperLoaderUtils.subjectToString(subject) + ", ");
                  throw e;
                }
              }
              
              //then add new members
              for (Subject subject : subjectsToAdd) {
                try {
                  group.addMember(subject);
                } catch (Exception e) {
                  GrouperUtil.injectInException(e, "Problem with " + GrouperLoaderUtils.subjectToString(subject) + ", ");
                  throw e;
                }
              }

              grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            
            
            return null;
          }
          
        });
        hib3GrouploaderLog.setStatus(status.name());
      } catch (Exception e) {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        hib3GrouploaderLog.insertJobMessage(ExceptionUtils.getFullStackTrace(e));
        throw new RuntimeException("Problem with group: " + groupName, e);
      } finally {
        GrouperSession.stopQuietly(grouperSession);
        hib3GrouploaderLog.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
      }
      
      
    }
  }, 
  
  /** 
   * various maintenance jobs on the system
   */
  MAINTENANCE{
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        return false;
      }
  
      /**
       * 
       * @see edu.internet2.middleware.grouper.loader.GrouperLoaderType#attributeOptional(java.lang.String)
       */
      @Override
      public boolean attributeOptional(String attributeName) {
        return false;
      }
      
      /**
       * sync up a group membership based on query and db
       * @param groupName
       * @param grouperLoaderDb
       * @param query
       * @param hib3GrouploaderLog
       * @param startTime
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
          Hib3GrouploaderLog hib3GrouploaderLog, long startTime) {
        
        if (StringUtils.equals(MAINTENANCE_CLEAN_LOGS, hib3GrouploaderLog.getJobName())) {
          int daysToKeepLogs = GrouperLoaderConfig.getPropertyInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
          if (daysToKeepLogs != -1) {
            //lets get a date
            Calendar calendar = GregorianCalendar.getInstance();
            //get however many days in the past
            calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
            //run a query to delete (note, dont retrieve records to java, just delete)
            int records = GrouperLoaderHibUtils.executeSql("delete from grouploader_log where last_updated < ?", 
                GrouperLoaderUtils.listObject(new Timestamp(calendar.getTimeInMillis())));
            hib3GrouploaderLog.setJobMessage("Deleted " + records + " records from grouploader_log older than " + daysToKeepLogs + " days old");
          } else {
            hib3GrouploaderLog.setJobMessage("Configured to not delete records from grouploader_log table");
          }
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        }
        
      }
    };
  
  /**
   * 
   */
  public static final String MAINTENANCE_CLEAN_LOGS = "jobMaintenance_cleanLogs";

  /**
   * see if an attribute if required or not
   * @param attributeName
   * @return true if required, false if not
   */
  public abstract boolean attributeRequired(String attributeName);

  /**
   * sync up a group membership based on query and db
   * @param groupName
   * @param grouperLoaderDb
   * @param query
   * @param hib3GrouploaderLog 
   * @param startTime 
   */
  public abstract void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
      Hib3GrouploaderLog hib3GrouploaderLog, long startTime);
  
  /**
   * see if an attribute if optional or not (if not, then it is either required or forbidden)
   * @param attributeName
   * @return true if optional, false if not
   */
  public abstract boolean attributeOptional(String attributeName);
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoaderType.class);

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  private Integer attributeValueValidateRequiredInteger(Group group, String attributeName) {
    String attributeValueString = StringUtils.trim(attributeValueValidateRequired(group, attributeName));
    return GrouperUtil.intObjectValue(attributeValueString, true);
  }

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  private String attributeValueValidateRequired(Group group, String attributeName) {
    
    String attributeValue = GrouperLoaderUtils.groupGetAttribute(group, attributeName);
    
    boolean hasValue = StringUtils.isNotBlank(attributeValue);
    boolean isRequired = this.attributeRequired(attributeName);
    boolean isOptional = this.attributeOptional(attributeName);
    
    //must have value if required
    if (!hasValue && isRequired) {
      throw new RuntimeException("Attribute '" + attributeName + "' is required, but is not set for loader type: " 
          + this.name() + ", groupName: " + group.getName());
    }
    
    // must not have value if not required or optional
    if (hasValue && !isRequired && !isOptional) {
      LOG.error("Attribute '" + attributeName + "' is not required or optional, " +
      		"but is set to '" + attributeValue + "' for loader type: " 
          + this.name() + ", groupName: " + group.getName());
    }
    return attributeValue;
  }
  
  /**
   * for all jobs in this loader type, schedule them with quartz
   */
  public static void scheduleLoads() {
    
    Set<Group> groups = retrieveGroups();
    
    for (Group group : groups) {
      
      String jobName = null;
      String groupUuid = null;
      String grouperLoaderScheduleType = null;
      String grouperLoaderQuartzCron = null;
      Integer grouperLoaderIntervalSeconds = null;
      Integer grouperLoaderPriority = null;
      String grouperLoaderType = null;
      try {
        
        jobName = "jobForGroup_" + group.getName();
        groupUuid = group.getUuid();
        //lets get all attribute values
        grouperLoaderType = GrouperLoaderUtils.groupGetAttribute(group, GrouperLoader.GROUPER_LOADER_TYPE);
        
        GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);

        String grouperLoaderDbName = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_DB_NAME);
        String grouperLoaderQuery = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUERY);
        grouperLoaderScheduleType = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
        grouperLoaderQuartzCron = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
        grouperLoaderIntervalSeconds = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
        grouperLoaderPriority = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_PRIORITY);
        
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail(jobName, null, GrouperLoaderJob.class);

        //set data for the job to execute
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_GROUP_NAME, group.getName());
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_GROUP_UUID, groupUuid);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_TYPE, grouperLoaderType);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_DB_NAME, grouperLoaderDbName);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_QUERY, grouperLoaderQuery);
        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, 
            grouperLoaderScheduleType);
//        jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, 
//            grouperLoaderQuartzCron);
        //put as string since getting as integer will require it to be string
//        if (grouperLoaderIntervalSeconds != null) {
//          jobDetail.getJobDataMap().putAsString(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, 
//              grouperLoaderIntervalSeconds);
//        } else {
//          jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, 
//              grouperLoaderIntervalSeconds);
//        }
//        if (grouperLoaderPriority != null) {
//          //put as string since getting as integer will require it to be string
//          jobDetail.getJobDataMap().putAsString(GrouperLoader.GROUPER_LOADER_PRIORITY, 
//              grouperLoaderPriority);
//        } else {
//          jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_PRIORITY, 
//              grouperLoaderPriority);
//          
//        }
        //schedule this job based on the schedule type and params
        GrouperLoaderScheduleType grouperLoaderScheduleTypeEnum = GrouperLoaderScheduleType
          .valueOfIgnoreCase(grouperLoaderScheduleType, true);
        
        Trigger trigger = grouperLoaderScheduleTypeEnum.createTrigger(grouperLoaderQuartzCron, grouperLoaderIntervalSeconds);
        
        trigger.setName("triggerForGroup_" + group.getName());
        
        //if there is a priority, set it
        if (grouperLoaderPriority != null) {
          trigger.setPriority(grouperLoaderPriority);
        }

        scheduler.scheduleJob(jobDetail, trigger);

        
      } catch (Exception e) {
        String errorMessage = null;
        
        //dont fail on all if any fail
        try {
          errorMessage = "Could not schedule group: '" + group.getName() + "', '" + group.getUuid() + "'";
          LOG.error(errorMessage, e);
          errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
        } catch (Exception e2) {
          errorMessage = "Could not schedule group.";
          //dont let error message mess us up
          LOG.error(errorMessage, e);
          LOG.error(e2);
          errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e) + "\n" + ExceptionUtils.getFullStackTrace(e2);
        }
        try {
          //lets enter a log entry so it shows up as error in the db
          Hib3GrouploaderLog hib3GrouploaderLog = new Hib3GrouploaderLog();
          hib3GrouploaderLog.setGroupUuid(groupUuid);
          hib3GrouploaderLog.setHost(GrouperLoaderUtils.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(jobName);
          hib3GrouploaderLog.setJobScheduleIntervalSeconds(grouperLoaderIntervalSeconds);
          hib3GrouploaderLog.setJobSchedulePriority(grouperLoaderPriority);
          hib3GrouploaderLog.setJobScheduleQuartzCron(grouperLoaderQuartzCron);
          hib3GrouploaderLog.setJobScheduleType(grouperLoaderScheduleType);
          hib3GrouploaderLog.setJobType(grouperLoaderType);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();
          
        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }
      }
      
      
    }
    
  }

  /**
   * retrieve all loader groups from the db
   * @return the groups (will not return null, only the empty set if none)
   */
  @SuppressWarnings("unchecked")
  private static Set<Group> retrieveGroups() {
    try {
      GrouperSession grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        );
      //find all groups with the attribute with this type
//      Set<Group> groupSet = new GroupAttributeExactFilter(GrouperLoader.GROUPER_LOADER_TYPE, this.name(), 
//          StemFinder.findRootStem(grouperSession)).getResults(grouperSession);
//      return GrouperUtil.nonNull(groupSet);
      Set<Group> groupSet = GroupFinder.findAllByType(grouperSession, GroupTypeFinder.find("grouperLoader"));
      return groupSet;
    } catch (Exception e) {
      throw new RuntimeException("Problem with finding loader groups", e);
    }
    
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperLoaderType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperLoaderType.class, 
        string, exceptionOnNull);

  }

}
