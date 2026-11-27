package edu.internet2.middleware.grouper.app.config.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.config.check.data.ObjectIdNotAllowedConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.esb.EsbHttpListenerAuthFileConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.esb.EsbXmppListenerRemovedConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.membershipRequire.MembershipRequireConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.oauth.OAuthRedirectUriMisspelledConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.messaging.MessagingAllowedGroupMissingConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.messaging.MessagingHostnameVerificationOffConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.messaging.MessagingTrustAllCertificatesConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesAccessToApiInElConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesElSandboxConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesRestrictRulesUiConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.rules.RulesSendEmailGateConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.provisioning.ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.provisioning.ScimUserGroupsConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ui.UiAntiFramingDisabledConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.workflow.WorkflowOffWithDataConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsAllowJsonWrapperConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsDiagnosticSendDetailsOutsideConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsOauthAuthenticatorConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsOidcAccessTokenUserInfoOnlyConfigurationCheck;
import edu.internet2.middleware.grouper.app.config.check.ws.WsRestAllowWritesWithGetConfigurationCheck;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runs the configuration review checks and aggregates their findings.  This is the single entry point
 * for every consumer (UI, web services, GSH): call {@link #checkConfiguration()}.
 *
 * New checks are registered by adding them to {@link #builtInChecks()}, which doubles as the readable
 * catalog of everything that is checked.
 */
public class GrouperConfigurationCheckEngine {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperConfigurationCheckEngine.class);

  /**
   * the built-in checks, in no particular order (the results are sorted by severity afterwards).  Add
   * new checks here.
   * @return the checks to run
   */
  private static List<GrouperConfigurationCheck> builtInChecks() {
    List<GrouperConfigurationCheck> checks = new ArrayList<GrouperConfigurationCheck>();

    checks.add(new RulesRestrictRulesUiConfigurationCheck());
    checks.add(new RulesSendEmailGateConfigurationCheck());
    checks.add(new RulesAccessToApiInElConfigurationCheck());
    checks.add(new RulesElSandboxConfigurationCheck());

    // ESB listeners (grouper-loader.properties)
    checks.add(new EsbXmppListenerRemovedConfigurationCheck());
    checks.add(new EsbHttpListenerAuthFileConfigurationCheck());

    // web services (grouper-ws.properties, and OIDC / diagnostic settings in grouper.properties)
    checks.add(new WsOauthAuthenticatorConfigurationCheck());
    checks.add(new WsOidcAccessTokenUserInfoOnlyConfigurationCheck());
    checks.add(new WsAllowJsonWrapperConfigurationCheck());
    checks.add(new WsRestAllowWritesWithGetConfigurationCheck());
    checks.add(new WsDiagnosticSendDetailsOutsideConfigurationCheck());

    // workflow
    checks.add(new WorkflowOffWithDataConfigurationCheck());

    // messaging (grouper.properties and grouper.client.properties)
    checks.add(new MessagingAllowedGroupMissingConfigurationCheck());
    checks.add(new MessagingTrustAllCertificatesConfigurationCheck());
    checks.add(new MessagingHostnameVerificationOffConfigurationCheck());

    // UI (grouper-ui.properties)
    checks.add(new UiAntiFramingDisabledConfigurationCheck());

    // membership requirements and custom composites which are configured but do not work
    checks.add(new MembershipRequireConfigurationCheck());

    // provisioning (grouper-loader.properties)
    checks.add(new ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck());
    checks.add(new ScimUserGroupsConfigurationCheck());

    // database: object ids outside the allowed pattern (from before ids were limited)
    checks.add(new ObjectIdNotAllowedConfigurationCheck());

    // OAuth (grouper.properties): misspelled redirect URI allowlist keys still in a config file
    checks.add(new OAuthRedirectUriMisspelledConfigurationCheck());

    return checks;
  }

  /**
   * run every configuration review check and return all findings, errors first.  A check that throws
   * does not stop the others; its failure is turned into an error finding so it is visible.
   * @return the findings across all checks
   */
  public static List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    for (GrouperConfigurationCheck check : builtInChecks()) {
      try {
        List<ConfigurationCheckResult> checkResults = check.checkConfiguration();
        if (checkResults != null) {
          results.addAll(checkResults);
        }
      } catch (Exception e) {
        LOG.error("Error running configuration review check: " + check.getName(), e);
        results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, check.getName(),
            null, null, null,
            "The configuration check '" + check.getName() + "' failed to run: " + e.getMessage(),
            "See the Grouper logs for the stack trace and report this error."));
      }
    }

    sortErrorsFirst(results);

    return results;
  }

  /**
   * sort so errors come before warnings, then by check name so the output is stable
   * @param results the findings to sort in place
   */
  private static void sortErrorsFirst(List<ConfigurationCheckResult> results) {
    Collections.sort(results, new Comparator<ConfigurationCheckResult>() {
      @Override
      public int compare(ConfigurationCheckResult o1, ConfigurationCheckResult o2) {
        int severityCompare = o1.getSeverity().ordinal() - o2.getSeverity().ordinal();
        if (severityCompare != 0) {
          return severityCompare;
        }
        return GrouperUtil.defaultString(o1.getCheckName()).compareTo(GrouperUtil.defaultString(o2.getCheckName()));
      }
    });
  }

}
