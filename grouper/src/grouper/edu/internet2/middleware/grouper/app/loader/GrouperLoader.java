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
 * $Id: GrouperLoader.java,v 1.15 2009-11-02 03:50:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.PlatformFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

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
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.client.ClientConfig;
import edu.internet2.middleware.grouper.client.ClientConfig.ClientGroupConfigBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;



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
//    GrouperCheckConfig.checkConfigProperties("grouper-loader.properties", 
//        "grouper-loader.example.properties");
    
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
    
    schedulePspFullSyncJob();
    schedulePspFullSyncRunAtStartupJob();
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
      
      Properties props = new Properties();
      for (String key : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
        if (key.startsWith("org.quartz.")) {
          String value = GrouperLoaderConfig.retrieveConfig().propertyValueString(key);
          if (value == null) {
            value = "";
          }
          props.put(key, value);
        }
      }

      String url = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url");
      
      if (GrouperUtil.isEmpty((String)props.get("org.quartz.dataSource.myDS.driver"))) {
        String driver = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.driver_class");
        driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(url, driver);
        props.put("org.quartz.dataSource.myDS.driver", driver);
      }
      
      if (GrouperUtil.isEmpty((String)props.get("org.quartz.jobStore.driverDelegateClass"))) {
        String driver = GrouperDdlUtils.convertUrlToQuartzDriverDelegateClassIfNeeded(url, null);
        props.put("org.quartz.jobStore.driverDelegateClass", driver);
      }
      
      if (GrouperUtil.isEmpty((String)props.get("org.quartz.dataSource.myDS.URL"))) {
        props.put("org.quartz.dataSource.myDS.URL", url);
      }
      
      if (GrouperUtil.isEmpty((String)props.get("org.quartz.dataSource.myDS.user"))) {
        props.put("org.quartz.dataSource.myDS.user", GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.username"));
      }
      
      if (GrouperUtil.isEmpty((String)props.get("org.quartz.dataSource.myDS.password"))) {
        String pass = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.password");
        pass = Morph.decryptIfFile(pass);
        props.put("org.quartz.dataSource.myDS.password", pass);
      }
      
      try {
        schedulerFactory = new StdSchedulerFactory(props);
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
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      String triggerName = "triggerChangeLog_grouperChangeLogTempToChangeLog";
      
      if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.changeLogTempToChangeLog.enable", false)) {
        LOG.warn("grouper-loader.properties key: changeLog.changeLogTempToChangeLog.enable is not " +
          "filled in or false so the change log temp to change log daemon will not run");
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
        return;
      }
      
      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.changeLogTempToChangeLog.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        cronString = "50 * * * * ?";
        
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG)
        .build();

      //schedule this job
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);


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
    Map<String, String> consumerMap = GrouperLoaderConfig.retrieveConfig().propertiesMap( 
        GrouperCheckConfig.grouperLoaderConsumerPattern);
    
    Set<String> changeLogJobNames = new HashSet<String>();
    
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
      changeLogJobNames.add(jobName);
      
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
        //        if (StringUtils.isBlank(cronString) || StringUtils.equals("0 * * * * ?", cronString)) {
        //        dont change the crons that are explicitly set... only blank ones
        if (StringUtils.isBlank(cronString)) {
          cronString = ((index * 2) % 60) + " * * * * ?";
        }
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
          .withIdentity(jobName)
          .build();
        
        //schedule this job
        GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

        Trigger trigger = grouperLoaderScheduleType.createTrigger("triggerChangeLog_" + jobName, priority, cronString, null);

        scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);

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
    
    // check to see if anything should be unscheduled.
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        
        String jobName = jobKey.getName();
        
        if (jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX) && !changeLogJobNames.contains(jobName)) {
          try {
            String triggerName = "triggerChangeLog_" + jobName;
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
              hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
              hib3GrouploaderLog.store();
              
            } catch (Exception e2) {
              LOG.error("Problem logging to loader db log", e2);
            }
          }
        }
      }
    } catch (Exception e) {
      
      String errorMessage = "Could not query change log jobs to see if any should be unscheduled.";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
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
  public static void scheduleDailyReportJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the job
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      String triggerName = "triggerMaintenance_grouperReport";
      
      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("daily.report.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: daily.report.quartz.cron is not " +
        		"filled in so the daily report will not run");
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));

        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.GROUPER_REPORT)
        .build();

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);


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
      boolean unscheduleAndReturn = false;
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      String triggerName = "triggerMaintenance_rules";

      if (!GrouperConfig.retrieveConfig().propertyValueBoolean("rules.enable", true)) {
        LOG.warn("grouper.properties key: rules.enable is false " +
          "so the rules engine/daemon will not run");
        unscheduleAndReturn = true;
        return;
      }

      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("rules.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: rules.quartz.cron is not " +
            "filled in so the rules daemon will not run");
        unscheduleAndReturn = true;
        return;
      }
      
      if (unscheduleAndReturn) {
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.GROUPER_RULES)
        .build();

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);


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
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      String triggerName = "triggerMaintenance_enabledDisabled";
      
      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.enabledDisabled.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: changeLog.enabledDisabled.quartz.cron is not " +
            "filled in so the enabled/disabled daemon will not run");
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));

        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.GROUPER_ENABLED_DISABLED)
        .build();

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);


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
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      String triggerName = "triggerMaintenance_externalSubjCalcFields";

      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("externalSubjects.calc.fields.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.info("grouper.properties key: externalSubjects.calc.fields.cron is not " +
            "filled in so the external subject calc fields daemon will not run");
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));

        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.GROUPER_EXTERNAL_SUBJ_CALC_FIELDS)
        .build();

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);


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
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS)
        .build();
      
      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
      
      Trigger trigger = grouperLoaderScheduleType.createTrigger("triggerMaintenance_cleanLogs", priority, cronString, null);

      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);

      
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
    
    String triggerNameHttpListener = GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER + "_trigger";
    String triggerNameXmmpListener = GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER + "_trigger";

    // String cronString = "15 55 13 * * ?";
    //cronString = cal.getTime().getSeconds() + " " + cal.getTime().getMinutes() + " " + cal.getTime().getHours() + " * * ?"; 
    //System.out.println(cronString);
    boolean runEsbHttpListener = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "esb.listeners.http.enable", false);

    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      if (runEsbHttpListener) {
        LOG.info("Starting experimental HTTP(S) listener");
        String port = GrouperLoaderConfig.retrieveConfig().propertyValueString("esb.listeners.http.port",
            "8080");
        String bindAddress = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.bindaddress", "127.0.0.1");
        String authConfigFile = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.authConfigFile", "");
        String sslKeystore = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.ssl.keystore", "");
        String sslKeyPassword = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.ssl.keyPassword", "");
        String sslTrustStore = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.ssl.trustStore", "");
        String sslTrustPassword = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.ssl.trustPassword", "");
        String sslPassword = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.http.ssl.password", "");
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = JobBuilder.newJob(GrouperUtil.forName("EsbHttpServer"))
          .withIdentity(GrouperLoaderType.GROUPER_ESB_HTTP_LISTENER)
          .usingJobData("port", port)
          .usingJobData("bindAddress", bindAddress)
          .usingJobData("authConfigFile", authConfigFile)
          .usingJobData("keystore", sslKeystore)
          .usingJobData("keyPassword", sslKeyPassword)
          .usingJobData("trustStore", sslTrustStore)
          .usingJobData("trustPassword", sslTrustPassword)
          .usingJobData("keystore", sslKeystore)
          .usingJobData("password", sslPassword)
          .build();

        //schedule this job to run in 5 seconds
        Trigger trg = TriggerBuilder.newTrigger()
          .withIdentity(triggerNameHttpListener)
          .startAt(runTime)
          .withPriority(priority)
          .build();

        scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trg), true);
      } else {
        LOG.info("Not starting experimental HTTP(S) listener");
       
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerNameHttpListener));
      }
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

    boolean runEsbHXmppListener = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(
        "esb.listeners.xmpp.enable", false);
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      boolean unschedule = false;
      if (runEsbHXmppListener) {
        LOG.info("Starting experimental XMPP listener");

        String server = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.xmpp.server", "");
        if (server.equals("")) {
          LOG.warn("XMPP server must be configured in grouper-loader.properties");
        }
        String port = GrouperLoaderConfig.retrieveConfig().propertyValueString("esb.listeners.xmpp.port",
            "5222");
        String username = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.xmpp.username", "");
        if (username.equals("")) {
          LOG.warn("XMPP username must be configured in grouper-loader.properties");
        }
        String password = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.xmpp.password", "");
        if (password.equals("")) {
          LOG.warn("XMPP password must be configured in grouper-loader.properties");
        }
        String sendername = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.xmpp.sendername", "");
        String resource = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "esb.listeners.xmpp.resource", "GrouperListener");
        if (server.equals("")) {
          LOG.warn("XMPP sendername must be configured in grouper-loader.properties");
        }
        if (!(server.equals("")) & !(username.equals("")) && !(password.equals(""))
            && !(sendername.equals(""))) {

          //the name of the job must be unique, so use the group name since one job per group (at this point)
          JobDetail jobDetail = JobBuilder.newJob(GrouperUtil.forName("EsbXmppListener"))
            .withIdentity(GrouperLoaderType.GROUPER_ESB_XMMP_LISTENER)
            .usingJobData("port", port)
            .usingJobData("server", server)
            .usingJobData("username", username)
            .usingJobData("password", password)
            .usingJobData("sendername", sendername)
            .usingJobData("resource", resource)
            .build();
          
          Trigger trg = TriggerBuilder.newTrigger()
            .withIdentity(triggerNameXmmpListener)
            .startAt(runTime)
            .withPriority(priority)
            .build();

          scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trg), true);
        } else {
          unschedule = true;
        }
      } else {
        LOG.info("Not starting experimental XMPP listener");
        unschedule = true;
      }
      
      if (unschedule) {
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerNameXmmpListener));
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
      
      String status = "SUBJECT_PROBLEMS".equals(hib3GrouperLoaderLog.getStatus()) ? "with subject problems" :
        "successfully";
      
      return "loader " + (isDryRun() ? "dry " : "") + "ran " + status + ", " + (isDryRun() ? "would have " : "") + "inserted " + hib3GrouperLoaderLog.getInsertCount()
        + " memberships, " + (isDryRun() ? "would have " : "") + "deleted " + hib3GrouperLoaderLog.getDeleteCount() + " memberships, total membership count: "
        + hib3GrouperLoaderLog.getTotalCount() + ", unresolvable subjects: " + hib3GrouperLoaderLog.getUnresolvableSubjectCount();
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
    
    return "loader " + (isDryRun() ? "dry " : "") + "ran successfully, " + (isDryRun() ? "would have " : "") + "inserted " + hib3GrouperLoaderLog.getInsertCount()
      + " attrDefNames, " + (isDryRun() ? "would have " : "") + "deleted " + hib3GrouperLoaderLog.getDeleteCount() + " records, total record count: "
      + hib3GrouperLoaderLog.getTotalCount();
  }

  /**
   * schedule rules job
   */
  public static void scheduleGroupSyncJobs() {
  
    Map<String, ClientGroupConfigBean> clientGroupConfigBeanCache = ClientConfig.clientGroupConfigBeanCache();
    
    Set<String> groupSyncJobNames = new HashSet<String>();
    
    //loop through all of them configured
    for (String localGroupName : clientGroupConfigBeanCache.keySet()) {
      
      ClientGroupConfigBean clientGroupConfigBean = clientGroupConfigBeanCache.get(localGroupName);
      
      if (StringUtils.isBlank(clientGroupConfigBean.getLocalGroupName())) {
        LOG.error("Why is local group name blank? " + clientGroupConfigBean.getConfigId());
      }
      
      String jobName = GrouperLoaderType.GROUPER_GROUP_SYNC + "__" + clientGroupConfigBean.getLocalGroupName();
      groupSyncJobNames.add(jobName);

      //schedule the job
      try {
      
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    
        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
          .withIdentity(jobName)
          .build();
    
        //schedule this job daily at 6am
        GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
    
        Trigger trigger = grouperLoaderScheduleType.createTrigger("trigger_" + jobName, Trigger.DEFAULT_PRIORITY, clientGroupConfigBean.getCron(), null);
        
        scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);
    
    
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
    
    // check to see if anything should be unscheduled.
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        
        String jobName = jobKey.getName();
        
        if (jobName.startsWith(GrouperLoaderType.GROUPER_GROUP_SYNC + "__") && !groupSyncJobNames.contains(jobName)) {
          try {
            String triggerName = "trigger_" + jobName;
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
              hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());
              hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
              hib3GrouploaderLog.store();
              
            } catch (Exception e2) {
              LOG.error("Problem logging to loader db log", e2);
            }
          }
        }
      }
    } catch (Exception e) {
      
      String errorMessage = "Could not query group sync jobs to see if any should be unscheduled.";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }
  }

  /**
   * schedule psp full sync job
   */
  public static void schedulePspFullSyncJob() {
  
    String cronString = null;
    
    //this is a medium priority job
    int priority = 5;
  
    //schedule the job
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      cronString = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.psp.fullSync.quartzCron");
     
      String triggerName = "trigger_" + GrouperLoaderType.PSP_FULL_SYNC.name();
      
      boolean unscheduleAndReturn = false;
      
      if (StringUtils.isEmpty(cronString)) {
        LOG.warn("Full synchronization provisioning jobs are not scheduled. To schedule full synchronization jobs, " +
                 "set grouper-loader.properties key 'changeLog.psp.fullSync.quartzCron' to a cron expression.");
        unscheduleAndReturn = true;
      } else if (StringUtils.isEmpty(GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.psp.fullSync.class"))) {
        LOG.warn("Unable to run a full synchronization provisioning job. " +
            "Set grouper-loader.properties key 'changeLog.psp.fullSync.class' to the name of the class providing a fullSync() method.");
        unscheduleAndReturn = true;
      }
      
      if (unscheduleAndReturn) {
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
        return;
      }
      
      LOG.info("Scheduling " + GrouperLoaderType.PSP_FULL_SYNC.name());
        
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there

      //the name of the job must be unique
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.PSP_FULL_SYNC.name())
        .build();
  
      //schedule this job
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
  
      Trigger trigger = grouperLoaderScheduleType.createTrigger(triggerName, priority, cronString, null);
  
      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trigger), true);
  
    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.PSP_FULL_SYNC.name() + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.PSP_FULL_SYNC.name());
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
        hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
        hib3GrouploaderLog.setJobType(GrouperLoaderType.PSP_FULL_SYNC.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }
  
  }
  
  /**
   * schedule psp full sync job once at startup
   */
  public static void schedulePspFullSyncRunAtStartupJob() {

    //this is a medium priority job
    int priority = 5;        
  
    //schedule the job
    try {        
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      String triggerName = "trigger_" + GrouperLoaderType.PSP_FULL_SYNC.name() + ".runAtStartup";
      
      boolean unscheduleAndReturn = false;

      if (StringUtils.isEmpty(GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.psp.fullSync.runAtStartup"))) {
        LOG.warn("A full synchronization provisioning job will not run once at startup. To run one full synchronization job at startup, " +
            "set grouper-loader.properties key 'changeLog.psp.fullSync.runAtStartup' to 'true'.");
        unscheduleAndReturn = true;
      } else if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.psp.fullSync.runAtStartup", false)) {
        unscheduleAndReturn = true;
      } else if (StringUtils.isEmpty(GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.psp.fullSync.class"))) {
        LOG.warn("Unable to run a full synchronization provisioning job. " +
            "Set grouper-loader.properties key 'changeLog.psp.fullSync.class' to the name of the class providing a fullSync() method.");
        unscheduleAndReturn = true;
      }
      
      if (unscheduleAndReturn) {
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
        return;
      }
      
      LOG.info("Scheduling to run at startup " + GrouperLoaderType.PSP_FULL_SYNC.name());
        
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
              
      //the name of the job must be unique
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity(GrouperLoaderType.PSP_FULL_SYNC.name() + ".runAtStartup")
        .build();
  
      //schedule this job
      Trigger trg = TriggerBuilder.newTrigger()
        .withIdentity(triggerName)
        .startAt(new Date())
        .withPriority(priority)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
        .build();
              
      scheduler.scheduleJob(jobDetail, GrouperUtil.toSet(trg), true);
  
    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.PSP_FULL_SYNC.name() + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.PSP_FULL_SYNC.name());
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleType("simple");
        hib3GrouploaderLog.setJobType(GrouperLoaderType.PSP_FULL_SYNC.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }
  
  }

  /**
   * if there is a threadlocal, then we are in dry run mode
   */
  private static ThreadLocal<GrouperLoaderDryRunBean> threadLocalGrouperLoaderDryRun = new ThreadLocal<GrouperLoaderDryRunBean>();
  
  
  
  
  /**
   * @return the threadLocalGrouperLoaderDryRun
   */
  public static GrouperLoaderDryRunBean internal_retrieveThreadLocalGrouperLoaderDryRun() {
    return threadLocalGrouperLoaderDryRun.get();
  }

  
  /**
   * @param theThreadLocalGrouperLoaderDryRun the threadLocalGrouperLoaderDryRun to set
   */
  public static void internal_assignThreadLocalGrouperLoaderDryRun(
      GrouperLoaderDryRunBean theThreadLocalGrouperLoaderDryRun) {
    if (theThreadLocalGrouperLoaderDryRun == null) {
      threadLocalGrouperLoaderDryRun.remove();
    } else {
      threadLocalGrouperLoaderDryRun.set(theThreadLocalGrouperLoaderDryRun);
    }
  }

  /**
   * bean holds where the logging goes, and if there, then it means we are in dry run mode
   *
   */
  public static class GrouperLoaderDryRunBean {
    
    /**
     * filewriter for output
     */
    private FileWriter fileWriter;
    
    /**
     * file
     */
    private File file;

    /**
     * construct
     * @param fileName
     */
    public GrouperLoaderDryRunBean(String fileName) {
      if (!StringUtils.isBlank(fileName)) {
        this.file = new File(fileName); 
        try {
          this.fileWriter = new FileWriter(this.file);
        } catch (IOException ioe) {
          throw new RuntimeException("Cant open file: " + fileName, ioe);
        }
      }
    }
    
    /**
     * finish everything up
     * @param success
     */
    public void finish(boolean success) {
      if (this.fileWriter != null) {
        try {
          this.fileWriter.close();
        } catch (IOException ioe) {
          throw new RuntimeException("Problem closing file: " + this.file.getAbsolutePath(), ioe);
        }
        System.out.println("Wrote dry run to file: " + this.file.getAbsolutePath() + ", succcess? " + success);
      }
    }
    
    /**
     * write a line, it shouldnt end in newline
     * @param line
     */
    public void writeLine(String line) {
      if (this.fileWriter != null) {
        try {
          this.fileWriter.write(line);
          this.fileWriter.write("\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem writing to file: " + this.file.getAbsolutePath(), ioe);
        }
      } else {
        System.out.println(line);
      }
    }
    
  }
  
  /**
   * @param group
   * @param grouperSession
   * @param fileName is the file where output should go
   * @return status
   */
  public static String dryRunJobOnceForGroup(final GrouperSession grouperSession, final Group group, String fileName) {
    
    //put grouepr in readonly mode
    HibernateSession.threadLocalReadonlyAssign();

    try {
      threadLocalGrouperLoaderDryRun.set(new GrouperLoaderDryRunBean(fileName));
      
      boolean success = false;
      try {
      
        String result = (String)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, new GrouperTransactionHandler() {
          
          /**
           * 
           */
          @Override
          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            return runJobOnceForGroup(grouperSession, group);
            
          }
        });
        
        success = true;
        return result;
        
      } finally {
        GrouperLoaderDryRunBean grouperLoaderDryRunBean = threadLocalGrouperLoaderDryRun.get();
        if (grouperLoaderDryRunBean != null) {
          try {
            grouperLoaderDryRunBean.finish(success);
          } finally {
            threadLocalGrouperLoaderDryRun.remove();
          }
        }
      }
    } finally {
      //no longer in readonly mode
      HibernateSession.threadLocalReadonlyClear();
    }
  }
  
  /**
   * 
   * @return true if dry run
   */
  public static boolean isDryRun() {
    return threadLocalGrouperLoaderDryRun.get() != null;
  }
  
  /**
   * 
   * @param line
   */
  public static void dryRunWriteLine(String line) {
    GrouperLoaderDryRunBean grouperLoaderDryRunBean = threadLocalGrouperLoaderDryRun.get();
    if (grouperLoaderDryRunBean != null) {
      grouperLoaderDryRunBean.writeLine(line);
    }
  }
  
}
