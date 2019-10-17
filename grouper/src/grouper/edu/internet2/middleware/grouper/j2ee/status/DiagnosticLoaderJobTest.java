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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
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
   * daemon jobs enabled cache - 5 minutes
   */
  private static GrouperCache<String, Boolean> daemonJobsEnabledCache = new GrouperCache<String, Boolean>("daemonJobsEnabledCache", 20000, false, 60 * 5, 60 * 5, false);

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    int minutesSinceLastSuccess = DaemonJobStatus.getMinutesSinceLastSuccess(this.jobName, this.grouperLoaderType);
    
    Long lastSuccess = loaderResultsCache.get(this.jobName);
    
    if (lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess ) {
      this.appendSuccessTextLine("Not checking, there was a success from before: " + GrouperUtil.dateStringValue(lastSuccess) 
          + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
    } else {
     
      DaemonJobStatus daemonJobStatus = new DaemonJobStatus(this.jobName, minutesSinceLastSuccess);
      boolean isSuccess = daemonJobStatus.isSuccess();
      lastSuccess = daemonJobStatus.getLastSuccess();
      
      if (isSuccess) {
        
        this.appendSuccessTextLine("Found the most recent success: " + GrouperUtil.dateStringValue(lastSuccess) 
            + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
      } else if (!isJobEnabled(this.jobName)) {
        this.appendSuccessTextLine("Job is not enabled");
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

  private boolean isJobEnabled(String jobName) {
    if (daemonJobsEnabledCache.get(jobName) == null) {
      daemonJobsEnabledCache.put(jobName, GrouperLoader.isJobEnabled(jobName));
    }
    
    return daemonJobsEnabledCache.get(jobName);
  }
}
