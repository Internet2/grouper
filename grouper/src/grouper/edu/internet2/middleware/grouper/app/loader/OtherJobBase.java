/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public abstract class OtherJobBase implements Job {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(OtherJobBase.class);

  /**
   * 
   */
  public static class OtherJobInput {
    
    /**
     * grouper session
     */
    private GrouperSession grouperSession;
    
    /**
     * @return the grouperSession
     */
    public GrouperSession getGrouperSession() {
      return this.grouperSession;
    }
    
    /**
     * @param grouperSession the grouperSession to set
     */
    public void setGrouperSession(GrouperSession grouperSession) {
      this.grouperSession = grouperSession;
    }

    /**
     * job name, e.g. OTHER_JOB_attestationDaemon
     */
    private String jobName;

    
    /**
     * @return the jobName
     */
    public String getJobName() {
      return this.jobName;
    }

    
    /**
     * @param jobName the jobName to set
     */
    public void setJobName(String jobName) {
      this.jobName = jobName;
    }
    
    /**
     * loader log for status
     */
    private Hib3GrouperLoaderLog hib3GrouperLoaderLog;
    
    /**
     * @return the hib3GrouperLoaderLog
     */
    public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
      return this.hib3GrouperLoaderLog;
    }
    
    /**
     * @param hib3GrouperLoaderLog1 the hib3GrouperLoaderLog to set
     */
    public void setHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
      this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
    }
    
  }
  
  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  public static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
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


  
  /**
   * 
   */
  public static class OtherJobOutput {
    
  }
  
  /**
   * 
   */
  public OtherJobBase() {
  }

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  final public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = null;
    jobName = context.getJobDetail().getKey().getName();

    if (GrouperLoader.isJobRunning(jobName)) {
      LOG.warn("Data in grouper_loader_log suggests that job " + jobName + " is currently running already.  Aborting this run.");
      return;
    }

    try {
      execute(jobName, null);
    } catch (Exception e) {
      //make sure this is job execution exception
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      throw (JobExecutionException)e;
    }
  }
    
  /**
   * @param jobName e.g. OTHER_JOB_attestationDaemon
   * @param hib3GrouperLoaderLog if null create a new one
   */
  final public void execute(final String jobName, final Hib3GrouperLoaderLog hib3GrouperLoaderLogInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        long startTime = System.currentTimeMillis();
        boolean assignedContext = false;
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = hib3GrouperLoaderLogInput;
        try {
          if (GrouperContext.retrieveDefaultContext() == null) {
            GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
            assignedContext = true;
          }
          boolean madeChange = false;
          if (hib3GrouperLoaderLog == null) {
            hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getJobName())) {
            hib3GrouperLoaderLog.setJobName(jobName);
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getHost())) {
            hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
            madeChange = true;
          }
          if (null == hib3GrouperLoaderLog.getStartedTime()) {
            hib3GrouperLoaderLog.setStartedTime(new Timestamp(startTime));
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getJobType())) {
            hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getStatus())) {
            hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
            madeChange = true;
          }
          if (madeChange) {
            hib3GrouperLoaderLog.store();
          }
          
          OtherJobInput otherJobInput = new OtherJobInput();
          
          otherJobInput.setJobName(jobName);
          otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
          otherJobInput.setGrouperSession(grouperSession);
          
          OtherJobBase.this.run(otherJobInput);
          
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
          storeLogInDb(hib3GrouperLoaderLog, true, startTime);
          
        } catch (RuntimeException e) {
          LOG.error("Error occurred while running job: " + jobName, e);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          hib3GrouperLoaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));
          
          storeLogInDb(hib3GrouperLoaderLog, false, startTime);
          throw e;
        } finally {
          if (assignedContext) {
            GrouperContext.deleteDefaultContext();
          }
        }

        return null;
      }
    });
    
  }

  /**
   * implement this method for the logic of the OtherJob
   * @param otherJobInput
   * @return the output
   */
  public abstract OtherJobOutput run(OtherJobInput otherJobInput);
  
}
