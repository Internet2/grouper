package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.changeLog.consumer.PrintChangeLogConsumer;

public class GrouperDaemonChangeLogConsumerConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # specify the consumers here.  specify the consumer name after the changeLog.consumer. part.  This example is "printTest"
  //  # but it could be "myConsumerName" e.g. changeLog.consumer.myConsumerName.class
  //  # the class must extend edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase
  //  # note see Impl below
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase", regex: "^changeLog\\.consumer\\.([^.]+)\\.class$"}
  //  # changeLog.consumer.printTest.class = edu.internet2.middleware.grouper.changeLog.consumer.PrintTest
  //
  //  # the quartz cron is a cron-like string.  it defaults to every minute on the minute (since the temp to change log job runs
  //  # at 10 seconds to each minute).  it defaults to this: 0 * * * * ?
  //  # though it will stagger each one by 2 seconds.  You can leave this blank
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.quartzCron$"}
  //  # changeLog.consumer.printTest.quartzCron = 

      
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
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisDaemon() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisDaemon() {
    return PrintChangeLogConsumer.class.getName();
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX);
  }
}
