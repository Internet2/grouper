package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonFindBadMembershipsConfiguration extends GrouperDaemonConfiguration {
  
  public static void main(String[] args) {
    String prefix = "otherJob.findBadMemberships.";
    String[] splits = prefix.split("\\.");
    String val = splits[splits.length - 1];
    System.out.println(val);
  }
  
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
