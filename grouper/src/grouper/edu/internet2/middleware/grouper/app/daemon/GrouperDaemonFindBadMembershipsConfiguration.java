package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonFindBadMembershipsConfiguration extends GrouperDaemonConfiguration {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
  
  @Override
  public String getConfigItemPrefix() {
    return "otherJob.findBadMemberships.";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.findBadMemberships)\\.(.*)$";
  }

  @Override
  public boolean isMultiple() {
    return false;
  }

}
