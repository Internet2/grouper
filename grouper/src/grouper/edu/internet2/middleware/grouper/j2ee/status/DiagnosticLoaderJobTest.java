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
/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee.status;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticLoaderJobTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagnosticLoaderJobTest) {
      DiagnosticLoaderJobTest other = (DiagnosticLoaderJobTest)obj;
      return new EqualsBuilder().append(this.grouperLoaderType, other.grouperLoaderType).append(this.jobName, other.jobName).isEquals();
    }
    return false;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.grouperLoaderType).append(this.jobName).toHashCode();
  }

  /** */
  private static final String INVALID_PROPERTIES_REGEX = "[^a-zA-Z0-9._-]";

  /** job name */
  private String jobName;

  /** grouper loader type */
  private GrouperLoaderType grouperLoaderType;
  
  /**
   * construct with source id
   * @param theJobName
   * @param theGrouperLoaderType
   */
  public DiagnosticLoaderJobTest(String theJobName, GrouperLoaderType theGrouperLoaderType) {
    this.jobName = theJobName;
    this.grouperLoaderType = theGrouperLoaderType;
  }
  
  /**
   * cache the configs for 50 hours
   */
  private static GrouperCache<String, Long> loaderResultsCache = new GrouperCache<String, Long>("loaderResultsDiagnostic", 10000, false, 60 * 60 * 50, 60 * 60 * 50, false);
  
  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    //we will give it 52 hours... 48 (two days), plus 4 hours to run...
    int defaultMinutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.defaultMinutesSinceLastSuccess", 60*52);
    
    //change logs should go every minute
    if (this.grouperLoaderType == GrouperLoaderType.CHANGE_LOG) {
      defaultMinutesSinceLastSuccess = GrouperConfig.retrieveConfig().propertyValueInt("ws.diagnostic.defaultMinutesChangeLog", 30);
    }
    
    //default of last success is usually 25 hours, but can be less for change log jobs
    int minutesSinceLastSuccess = -1;

    String diagnosticsName = this.retrieveName();

    //for these, also accept with no uuid
    if (this.grouperLoaderType == GrouperLoaderType.ATTR_SQL_SIMPLE 
        || this.grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE
        || this.grouperLoaderType == GrouperLoaderType.SQL_SIMPLE
        || this.grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST) {
      
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
      
    
    Long lastSuccess = loaderResultsCache.get(this.jobName);
    
    if (lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess ) {
      this.appendSuccessTextLine("Not checking, there was a success from before: " + GrouperUtil.dateStringValue(lastSuccess) 
          + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
    } else {
     
      Timestamp timestamp = HibernateSession.byHqlStatic().createQuery("select max(theLoaderLog.endedTime) from Hib3GrouperLoaderLog theLoaderLog " +
      		"where theLoaderLog.jobName = :theJobName and theLoaderLog.status = 'SUCCESS'").setString("theJobName", this.jobName).uniqueResult(Timestamp.class);
      
      lastSuccess = timestamp == null ? null : timestamp.getTime();
      
      boolean isSuccess = lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess;
      if (!isSuccess) {
        if (this.jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX) && 
            GrouperConfig.retrieveConfig().propertyValueBoolean("ws.diagnostic.successIfChangeLogConsumerProgress", true)) {
          // check if status is started first - probably not too reliable since a previous one could have been stuck on started.
          
          Long count = HibernateSession.byHqlStatic().createQuery("select count(*) from Hib3GrouperLoaderLog theLoaderLog " +
            "where theLoaderLog.jobName = :theJobName and theLoaderLog.status = 'STARTED'").setString("theJobName", this.jobName).uniqueResult(Long.class);
      
          if (count > 0) {
            // now the real check.  check for progress on the consumer
            String consumerName = this.jobName.substring(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length());
            
            ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName(consumerName, false);
            if (changeLogConsumer != null && changeLogConsumer.getLastUpdated() != null && (System.currentTimeMillis() - changeLogConsumer.getLastUpdated().getTime()) / (1000 * 60) < minutesSinceLastSuccess) {
              isSuccess = true;
              lastSuccess = changeLogConsumer.getLastUpdated().getTime();
            }
          }
        }
      }
      
      if (isSuccess) {
        
        this.appendSuccessTextLine("Found the most recent success: " + GrouperUtil.dateStringValue(lastSuccess) 
            + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
        
      } else {
        loaderResultsCache.remove(this.jobName);
        if (lastSuccess == null) {
          throw new RuntimeException("Cant find a success in job " + this.jobName + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
        }
        throw new RuntimeException("Cant find a success in job " + this.jobName + " since: " + GrouperUtil.dateStringValue(lastSuccess) 
            + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
      }
      
      loaderResultsCache.put(this.jobName, lastSuccess);
    }
        
    return true;
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "loader_" + this.jobName;
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Loader job " + this.jobName;
  }

}
