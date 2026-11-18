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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY: the recipient, subject and body of a sendEmail rule (ruleThenEnumArg0, 1 and 2) are
 * run through GrouperUtil.substituteExpressionLanguage when the rule fires, as the rule's act-as
 * subject (GrouperSystem for rules saved in the UI).  Every ${...} in them is a full JEXL expression
 * with ruleElUtils (which grants privileges and changes memberships) and the other rule helpers in
 * scope.  So a non-sysadmin who can save a sendEmail rule could run code as GrouperSystem.
 *
 * The fix is at save time only (nothing changes when a rule fires): a non-wheel author may save a
 * sendEmail rule only if every ${...} in the three fields is one of the predefined variables:
 * <ul>
 *   <li>a plain variable name the rule provides (groupName, groupDisplayExtension, stemName,
 *   memberId, membership and permission timestamps, role and attribute names, ...), see
 *   PLAIN_VARIABLES</li>
 *   <li>safeSubject.name, .description, .id, .sourceId, .typeName, .emailAddress</li>
 *   <li>the getter forms of those: safeSubject.getName() etc.</li>
 *   <li>safeSubject.getAttributeValue('x') or getAttributeValueOrCommaSeparated('x') with a
 *   literal attribute name</li>
 * </ul>
 * Anything else is refused with an error naming the expression.  Wheel/root may save anything.
 * Every field is checked on every save, so a non-wheel user cannot edit a rule in which a sysadmin
 * wrote an expression.  A "template: name" subject or body has no ${ in the field itself and is
 * allowed (the template file is written by the site).
 *
 * Called from RuleService.saveOrUpdateRuleAttributes (the rules UI, which saves in a root session
 * and carries the logged in user on the RuleConfig) and from GrouperAttributeAssignValueRulesConfigHook
 * (WS, GSH, attribute assignment by a non-wheel session).
 *
 * This is its own class so the fix can be dropped into older releases as whole files: it uses only
 * APIs present from 2.6.19 to 7.x (no RuleConfig import, no commons-lang, no Java 9+ APIs), and the
 * churny files (RuleService, the hook) only get a few lines that call it.
 */
public class RuleEmailTemplateCheck {

  // ---------------------------------------------------------------- what is allowed

  /**
   * Plain variable names a sendEmail rule provides (RuleDefinition.addElVariables, RuleCheck and
   * RuleCheckType addElVariables), the string and timestamp ones only.  Object variables (group,
   * member, membership, subject, stem, role, attributeDef, attributeAssign, ...) and the helpers
   * (ruleElUtils, grouperUtil, ruleUtils, ruleDefinition, custom EL classes) are not here.
   */
  private static final Set<String> PLAIN_VARIABLES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
      // group
      "groupId", "groupName", "groupDisplayName", "groupExtension", "groupDisplayExtension", "groupDescription",
      // folder
      "stemId", "stemName",
      // member and membership
      "memberId", "membershipId", "membershipDisabledTimestamp", "membershipEnabledTimestamp",
      // permissions
      "permissionDisabledTimestamp", "permissionEnabledTimestamp", "action",
      "roleId", "roleName", "roleDisplayName", "roleExtension", "roleDisplayExtension", "roleDescription",
      // attributes
      "attributeDefId", "attributeDefName", "attributeDefExtension", "nameOfAttributeDef",
      "attributeDefNameId", "attributeDefNameName", "attributeDefNameExtension", "attributeDefNameDisplayName",
      "attributeDefNameDescription", "attributeDefNameDisplayExtension", "attributeAssignId",
      // the rule's owner and check
      "ownerAttributeAssignId", "ownerAttributeDefId", "ownerGroupId", "ownerMemberId", "ownerMembershipId",
      "ownerStemId", "checkOwnerId", "checkOwnerName", "checkStemScope")));

  /** safeSubject.name etc. (JEXL calls the getter) */
  private static final Pattern SAFE_SUBJECT_PROPERTY = Pattern.compile(
      "^safeSubject\\s*\\.\\s*(name|description|id|sourceId|typeName|emailAddress)$");

  /** safeSubject.getName() etc. */
  private static final Pattern SAFE_SUBJECT_GETTER = Pattern.compile(
      "^safeSubject\\s*\\.\\s*get(Name|Description|Id|SourceId|TypeName|EmailAddress)\\s*\\(\\s*\\)$");

  /** safeSubject.getAttributeValue('x') or getAttributeValueOrCommaSeparated('x'), literal name only */
  private static final Pattern SAFE_SUBJECT_ATTRIBUTE = Pattern.compile(
      "^safeSubject\\s*\\.\\s*(getAttributeValue|getAttributeValueOrCommaSeparated)\\s*\\(\\s*"
      + "('[A-Za-z0-9_.:@-]+'|\"[A-Za-z0-9_.:@-]+\")\\s*\\)$");

  /** plain identifier (looked up in PLAIN_VARIABLES) */
  private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

  /** externalized text for the error, with ##field## and ##expression## */
  public static final String ERROR_TEXT_KEY = "grouperRuleConfigAddEditEmailExpressionRequiresWheel";

  /** hook veto reason key */
  public static final String HOOK_VETO_REASON_KEY = "veto.rules.sendEmailExpressionRequiresWheel";

  /** longest expression quoted in an error */
  private static final int MAX_QUOTED_LENGTH = 100;

  /**
   * Is this ${...} content (without the ${ and }) a predefined variable?
   * @param expression the text between ${ and }
   * @return true if allowed for a non-wheel author
   */
  public static boolean isPredefinedExpression(String expression) {
    if (expression == null) {
      return false;
    }
    String trimmed = expression.trim();
    if (IDENTIFIER.matcher(trimmed).matches()) {
      return PLAIN_VARIABLES.contains(trimmed);
    }
    return SAFE_SUBJECT_PROPERTY.matcher(trimmed).matches()
        || SAFE_SUBJECT_GETTER.matcher(trimmed).matches()
        || SAFE_SUBJECT_ATTRIBUTE.matcher(trimmed).matches();
  }

  /**
   * Find the first ${...} in a field that is not a predefined variable.  Every "${" is checked,
   * including ones nested in or overlapping another (the substitution engine matches "${" to the
   * first "}" and balances inner curlies, so content with a curly is never allowed).  An unclosed
   * "${" is refused too.
   * @param fieldValue recipient, subject or body as stored
   * @return the offending text (from ${ to the next }, or to the end), or null if all are allowed
   */
  public static String disallowedExpression(String fieldValue) {
    if (fieldValue == null) {
      return null;
    }
    int start = fieldValue.indexOf("${");
    while (start >= 0) {
      int end = fieldValue.indexOf('}', start + 2);
      if (end < 0) {
        return fieldValue.substring(start);
      }
      String inner = fieldValue.substring(start + 2, end);
      if (!isPredefinedExpression(inner)) {
        return fieldValue.substring(start, end + 1);
      }
      // next "${" after this one's start (not its end), so an overlapping one is still checked
      start = fieldValue.indexOf("${", start + 2);
    }
    return null;
  }

  // ---------------------------------------------------------------- the save-time decision

  /**
   * Is this then option the sendEmail one (RuleThenEnum.valueOfIgnoreCase is case insensitive)?
   * @param thenOption ruleConfig then option or the stored ruleThenEnum value
   * @return true if sendEmail
   */
  public static boolean isSendEmail(String thenOption) {
    return thenOption != null && RuleThenEnum.sendEmail.name().equalsIgnoreCase(thenOption.trim());
  }

  /**
   * Null safe wheel/root check.  A null subject is not wheel (fail closed).
   * @param subject subject
   * @return true if wheel or root
   */
  public static boolean isWheelOrRoot(Subject subject) {
    if (subject == null) {
      return false;
    }
    try {
      return PrivilegeHelper.isWheelOrRoot(subject);
    } catch (RuntimeException re) {
      // cannot establish it: treat as not wheel
      return false;
    }
  }

  /**
   * The logged in user on a RuleConfig.  RuleConfig.getSubject() only exists from 6.4.1 / 7.5.1, so
   * read the private "subject" field (present since RuleConfig was added in 5.8.0).  Taking an Object
   * keeps this class free of RuleConfig, so it compiles on releases without it.
   * @param ruleConfig the RuleConfig being saved
   * @return the configuring subject, or null if not set or not readable
   */
  public static Subject configuringSubject(Object ruleConfig) {
    if (ruleConfig == null) {
      return null;
    }
    try {
      Class<?> theClass = ruleConfig.getClass();
      while (theClass != null) {
        try {
          Field field = theClass.getDeclaredField("subject");
          field.setAccessible(true);
          Object value = field.get(ruleConfig);
          return value instanceof Subject ? (Subject)value : null;
        } catch (NoSuchFieldException nsfe) {
          theClass = theClass.getSuperclass();
        }
      }
    } catch (Exception e) {
      // not readable: null, which is treated as not wheel
    }
    return null;
  }

  /**
   * Check a sendEmail rule's three fields for a save.
   * @param configuringSubject who is saving (the logged in user, or the session subject); null is not wheel
   * @param thenOption then option (only sendEmail is checked)
   * @param recipient ruleThenEnumArg0
   * @param subject ruleThenEnumArg1
   * @param body ruleThenEnumArg2
   * @return an error naming the field and expression, or null if allowed
   */
  public static String sendEmailExpressionError(Subject configuringSubject, String thenOption,
      String recipient, String subject, String body) {

    if (!isSendEmail(thenOption)) {
      return null;
    }
    // check the fields first and look up wheel only when something would be refused
    String[] fieldNames = {"recipient", "subject", "body"};
    String[] fieldValues = {recipient, subject, body};
    for (int i = 0; i < fieldValues.length; i++) {
      String expression = disallowedExpression(fieldValues[i]);
      if (expression != null) {
        if (isWheelOrRoot(configuringSubject)) {
          return null;
        }
        return notAllowedError(fieldNames[i], expression);
      }
    }
    return null;
  }

  /**
   * Is this GrouperSystem?  Cheap, no lookups (the hook calls this for every attribute value write).
   * @param subject subject
   * @return true if root
   */
  public static boolean isRootSubject(Subject subject) {
    return subject != null && "GrouperSystem".equals(subject.getId()) && "g:isa".equals(subject.getSourceId());
  }

  /**
   * Check the rule an attribute value belongs to, for the attribute value hook.  Reads the whole rule
   * as stored (with the value just written), so every field is checked on every save.  Call it in a
   * root session.  Cheap for anything that is not a rule value written by a non-root session: root
   * returns at once, other attributes after an id compare, and the wheel lookup is only done when a
   * field would be refused.
   * @param runningSubject the subject of the session doing the write
   * @param attributeAssignValue the value being inserted or updated
   * @return an error, or null if the value is not part of a sendEmail rule or the rule is allowed
   */
  public static String sendEmailExpressionErrorForValue(Subject runningSubject, AttributeAssignValue attributeAssignValue) {

    if (attributeAssignValue == null || isRootSubject(runningSubject)) {
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
    if (ruleTypeAssignId == null || ruleTypeAssignId.trim().length() == 0) {
      return null;
    }
    RuleDefinition ruleDefinition = new RuleDefinition(ruleTypeAssignId);
    RuleThen ruleThen = ruleDefinition.getThen();
    if (ruleThen == null) {
      return null;
    }
    return sendEmailExpressionError(runningSubject, ruleThen.getThenEnum(), ruleThen.getThenEnumArg0(),
        ruleThen.getThenEnumArg1(), ruleThen.getThenEnumArg2());
  }

  /**
   * The error for a refused field.  Externalized text grouperRuleConfigAddEditEmailExpressionRequiresWheel
   * (##field## and ##expression##), with a fallback so the fix does not need a properties file.
   * @param fieldName recipient, subject or body
   * @param expression the refused ${...}
   * @return the message
   */
  public static String notAllowedError(String fieldName, String expression) {
    String quoted = expression == null ? "" : expression;
    if (quoted.length() > MAX_QUOTED_LENGTH) {
      quoted = quoted.substring(0, MAX_QUOTED_LENGTH) + "...";
    }
    // the UI shows the message as HTML; the expression is the user's own input
    quoted = escapeHtml(quoted);
    String error = null;
    try {
      error = GrouperTextContainer.textOrNull(ERROR_TEXT_KEY);
    } catch (RuntimeException re) {
      // text container not available in this context (hook, GSH), use the fallback
      error = null;
    }
    if (error == null || error.trim().length() == 0) {
      error = "Only a Grouper administrator can save an email rule whose recipient, subject or body uses "
          + "an expression other than the predefined variables (for example ${groupName} or ${safeSubject.name}).  "
          + "Not allowed in the ##field##: ##expression##";
    }
    return error.replace("##field##", fieldName).replace("##expression##", quoted);
  }

  /**
   * minimal HTML escape for text content (the UI shows errors as HTML; quotes are left alone so the
   * WS and GSH messages stay readable)
   * @param input text
   * @return escaped text
   */
  private static String escapeHtml(String input) {
    StringBuilder result = new StringBuilder(input.length() + 16);
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '<': result.append("&lt;"); break;
        case '>': result.append("&gt;"); break;
        case '&': result.append("&amp;"); break;
        default: result.append(c);
      }
    }
    return result.toString();
  }

}
