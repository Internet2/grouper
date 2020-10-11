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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.pspng.FullSyncProvisioner.QUEUE_TYPE;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/** 
 * This class connects a PSPNG provsioner with the changelog. This is only necessary
 * until the PSPNG provisioners are written as daemons that pull events from queues.
 * 
 * @author Bert Bee-Lindgren
 *
 */
public class PspChangelogConsumerShim extends ChangeLogConsumerBase {
  final private static Logger LOG = LoggerFactory.getLogger(PspChangelogConsumerShim.class);
  
  public PspChangelogConsumerShim() {
    LOG.debug("Constructing PspngChangelogConsumerShim");
  }
  
  public static void main(final String[] args) {
    GrouperStartup.startup();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (args.length != 1) {
          throw new RuntimeException("Pass in job name, e.g. CHANGE_LOG_consumer_pspng_oneprodFull");
        }
        
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobName(args[0]);
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
//        hib3GrouploaderLog.store();
        
        try {
          String consumerName = hib3GrouploaderLog.getJobName().substring(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX.length());

          ChangeLogHelper.processRecords(consumerName, hib3GrouploaderLog, new PspChangelogConsumerShim());
          
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
          
        } catch (Exception e) {
          LOG.error("Error processing records", e);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        }
//        hib3GrouploaderLog.store();

        return null;
      }
    });
  }
  
  private Provisioner provisioner = null;
  
  
  
  
  public Provisioner getProvisioner() {
    return provisioner;
  }

  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    Date batchStartTime = new Date();
    JobStatistics incrementalStats = new JobStatistics();
    JobStatistics fullSyncStats = new JobStatistics();
    try {
      String consumerName = changeLogProcessorMetadata.getConsumerName();
      MDC.put("why", "CLog/");
      MDC.put("who", consumerName+"/");
      LOG.info("{}: +processChangeLogEntries({})", consumerName, changeLogEntryList.size());
      
      try {
        this.provisioner = ProvisionerFactory.getIncrementalProvisioner(consumerName);
        Provisioner.allGroupsForProvisionerFromCacheClear(this.provisioner.getConfigName());
        
        this.provisioner.setJobStatistics(incrementalStats);
        // Make sure the full syncer is also created and running
        FullSyncProvisioner fullSyncProvisioner = FullSyncProvisionerFactory.getFullSyncer(provisioner);
        fullSyncProvisioner.getProvisioner().setJobStatistics(fullSyncStats);

        for (QUEUE_TYPE queue_type : QUEUE_TYPE.values()) {
          if (queue_type.usesGrouperMessagingQueue) {
            fullSyncProvisioner.setUpGrouperMessagingQueue(queue_type);
          }
        }


      } catch (PspException e1) {
        LOG.error("Provisioner {} could not be created", consumerName, e1);
        throw new RuntimeException("provisioner could not be created: " + e1.getMessage());
      }
      
      List<ProvisioningWorkItem> workItems = new ArrayList<ProvisioningWorkItem>();
      
      for ( ChangeLogEntry entry : changeLogEntryList ) {
        workItems.add(new ProvisioningWorkItem(entry));
      }
      
      provisioner.provisionBatchOfItems(workItems);
      
      // This determines which changelog entries to acknowledge: The highest
      // number entry before we have a failure
      long lastSuccessfulChangelogEntry = -1;
      int numSuccessfulWorkItems = 0;
      int numFailedWorkItems = 0;
      int numSuccessfulWorkItems_thatWillBeRetried=0;
      
      String firstErrorMessage = null;
      
      for( ProvisioningWorkItem workItem : workItems ) {
        if ( workItem.wasSuccessful() ) {
          numSuccessfulWorkItems++;
          
          // If we haven't seen a failure yet, then keep counting up the successes
          if ( numFailedWorkItems == 0 )
            lastSuccessfulChangelogEntry = workItem.getChangelogEntry().getSequenceNumber();
          else
            numSuccessfulWorkItems_thatWillBeRetried++;
        }
        else if ( firstErrorMessage == null ) {
          numFailedWorkItems++;
          firstErrorMessage = workItem.getStatusMessage();
        }
      }
      
      StringBuilder summary = new StringBuilder();
      summary.append(String.format("%d successes/%d failures. Duration=%s ",
              numSuccessfulWorkItems, numFailedWorkItems, PspUtils.formatElapsedTime(batchStartTime, null)));
      
      if ( numSuccessfulWorkItems_thatWillBeRetried > 0 )
        summary.append(String.format("(%d successful entries will be retried because they follow a failure in the queue.) ",
            numSuccessfulWorkItems_thatWillBeRetried));
      
      if ( firstErrorMessage != null )
        summary.append(String.format("First error was: %s", firstErrorMessage));
      
      
      if ( numFailedWorkItems > 0 )
        LOG.warn("Provisioning batch summary: {}", summary);
      else
        LOG.info("Provisioning batch summary: {}", summary);
      
      changeLogProcessorMetadata.getHib3GrouperLoaderLog().setInsertCount(incrementalStats.insertCount.get() + fullSyncStats.insertCount.get());
      changeLogProcessorMetadata.getHib3GrouperLoaderLog().setDeleteCount(incrementalStats.deleteCount.get() + fullSyncStats.deleteCount.get());
      changeLogProcessorMetadata.getHib3GrouperLoaderLog().setTotalCount(workItems.size());
      
      changeLogProcessorMetadata.getHib3GrouperLoaderLog().appendJobMessage(summary.toString());
      return lastSuccessfulChangelogEntry;
    }
    finally {
      MDC.remove("why");
      MDC.remove("who");
    }
  }
}
