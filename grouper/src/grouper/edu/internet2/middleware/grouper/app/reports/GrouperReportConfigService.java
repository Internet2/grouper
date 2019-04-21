package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DESCRIPTION;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_BODY;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_SUBJECT;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ENABLED;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FILE_NAME;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUARTZ_CRON;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUERY;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_GROUP_ID;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderScheduleType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperReportConfigService {
  
  /**
   * retrieve report config for a given grouper object (group/stem) and report config name
   * @param grouperObject
   * @param reportConfigName
   * @return GrouperReportConfigurationBean
   */
  public static GrouperReportConfigurationBean getGrouperReportConfigBean(GrouperObject grouperObject, String reportConfigName) {
    
    Set<AttributeAssign> attributeAssigns = getAttributeAssigns(grouperObject);
    
    AttributeAssign attributeAssignForReporConfigName = findAttributeAssignForReportConfigName(attributeAssigns, reportConfigName);
    
    if (attributeAssignForReporConfigName == null) {
      return null;
    }
    
    return buildGrouperReportConfigurationBean(attributeAssignForReporConfigName);
  }
  
  /**
   * retrieve report config bean from owner object and attribute assign marker id
   * @param attributeAssignmentMarkerId
   * @return
   */
  public static GrouperReportConfigurationBean getGrouperReportConfigBean(String attributeAssignmentMarkerId) {
    
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignmentMarkerId, true);
    
    return buildGrouperReportConfigurationBean(attributeAssign);
    
  }
  
  /**
   * retrieve report config for a given grouper object (group/stem)
   * @param grouperObject
   * @return set of GrouperReportConfigurationBean
   */
  public static List<GrouperReportConfigurationBean> getGrouperReportConfigs(GrouperObject grouperObject) {
    
    List<GrouperReportConfigurationBean> grouperReportConfigBeans = new ArrayList<GrouperReportConfigurationBean>();
    
    Set<AttributeAssign> attributeAssigns = getAttributeAssigns(grouperObject);
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      GrouperReportConfigurationBean reportConfigurationBean = buildGrouperReportConfigurationBean(attributeAssign);
      grouperReportConfigBeans.add(reportConfigurationBean);
    }
    
    return grouperReportConfigBeans;
  }
  
  /**
   * save or update report config for a given grouper object (group/stem)
   * @param reportConfigBean
   * @param grouperObject
   */
  public static void saveOrUpdateReportConfigAttributes(GrouperReportConfigurationBean reportConfigBean, GrouperObject grouperObject) {
    
    Set<AttributeAssign> attributeAssigns = getAttributeAssigns(grouperObject);
    
    AttributeAssign attributeAssign = findAttributeAssignForReportConfigName(attributeAssigns, reportConfigBean.getReportConfigName());
    
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigDescription());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_BODY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigEmailBody());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_SUBJECT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigEmailSubject());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(reportConfigBean.isReportConfigEnabled()));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FILE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigFilename());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT, true);
    String reportConfigFormat = reportConfigBean.getReportConfigFormat() != null ? reportConfigBean.getReportConfigFormat().name(): null;
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigFormat);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigName());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUARTZ_CRON, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigQuartzCron());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUERY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigQuery());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(reportConfigBean.isReportConfigSendEmail()));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigSendEmailToGroupId());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(reportConfigBean.isReportConfigSendEmailToViewers()));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE, true);
    String reportConfigType = reportConfigBean.getReportConfigType() != null ? reportConfigBean.getReportConfigType().name(): null;
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigType);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigBean.getReportConfigViewersGroupId());
    
    attributeAssign.saveOrUpdate();       
  }
  
  
  /**
   * schedule quartz job
   * @param configBean
   * @param owner
   * @throws SchedulerException
   */
  public static void scheduleJob(GrouperReportConfigurationBean configBean, GrouperObject owner)
      throws SchedulerException {
    
    String jobName = "grouper_report_"+owner.getId()+"_"+configBean.getAttributeAssignmentMarkerId();
    
    JobDetail jobDetail = JobBuilder.newJob(GrouperReportJob.class)
      .withIdentity(jobName)
      .build();
    
    String triggerName = "triggerFor_" + jobName;
    
    Trigger trigger = GrouperLoaderScheduleType.CRON.createTrigger(
        triggerName,
        Trigger.DEFAULT_PRIORITY,
        configBean.getReportConfigQuartzCron(),
        null);

    GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);
  }
  
  /**
   * unschedule quartz job
   * @param configBean
   * @param owner
   * @throws SchedulerException
   */
  public static void unscheduleJob(GrouperReportConfigurationBean configBean, GrouperObject owner) throws SchedulerException {

    String jobName = "grouper_report_"+owner.getId()+"_"+configBean.getAttributeAssignmentMarkerId();
    
    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    
    String triggerName = "triggerFor_" + jobName;
    
    scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
    
  }
  
  /**
   * delete jobs for a given group/stem id
   * @param ownerId
   * @throws SchedulerException
   */
  public static void deleteJobs(String ownerId) throws SchedulerException {
    
    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"));
    
    for (JobKey jobKey: jobKeys) {
      String jobName = jobKey.getName();
      
      if (jobName.startsWith("grouper_report_"+ownerId)) {
        scheduler.deleteJob(jobKey);
      }
    }
  }
  
  /**
   * delete grouper report config and all the instances associated with it
   * @param grouperObject
   * @param reportConfigBean
   */
  public static void deleteGrouperReportConfig(GrouperObject grouperObject, GrouperReportConfigurationBean reportConfigBean) throws SchedulerException {
    List<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(grouperObject, reportConfigBean.getAttributeAssignmentMarkerId());
    
    GrouperReportInstanceService.deleteReportInstances(new ArrayList<GrouperReportInstance>(reportInstances));
    deleteReportConfig(reportConfigBean);
    
    deleteJob(reportConfigBean, grouperObject);
  }
  
  
  /**
   * delete quartz job
   * @param configBean
   * @param owner
   * @throws SchedulerException
   */
  private static void deleteJob(GrouperReportConfigurationBean configBean, GrouperObject owner) throws SchedulerException {

    String jobName = "grouper_report_"+owner.getId()+"_"+configBean.getAttributeAssignmentMarkerId();
    
    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    
    scheduler.deleteJob(JobKey.jobKey(jobName));
    
  }
  
  /**
   * delete report config
   * @param reportConfig
   */
  private static void deleteReportConfig(GrouperReportConfigurationBean reportConfig) {
    String attributeAssignId = reportConfig.getAttributeAssignmentMarkerId();
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    attributeAssign.delete();
  }
  
  /**
   * populate report config bean from attribute assign values
   * @param attributeAssign
   * @return
   */
  private static GrouperReportConfigurationBean buildGrouperReportConfigurationBean(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperReportConfigurationBean result = new GrouperReportConfigurationBean();
    result.setAttributeAssignmentMarkerId(attributeAssign.getId());
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_DESCRIPTION);
    result.setReportConfigDescription(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_EMAIL_BODY);
    result.setReportConfigEmailBody(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_EMAIL_SUBJECT);
    result.setReportConfigEmailSubject(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_ENABLED);
    result.setReportConfigEnabled(assignValue != null ? BooleanUtils.toBoolean(assignValue.getValueString()): false);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_FILE_NAME);
    result.setReportConfigFilename(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_FORMAT);
    result.setReportConfigFormat(assignValue != null ? ReportConfigFormat.valueOfIgnoreCase(assignValue.getValueString(), false) : null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_NAME);
    result.setReportConfigName(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_QUARTZ_CRON);
    result.setReportConfigQuartzCron(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_QUERY);
    result.setReportConfigQuery(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_SEND_EMAIL);
    result.setReportConfigSendEmail(assignValue != null ? BooleanUtils.toBoolean(assignValue.getValueString()): false);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_GROUP_ID);
    result.setReportConfigSendEmailToGroupId(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS);
    result.setReportConfigSendEmailToViewers(assignValue != null ? BooleanUtils.toBoolean(assignValue.getValueString()): false);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_TYPE);
    result.setReportConfigType(assignValue != null ? ReportConfigType.valueOfIgnoreCase(assignValue.getValueString(), false) : null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID);
    result.setReportConfigViewersGroupId(assignValue != null ? assignValue.getValueString(): null);
    
    return result;
  }
  
  /**
   * get config attribute assigns for a given grouper object
   * @param grouperObject
   * @return
   */
  private static Set<AttributeAssign> getAttributeAssigns(GrouperObject grouperObject) {
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      return group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    Stem stem = (Stem)grouperObject;
    return stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    
  }
  
  /**
   * @param attributeAssigns
   * @param reportConfigName
   * @return
   */
  private static AttributeAssign findAttributeAssignForReportConfigName(Set<AttributeAssign> attributeAssigns, String reportConfigName) {
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_CONFIG_NAME);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String reportConfigNameFromDb = attributeAssignValue.getValueString();
      if (reportConfigName.equals(reportConfigNameFromDb)) {
       return attributeAssign;
      }
      
    }
    
    return null;
  }

}
