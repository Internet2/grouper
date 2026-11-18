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
package edu.internet2.middleware.grouper.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.limits.LimitElUtils;
import edu.internet2.middleware.grouper.rules.RuleApi;
import junit.textui.TestRunner;

/**
 * SECURITY tests for GrouperJexlSandbox: expressions evaluated with allowStaticClasses=false
 * (the 2-arg default, rules incl. predefined send-email rules, permission limits, external
 * subject descriptions) must not reach GSH, files, reflection, class loading or constructors,
 * while the documented helpers and public methods of passed-in variables keep working.
 *
 * Before the fix, the GrouperUtil injected as "grouperUtil" was unrestricted, so a non-wheel
 * user who could write a send-email rule subject could run grouperUtil.gshRunScript(...) as
 * GrouperSystem.  This class does not compile before the fix (GrouperJexlSandbox does not exist),
 * and the gshRunScript cases would run the marker script.
 */
public class GrouperJexlSandboxTest extends GrouperTest {

  /** system property the GSH marker script sets if it ever runs */
  private static final String MARKER = "grouperJexlSandboxTestMarker";

  /** GSH script that sets the marker */
  private static final String MARKER_GSH = "System.setProperty('" + MARKER + "', 'ran')";

  /** EL that tries to run the marker GSH */
  private static final String GSH_EL = "${grouperUtil.gshRunScript(\"" + MARKER_GSH + "\")}";

  /**
   *
   */
  public GrouperJexlSandboxTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperJexlSandboxTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperJexlSandboxTest("testSendEmailRuleCannotRunGsh"));
  }

  @Override
  protected void setUp() {
    super.setUp();
    System.clearProperty(MARKER);
  }

  @Override
  protected void tearDown() {
    System.clearProperty(MARKER);
    super.tearDown();
  }

  // ------------------------------------------------------------------------------------------
  // helpers
  // ------------------------------------------------------------------------------------------

  /**
   * evaluate EL in all three restricted modes; each must throw or return without the payload
   * having run.  Silent mode is also tried, it must swallow (not run) the call.
   * @param el the ${...} expression
   * @param forbiddenInResult text that must not be in a returned result (e.g. a class name)
   */
  private void assertDeniedAllModes(String el, String forbiddenInResult) {
    // default injected grouperUtil (GrouperUtil on 6.x / 7.x, GrouperUtilElSafe on 2.6 / 4.x / 5.x),
    // and a full GrouperUtil in the map, like RuleDefinition.addElVariables adds for
    // rules.accessToApiInEl (that is how a send-email rule reaches gshRunScript on every release)
    for (boolean fullGrouperUtil : new boolean[] {false, true}) {
      for (boolean silent : new boolean[] {false, true}) {
        String label = " silent=" + silent + " fullGrouperUtil=" + fullGrouperUtil;
        assertDenied("expression" + label, el, forbiddenInResult, 0, silent, fullGrouperUtil);
        assertDenied("script" + label, el, forbiddenInResult, 1, silent, fullGrouperUtil);
        assertDenied("template" + label, el, forbiddenInResult, 2, silent, fullGrouperUtil);
      }
    }
  }

  /**
   * @param label
   * @param el
   * @param forbiddenInResult
   * @param mode 0 expression, 1 script, 2 template
   * @param silent
   * @param fullGrouperUtil true to put a full GrouperUtil in the map as grouperUtil
   */
  private void assertDenied(String label, String el, String forbiddenInResult, int mode, boolean silent, boolean fullGrouperUtil) {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    if (fullGrouperUtil) {
      variableMap.put("grouperUtil", new GrouperUtil());
    }
    Object result = null;
    try {
      if (mode == 0) {
        result = GrouperUtil.substituteExpressionLanguage(el, variableMap, false, silent, true);
      } else if (mode == 1) {
        result = GrouperUtil.substituteExpressionLanguageScript(el, variableMap, false, silent, true);
      } else {
        result = GrouperUtil.substituteExpressionLanguageTemplate(el, variableMap, false, silent, true);
      }
    } catch (RuntimeException re) {
      // denied with an error, fine
    }
    assertNull(label + ": GSH marker must not be set by " + el, System.getProperty(MARKER));
    if (result != null && forbiddenInResult != null) {
      assertFalse(label + ": result of " + el + " must not contain " + forbiddenInResult + ": " + result,
          GrouperUtil.stringValue(result).contains(forbiddenInResult));
    }
  }

  // ------------------------------------------------------------------------------------------
  // tests
  // ------------------------------------------------------------------------------------------

  /**
   * GSH, file, reflection and class helpers on grouperUtil are denied in expression, script and
   * template mode, as is reflection through ''.class and new(...)
   */
  public void testDangerousCallsDenied() {

    assertDeniedAllModes(GSH_EL, null);

    // reflection through any object
    assertDeniedAllModes("${''.class.forName('java.lang.System').getName()}", "java.lang.System");
    assertDeniedAllModes("${''.getClass().getName()}", "java.lang.String");
    assertDeniedAllModes("${grouperUtil.forName('java.lang.System').getName()}", "java.lang.System");
    assertDeniedAllModes("${grouperUtil.callMethod('abc', 'toUpperCase')}", "ABC");

    // constructors
    assertDeniedAllModes("${new('java.lang.StringBuilder', 'xyzzy').reverse().toString()}", "yzzyx");

    // files
    String tempDir = System.getProperty("java.io.tmpdir");
    assertDeniedAllModes("${grouperUtil.fileCanonicalPath(grouperUtil.fileFromResourceName('grouper.base.properties'))}", "grouper.base.properties");
    assertDeniedAllModes("${grouperUtil.tmpDir()}", tempDir);
  }

  /**
   * the helpers used by base config and the docs still work in all three modes, and public
   * methods of a passed-in variable still work (what GRP-6773 needed)
   */
  public void testAllowedHelpersStillWork() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", SubjectTestHelper.SUBJ0);
    variableMap.put("name", "John");
    // a full GrouperUtil, so the allowlist itself is tested on every release (2.6 / 4.x / 5.x
    // inject GrouperUtilElSafe by default, which has no escapeHtml)
    variableMap.put("grouperUtil", new GrouperUtil());

    String el = "${grouperUtil.xmlEscape('<a>')}|${grouperUtil.defaultString(null)}|${grouperUtil.escapeHtml('&', true)}"
        + "|${grouperUtil.appendPrefixIfStringNotBlank('[x]', ' ', grouperUtil.appendIfNotBlankString(name, ' - ', 'Penn'))}"
        + "|${grouperUtil.abbreviate('abcdefghij', 6)}|${subject.getId()}";
    String expected = "&lt;a&gt;||&amp;|[x] John - Penn|abc...|" + SubjectTestHelper.SUBJ0.getId();

    assertEquals(expected, GrouperUtil.substituteExpressionLanguage(el, variableMap, false, false, false));
    assertEquals(expected, GrouperUtil.substituteExpressionLanguageTemplate(el, variableMap, false, false, false));
    assertEquals(SubjectTestHelper.SUBJ0.getId(), GrouperUtil.substituteExpressionLanguageScript(
        "${subject.getId()}", variableMap, false, false, false));

    // 2-arg default (rules use it) with the default injected grouperUtil (GrouperUtil on 6.x / 7.x,
    // GrouperUtilElSafe before; GrouperUtil itself has toLowerCase / capitalizeFully from 6.0.0)
    assertEquals("ab Ab Cd", GrouperUtil.substituteExpressionLanguage(
        "${grouperUtil.toLowerCase('AB')} ${grouperUtil.capitalizeFully('aB cD')}", new HashMap<String, Object>()));

    // arrays and maps still work
    variableMap.put("arr", new String[] {"x", "y"});
    Map<String, Object> theMap = new HashMap<String, Object>();
    theMap.put("k", "v");
    variableMap.put("theMap", theMap);
    assertEquals("y v", GrouperUtil.substituteExpressionLanguage("${arr[1]} ${theMap.k}", variableMap, false, false, false));
    assertNotNull(GrouperUtil.substituteExpressionLanguage("${grouperUtil.hostname()}", variableMap, false, false, false));
  }

  /**
   * permission limit EL keeps limitElUtils, and still cannot reach GSH
   */
  public void testPermissionLimitStyleEl() {
    Map<String, Object> limitEnvVars = new HashMap<String, Object>();
    limitEnvVars.put("limitElUtils", new LimitElUtils());
    limitEnvVars.put("ipAddress", "1.2.3.4");
    assertEquals("true", GrouperUtil.substituteExpressionLanguage(
        "${limitElUtils.ipOnNetworks(ipAddress, '1.2.3.0/24')}", limitEnvVars, false, false, false));
    try {
      GrouperUtil.substituteExpressionLanguage(GSH_EL, limitEnvVars, false, false, false);
    } catch (RuntimeException re) {
      // denied
    }
    assertNull(System.getProperty(MARKER));
  }

  /**
   * control: the privileged evaluator (allowStaticClasses=true, sysadmin config only) still has
   * the full GrouperUtil.  This also proves the marker would be seen if the sandbox let it run.
   */
  public void testPrivilegedEvaluatorUnchanged() {
    // gshRunScript needs an open session on 4.x (the sysadmin evaluator runs as root anyway)
    GrouperSession.startRootSession();
    // full GrouperUtil in the map: 2.6 / 4.x / 5.x inject GrouperUtilElSafe by default even here
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("grouperUtil", new GrouperUtil());
    GrouperUtil.substituteExpressionLanguage(GSH_EL, variableMap, true, false, false);
    assertEquals("ran", System.getProperty(MARKER));
  }

  /**
   * the verified attack: a predefined send-email rule whose subject calls grouperUtil.gshRunScript
   * to delete a group.  The rule acts as GrouperSystem (which also has rules.accessToApiInEl, so
   * RuleDefinition itself puts a full GrouperUtil in the map).  The GSH must not run, the victim
   * group must survive, and the email still goes out.
   */
  public void testSendEmailRuleCannotRunGsh() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    // init the rules consumer first (like RuleTest): a new consumer starts at the max change log
    // sequence, so without this the membership add below is skipped and the rule never fires
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    Group ruleGroup = new GroupSave(grouperSession).assignName("test:jexlSandboxRuleGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(grouperSession).assignName("test:jexlSandboxVictim")
        .assignCreateParentStemsIfNotExist(true).save();

    String subjectEl = "${grouperUtil.gshRunScript(\"delGroup('test:jexlSandboxVictim'); " + MARKER_GSH + "\")}";

    RuleApi.emailOnFlattenedMembershipAdd(SubjectFinder.findRootSubject(), ruleGroup,
        "a@b.c", "Added: ${groupDisplayExtension} " + subjectEl, "body");

    long initialEmailCount = GrouperEmail.testingEmailCount;

    ruleGroup.addMember(SubjectTestHelper.SUBJ0);

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertNull("GSH in the email subject must not run", System.getProperty(MARKER));
    assertNotNull("victim group must still exist",
        GroupFinder.findByName(grouperSession, "test:jexlSandboxVictim", false));

    // From 5.19.0 / 6.0.0 sendEmail is failsafe (GRP-6247): the subject falls back to the raw
    // template and the email still goes out.  Before that the denied expression stops the rule
    // and no email is sent, which is also safe.
    if (GrouperEmail.testingEmailCount == initialEmailCount) {
      System.out.println("skip email check in testSendEmailRuleCannotRunGsh: sendEmail on this release is not failsafe (GRP-6247), the denied expression stopped the rule");
      return;
    }
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
    List<GrouperEmail> emails = GrouperEmail.testingEmails();
    if (emails != null && emails.size() > 0) {
      String emailSubject = emails.get(emails.size() - 1).getSubject();
      assertFalse("email subject must not have GSH output: " + emailSubject,
          GrouperUtil.defaultString(emailSubject).contains("Output:"));
    }
  }

}
