/**
 * 
 */
package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class ProvisionerConfigurationOptionValueDriver implements OptionValueDriver {

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<ProvisionerConfiguration> configuredProvisioners = ProvisionerConfiguration.retrieveAllProvisionerConfigurations();
    
    for (ProvisionerConfiguration provisionerConfiguration: configuredProvisioners) {
      
      String configId = provisionerConfiguration.getConfigId();
      keysAndLabels.add(new MultiKey(configId, configId));
    }
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
  }

}
