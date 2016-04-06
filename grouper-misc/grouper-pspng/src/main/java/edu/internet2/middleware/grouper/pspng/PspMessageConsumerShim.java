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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PspMessageConsumerShim extends ChangeLogConsumerBase {
  final private static Logger LOG = LoggerFactory.getLogger(PspMessageConsumerShim.class);
  
  private static Map<String, Provisioner> provisioners=new ConcurrentHashMap<String, Provisioner>();
  
  public PspMessageConsumerShim() {
    LOG.debug("Constructing PspngChangelogConsumerShim");
  }
  
  public synchronized Provisioner getProvisioner(String consumerName) {
    if ( !provisioners.containsKey(consumerName) )
    {
      LdapGroupProvisionerProperties provisionerProperties = new LdapGroupProvisionerProperties(consumerName);
      provisionerProperties.readConfiguration();
      Provisioner provisioner = new LdapGroupProvisioner(consumerName, provisionerProperties);
      provisioners.put(consumerName, provisioner);
    }
    
    return provisioners.get(consumerName);
  }
  
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    
    String consumerName = changeLogProcessorMetadata.getConsumerName();
    Provisioner provisioner = getProvisioner(consumerName);
    
    List<ProvisioningWorkItem> workItems = new ArrayList<ProvisioningWorkItem>();
    
    LOG.warn("Processing changelog entries: " + changeLogEntryList.size() );
    
    for ( ChangeLogEntry entry : changeLogEntryList ) {
      workItems.add(new ProvisioningWorkItem(entry));
    }
    
    // Tell the provisioner about this batch of workItems
    try {
      provisioner.startProvisioningBatch(workItems);
    }
    catch (PspException e) {
      LOG.error("Unable to begin the provisioning batch", e);
      throw new RuntimeException("No entries provisioned. Batch-Start failed: " + e.getMessage());
    }
    
    // Go through the workItems that were not marked as processed by the startProvisioningBatch
    // and provision them
    for ( ProvisioningWorkItem workItem : workItems ) {
      if ( !workItem.hasBeenProcessed() ) {
        try {
          provisioner.provisionItem(workItem);
        }
        catch (PspException e) {
          LOG.error( String.format("Problem provisioning %s: %s", workItem), e);
          workItem.markAsFailure(e.getMessage());
        }
      }
    }
    
    // Do 'finish' task for workItems that are not marked as processed before now
    
    List<ProvisioningWorkItem> workItemsToFinish = new ArrayList<ProvisioningWorkItem>();
    try {
      for ( ProvisioningWorkItem workItem : workItems ) {
        if ( !workItem.hasBeenProcessed() )
          workItemsToFinish.add(workItem);
      }
      provisioner.finishProvisioningBatch(workItemsToFinish);
    }
    catch (PspException e) {
      LOG.error("Problem completing provisioning batch", e);
      for ( ProvisioningWorkItem workItem : workItemsToFinish ) {
        if ( !workItem.hasBeenProcessed() )
          workItem.markAsFailure("Unable to finish provisioning (%s)", e.getMessage());
      }
    }
    
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
}
