package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ATTRIBUTE_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperReportClearJob extends OtherJobBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperReportClearJob.class);

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    int instancesDeleted = clearOldReports();
    
    otherJobInput.getHib3GrouperLoaderLog().store();
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Deleted "+instancesDeleted+" of grouper reports");
    
    return null;
  }
  
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_reportsClearDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperReportClearJob().run(otherJobInput);
  }
  
  /**
   * delete report instances that are old
   * @return number of deleted report instances
   */
  protected static int clearOldReports() {
    
    if (!GrouperReportSettings.grouperReportsEnabled()) {
      LOG.info("grouper reports are not enabled. not going to run the grouper reports clear job");
      return 0;
    }
    
    List<GrouperObject> grouperObjects = new ArrayList<GrouperObject>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(GrouperReportSettings.reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_ATTRIBUTE_NAME)
        .findStems());
    
    grouperObjects.addAll(new ArrayList<GrouperObject>(new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(GrouperReportSettings.reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_ATTRIBUTE_NAME)
        .findGroups()));
    
    return clearOldReports(grouperObjects);
    
  }
  
  /**
   * @param grouperObjects
   * @return number of deleted reports
   */
  private static int clearOldReports(List<GrouperObject> grouperObjects) {
    
    int totalInstancesCleared = 0;
    for (GrouperObject grouperObject: grouperObjects) {
      
      List<GrouperReportConfigurationBean> reportConfigs = GrouperReportConfigService.getGrouperReportConfigs(grouperObject);
      
      for (GrouperReportConfigurationBean configBean: reportConfigs) {
        List<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(grouperObject, configBean.getAttributeAssignmentMarkerId());
        int instancesClearedPerConfig = deleteOldInstances(new ArrayList<>(reportInstances));
        totalInstancesCleared = totalInstancesCleared + instancesClearedPerConfig;
      }
      
    }
    
    return totalInstancesCleared;
    
  }
  
  /**
   * pick old reports from given list of instances and delete them
   * @param reportInstances
   * @return number of deleted reports
   */
  private static int deleteOldInstances(List<GrouperReportInstance> reportInstances) {
    
    sortInstancesByReportGeneratedTime(reportInstances);
    int instancesCleared = 0;
    // only keep 100 instances; delete rest
    if (reportInstances.size() > 100) {
      List<GrouperReportInstance> toBeDeleted = reportInstances.subList(100, reportInstances.size());
      GrouperReportInstanceService.deleteReportInstances(toBeDeleted);
      instancesCleared = instancesCleared + toBeDeleted.size();
      reportInstances.subList(100, reportInstances.size()).clear();
    }
    
    // delete instances that were generated more than 30 days ago
    for (GrouperReportInstance reportInstance: reportInstances) {
      long reportGeneratedTime = reportInstance.getReportInstanceMillisSince1970();
      long today = new Date().getTime();
      long diff = today - reportGeneratedTime;
      long diffInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
      if (diffInDays > 30) {
        GrouperReportInstanceService.deleteReportInstances(Arrays.asList(reportInstance));
        instancesCleared = instancesCleared + 1;
      }
    }
    
    return instancesCleared;
    
  }
  
  /**
   * @param instances
   */
  private static void sortInstancesByReportGeneratedTime(List<GrouperReportInstance> instances) {
    
    instances.sort(new Comparator<GrouperReportInstance>() {
      @Override
      public int compare(GrouperReportInstance o1, GrouperReportInstance o2) {
        return new Long(o1.getReportInstanceMillisSince1970()).compareTo(new Long(o2.getReportInstanceMillisSince1970()));
      }
    });
    
  }
  
}
