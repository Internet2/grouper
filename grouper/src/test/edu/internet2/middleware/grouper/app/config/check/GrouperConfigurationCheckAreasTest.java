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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.check.esb.EsbHttpListenerAuthFileConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.esb.EsbXmppListenerRemovedConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.messaging.MessagingAllowedGroupMissingConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.messaging.MessagingTrustAllCertificatesConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.provisioning.ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ui.UiAntiFramingDisabledConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.workflow.WorkflowOffWithDataConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsAllowJsonWrapperConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsDiagnosticSendDetailsOutsideConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsOauthAuthenticatorConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsOidcAccessTokenUserInfoOnlyConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsRestAllowWritesWithGetConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import junit.textui.TestRunner;

/**
 * GRP-7380 tests for the configuration review checks by area (ESB, WS, workflow, messaging, UI).
 * Each test drives the effective configuration through the override maps of the config classes
 * (grouper.properties, grouper-loader.properties, grouper-ws.properties, grouper-ui.properties,
 * grouper.client.properties) and asserts on the findings.  Every override goes through
 * {@link #override(ConfigPropertiesCascadeBase, String, String)} so tearDown removes all of them.
 * Assertions look only at findings for the keys a test set, so other config in the test
 * environment does not affect them.
 */
public class GrouperConfigurationCheckAreasTest extends GrouperTest {

  /** test OIDC external system config id */
  private static final String OIDC_CONFIG_ID = "configReviewTestOidc";

  /** test client version mapping key */
  private static final String CLIENT_VERSION_PROPERTY_NAME = "ws.security.authn.oauth.clientVersion.9.9.9.grouperOidcConfigId";

  /** test messaging system name */
  private static final String MESSAGING_SYSTEM_NAME = "configReviewTestMessaging";

  /** root session used to create test groups */
  private GrouperSession grouperSession;

  /** every override this test set, removed in tearDown */
  private List<Object[]> overrides = new ArrayList<Object[]>();

  /** temp file for the ESB auth file test, deleted in tearDown */
  private File tempFile;

  /**
   *
   */
  public GrouperConfigurationCheckAreasTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperConfigurationCheckAreasTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperConfigurationCheckAreasTest("testWsOauthAuthenticatorMappedConfigMissingSettingsError"));
  }

  @Override
  protected void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();
  }

  @Override
  protected void tearDown() {
    for (Object[] override : this.overrides) {
      ConfigPropertiesCascadeBase config = (ConfigPropertiesCascadeBase)override[0];
      config.propertiesOverrideMap().remove((String)override[1]);
    }
    this.overrides.clear();
    if (this.tempFile != null) {
      this.tempFile.delete();
      this.tempFile = null;
    }
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * set a config override and remember it for tearDown
   * @param config the config class instance
   * @param propertyName key
   * @param value value
   */
  private void override(ConfigPropertiesCascadeBase config, String propertyName, String value) {
    config.propertiesOverrideMap().put(propertyName, value);
    this.overrides.add(new Object[] {config, propertyName});
  }

  /**
   * @param results all findings of a check
   * @param propertyName the key of interest
   * @return the findings about that key
   */
  private static List<ConfigurationCheckResult> resultsForProperty(List<ConfigurationCheckResult> results, String propertyName) {
    List<ConfigurationCheckResult> filtered = new ArrayList<ConfigurationCheckResult>();
    for (ConfigurationCheckResult result : results) {
      if (propertyName.equals(result.getPropertyName())) {
        filtered.add(result);
      }
    }
    return filtered;
  }

  /**
   * @param results all findings of a check
   * @param fragment text the key contains
   * @return the findings whose key contains the fragment
   */
  private static List<ConfigurationCheckResult> resultsForPropertyContaining(List<ConfigurationCheckResult> results, String fragment) {
    List<ConfigurationCheckResult> filtered = new ArrayList<ConfigurationCheckResult>();
    for (ConfigurationCheckResult result : results) {
      if (result.getPropertyName() != null && result.getPropertyName().contains(fragment)) {
        filtered.add(result);
      }
    }
    return filtered;
  }

  /**
   * @param results the findings
   * @param severity the severity that should be the only finding
   */
  private static void assertOneResultOfSeverity(List<ConfigurationCheckResult> results, ConfigurationCheckSeverity severity) {
    assertEquals("expected exactly one finding but got " + results, 1, results.size());
    assertEquals(severity, results.get(0).getSeverity());
  }

  /**
   * @param results the findings
   */
  private static void assertNoResults(List<ConfigurationCheckResult> results) {
    assertEquals("expected no findings but got " + results, 0, results.size());
  }

  // ---------------------------------------------------------------------------------------------
  // ESB
  // ---------------------------------------------------------------------------------------------

  /**
   * the removed XMPP listener still enabled is an error, and a leftover key is a warning
   */
  public void testEsbXmppListenerEnabledError() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    override(config, EsbXmppListenerRemovedConfigurationCheck.ENABLE_PROPERTY_NAME, "true");
    override(config, "esb.listeners.xmpp.server", "xmpp.example.edu");
    List<ConfigurationCheckResult> results = new EsbXmppListenerRemovedConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, EsbXmppListenerRemovedConfigurationCheck.ENABLE_PROPERTY_NAME),
        ConfigurationCheckSeverity.ERROR);
    assertOneResultOfSeverity(resultsForProperty(results, "esb.listeners.xmpp.server"), ConfigurationCheckSeverity.WARNING);
  }

  /**
   * XMPP listener off with no leftover values: no finding
   */
  public void testEsbXmppListenerOffNoFinding() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    override(config, EsbXmppListenerRemovedConfigurationCheck.ENABLE_PROPERTY_NAME, "false");
    override(config, "esb.listeners.xmpp.server", "");
    List<ConfigurationCheckResult> results = new EsbXmppListenerRemovedConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, EsbXmppListenerRemovedConfigurationCheck.ENABLE_PROPERTY_NAME));
    assertNoResults(resultsForProperty(results, "esb.listeners.xmpp.server"));
  }

  /**
   * ESB HTTP listener on with a blank auth file is an error
   */
  public void testEsbHttpListenerBlankAuthFileError() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    override(config, EsbHttpListenerAuthFileConfigurationCheck.ENABLE_PROPERTY_NAME, "true");
    override(config, EsbHttpListenerAuthFileConfigurationCheck.AUTH_CONFIG_FILE_PROPERTY_NAME, "");
    List<ConfigurationCheckResult> results = new EsbHttpListenerAuthFileConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.ERROR);
  }

  /**
   * ESB HTTP listener on with an auth file that does not exist is an error
   */
  public void testEsbHttpListenerMissingAuthFileError() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    override(config, EsbHttpListenerAuthFileConfigurationCheck.ENABLE_PROPERTY_NAME, "true");
    override(config, EsbHttpListenerAuthFileConfigurationCheck.AUTH_CONFIG_FILE_PROPERTY_NAME,
        "/configReviewTestDoesNotExist/esbRealm.properties");
    List<ConfigurationCheckResult> results = new EsbHttpListenerAuthFileConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(results, ConfigurationCheckSeverity.ERROR);
  }

  /**
   * ESB HTTP listener on with a readable auth file, or off: no finding
   * @throws Exception
   */
  public void testEsbHttpListenerReadableAuthFileNoFinding() throws Exception {
    this.tempFile = File.createTempFile("configReviewEsbRealm", ".properties");
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    override(config, EsbHttpListenerAuthFileConfigurationCheck.ENABLE_PROPERTY_NAME, "true");
    override(config, EsbHttpListenerAuthFileConfigurationCheck.AUTH_CONFIG_FILE_PROPERTY_NAME, this.tempFile.getAbsolutePath());
    assertNoResults(new EsbHttpListenerAuthFileConfigurationCheck().checkConfiguration());

    override(config, EsbHttpListenerAuthFileConfigurationCheck.ENABLE_PROPERTY_NAME, "false");
    override(config, EsbHttpListenerAuthFileConfigurationCheck.AUTH_CONFIG_FILE_PROPERTY_NAME, "");
    assertNoResults(new EsbHttpListenerAuthFileConfigurationCheck().checkConfiguration());
  }

  // ---------------------------------------------------------------------------------------------
  // WS OAuth
  // ---------------------------------------------------------------------------------------------

  /**
   * point WS at the OAuth authenticator and map a client version to the test OIDC config
   */
  private void configureWsOauthWithMapping() {
    GrouperWsConfigInApi wsConfig = GrouperWsConfigInApi.retrieveConfig();
    override(wsConfig, WsOauthAuthenticatorConfigurationCheck.AUTHENTICATION_CLASS_PROPERTY_NAME,
        WsOauthAuthenticatorConfigurationCheck.OAUTH_AUTHENTICATION_CLASS_NAME);
    override(wsConfig, CLIENT_VERSION_PROPERTY_NAME, OIDC_CONFIG_ID);
    // a key so the OIDC config exists
    override(GrouperConfig.retrieveConfig(), "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".clientId", "grouperWs");
  }

  /**
   * mapped OIDC config without useForWs and wsAccessTokenValidation: an error for each
   */
  public void testWsOauthAuthenticatorMappedConfigMissingSettingsError() {
    configureWsOauthWithMapping();
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".useForWs", "false");
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".wsAccessTokenValidation", "");
    List<ConfigurationCheckResult> results = new WsOauthAuthenticatorConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".useForWs"),
        ConfigurationCheckSeverity.ERROR);
    assertOneResultOfSeverity(resultsForProperty(results, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".wsAccessTokenValidation"),
        ConfigurationCheckSeverity.ERROR);
  }

  /**
   * a client version mapped to an OIDC config that does not exist is an error
   */
  public void testWsOauthAuthenticatorMappedConfigNotFoundError() {
    GrouperWsConfigInApi wsConfig = GrouperWsConfigInApi.retrieveConfig();
    override(wsConfig, WsOauthAuthenticatorConfigurationCheck.AUTHENTICATION_CLASS_PROPERTY_NAME,
        WsOauthAuthenticatorConfigurationCheck.OAUTH_AUTHENTICATION_CLASS_NAME);
    override(wsConfig, CLIENT_VERSION_PROPERTY_NAME, "configReviewTestOidcDoesNotExist");
    List<ConfigurationCheckResult> results = new WsOauthAuthenticatorConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, CLIENT_VERSION_PROPERTY_NAME), ConfigurationCheckSeverity.ERROR);
  }

  /**
   * mapped OIDC config with every WS setting: no finding for it
   */
  public void testWsOauthAuthenticatorMappedConfigCompleteNoFinding() {
    configureWsOauthWithMapping();
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".useForWs", "true");
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".claimSource", "userInfoEndpoint");
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".wsAccessTokenValidation", "jwt");
    override(grouperConfig, "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".wsAccessTokenAudiences", "grouperWs");
    List<ConfigurationCheckResult> results = new WsOauthAuthenticatorConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForPropertyContaining(results, OIDC_CONFIG_ID));
  }

  /**
   * no OAuth authenticator: the check does not look at OIDC configs at all
   */
  public void testWsOauthAuthenticatorNotOauthNoFinding() {
    override(GrouperWsConfigInApi.retrieveConfig(), WsOauthAuthenticatorConfigurationCheck.AUTHENTICATION_CLASS_PROPERTY_NAME, "");
    override(GrouperConfig.retrieveConfig(), "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".useForWs", "true");
    assertNoResults(new WsOauthAuthenticatorConfigurationCheck().checkConfiguration());
  }

  /**
   * wsAccessTokenValidation=userInfoOnly is a warning, jwt is not
   */
  public void testWsOidcUserInfoOnly() {
    String propertyName = "grouper.oidcExternalSystem." + OIDC_CONFIG_ID + ".wsAccessTokenValidation";
    override(GrouperConfig.retrieveConfig(), propertyName, "userInfoOnly");
    List<ConfigurationCheckResult> results = new WsOidcAccessTokenUserInfoOnlyConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, propertyName), ConfigurationCheckSeverity.WARNING);

    override(GrouperConfig.retrieveConfig(), propertyName, "jwt");
    results = new WsOidcAccessTokenUserInfoOnlyConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, propertyName));
  }

  // ---------------------------------------------------------------------------------------------
  // WS settings
  // ---------------------------------------------------------------------------------------------

  /**
   * ws.allowJsonWrapper=true is a warning, false is not
   */
  public void testWsAllowJsonWrapper() {
    override(GrouperWsConfigInApi.retrieveConfig(), WsAllowJsonWrapperConfigurationCheck.PROPERTY_NAME, "true");
    assertOneResultOfSeverity(new WsAllowJsonWrapperConfigurationCheck().checkConfiguration(), ConfigurationCheckSeverity.WARNING);
    override(GrouperWsConfigInApi.retrieveConfig(), WsAllowJsonWrapperConfigurationCheck.PROPERTY_NAME, "false");
    assertNoResults(new WsAllowJsonWrapperConfigurationCheck().checkConfiguration());
  }

  /**
   * ws.rest.allowWritesWithGet=true is a warning, blank (default false) is not
   */
  public void testWsRestAllowWritesWithGet() {
    override(GrouperWsConfigInApi.retrieveConfig(), WsRestAllowWritesWithGetConfigurationCheck.PROPERTY_NAME, "true");
    assertOneResultOfSeverity(new WsRestAllowWritesWithGetConfigurationCheck().checkConfiguration(), ConfigurationCheckSeverity.WARNING);
    override(GrouperWsConfigInApi.retrieveConfig(), WsRestAllowWritesWithGetConfigurationCheck.PROPERTY_NAME, "");
    assertNoResults(new WsRestAllowWritesWithGetConfigurationCheck().checkConfiguration());
  }

  /**
   * ws.diagnostic.sendDetailsOutsideSourceIpAddresses=true is a warning, false is not
   */
  public void testWsDiagnosticSendDetailsOutside() {
    override(GrouperConfig.retrieveConfig(), WsDiagnosticSendDetailsOutsideConfigurationCheck.PROPERTY_NAME, "true");
    assertOneResultOfSeverity(new WsDiagnosticSendDetailsOutsideConfigurationCheck().checkConfiguration(), ConfigurationCheckSeverity.WARNING);
    override(GrouperConfig.retrieveConfig(), WsDiagnosticSendDetailsOutsideConfigurationCheck.PROPERTY_NAME, "false");
    assertNoResults(new WsDiagnosticSendDetailsOutsideConfigurationCheck().checkConfiguration());
  }

  // ---------------------------------------------------------------------------------------------
  // workflow
  // ---------------------------------------------------------------------------------------------

  /**
   * a workflow check whose database count is fixed
   * @param count the count to report
   * @return the check
   */
  private static WorkflowOffWithDataConfigurationCheck workflowCheckWithCount(final long count) {
    return new WorkflowOffWithDataConfigurationCheck() {

      @Override
      protected long retrieveWorkflowAttributeAssignCount() {
        return count;
      }
    };
  }

  /**
   * schedule (or unschedule) both workflow daemons
   * @param quartzCron cron, or blank to disable
   */
  private void scheduleWorkflowDaemons(String quartzCron) {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    for (String daemonName : WorkflowOffWithDataConfigurationCheck.WORKFLOW_DAEMON_NAMES) {
      override(config, "otherJob." + daemonName + ".quartzCron", quartzCron);
    }
  }

  /**
   * workflow off, workflow data, daemons scheduled: an error per daemon
   */
  public void testWorkflowOffWithDataDaemonsOnError() {
    override(GrouperConfig.retrieveConfig(), WorkflowOffWithDataConfigurationCheck.WORKFLOW_ENABLE_PROPERTY_NAME, "false");
    scheduleWorkflowDaemons("0 0 4 * * ?");
    List<ConfigurationCheckResult> results = workflowCheckWithCount(3).checkConfiguration();
    assertEquals("expected one finding per daemon but got " + results, 2, results.size());
    for (ConfigurationCheckResult result : results) {
      assertEquals(ConfigurationCheckSeverity.ERROR, result.getSeverity());
    }
  }

  /**
   * workflow on, or no data, or daemons off: no finding.  Also runs the real database query once
   */
  public void testWorkflowOffWithDataNoFinding() {
    override(GrouperConfig.retrieveConfig(), WorkflowOffWithDataConfigurationCheck.WORKFLOW_ENABLE_PROPERTY_NAME, "true");
    scheduleWorkflowDaemons("0 0 4 * * ?");
    assertNoResults(workflowCheckWithCount(3).checkConfiguration());

    override(GrouperConfig.retrieveConfig(), WorkflowOffWithDataConfigurationCheck.WORKFLOW_ENABLE_PROPERTY_NAME, "false");
    assertNoResults(workflowCheckWithCount(0).checkConfiguration());

    scheduleWorkflowDaemons("");
    assertNoResults(workflowCheckWithCount(3).checkConfiguration());

    // the real query runs without error (the count depends on the test database)
    scheduleWorkflowDaemons("0 0 4 * * ?");
    assertNotNull(new WorkflowOffWithDataConfigurationCheck().checkConfiguration());
  }

  // ---------------------------------------------------------------------------------------------
  // messaging
  // ---------------------------------------------------------------------------------------------

  /**
   * allowedGroupName naming a group that does not exist is an error, an existing group is not
   */
  public void testMessagingAllowedGroupMissing() {
    String propertyName = "grouper.messaging.system." + MESSAGING_SYSTEM_NAME + ".allowedGroupName";
    override(GrouperConfig.retrieveConfig(), propertyName, "test:configReviewDoesNotExistMessagingGroup");
    List<ConfigurationCheckResult> results = new MessagingAllowedGroupMissingConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, propertyName), ConfigurationCheckSeverity.ERROR);

    Group group = new GroupSave(this.grouperSession).assignName("test:configReviewMessagingGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    override(GrouperConfig.retrieveConfig(), propertyName, group.getName());
    results = new MessagingAllowedGroupMissingConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, propertyName));
  }

  /**
   * trustAllCertificates=true is a warning; false, or a configured trust store, is not
   */
  public void testMessagingTrustAllCertificates() {
    GrouperClientConfig config = GrouperClientConfig.retrieveConfig();
    String prefix = "grouper.messaging.system." + MESSAGING_SYSTEM_NAME + ".";
    String propertyName = prefix + "trustAllCertificates";
    override(config, propertyName, "true");
    List<ConfigurationCheckResult> results = new MessagingTrustAllCertificatesConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, propertyName), ConfigurationCheckSeverity.WARNING);

    // a trust store wins over trustAllCertificates
    override(config, prefix + "pathToTrustStore", "/configReviewTest/truststore.jks");
    override(config, prefix + "trustPassphrase", "configReviewTest");
    results = new MessagingTrustAllCertificatesConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, propertyName));

    override(config, prefix + "pathToTrustStore", "");
    override(config, propertyName, "false");
    results = new MessagingTrustAllCertificatesConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, propertyName));
  }

  // ---------------------------------------------------------------------------------------------
  // UI
  // ---------------------------------------------------------------------------------------------

  /**
   * grouperUi.antiFraming.enabled=false is a warning, true is not
   */
  public void testUiAntiFramingDisabled() {
    override(GrouperUiConfigInApi.retrieveConfig(), UiAntiFramingDisabledConfigurationCheck.PROPERTY_NAME, "false");
    assertOneResultOfSeverity(new UiAntiFramingDisabledConfigurationCheck().checkConfiguration(), ConfigurationCheckSeverity.WARNING);
    override(GrouperUiConfigInApi.retrieveConfig(), UiAntiFramingDisabledConfigurationCheck.PROPERTY_NAME, "true");
    assertNoResults(new UiAntiFramingDisabledConfigurationCheck().checkConfiguration());
  }

  /**
   * a provisioner that trusts the target to re-link entities is a warning
   */
  public void testProvisionerTrustTargetToRelinkEntitiesWarning() {
    String propertyName = "provisioner.configReviewTestProvisioner." + ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck.PROPERTY_SUFFIX;
    override(GrouperLoaderConfig.retrieveConfig(), propertyName, "true");
    List<ConfigurationCheckResult> results = new ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck().checkConfiguration();
    assertOneResultOfSeverity(resultsForProperty(results, propertyName), ConfigurationCheckSeverity.WARNING);
  }

  /**
   * the default (off) is no finding
   */
  public void testProvisionerTrustTargetToRelinkEntitiesOffNoFinding() {
    String propertyName = "provisioner.configReviewTestProvisioner." + ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck.PROPERTY_SUFFIX;
    override(GrouperLoaderConfig.retrieveConfig(), propertyName, "false");
    List<ConfigurationCheckResult> results = new ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck().checkConfiguration();
    assertNoResults(resultsForProperty(results, propertyName));
  }

}
