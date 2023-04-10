package edu.internet2.middleware.grouperMessagingActiveMQ;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class ActiveMqGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public List<String> test() throws UnsupportedOperationException {
    return null;
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_CLIENT_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.messaging.system." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.messaging\\.system)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperMessagingActiveMQSystem.class.getName();
  }

}
