package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperReportLogicTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  public void testRunReport() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("reporting.storage.option", "fileSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("reporting.file.system.path", GrouperUtil.tmpDir(true) + "grouperReports");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign configAttributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    GrouperReportConfigurationBean reportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(configAttributeAssign.getId());
    
    GrouperReportInstance newReportInstance = new GrouperReportInstance();
    newReportInstance.setGrouperReportConfigurationBean(reportConfigBean);
    newReportInstance.setReportInstanceConfigMarkerAssignmentId(reportConfigBean.getAttributeAssignmentMarkerId());
    newReportInstance.setReportInstanceMillisSince1970(System.currentTimeMillis());
    newReportInstance.setReportInstanceDownloadCount(0L);
    
    //When
    GrouperReportLogic.runReport(reportConfigBean, newReportInstance, stem0);
    
    //Then
    assertEquals(GrouperReportInstance.STATUS_SUCCESS, newReportInstance.getReportInstanceStatus());
    assertNotNull(newReportInstance.getAttributeAssignId());
    assertNotNull(newReportInstance.getReportDateMillis());
    assertNotNull(newReportInstance.getReportInstanceEncryptionKey());
    assertNotNull(newReportInstance.getReportInstanceRows());
   
  }
  
  private static AttributeAssign saveReportConfigAttributeMetadata(Stem stem, String reportConfigName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
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
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "select name from grouper_members");
    
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
