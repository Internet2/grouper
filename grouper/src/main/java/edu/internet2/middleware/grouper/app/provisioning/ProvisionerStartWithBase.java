package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * 
 */
public abstract class ProvisionerStartWithBase extends GrouperConfigurationModuleBase {
  
  private static ExpirableCache<String, Map<String, String>> sessionIdToConfigKeyToValue = new ExpirableCache<>(300);
  
  public void populateCache(String sessionId, Map<String, String> configKeyToValue) {
    sessionIdToConfigKeyToValue.put(sessionId, configKeyToValue);
  }
  
  public Map<String, String> getCachedConfigKeyToValue(String sessionId) {
    return sessionIdToConfigKeyToValue.get(sessionId);
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "provisionerStartWith";
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
 
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisionerStartWith." + this.getConfigId() + ".";
  }
 
  @Override
  public String getConfigIdRegex() {
    return "^(provisionerStartWith)\\.([^.]+)\\.(.*)$";
  }
   
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "startWith";
  }

  /**
   * populate provisionerSuffixToValue with values that are driven off of values from startWithSuffixToValue
   * @param startWithSuffixToValue
   * @param provisionerSuffixToValue
   */
  public abstract void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue, 
      Map<String, Object> provisionerSuffixToValue);
  
  /**
   * call this method after populateProvisionerConfigurationValuesFromStartWith to copy startWith values over to provisionerSuffixToValue where
   * key for startWith matches key in provisionerSuffixToValue
   * @param configId
   * @param startWithSuffixToValue
   * @param provisionerSuffixToValue
   */
  public void  manipulateProvisionerConfigurationValue(String configId, 
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
      
    ProvisioningConfiguration provisionerConfiguration = GrouperUtil.newInstance(getProvisioningConfiguration());
    provisionerConfiguration.setConfigId(configId);
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = provisionerConfiguration.retrieveAttributes();
    
    for (String key: startWithSuffixToValue.keySet()) {
    
      if (attributes.containsKey(key) && !provisionerSuffixToValue.containsKey(key)) {
        provisionerSuffixToValue.put(key, startWithSuffixToValue.get(key));
      }
    }
  }
  
  /**
   * In the <code>map</code>, set all <code>allKeysToSetToNull</code> keys to null except in <code>keysToKeep</code> 
   * @param map
   * @param keysToKeep
   * @param allKeysToSetToNull
   */
  public static void setValuesToNull(Map<String, String> map, Set<String> keysToKeep, Set<String> allKeysToSetToNull) {
    
    for (String keyToSetNull: allKeysToSetToNull) {
      if (!keysToKeep.contains(keyToSetNull)) {
        map.put(keyToSetNull, null);
      }
    }
  }
  
  /**
   * Called everytime screen is redrawn, called before showEl is calculated.
   * 
   * @param suffixToValue - current config submitted by the user
   * @param suffixesUserJustChanged - what the user edited since the last submit to the server
   * @return - suffix to value of things that should change on the screen
   */
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue, Set<String> suffixesUserJustChanged) {
    return null;
  }
  
  public abstract Class<? extends ProvisioningConfiguration> getProvisioningConfiguration();
  
}
