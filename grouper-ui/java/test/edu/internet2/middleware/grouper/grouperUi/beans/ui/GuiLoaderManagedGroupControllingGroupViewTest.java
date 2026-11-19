package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2GrouperLoader;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue loaderManagedGroupControllingGroupDisclosure: a group managed by a
 * multi-group loader job carries the controlling loader group's UUID in its loader metadata.
 * UiV2GrouperLoader.setupLoaderManagedGroup (called for the group header on every group screen
 * when the user can see the loader, and for the Loader tab) resolves that group in a root
 * session, and groupHeader.jsp / grouperLoaderGroupTab.jsp render its shortLinkWithIcon (UUID,
 * name, folder path, description) for an ADMIN of the managed group who cannot VIEW the loader
 * group.  After the fix GuiLoaderManagedGroup.getControllingGroup() returns a
 * GuiGroupRestrictedReference unless the logged in user can VIEW the controlling group (or can
 * edit loaders).</p>
 *
 * <p>The logged in user is set in an in-memory UI request (GrouperUiFilter thread locals), and
 * setupLoaderManagedGroup is called the way UiV2Group.retrieveGroupHelper calls it.</p>
 *
 * <p>Setup: SUBJ0 is ADMIN of test:loader155:managed and has nothing on
 * test:loader155private:controlling (EveryEntity VIEW/READ revoked).  The managed group has the
 * loader metadata (loaded=true, group id=controlling), as a SQL_GROUP_LIST job writes it.  No
 * loader job is run.</p>
 */
public class GuiLoaderManagedGroupControllingGroupViewTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GuiLoaderManagedGroupControllingGroupViewTest("testControllingGroupHiddenWithoutView"));
  }

  /**
   *
   */
  public GuiLoaderManagedGroupControllingGroupViewTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiLoaderManagedGroupControllingGroupViewTest(String name) {
    super(name);
  }

  /** description of the private controlling group, must never be rendered without VIEW */
  private static final String SECRET_DESCRIPTION = "loader155SecretDescription";

  /** group managed by the loader job, SUBJ0 ADMIN */
  private Group managed;

  /** private controlling loader group */
  private Group controlling;

  /**
   * groups and loader metadata, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.controlling = new GroupSave(grouperSession).assignName("test:loader155private:controlling")
          .assignDescription(SECRET_DESCRIPTION).assignCreateParentStemsIfNotExist(true).save();
      this.controlling.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.controlling.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      this.managed = new GroupSave(grouperSession).assignName("test:loader155:managed")
          .assignCreateParentStemsIfNotExist(true).save();
      this.managed.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      // the loader metadata a multi-group loader job puts on each group it manages
      String stemName = GrouperCheckConfig.loaderMetadataStemName();
      AttributeDefName valueDef = AttributeDefNameFinder.findByName(
          stemName + ":" + GrouperLoader.LOADER_METADATA_VALUE_DEF, true);
      AttributeAssign assign = this.managed.getAttributeDelegate().assignAttribute(valueDef).getAttributeAssign();
      assign.getAttributeValueDelegate().assignValueString(
          stemName + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LOADED, "true");
      assign.getAttributeValueDelegate().assignValueString(
          stemName + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID, this.controlling.getId());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // without VIEW: placeholder only, the rest of the loader metadata still shown
  // ---------------------------------------------------------------------------------------------

  /**
   * ADMIN of the managed group without VIEW on the loader group: the header still says the group
   * is loader managed, but the controlling group renders none of its metadata
   */
  public void testControllingGroupHiddenWithoutView() {
    GuiLoaderManagedGroup guiLoaderManagedGroup = setupAs(SubjectTestHelper.SUBJ0);
    assertNotNull("screen still knows the group is loader managed", guiLoaderManagedGroup);
    assertTrue(guiLoaderManagedGroup.isGrouperLoaderMetadataLoaded());
    assertEquals(this.managed.getId(), guiLoaderManagedGroup.getGroupBeingManaged().getGroup().getId());
    assertRestricted(guiLoaderManagedGroup.getControllingGroup());
  }

  // ---------------------------------------------------------------------------------------------
  // with VIEW, or wheel/root: normal link
  // ---------------------------------------------------------------------------------------------

  /**
   * after VIEW is granted the normal link (with the UUID) comes back; revoking VIEW hides it again
   */
  public void testControllingGroupShownWithView() {
    grantView(true);
    GuiGroup guiGroup = setupAs(SubjectTestHelper.SUBJ0).getControllingGroup();
    assertFalse(guiGroup instanceof GuiGroupRestrictedReference);
    assertEquals(this.controlling.getId(), guiGroup.getGroup().getId());
    String shortLink = shortLinkWithIcon(guiGroup);
    assertTrue(shortLink, shortLink.contains(this.controlling.getId()));

    grantView(false);
    assertRestricted(setupAs(SubjectTestHelper.SUBJ0).getControllingGroup());
  }

  /**
   * the short link of a GuiGroup outside a real request: it needs a UI request container, so use
   * the static one where the release has it (assignUseStaticRequestContainer, by reflection since
   * 2.6.19, 4.x and early 5.x lack it), and turn it off again afterwards
   * @param guiGroup
   * @return the link html
   */
  private static String shortLinkWithIcon(GuiGroup guiGroup) {
    Class<?> containerClass = edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer.class;
    Method assignStatic = null;
    try {
      assignStatic = containerClass.getMethod("assignUseStaticRequestContainer", boolean.class);
    } catch (NoSuchMethodException nsme) {
      // older release: no static container switch
    }
    try {
      if (assignStatic != null) {
        assignStatic.invoke(null, true);
      }
      return guiGroup.getShortLinkWithIcon();
    } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
      throw new RuntimeException(e);
    } finally {
      if (assignStatic != null) {
        try {
          assignStatic.invoke(null, false);
          containerClass.getMethod("clearStaticRequestContainer").invoke(null);
        } catch (Exception e) {
          // best effort cleanup
        }
      }
    }
  }

  /**
   * root sees the controlling group as before
   */
  public void testControllingGroupShownToRoot() {
    GuiGroup guiGroup = setupAs(SubjectFinder.findRootSubject()).getControllingGroup();
    assertFalse(guiGroup instanceof GuiGroupRestrictedReference);
    assertEquals(this.controlling.getId(), guiGroup.getGroup().getId());
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call setupLoaderManagedGroup for the managed group in a UI request by this subject, the way
   * UiV2Group.retrieveGroupHelper does (group container has the group, then the loader setup)
   * @param subject logged in subject
   * @return the loader managed group from the request's loader container
   */
  private GuiLoaderManagedGroup setupAs(Subject subject) {
    new UiRequest(subject);
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      grouperRequestContainer.getGroupContainer().setGuiGroup(new GuiGroup(this.managed));
      GrouperLoaderContainer grouperLoaderContainer = grouperRequestContainer.getGrouperLoaderContainer();
      UiV2GrouperLoader.setupLoaderManagedGroup(this.managed, grouperLoaderContainer);
      GuiLoaderManagedGroup guiLoaderManagedGroup = grouperLoaderContainer.getLoaderManagedGroup();
      if (guiLoaderManagedGroup != null) {
        // the JSPs render it while the request is still running: resolve it here too
        guiLoaderManagedGroup.getControllingGroup();
      }
      return guiLoaderManagedGroup;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * the gui group must be the placeholder, and none of its renderings may contain the private
   * group's UUID, name, folder or description
   * @param guiGroup controlling gui group
   */
  private void assertRestricted(GuiGroup guiGroup) {
    assertTrue("expected a restricted placeholder, got " + (guiGroup == null ? null : guiGroup.getClass()),
        guiGroup instanceof GuiGroupRestrictedReference);
    assertNull(guiGroup.getGroup());
    for (String html : new String[] {guiGroup.getLinkWithIcon(), guiGroup.getShortLinkWithIcon(), guiGroup.getShortLinkWithIconAndPath()}) {
      assertNotNull(html);
      assertFalse(html, html.contains(this.controlling.getId()));
      assertFalse(html, html.contains("controlling"));
      assertFalse(html, html.contains("loader155private"));
      assertFalse(html, html.contains(SECRET_DESCRIPTION));
    }
  }

  /**
   * @param grant true to grant SUBJ0 VIEW on the controlling group, false to revoke it
   */
  private void grantView(boolean grant) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      if (grant) {
        this.controlling.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      } else {
        this.controlling.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * In-memory UI request for calling a UiV2 handler directly: request, session, response and a
   * servlet whose JSP dispatcher renders nothing, set into GrouperUiFilter's thread locals, with
   * the logged in subject stored in the SessionContainer.
   */
  private static class UiRequest {

    /** request params */
    private Map<String, String> params = new HashMap<String, String>();

    /** request attributes */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /** session attributes */
    private Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    /** session */
    private HttpSession session = proxy(HttpSession.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getAttribute".equals(name)) {
          return UiRequest.this.sessionAttributes.get(args[0]);
        }
        if ("setAttribute".equals(name)) {
          UiRequest.this.sessionAttributes.put((String)args[0], args[1]);
          return null;
        }
        if ("removeAttribute".equals(name)) {
          UiRequest.this.sessionAttributes.remove(args[0]);
          return null;
        }
        if ("getAttributeNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.sessionAttributes.keySet());
        }
        if ("getId".equals(name)) {
          return "junitSession";
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** request */
    private HttpServletRequest request = proxy(HttpServletRequest.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getParameter".equals(name)) {
          return UiRequest.this.params.get(args[0]);
        }
        if ("getParameterValues".equals(name)) {
          String value = UiRequest.this.params.get(args[0]);
          return value == null ? null : new String[] {value};
        }
        if ("getParameterNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.params.keySet());
        }
        if ("getParameterMap".equals(name)) {
          Map<String, String[]> result = new HashMap<String, String[]>();
          for (String key : UiRequest.this.params.keySet()) {
            result.put(key, new String[] {UiRequest.this.params.get(key)});
          }
          return result;
        }
        if ("getAttribute".equals(name)) {
          return UiRequest.this.attributes.get(args[0]);
        }
        if ("setAttribute".equals(name)) {
          UiRequest.this.attributes.put((String)args[0], args[1]);
          return null;
        }
        if ("removeAttribute".equals(name)) {
          UiRequest.this.attributes.remove(args[0]);
          return null;
        }
        if ("getAttributeNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.attributes.keySet());
        }
        if ("getSession".equals(name)) {
          return UiRequest.this.session;
        }
        if ("getLocale".equals(name)) {
          return Locale.US;
        }
        if ("getLocales".equals(name)) {
          return Collections.enumeration(Collections.singletonList(Locale.US));
        }
        if ("getMethod".equals(name)) {
          return "POST";
        }
        if ("getRequestURI".equals(name)) {
          return "/grouper/grouperUi/app/UiV2Main.index";
        }
        if ("getContextPath".equals(name)) {
          return "/grouper";
        }
        if ("getCharacterEncoding".equals(name)) {
          return "UTF-8";
        }
        if ("getRemoteUser".equals(name)) {
          return UiRequest.this.loggedIn.getId();
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** response, output is discarded */
    private HttpServletResponse response = proxy(HttpServletResponse.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getWriter".equals(name)) {
          return new PrintWriter(new StringWriter());
        }
        if ("getOutputStream".equals(name)) {
          return new ServletOutputStream() {
            @Override
            public void write(int b) {
            }
            @Override
            public boolean isReady() {
              return true;
            }
            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
          };
        }
        if ("getCharacterEncoding".equals(name)) {
          return "UTF-8";
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** servlet whose JSP includes do nothing */
    private HttpServlet servlet = new HttpServlet() {
      private static final long serialVersionUID = 1L;
      @Override
      public ServletContext getServletContext() {
        return proxy(ServletContext.class, new InvocationHandler() {
          @Override
          public Object invoke(Object p, Method method, Object[] args) {
            if ("getRequestDispatcher".equals(method.getName())) {
              return proxy(RequestDispatcher.class, new InvocationHandler() {
                @Override
                public Object invoke(Object p2, Method method2, Object[] args2) {
                  return objectMethodOrDefault(p2, method2, args2);
                }
              });
            }
            return objectMethodOrDefault(p, method, args);
          }
        });
      }
    };

    /** logged in subject */
    private Subject loggedIn;

    /**
     * set up the thread locals for a request by this subject
     * @param theLoggedIn subject
     */
    private UiRequest(Subject theLoggedIn) {
      this.loggedIn = theLoggedIn;
      setThreadLocal("threadLocalRequest", this.request);
      setThreadLocal("threadLocalResponse", this.response);
      setThreadLocal("threadLocalServlet", this.servlet);
      SessionContainer.retrieveFromSession().setSubjectLoggedIn(theLoggedIn);
    }

    /**
     * remove the thread locals
     */
    private static void clear() {
      setThreadLocal("threadLocalRequest", null);
      setThreadLocal("threadLocalResponse", null);
      setThreadLocal("threadLocalServlet", null);
    }

    /**
     * @param fieldName GrouperUiFilter thread local field
     * @param value value or null to remove
     */
    @SuppressWarnings("unchecked")
    private static void setThreadLocal(String fieldName, Object value) {
      try {
        Field field = GrouperUiFilter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ThreadLocal<Object> threadLocal = (ThreadLocal<Object>)field.get(null);
        if (value == null) {
          threadLocal.remove();
        } else {
          threadLocal.set(value);
        }
      } catch (Exception e) {
        throw new RuntimeException("cant set GrouperUiFilter." + fieldName, e);
      }
    }

    /**
     * @param <T> interface
     * @param theInterface interface
     * @param handler handler
     * @return proxy
     */
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> theInterface, InvocationHandler handler) {
      return (T)Proxy.newProxyInstance(UiRequest.class.getClassLoader(),
          new Class<?>[] {theInterface}, handler);
    }

    /**
     * equals / hashCode / toString, else a default for the return type (null, false, 0)
     * @param p proxy
     * @param method method
     * @param args args
     * @return value
     */
    private static Object objectMethodOrDefault(Object p, Method method, Object[] args) {
      String name = method.getName();
      if ("hashCode".equals(name)) {
        return System.identityHashCode(p);
      }
      if ("equals".equals(name)) {
        return p == args[0];
      }
      if ("toString".equals(name)) {
        return "junitProxy";
      }
      Class<?> type = method.getReturnType();
      if (type == boolean.class) {
        return false;
      }
      if (type == int.class) {
        return 0;
      }
      if (type == long.class) {
        return 0L;
      }
      return null;
    }
  }
}
