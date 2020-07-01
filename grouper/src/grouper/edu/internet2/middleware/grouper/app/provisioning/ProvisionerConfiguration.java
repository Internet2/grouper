package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;

public abstract class ProvisionerConfiguration extends GrouperConfigurationModuleBase {
  
  public final static Set<String> provisionerConfigClassNames = new LinkedHashSet<String>();
  
  static {
    provisionerConfigClassNames.add(LdapProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(SqlProvisionerConfiguration.class.getName());
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurationTypes() {
    return (List<ProvisionerConfiguration>) (Object) retrieveAllConfigurationTypesHelper(provisionerConfigClassNames);
  }
  
  /**
   * list of configured provisioner systems
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurations() {
   return (List<ProvisionerConfiguration>) (Object) retrieveAllConfigurations(provisionerConfigClassNames);
  }
  
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "provisionerConfiguration";
  }

}
