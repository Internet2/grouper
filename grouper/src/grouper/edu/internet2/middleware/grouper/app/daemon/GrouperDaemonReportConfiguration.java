package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonReportConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # daily grouper report 
//  # {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
//  otherJob.dailyReport.class = edu.internet2.middleware.grouper.misc.GrouperReport
//
//  #quartz cron-like schedule for daily grouper report, the default is 7am every day: 0 0 7 * * ?
//  # {valueType: "cron"}
//  otherJob.dailyReport.quartzCron = 0 0 7 * * ? 
//
//  #comma separated email addresses to email the daily report, e.g. a@b.c, b@c.d
//  # {valueType: "string", multiple: true}
//  daily.report.emailTo = 
//
//  #if you put a directory here, the daily reports will be saved there, and you can
//  #link up to a web service or store them or whatever.  e.g. /home/grouper/reports/
//  # {valueType: "string"}
//  daily.report.saveInDirectory =



  public GrouperDaemonReportConfiguration() {
    this.extraConfigKeys.add("daily.report.emailTo");
    this.extraConfigKeys.add("daily.report.saveInDirectory");
  }
      
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.dailyReport)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.dailyReport.";
  }

  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "OTHER_JOB_dailyReport".equals(jobName);
  }
}
