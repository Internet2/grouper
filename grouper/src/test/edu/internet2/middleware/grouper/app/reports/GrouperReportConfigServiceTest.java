package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.List;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;

public class GrouperReportConfigServiceTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  public void testGetGrouperReportConfigBeanByOwnerAndName() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveReportConfigAttributeMetadata(stem0, "test config");
    
    //When
    GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(stem0, "test config");
    
    //Then
    assertEquals("test config", configBean.getReportConfigName());
    assertEquals("test email body", configBean.getReportConfigEmailBody());
    assertEquals("test description", configBean.getReportConfigDescription());
    assertEquals("email subject", configBean.getReportConfigEmailSubject());
    assertEquals("file name", configBean.getReportConfigFilename());
    assertEquals(true, configBean.isReportConfigEnabled());
    assertEquals(true, configBean.isReportConfigSendEmailToViewers());
    assertEquals("0 */2 * ? * *", configBean.getReportConfigQuartzCron());
    assertEquals("select column from table", configBean.getReportConfigQuery());
    assertEquals("abcdef", configBean.getReportConfigViewersGroupId());
    assertEquals(null, configBean.getReportConfigSendEmailToGroupId());
    assertEquals("CSV", configBean.getReportConfigFormat().name());
    assertEquals("SQL", configBean.getReportConfigType().name());
    
  }
  
  public void testGetGrouperReportConfigBeanByAttributeAssignId() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign attributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    
    //When
    GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssign.getId());
    
    //Then
    assertEquals("test config", configBean.getReportConfigName());
    assertEquals("test email body", configBean.getReportConfigEmailBody());
    assertEquals("test description", configBean.getReportConfigDescription());
    assertEquals("email subject", configBean.getReportConfigEmailSubject());
    assertEquals("file name", configBean.getReportConfigFilename());
    assertEquals(true, configBean.isReportConfigEnabled());
    assertEquals(true, configBean.isReportConfigSendEmailToViewers());
    assertEquals("0 */2 * ? * *", configBean.getReportConfigQuartzCron());
    assertEquals("select column from table", configBean.getReportConfigQuery());
    assertEquals("abcdef", configBean.getReportConfigViewersGroupId());
    assertEquals(null, configBean.getReportConfigSendEmailToGroupId());
    assertEquals("CSV", configBean.getReportConfigFormat().name());
    assertEquals("SQL", configBean.getReportConfigType().name());
    
  }
  
  public void testSaveOrUpdateReportConfigAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperReportConfigurationBean toBeSaved = buildReportConfigBeanForTest();
    
    //When 
    GrouperReportConfigService.saveOrUpdateReportConfigAttributes(toBeSaved, stem0);
    
    //Then
    GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(stem0, "test report config");
    assertEquals("test report config", configBean.getReportConfigName());
    assertEquals("email body", configBean.getReportConfigEmailBody());
    assertEquals("test description", configBean.getReportConfigDescription());
    assertEquals("email subject", configBean.getReportConfigEmailSubject());
    assertEquals("test_file.csv", configBean.getReportConfigFilename());
    assertEquals(true, configBean.isReportConfigEnabled());
    assertEquals(true, configBean.isReportConfigSendEmailToViewers());
    assertEquals("0 */5 * ? * *", configBean.getReportConfigQuartzCron());
    assertEquals("select col from tbl", configBean.getReportConfigQuery());
    assertEquals("abcdefg", configBean.getReportConfigViewersGroupId());
    assertEquals(null, configBean.getReportConfigSendEmailToGroupId());
    assertEquals("CSV", configBean.getReportConfigFormat().name());
    assertEquals("SQL", configBean.getReportConfigType().name());
    
  }
  
  public void testScheduleJob() throws Exception {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperReportConfigurationBean configBean = buildReportConfigBeanForTest();
    configBean.setAttributeAssignmentMarkerId("aaaaaaaaabbbbbb");
    
    //When
    GrouperReportConfigService.scheduleJob(configBean, stem0);
    
    //Then
    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    JobKey jobKey = JobKey.jobKey("grouper_report_"+stem0.getId()+"_"+configBean.getAttributeAssignmentMarkerId());
    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
    
    assertNotNull(jobDetail);
    
    //clean up
    scheduler.deleteJob(jobKey);
    
  }
  
  public void testGetGrouperReportConfigs() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveReportConfigAttributeMetadata(stem0, "test config");
    saveReportConfigAttributeMetadata(stem0, "another test config");
    
    //When
    List<GrouperReportConfigurationBean> configBeans = GrouperReportConfigService.getGrouperReportConfigs(stem0);
    
    //Then
    assertEquals(2, configBeans.size());
    
  }
  
  public void testDeleteGrouperReportConfig() throws SchedulerException {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveReportConfigAttributeMetadata(stem0, "test config");
    GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(stem0, "test config");
    
    //When
    GrouperReportConfigService.deleteGrouperReportConfig(stem0, configBean);
    
    //Then
    configBean = GrouperReportConfigService.getGrouperReportConfigBean(stem0, "test config");
    assertNull(configBean);
    
  }
  
  private static GrouperReportConfigurationBean buildReportConfigBeanForTest() {
    
    GrouperReportConfigurationBean configBean = new GrouperReportConfigurationBean();
    configBean.setReportConfigDescription("test description");
    configBean.setReportConfigEmailBody("email body");
    configBean.setReportConfigEmailSubject("email subject");
    configBean.setReportConfigEnabled(true);
    configBean.setReportConfigFilename("test_file.csv");
    configBean.setReportConfigFormat(ReportConfigFormat.CSV);
    configBean.setReportConfigName("test report config");
    configBean.setReportConfigQuartzCron("0 */5 * ? * *");
    configBean.setReportConfigQuery("select col from tbl");
    configBean.setReportConfigSendEmail(true);
    configBean.setReportConfigSendEmailToViewers(true);
    configBean.setReportConfigType(ReportConfigType.SQL);
    configBean.setReportConfigViewersGroupId("abcdefg");
    
    return configBean;
    
  }
  
  private static AttributeAssign saveReportConfigAttributeMetadata(Stem stem, String reportConfigName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test description");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_BODY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test email body");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_SUBJECT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "email subject");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FILE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "file name");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "CSV");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportConfigName);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUARTZ_CRON, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "0 */2 * ? * *");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUERY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "select column from table");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "SQL");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abcdef");
    
    attributeAssign.saveOrUpdate();
    
    return attributeAssign;
    
  }
}
