package edu.internet2.middleware.grouper.app.workflow;

import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix 113: workflow snapshot storage names must be unique per instance, so two
 * instances of the same workflow saved in the same state in the same second neither fail
 * (database unique file_path) nor overwrite each other (S3, file system).
 * Before the fix this does not compile (GrouperWorkflowFileNames is missing); the storage
 * cases fail when the two saves land in the same second (database: unique index error; file
 * system: one file, the first instance cannot be decrypted).
 * S3 is not tested (no mock); it uses the same name helper.
 * </pre>
 */
public class GrouperWorkflowFileNamesTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** owner group */
  private Group ownerGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowFileNamesTest("testDatabaseStorageSameSecond"));
  }

  /**
   */
  public GrouperWorkflowFileNamesTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowFileNamesTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    this.grouperSession = grouperSessionResult.getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");

    this.ownerGroup = new GroupSave(this.grouperSession).assignName("test:wf:files").assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.storage.option");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.file.system.path");
    super.tearDown();
  }

  /**
   * names differ by instance uuid even with the same config, state and time
   */
  public void testFileNameUnique() {
    Date date = new Date();
    String name1 = GrouperWorkflowFileNames.fileName("wf", "uuid1", "initiate", date);
    String name2 = GrouperWorkflowFileNames.fileName("wf", "uuid2", "initiate", date);
    assertFalse(name1.equals(name2));
    assertTrue(name1, name1.startsWith("wf_uuid1_initiate_"));

    // blank uuid (should not happen) still gives distinct names
    String blank1 = GrouperWorkflowFileNames.fileName("wf", null, "initiate", date);
    String blank2 = GrouperWorkflowFileNames.fileName("wf", "", "initiate", date);
    assertFalse(blank1.equals(blank2));
  }

  /**
   * save a config on the owner group
   * @return the stored config
   */
  private GrouperWorkflowConfig saveConfig() {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.ownerGroup.getId())));
    config.setOwnerGroup(this.ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled(toStringTrueFalse(true));
    // initiate builds the html from this form and needs an element per default param
    // (an empty form NPEs in GrouperWorkflowConfig.buildHtmlFromConfigForm)
    config.setWorkflowConfigForm(GrouperTextContainer.retrieveFromRequest().getText().get("workflowConfigDefaultHtmlForm"));
    config.setWorkflowConfigId("wfFiles");
    config.setWorkflowConfigName("wfFiles");
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, this.ownerGroup);
    GrouperWorkflowConfig stored = GrouperWorkflowConfigService.getWorkflowConfig(this.ownerGroup, "wfFiles");
    assertNotNull(stored);
    return stored;
  }

  /**
   * two initiates back to back (same second in practice), both stored, distinct names, both decrypt
   */
  private void twoInstancesSameSecond() {
    GrouperWorkflowConfig config = saveConfig();

    GrouperWorkflowInstanceService.saveInitiateStateInstance(config, SubjectTestHelper.SUBJ0,
        new HashMap<GrouperWorkflowConfigParam, String>(), this.ownerGroup);
    GrouperWorkflowInstanceService.saveInitiateStateInstance(config, SubjectTestHelper.SUBJ1,
        new HashMap<GrouperWorkflowConfigParam, String>(), this.ownerGroup);

    List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(this.ownerGroup, "wfFiles");
    assertEquals(2, instances.size());

    Set<String> fileNames = new HashSet<String>();
    Set<String> filePointers = new HashSet<String>();
    for (GrouperWorkflowInstance instance : instances) {
      GrouperWorkflowInstanceFileInfo fileInfo = instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().get(0);
      filePointers.add(fileInfo.getFilePointer());
      if (fileInfo.getFileName() != null) {
        fileNames.add(fileInfo.getFileName());
        assertTrue(fileInfo.getFileName(), fileInfo.getFileName().contains(instance.getWorkflowInstanceUuid()));
      }
      // each instance decrypts its own snapshot
      String html = GrouperWorkflowInstanceService.getCurrentHtmlContent(instance);
      assertTrue(html, GrouperUtil.isNotBlank(html));
    }
    assertEquals(2, filePointers.size());
  }

  /**
   * default database storage
   */
  public void testDatabaseStorageSameSecond() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.storage.option", "database");
    twoInstancesSameSecond();
  }

  /**
   * file system storage, no overwrite
   */
  public void testFileSystemStorageSameSecond() {
    File dir = new File(GrouperUtil.tmpDir(true) + "grouperWorkflowFileNamesTest_" + System.currentTimeMillis());
    GrouperUtil.mkdirs(dir);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.storage.option", "fileSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.file.system.path", dir.getAbsolutePath());
    try {
      twoInstancesSameSecond();
      assertEquals(2, dir.listFiles().length);
    } finally {
      for (File file : GrouperUtil.nonNull(dir.listFiles(), File.class)) {
        GrouperUtil.deleteFile(file);
      }
      GrouperUtil.deleteFile(dir);
    }
  }
}
