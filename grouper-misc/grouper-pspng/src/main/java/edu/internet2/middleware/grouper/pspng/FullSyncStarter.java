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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
public class FullSyncStarter extends ChangeLogConsumerBase {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    return 0;
  }
  
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
