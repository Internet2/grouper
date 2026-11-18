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
package edu.internet2.middleware.grouper.rules;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY: server side enforcement of the rules UI "Run daemon" setting.
 *
 * The rules UI only shows the run daemon control when RuleConfig.isCanSetDaemon() is true (the
 * pattern supports a daemon, and the user is wheel/root or the pattern is daemon-assignable by a
 * non-admin).  The group and folder submit actions read the grouperRuleRunDaemon request parameter
 * whether or not the control was shown, and RuleService.saveOrUpdateRuleAttributes stores it in a
 * root session.  So a non-admin could forge runDaemon=true on a pattern reserved for admins, or on a
 * rule whose check type has no daemon, which later throws "This rule is explicitly set to run a
 * daemon, but it is not implemented" every time the rule is processed for its owner.
 *
 * Design (2026-11-13): a value the user could not set must not do anything, so it is not spoofable.
 * When a non-admin may not set run daemon, the submitted value (true AND false) is ignored, with no
 * error: a new rule stores run daemon blank (the rule engine default: on if the check type has a
 * daemon), and an edit keeps the rule's stored value.  Wheel/root and non-admins who see the control
 * behave as before.  Separately, true on a rule whose check type has no daemon is refused for
 * everyone, admins included, because that rule would throw every time it runs.
 *
 * This is its own class, not a method on RuleService, so the fix can be dropped into older releases
 * as whole files: RuleService differs from release to release (newer ones carry the GRP-7359 rules
 * lockdown), and this class only uses APIs that have been stable since 5.8.0 (RuleConfig.getRunDaemon,
 * isCanSetDaemon, its subject field, PrivilegeHelper.isWheelOrRoot, RuleCheckType.canRunDeamon).  RuleService only gets a
 * few lines that call it.
 */
public class RuleDaemonSettingCheck {

  // ---------------------------------------------------------------- who may set run daemon

  /**
   * Whether the submitted run daemon value must be ignored, checked before anything is written.
   * It is used (not ignored) when the configuring subject is wheel/root, or when
   * RuleConfig.isCanSetDaemon() is true (the UI showed the control).  Otherwise the UI did not offer
   * the control, so whatever came in the request is not the user's choice and is dropped.
   * Wheel/root are never ignored, so their true on a rule with no daemon is still refused by
   * daemonCapabilityError (as before), rather than silently dropped.
   * Fails closed: no RuleConfig, no configuring subject, or an unknown pattern name means ignore.
   * @param ruleConfig the rule being saved (carries the logged in user as its subject)
   * @return true if the submitted run daemon value must not be used
   */
  public static boolean isSubmittedDaemonSettingIgnored(RuleConfig ruleConfig) {

    if (ruleConfig == null) {
      return true;
    }
    Subject configuringSubject = configuringSubject(ruleConfig);
    if (configuringSubject == null) {
      return true;
    }

    try {
      // admins keep their value, the capability check below still applies to them
      if (PrivilegeHelper.isWheelOrRoot(configuringSubject)) {
        return false;
      }
    } catch (RuntimeException re) {
      // cannot tell if this is an admin: treat as a non-admin
    }

    try {
      // the same decision the UI uses to show the control: daemon-applicable pattern, and wheel/root
      // or a pattern a non-admin may set the daemon on (false for custom rules)
      return !ruleConfig.isCanSetDaemon();
    } catch (RuntimeException re) {
      // unknown pattern name: fail closed
      return true;
    }
  }

  /**
   * The subject configuring the rule (the logged in user), set by the RuleConfig constructor.
   * RuleConfig.getSubject() only exists from 6.4.1/7.5.1 (GRP-7359), so read the private field,
   * which has the same name in every release from 5.8.0, to keep this class one version for all.
   * @param ruleConfig the rule being saved
   * @return the subject, or null if not set or not readable
   */
  private static Subject configuringSubject(RuleConfig ruleConfig) {
    try {
      Field subjectField = RuleConfig.class.getDeclaredField("subject");
      subjectField.setAccessible(true);
      return (Subject)subjectField.get(ruleConfig);
    } catch (Exception e) {
      // not readable: no configuring subject, which fails closed
      return null;
    }
  }

  /**
   * The run daemon value the save will actually store, for the capability check.  Not ignored: the
   * submitted value.  Ignored on a new rule: absent.  Ignored on an edit: the rule's stored value,
   * which the save leaves unchanged.
   * @param ruleConfig the rule being saved
   * @param submittedDaemonSettingIgnored result of isSubmittedDaemonSettingIgnored
   * @param existingAttributeAssign the rule being edited, or null for a new rule
   * @return TRUE, FALSE, or null for absent
   */
  public static Boolean effectiveRunDaemon(RuleConfig ruleConfig, boolean submittedDaemonSettingIgnored,
      AttributeAssign existingAttributeAssign) {

    if (!submittedDaemonSettingIgnored) {
      return ruleConfig == null ? null : ruleConfig.getRunDaemon();
    }
    if (existingAttributeAssign == null) {
      return null;
    }
    String storedValue = existingAttributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleRunDaemonName());
    if (StringUtils.isBlank(storedValue)) {
      return null;
    }
    // the same spellings GrouperUtil.booleanValue accepts
    storedValue = storedValue.trim();
    if (StringUtils.equalsIgnoreCase(storedValue, "T") || StringUtils.equalsIgnoreCase(storedValue, "true")
        || StringUtils.equalsIgnoreCase(storedValue, "Y") || StringUtils.equalsIgnoreCase(storedValue, "yes")) {
      return Boolean.TRUE;
    }
    if (StringUtils.equalsIgnoreCase(storedValue, "F") || StringUtils.equalsIgnoreCase(storedValue, "false")
        || StringUtils.equalsIgnoreCase(storedValue, "N") || StringUtils.equalsIgnoreCase(storedValue, "no")) {
      return Boolean.FALSE;
    }
    // not a value this check understands; it is left as stored
    return null;
  }

  // ---------------------------------------------------------------- does the rule support a daemon

  /**
   * Check the fully built rule supports a daemon when run daemon will be true.  canRunDeamon returns
   * false, or throws, when the check type has no daemon for this rule (the old code ignored both).
   * Applies to everyone, admins included.
   * @param runDaemon the value the save will store (see effectiveRunDaemon)
   * @param ruleDefinition the rule built from the request
   * @return an error message if runDaemon is true and the rule cannot run a daemon, else null
   */
  public static String daemonCapabilityError(Boolean runDaemon, RuleDefinition ruleDefinition) {

    if (!Boolean.TRUE.equals(runDaemon)) {
      return null;
    }

    boolean canRunDaemon = false;
    try {
      canRunDaemon = ruleDefinition != null && ruleDefinition.getCheck() != null
          && ruleDefinition.getCheck().checkTypeEnum() != null
          && ruleDefinition.getCheck().checkTypeEnum().canRunDeamon(ruleDefinition);
    } catch (RuntimeException re) {
      // the check type refuses a daemon for this rule
      canRunDaemon = false;
    }
    if (canRunDaemon) {
      return null;
    }
    return cannotSetDaemonError();
  }

  /**
   * Error message when run daemon may not be true.  Externalized text
   * grouperRuleConfigAddEditCannotSetDaemon, with a fallback so the fix does not need a properties file.
   * @return the message
   */
  public static String cannotSetDaemonError() {
    String error = null;
    try {
      error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotSetDaemon");
    } catch (RuntimeException re) {
      // text container not available in this context, use the fallback
      error = null;
    }
    if (StringUtils.isBlank(error)) {
      error = "Run daemon cannot be set to true for this rule because the rule does not support a daemon.";
    }
    return error;
  }

}
