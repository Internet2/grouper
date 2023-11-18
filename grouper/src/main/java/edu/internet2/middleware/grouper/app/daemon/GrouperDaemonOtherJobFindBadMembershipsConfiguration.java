package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobFindBadMembershipsConfiguration extends GrouperDaemonConfiguration {
  
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
  
  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "OTHER_JOB_findBadMemberships".equals(jobName);
  }
  
  

}
