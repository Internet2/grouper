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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FullSyncProvisionerFactory {
  private static Map<String, FullSyncProvisioner> fullSyncers=new ConcurrentHashMap<String, FullSyncProvisioner>();
  
  /**
   * Factory for FullSync objects
   * @param name
   * @return
   * @throws PspException
   */
  public static synchronized FullSyncProvisioner getFullSyncer(String name) throws PspException {
    if ( !fullSyncers.containsKey(name) )
    {
      Provisioner provisioner = ProvisionerFactory.createProvisionerWithName(name);
      
      FullSyncProvisioner fullSyncer = new FullSyncProvisioner(provisioner);
      
      fullSyncers.put(name, fullSyncer);
      
      fullSyncer.start();
      fullSyncer.queueAllGroupsForFullSync();
    }
    
    return fullSyncers.get(name);
  }
}
