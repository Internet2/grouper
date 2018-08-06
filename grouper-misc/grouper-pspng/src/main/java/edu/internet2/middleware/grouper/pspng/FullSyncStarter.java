package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;

/**
 * This is the class run by the Changelog/Loader quartz job to kick
 * off Full Syncs. This class makes sure the FullSyncProvisioners have
 * been started and then tells each to queueAllGroupsForFullSync().
 * 
 * @author bert
 *
 */
public class FullSyncStarter
        extends ChangeLogConsumerBase
        implements Job {
  protected final static Logger LOG = LoggerFactory.getLogger(FullSyncStarter.class);
  private ThreadLocal<Hib3GrouperLoaderLog> currentLoaderLogEntry = new ThreadLocal<>();

  /**
   * Called directly by quartz from a loader property like: otherJob.key-related-to-provisioner-name.class=...FullSyncStarter
   *
   * @param context
   * @throws JobExecutionException
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long startTime = System.currentTimeMillis();

    GrouperSession grouperSession = null;

    Hib3GrouperLoaderLog hib3GrouploaderLog=null;
    try {
      grouperSession = GrouperSession.startRootSession();
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

      // grouper-loader property: otherJob.provisioner_full.class= and otherJob.provisioner_full.quartzCron=
      // jobName is OTHER_JOB_provisioner_full
      String jobName = context.getJobDetail().getKey().getName();

      // otherJobConfigName will be provisioner_full
      String otherJobConfigName;

      LOG.info("FullSyncStarter being run via quartz job: {}", jobName);

      if (jobName.startsWith(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX)) {
        otherJobConfigName = jobName.substring(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX.length());
      }
      else
        throw new JobExecutionException("PSPNG full-syncs have to be run via otherJob properties, not via " + jobName);

      if (GrouperLoader.isJobRunning(jobName)) {
        LOG.warn("Data in grouper_loader_log suggests that job " + jobName + " is currently running already.  Aborting this run.");
        return;
      }

      hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      currentLoaderLogEntry.set(hib3GrouploaderLog);

      hib3GrouploaderLog.setJobName(jobName);
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(startTime));
      hib3GrouploaderLog.setJobType("OTHER_JOB");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      hib3GrouploaderLog.store();

      // Find the name of the provisioner
      FullSyncProvisioner fullSyncProvisioner = getProvisionerFromOtherJobKey(otherJobConfigName);
      if ( fullSyncProvisioner == null ) {
        throw new Exception("No provisioner found for job: " + otherJobConfigName);
      }

      JobStatistics stats = fullSyncProvisioner.startFullSyncOfAllGroupsAndWaitForCompletion();

      LOG.info("Finished running full-sync job: {}", jobName);
      hib3GrouploaderLog.appendJobMessage("Finished running full-sync job.");

      stats.updateLoaderLog(hib3GrouploaderLog);


      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      storeLogInDb(hib3GrouploaderLog, true, startTime);
    } catch (Exception e) {
      LOG.error("Error running full-sync job", e);
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));

      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      throw jobExecutionException;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   *
   * @param otherJobConfigName If the job is being run with otherJob.provisioner_full.quartzCron=, then
   *                           otherJobConfigName will be provisioner_full
   * @return
   */
  private FullSyncProvisioner getProvisionerFromOtherJobKey(String otherJobConfigName) throws PspException {
    // Remove both full_ prefix and _full suffix
    String provisionerName = otherJobConfigName.replaceAll("_full$", "");
    provisionerName = provisionerName.replaceAll("^full_", "");

    return FullSyncProvisionerFactory.getFullSyncer(provisionerName);
  }


  /**
   * This has been copied from {@link edu.internet2.middleware.grouper.instrumentation.TierInstrumentationDaemon}
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
      hib3GrouploaderLog.setMillis((int) (endTime - startTime));

      hib3GrouploaderLog.store();

    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }

    @Override
  /**
   * This is needed as part of old (psp) way of starting the full-syncs.
   */
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
                                      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    return 0;
  }


  /**
   * Old way of starting full-syncs with changeLog.psp.fullSync.* properties. New way is otherJob.<provisioner>* which invokes execute( )
   */
  public void fullSync() {
    
    Collection<String> provisionerJobNames = getProvisioningJobNames();
    
    LOG.info("Found {} provisioner jobs. Starting full-sync threads for them.: {}", provisionerJobNames.size(), provisionerJobNames);

    Boolean runFullSyncAtStartup = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.psp.fullSync.runAtStartup");
    for ( String name : provisionerJobNames ) {
      FullSyncProvisioner fsProvisioner;
      try {
        fsProvisioner = FullSyncProvisionerFactory.getFullSyncer(name);


        if ( runFullSyncAtStartup != null && runFullSyncAtStartup ) {
          fsProvisioner.queueAllGroupsForFullSync("full-sync-at-startup");
        }
      } catch (PspException e) {
        LOG.error("Problem setting up full sync provisioner {}", name, e);
      }
    }
  }

  /**
   * This looks through the loader properties and pulls out the jobs that are Provisioning jobs
   * by looking at the class referred to in 'type' to see if it is a Provisioner subclass.
   */
  protected Collection<String> getProvisioningJobNames() {
    List<String> provisionerJobNames = new ArrayList<String>();
    
    Map<String, String> loaderProperties = GrouperLoaderConfig.retrieveConfig().propertiesMap(Pattern.compile(".*.type$"));
    
    for ( String key : loaderProperties.keySet() ) {
      String propertyValue=null;
      try {
        propertyValue = loaderProperties.get(key);
        Class clazz = Class.forName(propertyValue);
        if ( Provisioner.class.isAssignableFrom(clazz) ) {
          String keyPieces[] = key.split("\\.");
          String jobName = keyPieces[keyPieces.length-2];
          provisionerJobNames.add(jobName);
        }
        else
          LOG.info("Class is not a Provisioner subclass: {}", propertyValue);
      } catch (ClassNotFoundException e)
      { // Skip invalid class reference
        LOG.warn("Could not find class {}. Assuming it is not a Provisioner subclass.", propertyValue);
        continue; 
      }
    }
    return provisionerJobNames;
  }
}
