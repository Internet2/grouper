package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;

/**
 * 
 */
public abstract class ProvisionerStartWithBase extends GrouperConfigurationModuleBase {

  @Override
  protected String getConfigurationTypePrefix() {
    return "provisionerStartWith";
  }

  /**
   * populate provisionerSuffixToValue with values that are driven off of values from startWithSuffixToValue
   * @param startWithSuffixToValue
   * @param provisionerSuffixToValue
   */
  public abstract void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue, 
      Map<String, Object> provisionerSuffixToValue);
  
}
