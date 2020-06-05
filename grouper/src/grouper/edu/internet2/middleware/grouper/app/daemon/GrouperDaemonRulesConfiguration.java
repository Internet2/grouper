package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonRulesConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # when the rules validations and daemons run.  Leave blank to not run
  //  # {valueType: "string"}
  //  rules.quartz.cron = 0 0 7 * * ?
  
  @Override
  public String getConfigIdRegex() {
    return "^(rules)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "rules.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public String getDaemonJobPrefix() {
    return "MAINTENANCE__";
  }
  
  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "MAINTENANCE__rules".equals(jobName);
  }
}
