package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderIncrementalJob;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

// generic other job to add just classname and cron
public class GrouperDaemonOtherJobConfiguration extends GrouperDaemonConfiguration {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
  
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "otherJob." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public boolean isMultiple() {
    return true;
  }


  
  @Override
  public String getPropertySuffixThatIdentifiesThisDaemon() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisDaemon() {
    return "a.b.c.SomethingThatExtendsOtherJobBase";
  }

  
  
}
