package edu.internet2.middleware.grouper.abac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import junit.textui.TestRunner;

/**
 * <pre>
 * Scripted-group (JEXL_SCRIPT loader) scripts in 2.6.x and 4.x used to run as JEXL with static
 * class access, in the UI on the loader edit form refresh and on save, and in the daemon on every
 * full sync.  After the fix a script must be entity.memberOf('...') combined with and / or / not
 * and parentheses (GrouperAbacScriptAllowlist), is evaluated without JEXL, a non-wheel editor
 * must be able to READ every group it names (current name only), and the full sync skips a
 * stored script that fails the allowlist.
 *
 * Before the fix this test does not compile (GrouperAbacScriptAllowlist does not exist); with
 * only the class added, the "not executed" checks fail (validScript and the full sync run the
 * script and set a system property).
 *
 * The UI calls GrouperAbacScriptAllowlist.validScriptForEditor(script, loggedInSubject) on the
 * form refresh and on save; this test calls it directly (see the manual test steps for the screen).
 *
 * Same file for every patched release (2.6.19, 4.17.7 to 4.26.0): Java 8, GrouperTest base.
 * </pre>
 */
public class GrouperAbacScriptAllowlistTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperAbacScriptAllowlistTest("testCodeIsNotExecuted"));
  }

  /** */
  public GrouperAbacScriptAllowlistTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperAbacScriptAllowlistTest(String name) {
    super(name);
  }

  /** set by the payloads below if they ever run */
  private static final String PROOF_PROPERTY = "grouperAbacScriptAllowlistTestRan";

  /** the reported payload shapes, made harmless: they only set a system property */
  private static final String[] CODE_PAYLOADS = new String[] {
    "${''.class.forName('java.lang.System').setProperty('" + PROOF_PROPERTY + "', 'true') == null}",
    "${java.lang.System.setProperty('" + PROOF_PROPERTY + "', 'true') == null}",
    "${edu.internet2.middleware.grouper.util.GrouperUtil.gshRunScript(\"System.setProperty('" + PROOF_PROPERTY + "', 'true');\") != null}",
    "${entity.memberOf('test:abacAllowlist:readable') || ''.class.forName('java.lang.System').setProperty('" + PROOF_PROPERTY + "', 'true') == null}",
  };

  /** root session */
  private GrouperSession rootSession;

  /** SUBJ0 can READ */
  private Group readable;

  /** SUBJ0 can VIEW, not READ */
  private Group viewOnly;

  /** SUBJ0 has nothing */
  private Group hidden;

  // SUBJ0: non-wheel editor, SUBJ3: wheel

  @Override
  protected void setUp() {
    super.setUp();
    System.clearProperty(PROOF_PROPERTY);
    this.rootSession = GrouperSession.startRootSession();

    Group wheel = new GroupSave(this.rootSession).assignName("test:abacAllowlistWheel")
        .assignCreateParentStemsIfNotExist(true).save();
    wheel.addMember(SubjectTestHelper.SUBJ3, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    PrivilegeHelper.wheelMemberCacheClear();

    this.readable = newGroupNoAllPrivs("test:abacAllowlist:readable");
    this.readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    this.viewOnly = newGroupNoAllPrivs("test:abacAllowlist:viewOnly");
    this.viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.hidden = newGroupNoAllPrivs("test:abacAllowlist:hidden");
  }

  @Override
  protected void tearDown() {
    System.clearProperty(PROOF_PROPERTY);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.use");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.group");
    PrivilegeHelper.wheelMemberCacheClear();
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * a group that GrouperAll cannot READ or VIEW
   * @param name
   * @return the group
   */
  private Group newGroupNoAllPrivs(String name) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    return group;
  }

  /**
   * evaluate with an entity in these groups
   * @param script
   * @param groupNames
   * @return the result
   */
  private static boolean evaluate(String script, String... groupNames) {
    GrouperAbacEntity entity = new GrouperAbacEntity();
    Set<String> memberOf = new HashSet<String>();
    for (String groupName : groupNames) {
      memberOf.add(groupName);
    }
    entity.setMemberOfGroupNames(memberOf);
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("entity", entity);
    // GrouperAbac.runScriptStatic is what the full sync calls per member
    Object result = GrouperAbac.runScriptStatic(script, variableMap);
    assertEquals(script, Boolean.valueOf(GrouperAbacScriptAllowlist.evaluate(script, entity)), result);
    return ((Boolean)result).booleanValue();
  }

  /**
   * a scripted group as the loader screen saves it (as root)
   * @param name
   * @param script
   * @return the group
   */
  private Group scriptedGroup(String name, String script) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName marker = AttributeDefNameFinder.findByName(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(marker).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(
        GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT, script);
    return group;
  }

  // ---------------------------------------------------------------- tests

  /**
   * memberOf with and / or / not / parentheses is accepted and evaluated like JEXL would
   */
  public void testAllowedScripts() {
    String[] allowed = new String[] {
      "${entity.memberOf('a:b')}",
      "  ${ entity.memberOf( 'a:b' ) }  ",
      "${entity.memberOf('a:b') && !entity.memberOf('a:c')}",
      "${(entity.memberOf('a:b') || entity.memberOf('a:c')) && not entity.memberOf('a:d')}",
      "${entity.memberOf('a:b') and entity.memberOf('a:c') or entity.memberOf('a:d')}",
      "${!(entity.memberOf('a:b with space'))}",
      "${entity.memberOf('a:b')\n  || entity.memberOf('a:c')}",
    };
    for (String script : allowed) {
      assertNull(script, GrouperAbacScriptAllowlist.allowlistErrorMessage(script));
      assertNull(script, GrouperAbac.validScript(script));
    }

    assertEquals(3, GrouperAbacScriptAllowlist.groupNames(allowed[3]).size());
    assertTrue(GrouperAbacScriptAllowlist.groupNames(allowed[5]).contains("a:b with space"));

    assertTrue(evaluate("${entity.memberOf('a:b')}", "a:b"));
    assertFalse(evaluate("${entity.memberOf('a:b')}", "a:c"));
    assertTrue(evaluate("${entity.memberOf('a:b') && !entity.memberOf('a:c')}", "a:b"));
    assertFalse(evaluate("${entity.memberOf('a:b') && !entity.memberOf('a:c')}", "a:b", "a:c"));
    // && binds tighter than ||, ! tighter than &&
    assertTrue(evaluate("${entity.memberOf('a:x') || entity.memberOf('a:b') && entity.memberOf('a:c')}", "a:x"));
    assertFalse(evaluate("${(entity.memberOf('a:x') || entity.memberOf('a:b')) && entity.memberOf('a:c')}", "a:x"));
    assertTrue(evaluate("${!entity.memberOf('a:b') && entity.memberOf('a:c')}", "a:c"));
    assertTrue(evaluate("${not (entity.memberOf('a:b') or entity.memberOf('a:c'))}"));
    assertFalse(evaluate("${not (entity.memberOf('a:b') or entity.memberOf('a:c'))}", "a:c"));
  }

  /**
   * anything else is refused, with a message and without running it
   */
  public void testCodeIsNotExecuted() {
    String[] refused = new String[] {
      CODE_PAYLOADS[0], CODE_PAYLOADS[1], CODE_PAYLOADS[2], CODE_PAYLOADS[3],
      "${entity.memberOf(\"a:b\")}",
      "${entity.memberOf('a:b') == true}",
      "${entity.memberOf('a:b') != false}",
      "${entity.memberOf('a\\'b')}",
      "${entity.memberOf('')}",
      "${entity . memberOf('a:b')}",
      "${entity.memberOfX('a:b')}",
      "${entity.memberOf('a:b') ; entity.memberOf('a:c')}",
      "${entity.memberOf('a:b')} ${entity.memberOf('a:c')}",
      "${entity.memberOf('a:b') & entity.memberOf('a:c')}",
      "${entity.memberOf('a:b') andentity.memberOf('a:c')}",
      "${(entity.memberOf('a:b')}",
      "${true}",
      "${}",
      "entity.memberOf('a:b')",
      "${entity.multiValuedGroupExtensionInFolder('a').size() > 0}",
      "${grouperUtil.toString('a') == 'a'}",
      null,
    };
    for (String script : refused) {
      assertNotNull(script, GrouperAbacScriptAllowlist.allowlistErrorMessage(script));
      String validScriptMessage = GrouperAbac.validScript(script);
      assertTrue(script, validScriptMessage != null && validScriptMessage.length() > 0);
      try {
        evaluate(script, "test:abacAllowlist:readable");
        fail("should not evaluate: " + script);
      } catch (RuntimeException re) {
        // expected
      }
    }
    // even wheel/root cannot save one, and checking it does not run it
    for (String script : CODE_PAYLOADS) {
      assertNotNull(script, GrouperAbacScriptAllowlist.validScriptForEditor(script, SubjectFinder.findRootSubject()));
      assertNotNull(script, GrouperAbacScriptAllowlist.validScriptForEditor(script, SubjectTestHelper.SUBJ3));
    }
    assertNull("a refused script was executed", System.getProperty(PROOF_PROPERTY));

    // a nesting bomb is refused, not a stack overflow
    StringBuilder deep = new StringBuilder("${");
    for (int i = 0; i < 5000; i++) {
      deep.append("!(");
    }
    deep.append("entity.memberOf('a:b')");
    for (int i = 0; i < 5000; i++) {
      deep.append(")");
    }
    deep.append("}");
    assertNotNull(GrouperAbacScriptAllowlist.allowlistErrorMessage(deep.toString()));
  }

  /**
   * a non-wheel editor must READ every group the script names; wheel need not
   */
  public void testEditorNeedsReadOnEveryGroup() {
    String readableScript = "${entity.memberOf('test:abacAllowlist:readable')}";
    assertNull(GrouperAbacScriptAllowlist.validScriptForEditor(readableScript, SubjectTestHelper.SUBJ0));

    String[] notReadable = new String[] {
      "test:abacAllowlist:viewOnly",
      "test:abacAllowlist:hidden",
      "test:abacAllowlist:doesNotExist",
    };
    String firstMessage = null;
    for (String groupName : notReadable) {
      String script = "${entity.memberOf('test:abacAllowlist:readable') && !entity.memberOf('" + groupName + "')}";
      String message = GrouperAbacScriptAllowlist.validScriptForEditor(script, SubjectTestHelper.SUBJ0);
      assertNotNull(groupName, message);
      assertTrue(message, message.contains(groupName));
      assertFalse(message, message.contains("test:abacAllowlist:readable"));
      // one message for every reason, so it does not tell whether the group exists
      String generic = message.replace(groupName, "X");
      if (firstMessage == null) {
        firstMessage = generic;
      } else {
        assertEquals(firstMessage, generic);
      }

      // wheel and root may use any group (they can read them all)
      assertNull(groupName, GrouperAbacScriptAllowlist.validScriptForEditor(script, SubjectTestHelper.SUBJ3));
      assertNull(groupName, GrouperAbacScriptAllowlist.validScriptForEditor(script, SubjectFinder.findRootSubject()));
    }

    // an old name after a rename is not accepted (the full sync does not resolve it either)
    Group renamed = newGroupNoAllPrivs("test:abacAllowlist:beforeRename");
    renamed.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    renamed.setExtension("afterRename");
    renamed.store();
    assertNotNull(GrouperAbacScriptAllowlist.validScriptForEditor(
        "${entity.memberOf('test:abacAllowlist:beforeRename')}", SubjectTestHelper.SUBJ0));
    assertNull(GrouperAbacScriptAllowlist.validScriptForEditor(
        "${entity.memberOf('test:abacAllowlist:afterRename')}", SubjectTestHelper.SUBJ0));
  }

  /**
   * the full sync daemon skips a stored script that fails the allowlist, and still syncs a good one
   */
  public void testFullSyncSkipsDisallowedScript() {
    this.readable.addMember(SubjectTestHelper.SUBJ1, false);
    this.readable.addMember(SubjectTestHelper.SUBJ2, false);
    this.viewOnly.addMember(SubjectTestHelper.SUBJ2, false);

    Group bad = scriptedGroup("test:abacAllowlist:scriptedBad", CODE_PAYLOADS[3]);
    Group good = scriptedGroup("test:abacAllowlist:scriptedGood",
        "${entity.memberOf('test:abacAllowlist:readable') && !entity.memberOf('test:abacAllowlist:viewOnly')}");

    try {
      GrouperLoaderJexlScriptFullSync.runDaemonStandalone();
    } catch (RuntimeException re) {
      // other scripted groups in the test database may have errors; ours are checked below
      System.out.println("full sync threw (checking this test's groups anyway): " + re.getMessage());
    }

    assertNull("the stored script was executed by the full sync", System.getProperty(PROOF_PROPERTY));
    assertFalse(bad.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(good.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(good.hasMember(SubjectTestHelper.SUBJ2));
  }
}
