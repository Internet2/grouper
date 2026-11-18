/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.j2ee.CommonServletContainerInitializer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;

/**
 * <p>The UI anti-framing (clickjacking) headers: GrouperUiAntiFramingFilter sends
 * Content-Security-Policy frame-ancestors 'self' and X-Frame-Options SAMEORIGIN by default, and
 * the grouper-ui.properties settings relax or turn them off.</p>
 *
 * <p>Before the fix this test does not compile (the filter does not exist) and no UI response
 * carries either header.  The servlet request, response and context are java.lang.reflect.Proxy
 * stand-ins; no running UI needed.  testRegisteredByInitializer runs
 * CommonServletContainerInitializer.onStartup (which starts Grouper, so it needs the test
 * database) and checks that it registers the filter on every UI path, before GrouperUiFilter.
 * UiAntiFramingHeadersHttpTest checks the headers on the running UI.</p>
 *
 * <p>Goes in grouper-ui/java/test/edu/internet2/middleware/grouper/ui/.  Same file for every
 * release (Java 8 syntax for 2.6.19).</p>
 */
public class GrouperUiAntiFramingFilterTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiAntiFramingFilterTest.class);
    // Grouper may start non-daemon background threads; without this the JVM may not exit
    System.exit(0);
  }

  /**
   *
   */
  public GrouperUiAntiFramingFilterTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperUiAntiFramingFilterTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    clearOverrides();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    clearOverrides();
    super.tearDown();
  }

  /**
   * remove this test's config overrides
   */
  private static void clearOverrides() {
    Map<String, String> overrides = GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap();
    overrides.remove(GrouperUiAntiFramingFilter.CONFIG_ENABLED);
    overrides.remove(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS);
  }

  /**
   * set a config override in the test JVM
   * @param key
   * @param value
   */
  private static void override(String key, String value) {
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  // ---------------------------------------------------------------- tests

  /**
   * default: frame-ancestors 'self' and SAMEORIGIN, and the rest of the chain still runs
   * @throws Exception
   */
  public void testDefaultHeaders() throws Exception {
    // blank means the default
    override(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS, "");
    FakeResponse fake = new FakeResponse();
    boolean[] chainRan = runFilter(fake);
    assertTrue("chain must run", chainRan[0]);
    assertEquals(Arrays.asList("frame-ancestors 'self'"), fake.headers("Content-Security-Policy"));
    assertEquals(Arrays.asList("SAMEORIGIN"), fake.headers("X-Frame-Options"));
  }

  /**
   * a site that embeds the UI in a portal lists the portal origin; X-Frame-Options cannot say that,
   * so it is left out
   * @throws Exception
   */
  public void testPortalOriginRelaxes() throws Exception {
    override(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS, "'self' https://portal.example.edu");
    FakeResponse fake = new FakeResponse();
    runFilter(fake);
    assertEquals(Arrays.asList("frame-ancestors 'self' https://portal.example.edu"), fake.headers("Content-Security-Policy"));
    assertEquals(0, fake.headers("X-Frame-Options").size());
  }

  /**
   * 'none': no framing at all, X-Frame-Options DENY
   * @throws Exception
   */
  public void testNone() throws Exception {
    override(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS, "'none'");
    FakeResponse fake = new FakeResponse();
    runFilter(fake);
    assertEquals(Arrays.asList("frame-ancestors 'none'"), fake.headers("Content-Security-Policy"));
    assertEquals(Arrays.asList("DENY"), fake.headers("X-Frame-Options"));
  }

  /**
   * turned off: neither header, chain still runs
   * @throws Exception
   */
  public void testDisabled() throws Exception {
    override(GrouperUiAntiFramingFilter.CONFIG_ENABLED, "false");
    FakeResponse fake = new FakeResponse();
    boolean[] chainRan = runFilter(fake);
    assertTrue("chain must run", chainRan[0]);
    assertEquals(0, fake.headers("Content-Security-Policy").size());
    assertEquals(0, fake.headers("X-Frame-Options").size());
  }

  /**
   * a CSP set earlier (e.g. a site-registered ContentSecurityPolicyFilter) is kept, ours is added;
   * an X-Frame-Options set earlier is not replaced
   * @throws Exception
   */
  public void testExistingHeadersKept() throws Exception {
    FakeResponse fake = new FakeResponse();
    fake.put("Content-Security-Policy", "default-src 'self'", false);
    fake.put("X-Frame-Options", "DENY", false);
    runFilter(fake);
    assertEquals(Arrays.asList("default-src 'self'", "frame-ancestors 'self'"), fake.headers("Content-Security-Policy"));
    assertEquals(Arrays.asList("DENY"), fake.headers("X-Frame-Options"));
  }

  /**
   * the configured value stays one frame-ancestors directive: no ";" (another directive),
   * "," (another policy) or line breaks
   * @throws Exception
   */
  public void testConfiguredValueCleaned() throws Exception {
    override(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS, "'self' https://a.example.edu; script-src *,\r\nX-Other: 1");
    FakeResponse fake = new FakeResponse();
    runFilter(fake);
    List<String> csp = fake.headers("Content-Security-Policy");
    assertEquals(1, csp.size());
    String value = csp.get(0);
    assertTrue(value, value.startsWith("frame-ancestors 'self' https://a.example.edu"));
    assertEquals(value, -1, value.indexOf(';'));
    assertEquals(value, -1, value.indexOf(','));
    assertEquals(value, -1, value.indexOf('\r'));
    assertEquals(value, -1, value.indexOf('\n'));
    // only separators: falls back to 'self'
    override(GrouperUiAntiFramingFilter.CONFIG_FRAME_ANCESTORS, " ; , ");
    fake = new FakeResponse();
    runFilter(fake);
    assertEquals(Arrays.asList("frame-ancestors 'self'"), fake.headers("Content-Security-Policy"));
    assertEquals(Arrays.asList("SAMEORIGIN"), fake.headers("X-Frame-Options"));
  }

  /**
   * CommonServletContainerInitializer registers the filter in its UI block: name, class, the url
   * patterns that cover every UI response, REQUEST dispatcher, and before GrouperUiFilter (so the
   * headers are set before GrouperUiFilter can redirect or fail).  The filter is not a @WebFilter
   * (an annotation is not scanned in every deployment, and both would clash on the name).
   * Runs onStartup with a java.lang.reflect.Proxy ServletContext that records the registrations,
   * with only grouper.is.ui on.  onStartup starts Grouper (GrouperStartup), so this needs the
   * test database like other Grouper tests.  UiAntiFramingHeadersHttpTest checks the running UI.
   * @throws Exception
   */
  public void testRegisteredByInitializer() throws Exception {

    // ---- the filter class: not annotated, a Filter the container can create
    assertNull("registered by CommonServletContainerInitializer, not by @WebFilter (would clash on the name)",
        GrouperUiAntiFramingFilter.class.getAnnotation(WebFilter.class));
    assertTrue("must implement javax.servlet.Filter", Filter.class.isAssignableFrom(GrouperUiAntiFramingFilter.class));
    try {
      assertTrue("constructor must be public", Modifier.isPublic(GrouperUiAntiFramingFilter.class.getConstructor().getModifiers()));
    } catch (NoSuchMethodException nsme) {
      fail("the container needs a public no-arg constructor to create the filter");
    }

    // ---- run the initializer as a UI-only webapp
    Map<String, String> hibernateOverrides = GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap();
    Map<String, String> grouperOverrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    String[] offKeys = new String[] {"grouper.is.mockServices", "grouper.is.ws", "grouper.is.ws.soap",
        "grouper.is.scim", "grouper.is.mcp", "grouper.is.daemon"};
    try {
      hibernateOverrides.put("grouper.is.ui", "true");
      for (String offKey : offKeys) {
        hibernateOverrides.put(offKey, "false");
      }
      grouperOverrides.put("grouper.osgi.enable", "false");
      assertTrue("grouper.is.ui override not seen by GrouperHibernateConfig",
          GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false));
      assertFalse("grouper.is.daemon override not seen by GrouperHibernateConfig",
          GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.daemon", false));

      RecordingServletContext recorder = new RecordingServletContext();
      ServletContext servletContext = (ServletContext)Proxy.newProxyInstance(
          GrouperUiAntiFramingFilterTest.class.getClassLoader(), new Class[] {ServletContext.class}, recorder);
      new CommonServletContainerInitializer().onStartup(new HashSet<Class<?>>(), servletContext);

      // ---- what was registered
      int antiFramingIndex = recorder.filterNames.indexOf(GrouperUiAntiFramingFilter.FILTER_NAME);
      assertTrue("filter " + GrouperUiAntiFramingFilter.FILTER_NAME + " not registered, filters: " + recorder.filterNames,
          antiFramingIndex >= 0);
      assertEquals(GrouperUiAntiFramingFilter.class, recorder.filterClasses.get(antiFramingIndex));
      int grouperUiIndex = recorder.filterNames.indexOf("GrouperUi");
      assertTrue("GrouperUiFilter not registered, filters: " + recorder.filterNames, grouperUiIndex >= 0);
      assertTrue("must be registered before GrouperUiFilter, filters: " + recorder.filterNames,
          antiFramingIndex < grouperUiIndex);

      List<String> patterns = recorder.filterPatterns.get(GrouperUiAntiFramingFilter.FILTER_NAME);
      for (String urlPattern : GrouperUiAntiFramingFilter.URL_PATTERNS) {
        assertTrue(urlPattern + " not mapped: " + patterns, patterns.contains(urlPattern));
      }
      assertTrue(patterns.toString(), patterns.contains("/grouperUi/*"));
      assertTrue(patterns.toString(), patterns.contains("/grouperExternal/*"));
      assertTrue(patterns.toString(), patterns.contains("*.jsp"));
      // null means REQUEST
      Object dispatcherTypes = recorder.filterDispatcherTypes.get(GrouperUiAntiFramingFilter.FILTER_NAME);
      assertTrue("dispatcher types " + dispatcherTypes, dispatcherTypes == null
          || ((Collection<?>)dispatcherTypes).contains(DispatcherType.REQUEST));
    } finally {
      hibernateOverrides.remove("grouper.is.ui");
      for (String offKey : offKeys) {
        hibernateOverrides.remove(offKey);
      }
      grouperOverrides.remove("grouper.osgi.enable");
    }
  }

  // ---------------------------------------------------------------- helpers

  /**
   * run the filter once with a fake request and the given fake response
   * @param fake
   * @return one-element array, true if the chain ran
   * @throws Exception
   */
  private static boolean[] runFilter(FakeResponse fake) throws Exception {
    final boolean[] chainRan = new boolean[] {false};
    HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(
        GrouperUiAntiFramingFilterTest.class.getClassLoader(), new Class[] {HttpServletRequest.class},
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) {
            return defaultValue(method.getReturnType());
          }
        });
    HttpServletResponse response = (HttpServletResponse)Proxy.newProxyInstance(
        GrouperUiAntiFramingFilterTest.class.getClassLoader(), new Class[] {HttpServletResponse.class}, fake);
    FilterChain chain = new FilterChain() {
      public void doFilter(ServletRequest theRequest, ServletResponse theResponse) {
        chainRan[0] = true;
      }
    };
    GrouperUiAntiFramingFilter filter = new GrouperUiAntiFramingFilter();
    filter.init(null);
    filter.doFilter(request, response, chain);
    filter.destroy();
    return chainRan;
  }

  /**
   * default return value for a proxied method
   * @param type
   * @return null, false or zero
   */
  static Object defaultValue(Class<?> type) {
    if (type == boolean.class) {
      return Boolean.FALSE;
    }
    if (type == int.class) {
      return Integer.valueOf(0);
    }
    if (type == long.class) {
      return Long.valueOf(0);
    }
    return null;
  }

  /**
   * records the headers set on a proxied HttpServletResponse (names case-insensitive)
   */
  static class FakeResponse implements InvocationHandler {

    /** lower case header name to values */
    private final Map<String, List<String>> headerMap = new LinkedHashMap<String, List<String>>();

    /**
     * @param name
     * @return the values of the header (empty list if none)
     */
    List<String> headers(String name) {
      List<String> values = this.headerMap.get(name.toLowerCase());
      return values == null ? new ArrayList<String>() : values;
    }

    /**
     * set or add a header
     * @param name
     * @param value
     * @param add true for addHeader, false for setHeader
     */
    void put(String name, String value, boolean add) {
      String key = name.toLowerCase();
      List<String> values = this.headerMap.get(key);
      if (values == null || !add) {
        values = new ArrayList<String>();
        this.headerMap.put(key, values);
      }
      values.add(value);
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) {
      String name = method.getName();
      if ("setHeader".equals(name)) {
        put((String)args[0], (String)args[1], false);
        return null;
      }
      if ("addHeader".equals(name)) {
        put((String)args[0], (String)args[1], true);
        return null;
      }
      if ("containsHeader".equals(name)) {
        return Boolean.valueOf(this.headerMap.containsKey(((String)args[0]).toLowerCase()));
      }
      return defaultValue(method.getReturnType());
    }
  }

  /**
   * records the filters a ServletContainerInitializer adds to a proxied ServletContext; servlet
   * and filter registrations are proxies too, so the initializer can call addMapping etc.
   */
  static class RecordingServletContext implements InvocationHandler {

    /** filter names in the order they were added */
    final List<String> filterNames = new ArrayList<String>();

    /** filter classes, same order as filterNames */
    final List<Object> filterClasses = new ArrayList<Object>();

    /** filter name to the url patterns mapped */
    final Map<String, List<String>> filterPatterns = new LinkedHashMap<String, List<String>>();

    /** filter name to the dispatcher types of its last mapping (null means REQUEST) */
    final Map<String, Object> filterDispatcherTypes = new LinkedHashMap<String, Object>();

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) {
      String name = method.getName();
      if ("addFilter".equals(name)) {
        final String filterName = (String)args[0];
        this.filterNames.add(filterName);
        this.filterClasses.add(args[1]);
        this.filterPatterns.put(filterName, new ArrayList<String>());
        return Proxy.newProxyInstance(GrouperUiAntiFramingFilterTest.class.getClassLoader(),
            new Class[] {FilterRegistration.Dynamic.class}, new InvocationHandler() {
              public Object invoke(Object filterProxy, Method filterMethod, Object[] filterArgs) {
                if ("addMappingForUrlPatterns".equals(filterMethod.getName())) {
                  RecordingServletContext.this.filterDispatcherTypes.put(filterName, filterArgs[0]);
                  for (int i = 2; i < filterArgs.length; i++) {
                    Object patternArg = filterArgs[i];
                    if (patternArg instanceof String[]) {
                      RecordingServletContext.this.filterPatterns.get(filterName).addAll(Arrays.asList((String[])patternArg));
                    } else if (patternArg instanceof String) {
                      RecordingServletContext.this.filterPatterns.get(filterName).add((String)patternArg);
                    }
                  }
                  return null;
                }
                return defaultValue(filterMethod.getReturnType());
              }
            });
      }
      if ("addServlet".equals(name)) {
        return Proxy.newProxyInstance(GrouperUiAntiFramingFilterTest.class.getClassLoader(),
            new Class[] {ServletRegistration.Dynamic.class}, new InvocationHandler() {
              public Object invoke(Object servletProxy, Method servletMethod, Object[] servletArgs) {
                if (servletMethod.getReturnType() == Set.class) {
                  return new HashSet<String>();
                }
                return defaultValue(servletMethod.getReturnType());
              }
            });
      }
      return defaultValue(method.getReturnType());
    }
  }
}
