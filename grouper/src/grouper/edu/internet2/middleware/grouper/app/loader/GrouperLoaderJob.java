/*
 * @author mchyzer
 * $Id: GrouperLoaderJob.java,v 1.7.2.1 2009-04-28 19:37:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hooks.LoaderHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * class which will run a loader job
 * implements StatefulJob so multiple dont run at once
 */
public class GrouperLoaderJob implements Job, StatefulJob {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long startTime = System.currentTimeMillis();

    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    
    Group group = null;
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      String jobName = context.getJobDetail().getName();
  
      hib3GrouploaderLog.setJobName(jobName);
      
      String grouperLoaderGroupUuid = null;
      String grouperLoaderQuartzCronFromGroup = null;
      String grouperLoaderTypeFromGroup = null;
      String grouperLoaderScheduleTypeFromGroup = null;
      Integer grouperLoaderPriorityFromGroup = null;
      Integer grouperLoaderIntervalSecondsFromGroup = null;
      
      //job name is GrouperLoaderType__groupname__uuid
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.typeForThisName(jobName);
      if (grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST) || 
          grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
        
        if (uuidIndexStart >= 0) {
          grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
          
          group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid);
          grouperLoaderQuartzCronFromGroup = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
          grouperLoaderScheduleTypeFromGroup = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
          grouperLoaderTypeFromGroup = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_TYPE);
          grouperLoaderIntervalSecondsFromGroup = GrouperUtil.intObjectValue(
              GrouperLoaderType.attributeValueOrDefaultOrNull(group,
              GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS), true);
          grouperLoaderPriorityFromGroup = GrouperUtil.intObjectValue(
              GrouperLoaderType.attributeValueOrDefaultOrNull(group,
              GrouperLoader.GROUPER_LOADER_PRIORITY), true);
          
          //lets reset the job name in case the name has changed
          jobName = grouperLoaderTypeFromGroup + "__" + group.getName() + "__" + group.getUuid();
          hib3GrouploaderLog.setJobName(jobName);
          
        }
      }
      
      //switch the job type?
      if (!StringUtils.isBlank(grouperLoaderTypeFromGroup)) {
        GrouperLoaderType grouperLoaderTypeFromGroupEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeFromGroup, true);
        if (!grouperLoaderTypeFromGroupEnum.equals(grouperLoaderType)) {
          LOG.debug("Grouper loader type has changed to " + grouperLoaderTypeFromGroupEnum
              + " from " + grouperLoaderType + ", for job: " + jobName);
          grouperLoaderType = grouperLoaderTypeFromGroupEnum;
        }
      }
      
      hib3GrouploaderLog.setGroupUuid(grouperLoaderGroupUuid);

      Trigger trigger = context.getTrigger();
      String grouperLoaderQuartzCron = null;
      String grouperLoaderScheduleType = null;
      if (trigger instanceof CronTrigger) {
        grouperLoaderQuartzCron = ((CronTrigger)trigger).getCronExpression();
        grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON.name();
      }
      Integer grouperLoaderIntervalSeconds = null;
      if (trigger instanceof SimpleTrigger) {
        grouperLoaderIntervalSeconds = (int)(((SimpleTrigger)trigger).getRepeatInterval()/1000);
        grouperLoaderScheduleType = GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
      }
      
      boolean scheduleChange = false;
      
      if (!StringUtils.isBlank(grouperLoaderScheduleTypeFromGroup)
          && !StringUtils.equals(grouperLoaderScheduleTypeFromGroup, grouperLoaderScheduleType)) {
        scheduleChange = true;
      }
      if (!StringUtils.isBlank(grouperLoaderQuartzCronFromGroup)
          && !StringUtils.equals(grouperLoaderQuartzCronFromGroup, grouperLoaderQuartzCron)) {
        scheduleChange = true;
      }
      if (grouperLoaderIntervalSecondsFromGroup != null
          && !ObjectUtils.equals(grouperLoaderIntervalSecondsFromGroup, grouperLoaderIntervalSeconds)) {
        scheduleChange = true;
      }
      if (grouperLoaderPriorityFromGroup != null && 
          !ObjectUtils.equals(grouperLoaderPriorityFromGroup, trigger.getPriority())) {
        scheduleChange = true;
      }
      
      //see if the runtime settings have changed
      if (scheduleChange) {
        
        GrouperLoaderScheduleType grouperLoaderScheduleTypeEnumFromGroup = GrouperLoaderScheduleType
          .valueOfIgnoreCase(grouperLoaderScheduleTypeFromGroup, true);
        
        if (grouperLoaderScheduleTypeEnumFromGroup.equals(GrouperLoaderScheduleType.START_TO_START_INTERVAL)) {
          if (grouperLoaderIntervalSecondsFromGroup == null) {
            grouperLoaderIntervalSecondsFromGroup = 60*60*24;
          }
        }
        if (grouperLoaderScheduleTypeEnumFromGroup.equals(GrouperLoaderScheduleType.CRON)) {
          if (StringUtils.isBlank(grouperLoaderQuartzCronFromGroup)) {
            throw new RuntimeException("Cron cant be blank if cron schedule: " + jobName);
          }
        }
      }
      
      if (grouperLoaderType != null) {
        hib3GrouploaderLog.setJobType(grouperLoaderType.name());
      }
      if (!StringUtils.isBlank(grouperLoaderScheduleTypeFromGroup)) {
        hib3GrouploaderLog.setJobScheduleType(grouperLoaderScheduleTypeFromGroup);
      }
      hib3GrouploaderLog.setJobScheduleIntervalSeconds(grouperLoaderIntervalSecondsFromGroup);
      
      if (grouperLoaderPriorityFromGroup != null || trigger != null) {
        
        hib3GrouploaderLog.setJobSchedulePriority(grouperLoaderPriorityFromGroup != null ? grouperLoaderPriorityFromGroup 
            : trigger.getPriority());
      }

      hib3GrouploaderLog.setJobScheduleQuartzCron(!StringUtils.isBlank(grouperLoaderQuartzCronFromGroup) ? 
          grouperLoaderQuartzCronFromGroup : grouperLoaderQuartzCron );
      
      if (scheduleChange) {
        LOG.warn("Detected a grouper loader schedule change in job: " + jobName + ", to: " 
            + grouperLoaderScheduleTypeFromGroup + ", cron: " + grouperLoaderQuartzCronFromGroup
            + ", interval: " + grouperLoaderIntervalSecondsFromGroup);
        
        GrouperLoaderType.scheduleJob(jobName, true, grouperLoaderScheduleTypeFromGroup, grouperLoaderQuartzCronFromGroup,
            grouperLoaderIntervalSecondsFromGroup, grouperLoaderPriorityFromGroup);
        
      }
      
      runJob(hib3GrouploaderLog, group, grouperSession);
    } catch (Exception e) {
      LOG.error("Error running up job", e);
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      throw jobExecutionException;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * run a job (either from quartz or outside)
   * @param hib3GrouploaderLog will get information, most importantly the job name
   * @param jobGroup if a group job, this is the group object
   * @param grouperSession 
   */
  public static void runJob(Hib3GrouperLoaderLog hib3GrouploaderLog, Group jobGroup, GrouperSession grouperSession) {
    long startTime = System.currentTimeMillis();
    boolean throwExceptionsInFinally = true;
    String jobName = null;
    try {
      
      jobName = hib3GrouploaderLog.getJobName();
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.typeForThisName(jobName);
      
      List<Group> andGroups = new ArrayList<Group>();

      //find the groups whose membership we "and" with the dynamic group
      String grouperLoaderAndGroupNames = hib3GrouploaderLog.getAndGroupNames();
      if (!StringUtils.isBlank(grouperLoaderAndGroupNames)) {
        
        hib3GrouploaderLog.setAndGroupNames(grouperLoaderAndGroupNames);
        
        //there are groups to and with, get the list
        String[] groupNames = GrouperUtil.splitTrim(grouperLoaderAndGroupNames, ",");
        
        
        for (String groupName : groupNames) {
          Group group = GroupFinder.findByName(grouperSession, groupName);
          andGroups.add(group);
        }
      }
      
      //log that we are starting a job
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      
      hib3GrouploaderLog.store();
      
      String grouperLoaderDbName = "grouper";
      
      String grouperLoaderQuery = null;
      String groupName = null;
      
      String groupTypesString = null;
      String groupLikeString = null;
      String groupQuery = null;
      
      if (jobGroup != null) {
        grouperLoaderDbName = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DB_NAME);
        grouperLoaderQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUERY);
        groupTypesString = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_TYPES);
        groupLikeString = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE);
        groupQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_QUERY);
        groupName = jobGroup.getName();
        hib3GrouploaderLog.setGroupUuid(jobGroup.getUuid());
      }
      
      GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(grouperLoaderDbName);
      
      List<GroupType> groupTypes = null;
      if (!StringUtils.isBlank(groupTypesString)) {
        String[] groupTypeArray = GrouperUtil.splitTrim(groupTypesString, ",");
        groupTypes = new ArrayList<GroupType>();
        for (String groupType : groupTypeArray) {
          //this better find the type!
          groupTypes.add(GroupTypeFinder.find(groupType));
        }
      }
      
      LoaderJobBean loaderJobBean = new LoaderJobBean(grouperLoaderTypeEnum, groupName, grouperLoaderDb, grouperLoaderQuery, 
          hib3GrouploaderLog, grouperSession, andGroups, groupTypes, groupLikeString, groupQuery, startTime);
      
      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_PRE_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_PRE_RUN);

      //based on type, run query from the db and sync members
      grouperLoaderTypeEnum.syncGroupMembership(loaderJobBean);

      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_POST_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_POST_RUN);

    } catch (Exception t) {
      LOG.error("Error on job: " + jobName, t);
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(t));
      throwExceptionsInFinally = false;
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t.getMessage(), t);
    } finally {
      
      storeLogInDb(hib3GrouploaderLog, throwExceptionsInFinally, startTime);
    }
    
  }

  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  private static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
      boolean throwException, long startTime) {
    //store this safely
    try {
      
      long endTime = System.currentTimeMillis();
      hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
      hib3GrouploaderLog.setMillis((int)(endTime-startTime));
      
      hib3GrouploaderLog.store();
      
    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }
  
}
