/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.8 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset.Row;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperReport;
import edu.internet2.middleware.grouper.util.GrouperEmail;
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
     * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
     */
    @Override
    public boolean attributeRequired(String attributeName) {
      return StringUtils.equals(GrouperLoader.GROUPER_LOADER_DB_NAME, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUERY, attributeName)
          || StringUtils.equals(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, attributeName);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
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
     * @param groupTypes comma separated group types
     * @param groupLikeString locates groups being managed so we can delete if necessary
     */
    @SuppressWarnings("unchecked")
    @Override
    public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
        Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
        List<Group> andGroups, List<GroupType> groupTypes, String groupLikeString) {
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(grouperLoaderDb, query);
      
      syncOneGroupMembership(groupName, hib3GrouploaderLog, startTime,
          grouperLoaderResultset, false, grouperSession, andGroups, groupTypes);
      
      
    }
  }, 
  
  /** 
   * various maintenance jobs on the system
   */
  MAINTENANCE{
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        return false;
      }
  
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
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
       * @param groupTypes comma separated group types
       * @param groupLikeString locates groups being managed so we can delete if necessary
       * 
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
          Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, List<Group> andGroups,
          List<GroupType> groupTypes, String groupLikeString) {
        
        if (StringUtils.equals(MAINTENANCE_CLEAN_LOGS, hib3GrouploaderLog.getJobName())) {
          int daysToKeepLogs = GrouperLoaderConfig.getPropertyInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
          if (daysToKeepLogs != -1) {
            //lets get a date
            Calendar calendar = GregorianCalendar.getInstance();
            //get however many days in the past
            calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
            //run a query to delete (note, dont retrieve records to java, just delete)
            int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where last_updated < ?", 
                (List<Object>)(Object)GrouperUtil.toList(new Timestamp(calendar.getTimeInMillis())));
            hib3GrouploaderLog.setJobMessage("Deleted " + records + " records from grouper_loader_log older than " + daysToKeepLogs + " days old");
          } else {
            hib3GrouploaderLog.setJobMessage("Configured to not delete records from grouper_loader_log table");
          }
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        }
        
        if (StringUtils.equals(GROUPER_REPORT, hib3GrouploaderLog.getJobName())) {


          //how often to run usdu
          String usduSchedule = StringUtils.defaultString(GrouperLoaderConfig.getPropertyString(
              "daily.report.usdu.daysToRun")).toLowerCase();
          String badMemberSchedule = StringUtils.defaultString(GrouperLoaderConfig.getPropertyString(
              "daily.report.badMembership.daysToRun")).toLowerCase();
          
          boolean isRunUsdu = dayListContainsToday(usduSchedule);
          boolean isRunBadMember = dayListContainsToday(badMemberSchedule);
          
          String emailTo = GrouperLoaderConfig.getPropertyString("daily.report.emailTo");
          if (StringUtils.isBlank(emailTo)) {
            throw new RuntimeException("grouper-loader.properties property daily.report.emailTo needs to be filled in");
          }
          
          String report = GrouperReport.report(isRunUsdu, isRunBadMember);
          
          new GrouperEmail().setBody(report).setSubject("Grouper report").setTo(emailTo).send();
          
          
          hib3GrouploaderLog.setJobMessage("Ran the grouper report, isRunUnresolvable: " 
              + isRunUsdu + ", isRunBadMembershipFinder: " + isRunBadMember + ", sent to: " + emailTo);
          
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
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        return StringUtils.equals(GrouperLoader.GROUPER_LOADER_DB_NAME, attributeName)
            || StringUtils.equals(GrouperLoader.GROUPER_LOADER_QUERY, attributeName)
            || StringUtils.equals(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, attributeName);
      }
  
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
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
       * @param groupTypes group types to add to loader managed group
       * @param groupLikeString locates groups being managed so we can delete if necessary
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(String groupNameOverall, GrouperLoaderDb grouperLoaderDb, String query, 
          Hib3GrouperLoaderLog hib3GrouploaderLogOverall, long startTime, GrouperSession grouperSession, 
          List<Group> andGroups, List<GroupType> groupTypes, String groupLikeString) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupNameOverall + ": start syncing membership");
        }
        
        long startTimeLoadData = 0;
        GrouperLoaderStatus statusOverall = GrouperLoaderStatus.SUCCESS;
        
        try {
          //get a resultset from the db
          final GrouperLoaderResultset grouperLoaderResultsetOverall = new GrouperLoaderResultset(grouperLoaderDb, 
              query + " order by group_name");
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": found " + grouperLoaderResultsetOverall + " members overall");
          }
          
          hib3GrouploaderLogOverall.setMillisGetData((int)(System.currentTimeMillis()-startTime));

          startTimeLoadData =  System.currentTimeMillis();

          Set<String> groupNames = grouperLoaderResultsetOverall.groupNames();
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": syncing membership for " + groupNames.size() + " groups");
          }
          
          int count=1;
          
          for (String groupName : groupNames) {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(groupNameOverall + ": syncing membership for " + groupName + " " + count + " out of " + groupNames.size() + " groups");
            }
            
            Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
            long groupStartedMillis = System.currentTimeMillis();
            try {
              GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(
                  grouperLoaderResultsetOverall, groupName);
              //make a new log object for this one subgroup
              
              hib3GrouploaderLog.setHost(GrouperUtil.hostname());
              hib3GrouploaderLog.setJobName("subjobFor_" + groupName);
              hib3GrouploaderLog.setStartedTime(new Timestamp(groupStartedMillis));
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
              hib3GrouploaderLog.setParentJobId(hib3GrouploaderLogOverall.getId());
              hib3GrouploaderLog.setParentJobName(hib3GrouploaderLogOverall.getJobName());
              
              hib3GrouploaderLog.store();
              
              //based on type, run query from the db and sync members
              syncOneGroupMembership(groupName, hib3GrouploaderLog, groupStartedMillis,
                  grouperLoaderResultset, true, grouperSession, andGroups, groupTypes);
              
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
            //store after each group to get progress
            hib3GrouploaderLogOverall.store();
            count++;
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": done syncing membership");
          }

        } finally {
          hib3GrouploaderLogOverall.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
          hib3GrouploaderLogOverall.setStatus(statusOverall.name());
        }
      }
    };
  
  /**
   * if this job name is for this type
   * @param jobName
   * @return true if this name is for this type
   */
  public boolean nameForThisType(String jobName) {
    return jobName.startsWith(this.name());
  }

  /**
   * return the type for this job name
   * @param jobName
   * @return the type
   */
  public static GrouperLoaderType typeForThisName(String jobName) {
    for (GrouperLoaderType grouperLoaderType : GrouperLoaderType.values()) {
      if (grouperLoaderType.nameForThisType(jobName)) {
        return grouperLoaderType;
      }
    }
    throw new RuntimeException("Cant fine job type for this name: " + jobName);
  }
  
  /**
   * maintenance clean logs name
   */
  public static final String MAINTENANCE_CLEAN_LOGS = GrouperLoaderType.MAINTENANCE.name() + "_cleanLogs";

  /**
   * maintenance clean logs name
   */
  public static final String GROUPER_REPORT = GrouperLoaderType.MAINTENANCE.name() + "_grouperReport";

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
   * @param groupTypes group types to add to loader managed groups
   * @param groupLikeString locates groups being managed so we can delete if necessary
   */
  public abstract void syncGroupMembership(String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, List<Group> andGroups,
      List<GroupType> groupTypes, String groupLikeString);
  
  /**
   * see if an attribute if optional or not (if not, then it is either required or forbidden)
   * @param attributeName
   * @return true if optional, false if not
   */
  public abstract boolean attributeOptional(String attributeName);
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderType.class);

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  Integer attributeValueValidateRequiredInteger(Group group, String attributeName) {
    String attributeValueString = StringUtils.trim(attributeValueValidateRequired(group, attributeName));
    return GrouperUtil.intObjectValue(attributeValueString, true);
  }

  /**
   * get an attribute value, or null, or a default if exists
   * @param group
   * @param attributeName
   * @return the attribute value
   */
  public static String attributeValueOrDefaultOrNull(Group group, String attributeName) {
    
    String attributeValue = group.getAttributeOrNull(attributeName);
    
    //if value, go with that
    if (!StringUtils.isBlank(attributeValue)) {
      return attributeValue;
    }
    
    if (StringUtils.equals(GrouperLoader.GROUPER_LOADER_TYPE, attributeName)) {
      String query = group.getAttributeOrNull(GrouperLoader.GROUPER_LOADER_QUERY);
      if (!StringUtils.isBlank(query)) {
        query = query.toLowerCase();
        String preFrom = GrouperUtil.prefixOrSuffix(query, "from", true);
        if (preFrom.contains("group_name")) {
          return GrouperLoaderType.SQL_GROUP_LIST.name();
        }
        return GrouperLoaderType.SQL_SIMPLE.name();
      }
    }
    
    if (StringUtils.equals(GrouperLoader.GROUPER_LOADER_DB_NAME, attributeName)) {
      String grouperLoaderTypeString = attributeValueOrDefaultOrNull(group, GrouperLoader.GROUPER_LOADER_TYPE);
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, false);
      
      if (grouperLoaderType != null && 
          (grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST) 
              || grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE))) {
        //assume default database
        return "grouper";
      }
    }

    if (StringUtils.equals(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, attributeName)) {
      String cron = group.getAttributeOrNull(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
      boolean hasCron = StringUtils.isNotBlank(cron); 
      String intervalSeconds = group.getAttributeOrNull(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
      boolean hasIntervalSeconds = StringUtils.isNotBlank(intervalSeconds);
      
      if (!hasCron && !hasIntervalSeconds) {
        return GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
      }
      
    }
    
    if (StringUtils.equals(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, attributeName)) {
      String scheduleTypeString = attributeValueOrDefaultOrNull(group, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.valueOfIgnoreCase(scheduleTypeString, false);
      if (grouperLoaderScheduleType != null && grouperLoaderScheduleType.equals(GrouperLoaderScheduleType.START_TO_START_INTERVAL)) {

        //default to 1 day
        return Integer.toString(60 * 60 * 24);
      }
    }

    return attributeValue;
  }
  
  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param group is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  String attributeValueValidateRequired(Group group, String attributeName) {
    
    String attributeValue = group.getAttributeOrNull(attributeName);
    
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
   * 
   * @param dayList
   * @return true if today is in day list, false if not
   */
  public boolean dayListContainsToday(String dayList) {
    if (StringUtils.isBlank(dayList)) {
      return false;
    }
    String weekday = new SimpleDateFormat("EEEE").format(new Date()).toLowerCase();

    dayList = dayList.toLowerCase();
    String[] days = GrouperUtil.splitTrim(dayList, ",");
    for (String day : days) {
      if (StringUtils.equals(weekday, day) || weekday.startsWith(day)) {
        return true;
      }
    }
    LOG.debug("Day: " + weekday + " is not in daylist: " + dayList);
    return false;
  }
  
  /**
   * @param groupName
   * @param hib3GrouploaderLog
   * @param startTime
   * @param grouperLoaderResultset
   * @param groupList if this is a list of groups, then do something else with group name and the resultset
   * @param grouperSession 
   * @param andGroups 
   * @param groupTypes comma separated group types
   */
  @SuppressWarnings("unchecked")
  protected static void syncOneGroupMembership(final String groupName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime,
      final GrouperLoaderResultset grouperLoaderResultset, boolean groupList,
      final GrouperSession grouperSession, List<Group> andGroups, List<GroupType> groupTypes) {
    
    //keep this separate so we can prepend stuff inside...
    final StringBuilder jobMessage = new StringBuilder(StringUtils.defaultString(hib3GrouploaderLog.getJobMessage()));
    
    final String[] jobStatus = new String[1];
    
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    if (LOG.isDebugEnabled()) {
      LOG.debug(groupName + " start syncing membership");
    }
    
    hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis()-startTime));

    long startTimeLoadData = System.currentTimeMillis();
    
    int totalCount = 0;
    
    //assume success
    GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
    
    try {

      int numberOfRows = grouperLoaderResultset.numberOfRows();
      hib3GrouploaderLog.setTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(groupName + " syncing " + numberOfRows + " rows");
      }

      String groupExtension = GrouperUtil.extensionFromName(groupName);
      
      final Group[] group = new Group[]{groupList ?
          Group.saveGroup(grouperSession, groupName, null, groupName, groupExtension, 
              groupExtension + " auto-created by grouperLoader", null, true)
          : GroupFinder.findByName(grouperSession, groupName)};
      
      //see if we are adding types
      if (GrouperUtil.length(groupTypes) > 0) {
        for (GroupType groupType : groupTypes) {
          boolean added = group[0].addType(groupType, false);
          if (added) {
            LOG.debug("Added type: " + groupType.getName() + " to group: " + group[0].getName());
          }
        }
      }
      
      hib3GrouploaderLog.setGroupUuid(group[0].getUuid());

      final Set<Member> currentMembers = group[0].getImmediateMembers();
      
      //now lets remove data from each since the member is there and is supposed to be there
      Iterator<Member> iterator = currentMembers.iterator();
      
      int count = 0;
      
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
              if (LOG.isDebugEnabled()) {
                LOG.debug(groupName + " found unresolvable subject: " + row.getSubjectError() + ", " + count + " of " + numberOfRows + " subjects");
              }
              //keep track
              hib3GrouploaderLog.addUnresolvableSubjectCount(1);
              jobMessage.append(row.getSubjectError());
              hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
            } else {
              for (Group andGroup : andGroups) {
                if (!andGroup.hasMember(subject)) {
                  if (LOG.isDebugEnabled()) {
                    LOG.debug(groupName + " subject not in andGroup: " + subject.getSource().getName() + "/" + subject.getId() + ", " + count + " of " + numberOfRows + " subjects");
                  }
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
        count++;
        totalCount++;
        if (totalCount != 0 && totalCount % 500 == 0) {
          String logStatus = groupName + " processed " + totalCount + " records, finding new members to remove, " + count + " of " + numberOfRows + " subjects";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }
      }
      
      //lets lookup the subjects first
      final Set<Subject> subjectsToAdd = new HashSet<Subject>();
      
      //here are new members
      numberOfRows = grouperLoaderResultset.numberOfRows();
      count = 1;
      for (int i=0;i<numberOfRows;i++) {
        
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
            if (LOG.isDebugEnabled()) {
              LOG.debug(groupName + " will add subject to group: " + subject.getSource().getName() + "/" + subject.getId() + ", " + count + " of " + numberOfRows + " subjects");
            }
            subjectsToAdd.add(subject);
          }
        } else {
          
          //put something in log
          hib3GrouploaderLog.appendJobMessage(row.getSubjectError());
          hib3GrouploaderLog.addUnresolvableSubjectCount(1);
          status = GrouperLoaderStatus.SUBJECT_PROBLEMS;
           
        }
        count++;
        totalCount++;
        
        if (totalCount != 0 && totalCount % 500 == 0) {
          String logStatus = groupName + " processed " + totalCount + " records, finding new members, " + count + " of " + numberOfRows + " subjects";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }

      }
      
      
      //here are members to remove
      final Set<Member> membersToRemove = new HashSet<Member>();
      numberOfRows = currentMembers.size();
      count = 1;
      //first remove members
      for (Member member : currentMembers) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupName + " will remove subject from group: " + member.getSubjectSourceIdDb() + "/" + member.getSubjectIdDb() + ", " + count + " of " + numberOfRows + " members");
        }
        membersToRemove.add(member);
        count++;
      }
      
      //now the currentMembers is full of members to remove, and the grouperLoaderResultset is full
      //of members to add
      //start a transaction
      boolean useTransactions = GrouperLoaderConfig.getPropertyBoolean("loader.use.transactions", false);
      
      final int[] TOTAL_COUNT = new int[]{totalCount};
      final Hib3GrouperLoaderLog HIB3_GROUPER_LOADER_LOG = hib3GrouploaderLog;
      final GrouperTransactionType grouperTransactionType = useTransactions ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING 
          : GrouperTransactionType.NONE;
      GrouperTransaction.callbackGrouperTransaction(grouperTransactionType, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          try {
            int numberOfRows = membersToRemove.size();
            int count = 1;
            //first remove members
            for (Member member : membersToRemove) {
              try {
                //go from subject since large lists might be removed from cache
                boolean alreadyDeleted = group[0].deleteMember(member.getSubject(), false);
                if (LOG.isDebugEnabled() && (count != 0 && count % 200 == 0)) {
                  LOG.debug(groupName + " removing: " + count + " of " + numberOfRows + " members" 
                      + (alreadyDeleted ? ", [note: was already deleted... weird]" : ""));
                }

              } catch (Exception e) {
                GrouperUtil.injectInException(e, "Problem deleting member: " 
                    + member + ", ");
                throw e;
              }
              count++;
              
              if (TOTAL_COUNT[0] != 0 && TOTAL_COUNT[0] % 500 == 0) {
                String logStatus = groupName + " processed " + TOTAL_COUNT[0] + " records, deleting members, " + count + " of " + numberOfRows + " subjects";
                LOG.info(logStatus);
                jobStatus[0] = logStatus;
                HIB3_GROUPER_LOADER_LOG.setJobMessage(jobStatus[0] + ", " + jobMessage);
                HIB3_GROUPER_LOADER_LOG.store();
                //refresh group so it doesnt time out
                group[0] = GroupFinder.findByUuid(grouperSession, group[0].getUuid());
              }
              TOTAL_COUNT[0]++;
            }
            
            numberOfRows = subjectsToAdd.size();
            count = 1;
            //then add new members
            for (Subject subject : subjectsToAdd) {
              try {
                boolean alreadyAdded = group[0].addMember(subject, false);
                if (LOG.isDebugEnabled() && (count != 0 && count % 200 == 0)) {
                  LOG.debug(groupName + " adding: " + count + " of " + numberOfRows + " subjects"
                      + (alreadyAdded ? ", [note: was already added... weird]" : ""));
                }
              } catch (Exception e) {
                GrouperUtil.injectInException(e, "Problem with " 
                    + GrouperUtil.subjectToString(subject) + ", ");
                throw e;
              }
              count++;
              
              if (TOTAL_COUNT[0] != 0 && TOTAL_COUNT[0] % 500 == 0) {
                String logStatus = groupName + " processed " + TOTAL_COUNT[0] + " records, adding members, " + count + " of " + numberOfRows + " subjects";
                LOG.info(logStatus);
                jobStatus[0] = logStatus;
                HIB3_GROUPER_LOADER_LOG.setJobMessage(jobStatus[0] + ", " + jobMessage);
                HIB3_GROUPER_LOADER_LOG.store();
                //refresh group so it doesnt time out
                group[0] = GroupFinder.findByUuid(grouperSession, group[0].getUuid());
              }
              TOTAL_COUNT[0]++;

            }
            if (grouperTransactionType != GrouperTransactionType.NONE) {
              grouperTransaction.commit(GrouperCommitType.COMMIT_IF_NEW_TRANSACTION);
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          
          
          return null;
        }
        
      });
      hib3GrouploaderLog.setInsertCount(subjectsToAdd.size());
      hib3GrouploaderLog.setDeleteCount(membersToRemove.size());
      hib3GrouploaderLog.setStatus(status.name());
      //take out the job status
      hib3GrouploaderLog.setJobMessage(jobMessage.toString());

      if (LOG.isInfoEnabled()) {
        LOG.info(groupName + " done syncing membership, processed " + totalCount + " records.  Total members: " 
            + hib3GrouploaderLog.getTotalCount() + ", inserts: " + hib3GrouploaderLog.getInsertCount()
            + ", deletes: " + hib3GrouploaderLog.getDeleteCount());
      }
    } catch (Exception e) {
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.insertJobMessage(ExceptionUtils.getFullStackTrace(e));
      throw new RuntimeException("Problem with group: " + groupName, e);
    } finally {
      hib3GrouploaderLog.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
      try {
        hib3GrouploaderLog.store();
      } catch (Exception e) {
        //dont worry, just trying to store the log at end
      }
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
          
          groupUuid = group.getUuid();
          //lets get all attribute values
          grouperLoaderType = group.getAttributeOrNull(GrouperLoader.GROUPER_LOADER_TYPE);

          GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
  
          jobName = grouperLoaderTypeEnum.name() + "__" + group.getName() + "__" + group.getUuid();
          
          grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_DB_NAME);
          grouperLoaderAndGroups = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_AND_GROUPS);
          grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUERY);
          grouperLoaderScheduleType = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
          grouperLoaderQuartzCron = grouperLoaderTypeEnum.attributeValueValidateRequired(group, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
          grouperLoaderIntervalSeconds = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
          grouperLoaderPriority = grouperLoaderTypeEnum.attributeValueValidateRequiredInteger(group, GrouperLoader.GROUPER_LOADER_PRIORITY);
          
          scheduleJob(jobName, false, grouperLoaderScheduleType, grouperLoaderQuartzCron,
              grouperLoaderIntervalSeconds, grouperLoaderPriority);
          
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
            Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
            hib3GrouploaderLog.setGroupUuid(groupUuid);
            hib3GrouploaderLog.setHost(GrouperUtil.hostname());
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
   * @param jobName 
   * @param unschedule 
   * @param grouperLoaderScheduleType 
   * @param grouperLoaderQuartzCron 
   * @param grouperLoaderIntervalSeconds 
   * @param grouperLoaderPriority 
   * @throws SchedulerException 
   * 
   */
  static void scheduleJob(String jobName, boolean unschedule, String grouperLoaderScheduleType,
      String grouperLoaderQuartzCron, Integer grouperLoaderIntervalSeconds, 
      Integer grouperLoaderPriority) throws SchedulerException {
    
    //at this point we have all the attributes and we know the required ones are there, and logged when 
    //forbidden ones are there
    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

    JobDetail jobDetail = new JobDetail(jobName, null, GrouperLoaderJob.class);
    
    //schedule this job based on the schedule type and params
    GrouperLoaderScheduleType grouperLoaderScheduleTypeEnum = GrouperLoaderScheduleType
      .valueOfIgnoreCase(grouperLoaderScheduleType, true);
    
    Trigger trigger = grouperLoaderScheduleTypeEnum.createTrigger(grouperLoaderQuartzCron, 
        grouperLoaderIntervalSeconds);
    
    if (LOG.isDebugEnabled() && trigger instanceof SimpleTrigger) {
      LOG.debug("Starting job " + jobName + " at " + ((SimpleTrigger)trigger).getStartTime());
    }
    
    trigger.setName("triggerFor_" + jobName);
    
    //if there is a priority, set it
    if (grouperLoaderPriority != null) {
      trigger.setPriority(grouperLoaderPriority);
    }
    if (unschedule) {
      scheduler.unscheduleJob(trigger.getName(), null);
    }
    //scheduler.unscheduleJob()
    scheduler.scheduleJob(jobDetail, trigger);

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
      GroupType groupType = GroupTypeFinder.find("grouperLoader", false);
      if (groupType == null) {
        LOG.warn("Group type grouperLoader does not exist, so no loader jobs about groups will be scheduled");
        return new HashSet<Group>();
      }
      Set<Group> groupSet = GroupFinder.findAllByType(grouperSession, groupType);
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
