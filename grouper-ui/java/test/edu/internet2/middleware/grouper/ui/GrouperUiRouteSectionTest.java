package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter.UiSection;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security fix (alternateUiRouteAdmissionAuthorization): the UI section, and so the
 * require.group.for.logins check, is decided from the path the servlet dispatches.
 * Before the fix /grouperExternal/app/AnyHandler.method and any /grouperUi/app/ url with
 * operation=Misc.x or MiscMenu.x were ANONYMOUS and skipped the login group.
 * No container or database: the request is a small proxy.
 */
public class GrouperUiRouteSectionTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiRouteSectionTest.class);
  }

  /**
   * @param name
   */
  public GrouperUiRouteSectionTest(String name) {
    super(name);
  }

  /**
   * request with a uri and parameters, only getRequestURI and getParameter are supported
   * @param uri
   * @param params name, value, name, value...
   * @return the request
   */
  private static HttpServletRequest request(final String uri, String... params) {
    final Map<String, String> paramMap = new HashMap<String, String>();
    for (int i = 0; i < params.length; i += 2) {
      paramMap.put(params[i], params[i + 1]);
    }
    return (HttpServletRequest)Proxy.newProxyInstance(GrouperUiRouteSectionTest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getRequestURI".equals(method.getName())) {
              return uri;
            }
            if ("getParameter".equals(method.getName())) {
              return paramMap.get(args[0]);
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * @param uri
   * @param params
   * @return the section
   */
  private static UiSection section(String uri, String... params) {
    return GrouperUiRouteSection.uiSectionForRequest(request(uri, params));
  }

  /**
   * @param uri
   * @param params
   */
  private static void assertExternalServletRejects(String uri, String... params) {
    try {
      UiSection uiSection = section(uri, params);
      fail("expected an error for " + uri + " but got " + uiSection);
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Cannot use the external servlet"));
    }
  }

  /**
   * the alternate servlet mapping only runs the external self register screens (before: ANONYMOUS)
   */
  public void testExternalServletOnlySelfRegister() {
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2Main.index");
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2Main.indexMain");
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2Group.viewGroup", "groupId", "abc");
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2Main.index",
        "operation", "UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister");
    // a lookalike class is not the self register class
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegisterX.index");
    // encoded spellings decode to the same handler the servlet would run
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2Main%2Eindex");
    assertExternalServletRejects("/grouper/grouper%45xternal/app/UiV2Main.index");
    // extra path segments
    assertExternalServletRejects("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister.index/x");

    assertEquals(UiSection.EXTERNAL, section("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister"));
    assertEquals(UiSection.EXTERNAL, section("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister.submit"));
    assertEquals(UiSection.EXTERNAL, section("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister%2Esubmit"));
    assertEquals(UiSection.EXTERNAL, section("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister.submit#x"));
    // the operation parameter does not change the answer
    assertEquals(UiSection.EXTERNAL, section("/grouper/grouperExternal/app/UiV2ExternalSubjectSelfRegister.submit",
        "operation", "UiV2Main.index"));
  }

  /**
   * the operation parameter no longer makes a normal handler anonymous (before: ANONYMOUS)
   */
  public void testOperationParameterIgnored() {
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Subject.viewSubject", "operation", "Misc.probe"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Subject.viewSubject", "operation", "MiscMenu.probe"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Subject.viewSubject"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Main.index", "operation", "UiV2Public.index"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Main.index", "operation", "UiV2ExternalSubjectSelfRegister.index"));
    // the old lite UI menu needs the login group too
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/MiscMenu.miscMenu"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/appHtml/grouper.html", "operation", "Misc.x"));
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Main.index.jsp"));
    // the self register class on the normal mapping is still the normal UI
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2ExternalSubjectSelfRegister.submit"));
  }

  /**
   * anonymous entry points are unchanged
   */
  public void testAnonymousEntryPoints() {
    assertEquals(UiSection.ANONYMOUS, section("/grouper/"));
    assertEquals(UiSection.ANONYMOUS, section("/grouper/index.jsp"));
    assertEquals(UiSection.ANONYMOUS, section("/grouper/grouperExternal/public/UiV2Public.index"));
    assertEquals(UiSection.ANONYMOUS, section("/grouper/grouperExternal/public/UiV2Public.postIndex"));
    assertEquals(UiSection.ANONYMOUS, section("/grouper/grouperUi/app/UiV2Public.postIndex"));
    assertEquals(UiSection.ANONYMOUS, section("/grouper/grouperExternal/public/UiV2Public.index",
        "operation", "UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister"));
    // an encoded spelling is not an exact entry point, so it gets the stricter section
    assertEquals(UiSection.ADMIN_UI, section("/grouper/grouperUi/app/UiV2Public%2EpostIndex"));
  }

  /**
   * parsing helpers
   */
  public void testHelpers() {
    assertEquals("UiV2Main", GrouperUiRouteSection.handlerClass("UiV2Main.index"));
    assertEquals("UiV2Main", GrouperUiRouteSection.handlerClass("UiV2Main.index#abc"));
    assertNull(GrouperUiRouteSection.handlerClass("UiV2Main"));
    assertEquals(4, GrouperUiRouteSection.decodedSegments("/grouper//grouperUi/app/UiV2Main.index").size());
    assertEquals("a.b", GrouperUiRouteSection.urlDecode("a%2Eb"));
    assertEquals("a%zz", GrouperUiRouteSection.urlDecode("a%zz"));
  }

}
