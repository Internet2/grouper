/*
 * @author mchyzer
 * $Id: GrouperLoaderType.java,v 1.24 2009-11-02 03:50:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset.Row;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.client.GroupSyncDaemon;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperReport;
import edu.internet2.middleware.grouper.misc.GrouperReportException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
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
    public void runJob(LoaderJobBean loaderJobBean) {
      
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

//      String groupName, GrouperLoaderDb grouperLoaderDb, String query, 
//      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
//      List<Group> andGroups, List<GroupType> groupTypes, String groupLikeString, String groupQuery
      
      //get a resultset from the db
      final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(
          loaderJobBean.getGrouperLoaderDb(), loaderJobBean.getQuery());
      
      syncOneGroupMembership(loaderJobBean.getGroupNameOverall(), null, null, 
          loaderJobBean.getHib3GrouploaderLogOverall(), loaderJobBean.getStartTime(),
          grouperLoaderResultset, false, loaderJobBean.getGrouperSession(), 
          loaderJobBean.getAndGroups(), loaderJobBean.getGroupTypes(), null, null);
      
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
      public void runJob(LoaderJobBean loaderJobBean) {
        
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

        Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean.getHib3GrouploaderLogOverall();
        
        if (StringUtils.equals(MAINTENANCE_CLEAN_LOGS, hib3GrouploaderLog.getJobName())) {
          StringBuilder jobMessage = new StringBuilder();
          {
            int daysToKeepLogs = GrouperLoaderConfig.getPropertyInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
            if (daysToKeepLogs != -1) {
              //lets get a date
              Calendar calendar = GregorianCalendar.getInstance();
              //get however many days in the past
              calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
              //run a query to delete (note, dont retrieve records to java, just delete)
              int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where last_updated < ?", 
                  (List<Object>)(Object)GrouperUtil.toList(new Timestamp(calendar.getTimeInMillis())));
              jobMessage.append("Deleted " + records + " records from grouper_loader_log older than " + daysToKeepLogs + " days old.  ");
              
            } else {
              jobMessage.append("Configured to not delete records from grouper_loader_log table.  ");
            }
          }
          {
            int daysToKeepLogs = GrouperLoaderConfig.getPropertyInt("loader.retain.db.change_log_entry.days", 14);
            if (daysToKeepLogs != -1) {
              //lets get a date
              Calendar calendar = GregorianCalendar.getInstance();
              //get however many days in the past
              calendar.add(Calendar.DAY_OF_YEAR, -1 * daysToKeepLogs);
              //note, this is *1000 so that we can differentiate conflicting records
              long time = calendar.getTimeInMillis()*1000L;
              //run a query to delete (note, dont retrieve records to java, just delete)
              int records = HibernateSession.bySqlStatic().executeSql("delete from grouper_change_log_entry where created_on < ?", 
                  (List<Object>)(Object)GrouperUtil.toList(new Long(time)));
              jobMessage.append("Deleted " + records + " records from grouper_change_log_entry older than " + daysToKeepLogs + " days old. (" + time + ")  ");
            } else {
              jobMessage.append("Configured to not delete records from grouper_change_log_entry table.  ");
            }
            
          }
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
          hib3GrouploaderLog.setJobMessage(jobMessage.toString());
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
            report = new GrouperReport().findBadMemberships(isRunBadMember).findUnresolvables(isRunUsdu)
              .runReport();
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
        } else if (StringUtils.equals(GROUPER_ENABLED_DISABLED, hib3GrouploaderLog.getJobName())) {

          int records = Membership.internal_fixEnabledDisabled();
          records += AttributeAssign.internal_fixEnabledDisabled();
          records += ExternalSubject.internal_fixDisabled();

          hib3GrouploaderLog.setUpdateCount(records);

          hib3GrouploaderLog.setJobMessage("Ran enabled/disabled daemon, changed " + records + " records");
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        } else if (StringUtils.equals(GROUPER_EXTERNAL_SUBJ_CALC_FIELDS, hib3GrouploaderLog.getJobName())) {

          int records = ExternalSubject.internal_daemonCalcFields();

          hib3GrouploaderLog.setUpdateCount(records);

          hib3GrouploaderLog.setJobMessage("Ran ext subj calc fields daemon, changed " + records + " records");
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        } else if (StringUtils.equals(GROUPER_RULES, hib3GrouploaderLog.getJobName())) {

          int records = RuleEngine.daemon();
          hib3GrouploaderLog.setUpdateCount(records);

          hib3GrouploaderLog.setJobMessage("Ran rules daemon, changed " + records + " records");
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        } else if (hib3GrouploaderLog.getJobName().startsWith(GrouperLoaderType.GROUPER_GROUP_SYNC)) {

          //strip off the beginning
          String localGroupName = hib3GrouploaderLog.getJobName().substring(GrouperLoaderType.GROUPER_GROUP_SYNC.length()+2);
          int records = GroupSyncDaemon.syncGroup(localGroupName);
          hib3GrouploaderLog.setUpdateCount(records);

          hib3GrouploaderLog.setJobMessage("Ran group sync daemon, changed " + records + " records");
          
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
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#syncOneGroupMembership(String, String, String, Hib3GrouperLoaderLog, long, GrouperLoaderResultset, boolean, GrouperSession, List, List, Map, Set)
       */
      @SuppressWarnings("unchecked")
      @Override
      public void runJob(LoaderJobBean loaderJobBean) {
        
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

        String groupNameOverall = loaderJobBean.getGroupNameOverall();
        GrouperLoaderDb grouperLoaderDb = loaderJobBean.getGrouperLoaderDb();
        String query = loaderJobBean.getQuery();
        Hib3GrouperLoaderLog hib3GrouploaderLogOverall = loaderJobBean.getHib3GrouploaderLogOverall();
        GrouperSession grouperSession = loaderJobBean.getGrouperSession();
        List<Group> andGroups = loaderJobBean.getAndGroups();
        List<GroupType> groupTypes = GrouperUtil.nonNull(loaderJobBean.getGroupTypes());
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
          Map<String, Subject> subjectCache = new HashMap<String, Subject>();
          Map<String, Map<Privilege, List<Subject>>> privsToAdd = new LinkedHashMap<String, Map<Privilege, List<Subject>>>();
          Set<String> groupNamesFromGroupQuery = new LinkedHashSet<String>();
          if (!StringUtils.isBlank(groupQuery)) {
            //get a resultset from the db
            final GrouperLoaderResultset grouperLoaderGroupsResultset = new GrouperLoaderResultset(
                grouperLoaderDb, groupQuery + " order by group_name");
            
            groupMetadataNumberOfRows = grouperLoaderGroupsResultset.numberOfRows();
            for (int i=0;i<groupMetadataNumberOfRows;i++) {
              
              String groupName = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_NAME_COL, true);
              groupNamesFromGroupQuery.add(groupName);
              String groupDisplayName = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_DISPLAY_NAME_COL, false);
              groupNameToDisplayName.put(groupName, groupDisplayName);
              String groupDescription = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_DESCRIPTION_COL, false);
              groupNameToDescription.put(groupName, groupDescription);
              Map<Privilege, List<Subject>> privsToAddForGroup = new HashMap<Privilege, List<Subject>>();
              privsToAdd.put(groupName, privsToAddForGroup);
              {
                String groupViewers = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_VIEWERS_COL, false);
                if (!StringUtils.isBlank(groupViewers)) {
                  List<Subject> viewerSubjects = lookupSubject(subjectCache, groupViewers);
                  privsToAddForGroup.put(AccessPrivilege.VIEW, viewerSubjects);
                }
            }
              {
                String groupReaders = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_READERS_COL, false);
                if (!StringUtils.isBlank(groupReaders)) {
                  List<Subject> readerSubjects = lookupSubject(subjectCache, groupReaders);
                  privsToAddForGroup.put(AccessPrivilege.READ, readerSubjects);
                }
              }
              {
                String groupUpdaters = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_UPDATERS_COL, false);
                if (!StringUtils.isBlank(groupUpdaters)) {
                  List<Subject> updaterSubjects = lookupSubject(subjectCache, groupUpdaters);
                  privsToAddForGroup.put(AccessPrivilege.UPDATE, updaterSubjects);
                }
              }
              {
                String groupAdmins = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_ADMINS_COL, false);
                if (!StringUtils.isBlank(groupAdmins)) {
                  List<Subject> adminSubjects = lookupSubject(subjectCache, groupAdmins);
                  privsToAddForGroup.put(AccessPrivilege.ADMIN, adminSubjects);
                }
              }
              {
                String groupOptins = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_OPTINS_COL, false);
                if (!StringUtils.isBlank(groupOptins)) {
                  List<Subject> optinSubjects = lookupSubject(subjectCache, groupOptins);
                  privsToAddForGroup.put(AccessPrivilege.OPTIN, optinSubjects);
                }
              }
              {
                String groupOptouts = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_OPTOUTS_COL, false);
                if (!StringUtils.isBlank(groupOptouts)) {
                  List<Subject> optoutSubjects = lookupSubject(subjectCache, groupOptouts);
                  privsToAddForGroup.put(AccessPrivilege.OPTOUT, optoutSubjects);
                }
              }
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
          
          //if we are configured to, get the group names in one fell swoop
          // && GrouperLoaderConfig.getPropertyBoolean(
          // "loader.getAllGroupListMembershipsAtOnce", false);
          //2010/05/02 I think this didnt pan out as a performance gain...
          boolean getMembershipsAtOnce = false;
          
          
          //set of immediate memberships in the regsitry, key is group name, multikey by subjectId, and optionally sourceId
          Map<String, Set<MultiKey>> membershipsInRegistry = new HashMap<String, Set<MultiKey>>();
          
          if (getMembershipsAtOnce) {
            
            String queryPrefix = "select distinct a.value, gm.subjectIdDb, gm.subjectSourceIdDb "
              + "from Attribute a, Field f, Membership gms, Member gm, Field mf "
              + "where f.name = 'name' and a.fieldId = f.uuid "
              + "and gms.ownerUuid = a.groupUuid and gms.memberUuid = gm.uuid "
              + "and gms.fieldId = mf.uuid and mf.name = 'members' and mf.typeString = 'list' "
              + "and gms.type = 'immediate' and a.value  ";
            
            List<Object[]> result = null;
            //lets see which type to do
            if (!StringUtils.isBlank(groupLikeString)) {
              
              result = HibernateSession.byHqlStatic()
                .createQuery(queryPrefix + " like :thePattern")
                .setString("thePattern", groupLikeString).list(Object[].class);
                            
            } else {
              //just batch up the group names to get the results, in size of 100
              int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupNames, 100);
              List<String> groupNamesList = GrouperUtil.listFromCollection(groupNames);
              result = new ArrayList<Object[]>();
              for (int i=0;i<numberOfBatches;i++) {
                
                List<String> groupNamesInBatch = GrouperUtil.batchList(groupNamesList, 100, i);
                
                ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
                String queryInClause = HibUtils.convertToInClause(groupNamesInBatch, byHqlStatic);
                byHqlStatic.createQuery(queryPrefix + " in (" + queryInClause + ")");
                result.addAll(byHqlStatic.list(Object[].class));
                
              }
              
              
            }
            
            for (Object[] resultLine : GrouperUtil.nonNull(result)) {
              String groupName = (String)resultLine[0];
              String subjectId = (String)resultLine[1];
              String sourceId = (String)resultLine[2];
              
              Set<MultiKey> members = membershipsInRegistry.get(groupName);
              
              if (members == null) {
                members = new HashSet<MultiKey>();
                membershipsInRegistry.put(groupName, members);
              }
               
              //here is the exact
              members.add(new MultiKey(subjectId, sourceId));
              
              //put in the subjectId, lets hope there arent duplicates going on
              //members.add(new MultiKey(new Object[]{subjectId}));
              
            }

            
          }
          
          Set<String> groupNamesToSync = new LinkedHashSet<String>();
          if (!StringUtils.isBlank(groupQuery)) {
            groupNamesToSync.addAll(groupNamesFromGroupQuery);
          } else {
            groupNamesToSync.addAll(groupNames);
          }
          
          for (String groupName : groupNamesToSync) {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(groupNameOverall + ": syncing membership for " + groupName + " " + count + " out of " + groupNamesToSync.size() + " groups");
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
                  grouperLoaderResultset, true, grouperSession, andGroups, groupTypes, privsToAdd.get(groupName), membershipsInRegistry.get(groupName));
              
              long endTime = System.currentTimeMillis();
              hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
              hib3GrouploaderLog.setMillis((int)(endTime-groupStartedMillis));
              
            } catch (Exception e) {
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
              LOG.error("Error in job for group: " + groupName, e);
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
          
          // The above code should be taking care of these group creates..
          /*
          for (String groupName : groupNamesFromGroupQuery) {
            
            Group group = GroupFinder.findByName(grouperSession, groupName, false);
            if (group == null) {
              
              String groupDisplayName = groupNameToDisplayName.get(groupName);
              groupDisplayName = StringUtils.defaultIfEmpty(groupDisplayName, groupName);
              String groupDescription = groupNameToDescription.get(groupName);
              
              Group newGroup = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT)
                .assignCreateParentStemsIfNotExist(true)
                .assignName(groupName).assignDisplayName(groupDisplayName)
                .assignDescription(groupDescription).save();
              
              for (GroupType groupType : groupTypes) {
                try {
                  newGroup.addType(groupType, false);
                  
                } catch (Exception se) {
                  //TODO remove this catch block in 1.5 when we have unchecked exceptions
                  throw new RuntimeException(se.getMessage(), se);
                }
              }
            }
          }*/
          
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
          public void runJob(LoaderJobBean loaderJobBean) {
            
            GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

            Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean.getHib3GrouploaderLogOverall();
            
            if (StringUtils.equals(GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG, hib3GrouploaderLog.getJobName())) {
    
              ChangeLogTempToEntity.convertRecords(hib3GrouploaderLog);
    
              hib3GrouploaderLog.setJobMessage("Ran the changeLogTempToChangeLog daemon");
              
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
            } else if (hib3GrouploaderLog.getJobName().startsWith(GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
              
              String consumerName = hib3GrouploaderLog.getJobName().substring(GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length());
              
              //ok, we have the sequence, and the job name, lets get the change log records after that sequence, and give them to the 
              //consumer
              String theClassName = GrouperLoaderConfig.getPropertyString("changeLog.consumer." + consumerName + ".class");
              Class<?> theClass = GrouperUtil.forName(theClassName);
              ChangeLogConsumerBase changeLogConsumerBase = (ChangeLogConsumerBase)GrouperUtil.newInstance(theClass);

              ChangeLogHelper.processRecords(consumerName, hib3GrouploaderLog, changeLogConsumerBase);
            } else {
              throw new RuntimeException("Cant find implementation for job: " + hib3GrouploaderLog.getJobName());
            }
          }
        }, 
        
    /** 
     * simple sql query where all results are all members of group.
     * must have a subject_id col, and optionally a subject_source_id col
     */
    ATTR_SQL_SIMPLE{
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        //cant think of a required one
        return false;
      }
    
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
       */
      @Override
      public boolean attributeOptional(String attributeName) {
        return StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_ATTR_QUERY, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_ATTR_SET_QUERY, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_ATTRS_LIKE, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_DB_NAME, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_INTERVAL_SECONDS, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_PRIORITY, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_QUARTZ_CRON, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_SCHEDULE_TYPE, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_TYPE, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_ACTION_QUERY, attributeName)
            || StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_ACTION_SET_QUERY, attributeName);
      }
      
      /**
       * sync up an attributeDefinition membership based on query and db
       */
      @SuppressWarnings("unchecked")
      @Override
      public void runJob(LoaderJobBean loaderJobBean) {

        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

        //      GrouperLoaderDb grouperLoaderDb, attributeDefName
        //      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
        //      attributeLoaderAttrQuery, attributeLoaderAttrSetQuery, attributeLoaderAttrsLike
        //   attributeLoaderActionQuery, attributeLoaderActionSetQuery
        
        syncOneAttributeDef(loaderJobBean.getAttributeDefName(), loaderJobBean.getHib3GrouploaderLogOverall(), 
            loaderJobBean.getGrouperLoaderDb(), 
            loaderJobBean.getStartTime(), loaderJobBean.getGrouperSession(), 
            loaderJobBean.getAttributeLoaderAttrsLike(), loaderJobBean.getAttributeLoaderAttrQuery(), 
            loaderJobBean.getAttributeLoaderAttrSetQuery(), loaderJobBean.getAttributeLoaderActionQuery(), 
            loaderJobBean.getAttributeLoaderActionSetQuery());
        
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
    throw new RuntimeException("Cant find job type for this name: " + jobName);
  }
  
  /**
   * maintenance clean logs name
   */
  public static final String MAINTENANCE_CLEAN_LOGS = "MAINTENANCE_cleanLogs";

  /**
   * maintenance grouper report name
   */
  public static final String GROUPER_REPORT = "MAINTENANCE__grouperReport";

  /**
   * maintenance enabledDisabled name
   */
  public static final String GROUPER_ENABLED_DISABLED = "MAINTENANCE__enabledDisabled";

  /**
   * maintenance, calculate enabled/disabled fields
   */
  public static final String GROUPER_EXTERNAL_SUBJ_CALC_FIELDS = "MAINTENANCE_externalSubjCalcFields";
  
  /**
   * maintenance rules name
   */
  public static final String GROUPER_RULES = "MAINTENANCE__rules";

  /**
   * group sync job name
   */
  public static final String GROUPER_GROUP_SYNC = "MAINTENANCE__groupSync";

  /**
   * change log temp to change log
   */
  public static final String GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG = "CHANGE_LOG_changeLogTempToChangeLog";

  /**
   * change log consumer prefix
   */
  public static final String GROUPER_CHANGE_LOG_CONSUMER_PREFIX = "CHANGE_LOG_consumer_";

  /**
   * esb http listener name
   */
  public static final String GROUPER_ESB_HTTP_LISTENER = "CHANGE_LOG_esb_http_listener";
  
  /**
   * esb xmpp listener name
   */
  public static final String GROUPER_ESB_XMMP_LISTENER = "CHANGE_LOG_esb_xmpp_listener";
  /**
  
  /**
   * see if an attribute if required or not
   * @param attributeName
   * @return true if required, false if not
   */
  public abstract boolean attributeRequired(String attributeName);

  /**
   * <pre>
   * sync up a group membership based on query and db.  Note, the first thing you should
   * do is set the context type:
   * 
   *             GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
   * </pre>       
   * @param loaderJobBean is the bean data
   */
  public abstract void runJob(LoaderJobBean loaderJobBean);
  
  /**
   * see if an attribute if optional or not (if not, then it is either required or forbidden)
   * @param attributeName
   * @return true if optional, false if not
   */
  public abstract boolean attributeOptional(String attributeName);
  
  /**
   * take in a subject list, comma separated
   * @param subjectCache
   * @param subjectIdOrIdentifierList
   * @return the list of subjects (never null)
   */
  public static List<Subject> lookupSubject(Map<String, Subject> subjectCache, String subjectIdOrIdentifierList) {
    List<Subject> subjectList = new ArrayList<Subject>();
    if (!StringUtils.isBlank(subjectIdOrIdentifierList)) {
      String[] subjectIdsArray = GrouperUtil.splitTrim(subjectIdOrIdentifierList, ",");
      for (String subjectIdOrIdentifier : subjectIdsArray) {
        Subject subject = subjectCache.get(subjectIdOrIdentifier);
        if (subject == null) {
          
          //we need to find this or make it
          try {
            subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
          } catch (Exception e) {
            //ignore I guess
            LOG.error("error looking for subject: " + subjectIdOrIdentifier, e);
          }
          if (subject == null && StringUtils.contains(subjectIdOrIdentifier, ':')) {
            //if there is a colon, that is a group, it doesnt exist, so create it
            //note, not sure why insert_or_update and not just insert, but we were getting errors that the group existed, not sure why
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            Group group = new GroupSave(grouperSession).assignName(subjectIdOrIdentifier)
              .assignCreateParentStemsIfNotExist(true)  
              .assignGroupNameToEdit(subjectIdOrIdentifier)
              .assignSaveMode(SaveMode.INSERT_OR_UPDATE).save();
            subject = group.toSubject();
          }
        }
        if (subject != null) {
          subjectCache.put(subjectIdOrIdentifier, subject);
          if (!SubjectHelper.inList(subjectList, subject)) {
            subjectList.add(subject);
          }
        }
      }
    }
    return subjectList;
    
  }
  
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
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param attributeDef is the attributeDef to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  Integer attributeValueValidateRequiredAttrDefInteger(AttributeDef attributeDef, String attributeName) {
    String attributeValueString = StringUtils.trim(attributeValueValidateRequiredAttrDef(attributeDef, attributeName));
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
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param attributeDef is the group to get the attribute from
   * @param attributeName
   * @return the attribute value
   */
  String attributeValueValidateRequiredAttrDef(AttributeDef attributeDef, String attributeName) {
    
    String attributeValue = attributeDef.getAttributeValueDelegate().retrieveValueString(GrouperCheckConfig.attributeLoaderStemName() + ":" + attributeName);
    
    boolean hasValue = StringUtils.isNotBlank(attributeValue);
    boolean isRequired = this.attributeRequired(attributeName);
    boolean isOptional = this.attributeOptional(attributeName);
    
    //must have value if required
    if (!hasValue && isRequired) {
      throw new RuntimeException("Attribute '" + attributeName + "' is required, but is not set for loader type: " 
          + this.name() + ", attributeDefName: " + attributeDef.getName());
    }
    
    // must not have value if not required or optional
    if (hasValue && !isRequired && !isOptional) {
      LOG.error("Attribute '" + attributeName + "' is not required or optional, " +
          "but is set to '" + attributeValue + "' for loader type: " 
          + this.name() + ", attributeDefName: " + attributeDef.getName());
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
   * @param groupPrivsToAdd priv
   * @param groupMembers if a grouplist, this is a pre-fetched list of group members, else this is null, 
   * meaning get all members here
   */
  @SuppressWarnings("unchecked")
  protected static void syncOneGroupMembership(final String groupName,
      final String groupDisplayNameForInsert, final String groupDescription,
      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime,
      final GrouperLoaderResultset grouperLoaderResultset, boolean groupList,
      final GrouperSession grouperSession, List<Group> andGroups, List<GroupType> groupTypes,
      Map<Privilege,List<Subject>> groupPrivsToAdd, Set<MultiKey> groupMembers) {
    
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
      
      if (groupList) {
        if (groupPrivsToAdd != null && groupPrivsToAdd.size() > 0) {
          
          Set<Group> groupsForPrivs = GroupTypeTupleIncludeExcludeHook.relatedGroups(theGroup);
          
          boolean isIncludeExclude = false;
          if (groupsForPrivs.size() > 1) {
          for (Group groupForPriv : groupsForPrivs) {
              if (groupForPriv.getName().endsWith(GroupTypeTupleIncludeExcludeHook.includeExtensionSuffix())) {
                isIncludeExclude = true;
              }
            }
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug("Related groups to " + theGroup.getName() 
                + ": " + GrouperUtil.toStringForLog(groupsForPrivs)
                + ", isIncludeExclude: " + isIncludeExclude + ", groupSize: " + groupsForPrivs.size());
          }
          
          for (Group groupForPriv : groupsForPrivs) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Cycling through privs, group: " + groupForPriv);
            }
            
            for (Privilege privilege : groupPrivsToAdd.keySet()) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Cycling through privs, priv: " + privilege.getName() + ", group: " + groupForPriv);
              }
              List<Subject> subjects = groupPrivsToAdd.get(privilege);
              for (Subject subject : subjects) {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Cycling through privs, subject: " + GrouperUtil.subjectToString(subject) 
                      + ", priv: " + privilege.getName() + ", group: " + groupForPriv);
                }
                //add the priv
                Boolean added = null;
                boolean skipPriv = false;
                try {
                  if (isIncludeExclude) {
                    if (AccessPrivilege.UPDATE.getName().equals(privilege.getName())) {
                      if (!groupForPriv.getName().endsWith(GroupTypeTupleIncludeExcludeHook.excludeExtensionSuffix())
                          && !groupForPriv.getName().endsWith(GroupTypeTupleIncludeExcludeHook.includeExtensionSuffix())) {
                        if (LOG.isDebugEnabled()) {
                          LOG.debug("Skipping priv: " + privilege + ", on group: " + groupForPriv.getName() 
                              + " since update and includeExclude group which is not the includes or excludes...");
                        }
                        skipPriv = true;
                      }
                    }
                  }
                  if (!skipPriv) {    
                  added = groupForPriv.grantPriv(subject, privilege, false);
                  }
                  if (added != null && added) {
                    hib3GrouploaderLog.addInsertCount(1);
                  }
                } finally {
                  if (!skipPriv && LOG.isDebugEnabled()) {
                  String logMessage = "Granting privilege " + privilege + " to group: " + groupForPriv.getName() + " to subject: "
                        + GrouperUtil.subjectToString(subject) + " already existed? " + (added == null ? null : !added);
                  //System.out.println(logMessage);
                  LOG.debug(logMessage);
                }
              }
            }
          }
        }
      }
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Done assigning privilege to related groups: " + theGroup.getName());
      }
      hib3GrouploaderLog.setGroupUuid(group[0].getUuid());

      Set<LoaderMemberWrapper> currentMembers = new LinkedHashSet<LoaderMemberWrapper>();
      
      if (groupMembers != null) {
        for (MultiKey multiKey : groupMembers) {
          currentMembers.add(new LoaderMemberWrapper((String)multiKey.getKey(0), (String)multiKey.getKey(1)));
        }
      } else {
        Set<Member> members = group[0].getImmediateMembers();
        for (Member member : GrouperUtil.nonNull(members)) {
          currentMembers.add(new LoaderMemberWrapper(member));
        }
      }
      
      //now lets remove data from each since the member is there and is supposed to be there
      Iterator<LoaderMemberWrapper> iterator = currentMembers.iterator();
      
      int count = 0;
      
      while (iterator.hasNext()) {
        
        LoaderMemberWrapper member = iterator.next();
        //see if it is in the current list
        Row row = grouperLoaderResultset.find(member.getSubjectId(), member.getSourceId());
        
        //this means the member exists in query, and in membership, so maybe do nothing
        if (row != null) {
          boolean andGroupsDoesntHaveSubject = false;
          if (andGroups.size() > 0) {
            Subject subject = row.getSubject(groupName);
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
        Subject subject = row.getSubject(groupName);
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
      final List<LoaderMemberWrapper> membersToRemove = new ArrayList<LoaderMemberWrapper>(currentMembers);
      numberOfRows = currentMembers.size();
      count = 1;
      //first remove members
      for (LoaderMemberWrapper loaderMemberWrapper : membersToRemove) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupName + " will remove subject from group: " + loaderMemberWrapper.getSourceId() + "/" + loaderMemberWrapper.getSubjectId() + ", " + count + " of " + numberOfRows + " members");
        }
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
            for (LoaderMemberWrapper member : membersToRemove) {
              try {
                //go from subject since large lists might be removed from cache
                boolean alreadyDeleted = group[0].deleteMember(member.findOrGetSubject(), false);
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
      LOG.error("Problem with group: " + groupName, e);
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
   * get an attribute value, or null, or a default if exists
   * @param attributeDef
   * @param attributeName
   * @return the attribute value
   */
  public static String attributeValueOrDefaultOrNullAttrDef(AttributeDef attributeDef, String attributeName) {
    
    String attributeValue = attributeDef.getAttributeValueDelegate().retrieveValueString(
        GrouperCheckConfig.attributeLoaderStemName() + ":" + attributeName);
    
    //if value, go with that
    if (!StringUtils.isBlank(attributeValue)) {
      return attributeValue;
    }
    
    if (StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_TYPE, attributeName)) {
      //this is all we have so far
      return GrouperLoaderType.ATTR_SQL_SIMPLE.name();
    }
    
    if (StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_DB_NAME, attributeName)) {
      //assume default database
      return "grouper";
    }
  
    if (StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_SCHEDULE_TYPE, attributeName)) {
      String cron = attributeDef.getAttributeValueDelegate().retrieveValueString(
          GrouperCheckConfig.attributeLoaderStemName() + ":" + GrouperLoader.ATTRIBUTE_LOADER_QUARTZ_CRON);
      boolean hasCron = StringUtils.isNotBlank(cron); 
      String intervalSeconds = attributeDef.getAttributeValueDelegate().retrieveValueString(
          GrouperCheckConfig.attributeLoaderStemName() + ":" + GrouperLoader.ATTRIBUTE_LOADER_INTERVAL_SECONDS);
      boolean hasIntervalSeconds = StringUtils.isNotBlank(intervalSeconds);
      
      if (!hasCron && !hasIntervalSeconds) {
        return GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
      }

      if (hasCron && !hasIntervalSeconds) {
        return GrouperLoaderScheduleType.CRON.name();
      }

      if (!hasCron && hasIntervalSeconds) {
        return GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
      }
    }
    
    if (StringUtils.equals(GrouperLoader.ATTRIBUTE_LOADER_INTERVAL_SECONDS, attributeName)) {
      String scheduleTypeString = attributeValueOrDefaultOrNullAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_SCHEDULE_TYPE);
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.valueOfIgnoreCase(scheduleTypeString, false);
      if (grouperLoaderScheduleType != null && grouperLoaderScheduleType.equals(GrouperLoaderScheduleType.START_TO_START_INTERVAL)) {
  
        //default to 1 day
        return Integer.toString(60 * 60 * 24);
      }
    }
  
    return attributeValue;
  }

  /**
   * @param attributeDefName
   * @param hib3GrouploaderLog
   * @param grouperLoaderDb 
   * @param startTime
   * @param grouperSession 
   * @param attributeLoaderAttrsLike 
   * @param attributeLoaderAttrQuery 
   * @param attributeLoaderAttrSetQuery 
   * @param attributeLoaderActionQuery 
   * @param attributeLoaderActionSetQuery 
   */
  @SuppressWarnings("unchecked")
  protected static void syncOneAttributeDef(final String attributeDefName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, GrouperLoaderDb grouperLoaderDb, 
      long startTime,
      final GrouperSession grouperSession, String attributeLoaderAttrsLike, 
      String attributeLoaderAttrQuery, String attributeLoaderAttrSetQuery,
      String attributeLoaderActionQuery, String attributeLoaderActionSetQuery  ) {
    
    //keep this separate so we can prepend stuff inside...
    final StringBuilder jobMessage = new StringBuilder(StringUtils.defaultString(hib3GrouploaderLog.getJobMessage()));
    
    final String[] jobStatus = new String[1];
    
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    if (LOG.isDebugEnabled()) {
      LOG.debug(attributeDefName + " start syncing attributeDef");
    }
    
    hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis()-startTime));

    long startTimeLoadData = System.currentTimeMillis();
    
    int[] totalCount = new int[]{0};
    int[] processedCount = new int[]{0};
    
    //assume success
    GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
    
    try {
      
      AttributeDef theAttributeDef = AttributeDefFinder.findByName(attributeDefName, true);

      //####################################
      //## ALL ATTRIBUTE DEF NAMES AND LIKE
      //
      //lets get all of them
      Set<AttributeDefName> attributeDefNames = GrouperDAOFactory.getFactory()
        .getAttributeDefName().findByAttributeDef(theAttributeDef.getId());
      
      totalCount[0] += attributeDefNames.size();
      
      //easy lookups
      Map<String, AttributeDefName> attributeDefNamesById = new HashMap<String, AttributeDefName>();
      Map<String, AttributeDefName> attributeDefNamesByName = new HashMap<String, AttributeDefName>();
      for (AttributeDefName current : attributeDefNames) {
        attributeDefNamesById.put(current.getId(), current);
        attributeDefNamesByName.put(current.getName(), current);
      }
      
      helperSyncAttributeDefNames(attributeDefName, hib3GrouploaderLog, grouperLoaderDb,
          grouperSession, attributeLoaderAttrsLike, attributeLoaderAttrQuery, jobMessage,
          jobStatus, totalCount, processedCount, theAttributeDef, attributeDefNames,
          attributeDefNamesById, attributeDefNamesByName);
      
      helperSyncAttributeDefNameSets(attributeDefName, hib3GrouploaderLog,
          grouperLoaderDb, attributeLoaderAttrSetQuery, jobMessage, jobStatus,
          totalCount, processedCount, theAttributeDef, attributeDefNamesByName, 
          attributeDefNamesById, attributeLoaderAttrsLike);

      //####################################
      //## ALL ATTRIBUTE ACTIONS
      if (!StringUtils.isBlank(attributeLoaderActionQuery) || !StringUtils.isBlank(attributeLoaderActionSetQuery)) {
        //
        //lets get all of them
        Set<AttributeAssignAction> actions = null;
        Map<String, AttributeAssignAction> actionsByName = null;
        Map<String, AttributeAssignAction> actionsById = null;

        actions = GrouperDAOFactory.getFactory()
          .getAttributeAssignAction().findByAttributeDefId(theAttributeDef.getId());

        actionsByName = new HashMap<String, AttributeAssignAction>();
        actionsById = new HashMap<String, AttributeAssignAction>();

        for (AttributeAssignAction current : actions) {
          actionsByName.put(current.getName(), current);
          actionsById.put(current.getId(), current);
        }

        totalCount[0] += actions.size();
        
        
        helperSyncAttributeActions(attributeDefName, hib3GrouploaderLog, grouperLoaderDb,
            attributeLoaderActionQuery, jobMessage, jobStatus,
            totalCount, processedCount, theAttributeDef, actionsByName, actionsById);
        
        helperSyncAttributeActionSets(attributeDefName, hib3GrouploaderLog,
            grouperLoaderDb, attributeLoaderActionSetQuery, jobMessage, jobStatus,
            totalCount, processedCount, theAttributeDef, actions, actionsByName, actionsById);
        
      //###################################
      //## END ATTRIBUTE ACTIONS OVERALL
      }
      
      hib3GrouploaderLog.setStatus(status.name());
      //take out the job status
      hib3GrouploaderLog.setJobMessage(jobMessage.toString());

      if (LOG.isInfoEnabled()) {
        LOG.info(attributeDefName + " done syncing attributeDef, processed " + processedCount[0] + " records.  Total members: " 
            + totalCount[0] + ", inserts: " + hib3GrouploaderLog.getInsertCount()
            + ", deletes: " + hib3GrouploaderLog.getDeleteCount());
      }
    } catch (Exception e) {
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.insertJobMessage(ExceptionUtils.getFullStackTrace(e));
      LOG.error("Problem with attributeDef: " + attributeDefName, e);
      throw new RuntimeException("Problem with attributeDef: " + attributeDefName, e);
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
   * 
   * @param attributeDefName
   * @param hib3GrouploaderLog
   * @param grouperLoaderDb
   * @param attributeLoaderActionSetQuery
   * @param jobMessage
   * @param jobStatus
   * @param totalCount
   * @param processedCount
   * @param theAttributeDef
   * @param actions
   * @param actionsByName
   * @param actionsById 
   */
  private static void helperSyncAttributeActionSets(final String attributeDefName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, GrouperLoaderDb grouperLoaderDb,
      String attributeLoaderActionSetQuery, final StringBuilder jobMessage,
      final String[] jobStatus, int[] totalCount, int[] processedCount,
      AttributeDef theAttributeDef, Set<AttributeAssignAction> actions,
      Map<String, AttributeAssignAction> actionsByName, Map<String, AttributeAssignAction> actionsById ) {
    if (!StringUtils.isBlank(attributeLoaderActionSetQuery)) {
      //####################################
      //## ALL ATTRIBUTE ACTION SETS
      //
      //lets get all of them
      Set<AttributeAssignActionSet> actionSets = null;
      Map<MultiKey, AttributeAssignActionSet> actionSetsByIfThenName = null;
      
      if (!StringUtils.isBlank(attributeLoaderActionSetQuery)) {
        actionSets = GrouperDAOFactory.getFactory()
          .getAttributeAssignActionSet().findByDepthOneForAttributeDef(theAttributeDef.getId());
        
        actionSetsByIfThenName = new HashMap<MultiKey, AttributeAssignActionSet>();
        
        for (AttributeAssignActionSet current : actionSets) {
          AttributeAssignAction ifHasAttributeAssignAction = actionsById.get(current.getIfHasAttrAssignActionId());
          AttributeAssignAction thenHasAttributeAssignAction = actionsById.get(current.getThenHasAttrAssignActionId());
          actionSetsByIfThenName.put(new MultiKey(ifHasAttributeAssignAction.getName(), thenHasAttributeAssignAction.getName()), current);
        }
        
        totalCount[0] += actions.size();
      }
      
      GrouperLoaderResultset attributeActionSetResultset = new GrouperLoaderResultset(
          grouperLoaderDb, attributeLoaderActionSetQuery);
      
      int numberOfRows = attributeActionSetResultset.numberOfRows();
      hib3GrouploaderLog.addTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(attributeDefName + " syncing " + numberOfRows + " attributeActionSet rows");
      }
      
      int count = 0;
      
      //loop through new ones
      int numberOfAttributeActionSets = attributeActionSetResultset.numberOfRows();
      processedCount[0] += numberOfAttributeActionSets;
      
      for (int i=0; i<attributeActionSetResultset.numberOfRows(); i++) {
        
        String ifHasActionName = (String)attributeActionSetResultset.getCell(
            i, GrouperLoaderResultset.IF_HAS_ACTION_NAME_COL, true);
        
        String thenHasActionName = (String)attributeActionSetResultset.getCell(
            i, GrouperLoaderResultset.THEN_HAS_ACTION_NAME_COL, false);
        
        //see if ok
        MultiKey multiSetKey = new MultiKey(ifHasActionName, thenHasActionName);
        AttributeAssignActionSet existingAttributeActionSet = actionSetsByIfThenName.get(multiSetKey);
        
        if (existingAttributeActionSet != null) {
          
          //if found we are in sync
          actionSetsByIfThenName.remove(multiSetKey);
          
        } else {
                      
          AttributeAssignAction ifHasAttributeAction = actionsByName.get(ifHasActionName);
          AttributeAssignAction thenHasAttributeAction = actionsByName.get(thenHasActionName);
          
          if (ifHasAttributeAction == null || thenHasAttributeAction == null) {

            
            StringBuilder error = new StringBuilder();
            
            if (ifHasAttributeAction == null) {
              error.append(", Cant find ifHasActionName: " + ifHasActionName + ", ");
            }
            if (thenHasAttributeAction == null) {
              error.append(", Cant find thenHasActionName: " + thenHasActionName + ", ");
            }
            
            LOG.error(error);
            //insert so we dont run out of space
            jobMessage.insert(0, error);
            hib3GrouploaderLog.setJobMessage(jobMessage.toString());
            hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
            hib3GrouploaderLog.store();
          } else {

            if (LOG.isDebugEnabled()) {
              LOG.debug(attributeDefName + " will insert " + ifHasActionName + " --> " + thenHasActionName
                  + ", " + count + " of " + numberOfAttributeActionSets + " attributeActionSets");
            }
            
            totalCount[0]++;
          
            //this is an insert
            ifHasAttributeAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(thenHasAttributeAction);
            
            hib3GrouploaderLog.addInsertCount(1);
          }                      
        }
        attributeActionSetResultset.remove(i);
        i--; //since we are removing a row, we need to decrement where we are...
        
        if (count != 0 && count % 500 == 0) {
          String logStatus = attributeDefName + " processed " + totalCount[0] 
            + " attributeActionSet records, finding new attributeActionSets to insert/remove, " + count 
            + " of " + numberOfAttributeActionSets + " attributeActionSets";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }

        count++;
        
        //###################################
        //## END ATTRIBUTE ACTION SET
      }
      
      
      //##########################
      //## Now we can remove ones in DB that shouldnt be there
      for (MultiKey current : GrouperUtil.nonNull(actionSetsByIfThenName).keySet()) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug(attributeDefName + " will delete action " + current.getKey(0) + " --> " + current.getKey(1));
        }
        totalCount[0]--;
        
        String ifHasActionName = (String)current.getKey(0);
        
        String thenHasActionName = (String)current.getKey(1);
          
        AttributeAssignAction ifHasAttributeAction = actionsByName.get(ifHasActionName);
        AttributeAssignAction thenHasAttributeAction = actionsByName.get(thenHasActionName);
        
        ifHasAttributeAction.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(thenHasAttributeAction);
        
        hib3GrouploaderLog.addDeleteCount(1);
      }

      //###################################
      //## END ATTRIBUTE ACTION SET
    }
  }

  /**
   * 
   * @param attributeDefName
   * @param hib3GrouploaderLog
   * @param grouperLoaderDb
   * @param attributeLoaderActionQuery
   * @param jobMessage
   * @param jobStatus
   * @param totalCount
   * @param processedCount
   * @param theAttributeDef
   * @param actionsByName
   * @param actionsById 
   */
  private static void helperSyncAttributeActions(final String attributeDefName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, GrouperLoaderDb grouperLoaderDb,
      String attributeLoaderActionQuery,
      final StringBuilder jobMessage, final String[] jobStatus, int[] totalCount,
      int[] processedCount, AttributeDef theAttributeDef,
      Map<String, AttributeAssignAction> actionsByName, Map<String, AttributeAssignAction> actionsById) {
    //####################################
    //## NEW ATTRIBUTE ACTIONS TO IMPORT
    //
    if (!StringUtils.isBlank(attributeLoaderActionQuery)) {
      
      Map<String, AttributeAssignAction> actionsToRemove = new HashMap<String, AttributeAssignAction>(actionsByName);
      
      GrouperLoaderResultset attributeActionResultset = new GrouperLoaderResultset(
          grouperLoaderDb, attributeLoaderActionQuery);
      int numberOfRows = attributeActionResultset.numberOfRows();
      hib3GrouploaderLog.addTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(attributeDefName + " syncing " + numberOfRows + " attributeAction rows");
      }

      int count = 0;
      //loop through new ones
      int numberOfAttributeActions = attributeActionResultset.numberOfRows();
      processedCount[0] += numberOfAttributeActions;
      
      for (int i=0; i<attributeActionResultset.numberOfRows(); i++) {
        
        String action = (String)attributeActionResultset.getCell(
            i, GrouperLoaderResultset.ACTION_NAME_COL, true);
        
        //see if ok
        AttributeAssignAction existingAttributeAssignAction = actionsByName.get(action);
        
        if (existingAttributeAssignAction != null) {
          
          //if found we are in sync
          actionsToRemove.remove(action);
          
        } else {
                      
          if (LOG.isDebugEnabled()) {
            LOG.debug(attributeDefName + " will insert action " + action
                + ", " + count + " of " + numberOfAttributeActions + " attributeActions");
          }
          totalCount[0]++;
          
          //this is an insert
          AttributeAssignAction actionInserted = theAttributeDef.getAttributeDefActionDelegate().addAction(action);
          
          hib3GrouploaderLog.addInsertCount(1);
          
          actionsByName.put(action, actionInserted);
          actionsById.put(actionInserted.getId(), actionInserted);
                      
        }
        attributeActionResultset.remove(i);
        i--; //since we are removing a row, we need to decrement where we are...
        
        if (count != 0 && count % 500 == 0) {
          String logStatus = attributeDefName + " processed " + totalCount[0] 
            + " attributeAction records, finding new attributeActions to insert/remove, " + count 
            + " of " + numberOfAttributeActions + " attributeActions";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }

        count++;
        
        //###################################
        //## END ATTRIBUTE ACTION ADD
      }
      
      
      //##########################
      //## Now we can remove actions in DB that shouldnt be there
      for (String currentAction : actionsToRemove.keySet()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(attributeDefName + " will delete action " + currentAction);
        }
        totalCount[0]--;
          
        theAttributeDef.getAttributeDefActionDelegate().removeAction(currentAction);
        
        hib3GrouploaderLog.addDeleteCount(1);
      }

      
      //###################################
      //## END ATTRIBUTE ACTION OVERALL
    }
  }

  /**
   * 
   * @param attributeDefName
   * @param hib3GrouploaderLog
   * @param grouperLoaderDb
   * @param attributeLoaderAttrSetQuery
   * @param jobMessage
   * @param jobStatus
   * @param totalCount
   * @param processedCount
   * @param theAttributeDef
   * @param attributeDefNamesByName
   * @param attributeDefNamesById 
   * @param attributeLoaderAttrsLike 
   */
  private static void helperSyncAttributeDefNameSets(final String attributeDefName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, GrouperLoaderDb grouperLoaderDb,
      String attributeLoaderAttrSetQuery, final StringBuilder jobMessage,
      final String[] jobStatus, int[] totalCount, int[] processedCount,
      AttributeDef theAttributeDef, Map<String, AttributeDefName> attributeDefNamesByName,
      Map<String, AttributeDefName> attributeDefNamesById, String attributeLoaderAttrsLike) {
    //###################################
    //## QUERY FOR ATTRIBUTE DEF NAME SET
    if (!StringUtils.isBlank(attributeLoaderAttrSetQuery)) {
      
      GrouperLoaderResultset attributeDefNameSetResultset = new GrouperLoaderResultset(
          grouperLoaderDb, attributeLoaderAttrSetQuery);
      Set<AttributeDefNameSet> existingAttributeDefNameSets = GrouperDAOFactory.getFactory()
        .getAttributeDefNameSet().findByDepthOneForAttributeDef(theAttributeDef.getId());
      
      Map<MultiKey, AttributeDefNameSet> attributeDefNameSetMap = new HashMap<MultiKey,AttributeDefNameSet>();
      
      //make a lookup map
      for (AttributeDefNameSet attributeDefNameSet : existingAttributeDefNameSets) {
        
        AttributeDefName ifHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getIfHasAttributeDefNameId());
        AttributeDefName thenHasAttributeDefName = attributeDefNamesById.get(attributeDefNameSet.getThenHasAttributeDefNameId());
        
        attributeDefNameSetMap.put(new MultiKey(ifHasAttributeDefName.getName(), 
            thenHasAttributeDefName.getName()), attributeDefNameSet);
        
      }
      
      int numberOfRows = attributeDefNameSetResultset.numberOfRows();
      hib3GrouploaderLog.addTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(attributeDefName + " syncing " + numberOfRows + " attributeDefNameSet rows");
      }
      
      int count = 0;
      //loop through new ones
      int numberOfAttributeDefNameSets = attributeDefNameSetResultset.numberOfRows();
      processedCount[0] += numberOfAttributeDefNameSets;
      
      for (int i=0; i<attributeDefNameSetResultset.numberOfRows(); i++) {
        
        String ifHasDefNameName = (String)attributeDefNameSetResultset.getCell(
            i, GrouperLoaderResultset.IF_HAS_ATTR_NAME_COL, true);
        
        String thenHasDefNameName = (String)attributeDefNameSetResultset.getCell(
            i, GrouperLoaderResultset.THEN_HAS_ATTR_NAME_COL, false);
        
        //see if ok
        MultiKey multiSetKey = new MultiKey(ifHasDefNameName, thenHasDefNameName);
        AttributeDefNameSet existingAttributeDefNameSet = attributeDefNameSetMap.get(multiSetKey);
        
        if (existingAttributeDefNameSet != null) {
          
          //if found we are in sync
          attributeDefNameSetMap.remove(multiSetKey);
          
        } else {
                      
          AttributeDefName ifHasAttributeDefName = attributeDefNamesByName.get(ifHasDefNameName);
          AttributeDefName thenHasAttributeDefName = attributeDefNamesByName.get(thenHasDefNameName);
          
          if (ifHasAttributeDefName == null || thenHasAttributeDefName == null) {

            
            StringBuilder error = new StringBuilder();
            
            if (ifHasAttributeDefName == null) {
              error.append(", Cant find ifHasAttrDefName: " + ifHasDefNameName + ", ");
            }
            if (thenHasAttributeDefName == null) {
              error.append(", Cant find thenHasAttrDefName: " + thenHasDefNameName + ", ");
            }
            
            LOG.error(error);
            //insert so we dont run out of space
            jobMessage.insert(0, error);
            hib3GrouploaderLog.setJobMessage(jobMessage.toString());
            hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
            hib3GrouploaderLog.store();
          } else {

            if (LOG.isDebugEnabled()) {
              LOG.debug(attributeDefName + " will insert " + ifHasDefNameName + " --> " + thenHasDefNameName
                  + ", " + count + " of " + numberOfAttributeDefNameSets + " attributeDefNameSets");
            }
            
            totalCount[0]++;
            
            //this is an insert
            ifHasAttributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(thenHasAttributeDefName);
            
            hib3GrouploaderLog.addInsertCount(1);
            
          }
          
                      
        }
        attributeDefNameSetResultset.remove(i);
        i--; //since we are removing a row, we need to decrement where we are...
        
        if (count != 0 && count % 500 == 0) {
          String logStatus = attributeDefName + " processed " + totalCount[0] 
            + " attributeDefNameSet records, finding new attributeDefNameSets to insert/remove, " + count 
            + " of " + numberOfAttributeDefNameSets + " attributeDefNameSets";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }

        count++;
        
        //###################################
        //## END ATTRIBUTE DEF NAME SET
      }
      
      
      //##########################
      if (!StringUtils.isBlank(attributeLoaderAttrsLike)) {
        //## Now we can remove ones in DB that shouldnt be there
        for (MultiKey current : GrouperUtil.nonNull(attributeDefNameSetMap).keySet()) {
          String ifHasDefNameName = (String)current.getKey(0);
          
          String thenHasDefNameName = (String)current.getKey(1);
            
          //make sure these are covered
          if (GrouperUtil.matchSqlString(attributeLoaderAttrsLike, ifHasDefNameName)
              && GrouperUtil.matchSqlString(attributeLoaderAttrsLike, thenHasDefNameName)) {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(attributeDefName + " will delete " + ifHasDefNameName + " --> " + thenHasDefNameName);
            }

            AttributeDefName ifHasAttributeDefName = attributeDefNamesByName.get(ifHasDefNameName);
            AttributeDefName thenHasAttributeDefName = attributeDefNamesByName.get(thenHasDefNameName);
            
            totalCount[0]--;
            
            ifHasAttributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(thenHasAttributeDefName);
            
            hib3GrouploaderLog.addDeleteCount(1);
          } else {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(attributeDefName + " will not delete " + ifHasDefNameName + " --> " + thenHasDefNameName 
                  + ", since one or both doesnt match sql like string: '" + attributeLoaderAttrsLike + "'");
            }

          }
          
        }
        
      }
      
    }
  }

  /**
   * 
   * @param attributeDefName
   * @param hib3GrouploaderLog
   * @param grouperLoaderDb
   * @param grouperSession
   * @param attributeLoaderAttrsLike
   * @param attributeLoaderAttrQuery
   * @param jobMessage
   * @param jobStatus
   * @param totalCount
   * @param processedCount
   * @param theAttributeDef
   * @param attributeDefNames
   * @param attributeDefNamesById
   * @param attributeDefNamesByName
   */
  private static void helperSyncAttributeDefNames(final String attributeDefName,
      Hib3GrouperLoaderLog hib3GrouploaderLog, GrouperLoaderDb grouperLoaderDb,
      final GrouperSession grouperSession, String attributeLoaderAttrsLike,
      String attributeLoaderAttrQuery, final StringBuilder jobMessage,
      final String[] jobStatus, int[] totalCount, int[] processedCount,
      AttributeDef theAttributeDef, Set<AttributeDefName> attributeDefNames,
      Map<String, AttributeDefName> attributeDefNamesById,
      Map<String, AttributeDefName> attributeDefNamesByName) {
    //#############################
    // SYNC ATTRIBUTE DEF NAMES
    if (!StringUtils.isBlank(attributeLoaderAttrQuery)) {

      //lets find all like attributeDefNames managed
      Set<AttributeDefName> attributeDefNamesLike = null;
      
      if (!StringUtils.isBlank(attributeLoaderAttrsLike)) {
        if (StringUtils.equals("%", attributeLoaderAttrsLike)) {
          attributeDefNamesLike = new HashSet<AttributeDefName>(attributeDefNames);
        } else {
          attributeDefNamesLike = GrouperDAOFactory.getFactory().getAttributeDefName()
            .findByAttributeDefLike(theAttributeDef.getId(), attributeLoaderAttrsLike);
        }
      }
      
      //###################################
      //## QUERY FOR ATTRIBUTE DEF NAMES FROM QUERY (NEW LIST)
      GrouperLoaderResultset attributeDefNameResultset = null;
      
      if (!StringUtils.isBlank(attributeLoaderAttrQuery)) {
        attributeDefNameResultset = new GrouperLoaderResultset(
            grouperLoaderDb, attributeLoaderAttrQuery);
      }
      
      int numberOfRows = attributeDefNameResultset.numberOfRows();
      hib3GrouploaderLog.addTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(attributeDefName + " syncing " + numberOfRows + " attributeDefName rows");
      }
      
      int count = 0;
      //loop through new ones
      int numberOfAttributeDefNames = attributeDefNameResultset.numberOfRows();
      processedCount[0] += numberOfAttributeDefNames;
      
      for (int i=0; i<attributeDefNameResultset.numberOfRows(); i++) {
        
        //the size changes as we iterate through...  so check again
        if (i >= attributeDefNameResultset.numberOfRows()) {
          break;
        }
        
        String name = (String)attributeDefNameResultset.getCell(
            i, GrouperLoaderResultset.ATTR_NAME_COL, true);
        
        String displayName = (String)attributeDefNameResultset.getCell(
            i, GrouperLoaderResultset.ATTR_DISPLAY_NAME_COL, false);
        
        String description = (String)attributeDefNameResultset.getCell(
            i, GrouperLoaderResultset.ATTR_DESCRIPTION_COL, false);
        
        //see if ok
        AttributeDefName existingAttributeDefName = attributeDefNamesByName.get(name);
        
        if (existingAttributeDefName != null) {
          
          boolean hasChange = false;
          
          if (!StringUtils.isBlank(displayName)) {
            
            //get extension
            String displayExtension = GrouperUtil.extensionFromName(displayName);
            if (!StringUtils.equals(displayExtension, existingAttributeDefName.getDisplayExtension())) {
              existingAttributeDefName.setDisplayExtensionDb(displayExtension);
              hasChange = true;
            }
            
          }
          if (!StringUtils.isBlank(description)) {
            
            if (!StringUtils.equals(description, existingAttributeDefName.getDescription())) {
              existingAttributeDefName.setDescription(description);
              hasChange = true;
            }
          }

          if (hasChange) {
            if (LOG.isDebugEnabled()) {
              LOG.debug(attributeDefName + " will update " + (existingAttributeDefName == null ? null : existingAttributeDefName.getName() )
                  + ", " + count + " of " + numberOfAttributeDefNames + " attributeDefNames");
            }
            existingAttributeDefName.store();
            hib3GrouploaderLog.addUpdateCount(1);
          }
          
          //manage the list to remove
          attributeDefNames.remove(existingAttributeDefName);
          if (attributeDefNamesLike != null) {
            attributeDefNamesLike.remove(existingAttributeDefName);
          }
          
        } else {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(attributeDefName + " will insert " + (existingAttributeDefName == null ? null : existingAttributeDefName.getName() )
                + ", " + count + " of " + numberOfAttributeDefNames + " attributeDefNames");
          }
          totalCount[0]++;
          //this is an insert
          AttributeDefName insertAttributeDefName = new AttributeDefNameSave(grouperSession, theAttributeDef).assignName(name)
            .assignDisplayName(displayName).assignDescription(description).assignCreateParentStemsIfNotExist(true).save();
          hib3GrouploaderLog.addInsertCount(1);
          
          //might need these for foreign keys later
          attributeDefNamesById.put(insertAttributeDefName.getId(), insertAttributeDefName);
          attributeDefNamesByName.put(insertAttributeDefName.getName(), insertAttributeDefName);
          
        }
        attributeDefNameResultset.remove(i);
        i--; //since we are removing a row, we need to decrement where we are...
        
        if (count != 0 && count % 500 == 0) {
          String logStatus = attributeDefName + " processed " + totalCount[0] 
            + " records, finding new attributeDefNames to update/remove, " + count 
            + " of " + numberOfAttributeDefNames + " attributeDefNames";
          LOG.info(logStatus);
          jobStatus[0] = logStatus;
          hib3GrouploaderLog.setJobMessage(jobStatus[0] + ", " + jobMessage);
          hib3GrouploaderLog.store();
        }

        count++;
      }
      
      //##########################
      //## Now we can remove ones in DB that shouldnt be there
      for (AttributeDefName current : GrouperUtil.nonNull(attributeDefNamesLike)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(attributeDefName + " will delete " + current.getName());
        }
        totalCount[0]--;
        current.delete();
        hib3GrouploaderLog.addDeleteCount(1);
      }
      
    }
  }

  /**
   * for all attribute jobs in this loader type, schedule them with quartz
   */
  public static void scheduleAttributeLoads() {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(
          SubjectFinder.findById("GrouperSystem", true)
        );

      //lets see if there is configuration
      String attrRootStem = GrouperConfig.getProperty("grouper.attribute.rootStem");
      if (StringUtils.isBlank(attrRootStem)) {
        return;
      }
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
          GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoader", false);
      
      //see if attributeDef
      if (attributeDefName == null) {
        return;
      }
      
      //lets get the attributeDefs which have this type
      Set<AttributeDef> attributeDefs = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAttributeDefsByAttributeDefNameId(attributeDefName.getId());

      for (AttributeDef attributeDef : attributeDefs) {
        
        String jobName = null;
        String attributeDefUuid = null;
        String grouperLoaderScheduleType = null;
        String grouperLoaderAndGroups = null;
        String grouperLoaderQuartzCron = null;
        Integer grouperLoaderIntervalSeconds = null;
        Integer grouperLoaderPriority = null;
        String grouperLoaderType = null;
        try {
          
          attributeDefUuid = attributeDef.getUuid();
          //lets get all attribute values
          grouperLoaderType = attributeDef.getAttributeValueDelegate().retrieveValueString(GrouperCheckConfig.attributeLoaderStemName() + ":" + GrouperLoader.ATTRIBUTE_LOADER_TYPE);
          
          GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
  
          jobName = grouperLoaderTypeEnum.name() + "__" + attributeDef.getName() + "__" + attributeDef.getUuid();
          
          //get the real attributes
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_DB_NAME);
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTR_QUERY);
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTRS_LIKE);
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTR_SET_QUERY);
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_ACTION_QUERY);
          grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_ACTION_SET_QUERY);
          grouperLoaderScheduleType = grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_SCHEDULE_TYPE);
          grouperLoaderQuartzCron = grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDef(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_QUARTZ_CRON);
          grouperLoaderIntervalSeconds = grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDefInteger(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_INTERVAL_SECONDS);
          grouperLoaderPriority = grouperLoaderTypeEnum.attributeValueValidateRequiredAttrDefInteger(attributeDef, GrouperLoader.ATTRIBUTE_LOADER_PRIORITY);
          
          scheduleJob(jobName, false, grouperLoaderScheduleType, grouperLoaderQuartzCron,
              grouperLoaderIntervalSeconds, grouperLoaderPriority);
          
        } catch (Exception e) {
          String errorMessage = null;
          
          //dont fail on all if any fail
          try {
            errorMessage = "Could not schedule attributeDef: '" + attributeDef.getName() + "', '" + attributeDef.getUuid() + "'";
            LOG.error(errorMessage, e);
            errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
          } catch (Exception e2) {
            errorMessage = "Could not schedule attributeDef.";
            //dont let error message mess us up
            LOG.error(errorMessage, e);
            LOG.error(e2);
            errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e) + "\n" + ExceptionUtils.getFullStackTrace(e2);
          }
          try {
            //lets enter a log entry so it shows up as error in the db
            Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
            hib3GrouploaderLog.setGroupUuid(attributeDefUuid);
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
    
    if (StringUtils.isBlank(grouperLoaderScheduleType)) {
      boolean hasCron = !StringUtils.isBlank(grouperLoaderQuartzCron);
      boolean hasInterval = grouperLoaderIntervalSeconds != null;
      if (hasCron != hasInterval) {
        if (hasCron) {
          grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON.name();
        }
        if (hasInterval) {
          grouperLoaderScheduleType = GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
        }
      }
    }
    
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
