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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;


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
  
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    try {
      String consumerName = changeLogProcessorMetadata.getConsumerName();
      MDC.put("why", "CLog/");
      MDC.put("who", consumerName+"/");
      LOG.info("{}: +processChangeLogEntries({})", consumerName, changeLogEntryList.size());
      
      Provisioner provisioner;
      try {
        provisioner = ProvisionerFactory.getProvisioner(consumerName);
        
        // Make sure the full syncer is also created and running
        FullSyncProvisionerFactory.getFullSyncer(consumerName);
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
      if ( firstErrorMessage != null ) 
        summary.append(String.format("Summary: %d successes/%d failures.  ", numSuccessfulWorkItems, numFailedWorkItems));
      
      if ( numSuccessfulWorkItems_thatWillBeRetried > 0 )
        summary.append(String.format("(%d successful entries will be retried because they follow a failure in the queue.) ",
            numSuccessfulWorkItems_thatWillBeRetried));
      
      if ( firstErrorMessage != null )
        summary.append(String.format("First error was: %s", firstErrorMessage));
      
      
      if ( numFailedWorkItems > 0 )
        LOG.warn("Provisioning summary: {}", summary);
      else
        LOG.info("Provisioning summary: {}", summary);
      
      changeLogProcessorMetadata.getHib3GrouperLoaderLog().appendJobMessage(summary.toString());
      return lastSuccessfulChangelogEntry;
    }
    finally {
      MDC.remove("why");
      MDC.remove("who");
    }
  }
}
