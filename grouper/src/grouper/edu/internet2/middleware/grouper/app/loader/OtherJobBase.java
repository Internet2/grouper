/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
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
  final public void execute(String jobName, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    
    long startTime = System.currentTimeMillis();
    GrouperSession grouperSession = null;
    boolean assignedContext = false;
    try {
      grouperSession = GrouperSession.startRootSession();
      if (GrouperContext.retrieveDefaultContext() == null) {
        GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
        assignedContext = true;
      }
      if (hib3GrouperLoaderLog == null) {
        hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        hib3GrouperLoaderLog.setJobName(jobName);
        hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
        hib3GrouperLoaderLog.setStartedTime(new Timestamp(startTime));
        hib3GrouperLoaderLog.setJobType("OTHER_JOB");
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();
        
      }
      
      OtherJobInput otherJobInput = new OtherJobInput();
      
      otherJobInput.setJobName(jobName);
      otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
      otherJobInput.setGrouperSession(grouperSession);
      
      this.run(otherJobInput);
      
      hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      storeLogInDb(hib3GrouperLoaderLog, true, startTime);
      
    } catch (RuntimeException e) {
      LOG.error("Error occurred while running job: " + jobName, e);
      hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouperLoaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));
      
      storeLogInDb(hib3GrouperLoaderLog, false, startTime);
      throw e;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      if (assignedContext) {
        GrouperContext.deleteDefaultContext();
      }
    }

    
  }

  /**
   * implement this method for the logic of the OtherJob
   * @param otherJobInput
   * @return the output
   */
  public abstract OtherJobOutput run(OtherJobInput otherJobInput);
  
}
