package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.textui.TestRunner;

/**
 * External invite submit (UiV2ExternalEntities.inviteSubmit) must authorize the whole
 * request before it stores a root-owned invitation: the feature must be on, there must be
 * at least one group, and the user must be able to invite into every group.
 * Before the fix a request with no group stored an invitation with nothing checked.
 */
public class ExternalInviteSubmitAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalInviteSubmitAuthorizationTest("testEmptyGroupsDenied"));
  }

  /**
   *
   */
  public ExternalInviteSubmitAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public ExternalInviteSubmitAuthorizationTest(String name) {
    super(name);
  }

  /** SUBJ0 has UPDATE */
  private Group updateGroup;

  /** SUBJ0 has only VIEW */
  private Group viewGroup;

  /** composite, SUBJ0 has UPDATE */
  private Group compositeGroup;

  /**
   * groups and privileges as root, then config for an enabled invite feature
   */
  @Override
  protected void setUp() {
    super.setUp();
    // 4.x GuiGroup.isCanInviteExternalUsers reads the "media" bundle from the http session
    setThreadLocalRequest(mediaRequest());

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      this.updateGroup = new GroupSave(rootSession).assignName("test:inviteUpdate")
          .assignCreateParentStemsIfNotExist(true).save();
      this.viewGroup = new GroupSave(rootSession).assignName("test:inviteView")
          .assignCreateParentStemsIfNotExist(true).save();
      this.compositeGroup = new GroupSave(rootSession).assignName("test:inviteComposite")
          .assignCreateParentStemsIfNotExist(true).save();
      Group left = new GroupSave(rootSession).assignName("test:inviteLeft")
          .assignCreateParentStemsIfNotExist(true).save();
      Group right = new GroupSave(rootSession).assignName("test:inviteRight")
          .assignCreateParentStemsIfNotExist(true).save();
      this.compositeGroup.addCompositeMember(CompositeType.UNION, left, right);

      this.updateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
      this.viewGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.compositeGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // GuiGroup.isCanInviteExternalUsers also needs the new-ui link switch
    setUiConfig("inviteExternalMembers.enableInvitation", "true");
    setUiConfig("inviteExternalPeople.link-from-new-ui", "true");
  }

  /**
   * @param key
   * @param value
   */
  private static void setUiConfig(String key, String value) {
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  @Override
  protected void tearDown() {
    setThreadLocalRequest(null);
    // do not let the inviter group setting leak into the next test
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().remove("require.group.for.inviteExternalSubjects.logins");
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("require.group.for.inviteExternalSubjects.logins");
    super.tearDown();
  }

  /**
   * a minimal request whose session has the media bundle, as GrouperUiFilter sets it up on a real
   * request (needed on 4.x, ignored by later releases)
   * @return the request proxy
   */
  private static HttpServletRequest mediaRequest() {
    // read the media properties on every lookup, so config a test sets after setUp is seen
    final ResourceBundle mediaBundle = new ResourceBundle() {
      @Override
      protected Object handleGetObject(String key) {
        return GrouperUiFilter.retrieveMediaProperties().getProperty(key);
      }
      @Override
      public Enumeration<String> getKeys() {
        Properties mediaProperties = GrouperUiFilter.retrieveMediaProperties();
        return Collections.enumeration(mediaProperties.stringPropertyNames());
      }
    };
    final LocalizationContext media = new LocalizationContext(mediaBundle);
    final HttpSession session = (HttpSession)Proxy.newProxyInstance(ExternalInviteSubmitAuthorizationTest.class.getClassLoader(),
        new Class<?>[] {HttpSession.class}, (proxy, method, args) -> {
          if ("getAttribute".equals(method.getName())) {
            return "media".equals(args[0]) ? media : null;
          }
          return null;
        });
    return (HttpServletRequest)Proxy.newProxyInstance(ExternalInviteSubmitAuthorizationTest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, (proxy, method, args) -> {
          if ("getSession".equals(method.getName())) {
            return session;
          }
          return null;
        });
  }

  /**
   * set GrouperUiFilter's thread local request (the field exists from 2.6.19 to 7.9.0)
   * @param request or null to clear
   */
  @SuppressWarnings("unchecked")
  private static void setThreadLocalRequest(HttpServletRequest request) {
    try {
      Field field = GrouperUiFilter.class.getDeclaredField("threadLocalRequest");
      field.setAccessible(true);
      ThreadLocal<HttpServletRequest> threadLocal = (ThreadLocal<HttpServletRequest>)field.get(null);
      if (request == null) {
        threadLocal.remove();
      } else {
        threadLocal.set(request);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * run the check as SUBJ0 (the handler runs it in a session as the logged in user)
   * @param groups
   * @return the deny text key or null
   */
  private static String denyAsSubj0(Group... groups) {
    Set<Group> allGroups = new LinkedHashSet<Group>();
    for (Group group : groups) {
      allGroups.add(group);
    }
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return ExternalInviteSubmitAuthorization.denyTextKey(allGroups);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the reproduced attack: no group at all
   */
  public void testEmptyGroupsDenied() {
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP, denyAsSubj0());
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP,
          ExternalInviteSubmitAuthorization.denyTextKey(null));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * feature off: denied even with a group SUBJ0 could invite into
   */
  public void testDisabledDenied() {
    setUiConfig("inviteExternalMembers.enableInvitation", "false");
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_NOT_ENABLED, denyAsSubj0(this.updateGroup));
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_NOT_ENABLED, denyAsSubj0());
  }

  /**
   * only VIEW: denied
   */
  public void testNoUpdateDenied() {
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP, denyAsSubj0(this.viewGroup));
  }

  /**
   * one allowed group and one not: the whole request is denied
   */
  public void testMixedDenied() {
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP,
        denyAsSubj0(this.updateGroup, this.viewGroup));
  }

  /**
   * composite groups cannot have invited members
   */
  public void testCompositeDenied() {
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP, denyAsSubj0(this.compositeGroup));
  }

  /**
   * configured inviter group that SUBJ0 is not in: denied
   */
  public void testNotInRequiredInviterGroupDenied() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GroupSave(rootSession).assignName("test:inviters").assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap()
      .put("require.group.for.inviteExternalSubjects.logins", "test:inviters");
    // 4.x reads it from the media bundle (UI config); later releases from grouper.properties
    setUiConfig("require.group.for.inviteExternalSubjects.logins", "test:inviters");
    assertEquals(ExternalInviteSubmitAuthorization.TEXT_KEY_CANT_FIND_GROUP, denyAsSubj0(this.updateGroup));
  }

  /**
   * normal use: one group with UPDATE is allowed
   */
  public void testAllowed() {
    assertNull(denyAsSubj0(this.updateGroup));
  }

}
