package edu.internet2.middleware.grouper.j2ee;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security: the focusOnElementName request param is concatenated into a GuiScreenAction.newScript()
 * javascript string by UiV2SubjectSource and UiV2ProvisionerConfiguration (reflected XSS).
 * GrouperRequestWrapper must only hand back values that are plain form field names.
 * No database needed.
 */
public class GrouperRequestWrapperFocusParamTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperRequestWrapperFocusParamTest.class);
  }

  /**
   * @param name
   */
  public GrouperRequestWrapperFocusParamTest(String name) {
    super(name);
  }

  /** the payload from the security report, ends the selector string, runs alert, comments out the rest */
  private static final String PAYLOAD = "x']\");alert('X');//";

  /**
   * field names the JSPs really send must pass
   */
  public void testSafeFocusOnElementNameAllowsFieldNames() {
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName("provisionerConfigType"));
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName("provisionerStartWithClass"));
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName("sourceConfigType"));
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName("config_targetGroupAttribute.0.name"));
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName("config_some-field_1"));
  }

  /**
   * anything that could break out of the javascript string or jquery selector must fail
   */
  public void testSafeFocusOnElementNameRejectsPayloads() {
    String[] bad = new String[] {PAYLOAD, "a'b", "a\"b", "a]b", "a[b", "a(b", "a)b", "a\\b", "a/b",
        "a;b", "a b", "a<b", "a\nb", "", null};
    for (String value : bad) {
      assertFalse(String.valueOf(value), GrouperRequestWrapper.isSafeFocusOnElementName(value));
    }
    StringBuilder longValue = new StringBuilder();
    for (int i = 0; i < 201; i++) {
      longValue.append('a');
    }
    assertFalse(GrouperRequestWrapper.isSafeFocusOnElementName(longValue.toString()));
    assertTrue(GrouperRequestWrapper.isSafeFocusOnElementName(longValue.substring(1)));
  }

  /**
   * the wrapper drops an unsafe focusOnElementName (getParameter, EL map, getParameterValues,
   * and the brackets fallback) but leaves other params alone
   */
  public void testWrapperDropsUnsafeFocusOnElementName() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("focusOnElementName", PAYLOAD);
    params.put("other", PAYLOAD);
    GrouperRequestWrapper wrapper = new GrouperRequestWrapper(fakeRequest(params));

    assertNull(wrapper.getParameter("focusOnElementName"));
    assertNull(wrapper.getParameter("focusOnElementName[]"));
    assertNull(wrapper.getParameterValues("focusOnElementName"));
    assertEquals(PAYLOAD, wrapper.getParameter("other"));

    // the brackets form radios send
    params.clear();
    params.put("focusOnElementName[]", PAYLOAD);
    wrapper = new GrouperRequestWrapper(fakeRequest(params));
    assertNull(wrapper.getParameter("focusOnElementName"));

    // a real field name passes through
    params.clear();
    params.put("focusOnElementName", "config_targetGroupAttribute.0.name");
    wrapper = new GrouperRequestWrapper(fakeRequest(params));
    assertEquals("config_targetGroupAttribute.0.name", wrapper.getParameter("focusOnElementName"));
    assertEquals("config_targetGroupAttribute.0.name", wrapper.getParameterValues("focusOnElementName")[0]);
  }

  /**
   * a non-multipart POST request backed by a map
   * @param params
   * @return the request
   */
  private static HttpServletRequest fakeRequest(final Map<String, String> params) {
    InvocationHandler handler = new InvocationHandler() {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("getParameter".equals(methodName)) {
          return params.get((String)args[0]);
        }
        if ("getParameterValues".equals(methodName)) {
          String value = params.get((String)args[0]);
          return value == null ? null : new String[] {value};
        }
        if ("getMethod".equals(methodName)) {
          return "POST";
        }
        if ("hashCode".equals(methodName)) {
          return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName)) {
          return proxy == args[0];
        }
        if ("toString".equals(methodName)) {
          return "fakeRequest";
        }
        // not used by these tests
        return null;
      }
    };
    return (HttpServletRequest)Proxy.newProxyInstance(GrouperRequestWrapperFocusParamTest.class.getClassLoader(),
        new Class[] {HttpServletRequest.class}, handler);
  }
}
