package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Security fix (uiOidcRouteCanonicalizationBypass): a route specific UI OIDC config
 * (uiPathRegexes) must apply however the protected request is spelled: parameter order,
 * percent encoding, parameter in the POST body, encoded path, the /grouperExternal/app/
 * alias, or ".oidc?" in the query.  Before the fix those spellings got the default config
 * or no config at all (GrouperOidc.externalSystemConfigIdForUi on the raw uri?query).
 * The request is a small proxy; config is set in the override map.
 */
public class GrouperUiOidcRouteSelectorTest extends GrouperTest {

  /** protected group */
  private static final String GROUP_UUID = "e88be02cabba4a94be4d86fb4b26c5a8";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperUiOidcRouteSelectorTest("testRouteSpellings"));
  }

  /**
   * @param name
   */
  public GrouperUiOidcRouteSelectorTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    clearOidcConfig();
    super.tearDown();
  }

  /**
   * remove the test oidc configs from the override map
   */
  private static void clearOidcConfig() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().keySet().removeIf(key -> key.startsWith("grouper.oidcExternalSystem.routeTest"));
  }

  /**
   * one UI oidc config
   * @param configId
   * @param uiPathRegexes or null for the default config
   * @param redirectUri
   */
  private static void oidcConfig(String configId, String uiPathRegexes, String redirectUri) {
    Map<String, String> overrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    String prefix = "grouper.oidcExternalSystem." + configId + ".";
    overrides.put(prefix + "clientId", "client_" + configId);
    overrides.put(prefix + "useForUi", "true");
    overrides.put(prefix + "enabled", "true");
    overrides.put(prefix + "redirectUri", redirectUri);
    if (uiPathRegexes != null) {
      overrides.put(prefix + "uiPathRegexes", uiPathRegexes);
    }
  }

  /**
   * route config for one group, as in the documented example
   */
  private static void routeConfig() {
    oidcConfig("routeTestGroup", "^.*UiV2Group\\.viewGroup[?&]groupId=" + GROUP_UUID + ".*$",
        "https://grouper.example.edu/grouper/grouperUi/app/UiV2Main.oidcGroup");
  }

  /**
   * default config (no regexes)
   */
  private static void defaultConfig() {
    oidcConfig("routeTestDefault", null, "https://grouper.example.edu/grouper/grouperUi/app/UiV2Main.oidc");
  }

  /**
   * request with a raw uri, raw query and the decoded parameter map the container would build
   * @param uri
   * @param query
   * @param params name, value, name, value... (query and body)
   * @return the request
   */
  private static HttpServletRequest request(final String uri, final String query, String... params) {
    return request(false, uri, query, params);
  }

  /**
   * @param multipart true to act like GrouperRequestWrapper on a multipart request: the form fields come
   *   back from getParameterNames / getParameterValues but not from getParameterMap
   * @param uri
   * @param query
   * @param params name, value, name, value... (query and body)
   * @return the request
   */
  private static HttpServletRequest request(final boolean multipart, final String uri, final String query, String... params) {
    final Map<String, String[]> paramMap = new LinkedHashMap<String, String[]>();
    for (int i = 0; i < params.length; i += 2) {
      String[] old = paramMap.get(params[i]);
      if (old == null) {
        paramMap.put(params[i], new String[] {params[i + 1]});
      } else {
        String[] values = new String[old.length + 1];
        System.arraycopy(old, 0, values, 0, old.length);
        values[old.length] = params[i + 1];
        paramMap.put(params[i], values);
      }
    }
    return (HttpServletRequest)Proxy.newProxyInstance(GrouperUiOidcRouteSelectorTest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getRequestURI".equals(method.getName())) {
              return uri;
            }
            if ("getQueryString".equals(method.getName())) {
              return query;
            }
            if ("getParameterMap".equals(method.getName())) {
              return multipart ? new LinkedHashMap<String, String[]>() : paramMap;
            }
            if ("getParameterNames".equals(method.getName())) {
              return java.util.Collections.enumeration(paramMap.keySet());
            }
            if ("getParameterValues".equals(method.getName())) {
              return paramMap.get((String)args[0]);
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * @param uri
   * @param query
   * @param params
   * @return the selected config id
   */
  private static String select(String uri, String query, String... params) {
    return GrouperUiOidcRouteSelector.externalSystemConfigIdForUi(request(uri, query, params));
  }

  /**
   * every spelling that reaches the same handler and group gets the route config
   */
  public void testRouteSpellings() {
    clearOidcConfig();
    routeConfig();
    defaultConfig();

    String path = "/grouper/grouperUi/app/UiV2Group.viewGroup";

    // control: the canonical spelling matched before the fix too
    assertEquals("routeTestGroup", select(path, "groupId=" + GROUP_UUID, "groupId", GROUP_UUID));
    // bypass 1: harmless parameter first (raw regex still matches via [?&], kept as a control)
    assertEquals("routeTestGroup", select(path, "x=1&groupId=" + GROUP_UUID, "x", "1", "groupId", GROUP_UUID));
    // bypass 2: encoded parameter name
    assertEquals("routeTestGroup", select(path, "group%49d=" + GROUP_UUID, "groupId", GROUP_UUID));
    // bypass 2b: encoded value
    assertEquals("routeTestGroup", select(path, "groupId=e88be02cabba4a94be4d86fb4b26c5a%38", "groupId", GROUP_UUID));
    // bypass 3: group id in the POST body only
    assertEquals("routeTestGroup", select(path, null, "groupId", GROUP_UUID, "OWASPCSRFTOKEN", "t"));
    // encoded path
    assertEquals("routeTestGroup", select("/grouper/grouperUi/app/UiV2Group%2EviewGroup", "groupId=" + GROUP_UUID, "groupId", GROUP_UUID));
    // alternate servlet mapping
    assertEquals("routeTestGroup", select("/grouper/grouperExternal/app/UiV2Group.viewGroup", "groupId=" + GROUP_UUID, "groupId", GROUP_UUID));
    // duplicate parameter values
    assertEquals("routeTestGroup", select(path, "groupId=abc&groupId=" + GROUP_UUID, "groupId", "abc", "groupId", GROUP_UUID));
    // ".oidc?" in the query no longer makes it look like the callback
    assertEquals("routeTestGroup", select(path, "groupId=" + GROUP_UUID + "&x=.oidc?", "groupId", GROUP_UUID, "x", ".oidc?"));
    assertEquals("routeTestGroup", select(path, "groupId=" + GROUP_UUID + "&x=.oidc%3F", "groupId", GROUP_UUID, "x", ".oidc?"));

    // controls: another group or another screen gets the default as before
    assertEquals("routeTestDefault", select(path, "groupId=abc", "groupId", "abc"));
    assertEquals("routeTestDefault", select("/grouper/grouperUi/app/UiV2Main.indexMain", null));
  }

  /**
   * ".oidc?" in the query does not skip the default config (before: null)
   */
  /**
   * a multipart request: the group id is a multipart form field, which GrouperRequestWrapper returns from
   * getParameterNames / getParameterValues but not getParameterMap; the route config is still selected
   * (2026-11-10)
   */
  public void testMultipartFormFieldSelectsRoute() {
    clearOidcConfig();
    routeConfig();
    defaultConfig();
    String path = "/grouper/grouperUi/app/UiV2Group.viewGroup";
    assertEquals("routeTestGroup", GrouperUiOidcRouteSelector.externalSystemConfigIdForUi(
        request(true, path, null, "groupId", GROUP_UUID, "OWASPCSRFTOKEN", "t")));
    // control: another group still gets the default
    assertEquals("routeTestDefault", GrouperUiOidcRouteSelector.externalSystemConfigIdForUi(
        request(true, path, null, "groupId", "abc")));
  }

  public void testDefaultNotSkipped() {
    clearOidcConfig();
    defaultConfig();
    assertEquals("routeTestDefault", select("/grouper/grouperUi/app/UiV2Main.index", "operation=UiV2Main.indexMain&x=.oidc?",
        "operation", "UiV2Main.indexMain", "x", ".oidc?"));
  }

  /**
   * the callback is recognized by its exact path
   */
  public void testCallback() {
    clearOidcConfig();
    routeConfig();
    defaultConfig();
    assertEquals("routeTestGroup", select("/grouper/grouperUi/app/UiV2Main.oidcGroup", "code=c&state=s", "code", "c", "state", "s"));
    assertEquals("routeTestDefault", select("/grouper/grouperUi/app/UiV2Main.oidc", "code=c&state=s", "code", "c", "state", "s"));
    // a different path with the callback text in a value is not the callback
    assertEquals("routeTestGroup", select("/grouper/grouperUi/app/UiV2Group.viewGroup",
        "groupId=" + GROUP_UUID + "&next=/grouper/grouperUi/app/UiV2Main.oidcGroup?x", "groupId", GROUP_UUID,
        "next", "/grouper/grouperUi/app/UiV2Main.oidcGroup?x"));
  }

  /**
   * disabled and non UI configs are ignored; no config means null
   */
  public void testDisabledIgnored() {
    clearOidcConfig();
    assertNull(select("/grouper/grouperUi/app/UiV2Main.index", null));
    routeConfig();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.routeTestGroup.enabled", "false");
    assertNull(select("/grouper/grouperUi/app/UiV2Group.viewGroup", "groupId=" + GROUP_UUID, "groupId", GROUP_UUID));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.routeTestGroup.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.routeTestGroup.useForUi", "false");
    assertNull(select("/grouper/grouperUi/app/UiV2Group.viewGroup", "groupId=" + GROUP_UUID, "groupId", GROUP_UUID));
  }

  /**
   * helpers
   */
  public void testHelpers() {
    assertEquals("/grouper/grouperUi/app/UiV2Main.oidc",
        GrouperUiOidcRouteSelector.redirectUriContext("https://grouper.example.edu/grouper/grouperUi/app/UiV2Main.oidc"));
    assertNull(GrouperUiOidcRouteSelector.redirectUriContext("not a url"));
    assertEquals("/grouper/grouperUi/app/UiV2Group.viewGroup", GrouperUiOidcRouteSelector.decodePath("/grouper/grouperUi/app/UiV2Group%2EviewGroup"));
    assertEquals("/g/grouperUi/app/X.y", GrouperUiOidcRouteSelector.normalServletAlias("/g/grouperExternal/app/X.y"));
  }

}
