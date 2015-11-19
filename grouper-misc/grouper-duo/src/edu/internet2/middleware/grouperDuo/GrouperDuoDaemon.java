/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderScheduleType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDuoDaemon {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoDaemon.class);

  /**
  * scheduler
  * @return scheduler
  */
  private static Scheduler scheduler() {
    try {
      Scheduler scheduler = schedulerFactory().getScheduler();
      return scheduler;
    } catch (SchedulerException se) {
      throw new RuntimeException(se);
    }
  }

  /**
  * scheduler factory singleton
  */
  private static SchedulerFactory schedulerFactory = null;

  /**
  * lazy load (and start the scheduler) the scheduler factory
  * @return the scheduler factory
  */
  private static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }

  /** make sure we only schedule jobs once */
  private static boolean scheduledJobs = false;
  
  /**
   * schedule jobs once
   */
  public static void scheduleJobsOnce() {
    if (scheduledJobs) {
      return;
    }
    synchronized (GrouperDuoDaemon.class) {
      if (scheduledJobs) {
        return;
      }
      try {
        
        boolean runDaemons = true;
        
        if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.dontRunDaemonsHere", false)) {
          
          LOG.warn("Daemons dont run here since this is set in grouper-loader.properties file overlay: " +
              "grouperDuo.dontRunDaemonsHere, hostname: " + GrouperUtil.hostname());

          runDaemons = false;
        }

        if (runDaemons) {
          
          String serverNamesString = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.daemonRunOnlyOnServerNames");
          
          if (!StringUtils.isBlank(serverNamesString)) {
            
            List<String> serverNamesList = GrouperUtil.splitTrimToList(serverNamesString, ",");
            runDaemons = false;
            for (String serverName : serverNamesList) {
              
              if (StringUtils.equalsIgnoreCase(serverName, GrouperUtil.hostname())) {
                
                runDaemons = true;
                LOG.warn("Daemons running since " + serverName + "  is in the list of allowed servernames from grouper-loader.properties file: " +
                    "grouperDuo.runOnlyOnServerNames: " + serverNamesString );
                break;
                
              }
              
            }
            
            if (!runDaemons) {

              LOG.warn("Daemons dont run here since " + GrouperUtil.hostname() + " is not in the list of allowed servernames " +
                  "from grouper-loader.properties config file: " +
                  "grouperDuo.runOnlyOnServerNames: " + serverNamesString );
              
            }
            
          }
          
          
        }
        
        if (runDaemons) {
          {
            String fullRefreshCron = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.daemonCron");
            scheduleDaemon(GrouperDuoFullRefresh.class, GrouperDuoFullRefresh.GROUPER_DUO_FULL_REFRESH, fullRefreshCron);
          }
        }
        
        scheduledJobs = true;
      } catch (Throwable t) {
        LOG.error("Error scheduling jobs once", t);
      }
    }
  }
  
  /**
   * generic schedule deamon method
   * @param jobClass 
   * @param cronString
   * @param jobName
   */
  private static void scheduleDaemon(Class<? extends Job> jobClass, String jobName, String cronString) {
    
    int priority = 5;
    
    try {
      Scheduler scheduler = scheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(jobClass)
          .withIdentity(jobName)
          .build();
          
      //schedule this job
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger("triggerChangeLog_" + jobName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + jobName + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(jobName);
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
        hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
        hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }
  }
}
