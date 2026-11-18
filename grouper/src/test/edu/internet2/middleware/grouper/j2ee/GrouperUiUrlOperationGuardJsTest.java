package edu.internet2.middleware.grouper.j2ee;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security patch for client side CSRF through the UiV2Main.index URL dispatcher (Grouper 5.x, 6.x, 7.x before
 * GRP-7503): commonBottom.jsp must load grouperUiUrlOperationGuard.js right after grouperUi.js, the guard must wrap
 * the dispatcher globals, and its allowlist must hold screens only (never an action like
 * UiV2Group.addMemberSubmit, which a crafted link used to add a subject to the sysadmin group).
 *
 * Also (2026-11-11): operations whose handler acts when a request parameter selects another branch (for example
 * UiV2Admin.daemonJobs with action=disable) must not be in the allowlist of a release where the handler acts.  Which
 * releases that is comes from this release's own handler source (see {@link #KNOWN_ACTING}), so the same file works
 * on every release.
 *
 * These are source checks (there is no browser or JSP harness in grouper-ui tests).  Before the patch they fail
 * because the guard file does not exist.  The grouper-ui dir is found from the working dir (repo root, grouper-ui,
 * or a sibling project); override with -DgrouperUiDir=/path/to/grouper-ui
 */
public class GrouperUiUrlOperationGuardJsTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiUrlOperationGuardJsTest.class);
  }

  /**
   * @param name
   */
  public GrouperUiUrlOperationGuardJsTest(String name) {
    super(name);
  }

  /** the shell footer every UiV2 page includes */
  private static final String COMMON_BOTTOM = "webapp/WEB-INF/grouperUi2/assetsJsp/commonBottom.jsp";

  /** the guard */
  private static final String GUARD_JS = "webapp/grouperExternal/public/assets/js/grouperUiUrlOperationGuard.js";

  /** Class.method */
  private static final Pattern OPERATION_PATTERN = Pattern.compile("^[A-Za-z0-9]+\\.[A-Za-z0-9]+$");

  /** actions that older releases link to with guiV2link or that the reproduction used: must never be allowed from a URL */
  private static final String[] ACTIONS = new String[] {
      "UiV2Group.addMemberSubmit",
      "UiV2Group.groupDeleteSubmit",
      "UiV2Group.deleteRuleOnGroup",
      "UiV2Group.convertGroupToRole",
      "UiV2Stem.deleteRuleOnStem",
      "UiV2Admin.deleteDaemon",
      "UiV2Admin.daemonJobsSchedule",
      "UiV2AttributeDefAction.deleteAttributeDefAction",
      "UiV2Configure.clearConfigCache",
      "UiV2Configure.clearConfigCacheAllNodes",
      "UiV2CustomUiConfig.deleteCustomUiConfig",
      "UiV2GrouperLoader.enableJob",
      "UiV2GrouperLoader.disableJob",
      "UiV2GrouperLoader.failsafeApprove",
      "UiV2ProvisionerConfiguration.deleteProvisionerConfiguration",
      "UiV2ProvisionerConfiguration.runFullSync",
      "UiV2Provisioning.runGroupSync",
      "UiV2Provisioning.removeProvisioningOnGroup",
      "UiV2SubjectSource.deleteSubjectSource",
      "UiV2SubjectSource.disableSource",
      "UiV2LocalEntity.deleteWsJwtKey",
      "UiV2Mcp.deleteMcpRecipe",
  };

  /** screens with Submit in the name that only search or show */
  private static final Set<String> READ_ONLY_SUBMITS = new TreeSet<String>(java.util.Arrays.asList(
      "UiV2Main.searchSubmit", "UiV2Deprovisioning.deprovisioningAffiliationSubmit"));

  /** where the UiV2 handlers are, relative to the grouper-ui dir */
  private static final String SERVICE_LOGIC_DIR = "java/src/edu/internet2/middleware/grouper/grouperUi/serviceLogic/";

  /**
   * operations whose handler is a screen in one branch and an action in another, so a crafted link
   * (UiV2Main.index?operation=...&amp;the params) acts for whoever opens it.  Each row: operation, and a piece of code
   * that is in the handler method body exactly in the releases where it acts (checked against every release tag
   * 2026-11-11).  In those releases the operation must not be in the allowlist; in the others (method missing, or
   * split into a display method and an action method) the row is skipped.
   * <pre>
   *   UiV2ProvisionerConfiguration.diagnostics   all releases (GROUPER_7_BRANCH splits off diagnosticsSubmit, GRP-7503)
   *   UiV2Admin.daemonJobs                       all releases (GROUPER_7_BRANCH passes performAction false, GRP-7503)
   *   UiV2Provisioning.provisioningToOnGroup     4.26.0, 5.8.0-7.8.0 (7.9.0 splits off provisioningToOnGroupSubmit, GRP-7392)
   *   UiV2Provisioning.editProvisioningOnGroup   5.8.0-5.11.0
   * </pre>
   */
  private static final String[][] KNOWN_ACTING = new String[][] {
      // provisionerInitted=true runs diagnostics, which creates the test group and adds the test subject
      {"UiV2ProvisionerConfiguration.diagnostics", "provisioner.provision(GrouperProvisioningType.diagnostics)"},
      // action and jobName run, disable, enable or failsafe-approve a daemon (the two arg helper always performs them)
      {"UiV2Admin.daemonJobs", "daemonJobsHelper(request, response))"},
      // a target without group metadata is assigned to the group at once
      {"UiV2Provisioning.provisioningToOnGroup", "saveOrUpdateProvisioningAttributes("},
      // same, in the releases before provisioningToOnGroup took this over
      {"UiV2Provisioning.editProvisioningOnGroup", "saveOrUpdateProvisioningAttributes("},
  };

  /**
   * @return the grouper-ui project dir or null
   */
  private static File grouperUiDir() {
    String override = System.getProperty("grouperUiDir");
    if (override != null && override.trim().length() > 0) {
      return new File(override.trim());
    }
    File[] candidates = new File[] {new File("."), new File("grouper-ui"), new File("../grouper-ui")};
    for (File candidate : candidates) {
      if (new File(candidate, COMMON_BOTTOM).isFile()) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * @param relativePath
   * @return the file contents
   */
  private static String read(String relativePath) {
    File grouperUiDir = grouperUiDir();
    assertNotNull("Cant find the grouper-ui dir from " + new File(".").getAbsolutePath()
        + ", run from the repo root or pass -DgrouperUiDir", grouperUiDir);
    File file = new File(grouperUiDir, relativePath);
    assertTrue("Missing " + file.getAbsolutePath(), file.isFile());
    try {
      return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException ioe) {
      throw new RuntimeException("Cant read " + file.getAbsolutePath(), ioe);
    }
  }

  /**
   * @return the operations in the allowlist of the guard
   */
  private static Set<String> allowlist() {
    String js = read(GUARD_JS);
    int start = js.indexOf("var allowedOperationList = [");
    assertTrue("allowedOperationList not found in the guard", start != -1);
    int end = js.indexOf("];", start);
    assertTrue("end of allowedOperationList not found in the guard", end != -1);
    Set<String> result = new LinkedHashSet<String>();
    Matcher matcher = Pattern.compile("'([^']*)'").matcher(js.substring(start, end));
    while (matcher.find()) {
      result.add(matcher.group(1));
    }
    return result;
  }

  /**
   * commonBottom.jsp loads the config and then the guard, right after grouperUi.js
   */
  public void testCommonBottomLoadsGuard() {
    String jsp = read(COMMON_BOTTOM);
    int grouperUiIndex = jsp.indexOf("assets/js/grouperUi.js");
    int configIndex = jsp.indexOf("var grouperUrlOperationGuardConfig");
    int guardIndex = jsp.indexOf("<script src=\"../../grouperExternal/public/assets/js/grouperUiUrlOperationGuard.js");
    assertTrue("commonBottom.jsp must load grouperUi.js", grouperUiIndex != -1);
    assertTrue("commonBottom.jsp must write grouperUrlOperationGuardConfig", configIndex != -1);
    assertTrue("commonBottom.jsp must load grouperUiUrlOperationGuard.js", guardIndex != -1);
    assertTrue("the guard must load after grouperUi.js", guardIndex > grouperUiIndex);
    assertTrue("the guard config must be written before the guard loads", configIndex < guardIndex);
    // config values are filtered, never printed raw
    assertTrue("additional operations must be filtered to Class.method",
        jsp.contains("matches(\"[A-Za-z0-9]+\\\\.[A-Za-z0-9]+\")"));
  }

  /**
   * the guard wraps the dispatcher globals and checks the final ajax url
   */
  public void testGuardWrapsDispatcher() {
    String js = read(GUARD_JS);
    assertTrue(js.contains("window.guiV2link = function"));
    assertTrue(js.contains("window.guiProcessUrlForAjax = function"));
    assertTrue(js.contains("window.ajax = function"));
    assertTrue(js.contains("window.processUrl = function"));
    // fail closed on unknown modes
    assertTrue(js.contains("(config.mode === 'logOnly' || config.mode === 'off') ? config.mode : 'enforce'"));
    for (int i = 0; i < js.length(); i++) {
      assertTrue("non ASCII char at " + i, js.charAt(i) < 128);
    }
  }

  /**
   * the allowlist has the main screens and no actions
   */
  public void testAllowlistIsScreensOnly() {
    Set<String> allowlist = allowlist();
    assertTrue("allowlist looks too small: " + allowlist.size(), allowlist.size() > 100);
    for (String screen : new String[] {"UiV2Main.indexMain", "UiV2Group.viewGroup", "UiV2Stem.viewStem",
        "UiV2Subject.viewSubject", "UiV2Public.postIndex"}) {
      assertTrue("screen should be allowed: " + screen, allowlist.contains(screen));
    }
    for (String action : ACTIONS) {
      assertFalse("action must not be allowed from a URL: " + action, allowlist.contains(action));
    }
    Set<String> bad = new TreeSet<String>();
    for (String operation : allowlist) {
      if (!OPERATION_PATTERN.matcher(operation).matches()) {
        bad.add(operation);
      }
      if (operation.endsWith("Submit") && !READ_ONLY_SUBMITS.contains(operation)) {
        bad.add(operation);
      }
    }
    assertTrue("bad entries in the allowlist: " + bad, bad.isEmpty());
  }

  /**
   * @param relativePath
   * @return the file contents, or null if the file is not there
   */
  private static String readIfExists(String relativePath) {
    File grouperUiDir = grouperUiDir();
    assertNotNull("Cant find the grouper-ui dir from " + new File(".").getAbsolutePath()
        + ", run from the repo root or pass -DgrouperUiDir", grouperUiDir);
    if (!new File(grouperUiDir, relativePath).isFile()) {
      return null;
    }
    return read(relativePath);
  }

  /**
   * body of public void methodName(...) { ... } in java source, braces in strings, chars and comments skipped
   * @param source
   * @param methodName
   * @return the body from { to } or null if the method is not there
   */
  static String methodBody(String source, String methodName) {
    int start = source.indexOf("public void " + methodName + "(");
    if (start == -1) {
      return null;
    }
    int open = source.indexOf('{', start);
    if (open == -1) {
      return null;
    }
    int depth = 0;
    int i = open;
    int length = source.length();
    while (i < length) {
      char c = source.charAt(i);
      if (source.startsWith("//", i)) {
        int end = source.indexOf('\n', i);
        i = end == -1 ? length : end;
        continue;
      }
      if (source.startsWith("/*", i)) {
        int end = source.indexOf("*/", i + 2);
        i = end == -1 ? length : end + 2;
        continue;
      }
      if (c == '"' || c == '\'') {
        i++;
        while (i < length && source.charAt(i) != c) {
          if (source.charAt(i) == '\\') {
            i++;
          }
          i++;
        }
        i++;
        continue;
      }
      if (c == '{') {
        depth++;
      } else if (c == '}') {
        depth--;
        if (depth == 0) {
          return source.substring(open, i + 1);
        }
      }
      i++;
    }
    return null;
  }

  /**
   * @return the operations of KNOWN_ACTING whose handler acts in this release (from the handler source)
   */
  private static Set<String> knownActingInThisRelease() {
    Set<String> result = new TreeSet<String>();
    for (String[] row : KNOWN_ACTING) {
      String operation = row[0];
      String className = operation.substring(0, operation.indexOf('.'));
      String methodName = operation.substring(operation.indexOf('.') + 1);
      String source = readIfExists(SERVICE_LOGIC_DIR + className + ".java");
      if (source == null) {
        System.out.println("skip " + operation + ": " + className + ".java not in this release");
        continue;
      }
      String body = methodBody(source, methodName);
      if (body == null) {
        System.out.println("skip " + operation + ": method not in this release");
        continue;
      }
      if (body.contains(row[1])) {
        result.add(operation);
      } else {
        System.out.println("skip " + operation + ": handler only displays in this release");
      }
    }
    return result;
  }

  /**
   * a handler that acts when a request parameter selects another branch must not be allowed from a URL in a release
   * where it acts.  Also checks the source reader on a made up handler, so the check can not pass by not finding code
   */
  public void testKnownActingOperationsNotAllowed() {
    // the body reader handles braces in strings and comments
    String sample = "class A {\n  public void screen(HttpServletRequest request) {\n    String s = \"}\"; // }\n"
        + "    /* } */ char c = '}';\n    if (true) { save(\"{\"); }\n  }\n  public void other() { }\n}\n";
    String sampleBody = methodBody(sample, "screen");
    assertNotNull(sampleBody);
    assertTrue(sampleBody, sampleBody.contains("save(") && !sampleBody.contains("other()"));
    assertNull(methodBody(sample, "missing"));

    // every operation in the table is a Class.method
    for (String[] row : KNOWN_ACTING) {
      assertTrue(row[0], OPERATION_PATTERN.matcher(row[0]).matches());
    }

    Set<String> acting = knownActingInThisRelease();
    // every release has diagnostics and daemonJobs; they act in all of them (only GROUPER_7_BRANCH, which has no
    // guard js, splits them), so if neither is found the source reader is broken
    assertFalse("found no acting handler, is " + SERVICE_LOGIC_DIR + " right?", acting.isEmpty());

    Set<String> allowlist = allowlist();
    Set<String> allowedButActing = new TreeSet<String>();
    for (String operation : acting) {
      if (allowlist.contains(operation)) {
        allowedButActing.add(operation);
      }
    }
    assertTrue("operations that act in this release must not be allowed from a URL: " + allowedButActing,
        allowedButActing.isEmpty());
  }

  /**
   * if the GRP-7503 server guard is on the classpath (mainline), the client allowlist should be the same list
   * (except operations of KNOWN_ACTING, which the client list may leave out)
   * @throws Exception
   */
  public void testAllowlistMatchesServerGuardIfPresent() throws Exception {
    Class<?> serverGuard = null;
    try {
      serverGuard = Class.forName("edu.internet2.middleware.grouper.j2ee.GrouperUiUrlOperationGuard");
    } catch (ClassNotFoundException cnfe) {
      // releases before GRP-7503 do not have it
      return;
    }
    Method method = serverGuard.getMethod("allowedOperations");
    @SuppressWarnings("unchecked")
    Collection<String> serverList = (Collection<String>) method.invoke(null);
    Set<String> clientOnly = new TreeSet<String>(allowlist());
    clientOnly.removeAll(serverList);
    Set<String> serverOnly = new TreeSet<String>(serverList);
    serverOnly.removeAll(allowlist());
    for (String[] row : KNOWN_ACTING) {
      serverOnly.remove(row[0]);
    }
    assertTrue("client allowlist differs from GrouperUiUrlOperationGuard, client only: " + clientOnly
        + ", server only: " + serverOnly, clientOnly.isEmpty() && serverOnly.isEmpty());
  }
}
