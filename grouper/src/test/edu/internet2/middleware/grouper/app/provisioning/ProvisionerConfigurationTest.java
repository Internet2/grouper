package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleSubSection;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import junit.textui.TestRunner;

public class ProvisionerConfigurationTest extends GrouperTest {
  
  public ProvisionerConfigurationTest(String name) {
    super(name);
  }
  
  public static void main(String[] args) {
    TestRunner.run(new ProvisionerConfigurationTest("testSqlProvisionerConfigurationInsertEditDelete"));
  }
  
  public void testLdapProvisionerConfigurationInsertEditDelete() {
    
    LdapProvisionerConfiguration provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    int attributesSize = provisionerConfiguration.retrieveAttributes().size();
    
    assertTrue(attributesSize > 0);
    
    List<GrouperConfigurationModuleSubSection> subSections = provisionerConfiguration.getSubSections();
    assertTrue(subSections.size() > 0);
    
    Set<String> subsectionsLabels = new HashSet<String>();
    subsectionsLabels.add("membership");
    subsectionsLabels.add("entity");
    subsectionsLabels.add("group");
    subsectionsLabels.add("assigningProvisioning");
    subsectionsLabels.add("advanced");
    
    for (GrouperConfigurationModuleSubSection subSection: subSections) {
      subsectionsLabels.contains(subSection.getLabel());
    }
    
    // set the required values so that the validation pass and values can be inserted into the db
    GrouperConfigurationModuleAttribute attribute = provisionerConfiguration.retrieveAttributes().get("ldapExternalSystemConfigId");
    attribute.setValue("ldapExternalSystem");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    attribute.setValue("mySubjectSource");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("provisioningType");
    attribute.setValue("groupAttributes");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("groupDnType");
    attribute.setValue("bushy");
    
    // set this value so that we can test that some internal attributes are also being saved correctly in the db.
    attribute = provisionerConfiguration.retrieveAttributes().get("hasTargetGroupLink");
    attribute.setValue("true");
    
    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    // insert config into db
    provisionerConfiguration.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
    
    assertEquals(0, validationErrorsToDisplay.size());
    
    provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    attributesSize = provisionerConfiguration.retrieveAttributes().size();
    
    assertTrue(attributesSize > 0);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    String value = attribute.getValue();
    assertEquals("mySubjectSource", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("provisioningType");
    value = attribute.getValue();
    assertEquals("groupAttributes", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("groupDnType");
    value = attribute.getValue();
    assertEquals("bushy", value);
    
    // edit the configuration 
    provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("groupDnType");
    attribute.setValue("flat");
     
    provisionerConfiguration.editConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
    assertEquals(0, validationErrorsToDisplay.size());
    
    // retrieve values and confirm the updated values are coming back from the db
    provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("groupDnType");
    value = attribute.getValue();
    assertEquals("flat", value);
    
    // before we delete - let's set up some grouper sync data because
    // we want to make sure that sync data gets deleted when provisioner is deleted
    
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myLdapProvisioner");
    gcGrouperSync.setUserCount(10);
    gcGrouperSync.setGroupCount(20);
    gcGrouperSync.setRecordsCount(30);
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setLastFullSyncRun(new Timestamp(System.currentTimeMillis() - 100000));
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis() - 700000));
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
    gcGrouperSyncJob.setLastSyncIndex(135L);
    gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncJob.setSyncType("testSyncType");
    gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJob);
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("myId");
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncLog gcGrouperSyncLog = new GcGrouperSyncLog();
    gcGrouperSyncLog.setDescriptionToSave("desc");
    gcGrouperSyncLog.setJobTookMillis(1232);
    gcGrouperSyncLog.setRecordsChanged(12);
    gcGrouperSyncLog.setRecordsProcessed(23);
    gcGrouperSyncLog.setGrouperSyncId(gcGrouperSync.getId());
    gcGrouperSyncLog.setSyncTimestamp(new Timestamp(System.currentTimeMillis() - 2000));
    gcGrouperSyncLog.setGrouperSyncOwnerId(gcGrouperSyncJob.getId());
    gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
    
    
    provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    // verify first that values were actually stored in the database
    ProvisionerConfigSyncDetails syncDetails = provisionerConfiguration.getSyncDetails();
    
    assertEquals(20, syncDetails.getGroupCount());
    assertEquals(10, syncDetails.getUserCount());
    assertEquals(30, syncDetails.getRecordsCount());
    
    List<GrouperSyncJobWrapper> syncJobs = syncDetails.getSyncJobs();
    assertEquals(1, syncJobs.size());
    
    GcGrouperSyncJob grouperSyncJob = syncJobs.get(0).getGcGrouperSyncJob();
    assertEquals(135L, grouperSyncJob.getLastSyncIndex().longValue());
    assertEquals("testSyncType", grouperSyncJob.getSyncType());
    
    GcGrouperSyncLog grouperSyncLog = syncJobs.get(0).getGcGrouperSyncLog();
    assertEquals("desc", grouperSyncLog.getDescriptionOrDescriptionClob());
    
    provisionerConfiguration.deleteConfig(false); // delete the config
    
    provisionerConfiguration = new LdapProvisionerConfiguration();
    provisionerConfiguration.setConfigId("myLdapProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("ldapExternalSystemConfigId");
    value = attribute.getValue();
    assertEquals("", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    value = attribute.getValue();
    assertEquals("", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("provisioningType");
    value = attribute.getValue();
    assertEquals("", value);
    
    syncDetails = provisionerConfiguration.getSyncDetails();
    assertNull(syncDetails);
    
  }
  
  public void testSqlProvisionerConfigurationInsertEditDelete() {
    
    SqlProvisionerConfiguration provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    int attributesSize = provisionerConfiguration.retrieveAttributes().size();
    
    assertTrue(attributesSize > 0);
    
    List<GrouperConfigurationModuleSubSection> subSections = provisionerConfiguration.getSubSections();
    assertTrue(subSections.size() > 0);
    
    Set<String> subsectionsLabels = new HashSet<String>();
    subsectionsLabels.add("membership");
    subsectionsLabels.add("entity");
    subsectionsLabels.add("group");
    subsectionsLabels.add("assigningProvisioning");
    subsectionsLabels.add("advanced");
    
    for (GrouperConfigurationModuleSubSection subSection: subSections) {
      subsectionsLabels.contains(subSection.getLabel());
    }
    
    // set the required values so that the validation pass and values can be inserted into the db
    GrouperConfigurationModuleAttribute attribute = provisionerConfiguration.retrieveAttributes().get("userTableName");
    attribute.setValue("userTableName");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("groupTableName");
    attribute.setValue("groupTableName");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("membershipTableName");
    attribute.setValue("membership_table");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    attribute.setValue("mySubjectSource");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("dbExternalSystemConfigId");
    attribute.setValue("Database External System");
    
    // set the following two values so that we can test that some internal attributes are also being saved correctly in the db.
    attribute = provisionerConfiguration.retrieveAttributes().get("hasTargetEntityLink");
    attribute.setValue("true");

    attribute = provisionerConfiguration.retrieveAttributes().get("userPrimaryKey");
    attribute.setValue("user_pk");

    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    // insert config into db
    provisionerConfiguration.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
    
    assertEquals(0, validationErrorsToDisplay.size());
    
    provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    attributesSize = provisionerConfiguration.retrieveAttributes().size();
    
    assertTrue(attributesSize > 0);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("membershipTableName");
    String value = attribute.getValue();
    assertEquals("membership_table", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    value = attribute.getValue();
    assertEquals("mySubjectSource", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("dbExternalSystemConfigId");
    value = attribute.getValue();
    assertEquals("Database External System", value);
    
    
    // edit the configuration 
    provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("membershipTableName");
    attribute.setValue("membership_table_updated");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    value = attribute.getValue();
    attribute.setValue("mySubjectSourceUpdated");
    
    provisionerConfiguration.editConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
    assertEquals(0, validationErrorsToDisplay.size());
    
    // retrieve values and confirm the updated values are coming back from the db
    provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("membershipTableName");
    value = attribute.getValue();
    assertEquals("membership_table_updated", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    value = attribute.getValue();
    assertEquals("mySubjectSourceUpdated", value);
    
    // before we delete - let's set up some grouper sync data because
    // we want to make sure that sync data gets deleted when provisioner is deleted
    
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("mySqlProvisioner");
    gcGrouperSync.setUserCount(10);
    gcGrouperSync.setGroupCount(15);
    gcGrouperSync.setRecordsCount(20);
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setLastFullSyncRun(new Timestamp(System.currentTimeMillis() - 100000));
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis() - 700000));
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
    gcGrouperSyncJob.setLastSyncIndex(135L);
    gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncJob.setSyncType("testSyncType");
    gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJob);
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("myId");
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncLog gcGrouperSyncLog = new GcGrouperSyncLog();
    gcGrouperSyncLog.setDescriptionToSave("desc");
    gcGrouperSyncLog.setJobTookMillis(1232);
    gcGrouperSyncLog.setRecordsChanged(12);
    gcGrouperSyncLog.setRecordsProcessed(23);
    gcGrouperSyncLog.setSyncTimestamp(new Timestamp(System.currentTimeMillis() - 2000));
    gcGrouperSyncLog.setGrouperSyncId(gcGrouperSync.getId());
    gcGrouperSyncLog.setGrouperSyncOwnerId(gcGrouperSyncJob.getId());
    gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
    
    provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    // verify first that values were actually stored in the database
    ProvisionerConfigSyncDetails syncDetails = provisionerConfiguration.getSyncDetails();
    
    assertEquals(10, syncDetails.getUserCount());
    assertEquals(15, syncDetails.getGroupCount());
    assertEquals(20, syncDetails.getRecordsCount());
    
    List<GrouperSyncJobWrapper> syncJobs = syncDetails.getSyncJobs();
    assertEquals(1, syncJobs.size());
    
    GcGrouperSyncJob grouperSyncJob = syncJobs.get(0).getGcGrouperSyncJob();
    assertEquals(135L, grouperSyncJob.getLastSyncIndex().longValue());
    assertEquals("testSyncType", grouperSyncJob.getSyncType());
    
    GcGrouperSyncLog grouperSyncLog = syncJobs.get(0).getGcGrouperSyncLog();
    assertEquals("desc", grouperSyncLog.getDescriptionOrDescriptionClob());
    
    provisionerConfiguration.deleteConfig(false); // delete the config
    
    provisionerConfiguration = new SqlProvisionerConfiguration();
    provisionerConfiguration.setConfigId("mySqlProvisioner");
    
    attribute = provisionerConfiguration.retrieveAttributes().get("membershipTableName");
    value = attribute.getValue();
    assertEquals("", value);
    
    attribute = provisionerConfiguration.retrieveAttributes().get("subjectSourcesToProvision");
    value = attribute.getValue();
    assertEquals("", value);
    
    syncDetails = provisionerConfiguration.getSyncDetails();
    assertNull(syncDetails);
    
  }

}
