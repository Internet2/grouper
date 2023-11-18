package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogTempToChangeLogConfiguration extends GrouperDaemonConfiguration {

  public GrouperDaemonChangeLogTempToChangeLogConfiguration() {
    this.extraConfigKeys.add("changeLog.includeFlattenedMemberships");
    this.extraConfigKeys.add("changeLog.includeFlattenedPrivileges");
    this.extraConfigKeys.add("changeLog.includeRolesWithPermissionChanges");
    this.extraConfigKeys.add("changeLog.includeSubjectsWithPermissionChanges");
    this.extraConfigKeys.add("changeLog.includeNonFlattenedMemberships");
    this.extraConfigKeys.add("changeLog.includeNonFlattenedPrivileges");
    this.extraConfigKeys.add("changeLog.tooManyChangeLogUpdatesSize");

  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.changeLogTempToChangeLog)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.changeLogTempToChangeLog.";
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "CHANGE_LOG_";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "CHANGE_LOG_changeLogTempToChangeLog".equals(jobName);
  }
}
