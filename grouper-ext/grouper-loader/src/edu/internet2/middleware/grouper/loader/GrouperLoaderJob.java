/*
 * @author mchyzer
 * $Id: GrouperLoaderJob.java,v 1.1 2008-06-01 21:27:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.loader.db.Hib3GrouploaderLog;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * class which will run a loader job
 * implements StatefulJob so multiple dont run at once
 */
public class GrouperLoaderJob implements Job, StatefulJob {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoaderJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = null;
    Hib3GrouploaderLog hib3GrouploaderLog = new Hib3GrouploaderLog();
    JobExecutionException jobExecutionException = null;
    long startTime = System.currentTimeMillis();
    
    try {
      
      jobName = context.getJobDetail().getName();
      hib3GrouploaderLog.setJobName(jobName);
      
      //get all the params that define the job
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      
      String grouperLoaderGroupName = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_GROUP_NAME);
      String grouperLoaderGroupUuid = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_GROUP_UUID);
      hib3GrouploaderLog.setGroupUuid(grouperLoaderGroupUuid);
      String grouperLoaderType = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_TYPE);
      String grouperLoaderDbName = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_DB_NAME);
      String grouperLoaderQuery = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_QUERY);
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
      
      String grouperLoaderScheduleType = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
      String grouperLoaderAndGroupNames = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_AND_GROUPS);
      
      List<Group> andGroups = new ArrayList<Group>();
      GrouperSession grouperSession = null;
      try {
        
        grouperSession = GrouperSession.start(
            SubjectFinder.findById("GrouperSystem")
        );
        //find the groups whose membership we "and" with the dynamic group
        if (!StringUtils.isBlank(grouperLoaderAndGroupNames)) {
          //there are groups to and with, get the list
          String[] groupNames = GrouperUtil.splitTrim(grouperLoaderAndGroupNames, ",");
          
          
          for (String groupName : groupNames) {
            Group group = GroupFinder.findByName(grouperSession, groupName);
            andGroups.add(group);
          }
        }
        
        Trigger trigger = context.getTrigger();
        
        String grouperLoaderQuartzCron = null;
        
        if (trigger instanceof CronTrigger) {
          grouperLoaderQuartzCron = ((CronTrigger)trigger).getCronExpression();
        }
        
        Integer grouperLoaderIntervalSeconds = null;
        
        if (trigger instanceof SimpleTrigger) {
          grouperLoaderIntervalSeconds = (int)(((SimpleTrigger)trigger).getRepeatInterval()/1000);
        }
        
        //log that we are starting a job
        hib3GrouploaderLog.setHost(GrouperLoaderUtils.hostname());
        hib3GrouploaderLog.setJobScheduleIntervalSeconds(grouperLoaderIntervalSeconds);
        hib3GrouploaderLog.setJobSchedulePriority(trigger.getPriority());
        hib3GrouploaderLog.setJobScheduleQuartzCron(grouperLoaderQuartzCron);
        hib3GrouploaderLog.setJobScheduleType(grouperLoaderScheduleType);
        hib3GrouploaderLog.setJobType(grouperLoaderType);
        hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        
        hib3GrouploaderLog.store();
        
        GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(grouperLoaderDbName);
        
        //based on type, run query from the db and sync members
        grouperLoaderTypeEnum.syncGroupMembership(grouperLoaderGroupName, grouperLoaderDb, 
            grouperLoaderQuery, hib3GrouploaderLog, startTime, grouperSession, andGroups);
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
    } catch (Throwable t) {
      LOG.error("Error on job: " + jobName, t);
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        
      jobExecutionException = new JobExecutionException(t);
      
      throw jobExecutionException;
    } finally {
      
      //store this safely
      try {
        
        long endTime = System.currentTimeMillis();
        hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
        hib3GrouploaderLog.setMillis((int)(endTime-startTime));
        
        hib3GrouploaderLog.store();
        
      } catch (Exception e) {
        LOG.error("Problem storing", e);
        //dont preempt an existing exception
        if (jobExecutionException!= null) {
          throw new JobExecutionException(e);
        }
      }
    }
  }
}
