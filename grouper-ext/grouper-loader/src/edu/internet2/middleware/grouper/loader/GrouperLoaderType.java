/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.1 2008-06-01 21:27:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderResultset.Row;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderHibUtils;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


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
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_AND_GROUPS, attributeName)
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
        Hib3GrouploaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
        List<Group> andGroups) {
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(grouperLoaderDb, query);
      
      syncOneGroupMembership(groupName, hib3GrouploaderLog, startTime,
          grouperLoaderResultset, false, grouperSession, andGroups);
      
      
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
       * @param grouperSession
       * @param andGroups
       * 
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
          Hib3GrouploaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, List<Group> andGroups) {
        
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
    }, 
    
    /** 
     * sql query where there is a column for group_name (which is the
     * extension of each stem, and the extension of the group, separated
     * by colons)
     * must have a subject_id col, and optionally a subject_source_id col.
     * note the query should have no order by, and if there is a where clause, it
     * should have "where" separated by whitespace on both side so it can be detected
     */
    SQL_GROUP_LIST {
      
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
            || StringUtils.equals(GrouperLoader.GROUPER_LOADER_AND_GROUPS, attributeName)
            || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, attributeName);
      }
      
      /**
       * sync up a group membership based on query and db
       * @param groupNameOverall
       * @param grouperLoaderDb
       * @param query
       * @param hib3GrouploaderLogOverall
       * @param startTime
       * @param grouperSession
       * @param andGroups
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(String groupNameOverall, GrouperLoaderDb grouperLoaderDb, String query, 
          Hib3GrouploaderLog hib3GrouploaderLogOverall, long startTime, GrouperSession grouperSession, 
          List<Group> andGroups) {
        
        long startTimeLoadData = 0;
        GrouperLoaderStatus statusOverall = GrouperLoaderStatus.SUCCESS;
        
        try {
          //get a resultset from the db
          final GrouperLoaderResultset grouperLoaderResultsetOverall = new GrouperLoaderResultset(grouperLoaderDb, 
              query + " order by group_name");
          
          hib3GrouploaderLogOverall.setMillisGetData((int)(System.currentTimeMillis()-startTime));

          startTimeLoadData =  System.currentTimeMillis();

          Set<String> groupNames = grouperLoaderResultsetOverall.groupNames();
          
          for (String groupName : groupNames) {
            
            Hib3GrouploaderLog hib3GrouploaderLog = new Hib3GrouploaderLog();
            long groupStartedMillis = System.currentTimeMillis();
            try {
              GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(
                  grouperLoaderResultsetOverall, groupName);
              //make a new log object for this one subgroup
              
              hib3GrouploaderLog.setHost(GrouperLoaderUtils.hostname());
              hib3GrouploaderLog.setJobName("subjobFor_" + groupName);
              hib3GrouploaderLog.setStartedTime(new Timestamp(groupStartedMillis));
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
              hib3GrouploaderLog.setParentJobId(hib3GrouploaderLogOverall.getId());
              hib3GrouploaderLog.setParentJobName(hib3GrouploaderLogOverall.getJobName());
              
              hib3GrouploaderLog.store();
              
              //based on type, run query from the db and sync members
              syncOneGroupMembership(groupName, hib3GrouploaderLog, groupStartedMillis,
                  grouperLoaderResultset, true, grouperSession, andGroups);
              
              long endTime = groupStartedMillis;
              hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
              hib3GrouploaderLog.setMillis((int)(endTime-groupStartedMillis));
              
            } catch (Exception e) {
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
            }
            hib3GrouploaderLog.store();
            
            //reconcile overall status
            //just take the first non-success code, but error trumps all
            GrouperLoaderStatus groupStatus = GrouperLoaderStatus.valueOfIgnoreCase(hib3GrouploaderLog.getStatus(), true);
            //default to error
            groupStatus = GrouperUtil.defaultIfNull(groupStatus, GrouperLoaderStatus.ERROR);
            if (GrouperLoaderStatus.ERROR.equals(groupStatus) 
                || statusOverall == GrouperLoaderStatus.SUCCESS) {
              statusOverall = groupStatus;
            }
            
            //count all the stats
            hib3GrouploaderLogOverall.addDeleteCount(hib3GrouploaderLog.getDeleteCount());
            hib3GrouploaderLogOverall.addInsertCount(hib3GrouploaderLog.getInsertCount());
            hib3GrouploaderLogOverall.addUpdateCount(hib3GrouploaderLog.getUpdateCount());
            hib3GrouploaderLogOverall.addTotalCount(hib3GrouploaderLog.getTotalCount());
            hib3GrouploaderLogOverall.addUnresolvableSubjectCount(hib3GrouploaderLog.getUnresolvableSubjectCount());
          }
        } finally {
          hib3GrouploaderLogOverall.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
          hib3GrouploaderLogOverall.setStatus(statusOverall.name());
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
   * @param grouperSession grouper session
   * @param andGroups groups whose memberships should be anded with the group in question
   */
  public abstract void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
      Hib3GrouploaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, List<Group> andGroups);
  
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
   * @param groupName
   * @param hib3GrouploaderLog
   * @param startTime
   * @param grouperLoaderResultset
   * @param groupList if this is a list of groups, then do something else with group name and the resultset
   * @param grouperSession 
   * @param andGroups 
   */
  @SuppressWarnings("unchecked")
  protected static void syncOneGroupMembership(String groupName,
      Hib3GrouploaderLog hib3GrouploaderLog, long startTime,
      final GrouperLoaderResultset grouperLoaderResultset, boolean groupList,
      GrouperSession grouperSession, List<Group> andGroups) {
    
    hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis()-startTime));

    long startTimeLoadData = System.currentTimeMillis();
    
    //assume success
    GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
    
    try {

      hib3GrouploaderLog.setTotalCount(grouperLoaderResultset.numberOfRows());

      String groupExtension = GrouperUtil.extensionFromName(groupName);
      
      final Group group = groupList ?
          Group.saveGroup(grouperSession, groupName, null, groupName, groupExtension, 
              groupExtension + " auto-created by grouperLoader", null, true)
          : GroupFinder.findByName(grouperSession, groupName);
      
      hib3GrouploaderLog.setGroupUuid(group.getUuid());

      final Set<Member> currentMembers = group.getImmediateMembers();
      
      //now lets remove data from each since the member is there and is supposed to be there
      Iterator<Member> iterator = currentMembers.iterator();
      
      while (iterator.hasNext()) {
        
        Member member = iterator.next();
        //see if it is in the current list
        Row row = grouperLoaderResultset.find(member.getSubjectId(), member.getSubjectSourceId());
        
        //this means the member exists in query, and in membership, so maybe do nothing
        if (row != null) {
          boolean andGroupsDoesntHaveSubject = false;
          if (andGroups.size() > 0) {
            Subject subject = row.getSubject();
            if (subject == null) {
              //keep track
              hib3GrouploaderLog.addUnresolvableSubjectCount(1);
              hib3GrouploaderLog.appendJobMessage(row.getSubjectError());
            } else {
              for (Group andGroup : andGroups) {
                if (!andGroup.hasMember(subject)) {
                  andGroupsDoesntHaveSubject = true;
                  hib3GrouploaderLog.addTotalCount(-1);
                  break;
                }
              }
            }
          }
          if (!andGroupsDoesntHaveSubject) {
            //if and groups is ok, then dont do anything with record
            iterator.remove();
          }
          //either way, we are done with the record in the resultset
          grouperLoaderResultset.remove(row);
        }
      }
      
      //lets lookup the subjects first
      final Set<Subject> subjectsToAdd = new HashSet<Subject>();
      
      //here are new members
      for (int i=0;i<grouperLoaderResultset.numberOfRows();i++) {
        
        Row row = grouperLoaderResultset.retrieveRow(i);
        Subject subject = row.getSubject();
        if (subject != null) {
          //make sure it is not in the restricted list
          boolean andGroupsDoesntHaveSubject = false;
          for (Group andGroup : andGroups) {
            if (!andGroup.hasMember(subject)) {
              andGroupsDoesntHaveSubject = true;
              hib3GrouploaderLog.addTotalCount(-1);
              break;
            }
          }
          if (!andGroupsDoesntHaveSubject) {
            subjectsToAdd.add(subject);
          }
        } else {
          
          //put something in log
          hib3GrouploaderLog.appendJobMessage(row.getSubjectError());
          hib3GrouploaderLog.addUnresolvableSubjectCount(1);
          status = GrouperLoaderStatus.SUBJECT_PROBLEMS;
           
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
                GrouperUtil.injectInException(e, "Problem with " 
                    + GrouperLoaderUtils.subjectToString(subject) + ", ");
                throw e;
              }
            }
            
            //then add new members
            for (Subject subject : subjectsToAdd) {
              try {
                group.addMember(subject);
              } catch (Exception e) {
                GrouperUtil.injectInException(e, "Problem with " 
                    + GrouperLoaderUtils.subjectToString(subject) + ", ");
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
      hib3GrouploaderLog.setInsertCount(subjectsToAdd.size());
      hib3GrouploaderLog.setDeleteCount(subjectsToRemove.size());
      hib3GrouploaderLog.setStatus(status.name());
    } catch (Exception e) {
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.insertJobMessage(ExceptionUtils.getFullStackTrace(e));
      throw new RuntimeException("Problem with group: " + groupName, e);
    } finally {
      hib3GrouploaderLog.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
    }
  }

  /**
   * for all jobs in this loader type, schedule them with quartz
   */
  public static void scheduleLoads() {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        );

      Set<Group> groups = retrieveGroups(grouperSession);
      
      for (Group group : groups) {
        
        String jobName = null;
        String groupUuid = null;
        String grouperLoaderScheduleType = null;
        String grouperLoaderAndGroups = null;
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
          grouperLoaderAndGroups = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_AND_GROUPS);
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
          jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_AND_GROUPS, grouperLoaderAndGroups);
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
            hib3GrouploaderLog.setAndGroupNames(grouperLoaderAndGroups);
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * retrieve all loader groups from the db
   * @return the groups (will not return null, only the empty set if none)
   * @param grouperSession
   */
  @SuppressWarnings("unchecked")
  private static Set<Group> retrieveGroups(GrouperSession grouperSession) {
    try {
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
