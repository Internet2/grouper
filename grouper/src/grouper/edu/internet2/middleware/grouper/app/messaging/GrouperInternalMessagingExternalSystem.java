package edu.internet2.middleware.grouper.app.messaging;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;

public class GrouperInternalMessagingExternalSystem extends GrouperExternalSystem {
  
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
    return GrouperBuiltinMessagingSystem.class.getName();
  }
  
  @Override
  public boolean isCanAdd() {
    return false;
  }

  @Override
  public boolean isCanDelete() {
    return false;
  }

}
