package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstance.STATUS_ERROR;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstance.STATUS_SUCCESS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_NAME;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_POINTER;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_STATUS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.List;

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

public class GrouperReportInstanceServiceTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  public void testGetGrouperReportInstance() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign configAttributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    AttributeAssign instanceAttributeAssign = saveReportInstanceAttributeMetadata(stem0, configAttributeAssign.getId(), STATUS_SUCCESS);
    
    //When
    GrouperReportInstance instance = GrouperReportInstanceService.getReportInstance(instanceAttributeAssign.getId());
    
    //Then
    assertEquals(GrouperReportInstance.STATUS_SUCCESS, instance.getReportInstanceStatus());
    assertEquals("data.csv", instance.getReportInstanceFileName());
    assertEquals("/tmp/reports/data.csv", instance.getReportInstanceFilePointer());
    
  }
  
  public void testGetGrouperReportInstances() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign configAttributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    saveReportInstanceAttributeMetadata(stem0, configAttributeAssign.getId(), STATUS_SUCCESS);
    saveReportInstanceAttributeMetadata(stem0, configAttributeAssign.getId(), STATUS_ERROR);
    
    //When
    List<GrouperReportInstance> instances = GrouperReportInstanceService.getReportInstances(stem0, configAttributeAssign.getId());
    
    //Then
    assertEquals(2, instances.size());
    
  }
  
  public void testSaveReportInstanceAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign configAttributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    GrouperReportInstance instance = buildReportInstanceForTest(configAttributeAssign.getId());
    
    //When 
    GrouperReportInstanceService.saveReportInstanceAttributes(instance, stem0);
    GrouperReportInstance instanceSaved = GrouperReportInstanceService.getReportInstance(instance.getAttributeAssignId());
    
    //Then
    assertEquals("data.csv", instanceSaved.getReportInstanceFileName());
    assertEquals("/tmp/reports/data.csv", instanceSaved.getReportInstanceFilePointer());
    
  }
  
  public void testDeleteReportInstances() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    AttributeAssign configAttributeAssign = saveReportConfigAttributeMetadata(stem0, "test config");
    saveReportInstanceAttributeMetadata(stem0, configAttributeAssign.getId(), STATUS_SUCCESS);
    List<GrouperReportInstance> reportInstances = GrouperReportInstanceService.getReportInstances(stem0, configAttributeAssign.getId());
    
    //When 
    GrouperReportInstanceService.deleteReportInstances(reportInstances);
    
    //Then
    reportInstances = GrouperReportInstanceService.getReportInstances(stem0, configAttributeAssign.getId());
    assertEquals(0, reportInstances.size());
    
  }
  
  private static GrouperReportInstance buildReportInstanceForTest(String configAttributeAssignId) {
    
    GrouperReportInstance instance = new GrouperReportInstance();
    instance.setReportDateMillis(System.currentTimeMillis());
    instance.setReportInstanceStatus(GrouperReportInstance.STATUS_SUCCESS);
    instance.setReportInstanceConfigMarkerAssignmentId(configAttributeAssignId);
    instance.setReportInstanceDownloadCount(0L);
    instance.setReportInstanceRows(10L);
    instance.setReportInstanceSizeBytes(4000L);
    instance.setReportElapsedMillis(300L);
    instance.setReportInstanceMillisSince1970(System.currentTimeMillis()-10000L);
    instance.setReportInstanceFileName("data.csv");
    instance.setReportInstanceFilePointer("/tmp/reports/data.csv");
    
    return instance;
    
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
  
  private static AttributeAssign saveReportInstanceAttributeMetadata(Stem stem, 
      String attributeAssignOfReportConfig, String status) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().
        addAttribute(GrouperReportInstanceAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_STATUS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), status);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), attributeAssignOfReportConfig);
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(System.currentTimeMillis()));
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "data.csv");
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_POINTER, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "/tmp/reports/data.csv");
    
    return attributeAssign;
    
  }
}
