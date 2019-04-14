package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperReportsClearJob extends OtherJobBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperReportsClearJob.class);

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    GrouperSession session = GrouperSession.startRootSession();
    clearReports(session);
    return null;
  }
  
  private static void clearReports(GrouperSession session) {
    
    if (!GrouperReportSettings.grouperReportsEnabled()) {
      LOG.info("grouper reports are not enabled. not going to run the grouper reports clear job");
      return;
    }
    
    List<GrouperObject> stems = new ArrayList<GrouperObject>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(GrouperReportSettings.reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_NAME)
        .findStems());
    
    //TODO do the same for groups
    
    clearOldReports(stems);
    
  }
  
  private static void clearOldReports(List<GrouperObject> grouperObjects) {
    
    for (GrouperObject grouperObject: grouperObjects) {
      
      Set<GrouperReportConfigurationBean> reportConfigs = GrouperReportConfigService.getGrouperReportConfigs(grouperObject);
      
      for (GrouperReportConfigurationBean configBean: reportConfigs) {
        Set<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(grouperObject, configBean.getAttributeAssignmentMarkerId());
        deleteOldInstances(new ArrayList<>(reportInstances));
      }
      
    }
    
  }
  
  private static void deleteOldInstances(List<GrouperReportInstance> reportInstances) {
    
    sortInstancesByReportGeneratedTime(reportInstances);
    // only keep 100 instances; delete rest
    if (reportInstances.size() > 100) {
      List<GrouperReportInstance> toBeDeleted = reportInstances.subList(100, reportInstances.size());
      deleteAllGivenInstances(toBeDeleted);
      reportInstances.subList(100, reportInstances.size()).clear();
    }
    
    // delete instances that were generated more than 30 days ago
    for (GrouperReportInstance reportInstance: reportInstances) {
      long reportGeneratedTime = reportInstance.getReportInstanceMillisSince1970();
      long today = new Date().getTime();
      long diff = today - reportGeneratedTime;
      long diffInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
      if (diffInDays > 30) {
        deleteAllGivenInstances(Arrays.asList(reportInstance));
      }
    }
    
  }
  
  public static void deleteAllGivenInstances(List<GrouperReportInstance> instancesToBeDeleted) {
    
    for (GrouperReportInstance instance: instancesToBeDeleted) {
      if (instance.isReportStoredInS3()) {
        GrouperReportLogic.deleteFileFromS3(instance);
        GrouperReportInstanceService.deleteReportInstance(instance);
      } else {
        GrouperReportLogic.deleteFromFileSystem(instance);
        GrouperReportInstanceService.deleteReportInstance(instance);
      }
    }
    
  }
  
  private static void sortInstancesByReportGeneratedTime(List<GrouperReportInstance> instances) {
    
    instances.sort(new Comparator<GrouperReportInstance>() {

      @Override
      public int compare(GrouperReportInstance o1, GrouperReportInstance o2) {
        return new Long(o1.getReportInstanceMillisSince1970()).compareTo(new Long(o2.getReportInstanceMillisSince1970()));
      }
    });
    
  }
  
  private static boolean shouldInstanceBeDeleted(GrouperReportInstance reportInstance) {
    return false;
  }
  
}
