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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY: a sendEmail rule's recipient, subject and body (ruleThenEnumArg0, 1, 2) run as JEXL when
 * the rule fires, as GrouperSystem for rules saved in the rules UI, with ruleElUtils in scope.  A
 * non-wheel author may only save ${...} that are predefined variables; wheel/root may save anything;
 * every field is checked on every save.
 *
 * Two save paths:
 * <ul>
 *   <li>rules UI: RulePattern.save -> RuleService.saveOrUpdateRuleAttributes in a root session with
 *   the logged in user on the RuleConfig (5.8.0 and later; called by reflection so this file
 *   compiles on 2.6.19 to 5.7.x, where those tests print "skip")</li>
 *   <li>attribute value hook: a non-wheel session assigning the rule attribute values directly
 *   (WS, GSH), every release</li>
 * </ul>
 *
 * Before the fix this class does not compile (RuleEmailTemplateCheck does not exist), and every
 * refused save below succeeds.  Java 8 compatible (2.6.19).
 */
public class RuleSendEmailPredefinedVariablesTest extends GrouperTest {

  /**
   *
   */
  public RuleSendEmailPredefinedVariablesTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleSendEmailPredefinedVariablesTest(String name) {
    super(name);
  }

  /** GRP-7381: send-email rules are sysadmin only unless a site configures who may create them */
  private static final String ALLOW_SEND_EMAIL_KEY = "rules.allowSendEmailRulesWhenNoGroupConfigured";

  /**
   * let a non-sysadmin create send-email rules, as a site that delegates them does (releases
   * without GRP-7381 ignore the key)
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap().put(ALLOW_SEND_EMAIL_KEY, "true");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(ALLOW_SEND_EMAIL_KEY);
    super.tearDown();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleSendEmailPredefinedVariablesTest("testUiPathNonWheelRefused"));
  }

  // ---------------------------------------------------------------- values

  /** the reproduced payload */
  private static final String RULE_EL_UTILS =
      "${ruleElUtils.assignGroupPrivilege('abc123','jdbc','test.subject.0',null,'admin')}";

  /** a method call on a predefined variable */
  private static final String METHOD_CALL = "${groupName.getClass()}";

  /** a variable the rule does not provide */
  private static final String UNKNOWN_VARIABLE = "${notAPredefinedVariable}";

  /** disallowed values, each tried in each field */
  private static final String[] DISALLOWED = {RULE_EL_UTILS, METHOD_CALL, UNKNOWN_VARIABLE};

  /** recipient using only predefined forms */
  private static final String OK_RECIPIENT = "a@b.c, ${safeSubject.emailAddress}";

  /** subject using only predefined forms */
  private static final String OK_SUBJECT = "Added to ${groupDisplayExtension} (${ groupName })";

  /** body using every safeSubject form */
  private static final String OK_BODY = "Hello ${safeSubject.name} ${safeSubject.getName()} ${safeSubject.description} "
      + "${safeSubject.getDescription()} ${safeSubject.id} ${safeSubject.sourceId} ${safeSubject.typeName} "
      + "${safeSubject.getAttributeValue('loginid')} ${safeSubject.getAttributeValueOrCommaSeparated(\"email\")} "
      + "${memberId} ${groupId} ${groupDisplayName} ${groupDescription} ${groupExtension}.  template-free text.";

  /** pattern used for the UI path (5.8.0 and later) */
  private static final String PATTERN = "SendEmailAfterNewMembership";

  /** field names in the error, by arg index */
  private static final String[] FIELD_NAMES = {"recipient", "subject", "body"};

  // ---------------------------------------------------------------- helpers

  /**
   * root makes a group SUBJ0 is ADMIN of
   * @param extension group extension under test:
   * @return the group
   */
  private static Group newGroup(final String extension) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName("test:" + extension).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      return group;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the RuleConfig and RulePattern classes (5.8.0 and later), or null
   * @return the RuleConfig class or null
   */
  private static Class<?> ruleConfigClass() {
    try {
      Class.forName("edu.internet2.middleware.grouper.rules.RulePattern");
      return Class.forName("edu.internet2.middleware.grouper.rules.RuleConfig");
    } catch (Throwable t) {
      return null;
    }
  }

  /**
   * Save the SendEmailAfterNewMembership pattern as the rules UI does: root session, the logged in
   * user on the RuleConfig.  By reflection so this compiles where RuleConfig does not exist.
   * @param subject logged in user
   * @param owner the group
   * @param fields recipient, subject, body
   * @param attributeAssignId rule id for an edit, null for an add
   * @return the save result
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Map<String, List<String>> saveUi(final Subject subject, final GrouperObject owner,
      final String[] fields, final String attributeAssignId) {
    return (Map<String, List<String>>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          Class<?> ruleConfigClass = ruleConfigClass();
          Object ruleConfig = ruleConfigClass.getConstructor(Subject.class, GrouperObject.class).newInstance(subject, owner);
          ruleConfigClass.getMethod("setPattern", String.class).invoke(ruleConfig, PATTERN);
          Map<String, String> properties = (Map<String, String>)ruleConfigClass.getMethod("getPatternPropertiesValues").invoke(ruleConfig);
          properties.put(PATTERN + ".emailTo", fields[0]);
          properties.put(PATTERN + ".emailSubject", fields[1]);
          properties.put(PATTERN + ".emailBody", fields[2]);
          Class rulePatternClass = Class.forName("edu.internet2.middleware.grouper.rules.RulePattern");
          Object pattern = Enum.valueOf(rulePatternClass, PATTERN);
          return rulePatternClass.getMethod("save", ruleConfigClass, String.class).invoke(pattern, ruleConfig, attributeAssignId);
        } catch (InvocationTargetException ite) {
          throw new RuntimeException(ite.getCause());
        } catch (RuntimeException re) {
          throw re;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * The rule on this group, as root, or null.
   * @param group group
   * @return the rule's attribute assign
   */
  private static AttributeAssign ruleOn(final Group group) {
    return (AttributeAssign)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Set<AttributeAssign> assigns = group.getAttributeDelegate().retrieveAssignments(RuleUtils.ruleAttributeDefName());
        assertTrue("at most one rule on " + group.getName() + ": " + assigns, GrouperUtil.length(assigns) <= 1);
        return GrouperUtil.length(assigns) == 0 ? null : assigns.iterator().next();
      }
    });
  }

  /**
   * The rule's attribute assign read fresh from the database (no cached delegate values, which
   * could still hold a value the hook refused).
   * @param attributeAssign the rule
   * @return a fresh copy
   */
  private static AttributeAssign fresh(AttributeAssign attributeAssign) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssign.getId(), true, false);
  }

  /**
   * a stored rule value, as root, read fresh
   * @param attributeAssign the rule
   * @param name the attribute name
   * @return the value
   */
  private static String ruleValue(final AttributeAssign attributeAssign, final String name) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return fresh(attributeAssign).getAttributeValueDelegate().retrieveValueString(name);
      }
    });
  }

  /**
   * the three fields with one replaced
   * @param index 0 recipient, 1 subject, 2 body
   * @param value the value for that field
   * @return the fields
   */
  private static String[] fieldsWith(int index, String value) {
    String[] fields = {OK_RECIPIENT, OK_SUBJECT, OK_BODY};
    fields[index] = value;
    return fields;
  }

  /**
   * assert a UI save was refused with this issue's error
   * @param message message
   * @param result save result
   * @param fieldIndex field expected in the error
   * @param expression expression expected in the error
   */
  private static void assertRefused(String message, Map<String, List<String>> result, int fieldIndex, String expression) {
    assertTrue(message + ": " + result, result != null && result.containsKey("ERROR"));
    String expected = RuleEmailTemplateCheck.notAllowedError(FIELD_NAMES[fieldIndex], expression);
    assertTrue(message + ": expected " + expected + " in " + result, result.get("ERROR").contains(expected));
  }

  /**
   * assert a UI save had no error
   * @param message message
   * @param result save result
   */
  private static void assertSaved(String message, Map<String, List<String>> result) {
    assertTrue(message + ": " + result, result == null || !result.containsKey("ERROR"));
  }

  // ---------------------------------------------------------------- the check itself

  /**
   * predefined forms pass, everything else is named
   */
  public void testPredefinedExpressionCheck() {
    assertNull(RuleEmailTemplateCheck.disallowedExpression(null));
    assertNull(RuleEmailTemplateCheck.disallowedExpression("no expressions at all, $ {x}"));
    assertNull(RuleEmailTemplateCheck.disallowedExpression("template: myTemplate"));
    assertNull(RuleEmailTemplateCheck.disallowedExpression(OK_RECIPIENT));
    assertNull(RuleEmailTemplateCheck.disallowedExpression(OK_SUBJECT));
    assertNull(RuleEmailTemplateCheck.disallowedExpression(OK_BODY));
    assertNull(RuleEmailTemplateCheck.disallowedExpression("${stemName} ${membershipEnabledTimestamp} ${roleDisplayName} ${attributeDefNameName}"));

    assertEquals(RULE_EL_UTILS, RuleEmailTemplateCheck.disallowedExpression("Hi " + RULE_EL_UTILS));
    assertEquals(METHOD_CALL, RuleEmailTemplateCheck.disallowedExpression("${groupName} " + METHOD_CALL));
    assertEquals(UNKNOWN_VARIABLE, RuleEmailTemplateCheck.disallowedExpression(UNKNOWN_VARIABLE));
    assertEquals("${grouperUtil.toString()}", RuleEmailTemplateCheck.disallowedExpression("${grouperUtil.toString()}"));
    assertEquals("${group}", RuleEmailTemplateCheck.disallowedExpression("${group}"));
    assertEquals("${safeSubject.getAttributeValue(groupName)}",
        RuleEmailTemplateCheck.disallowedExpression("${safeSubject.getAttributeValue(groupName)}"));
    assertEquals("${'abc'}", RuleEmailTemplateCheck.disallowedExpression("${'abc'}"));
    // nested and unclosed
    assertEquals("${groupName${ruleElUtils}", RuleEmailTemplateCheck.disallowedExpression("${groupName${ruleElUtils}}"));
    assertEquals("${groupName", RuleEmailTemplateCheck.disallowedExpression("text ${groupName"));

    // only sendEmail is checked, wheel may save anything, null subject is not wheel
    assertNull(RuleEmailTemplateCheck.sendEmailExpressionError(SubjectTestHelper.SUBJ0, "veto", RULE_EL_UTILS, RULE_EL_UTILS, RULE_EL_UTILS));
    assertNull(RuleEmailTemplateCheck.sendEmailExpressionError(SubjectFinder.findRootSubject(), "sendEmail", OK_RECIPIENT, RULE_EL_UTILS, OK_BODY));
    assertNull(RuleEmailTemplateCheck.sendEmailExpressionError(SubjectTestHelper.SUBJ0, "sendEmail", OK_RECIPIENT, OK_SUBJECT, OK_BODY));
    assertEquals(RuleEmailTemplateCheck.notAllowedError("subject", RULE_EL_UTILS),
        RuleEmailTemplateCheck.sendEmailExpressionError(SubjectTestHelper.SUBJ0, "SENDEMAIL", OK_RECIPIENT, RULE_EL_UTILS, OK_BODY));
    assertEquals(RuleEmailTemplateCheck.notAllowedError("body", METHOD_CALL),
        RuleEmailTemplateCheck.sendEmailExpressionError(null, "sendEmail", OK_RECIPIENT, OK_SUBJECT, METHOD_CALL));
    assertTrue(RuleEmailTemplateCheck.notAllowedError("subject", "${a<b>}").contains("${a&lt;b&gt;}"));
  }

  // ---------------------------------------------------------------- rules UI path (5.8.0+)

  /**
   * A non-wheel group admin is refused ruleElUtils, a method call and an unknown variable in each
   * of the three fields, and nothing is stored.
   */
  public void testUiPathNonWheelRefused() {
    if (ruleConfigClass() == null) {
      System.out.println("skip testUiPathNonWheelRefused: no RuleConfig/RulePattern (rules UI) in this release");
      return;
    }
    Group group = newGroup("ruleEmailNonWheelRefused");
    for (int fieldIndex = 0; fieldIndex < 3; fieldIndex++) {
      for (String disallowed : DISALLOWED) {
        Map<String, List<String>> result = saveUi(SubjectTestHelper.SUBJ0, group, fieldsWith(fieldIndex, disallowed), null);
        assertRefused("SUBJ0 " + FIELD_NAMES[fieldIndex] + " " + disallowed, result, fieldIndex, disallowed);
        assertNull("refused add stores nothing: " + disallowed, ruleOn(group));
      }
    }
  }

  /**
   * Predefined variables in every field are allowed for a non-wheel author.
   */
  public void testUiPathPredefinedAllowed() {
    if (ruleConfigClass() == null) {
      System.out.println("skip testUiPathPredefinedAllowed: no RuleConfig/RulePattern (rules UI) in this release");
      return;
    }
    Group group = newGroup("ruleEmailPredefinedAllowed");
    assertSaved("SUBJ0 predefined", saveUi(SubjectTestHelper.SUBJ0, group, new String[] {OK_RECIPIENT, OK_SUBJECT, OK_BODY}, null));
    AttributeAssign rule = ruleOn(group);
    assertNotNull(rule);
    assertEquals(OK_RECIPIENT, ruleValue(rule, RuleUtils.ruleThenEnumArg0Name()));
    assertEquals(OK_SUBJECT, ruleValue(rule, RuleUtils.ruleThenEnumArg1Name()));
    assertEquals(OK_BODY, ruleValue(rule, RuleUtils.ruleThenEnumArg2Name()));
  }

  /**
   * Wheel/root may save any expression, and a non-wheel user cannot then edit that rule, even
   * without touching the field with the expression.  A non-wheel edit of a predefined-only rule
   * works.
   */
  public void testUiPathWheelAllowedAndNonWheelEditRefused() {
    if (ruleConfigClass() == null) {
      System.out.println("skip testUiPathWheelAllowedAndNonWheelEditRefused: no RuleConfig/RulePattern (rules UI) in this release");
      return;
    }
    Group group = newGroup("ruleEmailWheelThenEdit");
    String wheelBody = "Hello ${grouperUtil.toString()}";
    assertSaved("root expression", saveUi(SubjectFinder.findRootSubject(), group,
        new String[] {OK_RECIPIENT, OK_SUBJECT, wheelBody}, null));
    AttributeAssign rule = ruleOn(group);
    assertNotNull(rule);
    assertEquals(wheelBody, ruleValue(rule, RuleUtils.ruleThenEnumArg2Name()));

    // SUBJ0 changes only the subject; the form sends the stored body back
    Map<String, List<String>> result = saveUi(SubjectTestHelper.SUBJ0, group,
        new String[] {OK_RECIPIENT, "New subject ${groupName}", wheelBody}, rule.getId());
    assertRefused("SUBJ0 edit of a rule with a wheel expression", result, 2, "${grouperUtil.toString()}");
    assertEquals("refused edit leaves the subject", OK_SUBJECT, ruleValue(rule, RuleUtils.ruleThenEnumArg1Name()));
    assertEquals("refused edit leaves the body", wheelBody, ruleValue(rule, RuleUtils.ruleThenEnumArg2Name()));

    // root makes it predefined-only; then SUBJ0 may edit it
    assertSaved("root edit", saveUi(SubjectFinder.findRootSubject(), group,
        new String[] {OK_RECIPIENT, OK_SUBJECT, OK_BODY}, rule.getId()));
    assertSaved("SUBJ0 edit of predefined rule", saveUi(SubjectTestHelper.SUBJ0, group,
        new String[] {OK_RECIPIENT, "New subject ${groupName}", OK_BODY}, rule.getId()));
    assertEquals("New subject ${groupName}", ruleValue(rule, RuleUtils.ruleThenEnumArg1Name()));
  }

  // ---------------------------------------------------------------- attribute value hook (all releases)

  /**
   * SUBJ0 may assign rule attributes (as WS or GSH would): grant the rule attribute defs
   */
  private static void grantRuleAttributeDefs() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * add a sendEmail rule on the group with the current session, check, recipient (not the subject or body)
   * @param group group
   * @param actAs act-as subject
   * @return the rule
   */
  private static AttributeAssign addEmailRuleStart(Group group, Subject actAs) {
    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.flattenedMembershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), group.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg0Name(), OK_RECIPIENT);
    return attributeAssign;
  }

  /**
   * assign a value and expect the hook to refuse it
   * @param attributeAssign rule
   * @param name attribute name
   * @param value value
   * @param expression expression expected in the error
   */
  private static void assertHookRefuses(AttributeAssign attributeAssign, String name, String value, String expression) {
    try {
      fresh(attributeAssign).getAttributeValueDelegate().assignValue(name, value);
    } catch (RuntimeException re) {
      String stack = GrouperUtil.getFullStackTrace(re);
      assertTrue("refused naming the expression: " + stack, stack.contains(expression));
      return;
    }
    fail("expected the hook to refuse " + name + " = " + value);
  }

  /**
   * A non-wheel session assigning rule values directly is refused ruleElUtils, a method call and an
   * unknown variable in the subject and body, and may assign predefined variables.  The refused value
   * is not stored.
   */
  public void testHookPathNonWheelRefused() {
    Group group = newGroup("ruleEmailHookRefused");
    grantRuleAttributeDefs();
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      AttributeAssign rule = addEmailRuleStart(group, SubjectTestHelper.SUBJ0);
      for (String disallowed : DISALLOWED) {
        assertHookRefuses(rule, RuleUtils.ruleThenEnumArg1Name(), "Hi " + disallowed, disallowed);
        assertHookRefuses(rule, RuleUtils.ruleThenEnumArg2Name(), disallowed, disallowed);
        assertHookRefuses(rule, RuleUtils.ruleThenEnumArg0Name(), disallowed, disallowed);
      }
      assertNull("refused subject not stored", ruleValue(rule, RuleUtils.ruleThenEnumArg1Name()));
      assertEquals("refused recipient not stored", OK_RECIPIENT, ruleValue(rule, RuleUtils.ruleThenEnumArg0Name()));

      // predefined forms are fine
      fresh(rule).getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg1Name(), OK_SUBJECT);
      fresh(rule).getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg2Name(), OK_BODY);
      assertEquals(OK_BODY, ruleValue(rule, RuleUtils.ruleThenEnumArg2Name()));
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * Root writes an expression directly; a non-wheel session then cannot change any value of that
   * rule (here the recipient).
   */
  public void testHookPathNonWheelEditOfWheelRuleRefused() {
    Group group = newGroup("ruleEmailHookWheelThenEdit");
    grantRuleAttributeDefs();
    String wheelBody = "Hello ${grouperUtil.toString()}";
    AttributeAssign rule = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      rule = addEmailRuleStart(group, SubjectFinder.findRootSubject());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg1Name(), OK_SUBJECT);
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg2Name(), wheelBody);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertEquals("root may write an expression", wheelBody, ruleValue(rule, RuleUtils.ruleThenEnumArg2Name()));

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertHookRefuses(rule, RuleUtils.ruleThenEnumArg0Name(), "x@y.z", "${grouperUtil.toString()}");
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertEquals("refused edit leaves the recipient", OK_RECIPIENT, ruleValue(rule, RuleUtils.ruleThenEnumArg0Name()));
  }

}
