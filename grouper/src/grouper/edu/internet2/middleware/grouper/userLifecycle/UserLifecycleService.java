package edu.internet2.middleware.grouper.userLifecycle;

import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UserLifecycleService {
  
  /**
   * 
   * @return number of lifecycle event configs
   */
  public static int retrieveUserLifecycleEventNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperLifecycleEventConfig.lifecycleEventConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle action configs
   */
  public static int retrieveUserLifecycleActionNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecycleActionConfiguration.lifecycleActionConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy configs
   */
  public static int retrieveUserLifecyclePolicyNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyConfiguration.lifecyclePolicyConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy part configs
   */
  public static int retrieveUserLifecyclePolicyPartNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyPartConfiguration.lifecyclePolicyPartConfigIds));
    return configIdsInConfig.size();
  }

}
