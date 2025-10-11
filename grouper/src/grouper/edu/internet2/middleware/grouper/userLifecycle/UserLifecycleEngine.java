package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UserLifecycleEngine {
  
  
  
  /**
   * take data fields and make sure the have an internal id
   */
  public static void syncUserLifecycleEventConfigs(GrouperConfig grouperConfig) {
    
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperLifecycleEventConfig.lifecycleEventConfigIds));

    
    List<GrouperLifecycleEventConfig> grouperLifecycleEventConfigsInDb = GrouperUtil.nonNull(UserLifecycleEventConfigDao.selectAll());

    Map<String, GrouperLifecycleEventConfig> configIdToGrouperLifecycleEventConfigInDb = new HashMap<String, GrouperLifecycleEventConfig>();
    Map<Long, GrouperLifecycleEventConfig> internalIdToGrouperLifecycleEventConfigInDb = new HashMap<Long, GrouperLifecycleEventConfig>();

    for (GrouperLifecycleEventConfig grouperLifecycleEventConfig : grouperLifecycleEventConfigsInDb) {
      configIdToGrouperLifecycleEventConfigInDb.put(grouperLifecycleEventConfig.getConfigId(), grouperLifecycleEventConfig);
      internalIdToGrouperLifecycleEventConfigInDb.put(grouperLifecycleEventConfig.getInternalId(), grouperLifecycleEventConfig);
    }
    
    // additions
    Set<String> configIdsToInsert = new HashSet<String>(configIdsInConfig);
    configIdsToInsert.removeAll(configIdToGrouperLifecycleEventConfigInDb.keySet());
    UserLifecycleEventConfigDao.insertMissingConfigIds(configIdsToInsert);
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperLifecycleEventConfigInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    
    List<GrouperLifecycleEventConfig> eventConfigsToDelete = new ArrayList<>();
    
    for (String configIdToDelete : configIdsToDelete) {
      GrouperLifecycleEventConfig grouperLifecycleEventConfig = configIdToGrouperLifecycleEventConfigInDb.get(configIdToDelete);
      eventConfigsToDelete.add(grouperLifecycleEventConfig);
    }
    UserLifecycleEventConfigDao.delete(eventConfigsToDelete);
    
    // updates
    
    // grouperLifecycleEventConfigsInDb is in the db (lifecycle event table) and configIdsInConfig is what's in the grouper_config table 
    // configIdsToInsert are already taken care of and configIdsToDelete are also taken care of
    
    Set<String> potentialConfigIdsToUpdate = new HashSet<String>(configIdsInConfig);
    potentialConfigIdsToUpdate.removeAll(configIdsToInsert);
    potentialConfigIdsToUpdate.removeAll(configIdsToDelete);
    
    for (String configIdToUpdate : potentialConfigIdsToUpdate) {
      GrouperLifecycleEventConfig grouperLifecycleEventConfig = configIdToGrouperLifecycleEventConfigInDb.get(configIdToUpdate);
      UserLifecycleEventConfigDao.updateEventLifecycleConfig(grouperLifecycleEventConfig, configIdToUpdate);
    }
    
  }

}
