package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobCsvReportConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################################
  //  ## CSV reports
  //  ## "reportId" is the key of the config, change that for your csv report
  //  #####################################################
  //
  //
  //  # set this to enable the instrumentation
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  # otherJob.reportId.class = edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob
  //
  //  # cron string
  //  # {valueType: "string"}
  //  # otherJob.reportId.quartzCron = 0 21 7 * * ?
  //
  //  # query to run
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.query$"}
  //  # otherJob.reportId.csvReport.query = select USER_ID, USER_NAME, EMAIL_ADDRESS, AUTH_TYPE, TITLE, DEPARTMENT, CUSTOM_STRING, DAY_PASS, CUSTOM_STRING2, GROUPS from some_view
  //
  //  # database to hit
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.database$"}
  //  # otherJob.reportId.csvReport.database = pennCommunity
  //
  //  # remove underscores and capitalize headers, go from USER_NAME to UserName
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.removeUnderscoresAndCapitalizeHeaders$"}
  //  # otherJob.reportId.csvReport.removeUnderscoresAndCapitalizeHeaders = false
  //
  //  # fileName, e.g. myFile.csv or /opt/whatever/myFile.csv.  If blank will create a name 
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.database$"}
  //  # otherJob.reportId.csvReport.fileName = MyFile.csv
  //
  //  # sftp config id (from grouper.properties) if sftp'ing this file somewhere, otherwise blank
  //  # https://spaces.at.internet2.edu/display/Grouper/Grouper+Sftp+files
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.sftp\\.configId$"}
  //  # otherJob.reportId.csvReport.sftp.configId = someSftpServer
  //
  //  # remote file to sftp to if sftp'ing
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.csvReport\\.sftp\\.fileNameRemote$"}
  //  # otherJob.reportId.csvReport.sftp.fileNameRemote = /data01/whatever/MyFile.csv
  //
  //  # if the file should be deleted from the grouper daemon server after sending it
  //  # {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.csvReport\\.deleteFile$"}
  //  # otherJob.reportId.csvReport.deleteFile = true
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
    return GrouperCsvReportJob.class.getName();
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
