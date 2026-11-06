package edu.internet2.middleware.grouper.j2ee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * GRP-7503: every screen the UI links to (guiV2link, handleGuiV2LinkClick, href="?operation=...") must be in
 * {@link GrouperUiUrlOperationGuard} allowlist, otherwise that link fails with "This action cannot be run from a link".
 * And every operation in the allowlist must exist
 */
public class GrouperUiUrlOperationGuardTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiUrlOperationGuardTest.class);
  }

  /**
   * @param name
   */
  public GrouperUiUrlOperationGuardTest(String name) {
    super(name);
  }

  /** operation in a url, e.g. operation=UiV2Group.viewGroup */
  private static final Pattern OPERATION_PATTERN = Pattern.compile("operation=([A-Za-z0-9]+)\\.([A-Za-z0-9]+)");

  /** jsp comment */
  private static final Pattern JSP_COMMENT_PATTERN = Pattern.compile("<%--.*?--%>", Pattern.DOTALL);

  /** service logic method */
  private static final String SERVICE_LOGIC_DIR = "java/src/edu/internet2/middleware/grouper/grouperUi/serviceLogic/";

  /**
   * grouper-ui project dir, works if run from grouper-ui (maven) or the repo root (IDE)
   * @return the dir or null if not found
   */
  private static File grouperUiDir() {
    File[] candidates = new File[] {new File("."), new File("grouper-ui"), new File("../grouper-ui")};
    for (File candidate : candidates) {
      if (new File(candidate, "webapp/WEB-INF/grouperUi2").isDirectory() && new File(candidate, SERVICE_LOGIC_DIR).isDirectory()) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * every linked screen is allowed
   */
  public void testLinkedOperationsAreAllowed() {
    File grouperUiDir = grouperUiDir();
    if (grouperUiDir == null) {
      System.out.println("Cant find grouper-ui source dir, skipping " + this.getName());
      return;
    }

    List<File> files = new ArrayList<File>();
    addFiles(new File(grouperUiDir, "webapp/WEB-INF"), files);
    addFiles(new File(grouperUiDir, "webapp/grouperExternal/public/assets/js/grouperUi.js"), files);
    addFiles(new File(grouperUiDir, "java/src"), files);
    addFiles(new File(grouperUiDir, "../grouper/conf"), files);
    addFiles(new File(grouperUiDir, "../grouper/src/grouper"), files);

    // operation to first file that links to it
    Map<String, String> notAllowed = new TreeMap<String, String>();

    for (File file : files) {
      String contents = GrouperUtil.readFileIntoString(file);
      if (file.getName().endsWith(".jsp")) {
        contents = JSP_COMMENT_PATTERN.matcher(contents).replaceAll("");
      }
      Matcher matcher = OPERATION_PATTERN.matcher(contents);
      while (matcher.find()) {
        String className = matcher.group(1);
        String methodName = matcher.group(2);
        String operation = className + "." + methodName;
        if (GrouperUiUrlOperationGuard.allowedOperations().contains(operation)) {
          continue;
        }
        // links to things that are not ui operations (old ui, examples in comments) can not run anyway
        if (!serviceLogicMethodExists(grouperUiDir, className, methodName)) {
          continue;
        }
        if (!notAllowed.containsKey(operation)) {
          notAllowed.put(operation, file.getPath());
        }
      }
    }

    assertTrue("These operations are linked to but are not in GrouperUiUrlOperationGuard allowedOperations.  "
        + "If they are screens (do not change anything) add them, if they are actions make the link an ajax() button: " 
        + notAllowed, notAllowed.size() == 0);
  }

  /**
   * every allowed operation exists
   */
  public void testAllowedOperationsExist() {
    File grouperUiDir = grouperUiDir();
    if (grouperUiDir == null) {
      System.out.println("Cant find grouper-ui source dir, skipping " + this.getName());
      return;
    }
    Set<String> missing = new TreeSet<String>();
    for (String operation : GrouperUiUrlOperationGuard.allowedOperations()) {
      String className = GrouperUtil.prefixOrSuffix(operation, ".", true);
      String methodName = GrouperUtil.prefixOrSuffix(operation, ".", false);
      if (!serviceLogicMethodExists(grouperUiDir, className, methodName)) {
        missing.add(operation);
      }
    }
    assertTrue("These operations in GrouperUiUrlOperationGuard allowedOperations do not exist: " + missing, missing.size() == 0);
  }

  /** class name to source */
  private Map<String, String> serviceLogicSource = new TreeMap<String, String>();

  /**
   * @param grouperUiDir
   * @param className
   * @param methodName
   * @return true if there is a public void method(HttpServletRequest ...) in the service logic class
   */
  private boolean serviceLogicMethodExists(File grouperUiDir, String className, String methodName) {
    if (!serviceLogicSource.containsKey(className)) {
      File file = new File(grouperUiDir, SERVICE_LOGIC_DIR + className + ".java");
      serviceLogicSource.put(className, file.isFile() ? GrouperUtil.readFileIntoString(file) : "");
    }
    String source = serviceLogicSource.get(className);
    return Pattern.compile("public\\s+(static\\s+)?void\\s+" + methodName + "\\s*\\(\\s*(final\\s+)?HttpServletRequest").matcher(source).find();
  }

  /**
   * @param fileOrDir
   * @param files jsp, js, java, properties files
   */
  private static void addFiles(File fileOrDir, List<File> files) {
    if (fileOrDir.isFile()) {
      String name = fileOrDir.getName();
      if (name.endsWith(".jsp") || name.endsWith(".js") || name.endsWith(".java") || name.endsWith(".properties")) {
        files.add(fileOrDir);
      }
      return;
    }
    File[] children = fileOrDir.listFiles();
    if (children == null) {
      return;
    }
    for (File child : children) {
      addFiles(child, files);
    }
  }
}
