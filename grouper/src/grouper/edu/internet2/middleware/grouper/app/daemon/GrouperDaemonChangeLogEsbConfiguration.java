package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;

public class GrouperDaemonChangeLogEsbConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## ESB integration
  //  #####################################
  //
  //  # quartz cron
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.quartzCron$"}
  //  #changeLog.consumer.awsJira.quartzCron = 0/15 * * * * ?
  //
  //  # class
  //  # {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer", regex: "^changeLog\\.consumer\\.([^.]+)\\.class$"}
  //  #changeLog.consumer.awsJira.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
  //
  //  # el filter
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.elfilter$"}
  //  #changeLog.consumer.awsJira.elfilter = event.eventType eq 'MEMBERSHIP_ADD' || event.eventType eq 'MEMBERSHIP_ADD'
  //
  //  # if dont send sensitive data
  //  # {valueType: "boolean", regex: "^changeLog\\.consumer\\.([^.]+)\\.noSensitiveData$"}
  //  #changeLog.consumer.awsJira.noSensitiveData = true
  //
  //  # if you want to encrypt messages, set this to an implementation of edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface
  //  # {valueType: "class", regex: "^changeLog\\.consumer\\.([^.]+)\\.encryptionImplementation$", mustImplementInterface: "edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface"}
  //  #changeLog.consumer.awsJira.encryptionImplementation = edu.internet2.middleware.grouperClient.encryption.GcSymmetricEncryptAesCbcPkcs5Padding
  //
  //  # this is a key or could be encrypted in a file as well like other passwords
  //  # generate a key with: java -cp grouperClient.jar edu.internet2.middleware.grouperClient.encryption.GcGenerateKey 
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.encryptionKey$"}
  //  #changeLog.consumer.awsJira.encryptionKey = abc123
  //
  //  # if you dont want to send the first 4 of the sha hash base 64 of the secret
  //  # {valueType: "boolean", regex: "^changeLog\\.consumer\\.([^.]+)\\.dontSendShaBase64secretFirst4$"}
  //  #changeLog.consumer.awsJira.dontSendShaBase64secretFirst4 = false
  //
  //  # publisher class
  //  # {valueType: "class", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.class$", mustExtendClass: "edu.internet2.middleware.grouperAwsChangelog.GrouperAwsEsbPublisher"}
  //  #changeLog.consumer.awsJira.publisher.class = edu.internet2.middleware.grouperAwsChangelog.GrouperAwsEsbPublisher
  //
  //  # aws access key
  //  # {valueType: "password", sensitive: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.awsAccessKey$"}
  //  #changeLog.consumer.awsJira.publisher.awsAccessKey = ABCXYZ
  //
  //  # aws secret key
  //  # {valueType: "password", sensitive: true, regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.awsSecretKey$"}
  //  #changeLog.consumer.awsJira.publisher.awsSecretKey = 123REWQ
  //
  //  # aws region
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.awsRegion$"}
  //  #changeLog.consumer.awsJira.publisher.awsRegion = US_EAST_1
  //
  //  # aws sns topic arn
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.awsSnsTopicArn$"}
  //  #changeLog.consumer.awsJira.publisher.awsSnsTopicArn = arn:aws:sns:us-east-1:123:name

      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId.");
    }
    return "changeLog.consumer." + this.getConfigId() + ".";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return EsbConsumer.class.getName();
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
