/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.config.check;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesAccessToApiInElConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesRestrictRulesUiConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesSendEmailGateConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import junit.textui.TestRunner;

/**
 * SECURITY (GRP-7380) tests for the configuration review checks.  Each test drives the effective
 * configuration with GrouperConfig.propertiesOverrideMap() and asserts on the findings a check
 * returns, so no servlet container or UI is needed.
 */
public class GrouperConfigurationCheckTest extends GrouperTest {

  /** root session used to create test groups */
  private GrouperSession grouperSession;

  /**
   *
   */
  public GrouperConfigurationCheckTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperConfigurationCheckTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperConfigurationCheckTest("testRestrictRulesUiBlankWarning"));
  }

  @Override
  protected void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();
  }

  @Override
  protected void tearDown() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    config.propertiesOverrideMap().remove(RulesRestrictRulesUiConfigurationCheck.PROPERTY_NAME);
    config.propertiesOverrideMap().remove(RulesSendEmailGateConfigurationCheck.EMAIL_SENDER_PROPERTY_NAME);
    config.propertiesOverrideMap().remove(RulesSendEmailGateConfigurationCheck.ALLOW_SEND_EMAIL_PROPERTY_NAME);
    config.propertiesOverrideMap().remove(RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME);
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * @param name group name to create under a test stem
   * @return the created group
   */
  private Group createGroup(String name) {
    return new GroupSave(this.grouperSession).assignName(name)
        .assignCreateParentStemsIfNotExist(true).save();
  }

  /**
   * @param results the findings
   * @param severity the severity that should be the only finding
   */
  private void assertOneResultOfSeverity(List<ConfigurationCheckResult> results, ConfigurationCheckSeverity severity) {
    assertEquals("expected exactly one finding but got " + results, 1, results.size());
    assertEquals(severity, results.get(0).getSeverity());
  }

  /**
   * blank rules-UI restrict group is a warning (recommended, not required)
   */
  public void testRestrictRulesUiBlankWarning() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(RulesRestrictRulesUiConfigurationCheck.PROPERTY_NAME, "");
    List<ConfigurationCheckResult> results = new RulesRestrictRulesUiConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.WARNING);
  }

  /**
   * rules-UI restrict group pointing at a non-existent group is an error (rule editing fails closed)
   */
  public void testRestrictRulesUiMissingGroupError() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesRestrictRulesUiConfigurationCheck.PROPERTY_NAME, "test:doesNotExistRestrictGroup");
    List<ConfigurationCheckResult> results = new RulesRestrictRulesUiConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.ERROR);
  }

  /**
   * rules-UI restrict group pointing at an existing group is fine (no finding)
   */
  public void testRestrictRulesUiExistingGroupNoFinding() {
    Group group = createGroup("test:reviewRestrictGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesRestrictRulesUiConfigurationCheck.PROPERTY_NAME, group.getName());
    List<ConfigurationCheckResult> results = new RulesRestrictRulesUiConfigurationCheck().checkConfiguration();
    assertEquals("expected no findings but got " + results, 0, results.size());
  }

  /**
   * both sendEmail gates blank (and no opt-out) warns that no trusted population is designated
   */
  public void testSendEmailGateBothBlankWarning() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.EMAIL_SENDER_PROPERTY_NAME, "");
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.RESTRICT_RULES_UI_PROPERTY_NAME, "");
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.ALLOW_SEND_EMAIL_PROPERTY_NAME, "false");
    List<ConfigurationCheckResult> results = new RulesSendEmailGateConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.WARNING);
  }

  /**
   * an email-sender group that exists designates the trusted population, so there is no finding
   */
  public void testSendEmailGateEmailGroupExistsNoFinding() {
    Group group = createGroup("test:reviewEmailSenderGroup");
    GrouperConfig config = GrouperConfig.retrieveConfig();
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.EMAIL_SENDER_PROPERTY_NAME, group.getName());
    List<ConfigurationCheckResult> results = new RulesSendEmailGateConfigurationCheck().checkConfiguration();
    assertEquals("expected no findings but got " + results, 0, results.size());
  }

  /**
   * an email-sender group that does not exist fails closed and is an error
   */
  public void testSendEmailGateEmailGroupMissingError() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesSendEmailGateConfigurationCheck.EMAIL_SENDER_PROPERTY_NAME, "test:doesNotExistEmailGroup");
    List<ConfigurationCheckResult> results = new RulesSendEmailGateConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.ERROR);
  }

  /**
   * blank accessToApiInEl.group grants nothing extra, so there is no finding
   */
  public void testAccessToApiInElBlankNoFinding() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME, "");
    List<ConfigurationCheckResult> results = new RulesAccessToApiInElConfigurationCheck().checkConfiguration();
    assertEquals("expected no findings but got " + results, 0, results.size());
  }

  /**
   * accessToApiInEl.group pointing at a non-existent group is an error
   */
  public void testAccessToApiInElMissingGroupError() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME, "test:doesNotExistApiGroup");
    List<ConfigurationCheckResult> results = new RulesAccessToApiInElConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.ERROR);
  }

  /**
   * an accessToApiInEl.group whose members are all sysadmins (wheel/root) is fine
   */
  public void testAccessToApiInElAllSysadminsNoFinding() {
    Group group = createGroup("test:reviewApiSysadminGroup");
    group.addMember(SubjectFinder.findRootSubject());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME, group.getName());
    List<ConfigurationCheckResult> results = new RulesAccessToApiInElConfigurationCheck().checkConfiguration();
    assertEquals("expected no findings but got " + results, 0, results.size());
  }

  /**
   * an accessToApiInEl.group with a non-sysadmin member is a warning (only sysadmins should be in it)
   */
  public void testAccessToApiInElNonSysadminMemberWarning() {
    Group group = createGroup("test:reviewApiNonSysadminGroup");
    group.addMember(SubjectTestHelper.SUBJ0);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME, group.getName());
    List<ConfigurationCheckResult> results = new RulesAccessToApiInElConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.WARNING);
  }

  /**
   * an accessToApiInEl.group that contains EveryEntity is a warning (EveryEntity is not a sysadmin)
   */
  public void testAccessToApiInElEveryEntityWarning() {
    Group group = createGroup("test:reviewApiEveryEntityGroup");
    group.addMember(SubjectFinder.findAllSubject());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        RulesAccessToApiInElConfigurationCheck.PROPERTY_NAME, group.getName());
    List<ConfigurationCheckResult> results = new RulesAccessToApiInElConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.WARNING);
  }

  /**
   * the engine aggregates findings across checks and never returns null
   */
  public void testEngineAggregatesFindings() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    config.propertiesOverrideMap().put(RulesRestrictRulesUiConfigurationCheck.PROPERTY_NAME, "");
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.EMAIL_SENDER_PROPERTY_NAME, "");
    config.propertiesOverrideMap().put(RulesSendEmailGateConfigurationCheck.ALLOW_SEND_EMAIL_PROPERTY_NAME, "false");
    List<ConfigurationCheckResult> results = GrouperConfigurationCheckEngine.checkConfiguration();
    assertNotNull(results);
    // at least the blank rules-UI warning and the blank sendEmail-gate warning
    assertTrue("expected at least two findings but got " + results, results.size() >= 2);
  }

}
