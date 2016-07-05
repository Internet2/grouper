/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderResultset.Row;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
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
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.messaging.MessagingListenerBase;
import edu.internet2.middleware.grouper.messaging.MessagingListenerController;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperReport;
import edu.internet2.middleware.grouper.misc.GrouperReportException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperFuture;
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
          loaderJobBean.getGrouperLoaderDb(), loaderJobBean.getQuery(), loaderJobBean.getHib3GrouploaderLogOverall().getJobName(), 
          loaderJobBean.getHib3GrouploaderLogOverall());
      
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
            int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7);
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
            int daysToKeepLogs = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.change_log_entry.days", 14);
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
          String usduSchedule = StringUtils.defaultString(GrouperLoaderConfig.retrieveConfig().propertyValueString(
              "daily.report.usdu.daysToRun")).toLowerCase();
          String badMemberSchedule = StringUtils.defaultString(GrouperLoaderConfig.retrieveConfig().propertyValueString(
              "daily.report.badMembership.daysToRun")).toLowerCase();
          
          boolean isRunUsdu = dayListContainsToday(usduSchedule);
          boolean isRunBadMember = dayListContainsToday(badMemberSchedule);
          
          String emailTo = GrouperLoaderConfig.retrieveConfig().propertyValueString("daily.report.emailTo");
          String reportDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueString("daily.report.saveInDirectory");
          
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
        } else if (StringUtils.equals(GROUPER_BUILTIN_MESSAGING_DAEMON, hib3GrouploaderLog.getJobName())) {

          int processedRecords = GrouperBuiltinMessagingSystem.cleanOldProcessedMessages();
          
          int unprocessedRecords = GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages();
          
          hib3GrouploaderLog.setUpdateCount(processedRecords + unprocessedRecords);

          hib3GrouploaderLog.setJobMessage("Ran builtin messaging daemon, deleted " + processedRecords + " processed records, deleted " + unprocessedRecords + " unprocessed records.");
          
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
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#runJob(LoaderJobBean)
       */
      @SuppressWarnings("unchecked")
      @Override
      public void runJob(LoaderJobBean loaderJobBean) {
        
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

        String groupNameOverall = loaderJobBean.getGroupNameOverall();
        GrouperLoaderDb grouperLoaderDb = loaderJobBean.getGrouperLoaderDb();
        String query = loaderJobBean.getQuery();
        Hib3GrouperLoaderLog hib3GrouploaderLogOverall = loaderJobBean.getHib3GrouploaderLogOverall();
        long startTime = loaderJobBean.getStartTime();
        
        GrouperSession grouperSession = loaderJobBean.getGrouperSession();
        List<Group> andGroups = loaderJobBean.getAndGroups();
        List<GroupType> groupTypes = GrouperUtil.nonNull(loaderJobBean.getGroupTypes());
        String groupLikeString = loaderJobBean.getGroupLikeString();
        String groupQuery = loaderJobBean.getGroupQuery();

        if (LOG.isDebugEnabled()) {
          LOG.debug(groupNameOverall + ": start syncing membership");
        }
        
        GrouperLoaderStatus[] statusOverall = new GrouperLoaderStatus[]{GrouperLoaderStatus.SUCCESS};
        
        try {
          //get a resultset from the db
          final GrouperLoaderResultset grouperLoaderResultsetOverall = new GrouperLoaderResultset(grouperLoaderDb, 
              query + " order by group_name", loaderJobBean.getHib3GrouploaderLogOverall().getJobName(), 
              loaderJobBean.getHib3GrouploaderLogOverall());
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": found " + grouperLoaderResultsetOverall + " members overall");
          }

          hib3GrouploaderLogOverall.setMillisGetData((int)(System.currentTimeMillis()-startTime));

          grouperLoaderResultsetOverall.bulkLookupSubjects();
          
          //#######################################
          //Get group metadata
          int groupMetadataNumberOfRows = 0;
          Map<String, String> groupNameToDisplayName = new LinkedHashMap<String, String>();
          Map<String, String> groupNameToDescription = new LinkedHashMap<String, String>();
          Map<String, Subject> subjectCache = new HashMap<String, Subject>();
          Map<String, Map<Privilege, List<Subject>>> privsToAdd = new LinkedHashMap<String, Map<Privilege, List<Subject>>>();
          Set<String> groupNamesFromGroupQuery = null;
          if (!StringUtils.isBlank(groupQuery)) {
            
            groupNamesFromGroupQuery = new LinkedHashSet<String>();
            //get a resultset from the db
            final GrouperLoaderResultset grouperLoaderGroupsResultset = new GrouperLoaderResultset(
                grouperLoaderDb, groupQuery + " order by group_name", hib3GrouploaderLogOverall.getJobName(), 
                hib3GrouploaderLogOverall);
            
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
              {
                String groupAttrReaders = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_ATTR_READERS_COL, false);
                if (!StringUtils.isBlank(groupAttrReaders)) {
                  List<Subject> groupAttrReadSubjects = lookupSubject(subjectCache, groupAttrReaders);
                  privsToAddForGroup.put(AccessPrivilege.GROUP_ATTR_READ, groupAttrReadSubjects);
                }
              }
              {
                String groupAttrUpdaters = (String)grouperLoaderGroupsResultset.getCell(i, GrouperLoaderResultset.GROUP_ATTR_UPDATERS_COL, false);
                if (!StringUtils.isBlank(groupAttrUpdaters)) {
                  List<Subject> groupAttrUpdateSubjects = lookupSubject(subjectCache, groupAttrUpdaters);
                  privsToAddForGroup.put(AccessPrivilege.GROUP_ATTR_UPDATE, groupAttrUpdateSubjects);
                }
              }
            }
            
          }
      
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": found " + groupMetadataNumberOfRows + " number of metadata rows");
          }
      
          //End group metadata
          //#######################################

          syncGroupList(grouperLoaderResultsetOverall, startTime, grouperSession, 
              andGroups, groupTypes, groupLikeString, groupNameOverall, hib3GrouploaderLogOverall,
              statusOverall, loaderJobBean.getGrouperLoaderDb(), groupNameToDisplayName, 
              groupNameToDescription, privsToAdd, groupNamesFromGroupQuery);
          
        } finally {
          hib3GrouploaderLogOverall.setStatus(statusOverall[0].name());
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
            
            Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

            if (LOG.isDebugEnabled()) {
              debugMap.put("operation", "runJob");
            }
            
            try {
            GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

            Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean.getHib3GrouploaderLogOverall();
            
              if (LOG.isDebugEnabled()) {
                debugMap.put("jobName", hib3GrouploaderLog.getJobName());
              }
              
            if (StringUtils.equals(GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG, hib3GrouploaderLog.getJobName())) {
    
                int recordsProcessed = ChangeLogTempToEntity.convertRecords(hib3GrouploaderLog);
                
                if (LOG.isDebugEnabled()) {
                  debugMap.put("success", true);
                  debugMap.put("recordsProcessed", recordsProcessed);
                }
    
              hib3GrouploaderLog.setJobMessage("Ran the changeLogTempToChangeLog daemon");
              
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
            } else if (hib3GrouploaderLog.getJobName().startsWith(GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
              
              String consumerName = hib3GrouploaderLog.getJobName().substring(GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length());
              
                if (LOG.isDebugEnabled()) {
                  debugMap.put("consumerName", consumerName);
                }
                
                try {
              //ok, we have the sequence, and the job name, lets get the change log records after that sequence, and give them to the 
              //consumer
              String theClassName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName + ".class");
                  
                  if (LOG.isDebugEnabled()) {
                    debugMap.put("className", theClassName);
                  }
                    
              Class<?> theClass = GrouperUtil.forName(theClassName);
                  
                  if (LOG.isDebugEnabled()) {
                    debugMap.put("class found", true);
                  }
                     
              ChangeLogConsumerBase changeLogConsumerBase = (ChangeLogConsumerBase)GrouperUtil.newInstance(theClass);

                  if (LOG.isDebugEnabled()) {
                    debugMap.put("instance created", true);
                  }
                  
              ChangeLogHelper.processRecords(consumerName, hib3GrouploaderLog, changeLogConsumerBase);
                  
                  if (LOG.isDebugEnabled()) {
                    debugMap.put("success", true);
                    debugMap.put("recordsProcessed", hib3GrouploaderLog.getTotalCount());
                  }
                } catch (RuntimeException re) {
                  LOG.error("Problem with change log consumer: " + consumerName, re);
                  throw re;
                }
            } else {
              throw new RuntimeException("Cant find implementation for job: " + hib3GrouploaderLog.getJobName());
            }
            } finally {
              if (LOG.isDebugEnabled()) {
                LOG.debug(GrouperUtil.mapToString(debugMap));
              }
            }
          }
        }, 
        
  /** 
   * various messaging jobs on the system
   */
  MESSAGE_LISTENER {

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

      Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>()
          : null;

      if (LOG.isDebugEnabled()) {
        debugMap.put("operation", "runMessagingJob");
      }

      try {
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

        Hib3GrouperLoaderLog hib3GrouploaderLog = loaderJobBean
            .getHib3GrouploaderLogOverall();

        if (LOG.isDebugEnabled()) {
          debugMap.put("jobName", hib3GrouploaderLog.getJobName());
        }

        if (hib3GrouploaderLog.getJobName().startsWith(
            GROUPER_MESSAGING_LISTENER_PREFIX)) {

          String listenerName = hib3GrouploaderLog.getJobName().substring(
              GROUPER_MESSAGING_LISTENER_PREFIX.length());

          if (LOG.isDebugEnabled()) {
            debugMap.put("listenerName", listenerName);
          }

          try {
            //ok, we have the sequence, and the job name, lets get the change log records after that sequence, and give them to the 
            //consumer
            String theClassName = GrouperLoaderConfig.retrieveConfig()
                .propertyValueString("messaging.listener." + listenerName + ".class");

            if (LOG.isDebugEnabled()) {
              debugMap.put("className", theClassName);
            }

            Class<?> theClass = GrouperUtil.forName(theClassName);

            if (LOG.isDebugEnabled()) {
              debugMap.put("class found", true);
            }

            MessagingListenerBase messagingListenerBase = (MessagingListenerBase) GrouperUtil
                .newInstance(theClass);

            if (LOG.isDebugEnabled()) {
              debugMap.put("instance created", true);
            }

            MessagingListenerController.processRecords(listenerName, hib3GrouploaderLog,
                messagingListenerBase);

            if (LOG.isDebugEnabled()) {
              debugMap.put("success", true);
              debugMap.put("recordsProcessed", hib3GrouploaderLog.getTotalCount());
            }
          } catch (RuntimeException re) {
            LOG.error("Problem with change log consumer: " + listenerName, re);
            throw re;
          }
        } else {
          throw new RuntimeException("Cant find implementation for job: "
              + hib3GrouploaderLog.getJobName());
        }
      } finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug(GrouperUtil.mapToString(debugMap));
        }
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
    }, 
    
    /** 
     * simple ldap query where all results are all members of group.
     * must have a subject id attribute
     */
    LDAP_SIMPLE {
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapServerIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapFilterName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapTypeName(), attributeName);
      }

      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
       */
      @Override
      public boolean attributeOptional(String attributeName) {
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAndGroupsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapPriorityName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), attributeName)
            ;
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapExtraAttributesName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDisplayExtensionExpressionName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapReadersName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapViewersName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAdminsName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapUpdatersName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptinsName(), attributeName)
        // not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptoutsName(), attributeName)

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
                
        final GrouperLoaderResultset grouperLoaderResultset = new GrouperLoaderResultset(loaderJobBean.getLdapServerId(), 
            loaderJobBean.getLdapFilter(), loaderJobBean.getLdapSearchDn(), loaderJobBean.getLdapSubjectAttribute(), 
            loaderJobBean.getLdapSourceId(), loaderJobBean.getLdapSubjectIdType(), loaderJobBean.getLdapSearchScope(), 
            loaderJobBean.getHib3GrouploaderLogOverall().getJobName(), 
            loaderJobBean.getHib3GrouploaderLogOverall(), loaderJobBean.getLdapSubjectExpression());
        
        syncOneGroupMembership(loaderJobBean.getGroupNameOverall(), null, null, 
            loaderJobBean.getHib3GrouploaderLogOverall(), loaderJobBean.getStartTime(), 
            grouperLoaderResultset, false, loaderJobBean.getGrouperSession(), loaderJobBean.getAndGroups(), null, null, null);
        
      }
    }, 
    
    /** 
     * ldap query where objects are group, and filter is for multi-valued object where all results are all members of group.
     * must have a subject id attribute
     */
    LDAP_GROUP_LIST {
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapServerIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapFilterName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapTypeName(), attributeName);
      }

      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
       */
      @Override
      public boolean attributeOptional(String attributeName) {
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAndGroupsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapPriorityName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapExtraAttributesName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapReadersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapViewersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAdminsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapUpdatersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptinsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptoutsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName(), attributeName)
            ;
        
        //not allowed: StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName(), attributeName)
      }

      /**
       * sync up an attributeDefinition membership based on query and db
       */
      @SuppressWarnings("unchecked")
      @Override
      public void runJob(LoaderJobBean loaderJobBean) {
    
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
    
        GrouperSession grouperSession = loaderJobBean.getGrouperSession();

        Hib3GrouperLoaderLog hib3GrouploaderLogOverall = loaderJobBean.getHib3GrouploaderLogOverall();
        GrouperLoaderStatus[] statusOverall = new GrouperLoaderStatus[]{GrouperLoaderStatus.SUCCESS};
        

        try {
        
          //      GrouperLoaderDb grouperLoaderDb, attributeDefName
          //      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
          //      attributeLoaderAttrQuery, attributeLoaderAttrSetQuery, attributeLoaderAttrsLike
          //   attributeLoaderActionQuery, attributeLoaderActionSetQuery
            
          String ldapSubjectAttribute = loaderJobBean.getLdapSubjectAttribute();

          Map<String, String> groupNameToDisplayName = new LinkedHashMap<String, String>();
          Map<String, String> groupNameToDescription = new LinkedHashMap<String, String>();
          
          final GrouperLoaderResultset grouperLoaderResultsetOverall = new GrouperLoaderResultset();
          
          Set<String> groupNames = new HashSet<String>();
          
          grouperLoaderResultsetOverall.initForLdapListOfGroups(
              loaderJobBean.getLdapServerId(), 
              loaderJobBean.getLdapFilter(), loaderJobBean.getLdapSearchDn(), ldapSubjectAttribute, 
              loaderJobBean.getLdapSourceId(), loaderJobBean.getLdapSubjectIdType(), loaderJobBean.getLdapSearchScope(), 
              hib3GrouploaderLogOverall.getJobName(), 
              hib3GrouploaderLogOverall, 
              loaderJobBean.getLdapSubjectExpression(), loaderJobBean.getLdapExtraAttributes(),
              loaderJobBean.getLdapGroupNameExpression(), loaderJobBean.getLdapGroupDisplayNameExpression(),
              loaderJobBean.getLdapGroupDescriptionExpression(), groupNameToDisplayName, 
              groupNameToDescription, groupNames);
          
          
          String groupNameOverall = hib3GrouploaderLogOverall.getGroupNameFromJobName();
  
          GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
  
          long startTime = loaderJobBean.getStartTime();
          
          List<Group> andGroups = loaderJobBean.getAndGroups();
          List<GroupType> groupTypes = GrouperUtil.nonNull(loaderJobBean.getGroupTypes());
          String groupLikeString = loaderJobBean.getGroupLikeString();

          Map<String, Subject> subjectCache = new HashMap<String, Subject>();
          Map<String, Map<Privilege, List<Subject>>> privsToAdd = new LinkedHashMap<String, Map<Privilege, List<Subject>>>();
          
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.VIEW, loaderJobBean.getLdapGroupViewers());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.READ, loaderJobBean.getLdapGroupReaders());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.ADMIN, loaderJobBean.getLdapGroupAdmins());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.UPDATE, loaderJobBean.getLdapGroupUpdaters());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.OPTIN, loaderJobBean.getLdapGroupOptins());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.OPTOUT, loaderJobBean.getLdapGroupOptouts());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.GROUP_ATTR_READ, loaderJobBean.getLdapGroupAttrReaders());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.GROUP_ATTR_UPDATE, loaderJobBean.getLdapGroupAttrUpdaters());
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": start syncing membership");
          }
        
          
          syncGroupList(grouperLoaderResultsetOverall, startTime, grouperSession, 
              andGroups, groupTypes, groupLikeString, groupNameOverall, hib3GrouploaderLogOverall,
              statusOverall, loaderJobBean.getGrouperLoaderDb(), groupNameToDisplayName, groupNameToDescription, privsToAdd, groupNames);
          
        } finally {
          hib3GrouploaderLogOverall.setStatus(statusOverall[0].name());
        }


        
        
      }
    },
    /** 
     * ldap query where objects are users, and filter is for multi-valued object where all results are affiliations or 
     * something that represents the groups of users.
     */
    LDAP_GROUPS_FROM_ATTRIBUTES {
      
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeRequired(java.lang.String)
       */
      @Override
      public boolean attributeRequired(String attributeName) {
        
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapServerIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapFilterName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapQuartzCronName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapTypeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName(), attributeName);
      }
    
      /**
       * 
       * @see edu.internet2.middleware.grouper.app.loader.GrouperLoaderType#attributeOptional(java.lang.String)
       */
      @Override
      public boolean attributeOptional(String attributeName) {
        return StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchDnName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSearchScopeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSourceIdName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAndGroupsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapPriorityName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapReadersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapViewersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapAdminsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapUpdatersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptinsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapOptoutsName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName(), attributeName)
            || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName(), attributeName)
            ;
        
        //not allowed: || StringUtils.equals(LoaderLdapUtils.grouperLoaderLdapExtraAttributesName(), attributeName)

        
      }
      
      /**
       * sync up an attributeDefinition membership based on query and db
       */
      @SuppressWarnings("unchecked")
      @Override
      public void runJob(LoaderJobBean loaderJobBean) {
    
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
        
        GrouperSession grouperSession = loaderJobBean.getGrouperSession();

        Hib3GrouperLoaderLog hib3GrouploaderLogOverall = loaderJobBean.getHib3GrouploaderLogOverall();
        GrouperLoaderStatus[] statusOverall = new GrouperLoaderStatus[]{GrouperLoaderStatus.SUCCESS};
        

        try {
        
          //      GrouperLoaderDb grouperLoaderDb, attributeDefName
          //      Hib3GrouperLoaderLog hib3GrouploaderLog, long startTime, GrouperSession grouperSession, 
          //      attributeLoaderAttrQuery, attributeLoaderAttrSetQuery, attributeLoaderAttrsLike
          //   attributeLoaderActionQuery, attributeLoaderActionSetQuery
            
          Map<String, String> groupNameToDisplayName = new LinkedHashMap<String, String>();
          Map<String, String> groupNameToDescription = new LinkedHashMap<String, String>();

          final GrouperLoaderResultset grouperLoaderResultsetOverall = new GrouperLoaderResultset();
          grouperLoaderResultsetOverall.initForLdapGroupsFromAttributes(
              loaderJobBean.getLdapServerId(), 
              loaderJobBean.getLdapFilter(), loaderJobBean.getLdapSearchDn(), loaderJobBean.getLdapSubjectAttribute(), 
              loaderJobBean.getLdapGroupAttribute(), 
              loaderJobBean.getLdapSourceId(), loaderJobBean.getLdapSubjectIdType(), loaderJobBean.getLdapSearchScope(), 
              hib3GrouploaderLogOverall.getJobName(), 
              hib3GrouploaderLogOverall, 
              loaderJobBean.getLdapSubjectExpression(), loaderJobBean.getLdapExtraAttributes(),
              loaderJobBean.getLdapGroupNameExpression(), 
              loaderJobBean.getLdapGroupDisplayNameExpression(),
              loaderJobBean.getLdapGroupDescriptionExpression(),
              groupNameToDisplayName, groupNameToDescription, loaderJobBean.getLdapAttributeFilterExpression());


          String groupNameOverall = hib3GrouploaderLogOverall.getGroupNameFromJobName();
  
          GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
  
          long startTime = loaderJobBean.getStartTime();
          
          List<Group> andGroups = loaderJobBean.getAndGroups();
          List<GroupType> groupTypes = GrouperUtil.nonNull(loaderJobBean.getGroupTypes());
          String groupLikeString = loaderJobBean.getGroupLikeString();
          
          Map<String, Subject> subjectCache = new HashMap<String, Subject>();
          Map<String, Map<Privilege, List<Subject>>> privsToAdd = new LinkedHashMap<String, Map<Privilege, List<Subject>>>();
          
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.VIEW, loaderJobBean.getLdapGroupViewers());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.READ, loaderJobBean.getLdapGroupReaders());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.ADMIN, loaderJobBean.getLdapGroupAdmins());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.UPDATE, loaderJobBean.getLdapGroupUpdaters());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.OPTIN, loaderJobBean.getLdapGroupOptins());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.OPTOUT, loaderJobBean.getLdapGroupOptouts());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.GROUP_ATTR_READ, loaderJobBean.getLdapGroupAttrReaders());
          initPrivilegesForGroup(grouperLoaderResultsetOverall, privsToAdd, subjectCache, AccessPrivilege.GROUP_ATTR_UPDATE, loaderJobBean.getLdapGroupAttrUpdaters());

          if (LOG.isDebugEnabled()) {
            LOG.debug(groupNameOverall + ": start syncing membership");
          }
        
          
          syncGroupList(grouperLoaderResultsetOverall, startTime, grouperSession, 
              andGroups, groupTypes, groupLikeString, groupNameOverall, hib3GrouploaderLogOverall,
              statusOverall, loaderJobBean.getGrouperLoaderDb(), groupNameToDisplayName, 
              groupNameToDescription, privsToAdd, null);
          
        } finally {
          hib3GrouploaderLogOverall.setStatus(statusOverall[0].name());
        }
       
      }
    },
    
    /** 
     * Run a psp full sync.
     */
    PSP_FULL_SYNC {

      /** {@inheritDoc} */
      public boolean attributeRequired(String attributeName) {
        return false;
      }

      /** {@inheritDoc} */
      public boolean attributeOptional(String attributeName) {
        return false;
      }

      /** {@inheritDoc} */
      public void runJob(LoaderJobBean loaderJobBean) {        
        LOG.info("Running " + PSP_FULL_SYNC.name());
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);        
        String theClassName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.psp.fullSync.class");
        Class<?> theClass = GrouperUtil.forName(theClassName);
        Object theClassInstance = GrouperUtil.newInstance(theClass);
        GrouperUtil.callMethod(theClassInstance, "fullSync");
      }
    };
  
  /**
   * init the privilege for a group, add entries to the privsToAdd method
   * @param grouperLoaderResultsetOverall result set with groups
   * @param privsToAddForGroup map to add results to
   * @param subjectCache to help with resolving
   * @param privilege to add
   * @param subjectsWithPrivilege the subjects who have the privilege
   */
  public static void initPrivilegesForGroup(GrouperLoaderResultset grouperLoaderResultsetOverall, 
      Map<String, Map<Privilege, List<Subject>>> privsToAddForGroup, Map<String, Subject> subjectCache, 
      Privilege privilege, String subjectsWithPrivilege) {
    
    if (!StringUtils.isBlank(subjectsWithPrivilege)) {
      
      //same list for all groups
      List<Subject> subjectsWithPrivilegeList = lookupSubject(subjectCache, subjectsWithPrivilege);
      
      //first get the list of group names
      Set<String> groupNames = new LinkedHashSet<String>();
      for (int i = 0; i < grouperLoaderResultsetOverall.numberOfRows(); i++) {
        
        String groupName = (String)grouperLoaderResultsetOverall.retrieveRow(i).getCell("GROUP_NAME", true);
        
        if (StringUtils.isBlank(groupName)) {
          throw new RuntimeException("Why is group name blank???");
        }
        
        groupNames.add(groupName);
      }
      
      for (String groupName : groupNames) {
        
        //privs for a group
        Map<Privilege, List<Subject>> privsToAdd = privsToAddForGroup.get(groupName);
        if (privsToAdd == null) {
          privsToAdd = new HashMap<Privilege, List<Subject>>();
          privsToAddForGroup.put(groupName, privsToAdd);
          
        }
        //clone list just in case
        privsToAdd.put(privilege, new ArrayList<Subject>(subjectsWithPrivilegeList));
        
      }
    }

      
  }
  
  /**
   * sync a group list  
   * @param grouperLoaderResultsetOverall
   * @param startTime
   * @param grouperSession 
   * @param andGroups 
   * @param groupTypes 
   * @param groupLikeString 
   * @param groupNameOverall 
   * @param hib3GrouploaderLogOverall 
   * @param statusOverall 
   * @param grouperLoaderDb 
   * @param groupNameToDisplayName 
   * @param groupNameToDescription 
   * @param privsToAdd 
   * @param groupNamesFromGroupQuery if not null, this is the list of groups to sync
   */
  public static void syncGroupList(final GrouperLoaderResultset grouperLoaderResultsetOverall, long startTime,
      final GrouperSession grouperSession, final List<Group> andGroups, final List<GroupType> groupTypes, final String groupLikeString,
      final String groupNameOverall, final Hib3GrouperLoaderLog hib3GrouploaderLogOverall,
      final GrouperLoaderStatus[] statusOverall, final GrouperLoaderDb grouperLoaderDb,
      final Map<String, String> groupNameToDisplayName, final Map<String, String> groupNameToDescription,
      final Map<String, Map<Privilege, List<Subject>>> privsToAdd, final Set<String> groupNamesFromGroupQuery) {
        
    long startTimeLoadData = 0;
    try {
    
      if (LOG.isDebugEnabled()) {
        LOG.debug(groupNameOverall + ": found " + grouperLoaderResultsetOverall.numberOfRows() + " members overall");
      }
      
      hib3GrouploaderLogOverall.setMillisGetData((int)(System.currentTimeMillis()-startTime));
  
      startTimeLoadData =  System.currentTimeMillis();
  
      Set<String> groupNames = grouperLoaderResultsetOverall.groupNames();
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(groupNameOverall + ": syncing membership for " + groupNames.size() + " groups");
      }
      
      
      //#######################################
      //Delete records in groups not there anymore.  maybe delete group too
      if (!StringUtils.isBlank(groupLikeString)) {
        
        //lets see which names are not in that list
        Set<String> groupNamesManaged = HibernateSession.byHqlStatic()
          .createQuery("select g.nameDb from Group g where g.nameDb like :thePattern")
          .setString("thePattern", groupLikeString).listSet(String.class);
        
        int totalManagedGroupsCount = groupNamesManaged.size();
        int totalManagedGroupsBeingClearedCount = 0;
        int totalManagedGroupsAlreadyEmptyCount = 0;
        
        // cache data gathered during first round
        Map<String, Long> getDataTimes = new HashMap<String, Long>();
        Map<String, Set<Member>> immediateMembers = new HashMap<String, Set<Member>>();
        Map<String, Group> groups = new HashMap<String, Group>();
      
        //take out the ones which exist
        groupNamesManaged.removeAll(groupNames);
        
        Boolean isIncludeExclude = null;
        
        for (String groupNameEmpty : new LinkedHashSet<String>(groupNamesManaged)) {
          
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
              groupNamesManaged.remove(groupNameEmpty);
              totalManagedGroupsCount--;
              continue;
            }
          }
          
          Long millisGetData = 0L;
          Group groupEmpty = GroupFinder.findByName(grouperSession, groupNameEmpty, false);
          if (groupEmpty == null) {
            continue;
          }
            
          millisGetData = System.currentTimeMillis();
          Set<Member> members = GrouperUtil.nonNull(groupEmpty.getImmediateMembers());
          millisGetData = System.currentTimeMillis() - millisGetData;

          if (members.size() > 0) {
            totalManagedGroupsBeingClearedCount++;
          } else {
            totalManagedGroupsAlreadyEmptyCount++;
          }
          
          getDataTimes.put(groupNameEmpty, millisGetData);
          immediateMembers.put(groupNameEmpty, members);
          groups.put(groupNameEmpty, groupEmpty);
        }
        
        int totalManagedGroupsWithMembersCount = totalManagedGroupsCount - totalManagedGroupsAlreadyEmptyCount;
        if (shouldAbortDueToTooManyGroupListManagedGroupsBeingCleared(totalManagedGroupsWithMembersCount, totalManagedGroupsBeingClearedCount)) {
          statusOverall[0] = GrouperLoaderStatus.ERROR;
          hib3GrouploaderLogOverall.insertJobMessage("Can't clear out "
              + totalManagedGroupsBeingClearedCount + " groups (totalManagedGroupsWithMembersCount: "
              + totalManagedGroupsWithMembersCount + ")"
              + " unless loader.failsafe.groupList.managedGroups.use is false, or loader.failsafe.groupList.managedGroups.minManagedGroups"
              + " or loader.failsafe.groupList.managedGroups.maxPercentRemove properties are changed.");
          hib3GrouploaderLogOverall.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
          hib3GrouploaderLogOverall.store();
          return;
        } 
                  
        for (String groupNameEmpty : groupNamesManaged) {
          Group groupEmpty = groups.get(groupNameEmpty);
          if (groupEmpty == null) {
            continue;
          }
          
          long millisGetData = getDataTimes.get(groupNameEmpty);
          Set<Member> members = immediateMembers.get(groupNameEmpty);
          
          long groupStartedMillis = System.currentTimeMillis();
          int memberCount = 0;
          GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
          StringBuilder jobDescription = new StringBuilder();
          boolean didSomething = false;
          long millisSetData = 0;
          try {
            
            //first of all remove members
            millisSetData = System.currentTimeMillis();
            memberCount = members.size();
            for (Member member : members) {
              didSomething = true;
              groupEmpty.deleteMember(member);
            }
  
            //see if we are deleting group.  It must not be in the group query (if exists), and it 
            //must be configured to do this in the grouper loader properties
            if (!GrouperUtil.nonNull(groupNamesFromGroupQuery).contains(groupNameEmpty) && GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
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
      
      final long groupStartedMillis = System.currentTimeMillis();
      
      //if we are configured to, get the group names in one fell swoop
      // && GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
      // "loader.getAllGroupListMembershipsAtOnce", false);
      //2010/05/02 I think this didnt pan out as a performance gain...
      boolean getMembershipsAtOnce = false;
      
      
      //set of immediate memberships in the regsitry, key is group name, multikey by subjectId, and optionally sourceId
      final Map<String, Set<MultiKey>> membershipsInRegistry = new HashMap<String, Set<MultiKey>>();
      
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
          result = new ArrayList<Object[]>();

          List<String> groupNamesList = groupNames instanceof List ? (List)groupNames : new ArrayList<String>(groupNames);

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
      if (groupNamesFromGroupQuery != null) {
        groupNamesToSync.addAll(groupNamesFromGroupQuery);
      } else {
        groupNamesToSync.addAll(groupNames);
      }
      
      final boolean useThreads = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.use.groupThreads", true);

      //see when threads are done processing
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();

      //if there were thread problems, run those again
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();

      int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.groupThreadPoolSize", 20);

      for (final String groupName : groupNamesToSync) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug(groupNameOverall + ": syncing membership for " + groupName + " " + count + " out of " + groupNamesToSync.size() + " groups");
        }
        
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("syncLogicForOneGroup: " + groupName) {

          @Override
          public Void callLogic() {
            syncGroupLogicForOneGroup(grouperLoaderResultsetOverall,
                GrouperSession.staticGrouperSession(), andGroups, groupTypes, hib3GrouploaderLogOverall,
                statusOverall, groupNameToDisplayName, groupNameToDescription, privsToAdd,
                groupStartedMillis, membershipsInRegistry, groupName);
            return null;
          }
        };

        if (!useThreads) {
          grouperCallable.callLogic();
        } else {
          GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
          futures.add(future);
          
          GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);

        }

        count++;
      }

      //wait for the rest
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(groupNameOverall + ": done syncing membership");
      }

    } finally {
      hib3GrouploaderLogOverall.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
      statusOverall[0] = getFinalStatusAfterUnresolvables(statusOverall[0], 
          hib3GrouploaderLogOverall.getTotalCount(), hib3GrouploaderLogOverall.getUnresolvableSubjectCount());
    }
    
  }

  /**
   * @param grouperLoaderResultsetOverall
   * @param grouperSession
   * @param andGroups
   * @param groupTypes
   * @param hib3GrouploaderLogOverall
   * @param statusOverall
   * @param groupNameToDisplayName
   * @param groupNameToDescription
   * @param privsToAdd
   * @param groupStartedMillis
   * @param membershipsInRegistry
   * @param groupName
   */
  private static void syncGroupLogicForOneGroup(
      GrouperLoaderResultset grouperLoaderResultsetOverall,
      GrouperSession grouperSession, List<Group> andGroups, List<GroupType> groupTypes,
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall,
      GrouperLoaderStatus[] statusOverall, Map<String, String> groupNameToDisplayName,
      Map<String, String> groupNameToDescription,
      Map<String, Map<Privilege, List<Subject>>> privsToAdd, long groupStartedMillis,
      Map<String, Set<MultiKey>> membershipsInRegistry, String groupName) {
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
    
    synchronized (hib3GrouploaderLogOverall) {
      if (GrouperLoaderStatus.ERROR.equals(groupStatus) 
          || statusOverall[0] == GrouperLoaderStatus.SUCCESS) {
        statusOverall[0] = groupStatus;
      }
      
      //count all the stats
      hib3GrouploaderLogOverall.addDeleteCount(hib3GrouploaderLog.getDeleteCount());
      hib3GrouploaderLogOverall.addInsertCount(hib3GrouploaderLog.getInsertCount());
      hib3GrouploaderLogOverall.addUpdateCount(hib3GrouploaderLog.getUpdateCount());
      hib3GrouploaderLogOverall.addTotalCount(hib3GrouploaderLog.getTotalCount());
      hib3GrouploaderLogOverall.addUnresolvableSubjectCount(hib3GrouploaderLog.getUnresolvableSubjectCount());
      //store after each group to get progress
      hib3GrouploaderLogOverall.store();
      
    }
    
  }
    
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
   * maintenance builtinMessagingDaemon name
   */
  public static final String GROUPER_BUILTIN_MESSAGING_DAEMON = "MAINTENANCE__builtinMessagingDaemon";

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
   * change log consumer prefix
   */
  public static final String GROUPER_MESSAGING_LISTENER_PREFIX = GrouperLoaderType.MESSAGE_LISTENER.name() + "_";

  /**
   * other jobs prefix
   */
  public static final String GROUPER_OTHER_JOB_PREFIX = "OTHER_JOB_";
  
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
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param attributeAssign attribute assignment to get the attribute from (attribute assigned to assignment)
   * @param underlyingObjectName for errors, to clarify
   * @param attributeName attribute def name
   * @return the attribute value
   */
  String attributeValueValidateRequiredAttributeAssign(AttributeAssign attributeAssign, String underlyingObjectName, String attributeName) {
    
    String attributeValue = attributeAssign.getAttributeValueDelegate().retrieveValueString(attributeName);
    
    boolean hasValue = StringUtils.isNotBlank(attributeValue);
    boolean isRequired = this.attributeRequired(attributeName);
    boolean isOptional = this.attributeOptional(attributeName);
    
    //must have value if required
    if (!hasValue && isRequired) {
      throw new RuntimeException("Attribute '" + attributeName + "' is required, but is not set for loader type: " 
          + this.name() + ", object name: " + underlyingObjectName);
    }
    
    // must not have value if not required or optional
    if (hasValue && !isRequired && !isOptional) {
      LOG.error("Attribute '" + attributeName + "' is not required or optional, " +
          "but is set to '" + attributeValue + "' for loader type: " 
          + this.name() + ", object name: " + underlyingObjectName);
    }
    return attributeValue;
  }

  /**
   * make sure if an attribute is required that it exists (non blank).  throw exception if problem
   * @param attributeAssign attribute assignment to get the attribute from (attribute assigned to assignment)
   * @param underlyingObjectName for errors, to clarify
   * @param attributeName attribute def name
   * @return the attribute value
   */
  Integer attributeValueValidateRequiredAttributeAssignInteger(AttributeAssign attributeAssign, String underlyingObjectName, String attributeName) {
    String attributeValueString = StringUtils.trim(attributeValueValidateRequiredAttributeAssign(attributeAssign, underlyingObjectName, attributeName));
    return GrouperUtil.intObjectValue(attributeValueString, true);
  }
  
  /**
   * @param group
   * @param jobNames
   * @param logErrorsToDb
   */
  public static void validateAndScheduleSqlLoad(Group group, Set<String> jobNames, boolean logErrorsToDb) {
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
      
      if (jobNames != null) {
        jobNames.add(jobName);
      }
      
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
      if (logErrorsToDb) {
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
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * for all jobs in this loader type, schedule them with quartz
   */
  public static void scheduleLoads() {
    
    GrouperSession grouperSession = null;
    Set<String> jobNames = new HashSet<String>();
    
    try {
      grouperSession = GrouperSession.startRootSession();
  
      Set<Group> groups = retrieveGroups(grouperSession);
      
      for (Group group : groups) {
        validateAndScheduleSqlLoad(group, jobNames, true);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    // check to see if anything should be unscheduled.
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        
        String jobName = jobKey.getName();
        
        if ((jobName.startsWith(GrouperLoaderType.SQL_SIMPLE.name() + "__") ||
            jobName.startsWith(GrouperLoaderType.SQL_GROUP_LIST.name() + "__")) && !jobNames.contains(jobName)) {
          try {
            String triggerName = "triggerFor_" + jobName;
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
          } catch (Exception e) {
            String errorMessage = "Could not unschedule job: '" + jobName + "'";
            LOG.error(errorMessage, e);
            errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
            try {
              //lets enter a log entry so it shows up as error in the db
              Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
              hib3GrouploaderLog.setHost(GrouperUtil.hostname());
              hib3GrouploaderLog.setJobMessage(errorMessage);
              hib3GrouploaderLog.setJobName(jobName);
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
              hib3GrouploaderLog.store();
              
            } catch (Exception e2) {
              LOG.error("Problem logging to loader db log", e2);
            }
          }
        }
      }
    } catch (Exception e) {
      
      String errorMessage = "Could not query sql jobs to see if any should be unscheduled.";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
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
    
    //assume success
    GrouperLoaderStatus status = GrouperLoaderStatus.SUCCESS;
    if (!StringUtils.isBlank(hib3GrouploaderLog.getStatus()) 
        && !StringUtils.equals(GrouperLoaderStatus.STARTED.toString(), hib3GrouploaderLog.getStatus())
        && !StringUtils.equals(GrouperLoaderStatus.RUNNING.toString(), hib3GrouploaderLog.getStatus())) {
      status = GrouperLoaderStatus.valueOfIgnoreCase(hib3GrouploaderLog.getStatus(), true);
    }

    if (StringUtils.isBlank(hib3GrouploaderLog.getStatus()) || StringUtils.equals(GrouperLoaderStatus.STARTED.toString(), hib3GrouploaderLog.getStatus())) {
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(groupName + " start syncing membership");
    }
    
    hib3GrouploaderLog.setMillisGetData((int)(System.currentTimeMillis()-startTime));

    long startTimeLoadData = System.currentTimeMillis();
    
    int totalCount = 0;
    
    
    try {

      int numberOfRows = grouperLoaderResultset.numberOfRows();
      hib3GrouploaderLog.setTotalCount(numberOfRows);

      if (LOG.isDebugEnabled()) {
        LOG.debug(groupName + " syncing " + numberOfRows + " rows");
      }

      String groupExtension = StringUtils.isBlank(groupDisplayNameForInsert) ? GrouperUtil.extensionFromName(groupName) : 
        GrouperUtil.extensionFromName(groupDisplayNameForInsert);
      
      //https://bugs.internet2.edu/jira/browse/GRP-1091
      String[] displayNameChangesAllowedUnder = GrouperUtil.splitTrim(GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.allowStemDisplayNameChangesUnderStems", ""), ",");
      if (!ArrayUtils.isEmpty(displayNameChangesAllowedUnder) && !StringUtils.isBlank(groupDisplayNameForInsert) 
            && groupName.contains(":") && groupDisplayNameForInsert.contains(":")) {
        String[] stems = groupName.split(":");
        String[] displayNamesStems = groupDisplayNameForInsert.split(":");
        if (stems.length == displayNamesStems.length) {
          String stemName = "";
          String displayName = "";
          for(int i=0; i<stems.length; i++) {
            stemName = stemName.equals("") ? stems[i]: (stemName+":"+stems[i]);
            displayName = displayName.equals("") ? displayNamesStems[i]: (displayName+":"+displayNamesStems[i]);
            Stem stem = StemFinder.findByName(grouperSession, stemName, false);
            if (stem != null && !stem.getDisplayName().equals(displayName) && isChangeAllowed(displayNameChangesAllowedUnder, stemName)) {
              StemSave stemSave = new StemSave(grouperSession)
              .assignUuid(stem.getId())
              .assignName(stem.getName())
              .assignSaveMode(SaveMode.UPDATE)
              .assignDisplayName(displayName)
              .assignDisplayExtension(displayNamesStems[i]);
              stem = stemSave.save();
            }
          }
        }
      }
      
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
                    if (GrouperLoader.isDryRun()) {
                      //no sure if true or false
                      added = true;
                      GrouperLoader.dryRunWriteLine("Group: " + groupForPriv.getName() + " assign priv " + privilege.getName());
                    } else {
                      
                      added = groupForPriv.grantPriv(subject, privilege, false);
                    }
                  }
                  if (added != null && added) {
                    hib3GrouploaderLog.addInsertCount(1);
                  }
                } finally {
                  if (!skipPriv && LOG.isDebugEnabled()) {
                    String logMessage = "Granting privilege " + privilege + " to group: " + groupForPriv.getName() + " to subject: "
                          + GrouperUtil.subjectToString(subject) + " already existed? " + (added == null ? null : !added);

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
        
        if (GrouperLoaderConfig.getPropertyBoolean("loader.useMemberObjectsInInitalQuery", false)) {

          Set<Member> members = group[0].getImmediateMembers();
          for (Member member : GrouperUtil.nonNull(members)) {
            currentMembers.add(new LoaderMemberWrapper(member));
          }
          
        } else {

          //TODO put this in the DAO
          StringBuilder sql = new StringBuilder("select m.subjectIdDb, m.subjectSourceIdDb "
          		+ " from Member m, MembershipEntry ms "
          		+ " where ms.ownerGroupId = :ownerGroupId and ms.memberUuid = m.uuid "
              + " and ms.type = 'immediate' and ms.enabledDb = 'T' "
              + " and ms.fieldId = '" + Group.getDefaultList().getUuid() + "'");

          Set<Object[]> results = HibernateSession.byHqlStatic().createQuery(sql.toString())
            .setString("ownerGroupId", group[0].getId()).listSet(Object[].class);
          
          for (Object[] row : GrouperUtil.nonNull(results)) {
            String subjectId = (String)row[0];
            String sourceId = (String)row[1];
            currentMembers.add(new LoaderMemberWrapper(subjectId, sourceId));
          }
          
        }
        
      }

      int originalGroupSize = currentMembers.size();
      
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
          if (GrouperUtil.nonNull(andGroups).size() > 0) {
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
      
      grouperLoaderResultset.bulkLookupSubjects();
      
      count = 1;
      for (int i=0;i<numberOfRows;i++) {
        
        Row row = grouperLoaderResultset.retrieveRow(i);
        Subject subject = row.getSubject(groupName);
        if (subject != null) {
          //make sure it is not in the restricted list
          boolean andGroupsDoesntHaveSubject = false;
          for (Group andGroup : GrouperUtil.nonNull(andGroups)) {
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
      
      // If somebody has a case problem between loader data and subject source, we may be adding and removing the same subjects
      // if the source is case insensitive.
      // If so, delete them from both lists.
      Map<MultiKey, Subject> subjectsToAddMap = new LinkedHashMap<MultiKey, Subject>();
      for (Subject subject : subjectsToAdd) {
        subjectsToAddMap.put(new MultiKey(subject.getSourceId(), subject.getId()), subject);
      }

      Iterator<LoaderMemberWrapper> currentMembersIter = currentMembers.iterator();
      while (currentMembersIter.hasNext()) {
        LoaderMemberWrapper member = currentMembersIter.next();
        Subject subjectToAdd = subjectsToAddMap.get(new MultiKey(member.getSourceId(), member.getSubjectId()));
        
        if (subjectToAdd != null) {
          subjectsToAdd.remove(subjectToAdd);
          currentMembersIter.remove();
          LOG.warn("Subject " + member.getSubjectId() + " marked to be added and removed from group " + groupName + ".  Possible case issue between subject source and loader source.");
        }
      }
            
      
      //here are members to remove
      final List<LoaderMemberWrapper> membersToRemove = new ArrayList<LoaderMemberWrapper>(currentMembers);
   
      // GRP-1130
      if(shouldAbortDueToTooManyMembersRemoved(originalGroupSize, membersToRemove.size())) {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        hib3GrouploaderLog.insertJobMessage("Can't remove "
            + membersToRemove.size() + " members from " + theGroup.getName() + " (originalGroupSize: "
            + originalGroupSize + ") "
            + " unless loader.failsafe.use is false, or loader.failsafe.minGroupSize "
            + " or loader.failsafe.maxPercentRemove properties are changed.");
        hib3GrouploaderLog.setMillisLoadData((int)(System.currentTimeMillis()-startTimeLoadData));
        hib3GrouploaderLog.store();
        return;
      } 
      
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
      final boolean useTransactions = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.use.transactions", false);

      final boolean useThreads = !useTransactions && GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.use.membershipThreads", true);
      
      final int[] TOTAL_COUNT = new int[]{totalCount};
      final Hib3GrouperLoaderLog HIB3_GROUPER_LOADER_LOG = hib3GrouploaderLog;
      final GrouperTransactionType grouperTransactionType = useTransactions ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING 
          : GrouperTransactionType.NONE;
      GrouperTransaction.callbackGrouperTransaction(grouperTransactionType, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          try {
            //see when threads are done processing
            List<GrouperFuture> futures = new ArrayList<GrouperFuture>();

            //if there were thread problems, run those again
            List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();

            int membershipThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.membershipThreadPoolSize", 10);

            {
              final int numberOfRows = membersToRemove.size();
              final int[] count = new int[]{1};
              //first remove members

              for (final LoaderMemberWrapper member : membersToRemove) {
                GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("syncOneMemberDeleteMemberLogic: " + groupName + ", " + member.getSubjectId()) {

                  @Override
                  public Void callLogic() {
                    syncOneMemberDeleteMemberLogic(groupName, GrouperSession.staticGrouperSession(),
                        jobMessage, jobStatus, group, TOTAL_COUNT, HIB3_GROUPER_LOADER_LOG,
                        numberOfRows, count, member);
                    return null;
                  }

                };
                if (!useThreads || membershipThreadPoolSize == 1 || membershipThreadPoolSize == 0) {
                  grouperCallable.callLogic();
                } else {
                  GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
                  futures.add(future);
                  
                  GrouperFuture.waitForJob(futures, membershipThreadPoolSize, callablesWithProblems);
                }
                
              }
              
            }
            {
              final int numberOfRows = subjectsToAdd.size();
              final int[] count = new int[]{1};
              //then add new members

              for (final Subject subject : subjectsToAdd) {

                GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("syncOneMemberAddMemberLogic: " + groupName + ", " + subject.getId()) {
                  
                  public Void callLogic() {

                    syncOneMemberAddMemberLogic(groupName, GrouperSession.staticGrouperSession(), jobMessage,
                        jobStatus, group, TOTAL_COUNT, HIB3_GROUPER_LOADER_LOG, numberOfRows,
                        count, subject);

                    return null;
                  }
                };
                
                if (!useThreads) {
                  
                  grouperCallable.callLogic();

                } else {

                  GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
                  futures.add(future);

                  GrouperFuture.waitForJob(futures, membershipThreadPoolSize, callablesWithProblems);

                }

              }
            }
            
            //wait for the rest
            GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

            GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
            
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
      hib3GrouploaderLog.setStatus(getFinalStatusAfterUnresolvables(GrouperLoaderStatus.valueOf(hib3GrouploaderLog.getStatus()), 
          hib3GrouploaderLog.getTotalCount(), hib3GrouploaderLog.getUnresolvableSubjectCount()).name());
      try {
        hib3GrouploaderLog.store();
      } catch (Exception e) {
        //dont worry, just trying to store the log at end
      }
    }
  }
  
  private static boolean isChangeAllowed(String[] changeAllowedUnder, String stemNameToBeChanged) {
    for(String stemUnderWhichChangeAllowed: changeAllowedUnder) {
      String stemUnderWhichChangeAllowedNoSpace = stemUnderWhichChangeAllowed.trim();
      if ( (stemNameToBeChanged.length() - stemUnderWhichChangeAllowedNoSpace.length() > 2)  
          && (stemNameToBeChanged.startsWith(stemUnderWhichChangeAllowedNoSpace))  
          && (stemNameToBeChanged.charAt(stemUnderWhichChangeAllowedNoSpace.length()) == ':') ) { 
            return true;
      }
    }
    return false;
  }

  /**
   * see if too many members are being removed and if we should abort this job
   * @param originalGroupSize
   * @param membersToRemoveSize
   * @return true if should abort
   */
  private static boolean shouldAbortDueToTooManyMembersRemoved(final int originalGroupSize, final int membersToRemoveSize) {
    
    //maybe dont use this feature
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.failsafe.use", false)) {
      return false;
    }
    
    //must be a group of a minimum size, and not so many members removed
    return originalGroupSize >= GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.minGroupSize")
        && ((membersToRemoveSize * 100)/originalGroupSize)  > GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.maxPercentRemove");
  }
  
  /**
   * Group list fail safe.
   * See if too many groups managed by the loader via grouperLoaderGroupsLike used to have members but now all would be deleted.
   * @param originalManagedGroupsWithMembersCount
   * @param groupsBeingClearedCount
   * @return true if should abort
   */
  private static boolean shouldAbortDueToTooManyGroupListManagedGroupsBeingCleared(final int originalManagedGroupsWithMembersCount, final int groupsBeingClearedCount) {
    
    //maybe dont use this feature
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.failsafe.groupList.managedGroups.use", false)) {
      return false;
    }
    
    //must be a group of a minimum size, and not so many members removed
    return originalManagedGroupsWithMembersCount >= GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.groupList.managedGroups.minManagedGroups")
        && ((groupsBeingClearedCount * 100)/originalManagedGroupsWithMembersCount)  > GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.groupList.managedGroups.maxPercentRemove");
  }
  
  /**
   * If the current status is success and there are too many unresolvables, the status should be subject problems.
   * @param currentStatus
   * @param membershipCount
   * @param unresolvableCount
   * @return final status
   */
  private static GrouperLoaderStatus getFinalStatusAfterUnresolvables(GrouperLoaderStatus currentStatus, int membershipCount, int unresolvableCount) {
    if (GrouperLoaderStatus.SUCCESS != currentStatus) {
      return currentStatus;
    }
    
    if (membershipCount < GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.unresolvables.minGroupSize")) {
      return currentStatus;
    }
    
    if ((unresolvableCount * 100) / membershipCount > GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.unresolvables.maxPercentForSuccess")) {
      return GrouperLoaderStatus.SUBJECT_PROBLEMS;
    }
    
    return currentStatus;
  }
  
  /**
   * get an attribute value, or null, or a default if exists
   * @param attributeAssign
   * @param attributeDefName
   * @return the attribute value
   */
  public static String attributeValueOrDefaultOrNull(AttributeAssign attributeAssign, String attributeDefName) {
    
    String attributeValue = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        attributeDefName);
    return attributeValue;
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
          grouperLoaderDb, attributeLoaderActionSetQuery, hib3GrouploaderLog.getJobName(), 
          hib3GrouploaderLog);
      
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
          grouperLoaderDb, attributeLoaderActionQuery, hib3GrouploaderLog.getJobName(), 
          hib3GrouploaderLog);
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
          grouperLoaderDb, attributeLoaderAttrSetQuery, hib3GrouploaderLog.getJobName(), 
          hib3GrouploaderLog);
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
            grouperLoaderDb, attributeLoaderAttrQuery, hib3GrouploaderLog.getJobName(), 
            hib3GrouploaderLog);
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
    Set<String> jobNames = new HashSet<String>();

    try {
      grouperSession = GrouperSession.startRootSession();

      //lets see if there is configuration
      String attrRootStem = GrouperConfig.retrieveConfig().propertyValueString("grouper.attribute.rootStem");
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
          jobNames.add(jobName);
          
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
    
    // check to see if anything should be unscheduled.
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        
        String jobName = jobKey.getName();
        
        if ((jobName.startsWith(GrouperLoaderType.ATTR_SQL_SIMPLE.name() + "__")) && !jobNames.contains(jobName)) {
          try {
            String triggerName = "triggerFor_" + jobName;
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
          } catch (Exception e) {
            String errorMessage = "Could not unschedule job: '" + jobName + "'";
            LOG.error(errorMessage, e);
            errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
            try {
              //lets enter a log entry so it shows up as error in the db
              Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
              hib3GrouploaderLog.setHost(GrouperUtil.hostname());
              hib3GrouploaderLog.setJobMessage(errorMessage);
              hib3GrouploaderLog.setJobName(jobName);
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
              hib3GrouploaderLog.store();
              
            } catch (Exception e2) {
              LOG.error("Problem logging to loader db log", e2);
            }
          }
        }
      }
    } catch (Exception e) {
      
      String errorMessage = "Could not query attribute jobs to see if any should be unscheduled.";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }
  }
  
  /**
   * @param attributeAssign
   * @param jobNames
   * @param logErrorsToDb
   */
  public static void validateAndScheduleLdapLoad(AttributeAssign attributeAssign, Set<String> jobNames, boolean logErrorsToDb) {
    String jobName = null;
    String groupId = null;
    String grouperLoaderAndGroups = null;
    String grouperLoaderQuartzCron = null;
    Integer grouperLoaderPriority = null;
    String grouperLoaderType = null;
    String groupName = null;

    try {
      
      Group group = attributeAssign.getOwnerGroup();
      groupId = group.getId();
      
      //lets get all attribute values
      grouperLoaderType = attributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName());
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);

      groupName = group.getName();
      jobName = grouperLoaderTypeEnum.name() + "__" + groupName + "__" + group.getUuid();
      
      if (jobNames != null) {
        jobNames.add(jobName);
      }
      
      //get the real attributes
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapFilterName());
      grouperLoaderQuartzCron = grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, LoaderLdapUtils.grouperLoaderLdapQuartzCronName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName,
          LoaderLdapUtils.grouperLoaderLdapServerIdName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName());
      
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName,
          LoaderLdapUtils.grouperLoaderLdapAndGroupsName());
      grouperLoaderPriority = grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssignInteger(
          attributeAssign, groupName, LoaderLdapUtils.grouperLoaderLdapPriorityName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSearchDnName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSearchScopeName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSourceIdName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupAttributeName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapExtraAttributesName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupTypesName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapReadersName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapAdminsName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapUpdatersName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapViewersName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapOptinsName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapOptoutsName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName());
      grouperLoaderTypeEnum.attributeValueValidateRequiredAttributeAssign(attributeAssign, groupName, 
          LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName());
      
      scheduleJob(jobName, false, "CRON", grouperLoaderQuartzCron,
          null, grouperLoaderPriority);
      
    } catch (Exception e) {
      if (logErrorsToDb) {
        String errorMessage = null;
        
        //dont fail on all if any fail
        try {
          errorMessage = "Could not schedule group: '" + groupName + "', groupId: '" + groupId + "', attributeAssignId: " + attributeAssign.getId();
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
          hib3GrouploaderLog.setGroupUuid(groupId);
          hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(jobName);
          hib3GrouploaderLog.setAndGroupNames(grouperLoaderAndGroups);
          hib3GrouploaderLog.setJobSchedulePriority(grouperLoaderPriority);
          hib3GrouploaderLog.setJobScheduleQuartzCron(grouperLoaderQuartzCron);
          hib3GrouploaderLog.setJobScheduleType("CRON");
          hib3GrouploaderLog.setJobType(grouperLoaderType);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();
          
        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * for all ldap jobs in this loader type, schedule them with quartz
   */
  public static void scheduleLdapLoads() {
    
    GrouperSession grouperSession = null;
    Set<String> jobNames = new HashSet<String>();

    try {
      grouperSession = GrouperSession.startRootSession();

      //lets see if there is configuration
      String attrRootStem = GrouperConfig.retrieveConfig().propertyValueString("grouper.attribute.rootStem");
      if (StringUtils.isBlank(attrRootStem)) {
        return;
      }
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
      
      //see if attributeDef
      if (attributeDefName == null) {
        return;
      }
      
      //lets get the attribute assignments of load type
      Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findGroupAttributeAssignments(null, null, GrouperUtil.toSet(attributeDefName.getId()), null, null, true, false);

      for (AttributeAssign attributeAssign : attributeAssigns) {
        validateAndScheduleLdapLoad(attributeAssign, jobNames, true);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    // check to see if anything should be unscheduled.
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        
        String jobName = jobKey.getName();
        
        if ((jobName.startsWith(GrouperLoaderType.LDAP_GROUP_LIST.name() + "__") ||
            jobName.startsWith(GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES.name() + "__") ||
            jobName.startsWith(GrouperLoaderType.LDAP_SIMPLE + "__")) && !jobNames.contains(jobName)) {
          try {
            String triggerName = "triggerFor_" + jobName;
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
          } catch (Exception e) {
            String errorMessage = "Could not unschedule job: '" + jobName + "'";
            LOG.error(errorMessage, e);
            errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
            try {
              //lets enter a log entry so it shows up as error in the db
              Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
              hib3GrouploaderLog.setHost(GrouperUtil.hostname());
              hib3GrouploaderLog.setJobMessage(errorMessage);
              hib3GrouploaderLog.setJobName(jobName);
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
              hib3GrouploaderLog.store();
              
            } catch (Exception e2) {
              LOG.error("Problem logging to loader db log", e2);
            }
          }
        }
      }
    } catch (Exception e) {
      
      String errorMessage = "Could not query ldap jobs to see if any should be unscheduled.";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
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

    JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
      .withIdentity(jobName)
      .build();
    
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
    String triggerName = "triggerFor_" + jobName;
    
    GrouperLoaderScheduleType grouperLoaderScheduleTypeEnum = GrouperLoaderScheduleType
      .valueOfIgnoreCase(grouperLoaderScheduleType, true);
    
    Trigger trigger = grouperLoaderScheduleTypeEnum.createTrigger(
        triggerName,
        grouperLoaderPriority != null ? grouperLoaderPriority : Trigger.DEFAULT_PRIORITY,
        grouperLoaderQuartzCron, 
        grouperLoaderIntervalSeconds);
    
    if (LOG.isDebugEnabled() && trigger instanceof SimpleTrigger) {
      LOG.debug("Starting job " + jobName + " at " + ((SimpleTrigger)trigger).getStartTime());
    }
    
    if (unschedule) {
      scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
    }
    //scheduler.unscheduleJob()
    GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);

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

  /**
   * @param groupName
   * @param grouperSession
   * @param jobMessage
   * @param jobStatus
   * @param group
   * @param TOTAL_COUNT
   * @param HIB3_GROUPER_LOADER_LOG
   * @param numberOfRows
   * @param count
   * @param member
   */
  private static void syncOneMemberDeleteMemberLogic(final String groupName,
      final GrouperSession grouperSession, final StringBuilder jobMessage,
      final String[] jobStatus, final Group[] group, final int[] TOTAL_COUNT,
      final Hib3GrouperLoaderLog HIB3_GROUPER_LOADER_LOG, final int numberOfRows, final int[] count,
      final LoaderMemberWrapper member) {
    try {
      
      boolean alreadyDeleted = false;
      
      Subject theSubject = member.findOrGetSubject();
      if (GrouperLoader.isDryRun()) {
        alreadyDeleted = !group[0].hasMember(theSubject);
        synchronized (HIB3_GROUPER_LOADER_LOG) {
          GrouperLoader.dryRunWriteLine("Group: " + groupName + " delete " + GrouperUtil.subjectToString(theSubject));
        }
      } else {
        //go from subject since large lists might be removed from cache
        alreadyDeleted = !group[0].deleteMember(theSubject, false);
        LOG.debug("Group: " + groupName + " delete " + GrouperUtil.subjectToString(theSubject) + ", alreadyDeleted? " + alreadyDeleted);
      }
       
      if (LOG.isInfoEnabled() && (count[0] != 0 && count[0] % 200 == 0)) {
        LOG.info(groupName + " removing: " + count + " of " + numberOfRows + " members");
      }

    } catch (RuntimeException e) {
      GrouperUtil.injectInException(e, "Problem deleting member: " 
          + member + ", ");
      throw e;
    }
    synchronized (HIB3_GROUPER_LOADER_LOG) {
      count[0]++;
    
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
  }

  /**
   * @param groupName
   * @param grouperSession
   * @param jobMessage
   * @param jobStatus
   * @param group
   * @param TOTAL_COUNT
   * @param HIB3_GROUPER_LOADER_LOG
   * @param numberOfRows
   * @param count
   * @param subject
   */
  private static void syncOneMemberAddMemberLogic(final String groupName,
      final GrouperSession grouperSession, final StringBuilder jobMessage,
      final String[] jobStatus, final Group[] group, final int[] TOTAL_COUNT,
      final Hib3GrouperLoaderLog HIB3_GROUPER_LOADER_LOG, final int numberOfRows,
      final int[] count, Subject subject) {
    try {
      boolean alreadyAdded = false;
      
      if (GrouperLoader.isDryRun()) {
        alreadyAdded = !group[0].hasMember(subject);
        GrouperLoader.dryRunWriteLine("Group: " + groupName + " add " + GrouperUtil.subjectToString(subject));
      } else {
        alreadyAdded = !group[0].addMember(subject, false);
        LOG.debug("Group: " + groupName + " add " + GrouperUtil.subjectToString(subject) + ", alreadyAdded: " + alreadyAdded);
      }
      
      if (LOG.isInfoEnabled() && (count[0] != 0 && count[0] % 200 == 0)) {
        LOG.info(groupName + " adding: " + count + " of " + numberOfRows + " subjects"
            + (alreadyAdded ? ", [note: was already added... weird]" : ""));
      }
    } catch (RuntimeException e) {
      GrouperUtil.injectInException(e, "Problem with " 
          + GrouperUtil.subjectToString(subject) + ", ");
      throw e;
    }
    count[0]++;
    
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

}
