package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob;
import edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobSftpToSqlConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################################
  //  ## sftp delimited file and sync to SQL table
  //  ## "sftpToSqlJobId" is the key of the config, change that for your csv file job
  //  #####################################################
  //
  //  # set this to enable the report
  //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  # otherJob.sftpToSqlJobId.class = edu.internet2.middleware.grouper.app.sqlSync.GrouperSftpToSqlJob
  //
  //  # cron string
  //  # {valueType: "cron", required: true}
  //  # otherJob.sftpToSqlJobId.quartzCron = 
  //
  //  # sftp config id (from grouper.properties) if sftp'ing this file somewhere, otherwise blank
  //  # https://spaces.at.internet2.edu/display/Grouper/Grouper+Sftp+files
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.sftp\\.configId$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.sftp.configId = 
  //
  //  # remote file to sftp to if sftp'ing, e.g. /data01/whatever/MyFile.csv
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.sftp\\.fileNameRemote$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.sftp.fileNameRemote = 
  //
  //  # if it should be an error if the remote file doesnt exist
  //  # {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.ignoreIfRemoteFileDoesNotExist$", defaultValue: "false"}
  //  # otherJob.sftpToSqlJobId.sftpToSql.errorIfRemoteFileDoesNotExist =
  //
  //  # if the file should be deleted from the grouper daemon server after sending it
  //  # {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.deleteFile$", defaultValue: "false"}
  //  # otherJob.sftpToSqlJobId.sftpToSql.deleteFile =
  //
  //  # database external system config id to hit, default to "grouper"
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.database$"}
  //  # otherJob.sftpToSqlJobId.sftpToSql.database = 
  //
  //  # table to sql to, e.g. some_table.  or you can qualify by schema: some_schema.another_table
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.table$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.table = 
  //
  //  # comma separated columns to sync to, e.g. col1, col2, col3
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.columns$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.columns = 
  //
  //  # comma separated primary key columns, e.g. col1
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.columnsPrimaryKey$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.columnsPrimaryKey = 
  //
  //  # if there is a header row
  //  # {valueType: "boolean", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.hasHeaderRow$", defaultValue: "false"}
  //  # otherJob.sftpToSqlJobId.sftpToSql.hasHeaderRow = 
  //
  //  # separator in file
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.separator$", required: true}
  //  # otherJob.sftpToSqlJobId.sftpToSql.separator = 
  //
  //  # escaped separator (cannot contain separator)
  //  # {valueType: "string", regex: "^otherJob\\.([^.]+)\\.sftpToSql\\.escapedSeparator$"}
  //  # otherJob.sftpToSqlJobId.sftpToSql.escapedSeparator = 

      
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
    return GrouperSftpToSqlJob.class.getName();
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
