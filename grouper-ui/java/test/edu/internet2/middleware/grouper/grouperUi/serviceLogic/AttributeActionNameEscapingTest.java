package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionLimitBeanContainer;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperUiFunctions;
import junit.textui.TestRunner;

/**
 * Stored XSS through attribute-definition action names (and the other places an action name is
 * printed). A delegated ATTR_ADMIN can create an action named e.g.
 * <code>&lt;svg/onload=document.title=7332&gt;</code> (40 chars max) through the UI or WS, and
 * the name was printed raw on the Attribute actions page, the action edit page, the six
 * permission-limit screens, the group/subject permission matrix (inside form field names, ids,
 * a URL and javascript strings in onclick), and the limit "assigned to" tooltip built in
 * GuiPermissionLimitBeanContainer.
 *
 * There is no JSP rendering harness in grouper-ui tests, so the JSP part is a static source check
 * of the ten JSPs (every EL output that carries an action name is escaped for its context), plus
 * checks of the escape functions the JSPs rely on, plus a database test of the java tooltip.
 */
public class AttributeActionNameEscapingTest extends GrouperTest {

  /** the reproduction payload, fits the 40 char action name column */
  private static final String PAYLOAD = "<svg/onload=document.title=7332>";

  /** directory of the v2 UI JSPs, relative to grouper-ui */
  private static final String UI2 = "webapp/WEB-INF/grouperUi2/";

  /** the JSPs that print an action name */
  private static final String[] JSPS = {
      "attributeDefAction/attributeDefActions.jsp",
      "attributeDefAction/attributeDefActionEdit.jsp",
      "groupPermissions/groupPermissionLimitAddValue.jsp",
      "groupPermissions/groupPermissionLimitEdit.jsp",
      "groupPermissions/groupPermissionLimitValueEdit.jsp",
      "groupPermissions/groupViewPermissions.jsp",
      "subjectPermissions/permissionLimitAddValue.jsp",
      "subjectPermissions/permissionLimitEdit.jsp",
      "subjectPermissions/permissionLimitValueEdit.jsp",
      "subjectPermissions/subjectViewPermissions.jsp"
  };

  /**
   * EL that carries an action name: the action name property, the action loop variables of the
   * edit page, the permission matrix id (which ends with the action name), the comma separated
   * actions of a limit
   */
  private static final Pattern ACTION_EL = Pattern.compile(
      "attributeAssignAction\\.name|attributeUpdateRequestContainer\\.action(?![\\w\\[])"
      + "|(?<![\\w.])(action|actionThatImply|actionImpliedBy|guiPermissionId)(?!\\w)|actionsCommaSeparated");

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeActionNameEscapingTest("testJspsEscapeActionNames"));
  }

  /**
   *
   */
  public AttributeActionNameEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeActionNameEscapingTest(String name) {
    super(name);
  }

  /**
   * find a v2 UI JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property grouperUiWebappDir
   * (the grouper-ui/webapp directory) overrides.
   * @param jsp path under grouperUi2
   * @return the file
   */
  private static File findJsp(String jsp) {
    String override = System.getProperty("grouperUiWebappDir");
    if (override != null && override.length() > 0) {
      return new File(override, "WEB-INF/grouperUi2/" + jsp);
    }
    String[] candidates = {"grouper-ui/" + UI2 + jsp, UI2 + jsp, "../grouper-ui/" + UI2 + jsp};
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + jsp + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DgrouperUiWebappDir=<path to grouper-ui/webapp>");
    return null;
  }

  /**
   * @param jsp path under grouperUi2
   * @return the JSP source
   * @throws Exception
   */
  private static String readJsp(String jsp) throws Exception {
    return new String(Files.readAllBytes(findJsp(jsp).toPath()), StandardCharsets.UTF_8);
  }

  /**
   * every EL output that carries an action name goes through an escape function for its context
   * (or is the javascript-escaped guiPermissionIdJs variable, or the page title function, which
   * escapes in every release that has it). EL in JSTL tag attributes is evaluated, not printed,
   * so those tags are removed first.
   * @throws Exception
   */
  public void testJspsEscapeActionNames() throws Exception {
    List<String> rawUses = new ArrayList<String>();
    for (String jsp : JSPS) {
      String text = readJsp(jsp);
      text = text.replaceAll("(?s)<%--.*?--%>", "");
      text = text.replaceAll("<c:(set|if|when|forEach|choose|otherwise)\\b(?:[^>\"]|\"[^\"]*\")*>", "");
      Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
      while (matcher.find()) {
        String expression = matcher.group(1).trim();
        if (!ACTION_EL.matcher(expression).find()) {
          continue;
        }
        if (expression.matches("grouper:(escapeHtml|escapeUrl|escapeJavascript|titleFromKeyAndText)\\(.*\\)")) {
          continue;
        }
        rawUses.add(jsp + ": ${" + expression + "}");
      }
    }
    assertTrue("Unescaped action name output: " + rawUses, rawUses.isEmpty());
  }

  /**
   * the escaped forms are really there (the check above is not vacuous)
   * @throws Exception
   */
  public void testJspsHaveEscapedForms() throws Exception {
    assertTrue(readJsp(JSPS[0]).contains("<td>${grouper:escapeHtml(attributeAssignAction.name)}</td>"));

    String edit = readJsp(JSPS[1]);
    assertTrue(edit.contains("<grouper:param>${grouper:escapeHtml(attributeUpdateRequestContainer.action)}</grouper:param>"));
    assertTrue(edit.contains("value=\"${grouper:escapeHtml(action)}\""));
    assertTrue(edit.contains("${grouper:escapeHtml(actionThatImply)}"));
    assertTrue(edit.contains("${grouper:escapeHtml(actionImpliedBy)}"));

    for (int i = 2; i < JSPS.length; i++) {
      String jsp = readJsp(JSPS[i]);
      if (JSPS[i].endsWith("ViewPermissions.jsp")) {
        assertTrue(JSPS[i], jsp.contains("name=\"previousState__${grouper:escapeHtml(guiPermissionId)}\""));
        assertTrue(JSPS[i], jsp.contains("name=\"permissionCheckbox__${grouper:escapeHtml(guiPermissionId)}\""));
        assertTrue(JSPS[i], jsp.contains("&guiPermissionId=${grouper:escapeUrl(guiPermissionId)}&"));
        assertTrue(JSPS[i], jsp.contains("id=\"permissionMenuButton_${grouper:escapeHtml(guiPermissionId)}__"));
        assertTrue(JSPS[i], jsp.contains("${grouper:escapeHtml(guiPermissionLimitBeanContainer.actionsCommaSeparated)}"));
        // 6.0.0+ has menu links that put the id in a javascript string
        if (jsp.contains("menuIdOfMenuTarget: 'permissionMenuButton_")) {
          assertTrue(JSPS[i], jsp.contains(
              "<c:set var=\"guiPermissionIdJs\">${grouper:escapeJavascript(fn:replace(guiPermissionId, '\\\\', '\\\\\\\\'))}</c:set>"));
          assertFalse(JSPS[i], jsp.contains("menuIdOfMenuTarget: 'permissionMenuButton_${guiPermissionId}"));
        }
      } else {
        assertTrue(JSPS[i], jsp.contains(
            "${grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignAction.name)}"));
      }
    }
  }

  /**
   * the escape functions the JSPs use make an action name inert in each context
   */
  public void testEscapeFunctions() {
    // html text and double-quoted attributes
    String html = GrouperUiFunctions.escapeHtml(PAYLOAD);
    assertTrue(html, html.startsWith("&lt;svg"));
    assertFalse(html, html.contains("<"));
    assertFalse(GrouperUiFunctions.escapeHtml("\" autofocus onfocus=\"x").contains("\""));

    // url parameter: only url-safe characters
    String url = GrouperUiFunctions.escapeUrl("a__b__c__" + PAYLOAD + "'\"\\&");
    assertTrue(url, url.matches("[A-Za-z0-9%._*+\\-]*"));

    // javascript string in an onclick attribute: escapeJavascript does not escape backslash, so a
    // name with \' closes the string unless backslashes are doubled first (what the JSP does)
    String name = "a\\'+alert(1)+'";
    assertTrue("escapeJavascript alone does not protect a name with a backslash",
        hasUnescapedSingleQuote(GrouperUiFunctions.escapeJavascript(name)));
    String js = GrouperUiFunctions.escapeJavascript(name.replace("\\", "\\\\"));
    assertFalse(js, hasUnescapedSingleQuote(js));
    // and the html specials are entities, so the onclick="..." attribute cannot be closed
    assertFalse(GrouperUiFunctions.escapeJavascript(name.replace("\\", "\\\\") + "\"").contains("\""));
  }

  /**
   * @param js
   * @return true if a ' in the string is preceded by an even number of backslashes (it would end
   * a single-quoted javascript string)
   */
  private static boolean hasUnescapedSingleQuote(String js) {
    for (int i = 0; i < js.length(); i++) {
      if (js.charAt(i) == '\'') {
        int backslashes = 0;
        for (int j = i - 1; j >= 0 && js.charAt(j) == '\\'; j--) {
          backslashes++;
        }
        if (backslashes % 2 == 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * the limit "assigned to" tooltip is html built in java: the owner permission's action name
   * must be escaped
   * @throws Exception
   */
  public void testLimitTooltipEscapesActionName() throws Exception {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("test:actionXss:permissionDef")
          .assignCreateParentStemsIfNotExist(true)
          .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
      AttributeDefName permission = new AttributeDefNameSave(grouperSession, permissionDef)
          .assignName("test:actionXss:permission").save();
      permissionDef.getAttributeDefActionDelegate().addAction(PAYLOAD);

      Role role = new GroupSave(grouperSession).assignName("test:actionXss:role")
          .assignTypeOfGroup(TypeOfGroup.role).save();
      AttributeAssign permissionAssign = role.getPermissionRoleDelegate()
          .assignRolePermission(PAYLOAD, permission).getAttributeAssign();
      assertEquals(PAYLOAD, permissionAssign.getAttributeAssignAction().getName());

      // a limit on the permission assignment (group_asgn), so the tooltip shows the action
      permissionAssign.getAttributeValueDelegate().assignValue(
          PermissionLimitUtils.limitElAttributeDefName().getName(), "true");
      AttributeAssign limitAssign = permissionAssign.getAttributeDelegate()
          .retrieveAssignments(PermissionLimitUtils.limitElAttributeDefName()).iterator().next();

      PermissionLimitBean permissionLimitBean = new PermissionLimitBean();
      permissionLimitBean.setLimitAssign(limitAssign);
      GuiPermissionLimitBeanContainer container = new GuiPermissionLimitBeanContainer();
      container.setPermissionLimitBean(permissionLimitBean);

      String tooltip;
      assignUiRequest();
      try {
        tooltip = container.getAssignedToTooltip();
      } finally {
        removeUiRequest();
      }
      assertTrue(tooltip, tooltip.contains("&lt;svg/onload=document.title=7332&gt;"));
      assertFalse(tooltip, tooltip.contains("<svg"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * TagUtils.navResourceString reads the "nav" bundle from the http session of the thread's
   * request; give it a minimal request whose bundle returns each key as its own text
   * @throws Exception
   */
  private static void assignUiRequest() throws Exception {
    final ResourceBundle nav = new ResourceBundle() {
      @Override
      protected Object handleGetObject(String key) {
        return key;
      }
      @Override
      public Enumeration<String> getKeys() {
        return Collections.emptyEnumeration();
      }
    };
    final LocalizationContext localizationContext = new LocalizationContext(nav);
    final HttpSession session = (HttpSession)Proxy.newProxyInstance(
        AttributeActionNameEscapingTest.class.getClassLoader(), new Class[] {HttpSession.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName()) && "nav".equals(args[0])) {
              return localizationContext;
            }
            return null;
          }
        });
    HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(
        AttributeActionNameEscapingTest.class.getClassLoader(), new Class[] {HttpServletRequest.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getSession".equals(method.getName())) {
              return session;
            }
            return null;
          }
        });
    threadLocalRequest().set(request);
  }

  /**
   * @throws Exception
   */
  private static void removeUiRequest() throws Exception {
    threadLocalRequest().remove();
  }

  /**
   * the private request thread local in GrouperUiFilter
   * @return the thread local
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static ThreadLocal<HttpServletRequest> threadLocalRequest() throws Exception {
    Field field = GrouperUiFilter.class.getDeclaredField("threadLocalRequest");
    field.setAccessible(true);
    return (ThreadLocal<HttpServletRequest>)field.get(null);
  }

}
