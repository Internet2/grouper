/*
 * @author mchyzer
 * $Id: GrouperLoader.java,v 1.15 2009-11-02 03:50:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.PlatformFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.client.ClientConfig;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * main class to start the grouper loader
 */
public class GrouperLoader {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoader.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

    //printAllSupportDdlUtilsPlatforms();
    GrouperStartup.startup();
    
    //make sure properties file is there
    GrouperCheckConfig.checkResource("grouper-loader.properties");
    
    //make sure properties are there
    GrouperCheckConfig.checkConfigProperties("grouper-loader.properties", 
        "grouper-loader.example.properties");
    
    GrouperCheckConfig.checkGrouperLoaderConfigDbs();
    GrouperCheckConfig.checkGrouperLoaderConsumers();
    
    //this will find all schedulable groups, and schedule them
    GrouperLoaderType.scheduleLoads();
    
    GrouperLoaderType.scheduleAttributeLoads();
    
    GrouperLoaderType.scheduleLdapLoads();

    scheduleMaintenanceJobs();
    scheduleChangeLogJobs();
    //this will schedule ESB listener jobs if enabled
    scheduleEsbListenerJobs();
  }

  /**
   * print out all ddlutils platforms
   */
  public static void printAllSupportDdlUtilsPlatforms() {
    String[] platforms = PlatformFactory.getSupportedPlatforms();
    Arrays.sort(platforms);
    for (String platform : platforms) {
      System.out.print(platform + ", ");
    }
  }
  
  /**
   * group attribute name of type of the loader, must match one of the enums in GrouperLoaderType.
   * If there is a query, and it has "group_name" before "from", then defaults to SQL_GROUP_LIST
   * else defaults to SQL_SIMPLE
   */
  public static final String GROUPER_LOADER_TYPE = "grouperLoaderType";

  /**
   * groups to and with to restrict members (e.g. "and" with activeEmployees)
   */
  public static final String GROUPER_LOADER_AND_GROUPS = "grouperLoaderAndGroups";

  /**
   * If you want the group (if not used from anywhere) or members deleted when 
   * no longer in loader sql results, list the sql like name, e.g. stem1:stem2:%:%org
   */
  public static final String GROUPER_LOADER_GROUPS_LIKE = "grouperLoaderGroupsLike";

  /**
   * optional group information for a group list query: e.g. to specify the display name of the
   * group/stem when it is created
   */
  public static final String GROUPER_LOADER_GROUP_QUERY = "grouperLoaderGroupQuery";

  /**
   * types to add to loaded groups
   */
  public static final String GROUPER_LOADER_GROUP_TYPES = "grouperLoaderGroupTypes";

  /**
   * group attribute name of type of schedule, must match one of the enums in GrouperLoaderScheduleType.
   * defaults to START_TO_START_INTERVAL if grouperLoaderQuartzCron is blank, else defaults to
   * CRON
   */
  public static final String GROUPER_LOADER_SCHEDULE_TYPE = "grouperLoaderScheduleType";

  /**
   * group attribute name of query, must have the required columns for the grouperLoaderType
   */
  public static final String GROUPER_LOADER_QUERY = "grouperLoaderQuery";

  /**
   * group attribute name of quartz cron-like string to describe when the job should run
   */
  public static final String GROUPER_LOADER_QUARTZ_CRON = "grouperLoaderQuartzCron";

  /**
   * group attribute name of the interval in seconds for a schedule type like START_TO_START_INTERVAL.
   * defaults to 86400 (1 day)
   */
  public static final String GROUPER_LOADER_INTERVAL_SECONDS = "grouperLoaderIntervalSeconds";

  /**
   * group attribute name of priority of job, optional, if not there, will be 5.  More is better.
   * if the threadpool is full, then this priority will help the schedule pick which job should go next
   */
  public static final String GROUPER_LOADER_PRIORITY = "grouperLoaderPriority";

  /**
   * group attribute name of the db connection where this query comes from.
   * if the name is "grouper", then it will be the group db name.  defaults to "grouper" for sql type
   * loaders
   */
  public static final String GROUPER_LOADER_DB_NAME = "grouperLoaderDbName";
  
  /**
   * Type of loader, e.g. ATTR_SQL_SIMPLE
   */
  public static final String ATTRIBUTE_LOADER_TYPE = "attributeLoaderType";
  
  /**
   * DB name in grouper-loader.properties or default grouper db if blank
   */
  public static final String ATTRIBUTE_LOADER_DB_NAME = "attributeLoaderDbName";
  
  /**
   * Type of schedule.  Defaults to CRON if a cron schedule is entered, or START_TO_START_INTERVAL if an interval is entered
   */
  public static final String ATTRIBUTE_LOADER_SCHEDULE_TYPE = "attributeLoaderScheduleType";
  
  /**
   * If a CRON schedule type, this is the cron setting string from the quartz product to run a job daily, hourly, weekly, etc.  e.g. daily at 7am: 0 0 7 * * ?
   */
  public static final String ATTRIBUTE_LOADER_QUARTZ_CRON = "attributeLoaderQuartzCron";

  /**
   * If a START_TO_START_INTERVAL schedule type, this is the number of seconds between runs
   */
  public static final String ATTRIBUTE_LOADER_INTERVAL_SECONDS = "attributeLoaderIntervalSeconds";
  
  /**
   * Quartz has a fixed threadpool (max configured in the grouper-loader.properties), and when the max is reached, then jobs are prioritized by this integer.  The higher the better, and the default if not set is 5.
   */
  public static final String ATTRIBUTE_LOADER_PRIORITY = "attributeLoaderPriority";

  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   */
  public static final String ATTRIBUTE_LOADER_ATTRS_LIKE = "attributeLoaderAttrsLike";
  
  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   */
  public static final String ATTRIBUTE_LOADER_ATTR_QUERY = "attributeLoaderAttrQuery";
  
  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   */
  public static final String ATTRIBUTE_LOADER_ATTR_SET_QUERY = "attributeLoaderAttrSetQuery";
  
  /**
   * SQL query with at least the following column: action_name
   */
  public static final String ATTRIBUTE_LOADER_ACTION_QUERY = "attributeLoaderActionQuery";
  
  /**
   * SQL query with at least the following columns: if_has_action_name, then_has_action_name
   */
  public static final String ATTRIBUTE_LOADER_ACTION_SET_QUERY = "attributeLoaderActionSetQuery";

  /**
   * scheduler factory singleton
   */
  private static SchedulerFactory schedulerFactory = null;

  /**
   * lazy load (and start the scheduler) the scheduler factory
   * @return the scheduler factory
   */
  public static SchedulerFactory schedulerFactory() {
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
  
  /**
   * schedule maintenance jobs
   */
  public static void scheduleMaintenanceJobs() {

    scheduleLogCleanerJob();
    scheduleDailyReportJob();
    scheduleEnabledDisabledJob();
    scheduleRulesJob();
    scheduleGroupSyncJobs();
  }

  /**
   * schedule change log jobs
   */
  public static void scheduleChangeLogJobs() {
    scheduleChangeLogTempToChangeLogJob();
    scheduleChangeLogConsumers();
  }
  
  /**
   * schedule maintenance job for moving records from change log to change log temp
   */
  public static void scheduleChangeLogTempToChangeLogJob() {

    String cronString = null;

    //this is a medium priority job
    int priority = 5;

    //schedule the log delete job
    try {
      
      if (!GrouperLoaderConfig.getPropertyBoolean("changeLog.changeLogTempToChangeLog.enable", false)) {
        LOG.warn("grouper-loader.properties key: changeLog.changeLogTempToChangeLog.enable is not " +
          "filled in or false so the change log temp to change log daemon will not run");
        return;
      }
      
      cronString = GrouperLoaderConfig.getPropertyString("changeLog.changeLogTempToChangeLog.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        cronString = "50 * * * * ?";
        
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG, null, GrouperLoaderJob.class);

      //schedule this job
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerChangeLog_grouperChangeLogTempToChangeLog");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
        hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
        hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }

  }

  /**
   * schedule change log consumer jobs
   */
  public static void scheduleChangeLogConsumers() {

    //changeLog.consumer.ldappc.class = 
    //changeLog.consumer.ldappc.quartz.cron
    
    //make sure sequences are ok
    Map<String, String> consumerMap = GrouperCheckConfig.retrievePropertiesKeys("grouper-loader.properties", 
        GrouperCheckConfig.grouperLoaderConsumerPattern);
    
    int index = 0;
    
    while (consumerMap.size() > 0) {
      
      //get one
      String consumerKey = consumerMap.keySet().iterator().next();
      //get the database name
      Matcher matcher = GrouperCheckConfig.grouperLoaderConsumerPattern.matcher(consumerKey);
      matcher.matches();
      String consumerName = matcher.group(1);
      boolean missingOne = false;
      //now find all 4 required keys
      String classKey = "changeLog.consumer." + consumerName + ".class";
      if (!consumerMap.containsKey(classKey)) {
        String error = "cannot find grouper-loader.properties key: " + classKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String cronKey = "changeLog.consumer." + consumerName + ".quartzCron";

      //check the classname
      Class<?> theClass = null;
      String className = consumerMap.get(classKey);
      String cronString = consumerMap.get(cronKey);
      
      String jobName = GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + consumerName;
      
      //this is a medium priority job
      int priority = 5;

      try {
        if (missingOne) {
          throw new RuntimeException("Cant find config param" );
        }
        
        theClass = GrouperUtil.forName(className);
        if (!ChangeLogConsumerBase.class.isAssignableFrom(theClass)) {
          throw new RuntimeException("not a subclass of ChangeLogConsumerBase");
        }

        //default to every minute on the minute, though add a couple of seconds for each one
        if (StringUtils.isBlank(cronString)) {
          cronString = ((index * 2) % 60) + " * * * * ?";
        }
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail(jobName, null, GrouperLoaderJob.class);

        //schedule this job
        GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

        Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

        trigger.setName("triggerChangeLog_" + jobName);

        trigger.setPriority(priority);

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
          hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();
          
        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }

      }
      
      consumerMap.remove(classKey);
      consumerMap.remove(cronKey);
      index++;
    }
      

  }


  /**
   * schedule maintenance job
   */
  public static void scheduleDailyReportJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the job
    try {
      
      cronString = GrouperLoaderConfig.getPropertyString("daily.report.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: daily.report.quartz.cron is not " +
        		"filled in so the daily report will not run");
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_REPORT, null, GrouperLoaderJob.class);

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerMaintenance_grouperReport");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_REPORT + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_REPORT);
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

  /**
   * schedule rules job
   */
  public static void scheduleRulesJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the job
    try {

      if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
        LOG.warn("grouper.properties key: rules.enable is false " +
          "so the rules engine/daemon will not run");
        return;
      }

      cronString = GrouperLoaderConfig.getPropertyString("rules.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: rules.quartz.cron is not " +
            "filled in so the rules daemon will not run");
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_RULES, null, GrouperLoaderJob.class);

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerMaintenance_rules");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_RULES + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_RULES);
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


  
  /**
   * schedule enabled/disabled job
   */
  public static void scheduleEnabledDisabledJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the job
    try {
      
      cronString = GrouperLoaderConfig.getPropertyString("changeLog.enabledDisabled.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: changeLog.enabledDisabled.quartz.cron is not " +
            "filled in so the enabled/disabled daemon will not run");
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_ENABLED_DISABLED, null, GrouperLoaderJob.class);

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerMaintenance_enabledDisabled");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_ENABLED_DISABLED + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_ENABLED_DISABLED);
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
  
  /**
   * schedule external subject subj calc fields
   */
  public static void scheduleExternalSubjCalcFieldsJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the job
    try {
      
      cronString = GrouperLoaderConfig.getPropertyString("externalSubjects.calc.fields.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.info("grouper.properties key: externalSubjects.calc.fields.cron is not " +
            "filled in so the external subject calc fields daemon will not run");
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_EXTERNAL_SUBJ_CALC_FIELDS, null, GrouperLoaderJob.class);

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerMaintenance_externalSubjCalcFields");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_EXTERNAL_SUBJ_CALC_FIELDS + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_EXTERNAL_SUBJ_CALC_FIELDS);
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

  /**
   * schedule maintenance job
   */
  public static void scheduleLogCleanerJob() {
    
    //schedule daily anytime
    //6am daily: "0 0 6 * * ?"
    //every minute for testing: "0 * * * * ?"
    String cronString = "0 0 6 * * ?";
    
    //this is a low priority job
    int priority = 1;

    //schedule the log delete job
    try {

      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS, null, GrouperLoaderJob.class);
      
      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
      
      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);
      
      trigger.setName("triggerMaintenance_cleanLogs");
      
      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);

      
    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.MAINTENANCE_CLEAN_LOGS + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS);
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

  /**
   * 
   */
  public static void scheduleEsbListenerJobs() {

    int priority = 1;
    GregorianCalendar cal = new GregorianCalendar();
    cal.add(GregorianCalendar.SECOND, 5);
    Date runTime = cal.getTime();
    // String cronString = "15 55 13 * * ?";
    //cronString = cal.getTime().getSeconds() + " " + cal.getTime().getMinutes() + " " + cal.getTime().getHours() + " * * ?"; 
    //System.out.println(cronString);
    boolean runEsbHttpListener = GrouperLoaderConfig.getPropertyBoolean(
        "esb.listeners.http.enable", false);
    if (runEsbHttpListener) {
      LOG.info("Starting experimental HTTP(S) listener");
      try {
        String port = GrouperLoaderConfig.getPropertyString("esb.listeners.http.port",
            "8080");
        String bindAddress = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.bindaddress", "127.0.0.1");
        String authConfigFile = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.authConfigFile", "");
        String sslKeystore = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.ssl.keystore", "");
        String sslKeyPassword = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.ssl.keyPassword", "");
        String sslTrustStore = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.ssl.trustStore", "");
        String sslTrustPassword = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.ssl.trustPassword", "");
        String sslPassword = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.http.ssl.password", "");
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER,
            null,
              GrouperUtil
                  .forName("edu.internet2.middleware.grouper.esb.listener.EsbHttpServer"));

        //schedule this job to run in 5 seconds

        SimpleTrigger simpleTrigger = new SimpleTrigger(
            GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER + "_trigger", null, runTime);
        simpleTrigger.setPriority(priority);

        jobDetail.getJobDataMap().put("port", port);
        jobDetail.getJobDataMap().put("bindAddress", bindAddress);
        jobDetail.getJobDataMap().put("authConfigFile", authConfigFile);
        jobDetail.getJobDataMap().put("keystore", sslKeystore);
        jobDetail.getJobDataMap().put("keyPassword", sslKeyPassword);
        jobDetail.getJobDataMap().put("trustStore", sslTrustStore);
        jobDetail.getJobDataMap().put("trustPassword", sslTrustPassword);
        jobDetail.getJobDataMap().put("keystore", sslKeystore);
        jobDetail.getJobDataMap().put("password", sslPassword);

        scheduler.scheduleJob(jobDetail, simpleTrigger);

      } catch (Exception e) {
        String errorMessage = "Could not schedule job: '"
            + GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER + "'";
        LOG.error(errorMessage, e);
        errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
        try {
          //lets enter a log entry so it shows up as error in the db
          Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
          hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER);
          hib3GrouploaderLog.setJobSchedulePriority(priority);
          hib3GrouploaderLog.setJobScheduleQuartzCron("5 seconds from now");
          hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
          hib3GrouploaderLog.setJobType(GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();

        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }
      }
    } else {
      LOG.info("Not starting experimental HTTP(S) listener");
    }

    boolean runEsbHXmppListener = GrouperLoaderConfig.getPropertyBoolean(
        "esb.listeners.xmpp.enable", false);
    if (runEsbHXmppListener) {
      LOG.info("Starting experimental XMPP listener");
      try {

        String server = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.xmpp.server", "");
        if (server.equals("")) {
          LOG.warn("XMPP server must be configured in grouper-loader.properties");
        }
        String port = GrouperLoaderConfig.getPropertyString("esb.listeners.xmpp.port",
            "5222");
        String username = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.xmpp.username", "");
        if (username.equals("")) {
          LOG.warn("XMPP username must be configured in grouper-loader.properties");
        }
        String password = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.xmpp.password", "");
        if (password.equals("")) {
          LOG.warn("XMPP password must be configured in grouper-loader.properties");
        }
        String sendername = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.xmpp.sendername", "");
        String resource = GrouperLoaderConfig.getPropertyString(
            "esb.listeners.xmpp.resource", "GrouperListener");
        if (server.equals("")) {
          LOG.warn("XMPP sendername must be configured in grouper-loader.properties");
        }
        if (!(server.equals("")) & !(username.equals("")) && !(password.equals(""))
            && !(sendername.equals(""))) {
          Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

          //the name of the job must be unique, so use the group name since one job per group (at this point)
          JobDetail jobDetail = new JobDetail(
              GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER,
              null,
                GrouperUtil
                    .forName("edu.internet2.middleware.grouper.esb.listener.EsbXmppListener"));

          SimpleTrigger simpleTrigger = new SimpleTrigger(
              GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER + "_trigger", null, runTime);
          simpleTrigger.setPriority(priority);

          jobDetail.getJobDataMap().put("port", port);
          jobDetail.getJobDataMap().put("server", server);
          jobDetail.getJobDataMap().put("username", username);
          jobDetail.getJobDataMap().put("password", password);
          jobDetail.getJobDataMap().put("sendername", sendername);
          jobDetail.getJobDataMap().put("resource", resource);

          scheduler.scheduleJob(jobDetail, simpleTrigger);
        }

      } catch (Exception e) {
        String errorMessage = "Could not schedule job: '"
            + GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER + "'";
        LOG.error(errorMessage, e);
        errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
        try {
          //lets enter a log entry so it shows up as error in the db
          Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
          hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER);
          hib3GrouploaderLog.setJobSchedulePriority(priority);
          hib3GrouploaderLog.setJobScheduleQuartzCron("5 seconds from now");
          hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
          hib3GrouploaderLog.setJobType(GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();

        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }
      }
    } else {
      LOG.info("Not starting experimental XMPP listener");
    }
  }

  /**
   * @param group
   * @param grouperSession
   * @return status
   */
  public static String runJobOnceForGroup(GrouperSession grouperSession, Group group) {
    try {
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
  
      boolean isSqlLoader = group.hasType(GroupTypeFinder.find("grouperLoader", false));
      boolean isLdapLoader = false;
      
      String grouperLoaderTypeString = null;
        
      if (!isSqlLoader) {
        AttributeDefName grouperLoaderLdapTypeAttributeDefName = GrouperDAOFactory.getFactory()
          .getAttributeDefName().findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
        AttributeAssign attributeAssign = grouperLoaderLdapTypeAttributeDefName == null ? null : 
          group.getAttributeDelegate().retrieveAssignment(
            null, grouperLoaderLdapTypeAttributeDefName, false, false);
        if (attributeAssign != null) {
          grouperLoaderTypeString = attributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName());
          isLdapLoader = true;
        }
      } else {
        grouperLoaderTypeString = GrouperLoaderType.attributeValueOrDefaultOrNull(group, GROUPER_LOADER_TYPE);
        if (!StringUtils.isBlank(grouperLoaderTypeString)) {
          isSqlLoader = true;
        }
      }
      
      if (StringUtils.isBlank(grouperLoaderTypeString)) {
        
        throw new RuntimeException("Cant find grouper loader type of group: " + group.getName());
      }
      
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
      
      hib3GrouperLoaderLog.setJobName(grouperLoaderType.name() + "__" + group.getName() + "__" + group.getUuid());
      hib3GrouperLoaderLog.setJobType(grouperLoaderTypeString);
      
      if (isSqlLoader) {
        GrouperLoaderJob.runJob(hib3GrouperLoaderLog, group, grouperSession);
      }
      
      if (isLdapLoader) {
        GrouperLoaderJob.runJobLdap(hib3GrouperLoaderLog, group, grouperSession);
      }
      
      return "loader ran successfully, inserted " + hib3GrouperLoaderLog.getInsertCount()
        + " memberships, deleted " + hib3GrouperLoaderLog.getDeleteCount() + " memberships, total membership count: "
        + hib3GrouperLoaderLog.getTotalCount();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param grouperSession
   * @param jobName
   * @return status
   */
  public static String runOnceByJobName(GrouperSession grouperSession, String jobName) {
    try {
  
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.typeForThisName(jobName);
      if (grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE) || grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
      
        String grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
        Group group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid, true);
        return runJobOnceForGroup(grouperSession, group);
      } else if (grouperLoaderType.equals(GrouperLoaderType.ATTR_SQL_SIMPLE)) {
        int uuidIndexStart = jobName.lastIndexOf("__");
        
        String grouperLoaderAttributeDefUuid = jobName.substring(uuidIndexStart+2, jobName.length());
        AttributeDef attributeDef = AttributeDefFinder.findById(grouperLoaderAttributeDefUuid, true);
        return runJobOnceForAttributeDef(grouperSession, attributeDef);
        
      }
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      hib3GrouperLoaderLog.setJobName(jobName);
      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, (Group)null, grouperSession);
      
      return "loader ran successfully: " + hib3GrouperLoaderLog.getJobMessage();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @param attributeDef
   * @param grouperSession
   * @return status
   */
  public static Hib3GrouperLoaderLog _internal_runJobOnceForAttributeDef(GrouperSession grouperSession, AttributeDef attributeDef) {
    try {
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      
      if (!attributeDef.getAttributeDelegate().hasAttributeByName(GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoader")) {
        throw new RuntimeException("Cant find attributeLoader type of attributeDef: " + attributeDef.getName());
      }
      String grouperLoaderTypeString = attributeDef.getAttributeValueDelegate()
        .retrieveValueString(GrouperCheckConfig.attributeLoaderStemName() + ":attributeLoaderType");

      GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
      
      hib3GrouperLoaderLog.setJobName(grouperLoaderType.name() + "__" + attributeDef.getName() + "__" + attributeDef.getUuid());
      hib3GrouperLoaderLog.setJobType(grouperLoaderTypeString);
  
      GrouperLoaderJob.runJobAttrDef(hib3GrouperLoaderLog, attributeDef, grouperSession);
      
      return hib3GrouperLoaderLog;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  /**
   * @param attributeDef
   * @param grouperSession
   * @return status
   */
  public static String runJobOnceForAttributeDef(GrouperSession grouperSession, AttributeDef attributeDef) {

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = _internal_runJobOnceForAttributeDef(grouperSession, attributeDef);
    
    return "loader ran successfully, inserted " + hib3GrouperLoaderLog.getInsertCount()
      + " attrDefNames, deleted " + hib3GrouperLoaderLog.getDeleteCount() + " records, total record count: "
      + hib3GrouperLoaderLog.getTotalCount();
  }

  /**
   * schedule rules job
   */
  public static void scheduleGroupSyncJobs() {
  
    Map<String, ClientGroupConfigBean> clientGroupConfigBeanCache = ClientConfig.clientGroupConfigBeanCache();
    
    //loop through all of them configured
    for (String localGroupName : clientGroupConfigBeanCache.keySet()) {
      
      ClientGroupConfigBean clientGroupConfigBean = clientGroupConfigBeanCache.get(localGroupName);
      
      if (StringUtils.isBlank(clientGroupConfigBean.getLocalGroupName())) {
        LOG.error("Why is local group name blank? " + clientGroupConfigBean.getConfigId());
      }
      
      String jobName = GrouperLoaderType.GROUPER_GROUP_SYNC + "__" + clientGroupConfigBean.getLocalGroupName();
      
      //schedule the job
      try {
      
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    
        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail(jobName, null, GrouperLoaderJob.class);
    
        //schedule this job daily at 6am
        GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
    
        Trigger trigger = grouperLoaderScheduleType.createTrigger(clientGroupConfigBean.getCron(), null);
    
        trigger.setName("trigger_" + jobName);
    
        scheduler.scheduleJob(jobDetail, trigger);
    
    
      } catch (Exception e) {
        
        //log and continue so we can schedule the other ones...
        
        String errorMessage = "Could not schedule job: " + jobName;
        LOG.error(errorMessage, e);
        errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
        try {
          //lets enter a log entry so it shows up as error in the db
          Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
          hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_GROUP_SYNC);
          hib3GrouploaderLog.setJobScheduleQuartzCron(clientGroupConfigBean.getCron());
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
  
}
