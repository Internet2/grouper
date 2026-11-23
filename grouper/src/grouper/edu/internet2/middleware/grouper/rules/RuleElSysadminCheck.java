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

import java.util.Map;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY (GRP-7596): who may write rule expression language, and how it runs.
 *
 * A rule's if-condition EL (ruleIfConditionEl) and then EL (ruleThenEl) are arbitrary JEXL run as the
 * rule's act-as subject.  From 7.9.1, 6.5.1 and 4.26.1 they ran in the restricted JEXL sandbox
 * (GrouperJexlSandbox), which broke sysadmin rules that use new(...), class objects or the full API
 * (e.g. the documented "veto delete membership if immediate membership has attribute value" pattern).
 *
 * The rule is instead: only a Grouper sysadmin (wheel/root) can save if/then EL.  A site that needs
 * more than the sandbox allows sets rules.allowUnrestrictedEl = true, and then if/then EL runs
 * unrestricted, with the full set of EL variables (the live group, member, membership, subject and the
 * rule objects).  Most sites do not need it, so the default (false) keeps the sandbox as a second line
 * of defense behind the save-time check.  The switch replaces rules.accessToApiInEl.group, which
 * decided the variables by the act-as subject instead of site wide.
 *
 * So that a site finds the switch:
 * <ul>
 *   <li>rule EL that fails while sandboxed throws with a hint naming the property (substituteRuleEl)</li>
 *   <li>saving if/then EL that uses something the sandbox refuses (usesSandboxRefusedSyntax) gives a
 *   warning in the rules UI, and a log warning on other save paths (saveTimeSandboxWarning)</li>
 * </ul>
 *
 * The save side:
 * <ul>
 *   <li>The rules UI (RuleService) refuses EL and custom rules for non-wheel users (GRP-7359).</li>
 *   <li>Everything else (WS, GSH, the API) writes rule attribute values, and
 *   GrouperAttributeAssignValueRulesConfigHook calls elErrorForValue on every insert or update and
 *   elErrorForDeletedValue on every delete.  A non-wheel session may not write or delete any value of
 *   a rule that has if/then EL, not only the EL values, so a non-wheel user cannot change the act-as
 *   subject, check, owner or if-condition of a sysadmin's EL rule.</li>
 * </ul>
 * Rules saved before this change are grandfathered: they run unrestricted too (see the upgrade
 * instructions and ruleElCheck.gsh for reviewing them).
 *
 * Send-email rules: a non-wheel author may only use the predefined variables in the recipient,
 * subject and body (RuleEmailTemplateCheck), and those run in the sandbox as before.  A rule with
 * anything else could only have been saved by a sysadmin, and a "template: name" subject or body is a
 * file the site wrote, so either runs unrestricted when the switch is on (sendEmailRunsUnrestricted).
 *
 * Its own class (like RuleEmailTemplateCheck) so the change only adds a few lines to the churny files.
 */
public class RuleElSysadminCheck {

  /** externalized text for the error (existing GRP-7359 text) */
  public static final String ERROR_TEXT_KEY = "grouperRuleConfigAddEditElRequiresWheel";

  /** hook veto reason key */
  public static final String HOOK_VETO_REASON_KEY = "veto.rules.elRequiresWheel";

  /** the switch: if true, sysadmin rule EL runs unrestricted, else in the JEXL sandbox */
  public static final String ALLOW_UNRESTRICTED_EL_PROPERTY = "rules.allowUnrestrictedEl";

  /** externalized text for the save-time warning, with ##property## */
  public static final String SAVE_WARNING_TEXT_KEY = "grouperRuleConfigAddEditElSandboxWarning";

  /**
   * Syntax the JEXL sandbox refuses (GrouperJexlSandbox): constructors, class objects, and the objects
   * rule EL only gets when unrestricted.  A heuristic, used only to warn (never to refuse): it finds the
   * common cases, the runtime hint (substituteRuleEl) catches the rest.
   */
  private static final Pattern SANDBOX_REFUSED = Pattern.compile(
      "(\\bnew\\s*\\(|\\.\\s*class\\b|\\bforName\\b|\\bgetClass\\s*\\("
      + "|\\b(group|member|membership|subject|attributeAssignType|ruleDefinition|ruleUtils)\\s*\\.)");

  // ---------------------------------------------------------------- the switch

  /**
   * Is rules.allowUnrestrictedEl true?  Read each time (config is cached), so a change applies without
   * a restart.
   * @return true if sysadmin rule EL runs unrestricted
   */
  public static boolean allowUnrestrictedEl() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean(ALLOW_UNRESTRICTED_EL_PROPERTY, false);
  }

  /**
   * Does this expression use something the sandbox refuses (constructors, class objects, the
   * group/member/membership/subject/rule objects)?  Heuristic, for warnings only.
   * @param el the expression (may be null)
   * @return true if it looks like it needs rules.allowUnrestrictedEl
   */
  public static boolean usesSandboxRefusedSyntax(String el) {
    return el != null && el.indexOf("${") >= 0 && SANDBOX_REFUSED.matcher(el).find();
  }

  // ---------------------------------------------------------------- runtime

  /**
   * Substitute rule EL.  When it runs sandboxed and fails, re-throw with a hint naming
   * rules.allowUnrestrictedEl, since the bare JEXL error ("unknown, ambiguous or inaccessible method")
   * does not say why or what to do.  An unrestricted failure is re-thrown as is.
   * @param el expression
   * @param variableMap variables
   * @param unrestricted true to run outside the sandbox
   * @param ruleDefinition the rule, for the message
   * @return the result
   */
  public static String substituteRuleEl(String el, Map<String, Object> variableMap, boolean unrestricted,
      RuleDefinition ruleDefinition) {
    try {
      return GrouperUtil.substituteExpressionLanguage(el, variableMap, unrestricted, false, false);
    } catch (RuntimeException re) {
      if (unrestricted) {
        throw re;
      }
      // the message can reach the end user whose change fired the rule (e.g. a veto), so it has the
      // hint and the rule id only; the expression and the JEXL error are in the chained cause (logs)
      throw new RuntimeException(sandboxHint(ruleDefinition), re);
    }
  }

  /**
   * The runtime hint for rule EL that failed in the sandbox.
   * @param ruleDefinition rule (may be null)
   * @return the message
   */
  public static String sandboxHint(RuleDefinition ruleDefinition) {
    StringBuilder result = new StringBuilder("This rule's expression language runs in the restricted sandbox ("
        + ALLOW_UNRESTRICTED_EL_PROPERTY + " is false), which does not allow constructors (new), class objects, "
        + "or the group, member, membership, subject and rule objects.  If a Grouper sysadmin wrote this rule "
        + "and it needs them, set " + ALLOW_UNRESTRICTED_EL_PROPERTY + " = true in grouper.properties (GRP-7596).");
    // rule id only (no owner, no expression): this can be shown to a user who cannot see the rule
    AttributeAssign attributeAssignType = ruleDefinition == null ? null : ruleDefinition.getAttributeAssignType();
    if (attributeAssignType != null) {
      result.append("  Rule id: ").append(attributeAssignType.getId()).append(".");
    }
    return result.toString();
  }

  // ---------------------------------------------------------------- save-time warning

  /**
   * After a rule is saved: a warning if the switch is off and the rule's if/then EL, or a send-email
   * field that runs unrestricted when the switch is on, uses something the sandbox refuses.  The rule
   * is saved either way.
   * @param ruleDefinition the saved rule
   * @return the warning, or null
   */
  public static String saveTimeSandboxWarning(RuleDefinition ruleDefinition) {
    if (ruleDefinition == null || allowUnrestrictedEl()) {
      return null;
    }
    boolean refused = false;
    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (ruleIfCondition != null && usesSandboxRefusedSyntax(ruleIfCondition.getIfConditionEl())) {
      refused = true;
    }
    RuleThen ruleThen = ruleDefinition.getThen();
    if (ruleThen != null) {
      if (usesSandboxRefusedSyntax(ruleThen.getThenEl())) {
        refused = true;
      }
      if (RuleEmailTemplateCheck.isSendEmail(ruleThen.getThenEnum()) && sendEmailRunsUnrestricted(ruleThen)
          && (usesSandboxRefusedSyntax(ruleThen.getThenEnumArg0()) || usesSandboxRefusedSyntax(ruleThen.getThenEnumArg1())
              || usesSandboxRefusedSyntax(ruleThen.getThenEnumArg2()))) {
        refused = true;
      }
    }
    return refused ? saveWarningText() : null;
  }

  /**
   * For the attribute value hook (WS, GSH, API): a warning if the switch is off and this value is if/then
   * EL that uses something the sandbox refuses.  The hook logs it (it cannot show a message).
   * @param attributeAssignValue value being inserted or updated
   * @return the warning, or null
   */
  public static String saveTimeSandboxWarningForValue(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null || !usesSandboxRefusedSyntax(attributeAssignValue.getValueString())
        || allowUnrestrictedEl()) {
      return null;
    }
    AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
    AttributeDefName attributeDefName = attributeAssign == null ? null : attributeAssign.getAttributeDefName();
    if (attributeDefName == null) {
      return null;
    }
    String name = attributeDefName.getName();
    if (!RuleUtils.ruleIfConditionElName().equals(name) && !RuleUtils.ruleThenElName().equals(name)) {
      return null;
    }
    return saveWarningText() + "  Rule id: " + attributeAssign.getOwnerAttributeAssignId() + ".";
  }

  /**
   * The save-time warning, externalized with a fallback
   * @return the text
   */
  static String saveWarningText() {
    String warning = null;
    try {
      warning = GrouperTextContainer.textOrNull(SAVE_WARNING_TEXT_KEY);
    } catch (RuntimeException re) {
      // text container not available in this context (hook, GSH), use the fallback
      warning = null;
    }
    if (isBlank(warning)) {
      warning = "The rule was saved, but its expression language uses something the restricted sandbox does not "
          + "allow (a constructor, a class object, or the group, member, membership, subject or rule objects), "
          + "so it will fail when it runs.  If it needs them, set ##property## = true in grouper.properties.";
    }
    return warning.replace("##property##", ALLOW_UNRESTRICTED_EL_PROPERTY);
  }

  /**
   * Does this rule have an if-condition EL or a then EL?
   * @param ruleDefinition rule
   * @return true if either is not blank
   */
  public static boolean hasIfOrThenEl(RuleDefinition ruleDefinition) {
    if (ruleDefinition == null) {
      return false;
    }
    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (ruleIfCondition != null && !isBlank(ruleIfCondition.getIfConditionEl())) {
      return true;
    }
    RuleThen ruleThen = ruleDefinition.getThen();
    return ruleThen != null && !isBlank(ruleThen.getThenEl());
  }

  /**
   * Check the rule an attribute value belongs to, for the attribute value hook.  Reads the whole rule
   * as stored (with the value just written).  Call it in a root session.  Cheap for anything that is
   * not a rule value written by a non-root session: root returns at once, other attributes after an id
   * compare, and the wheel lookup is only done when the rule has if/then EL.
   * @param runningSubject the subject of the session doing the write
   * @param attributeAssignValue the value being inserted or updated
   * @return an error, or null if the value is not part of a rule with if/then EL or the writer is wheel/root
   */
  public static String elErrorForValue(Subject runningSubject, AttributeAssignValue attributeAssignValue) {

    if (attributeAssignValue == null || RuleEmailTemplateCheck.isRootSubject(runningSubject)) {
      return null;
    }
    AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
    if (attributeAssign == null) {
      return null;
    }
    AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
    AttributeDef ruleAttrAttributeDef = RuleUtils.ruleAttrAttributeDef();
    if (attributeDefName == null || ruleAttrAttributeDef == null
        || !ruleAttrAttributeDef.getId().equals(attributeDefName.getAttributeDefId())) {
      return null;
    }
    String ruleTypeAssignId = attributeAssign.getOwnerAttributeAssignId();
    if (isBlank(ruleTypeAssignId)) {
      return null;
    }
    RuleDefinition ruleDefinition = new RuleDefinition(ruleTypeAssignId);
    if (!hasIfOrThenEl(ruleDefinition)) {
      return null;
    }
    // fail closed: a null or unresolvable subject is not wheel
    if (RuleEmailTemplateCheck.isWheelOrRoot(runningSubject)) {
      return null;
    }
    return notAllowedError();
  }

  /**
   * Check a deleted rule value, for the attribute value hook (post delete, so the value is gone and the
   * rule is read without it).  A non-wheel session may not delete any value of a rule that has if/then
   * EL, or the if/then EL itself: deleting the if-condition EL, an if owner or a stem scope widens when
   * a sysadmin's then EL fires.  Deleting the whole rule is not checked here: the hook returns earlier
   * when the rule's own assignment is being deleted, and its child values are then deleted in a root
   * session.  Call it in a root session.
   * @param runningSubject the subject of the session doing the delete
   * @param attributeAssignValue the value that was deleted
   * @return an error, or null if allowed
   */
  public static String elErrorForDeletedValue(Subject runningSubject, AttributeAssignValue attributeAssignValue) {

    if (attributeAssignValue == null || RuleEmailTemplateCheck.isRootSubject(runningSubject)) {
      return null;
    }
    AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
    if (attributeAssign == null) {
      return null;
    }
    AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
    AttributeDef ruleAttrAttributeDef = RuleUtils.ruleAttrAttributeDef();
    if (attributeDefName == null || ruleAttrAttributeDef == null
        || !ruleAttrAttributeDef.getId().equals(attributeDefName.getAttributeDefId())) {
      return null;
    }
    String ruleTypeAssignId = attributeAssign.getOwnerAttributeAssignId();
    if (isBlank(ruleTypeAssignId)) {
      return null;
    }
    String name = attributeDefName.getName();
    boolean deletedEl = (RuleUtils.ruleIfConditionElName().equals(name) || RuleUtils.ruleThenElName().equals(name))
        && !isBlank(attributeAssignValue.getValueString());
    if (!deletedEl && !hasIfOrThenEl(new RuleDefinition(ruleTypeAssignId))) {
      return null;
    }
    // fail closed: a null or unresolvable subject is not wheel
    if (RuleEmailTemplateCheck.isWheelOrRoot(runningSubject)) {
      return null;
    }
    return notAllowedError();
  }

  /**
   * Does this send-email rule run its recipient, subject and body unrestricted?  True if any of them
   * uses an expression other than the predefined variables (only a sysadmin can save that), or the
   * subject or body is a "template: name" file (written by the site).  Otherwise the predefined
   * variables run in the sandbox, as before.
   * @param ruleThen the rule's then
   * @return true if unrestricted
   */
  public static boolean sendEmailRunsUnrestricted(RuleThen ruleThen) {
    if (ruleThen == null) {
      return false;
    }
    if (isTemplateReference(ruleThen.getThenEnumArg1()) || isTemplateReference(ruleThen.getThenEnumArg2())) {
      return true;
    }
    return RuleEmailTemplateCheck.disallowedExpression(ruleThen.getThenEnumArg0()) != null
        || RuleEmailTemplateCheck.disallowedExpression(ruleThen.getThenEnumArg1()) != null
        || RuleEmailTemplateCheck.disallowedExpression(ruleThen.getThenEnumArg2()) != null;
  }

  /**
   * Is this a "template: name" email subject or body (see RuleUtils.emailTemplate)?
   * @param value stored subject or body
   * @return true if it names a template file
   */
  static boolean isTemplateReference(String value) {
    return value != null && value.trim().startsWith("template:");
  }

  /**
   * The error for a non-wheel write to a rule with if/then EL, with a fallback so it works without a
   * properties file.
   * @return the message
   */
  public static String notAllowedError() {
    String error = null;
    try {
      error = GrouperTextContainer.textOrNull(ERROR_TEXT_KEY);
    } catch (RuntimeException re) {
      // text container not available in this context (hook, GSH), use the fallback
      error = null;
    }
    if (isBlank(error)) {
      error = "Only a Grouper administrator can add or edit a rule that uses expression language (EL).";
    }
    return error;
  }

  /**
   * null safe blank check (no commons-lang, like RuleEmailTemplateCheck)
   * @param value value
   * @return true if null or whitespace
   */
  private static boolean isBlank(String value) {
    return value == null || value.trim().length() == 0;
  }

}
