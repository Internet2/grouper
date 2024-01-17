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
package edu.internet2.middleware.grouper.j2ee.status;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * @author shilen
 */
public class DaemonJobStatus {

  private Long lastSuccess;
  
  private boolean isSuccess;
  
  /** */
  private static final String INVALID_PROPERTIES_REGEX = "[^a-zA-Z0-9._-]";
  
  /**
   * @param jobName 
   * @param minutesSinceLastSuccess 
   */
  public DaemonJobStatus(String jobName, int minutesSinceLastSuccess) {
    boolean checkSubJobs = jobName.equals("CHANGE_LOG_changeLogTempToChangeLog") || 
        (jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX) && 
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + jobName.substring(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length()) + ".longRunning", false));
    
    Timestamp timestamp = null;
    
    if (checkSubJobs) {
      timestamp = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.endedTime) from Hib3GrouperLoaderLog theLoaderLog " +
          "where (theLoaderLog.jobName = :theJobName or theLoaderLog.jobName = :theSubJobName)  and theLoaderLog.status = 'SUCCESS'").setString("theJobName", jobName).setString("theSubJobName", "subjobFor_" + jobName).uniqueResult(Timestamp.class);

    } else {
      timestamp = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.endedTime) from Hib3GrouperLoaderLog theLoaderLog " +
        "where theLoaderLog.jobName = :theJobName and theLoaderLog.status = 'SUCCESS'").setString("theJobName", jobName).uniqueResult(Timestamp.class);
    }
    
    Long lastSuccess = timestamp == null ? null : timestamp.getTime();
    
    boolean isSuccess = lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess;

    if (!isSuccess) {

      //OTHER_JOB_syncAllSetTables  
      if (!StringUtils.isBlank(jobName) && jobName.startsWith("OTHER_JOB_")) {
        String innerJobName = GrouperUtil.prefixOrSuffix(jobName, "OTHER_JOB_", false);
        String otherJobQuartzCron = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + innerJobName + ".quartzCron");
        if (StringUtils.equals(otherJobQuartzCron, "59 59 23 31 12 ? 2099")) {
          Timestamp timestampError = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.endedTime) from Hib3GrouperLoaderLog theLoaderLog " +
              "where theLoaderLog.jobName = :theJobName and theLoaderLog.status not in ( 'SUCCESS', 'STARTED')").setString("theJobName", jobName).uniqueResult(Timestamp.class);
          Long lastError = timestampError == null ? null : timestampError.getTime();
          if (lastError == null || (lastSuccess != null && lastSuccess > lastError)) {
            // this isnt supposed to run or last run was success
            isSuccess = true;
          }
        }
      }

    }
    
    if (!isSuccess) {
      if (jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX) && 
          GrouperConfig.retrieveConfig().propertyValueBoolean("ws.diagnostic.successIfChangeLogConsumerProgress", true)) {
        // check if status is started first - probably not too reliable since a previous one could have been stuck on started.
        
        Long count = HibernateSession.byHqlStatic().createQuery("select count(*) from Hib3GrouperLoaderLog theLoaderLog " +
          "where theLoaderLog.jobName = :theJobName and theLoaderLog.status = 'STARTED'").setString("theJobName", jobName).uniqueResult(Long.class);
    
        if (count > 0) {
          // now the real check.  check for progress on the consumer
          String consumerName = jobName.substring(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length());
          
          ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName(consumerName, false);
          if (changeLogConsumer != null && changeLogConsumer.getLastUpdated() != null && (System.currentTimeMillis() - changeLogConsumer.getLastUpdated().getTime()) / (1000 * 60) < minutesSinceLastSuccess) {
            isSuccess = true;
            lastSuccess = changeLogConsumer.getLastUpdated().getTime();
          }
        }
      }
    }
    
    this.isSuccess = isSuccess;
    this.lastSuccess = lastSuccess;
  }
  
  private static ExpirableCache<Boolean, Map> jobNameToCronCache = new ExpirableCache<Boolean, Map>(1);
  
  public static synchronized Map<String, String> jobNameToCron() {
    Map<String, String> jobNameToCron = jobNameToCronCache.get(true);
    if (jobNameToCron == null) {
      
      jobNameToCron = new HashMap<>();
      
      List<Object[]> jobNameToCronList = new GcDbAccess().sql("select distinct gqt.job_name, gqct.cron_expression from grouper_qz_triggers gqt, "
          + "grouper_qz_cron_triggers gqct where gqt.trigger_name = gqct.trigger_name").selectList(Object[].class);
      for (Object[] jobNameCron : jobNameToCronList) {
        jobNameToCron.put((String)jobNameCron[0], (String)jobNameCron[1]);
      }
      jobNameToCronCache.put(true, jobNameToCron);
    }
    return jobNameToCron;
  }
  
  private static ExpirableCache<String, Integer> cronToDefaultMinutesSinceLastSuccessCache = new ExpirableCache<String, Integer>(60*24);
  
  /**
   * 0 * * * * ?
   */
  private static Pattern patternEveryMinute = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+\\*\\s+\\*\\s+[*?]\\s+[*?]\\s+[*?]\\s*[*?]?$");
  
  /**
   * 53 29 * * * ?
   */
  private static Pattern patternEveryHour = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+\\*\\s+[*?]\\s+[*?]\\s+[*?]\\s*[*?]?$");

  
  /**
   * 45 18 2 * * ?
   */
  private static Pattern patternEveryDay = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[*?]\\s+[*?]\\s+[*?]\\s*[*?]?$");

  /**
   * 0 10 5 ? * MON
   */
  private static Pattern patternEveryWeek = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[*?]\\s+[*?]\\s+[A-Za-z0-9,-]+\\s*[*?]?$");



  /**
   * 0 0 12 1 * ?
   */
  private static Pattern patternEveryMonth = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[*?]\\s+[*?]\\s*[*?]?$");

  
  /**
   * 0 0 12 1 3 ?
   */
  private static Pattern patternEveryYear = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[*?]\\s*[*?]?$");

  
  
  /**
   * 59 59 23 31 12 ? 2099
   */
  private static Pattern patternNever = Pattern.compile("^[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9a-zA-Z\\/?,-]+\\s+[0-9]+$");
      
  /**
   * 
   * @param cron
   * @return minutes or -1 if not known, -2 means dont check
   */
  public static int cronToDefaultMinutesSinceLastSuccess(String cron) {
    
    if (StringUtils.isBlank(cron)) {
      return -1;
    }
    
    Integer minutes = cronToDefaultMinutesSinceLastSuccessCache.get(cron);
    if (minutes == null) {
      minutes = -1;
      Matcher matcher = null;
      if (minutes == -1) {
        matcher = patternNever.matcher(cron);
        if (matcher.matches()) {
          minutes = -2;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryMinute.matcher(cron);
        if (matcher.matches()) {
          minutes = 30;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryHour.matcher(cron);
        if (matcher.matches()) {
          minutes = 150;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryDay.matcher(cron);
        if (matcher.matches()) {
          minutes = 60*52;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryWeek.matcher(cron);
        if (matcher.matches()) {
          minutes = 60*24*8;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryMonth.matcher(cron);
        if (matcher.matches()) {
          minutes = 60*24*33;
        }
      }
      if (minutes == -1) {
        matcher = patternEveryYear.matcher(cron);
        if (matcher.matches()) {
          minutes = 60*24*367;
        }
      }
      
      cronToDefaultMinutesSinceLastSuccessCache.put(cron, minutes);
      
    }
    
    
    return minutes;
    
  }
  
  /**
   * @param jobName
   * @param grouperLoaderType
   * @return expected minutes since last success
   */
  public static int getMinutesSinceLastSuccess(String jobName, GrouperLoaderType grouperLoaderType) {

    String diagnosticsName = "loader_" + jobName;

    //we will give it 52 hours... 48 (two days), plus 4 hours to run...
    int defaultMinutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.defaultMinutesSinceLastSuccess", 60*52);
    
    //change logs should go every minute
    if (grouperLoaderType == GrouperLoaderType.CHANGE_LOG) {
      defaultMinutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.defaultMinutesChangeLog", 30);
    }
    
    String cron = jobNameToCron().get(jobName);
    if (!StringUtils.isBlank(cron)) {
      
      int minutesForCron = cronToDefaultMinutesSinceLastSuccess(cron);
      if (minutesForCron > 0) {
        defaultMinutesSinceLastSuccess = minutesForCron;
      }
    }
    
    //default of last success is usually 25 hours, but can be less for change log jobs
    int minutesSinceLastSuccess = -1;


    //for these, also accept with no uuid
    if (grouperLoaderType == GrouperLoaderType.ATTR_SQL_SIMPLE 
        || grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE
        || grouperLoaderType == GrouperLoaderType.SQL_SIMPLE
        || grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST) {
      
      int underscoreIndex = diagnosticsName.lastIndexOf("__");
      
      if (underscoreIndex != -1) {
        
        String jobNameWithoutUuid = diagnosticsName.substring(0, underscoreIndex);
        jobNameWithoutUuid = jobNameWithoutUuid.replaceAll(INVALID_PROPERTIES_REGEX, "_");
        minutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.minutesSinceLastSuccess." + jobNameWithoutUuid, -1);
        
      }
      
    }
    
    //try with full job name
    if (minutesSinceLastSuccess == -1) {
      String configName = diagnosticsName.replaceAll(INVALID_PROPERTIES_REGEX, "_");

      minutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.minutesSinceLastSuccess." + configName, defaultMinutesSinceLastSuccess);
    }
    
    return minutesSinceLastSuccess;
  }



  
  /**
   * @return the lastSuccess
   */
  public Long getLastSuccess() {
    return lastSuccess;
  }



  
  /**
   * @return the isSuccess
   */
  public boolean isSuccess() {
    return isSuccess;
  }
}
