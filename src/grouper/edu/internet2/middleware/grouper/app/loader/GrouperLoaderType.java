/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.19 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset.Row;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperReport;
import edu.internet2.middleware.grouper.misc.GrouperReportException;
import edu.internet2.middleware.grouper.misc.SaveMode;
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
     */
    @SuppressWarnings("unchecked")
    @Override
    public void syncGroupMembership(LoaderJobBean loaderJobBean) {
      
//      String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
//      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
//      List<Group> andGroups, List<GroupType> groupTypes, String groupLikeString, String groupQuery
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(
          loaderJobBean.getGrouperLoaderDb(), loaderJobBean.getQuery());
      
      syncOneGroupMembership(loaderJobBean.getGroupNameOverall(), null, null, 
          loaderJobBean.getHib3GrouploaderLogOverall(), loaderJobBean.getStartTime(),
          grouperLoaderResultset, false, loaderJobBean.getGrouperSession(), 
          loaderJobBean.getAndGroups(), loaderJobBean.getGroupTypes());
      
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
       * @param loaderJobBean
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(LoaderJobBean loaderJobBean) {
        
        Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean.getHib3GrouploaderLogOverall();
        
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
        } else if (StringUtils.equals(GROUPER_REPORT, hib3GrouploaderLog.getJobName())) {


          //how often to run usdu
          String usduSchedule = StringUtils.defaultString(GrouperLoaderConfig.getPropertyString(
              "daily.report.usdu.daysToRun")).toLowerCase();
          String badMemberSchedule = StringUtils.defaultString(GrouperLoaderConfig.getPropertyString(
              "daily.report.badMembership.daysToRun")).toLowerCase();
          
          boolean isRunUsdu = dayListContainsToday(usduSchedule);
          boolean isRunBadMember = dayListContainsToday(badMemberSchedule);
          
          String emailTo = GrouperLoaderConfig.getPropertyString("daily.report.emailTo");
          String reportDirectory = GrouperLoaderConfig.getPropertyString("daily.report.saveInDirectory");
          
          if (StringUtils.isBlank(emailTo) && StringUtils.isBlank(reportDirectory)) {
            throw new RuntimeException("grouper-loader.properties property daily.report.emailTo " +
            		"or daily.report.saveInDirectory needs to be filled in");
          }
          
          String report = null;
          //keep track if ends up being error
          RuntimeException re = null;
          try {
            report = GrouperReport.report(isRunUsdu, isRunBadMember);
          } catch (RuntimeException e) {
            report = e.toString() + "\n\n" + GrouperUtil.getFullStackTrace(e) + "\n\n";
            if (e instanceof GrouperReportException) {
              report += ((GrouperReportException)e).getResult();
            }
            re = e;
          }
          
          //if we are emailing
          if (!StringUtils.isBlank(emailTo)) {
            new GrouperEmail().setBody(report).setSubject("Grouper report").setTo(emailTo).send();
          }
          
          //if we are saving to dir on server
          if (!StringUtils.isBlank(reportDirectory)) {
            reportDirectory = GrouperUtil.stripLastSlashIfExists(reportDirectory) + File.separator;
            GrouperUtil.mkdirs(new File(reportDirectory));
            File reportFile = new File(reportDirectory + "grouperReport_" 
                + new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date()) + ".txt");
            GrouperUtil.saveStringIntoFile(reportFile, report);
          }
          
          //end in error if error
          if (re != null) {
            throw re;
          }
          
          hib3GrouploaderLog.setJobMessage("Ran the grouper report, isRunUnresolvable: " 
              + isRunUsdu + ", isRunBadMembershipFinder: " + isRunBadMember + ", sent to: " + emailTo);
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        } else {
          throw new RuntimeException("Cant find implementation for job: " + hib3GrouploaderLog.getJobName());
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
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#syncGroupMembership(edu.internet2.middleware.grouper.app.loader.LoaderJobBean)
       */
      @SuppressWarnings("unchecked")
      @Override
      public void syncGroupMembership(LoaderJobBean loaderJobBean) {
        
        String groupNameOverall = loaderJobBean.getGroupNameOverall();
        GrouperLoaderDb grouperLoaderDb = loaderJobBean.getGrouperLoaderDb();
        String query = loaderJobBean.getQuery();
        Hib3GrouperLoaderLog hib3GrouploaderLogOverall = loaderJobBean.getHib3GrouploaderLogOverall();
        GrouperSession grouperSession = loaderJobBean.getGrouperSession();
        List<Group> andGroups = loaderJobBean.getAndGroups();
        List<GroupType> groupTypes = loaderJobBean.getGroupTypes();
        String groupLikeString = loaderJobBean.getGroupLikeString();
        String groupQuery = loaderJobBean.getGroupQuery();
        long startTime = loaderJobBean.getStartTime();
        
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
          
          //#######################################
          //Get group metadata
          int groupMetadataNumberOfRows = 0;
          Map<String, String> groupNameToDisplayName = new LinkedHashMap<String, String>();
          Map<String, String> groupNameToDescription = new LinkedHashMap<String, String>();
          Set<String> groupNamesFromGroupQuery = new LinkedHashSet<String>();
          if (!StringUtils.isBlank(groupQuery)) {
            //get a resultset from the db
            final GrouperLoaderResultset grouperLoaderGroupsResultset = new GrouperLoaderResultset(
                grouperLoaderDb, groupQuery);
            
            groupMetadataNumberOfRows = grouperLoaderGroupsResultset.numberOfRows();
            for (int i=0;i<groupMetadataNumberOfRows;i++) {
              
              String groupName = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_NAME_COL, true);
              groupNamesFromGroupQuery.add(groupName);
              String groupDisplayName = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_DISPLAY_NAME_COL, false);
              groupNameToDisplayName.put(groupName, groupDisplayName);
              String groupDescription = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_DESCRIPTION_COL, false);
              groupNameToDescription.put(groupName, groupDescription);
            }
            
          }

          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": found " + groupMetadataNumberOfRows + " number of metadata rows");
          }

          //End group metadata
          //#######################################
          
          //#######################################
          //Delete records in groups not there anymore.  maybe delete group too
          if (!StringUtils.isBlank(groupLikeString)) {
            
            //lets see which names are not in that list
            Set<String> groupNamesManaged = HibernateSession.byHqlStatic()
              .createQuery("select g.nameDb from Group g where g.nameDb like :thePattern")
              .setString("thePattern", groupLikeString).listSet(String.class);
          
            //take out the ones which exist
            groupNamesManaged.removeAll(groupNames);
            
            Boolean isIncludeExclude = null;
            
            for (String groupNameEmpty : groupNamesManaged) {
              
              //if we need to figure this out
              if (isIncludeExclude == null) {
                isIncludeExclude = false;
                //if it ends in a suffix
                if (GroupTypeTupleIncludeExcludeHook.nameIsIncludeExcludeRequireGroup(groupNameEmpty)) {
                  isIncludeExclude = true;
                } else {
                  //else if there is a system of record for it
                  if (GroupFinder.findByName(grouperSession, groupNameEmpty 
                      + GroupTypeTupleIncludeExcludeHook.systemOfRecordExtensionSuffix(), false) != null) {
                    isIncludeExclude = true;
                  }
                }
              }

              //now... if it is includeExclude
              if (isIncludeExclude) {
                //make sure this is the system of record group
                if (!groupNameEmpty.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordExtensionSuffix())) {
                  continue;
                }
              }
              
              long groupStartedMillis = System.currentTimeMillis();
              int memberCount = 0;
              GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
              StringBuilder jobDescription = new StringBuilder();
              boolean didSomething = false;
              long millisGetData = 0;
              long millisSetData = 0;
              try {
                
                //first of all remove members
                Group groupEmpty = GroupFinder.findByName(grouperSession, groupNameEmpty, false);
  
                //not sure why it would be null
                if (groupEmpty == null) {
                  continue;
                }
                millisGetData = System.currentTimeMillis();
                Set<Member> members = GrouperUtil.nonNull(groupEmpty.getImmediateMembers());
                millisGetData = System.currentTimeMillis() - millisGetData;
                
                millisSetData = System.currentTimeMillis();
                memberCount = members.size();
                for (Member member : members) {
                  didSomething = true;
                  groupEmpty.deleteMember(member);
                }
  
                //see if we are deleting group.  It must not be in the group query (if exists), and it 
                //must be configured to do this in the grouper loader properties
                if (!groupNamesFromGroupQuery.contains(groupNameEmpty) && GrouperLoaderConfig.getPropertyBoolean(
                    "loader.sqlTable.likeString.removeGroupIfNotUsed", true)) {

                  //see if we need to log
                  didSomething = true;
                  StringBuilder theLog = new StringBuilder();
                  int groupsDeleted = GroupTypeTupleIncludeExcludeHook.deleteGroupsIfNotUsed(grouperSession, 
                      groupNameEmpty, theLog, true);
                  GrouperUtil.append(jobDescription, "\n", theLog.toString());
                  if (groupsDeleted == 0) {
                    //this is a problem, something is being used...  warning
                    status = GrouperLoaderStatus.WARNING;
                  }
                }
                millisSetData = System.currentTimeMillis() - millisSetData;
              } catch (Exception e) {
                didSomething = true;
                status = GrouperLoaderStatus.ERROR;
                LOG.error("Error on group: " + groupNameEmpty, e);
                GrouperUtil.append(jobDescription, "\n", "Error: " + ExceptionUtils.getFullStackTrace(e));
              }
              
              //if we did something, log it
              if (didSomething) {
                Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
                //make a new log object for this one subgroup
                hib3GrouploaderLog.setHost(GrouperUtil.hostname());
                hib3GrouploaderLog.setJobName("subjobFor_" + groupNameEmpty);
                hib3GrouploaderLog.setStartedTime(new Timestamp(groupStartedMillis));
                hib3GrouploaderLog.setStatus(status.name());
                hib3GrouploaderLog.setParentJobId(hib3GrouploaderLogOverall.getId());
                hib3GrouploaderLog.setParentJobName(hib3GrouploaderLogOverall.getJobName());
                hib3GrouploaderLog.setJobDescription(jobDescription.toString());
                hib3GrouploaderLog.setDeleteCount(memberCount);
                long endTime = System.currentTimeMillis();
                hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
                hib3GrouploaderLog.setMillis((int)(endTime-groupStartedMillis));
                hib3GrouploaderLog.setMillisGetData(new Integer((int)millisGetData));
                hib3GrouploaderLog.setMillisLoadData(new Integer((int)millisSetData));
                
                hib3GrouploaderLog.setJobType(hib3GrouploaderLogOverall.getJobType());
                hib3GrouploaderLog.setJobScheduleType(hib3GrouploaderLogOverall.getJobScheduleType());
                hib3GrouploaderLog.setJobScheduleIntervalSeconds(hib3GrouploaderLogOverall.getJobScheduleIntervalSeconds());
                hib3GrouploaderLog.setJobSchedulePriority(hib3GrouploaderLogOverall.getJobSchedulePriority());
                hib3GrouploaderLog.setJobScheduleQuartzCron(hib3GrouploaderLogOverall.getJobScheduleQuartzCron());
                
                
                hib3GrouploaderLog.store();
                
                hib3GrouploaderLogOverall.addDeleteCount(memberCount);
                hib3GrouploaderLogOverall.store();
                
              }
            }
            
          }
          //End delete records in groups not there anymore.  maybe delete group too
          //#######################################

          int count=1;
          
          long groupStartedMillis = System.currentTimeMillis();
          for (String groupName : groupNames) {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(groupNameOverall + ": syncing membership for " + groupName + " " + count + " out of " + groupNames.size() + " groups");
            }
            
            Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
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

              hib3GrouploaderLog.setJobType(hib3GrouploaderLogOverall.getJobType());
              hib3GrouploaderLog.setJobScheduleType(hib3GrouploaderLogOverall.getJobScheduleType());
              hib3GrouploaderLog.setJobScheduleIntervalSeconds(hib3GrouploaderLogOverall.getJobScheduleIntervalSeconds());
              hib3GrouploaderLog.setJobSchedulePriority(hib3GrouploaderLogOverall.getJobSchedulePriority());
              hib3GrouploaderLog.setJobScheduleQuartzCron(hib3GrouploaderLogOverall.getJobScheduleQuartzCron());

              hib3GrouploaderLog.store();
              
              //based on type, run query from the db and sync members
              syncOneGroupMembership(groupName, groupNameToDisplayName.get(groupName), 
                  groupNameToDescription.get(groupName), hib3GrouploaderLog, groupStartedMillis,
                  grouperLoaderResultset, true, grouperSession, andGroups, groupTypes);
              
              long endTime = System.currentTimeMillis();
              hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
              hib3GrouploaderLog.setMillis((int)(endTime-groupStartedMillis));
              
            } catch (Exception e) {
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
            }
            //start next one now (so we dont lose time)
            groupStartedMillis = System.currentTimeMillis();
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
          
          //lets go through and create groups which arent there
          for (String groupName : groupNamesFromGroupQuery) {
            
            Group group = GroupFinder.findByName(grouperSession, groupName, false);
            if (group == null) {
              
              String groupDisplayName = groupNameToDisplayName.get(groupName);
              groupDisplayName = StringUtils.defaultIfEmpty(groupDisplayName, groupName);
              String groupDescription = groupNameToDescription.get(groupName);
              
              Group newGroup = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT)
                .assignCreateParentStemsIfNotExist(true)
                .assignName(groupName).assignDisplayName(groupDisplayName)
                .assignDescription(groupDescription).saveUnchecked();
              
              for (GroupType groupType : groupTypes) {
                try {
                  newGroup.addType(groupType, false);
                  
                } catch (Exception se) {
                  //TODO remove this catch block in 1.5 when we have unchecked exceptions
                  throw new RuntimeException(se.getMessage(), se);
                }
              }
            }
          }
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": done syncing membership");
          }

        } finally {
          hib3GrouploaderLogOverall.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
          hib3GrouploaderLogOverall.setStatus(statusOverall.name());
        }
      }
    }, 
    
    /** 
     * various change log jobs on the system
     */
    CHANGE_LOG {
          
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
           * @param loaderJobBean
           */
          @SuppressWarnings("unchecked")
          @Override
          public void syncGroupMembership(LoaderJobBean loaderJobBean) {
            
            Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean.getHib3GrouploaderLogOverall();
            
            if (StringUtils.equals(GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG, hib3GrouploaderLog.getJobName())) {
    
              ChangeLogTempToEntity.convertRecords(hib3GrouploaderLog);
    
              hib3GrouploaderLog.setJobMessage("Ran the changeLogTempToChangeLog daemon");
              
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
            } else if (hib3GrouploaderLog.getJobName().startsWith(GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
              
              String consumerName = GrouperUtil.stripStart(hib3GrouploaderLog.getJobName(), GROUPER_CHANGE_LOG_CONSUMER_PREFIX);
              ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName(consumerName, false);
              
              //if this is a new job
              if (changeLogConsumer == null) {
                changeLogConsumer = new ChangeLogConsumer();
                changeLogConsumer.setName(consumerName);
                GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
              }
              
              //if the sequence number is not set
              if (changeLogConsumer.getLastSequenceProcessed() == null) {
                changeLogConsumer.setLastSequenceProcessed(GrouperUtil.defaultIfNull(ChangeLogEntry.maxSequenceNumber(), 0l));
                GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
              }
              
              //ok, we have the sequence, and the job name, lets get the change log records after that sequence, and give them to the 
              //consumer
              String theClassName = GrouperLoaderConfig.getPropertyString("changeLog.consumer." + consumerName + ".class");
              Class<?> theClass = GrouperUtil.forName(theClassName);
              ChangeLogConsumerBase changeLogConsumerBase = (ChangeLogConsumerBase)GrouperUtil.newInstance(theClass);
              
              ChangeLogProcessorMetadata changeLogProcessorMetadata = new ChangeLogProcessorMetadata();
              changeLogProcessorMetadata.setHib3GrouperLoaderLog(hib3GrouploaderLog);
              
              //lets only do 100k records at a time
              for (int i=0;i<1000;i++) {
                
                //lets get 100 records
                List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
                  .retrieveBatch(changeLogConsumer.getLastSequenceProcessed(), 100);
                
                if (changeLogEntryList.size() == 0) {
                  break;
                }
                
                //pass this to the consumer
                long lastProcessed = changeLogConsumerBase.processChangeLogEntries(changeLogEntryList, changeLogProcessorMetadata);
                
                changeLogConsumer.setLastSequenceProcessed(lastProcessed);
                GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
                
                long lastSequenceInBatch = changeLogEntryList.get(changeLogEntryList.size()-1).getSequenceNumber();
                if (lastProcessed != lastSequenceInBatch) {
                  hib3GrouploaderLog.appendJobMessage("Did not get all the way through the batch! " + lastProcessed
                      + " != " + lastSequenceInBatch);
                  hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
                  //didnt get al the way through
                  break;
                }
                
                if (changeLogEntryList.size() < 100) {
                  break;
                }
              }
              
            } else {
              throw new RuntimeException("Cant find implementation for job: " + hib3GrouploaderLog.getJobName());
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
   * maintenance grouper report name
   */
  public static final String GROUPER_REPORT = GrouperLoaderType.MAINTENANCE.name() + "_grouperReport";

  /**
   * change log temp to change log
   */
  public static final String GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG = GrouperLoaderType.CHANGE_LOG.name() + "_changeLogTempToChangeLog";

  
  /**
   * change log consumer prefix
   */
  public static final String GROUPER_CHANGE_LOG_CONSUMER_PREFIX = GrouperLoaderType.CHANGE_LOG.name() + "_consumer_";

  
  
  /**
   * see if an attribute if required or not
   * @param attributeName
   * @return true if required, false if not
   */
  public abstract boolean attributeRequired(String attributeName);

  /**
   * sync up a group membership based on query and db
   * @param loaderJobBean is the bean data
   */
  public abstract void syncGroupMembership(LoaderJobBean loaderJobBean);
  
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
    
    String attributeValue = group.getAttributeValue(attributeName, false, false);
    
    //if value, go with that
    if (!StringUtils.isBlank(attributeValue)) {
      return attributeValue;
    }
    
    if (StringUtils.equals(GrouperLoader.GROUPER_LOADER_TYPE, attributeName)) {
      String query = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_QUERY, false, false);
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
      String cron = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, false, false);
      boolean hasCron = StringUtils.isNotBlank(cron); 
      String intervalSeconds = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS, false, false);
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
    
    String attributeValue = group.getAttributeValue(attributeName, false, false);
    
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
   * @param groupDisplayNameForInsert can be null to default to group name or extension.  This is display names
   * if a group needs to be created.  But the display extension will be changed if different
   * @param groupDescription can be null to default to generated description, or the description of the group
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
      final String groupDisplayNameForInsert, final String groupDescription,
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

      String groupExtension = StringUtils.isBlank(groupDisplayNameForInsert) ? GrouperUtil.extensionFromName(groupName) : 
        GrouperUtil.extensionFromName(groupDisplayNameForInsert);
      
      Group theGroup = null;
      if (groupList) {
        GroupSave groupSave = new GroupSave(grouperSession);
        groupSave.assignGroupNameToEdit(groupName).assignName(groupName);
        groupSave.assignDisplayExtension(groupExtension);
        groupSave.assignDisplayName(groupDisplayNameForInsert);
        String theGroupDescription = StringUtils.isBlank(groupDescription) ? 
            groupExtension + " auto-created by grouperLoader" : groupDescription;
        groupSave.assignDescription(theGroupDescription);
        groupSave.assignCreateParentStemsIfNotExist(true);
        theGroup = groupSave.save();
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupName + ": saving group if necessary, result type: " + groupSave.getSaveResultType());
        }
      } else {
        theGroup = GroupFinder.findByName(grouperSession, groupName, true);
      }

      final Group[] group = new Group[]{theGroup};
      
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
                group[0] = GroupFinder.findByUuid(grouperSession, group[0].getUuid(), true);
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
                group[0] = GroupFinder.findByUuid(grouperSession, group[0].getUuid(), true);
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
          SubjectFinder.findById("GrouperSystem", true)
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
          grouperLoaderType = group.getAttributeValue(GrouperLoader.GROUPER_LOADER_TYPE, false, false);

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
