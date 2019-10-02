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

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

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
    Timestamp timestamp = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.endedTime) from Hib3GrouperLoaderLog theLoaderLog " +
        "where theLoaderLog.jobName = :theJobName and theLoaderLog.status = 'SUCCESS'").setString("theJobName", jobName).uniqueResult(Timestamp.class);
    
    Long lastSuccess = timestamp == null ? null : timestamp.getTime();
    
    boolean isSuccess = lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess;
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
