package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
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
    return "^(otherJob)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "a.b.c.SomethingThatExtendsOtherJobBase";
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX);
  }
  
}
