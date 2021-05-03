package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.NotificationDaemon;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobNotificationConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################################
  //  ## Email notifications (e.g. daily)
  //  #####################################################
  //
  //
  //  # set this class to enable the email notification
  //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  # otherJob.emailNotificationConfigId.class = edu.internet2.middleware.grouper.app.loader.NotificationDaemon
  //
  //  # cron string
  //  # {valueType: "cron"}
  //  # otherJob.emailNotificationConfigId.quartzCron = 0 03 5 * * ?
  //
  //  # is the population to get the email from a group or from a sql query?
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.populationType$", formElement: "dropdown", optionValues: ["groupMembership", "sqlQuery"]}
  //  # otherJob.emailNotificationConfigId.populationType = 
  //
  //  # group name fully qualified of group which the population should receive the email.  e.g. a:b:c
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.emailListGroupName$", showEl: "${populationType == 'groupMembership'}"}
  //  # otherJob.emailNotificationConfigId.emailListGroupName = 
  //
  //  # sql connection id (of you database external systems) that the query runs where the results are the people to send emails to.
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.emailListDbConnection$", showEl: "${populationType == 'sqlQuery'}", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem"}
  //  # otherJob.emailNotificationConfigId.emailListDbConnection = 
  //
  //  # sql query where each row represents a subject to send an email to.
  //  # There must be a column of subject_id.  There can optionally be a column email_address_to_send_to if you want to override the subject email address.
  //  # Any other columns will be available for the email body and subject template.
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.emailListQuery$", showEl: "${populationType == 'sqlQuery'}"}
  //  # otherJob.emailNotificationConfigId.emailListQuery = 
  //
  //  # subject source id of subjects to send emails to (filter subjects from other sources)
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.subjectSourceId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.subject.provider.SourceManagerOptionValueDriver"}
  //  # otherJob.emailNotificationConfigId.subjectSourceId = 
  //
  //  # key from externalized text config file (e.g. grouper.text.en.us.properties) of the subject of the email.  Note, you can use any variables
  //  # that the body uses
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.emailSubjectTextKey$"}
  //  # otherJob.emailNotificationConfigId.emailSubjectTextKey = 
  //
  //  # key from externalized text config file (e.g. grouper.text.en.us.properties) of the body of the email.  You can use any variables
  //  # from the query or the subject.  Use JEXL expressions.  e.g. hello ${subject_name},
  //  # subject_name, subject_id, subject_description, subject_attribute_firstname (where "firstname" is a lower case subject attribute key),
  //  # column_some_column_name where "some_column_name" is a lower case column name from query (if applicable)
  //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.emailBodyTextKey$"}
  //  # otherJob.emailNotificationConfigId.emailBodyTextKey = 
  //
  //  # group name of a group that the user will be added to after an email is sent, with a membership attribute with value
  //  # of yyyy/mm/dd (string) of as email sent date
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.lastSentGroupName$"}
  //  # otherJob.emailNotificationConfigId.lastSentGroupName = 
  //
  //  # name of an attribute def name will be added to the membership of the user who got the email, 
  //  # with value of of yyyy/mm/dd (string) of as email sent date
  //  # the attribute definition must be string value, single assign, single value, and assignable to immediate membership
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.lastSentAttributeDefName$"}
  //  # otherJob.emailNotificationConfigId.lastSentAttributeDefName = 
  //
  //  # group name of a group that the user must be in, to be eligible to get emails sent to them
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.eligibilityGroupName$"}
  //  # otherJob.emailNotificationConfigId.eligibilityGroupName = 
  //
  //  # email addresses to be emailed as bcc (comma separated)
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.bccsCommaSeparated$"}
  //  # otherJob.emailNotificationConfigId.bccsCommaSeparated = 
  //

      
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
    return NotificationDaemon.class.getName();
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
