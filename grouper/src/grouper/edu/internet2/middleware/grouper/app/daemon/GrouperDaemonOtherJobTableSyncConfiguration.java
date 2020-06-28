package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobTableSyncConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  ################################
  //  ## Table sync jobs
  //  ## tableSync jobs should use class: edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
  //  ## and include a setting to point to the grouperClient config, if not same: otherJob.<otherJobName>.grouperClientTableSyncConfigKey = key
  //  ## this is the subtype of job to run: otherJob.<otherJobName>.syncType = fullSyncFull    
  //  ## (can be: fullSyncFull, fullSyncGroupings, fullSyncChangeFlag, incrementalAllColumns, incrementalPrimaryKey)
  //  ################################
  //
  //  # Object Type Job class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  # otherJob.membershipSync.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
  //
  //  # Object Type Job cron
  //  # {valueType: "string"}
  //  # otherJob.membershipSync.quartzCron = 0 0/30 * * * ?
  //
  //  # this is the key in the grouper.client.properties that represents this job
  //  # {valueType: "string"}
  //  # otherJob.membershipSync.grouperClientTableSyncConfigKey = memberships
  //
  //  # fullSyncFull, fullSyncGroupings, fullSyncChangeFlag, incrementalAllColumns, incrementalPrimaryKey
  //  # {valueType: "string"}
  //  # otherJob.membershipSync.syncType = fullSyncFull

      
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "otherJob." + this.getConfigId() + ".";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return TableSyncOtherJob.class.getName();
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX)) {
      if (StringUtils.equals(this.getPropertyValueThatIdentifiesThisConfig(),
          GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() 
              + this.getPropertySuffixThatIdentifiesThisConfig()))) {
        return true;
      }
    }
    return false;
  }
}
