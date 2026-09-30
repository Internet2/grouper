package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7381): a sendEmail rule sends arbitrary email whose recipients,
 * subject and body are evaluated as expression language at fire time as GrouperSystem.  This check
 * spans the three properties that gate who may create sendEmail rules and advises designating a
 * trusted population when none is set (or when the check has been explicitly opted out of).
 */
public class RulesSendEmailGateConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * group whose members may create sendEmail rules (email-specific designation)
   */
  public static final String EMAIL_SENDER_PROPERTY_NAME = "rules.restrictRulesEmailSendersToMembersOfThisGroupName";

  /**
   * group whose members may use the rules UI (a vetted rule-editor population)
   */
  public static final String RESTRICT_RULES_UI_PROPERTY_NAME = "rules.restrictRulesUiToMembersOfThisGroupName";

  /**
   * opt-out that restores the previous open behavior when neither group is configured
   */
  public static final String ALLOW_SEND_EMAIL_PROPERTY_NAME = "rules.allowSendEmailRulesWhenNoGroupConfigured";

  @Override
  public String getName() {
    return "rulesSendEmailGate";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    String emailSenderGroupName = grouperConfig.propertyValueString(EMAIL_SENDER_PROPERTY_NAME, "");

    // the email-specific designation wins when it is set; just make sure the group actually exists
    if (StringUtils.isNotBlank(emailSenderGroupName)) {
      Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
      if (emailSenderGroup == null) {
        results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
            ConfigFileName.GROUPER_PROPERTIES, EMAIL_SENDER_PROPERTY_NAME, emailSenderGroupName,
            "rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '" + emailSenderGroupName
                + "', but no group with that name exists.  sendEmail rule creation fails closed.",
            "Set rules.restrictRulesEmailSendersToMembersOfThisGroupName to an existing power-user "
                + "group, or clear it."));
      }
      return results;
    }

    // otherwise, if the rules UI itself is gated to a group, its members are the vetted (trusted)
    // population that may create sendEmail rules -- that is an acceptable posture, so nothing to advise
    String restrictRulesUiGroupName = grouperConfig.propertyValueString(RESTRICT_RULES_UI_PROPERTY_NAME, "");
    if (StringUtils.isNotBlank(restrictRulesUiGroupName)) {
      return results;
    }

    // both gates blank
    boolean allowWhenNoGroupConfigured = grouperConfig.propertyValueBoolean(ALLOW_SEND_EMAIL_PROPERTY_NAME, false);
    if (allowWhenNoGroupConfigured) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, ALLOW_SEND_EMAIL_PROPERTY_NAME, "true",
          "Both rules.restrictRulesEmailSendersToMembersOfThisGroupName and "
              + "rules.restrictRulesUiToMembersOfThisGroupName are blank and "
              + "rules.allowSendEmailRulesWhenNoGroupConfigured is true, so any rule editor may create "
              + "sendEmail rules.  A sendEmail rule sends arbitrary email evaluated as GrouperSystem.",
          "Set rules.restrictRulesEmailSendersToMembersOfThisGroupName to a power-user group, or set "
              + "rules.allowSendEmailRulesWhenNoGroupConfigured=false so that only wheel/root may create "
              + "sendEmail rules."));
    } else {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, EMAIL_SENDER_PROPERTY_NAME, "",
          "Neither rules.restrictRulesEmailSendersToMembersOfThisGroupName nor "
              + "rules.restrictRulesUiToMembersOfThisGroupName is set, so no trusted population is "
              + "designated for creating sendEmail rules (currently only wheel/root can create them).",
          "Set rules.restrictRulesEmailSendersToMembersOfThisGroupName (or "
              + "rules.restrictRulesUiToMembersOfThisGroupName) to a power-user group to designate who "
              + "may create sendEmail rules."));
    }

    return results;
  }

}
