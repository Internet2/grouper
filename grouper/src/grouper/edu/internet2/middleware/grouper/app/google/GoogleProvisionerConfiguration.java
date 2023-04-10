package edu.internet2.middleware.grouper.app.google;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GoogleProvisionerConfiguration extends ProvisioningConfiguration {
  
  public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
  
  static {
    startWithConfigClassNames.add(GoogleProvisioningStartWith.class.getName());
  }
  
  @Override
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    
    List<ProvisionerStartWithBase> result = new ArrayList<ProvisionerStartWithBase>();
    
    for (String className: startWithConfigClassNames) {
      try {
        Class<ProvisionerStartWithBase> configClass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(className);
        ProvisionerStartWithBase config = GrouperUtil.newInstance(configClass);
        result.add(config);
      } catch (Exception e) {
        //TODO
      }
    }
    
    return result;
    
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
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperGoogleProvisioner.class.getName();
  }

}
