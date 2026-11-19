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
package edu.internet2.middleware.grouper.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M2 (wsAttributeDefActionAuthorization), HTTP check against the running WS: the
 * assignAttributeDefActions operation must check ATTR_ADMIN on the attribute definition before it
 * adds, removes or replaces any action.  A caller with ATTR_READ and ATTR_UPDATE (not ATTR_ADMIN)
 * gets HTTP 403 INSUFFICIENT_PRIVILEGES and nothing changes: the actions stay and the assignment
 * that uses the action is not deleted.  A caller with ATTR_ADMIN still can remove the action
 * (and its assignment goes with it).  AttributeDefActionAuthorizationTest covers the core class;
 * this test checks the WS operation (GrouperServiceLogic.assignAttributeDefActions) through real
 * HTTP requests.
 *
 * <p>Runs against the running local WS (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running WS uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch32 (deleted at the end), and test.subject.3 and test.subject.4 are added to
 * ws.client.user.group.name (only if not members, and removed again at the end).</p>
 *
 * <p>Uses HttpURLConnection and Java 8 APIs only.</p>
 */
public class AttributeDefActionAuthorizationHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch32";

  /** the attribute definition whose actions are changed */
  private static final String ATTR_DEF_NAME = STEM_NAME + ":actionTestDef";

  /** attribute name assigned with dangerAction */
  private static final String ATTR_DEF_NAME_NAME = STEM_NAME + ":actionTestAttr";

  /** group the attribute is assigned to */
  private static final String GROUP_NAME = STEM_NAME + ":protectedGroup";

  /** custom action that has an assignment */
  private static final String DANGER_ACTION = "dangerAction";

  /** has ATTR_READ and ATTR_UPDATE, not ATTR_ADMIN */
  private static final String UPDATER_ID = "test.subject.3";

  /** has ATTR_ADMIN */
  private static final String ADMIN_ID = "test.subject.4";

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AttributeDefActionAuthorizationHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AttributeDefActionAuthorizationHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeDefActionAuthorizationHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the manual test steps 1-3, plus the replace-all operation as the non admin
   * @throws Exception
   */
  public void testAssignActionsNeedsAttrAdmin() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      String assignId = setup();
      admitToWs(UPDATER_ID, ADMIN_ID);
      waitForWsAdmission(UPDATER_ID);
      waitForWsAdmission(ADMIN_ID);

      Set<String> actionsBefore = actions();
      assertTrue("setup: " + actionsBefore, actionsBefore.contains(DANGER_ACTION));

      // step 1: remove dangerAction without ATTR_ADMIN
      WsResponse remove = assignActions(UPDATER_ID, "[\"" + DANGER_ACTION + "\"]", "F", null);
      assertEquals("actions must not change: " + remove, actionsBefore, actions());
      assertTrue("the assignment using dangerAction must not be deleted: " + remove, assignmentExists(assignId));
      assertEquals("must be HTTP 403: " + remove, 403, remove.status);
      assertEquals(remove.toString(), "INSUFFICIENT_PRIVILEGES", resultCode(remove.body));

      // step 2: add an action without ATTR_ADMIN
      WsResponse add = assignActions(UPDATER_ID, "[\"newAction\"]", "T", null);
      assertEquals("actions must not change: " + add, actionsBefore, actions());
      assertEquals("must be HTTP 403: " + add, 403, add.status);
      assertEquals(add.toString(), "INSUFFICIENT_PRIVILEGES", resultCode(add.body));

      // replace all actions without ATTR_ADMIN
      WsResponse replace = assignActions(UPDATER_ID, "[\"onlyThis\"]", "T", "T");
      assertEquals("actions must not change: " + replace, actionsBefore, actions());
      assertTrue("the assignment using dangerAction must not be deleted: " + replace, assignmentExists(assignId));
      assertEquals("must be HTTP 403: " + replace, 403, replace.status);
      assertEquals(replace.toString(), "INSUFFICIENT_PRIVILEGES", resultCode(replace.body));

      // step 3: with ATTR_ADMIN the removal works, and its assignment goes with it
      WsResponse adminRemove = assignActions(ADMIN_ID, "[\"" + DANGER_ACTION + "\"]", "F", null);
      assertTrue("ATTR_ADMIN removal must succeed: " + adminRemove, adminRemove.status >= 200 && adminRemove.status < 300);
      assertEquals(adminRemove.toString(), "T", success(adminRemove.body));
      assertFalse("dangerAction removed: " + adminRemove, actions().contains(DANGER_ACTION));
      assertFalse("its assignment removed with it", assignmentExists(assignId));

    } finally {
      cleanup();
    }
  }

  /**
   * PUT attributeDefActions
   * @param user caller
   * @param actionsJson json array of action names
   * @param assign T or F
   * @param replaceAllExisting T, F or null
   * @return the response
   * @throws IOException
   */
  private static WsResponse assignActions(String user, String actionsJson, String assign, String replaceAllExisting)
      throws IOException {
    String body = "{\"WsRestAssignAttributeDefActionsRequest\":{\"wsAttributeDefLookup\":{\"name\":\""
        + ATTR_DEF_NAME + "\"},\"actions\":" + actionsJson + ",\"assign\":\"" + assign + "\""
        + (replaceAllExisting == null ? "" : ",\"replaceAllExisting\":\"" + replaceAllExisting + "\"") + "}}";
    return call("PUT", wsBaseUrl() + "/servicesRest/v2_4_000/attributeDefActions", user, "application/json", body);
  }

  // ---------------------------------------------------------------- setup and cleanup

  /**
   * create the attribute definition with actions assign and dangerAction, an attribute name
   * assigned with dangerAction to a group, and the privileges
   * @return id of the attribute assignment that uses dangerAction
   */
  private static String setup() {
    deleteStem();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeDef attributeDef = new AttributeDefSave(rootSession).assignName(ATTR_DEF_NAME)
          .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
          .assignCreateParentStemsIfNotExist(true).save();
      attributeDef.getAttributeDefActionDelegate().addAction(DANGER_ACTION);
      AttributeDefName attributeDefName = new AttributeDefNameSave(rootSession, attributeDef)
          .assignName(ATTR_DEF_NAME_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      String assignId = group.getAttributeDelegate().assignAttribute(DANGER_ACTION, attributeDefName)
          .getAttributeAssign().getId();

      Subject updater = SubjectFinder.findByIdAndSource(UPDATER_ID, "jdbc", true);
      attributeDef.getPrivilegeDelegate().grantPriv(updater, AttributeDefPrivilege.ATTR_READ, false);
      attributeDef.getPrivilegeDelegate().grantPriv(updater, AttributeDefPrivilege.ATTR_UPDATE, false);
      Subject admin = SubjectFinder.findByIdAndSource(ADMIN_ID, "jdbc", true);
      attributeDef.getPrivilegeDelegate().grantPriv(admin, AttributeDefPrivilege.ATTR_ADMIN, false);
      return assignId;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return the actions of the definition, read with sql (the WS changes them in another JVM, so
   * this JVM's cached action list can be stale)
   */
  private static Set<String> actions() {
    List<Object> params = new ArrayList<Object>();
    params.add(ATTR_DEF_NAME);
    List<String> names = HibernateSession.bySqlStatic().listSelect(String.class,
        "select gaaa.name from grouper_attr_assign_action gaaa, grouper_attribute_def gad "
        + "where gaaa.attribute_def_id = gad.id and gad.name = ?", params);
    return new TreeSet<String>(names);
  }

  /**
   * @param assignId
   * @return true if the attribute assignment row is still there
   */
  private static boolean assignmentExists(String assignId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(assignId, false, false) != null;
  }

  /**
   * delete the folder and what is in it, if there
   */
  private static void deleteStem() {
    try {
      deleteStemOnce();
    } catch (RuntimeException re) {
      // the WS changed the actions in its own JVM, so this JVM's cached action list can be
      // stale (delete of an action row that is gone): clear the caches and try again
      GrouperCacheUtils.clearAllCaches();
      deleteStemOnce();
    }
  }

  /**
   * delete the folder and what is in it, if there
   */
  private static void deleteStemOnce() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.obliterate(false, false);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the folder and take out of ws.client.user.group.name exactly the subjects this test added
   */
  private void cleanup() {
    try {
      deleteStem();
    } finally {
      removeFromWs();
    }
  }

  // ---------------------------------------------------------------- WS admission

  /**
   * add subjects to ws.client.user.group.name (the WS only admits its members).  Only subjects
   * that are not already members are added and remembered, so cleanup removes only those.
   * @param subjectIds
   */
  private void admitToWs(String... subjectIds) {
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    if (GrouperUtil.isBlank(groupName)) {
      return;
    }
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);
      if (group == null) {
        // the WS creates it empty on first use too
        group = new GroupSave(rootSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
      }
      for (String subjectId : subjectIds) {
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, "jdbc", true);
        if (!group.hasMember(subject)) {
          group.addMember(subject, false);
          this.addedToWsClients.add(subject);
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * remove from ws.client.user.group.name the subjects admitToWs added
   */
  private void removeFromWs() {
    if (this.addedToWsClients.size() == 0) {
      return;
    }
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, true);
      for (Subject subject : this.addedToWsClients) {
        group.deleteMember(subject, false);
      }
      this.addedToWsClients.clear();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the WS caches "not admitted" per subject (ws.client.user.group.cache.minutes), so after
   * adding a subject keep calling until it is let in
   * @param subjectId
   * @throws IOException
   */
  private static void waitForWsAdmission(String subjectId) throws IOException {
    long deadline = System.currentTimeMillis() + 330 * 1000L;
    while (true) {
      WsResponse response = call("POST", wsBaseUrl() + "/servicesRest/v2_4_000/groups", subjectId,
          "application/json", "{\"WsRestFindGroupsRequest\":{\"wsQueryFilter\":{\"queryFilterType\":"
          + "\"FIND_BY_GROUP_NAME_EXACT\",\"groupName\":\"" + GROUP_NAME + "\"}}}");
      if (!response.body.contains("User is not authorized")) {
        return;
      }
      if (System.currentTimeMillis() > deadline) {
        fail("WS still refuses " + subjectId + " (ws.client.user.group.name): " + response);
      }
      GrouperUtil.sleep(10000);
    }
  }

  // ---------------------------------------------------------------- http helpers

  /**
   * one http response
   */
  private static final class WsResponse {

    /** http status */
    private int status;

    /** response body, empty if none */
    private String body;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "HTTP " + this.status + ": " + (this.body.length() > 3000 ? this.body.substring(0, 3000) + "..." : this.body);
    }
  }

  /**
   * base url of the running WS from the same junit.test.tomcat.* keys the mock tests use
   * @return e.g. http://localhost:8080/grouper-ws
   */
  private static String wsBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    return (ssl ? "https" : "http") + "://" + domainName + ":" + port + "/grouper-ws";
  }

  /**
   * send a request with HTTP basic auth (15 s timeouts)
   * @param method
   * @param url
   * @param user subject id, password is PASSWORD
   * @param contentType of the body, or null
   * @param body or null for none
   * @return the response
   * @throws IOException
   */
  private static WsResponse call(String method, String url, String user, String contentType, String body)
      throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    try {
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      connection.setRequestMethod(method);
      String credentials = user + ":" + PASSWORD;
      connection.setRequestProperty("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8")));
      if (body != null) {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", contentType);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes("UTF-8"));
        outputStream.close();
      }
      WsResponse wsResponse = new WsResponse();
      wsResponse.status = connection.getResponseCode();
      InputStream inputStream = wsResponse.status >= 400 ? connection.getErrorStream() : connection.getInputStream();
      wsResponse.body = readFully(inputStream);
      return wsResponse;
    } finally {
      connection.disconnect();
    }
  }

  /**
   * @param inputStream may be null
   * @return the content as UTF-8, empty if none
   * @throws IOException
   */
  private static String readFully(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }
    try {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, read);
      }
      return new String(result.toByteArray(), "UTF-8");
    } finally {
      inputStream.close();
    }
  }

  /**
   * @param body json response
   * @return the first resultCode in it, or null
   */
  private static String resultCode(String body) {
    return firstJsonString(body, "resultCode");
  }

  /**
   * @param body json response
   * @return the first success flag in it, or null
   */
  private static String success(String body) {
    return firstJsonString(body, "success");
  }

  /**
   * @param body json
   * @param field name
   * @return first string value of the field, or null
   */
  private static String firstJsonString(String body, String field) {
    Matcher matcher = Pattern.compile("\"" + field + "\":\"([^\"]*)\"").matcher(body);
    return matcher.find() ? matcher.group(1) : null;
  }

}
