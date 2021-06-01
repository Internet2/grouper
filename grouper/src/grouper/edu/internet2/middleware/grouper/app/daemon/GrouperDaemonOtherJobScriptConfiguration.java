package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobScriptConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################################
  //  ## Script daemons
  //  ## "scriptDaemonConfigKey" is the key of the config, change that for your script daemon
  //  #####################################################
  //
  //  # set this to enable the script daemon
  //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  # otherJob.scriptDaemonConfigKey.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
  //
  //  # cron string
  //  # {valueType: "cron", required: true}
  //  # otherJob.scriptDaemonConfigKey.quartzCron = 0 38 6 * * ?
  //
  //  # script type.  note: in SQL you should commit after DML commands.
  //  # {valueType: "string", requried: true, regex: "^otherJob\\.([^.]+)\\.scriptType$", formElement: "dropdown", optionValues: ["gsh", "sql"]}
  //  # otherJob.scriptDaemonConfigKey.scriptType = 
  //
  //  # file type, you can run a script in config, or run a file in your container
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.fileType$", formElement: "dropdown", optionValues: ["script", "file"]}
  //  # otherJob.scriptDaemonConfigKey.fileType = 
  //
  //  # source of script
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.scriptSource$", formElement: "textarea", showEl: "${fileType == 'script'}"}
  //  # otherJob.scriptDaemonConfigKey.scriptSource = 
  //
  //  # file name in container to run
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.fileName$", showEl: "${fileType == 'file'}"}
  //  # otherJob.scriptDaemonConfigKey.fileName = 
  //
  //  # if SQL this is the connection name to use
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.connectionName$", showEl: "${scriptType == 'sql'}", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem"}
  //  # otherJob.scriptDaemonConfigKey.connectionName = 

      
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
    return OtherJobScript.class.getName();
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
