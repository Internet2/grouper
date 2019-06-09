/**
 * 
 */
package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus.ERROR;
import static edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus.SUCCESS;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderLogger;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;


@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperReportJob implements Job {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperReportJob.class);
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    Pattern grouperReportingJobNamePattern = Pattern.compile("^grouper_report_([a-zA-Z0-9]+)_(\\w+)$");
    
    long startTime = System.currentTimeMillis();
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap("grouperReportLog");

    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    
    GrouperObject groupOrStem = null;
    GrouperSession grouperSession = null;
    GrouperReportInstance newReportInstance = new GrouperReportInstance();
    try {
      grouperSession = GrouperSession.startRootSession();
      String jobName = context.getJobDetail().getKey().getName();
      
      if (GrouperLoader.isJobRunning(jobName)) {
        GrouperLoaderLogger.addLogEntry("grouperReportLog", "grouperReportingJobAlreadyRunningSoAborting", true);
        LOG.warn("job " + jobName + " is currently running already.  Aborting this run");
        return;
      }
      
      if (!GrouperReportSettings.grouperReportsEnabled()) {
        GrouperLoaderLogger.addLogEntry("grouperReportLog", "grouperReportingNotEnabledSoAborting", true);
        LOG.info("grouper reporting is not enabled. aborting this run");
        return;
      }
      
      hib3GrouploaderLog.setJobName(jobName);
      
      Matcher matcher = grouperReportingJobNamePattern.matcher(jobName);
      
      String ownerGroupStemId = null;
      String attributeAssignmentMarkerId = null;
      if (matcher.matches()) {
        ownerGroupStemId = matcher.group(1);
        attributeAssignmentMarkerId = matcher.group(2);
      }
      
      if (ownerGroupStemId == null || attributeAssignmentMarkerId == null) {
        LOG.error("what?? why ownerGroupStemId or attributeAssignmentMarkerId is null. job name is "+jobName);
        return;
      }
      
      groupOrStem = GroupFinder.findByUuid(grouperSession, ownerGroupStemId, false);
      if (groupOrStem == null) {
        groupOrStem = StemFinder.findByUuid(grouperSession, ownerGroupStemId, false);
      }
      
      if (groupOrStem == null) {
        LOG.warn("owner grouper object is null for uuid: "+ownerGroupStemId+" job name is: "+jobName);
        GrouperReportConfigService.deleteJobs(ownerGroupStemId);
        return;
      }
      
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());

      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      hib3GrouploaderLog.store();

      GrouperReportConfigurationBean reportConfig = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignmentMarkerId);
      
      if (reportConfig != null) {
        
        newReportInstance.setGrouperReportConfigurationBean(reportConfig);
        newReportInstance.setReportInstanceConfigMarkerAssignmentId(reportConfig.getAttributeAssignmentMarkerId());
        newReportInstance.setReportInstanceMillisSince1970(System.currentTimeMillis());
        newReportInstance.setReportInstanceDownloadCount(0L);
        
        // run report and populate newReportInstance with values
        GrouperReportLogic.runReport(reportConfig, newReportInstance, groupOrStem);
        
        hib3GrouploaderLog.setJobMessage("Ran grouper report: "+reportConfig.getReportConfigName());
        
        GrouperLoaderStatus loaderStatus = newReportInstance.getReportInstanceStatus().equals(GrouperReportInstance.STATUS_SUCCESS) ? SUCCESS: ERROR;
        
        hib3GrouploaderLog.setStatus(loaderStatus.name());
        
      } else {
        LOG.error("No config found for attributeAssignmentMarkerId: "+attributeAssignmentMarkerId);
        hib3GrouploaderLog.setStatus(ERROR.name());
        hib3GrouploaderLog.setJobMessage("No config found for attributeAssignmentMarkerId: "+attributeAssignmentMarkerId);
      }
            
    } catch(Exception e) {
      LOG.error("Error running up job", e);
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      hib3GrouploaderLog.setStatus(ERROR.name());
      hib3GrouploaderLog.setJobMessage(e.getMessage());
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      throw jobExecutionException;
      
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging("grouperReportLog");
      }
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  private static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
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

}
