package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.misc.FindBadMembershipsDaemon;

public class GrouperDaemonFindBadMembershipsConfiguration extends GrouperDaemonConfiguration {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
  
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "otherJob.findBadMemberships.";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob)\\.(findBadMemberships)\\.(.*)$";
  }
  
  public String getProperySuffixThatIdentifiesThisDaemon() {
    return "class";
  }
  
  public String getProperyValueThatIdentifiesThisDaemon() {
    return FindBadMembershipsDaemon.class.getName();
  }

  @Override
  public boolean isMultiple() {
    return false;
  }

}
