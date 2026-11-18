package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * Workflow is off unless workflow.enable is true.  The code default is false and the base
 * properties default is blank (so the code default applies).  While workflow is off no workflow
 * config is read or saved through GrouperWorkflowConfigService, which covers the UI screens that
 * do not call checkWorkflow() (the join group form goes through getWorkflowConfigs) and the
 * instance code (getWorkflowConfig by marker id).
 *
 * Before the fix: testCodeDefaultOff fails (code default true) and the refusal tests fail (the
 * config service has no check).  testBaseDefaultOff needs the fixed grouper.base.properties on
 * the classpath (mainline, or the patched release file); with an unpatched release base file
 * (workflow.enable = true) it fails.
 * </pre>
 */
public class GrouperWorkflowDefaultOffTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** group that gets the workflow config */
  private Group ownerGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowDefaultOffTest("testOffRefusesAndHidesConfigs"));
  }

  /**
   */
  public GrouperWorkflowDefaultOffTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowDefaultOffTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    this.grouperSession = grouperSessionResult.getGrouperSession();

    this.ownerGroup = new GroupSave(this.grouperSession).assignName("test:wfOff:owner")
        .assignCreateParentStemsIfNotExist(true).save();
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.enable");
    super.tearDown();
  }

  /**
   * turn workflow on or off for this test
   * @param value true, false, or "" for blank (the code default applies)
   */
  private static void setWorkflowEnable(String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", value);
  }

  /**
   * a config like the UI builds
   * @param workflowId
   * @return the config (not saved)
   */
  private GrouperWorkflowConfig newConfig(String workflowId) {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.ownerGroup.getId())));
    config.setOwnerGroup(this.ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled("true");
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName(workflowId);
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    return config;
  }

  /**
   * assert a runtime exception names the property
   * @param re
   */
  private static void assertNamesProperty(RuntimeException re) {
    String stack = GrouperUtil.getFullStackTrace(re);
    assertTrue(stack, stack.contains("workflow.enable"));
  }

  /**
   * blank value: the code default is off
   */
  public void testCodeDefaultOff() {
    setWorkflowEnable("");
    assertFalse(GrouperWorkflowSettings.workflowEnabled());
    setWorkflowEnable("true");
    assertTrue(GrouperWorkflowSettings.workflowEnabled());
  }

  /**
   * no override: the base properties leave it blank, so it is off
   * (needs the fixed grouper.base.properties on the classpath)
   */
  public void testBaseDefaultOff() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.enable");
    assertFalse(GrouperWorkflowSettings.workflowEnabled());
  }

  /**
   * off: saving a config is refused
   */
  public void testOffRefusesSave() {
    setWorkflowEnable("false");
    try {
      GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(newConfig("wfOffSave"), this.ownerGroup);
      fail("save must be refused while workflow is off");
    } catch (RuntimeException re) {
      assertNamesProperty(re);
    }
    // nothing was stored: turn it on and look
    setWorkflowEnable("true");
    assertNull(GrouperWorkflowConfigService.getWorkflowConfig(this.ownerGroup, "wfOffSave"));
    assertFalse(GrouperWorkflowConfigService.workflowIdExists("wfOffSave"));
  }

  /**
   * a config saved while on is not visible or usable while off, and is back when on again
   */
  public void testOffRefusesAndHidesConfigs() {
    setWorkflowEnable("true");
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(newConfig("wfOffHide"), this.ownerGroup);
    GrouperWorkflowConfig stored = GrouperWorkflowConfigService.getWorkflowConfig(this.ownerGroup, "wfOffHide");
    assertNotNull(stored);
    String markerId = stored.getAttributeAssignmentMarkerId();
    assertNotNull(markerId);
    assertEquals(1, GrouperWorkflowConfigService.getWorkflowConfigs(this.ownerGroup).size());
    assertTrue(GrouperWorkflowConfigService.workflowIdExists("wfOffHide"));

    // off: the join group form (getWorkflowConfigs) finds nothing, lookups find nothing
    setWorkflowEnable("false");
    List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(this.ownerGroup);
    assertEquals(0, configs.size());
    assertNull(GrouperWorkflowConfigService.getWorkflowConfig(this.ownerGroup, "wfOffHide"));
    assertFalse(GrouperWorkflowConfigService.workflowIdExists("wfOffHide"));

    // by marker id (initiate submit, instances, daemon): refused with a clear message
    try {
      GrouperWorkflowConfigService.getWorkflowConfig(markerId);
      fail("lookup by marker id must be refused while workflow is off");
    } catch (RuntimeException re) {
      assertNamesProperty(re);
    }

    // changing it is refused too
    try {
      GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(newConfig("wfOffHide"), this.ownerGroup);
      fail("update must be refused while workflow is off");
    } catch (RuntimeException re) {
      assertNamesProperty(re);
    }

    // blank (the default) behaves like false
    setWorkflowEnable("");
    assertEquals(0, GrouperWorkflowConfigService.getWorkflowConfigs(this.ownerGroup).size());

    // on again: everything is back
    setWorkflowEnable("true");
    assertEquals(1, GrouperWorkflowConfigService.getWorkflowConfigs(this.ownerGroup).size());
    assertNotNull(GrouperWorkflowConfigService.getWorkflowConfig(markerId));
    assertTrue(GrouperWorkflowConfigService.workflowIdExists("wfOffHide"));
  }
}
