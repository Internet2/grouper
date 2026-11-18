/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOidc;
import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * UI OIDC login subject binding (issue uiOidcIdentityBindingIncompleteFix): the user id from a UI
 * OIDC login resolves only in the connector's sources and by its subjectIdType, never by falling
 * back to all sources, and never to an internal (g:isa) subject; the plural subjectSourceIds that
 * the config screen saves is read.
 *
 * <p>Config is set in the override map, one connector id per case (GrouperOidcConfig caches by id).
 * The connector's endpoints are never contacted.  Fails before the fix: GrouperUiOidcSubjectResolver
 * is new, and GrouperOidcConfig.getSubjectSourceId() ignores subjectSourceIds.  WS OAuth
 * (GrouperOidc.findSubject) is driven with the claims set directly: before the fix it passed a list
 * of selected sources to SubjectFinder as one source id.  The remoteUser part
 * of the fix (a container or form login identity no longer satisfies an OIDC route) is covered by
 * the manual test steps.</p>
 */
public class GrouperUiOidcSubjectResolverTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperUiOidcSubjectResolverTest("testResolveOnlyInConnectorSources"));
  }

  /**
   * @param name
   */
  public GrouperUiOidcSubjectResolverTest(String name) {
    super(name);
  }

  /** prefix of the connector ids of this test */
  private static final String CONFIG_ID_PREFIX = "secTest182";

  /** unique suffix per run */
  private final String runId = Long.toString(System.currentTimeMillis());

  /**
   * open a root session: SubjectFinder needs one on 4.x (SourcesXmlResolver.find); harmless later
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSession();
  }

  /**
   * remove the test connectors from the override map
   */
  @Override
  protected void tearDown() {
    Iterator<String> iterator = GrouperConfig.retrieveConfig().propertiesOverrideMap().keySet().iterator();
    while (iterator.hasNext()) {
      if (iterator.next().startsWith("grouper.oidcExternalSystem." + CONFIG_ID_PREFIX)) {
        iterator.remove();
      }
    }
    super.tearDown();
  }

  /**
   * one connector in the override map, endpoints are never contacted
   * @param name
   * @param subjectIdType
   * @param subjectSourceIds plural key as the config screen saves it, or null
   * @param subjectSourceId legacy singular key, or null
   * @return the connector config id
   */
  private String connector(String name, String subjectIdType, String subjectSourceIds, String subjectSourceId) {
    String configId = CONFIG_ID_PREFIX + name + this.runId;
    Map<String, String> overrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    String prefix = "grouper.oidcExternalSystem." + configId + ".";
    overrides.put(prefix + "clientId", "client_" + configId);
    overrides.put(prefix + "clientSecret", "notASecret");
    overrides.put(prefix + "enabled", "true");
    overrides.put(prefix + "useConfigurationMetadata", "false");
    overrides.put(prefix + "claimSource", "userInfoEndpoint");
    overrides.put(prefix + "userInfoUri", "https://idp.example.test/userinfo");
    overrides.put(prefix + "tokenEndpointUri", "https://idp.example.test/token");
    overrides.put(prefix + "authorizeUri", "https://idp.example.test/authorize");
    overrides.put(prefix + "redirectUri", "https://grouper.example.test/grouper/grouperUi/app/UiV2Main.oidc");
    overrides.put(prefix + "scope", "openid");
    overrides.put(prefix + "oidcResponseType", "code");
    overrides.put(prefix + "subjectIdType", subjectIdType);
    overrides.put(prefix + "subjectIdClaimName", "preferred_username");
    if (subjectSourceIds != null) {
      overrides.put(prefix + "subjectSourceIds", subjectSourceIds);
    }
    if (subjectSourceId != null) {
      overrides.put(prefix + "subjectSourceId", subjectSourceId);
    }
    return configId;
  }

  /**
   * strict resolve must fail
   * @param message
   * @param configId
   * @param userId
   */
  private static void assertRefused(String message, String configId, String userId) {
    try {
      Subject subject = GrouperUiOidcSubjectResolver.resolveSubject(configId, userId, null, true);
      fail(message + ": expected the login to fail, got " + subject.getSourceId() + " - " + subject.getId());
    } catch (SubjectNotFoundException snfe) {
      // good
    }
  }

  /**
   * the connector's sources (plural or singular key) and id type are enforced, no fallback, no g:isa
   */
  public void testResolveOnlyInConnectorSources() {

    Subject subj0 = SubjectFinder.findById(SubjectTestHelper.SUBJ0_ID, true);
    String subj0SourceId = subj0.getSourceId();

    // plural key, as the config screen saves it, is read and enforced
    String pluralConfigId = connector("Plural", "subjectIdOrIdentifier", subj0SourceId, null);
    assertEquals("subjectSourceIds is read", subj0SourceId,
        GrouperOidcConfig.retrieveFromConfigOrCache(pluralConfigId).getSubjectSourceId());
    Subject subject = GrouperUiOidcSubjectResolver.resolveSubject(pluralConfigId, SubjectTestHelper.SUBJ0_ID, null, true);
    assertEquals(subj0SourceId, subject.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());
    // GrouperSystem is not in the connector's source: before the fix the UI fell back to all sources (root)
    assertRefused("claim only in another source", pluralConfigId, "GrouperSystem");
    assertRefused("claim in no source", pluralConfigId, "noSuchSubject" + this.runId);

    // the connector's source wins over grouper.ui.authentication.sourceIds
    try {
      GrouperUiOidcSubjectResolver.resolveSubject(pluralConfigId, "GrouperSystem", "g:isa", true);
      fail("connector source wins over the UI sources");
    } catch (SubjectNotFoundException snfe) {
      // good
    }

    // legacy singular key still applies
    String singularConfigId = connector("Singular", "subjectIdOrIdentifier", null, subj0SourceId);
    assertEquals(subj0SourceId, GrouperOidcConfig.retrieveFromConfigOrCache(singularConfigId).getSubjectSourceId());
    assertRefused("singular key enforced", singularConfigId, "GrouperSystem");
    assertNotNull(GrouperUiOidcSubjectResolver.resolveSubject(singularConfigId, SubjectTestHelper.SUBJ0_ID, null, true));

    // subjectIdType is enforced: an identifier is not a subject id
    String idOnlyConfigId = connector("IdOnly", "subjectId", subj0SourceId, null);
    assertNotNull(GrouperUiOidcSubjectResolver.resolveSubject(idOnlyConfigId, SubjectTestHelper.SUBJ0_ID, null, true));
    assertRefused("identifier with subjectIdType subjectId", idOnlyConfigId, SubjectTestHelper.SUBJ0_IDENTIFIER);

    // no source anywhere: all sources, but never an internal subject
    String noSourceConfigId = connector("NoSource", "subjectIdOrIdentifier", null, null);
    assertNotNull(GrouperUiOidcSubjectResolver.resolveSubject(noSourceConfigId, SubjectTestHelper.SUBJ0_ID, null, true));
    assertRefused("GrouperSystem from OIDC", noSourceConfigId, "GrouperSystem");
    assertRefused("GrouperAll from OIDC", noSourceConfigId, "GrouperAll");

    // even if the connector names the internal source
    String isaConfigId = connector("Isa", "subjectId", "g:isa", null);
    assertRefused("connector source g:isa", isaConfigId, "GrouperSystem");

    // lenient (a container identity, not from OIDC): not found is null, not an exception
    assertNull(GrouperUiOidcSubjectResolver.resolveSubject(pluralConfigId, "GrouperSystem", null, false));
  }

  /**
   * WS OAuth (GrouperOidc.findSubject) with claims put in directly, no token is decoded
   * @param configId
   * @param claimValue value of the subjectIdClaimName claim (preferred_username)
   * @return the subject or null
   */
  private static Subject wsFindSubject(String configId, String claimValue) {
    GrouperOidc grouperOidc = new GrouperOidc().assignExternalSystemConfigId(configId);
    Map<String, String> claims = new HashMap<String, String>();
    claims.put("preferred_username", claimValue);
    // the claim map field differs by release (accessTokenAttributes in 2.6, userInfo / idToken later): set every one present
    int fieldsSet = 0;
    for (String fieldName : new String[] {"userInfoAttributes", "idTokenAttributes", "accessTokenAttributes"}) {
      try {
        Field field = GrouperOidc.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(grouperOidc, claims);
        fieldsSet++;
      } catch (NoSuchFieldException nsfe) {
        // not in this release
      } catch (IllegalAccessException iae) {
        throw new RuntimeException(iae);
      }
    }
    assertTrue("GrouperOidc has a claim map field", fieldsSet > 0);
    return grouperOidc.findSubject();
  }

  /**
   * @param throwable
   * @return true if this or a cause is SubjectNotUniqueException
   */
  private static boolean isNotUnique(Throwable throwable) {
    while (throwable != null) {
      if (throwable instanceof SubjectNotUniqueException) {
        return true;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  /**
   * WS OAuth looks in each selected source (comma separated subjectSourceIds), never in others,
   * refuses a claim that matches subjects in two selected sources, and with no source still
   * searches all sources as before
   */
  public void testWsOauthSelectedSources() {

    Subject subj0 = SubjectFinder.findById(SubjectTestHelper.SUBJ0_ID, true);
    String subj0SourceId = subj0.getSourceId();

    // two sources (with a blank entry and spaces): each is searched.  Before the fix the whole list
    // went to findByIdAndSource as one source id and the login failed
    String twoConfigId = connector("WsTwo", "subjectId", subj0SourceId + ", ,g:isa,", null);
    Subject subject = wsFindSubject(twoConfigId, SubjectTestHelper.SUBJ0_ID);
    assertNotNull("found in the first selected source", subject);
    assertEquals(subj0SourceId, subject.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());
    subject = wsFindSubject(twoConfigId, "GrouperSystem");
    assertNotNull("found in the second selected source", subject);
    assertEquals("g:isa", subject.getSourceId());
    assertNull("in no selected source", wsFindSubject(twoConfigId, "noSuchSubject" + this.runId));

    // by identifier, two sources
    String identifierConfigId = connector("WsIdentifier", "subjectIdentifier", "g:isa," + subj0SourceId, null);
    subject = wsFindSubject(identifierConfigId, SubjectTestHelper.SUBJ0_IDENTIFIER);
    assertNotNull("identifier found in a selected source", subject);
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());

    // one source: a subject that exists only in another source is not found (no fallback to all sources)
    String oneConfigId = connector("WsOne", "subjectIdOrIdentifier", subj0SourceId, null);
    assertNotNull(wsFindSubject(oneConfigId, SubjectTestHelper.SUBJ0_ID));
    assertNull("GrouperSystem is not in the selected source", wsFindSubject(oneConfigId, "GrouperSystem"));

    // no source selected: all sources, as before the fix
    String allConfigId = connector("WsAll", "subjectId", null, null);
    assertNotNull(wsFindSubject(allConfigId, SubjectTestHelper.SUBJ0_ID));

    // the same value is a subject in two selected sources: a group (g:gsa, id is its uuid) and a
    // registry subject in the person source with that uuid as its id.  Refused, never one of them
    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = null;
    try {
      group = new GroupSave(rootSession).assignName("test:secTest182ws" + this.runId)
          .assignCreateParentStemsIfNotExist(true).save();
      RegistrySubject.add(rootSession, group.getUuid(), "person", "secTest182 same id as a group");
      String ambiguousConfigId = connector("WsAmbiguous", "subjectId", subj0SourceId + ",g:gsa", null);
      try {
        subject = wsFindSubject(ambiguousConfigId, group.getUuid());
        fail("claim matching subjects in two selected sources must be refused, got "
            + (subject == null ? "null" : subject.getSourceId() + " - " + subject.getId()));
      } catch (RuntimeException re) {
        assertTrue("SubjectNotUniqueException expected: " + re, isNotUnique(re));
      }
      // each source alone still finds its own subject
      subject = wsFindSubject(connector("WsGroupOnly", "subjectId", "g:gsa", null), group.getUuid());
      assertNotNull(subject);
      assertEquals("g:gsa", subject.getSourceId());
    } finally {
      if (group != null) {
        group.delete();
      }
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the session marker says which connector a user id came from, for that user id only
   */
  public void testSessionMarker() {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    HttpSession httpSession = (HttpSession)Proxy.newProxyInstance(GrouperUiOidcSubjectResolverTest.class.getClassLoader(),
        new Class[] {HttpSession.class}, new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("removeAttribute".equals(method.getName())) {
              attributes.remove(args[0]);
              return null;
            }
            return null;
          }
        });

    assertNull(GrouperUiOidcSubjectResolver.oidcConfigIdForSessionUser(httpSession, "jsmith"));
    GrouperUiOidcSubjectResolver.assignOidcLoginToSession(httpSession, "myOidc", "jsmith");
    assertEquals("myOidc", GrouperUiOidcSubjectResolver.oidcConfigIdForSessionUser(httpSession, "jsmith"));
    assertNull("another user id did not come from that login",
        GrouperUiOidcSubjectResolver.oidcConfigIdForSessionUser(httpSession, "someoneElse"));
    assertNull(GrouperUiOidcSubjectResolver.oidcConfigIdForSessionUser(null, "jsmith"));
  }

}
