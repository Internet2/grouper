package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogScriptConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################################
  //  ## Change log script daemon
  //  ## "changeLogScriptDaemonConfigKey" is the key of the config, change that for your change log script daemon
  //  #####################################################
  //
  //  # set this to enable the script daemon
  //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
  //
  //  # cron string
  //  # {valueType: "cron", required: true}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.quartzCron = 0 * * * * ?
  //
  //  # el filter, e.g. event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.elfilter$"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.elfilter = 
  //
  //  # publishing class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.class$"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.publisher.class = edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript
  //
  //  # script type.  compiledJava runs a compiled GSH change-log daemon template.
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.changeLogScriptType$", formElement: "dropdown", optionValues: ["gsh", "compiledJava"], defaultValue: "gsh"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogScriptType =
  //
  //  # config id of the compiled GSH change-log daemon template (templateType=daemonChangeLog, templateMode=compiled) to run
  //  # {valueType: "string", required: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.gshTemplateConfigId$", showEl: "${changeLogScriptType == 'compiledJava'}"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.gshTemplateConfigId =
  //
  //  # file type, you can run a script in config, or run a file in your container
  //  # {valueType: "string", required: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.changeLogFileType$", formElement: "dropdown", optionValues: ["script", "file"], showEl: "${changeLogScriptType != 'compiledJava'}"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileType =
  //
  //  # source of script
  //  # {valueType: "string", required: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.changeLogScriptSource$", formElement: "textarea", showEl: "${changeLogScriptType != 'compiledJava' && changeLogFileType == 'script'}"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogScriptSource =
  //
  //  # file name in container to run
  //  # {valueType: "string", required: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.changeLogFileName$", showEl: "${changeLogScriptType != 'compiledJava' && changeLogFileType == 'file'}"}
  //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileName =

      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "changeLog.consumer." + this.getConfigId() + ".";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "publisher.class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return EsbPublisherChangeLogScript.class.getName();
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
      if (StringUtils.equals(this.getPropertyValueThatIdentifiesThisConfig(),
          GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() 
              + this.getPropertySuffixThatIdentifiesThisConfig()))) {
        return true;
      }
    }
    return false;
  }
}



