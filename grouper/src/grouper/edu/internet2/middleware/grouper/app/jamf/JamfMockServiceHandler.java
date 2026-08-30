package edu.internet2.middleware.grouper.app.jamf;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Mock Jamf Pro Classic API for offline tests. Simulates the accounts endpoints against the
 * mock_jamf_* tables, speaking XML in and out exactly like the real Classic API (including the
 * {@code <user_group>} wrapper Jamf returns on account-group writes).
 *
 * <p>Routes under mock name "jamf": e.g. {@code .../jamf/JSSResource/accounts/username/jdoe@upenn.edu}.
 * Auth is a static Bearer token (the OAuth client-credentials flow is a prod-only concern handled
 * by the external system; tests configure a static accessTokenPassword).</p>
 */
public class JamfMockServiceHandler extends MockServiceHandler {

  public JamfMockServiceHandler() {
  }

  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  private static boolean mockTablesThere = false;

  /**
   * Create the mock_jamf_* tables if they do not exist yet.
   */
  public static void ensureJamfMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_jamf_account").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_jamf_account_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_jamf_membership").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          JamfAccount.createTableJamfAccount(ddlVersionBean, database);
          JamfAccountGroup.createTableJamfAccountGroup(ddlVersionBean, database);
          JamfMembership.createTableJamfMembership(ddlVersionBean, database);
        }
      });
    }
  }

  /**
   * Validate the Bearer token against the configured accessTokenPassword.
   *
   * <p>This mock runs in the test Tomcat JVM, which caches config; the jamfDev external-system
   * config is written by the (separate) JUnit JVM and can be stale here. So if the token does not
   * match on the first read, the config cache is cleared and the read retried once, self-healing
   * the cross-JVM propagation lag instead of returning a spurious 401.</p>
   *
   * @param mockServiceRequest the request
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authHeader = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization: Bearer header is required");
    }
    String bearerToken = authHeader.substring("Bearer ".length()).trim();

    if (bearerTokenMatches(bearerToken)) {
      return;
    }
    // config may be stale in this Tomcat JVM -- force a fresh read from the DB and retry once
    ConfigPropertiesCascadeBase.clearCache();
    if (!bearerTokenMatches(bearerToken)) {
      throw new RuntimeException("Authorization Bearer token does not match accessTokenPassword");
    }
  }

  /**
   * @param bearerToken the token from the Authorization header
   * @return true if it equals the configured accessTokenPassword for the mock external system
   */
  private boolean bearerTokenMatches(String bearerToken) {
    String configId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperTest.exampleJamf.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      return false;
    }
    String accessTokenPassword = edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig
        .retrieveConfig().propertyValueString("grouper.wsBearerToken." + configId + ".accessTokenPassword");
    return !StringUtils.isBlank(accessTokenPassword) && StringUtils.equals(accessTokenPassword, bearerToken);
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureJamfMockTables();
      mockTablesThere = true;
    }

    String[] paths = mockServiceRequest.getPostMockNamePaths();
    if (GrouperUtil.length(paths) < 2
        || !"JSSResource".equals(paths[0]) || !"accounts".equals(paths[1])) {
      throw new RuntimeException("Jamf mock expects /JSSResource/accounts/...");
    }

    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();

    // sub-path after "accounts": e.g. ["username","jdoe@upenn.edu"] or ["groupid","34"] or []
    String subResource = paths.length >= 3 ? paths[2] : null;   // username | userid | groupid | null
    String subId = paths.length >= 4 ? paths[3] : null;

    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      mockServiceResponse.setContentType("application/xml");
      mockServiceResponse.setResponseBody("<error>invalid_client</error>");
      return;
    }

    if ("GET".equals(httpMethod)) {
      if (subResource == null) {
        getAccountsAndGroups(mockServiceResponse);
        return;
      }
      if ("username".equals(subResource)) {
        getAccountBy(mockServiceResponse, "name", subId);
        return;
      }
      if ("userid".equals(subResource)) {
        getAccountBy(mockServiceResponse, "id", subId);
        return;
      }
      if ("groupid".equals(subResource)) {
        getAccountGroup(mockServiceResponse, subId);
        return;
      }
    } else if ("POST".equals(httpMethod)) {
      if ("userid".equals(subResource)) {
        createAccount(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groupid".equals(subResource)) {
        createAccountGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
    } else if ("PUT".equals(httpMethod)) {
      if ("groupid".equals(subResource)) {
        updateAccountGroup(mockServiceRequest, mockServiceResponse, subId);
        return;
      }
      if ("userid".equals(subResource)) {
        updateAccount(mockServiceRequest, mockServiceResponse, subId);
        return;
      }
    } else if ("DELETE".equals(httpMethod)) {
      if ("userid".equals(subResource)) {
        deleteAccount(mockServiceResponse, subId);
        return;
      }
      if ("groupid".equals(subResource)) {
        deleteAccountGroup(mockServiceResponse, subId);
        return;
      }
    }

    throw new RuntimeException("Unsupported Jamf mock request: " + httpMethod + " "
        + mockServiceRequest.getPostMockNamePath());
  }

  // ==================== reads ====================

  /**
   * GET /JSSResource/accounts -- list all accounts and account groups (id + name).
   */
  private void getAccountsAndGroups(MockServiceResponse mockServiceResponse) {
    List<JamfAccount> accounts = HibernateSession.byHqlStatic()
        .createQuery("from JamfAccount order by name").list(JamfAccount.class);
    List<JamfAccountGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from JamfAccountGroup order by name").list(JamfAccountGroup.class);

    StringBuilder sb = new StringBuilder("<accounts><users>");
    for (JamfAccount account : accounts) {
      sb.append("<user>");
      appendEl(sb, "id", account.getId());
      appendEl(sb, "name", account.getName());
      sb.append("</user>");
    }
    sb.append("</users><groups>");
    for (JamfAccountGroup group : groups) {
      sb.append("<group>");
      appendEl(sb, "id", group.getId());
      appendEl(sb, "name", group.getName());
      sb.append("<site>");
      appendEl(sb, "id", GrouperUtil.defaultIfBlank(group.getSiteId(), "-1"));
      appendEl(sb, "name", GrouperUtil.defaultIfBlank(group.getSiteName(), "NONE"));
      sb.append("</site></group>");
    }
    sb.append("</groups></accounts>");
    ok(mockServiceResponse, sb.toString());
  }

  /**
   * GET account by name or id -- returns the full account detail.
   */
  private void getAccountBy(MockServiceResponse mockServiceResponse, String field, String value) {
    JamfAccount account = findAccount(field, value);
    if (account == null) {
      notFound(mockServiceResponse);
      return;
    }
    StringBuilder sb = new StringBuilder("<account>");
    appendEl(sb, "id", account.getId());
    appendEl(sb, "name", account.getName());
    appendEl(sb, "directory_user", account.getDirectoryUser() != null && account.getDirectoryUser() ? "true" : "false");
    appendEl(sb, "full_name", account.getFullName());
    appendEl(sb, "email", account.getEmail());
    appendEl(sb, "email_address", account.getEmail());
    appendEl(sb, "enabled", GrouperUtil.defaultIfBlank(account.getEnabled(), JamfAccount.ENABLED));
    appendEl(sb, "access_level", account.getAccessLevel());
    appendEl(sb, "privilege_set", account.getPrivilegeSet());
    sb.append("</account>");
    ok(mockServiceResponse, sb.toString());
  }

  /**
   * GET /JSSResource/accounts/groupid/{id} -- role detail with its members.
   */
  private void getAccountGroup(MockServiceResponse mockServiceResponse, String groupId) {
    JamfAccountGroup group = HibernateSession.byHqlStatic()
        .createQuery("from JamfAccountGroup where id = :theId")
        .setString("theId", groupId).uniqueResult(JamfAccountGroup.class);
    if (group == null) {
      notFound(mockServiceResponse);
      return;
    }
    List<JamfMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from JamfMembership where groupId = :theGroupId order by accountName")
        .setString("theGroupId", groupId).list(JamfMembership.class);

    StringBuilder sb = new StringBuilder("<group>");
    appendEl(sb, "id", group.getId());
    appendEl(sb, "name", group.getName());
    appendEl(sb, "access_level", group.getAccessLevel());
    appendEl(sb, "privilege_set", group.getPrivilegeSet());
    sb.append("<site>");
    appendEl(sb, "id", GrouperUtil.defaultIfBlank(group.getSiteId(), "-1"));
    appendEl(sb, "name", GrouperUtil.defaultIfBlank(group.getSiteName(), "NONE"));
    sb.append("</site>");
    sb.append("<members>");
    for (JamfMembership membership : memberships) {
      JamfAccount memberAccount = findAccount("name", membership.getAccountName());
      sb.append("<user>");
      appendEl(sb, "id", memberAccount == null ? "" : memberAccount.getId());
      appendEl(sb, "name", membership.getAccountName());
      sb.append("</user>");
    }
    sb.append("</members></group>");
    ok(mockServiceResponse, sb.toString());
  }

  // ==================== writes ====================

  /**
   * POST /JSSResource/accounts/userid/0 -- create an account.
   */
  private void createAccount(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    Element root = parseBody(mockServiceRequest);
    String name = root.elementTextTrim("name");
    if (StringUtils.isBlank(name)) {
      conflict(mockServiceResponse, "name is required");
      return;
    }
    if (findAccount("name", name) != null) {
      conflict(mockServiceResponse, "account already exists");
      return;
    }
    JamfAccount account = new JamfAccount();
    account.setId(newNumericId());
    account.setName(name);
    account.setFullName(root.elementTextTrim("full_name"));
    account.setEmail(root.elementTextTrim("email"));
    account.setAccessLevel(GrouperUtil.defaultIfBlank(root.elementTextTrim("access_level"),
        JamfAccount.ACCESS_LEVEL_GROUP_ACCESS));
    account.setEnabled(GrouperUtil.defaultIfBlank(root.elementTextTrim("enabled"), JamfAccount.ENABLED));
    account.setDirectoryUser(Boolean.FALSE);
    HibernateSession.byObjectStatic().save(account);

    created(mockServiceResponse, "<account><id>" + account.getId() + "</id></account>");
  }

  /**
   * PUT /JSSResource/accounts/userid/{id} -- partial account update.
   */
  private void updateAccount(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String accountId) {
    JamfAccount account = findAccount("id", accountId);
    if (account == null) {
      notFound(mockServiceResponse);
      return;
    }
    Element root = parseBody(mockServiceRequest);
    setIfPresent(root, "name", account::setName);
    setIfPresent(root, "full_name", account::setFullName);
    setIfPresent(root, "email", account::setEmail);
    setIfPresent(root, "access_level", account::setAccessLevel);
    setIfPresent(root, "privilege_set", account::setPrivilegeSet);
    setIfPresent(root, "enabled", account::setEnabled);
    HibernateSession.byObjectStatic().update(account);
    created(mockServiceResponse, "<account><id>" + account.getId() + "</id></account>");
  }

  /**
   * POST /JSSResource/accounts/groupid/0 -- create an account group (role). For test setup.
   */
  private void createAccountGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    Element root = parseBody(mockServiceRequest);
    JamfAccountGroup group = new JamfAccountGroup();
    group.setId(newNumericId());
    group.setName(root.elementTextTrim("name"));
    group.setAccessLevel(root.elementTextTrim("access_level"));
    group.setPrivilegeSet(root.elementTextTrim("privilege_set"));
    HibernateSession.byObjectStatic().save(group);
    // account-group writes echo a <user_group> wrapper in the real API
    created(mockServiceResponse, "<user_group><id>" + group.getId() + "</id></user_group>");
  }

  /**
   * PUT /JSSResource/accounts/groupid/{id} -- replace the member list (and/or partial group update).
   * A membership change sends only {@code <name>}/{@code <members>}; privileges are untouched.
   */
  private void updateAccountGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String groupId) {
    JamfAccountGroup group = HibernateSession.byHqlStatic()
        .createQuery("from JamfAccountGroup where id = :theId")
        .setString("theId", groupId).uniqueResult(JamfAccountGroup.class);
    if (group == null) {
      notFound(mockServiceResponse);
      return;
    }
    Element root = parseBody(mockServiceRequest);

    // partial group-attribute updates (privileges intentionally not modeled here)
    boolean groupChanged = false;
    if (root.element("access_level") != null) { group.setAccessLevel(root.elementTextTrim("access_level")); groupChanged = true; }
    if (root.element("privilege_set") != null) { group.setPrivilegeSet(root.elementTextTrim("privilege_set")); groupChanged = true; }
    if (groupChanged) {
      HibernateSession.byObjectStatic().update(group);
    }

    // if <members> is present, replace the entire membership set for this role
    Element membersElement = root.element("members");
    if (membersElement != null) {
      List<JamfMembership> existing = HibernateSession.byHqlStatic()
          .createQuery("from JamfMembership where groupId = :theGroupId")
          .setString("theGroupId", groupId).list(JamfMembership.class);
      for (JamfMembership membership : existing) {
        HibernateSession.byObjectStatic().delete(membership);
      }
      for (Object obj : membersElement.elements("user")) {
        String memberName = ((Element) obj).elementTextTrim("name");
        if (StringUtils.isBlank(memberName)) {
          continue;
        }
        JamfMembership membership = new JamfMembership();
        membership.setId(GrouperUuid.getUuid());
        membership.setGroupId(groupId);
        membership.setAccountName(memberName);
        HibernateSession.byObjectStatic().save(membership);
      }
    }

    created(mockServiceResponse, "<user_group><id>" + group.getId() + "</id></user_group>");
  }

  /**
   * DELETE /JSSResource/accounts/userid/{id} -- delete an account (test cleanup).
   */
  private void deleteAccount(MockServiceResponse mockServiceResponse, String accountId) {
    JamfAccount account = findAccount("id", accountId);
    if (account == null) {
      notFound(mockServiceResponse);
      return;
    }
    HibernateSession.byObjectStatic().delete(account);
    ok(mockServiceResponse, "<account><id>" + accountId + "</id></account>");
  }

  /**
   * DELETE /JSSResource/accounts/groupid/{id} -- delete a role and its memberships (test cleanup).
   */
  private void deleteAccountGroup(MockServiceResponse mockServiceResponse, String groupId) {
    JamfAccountGroup group = HibernateSession.byHqlStatic()
        .createQuery("from JamfAccountGroup where id = :theId")
        .setString("theId", groupId).uniqueResult(JamfAccountGroup.class);
    if (group == null) {
      notFound(mockServiceResponse);
      return;
    }
    List<JamfMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from JamfMembership where groupId = :theGroupId")
        .setString("theGroupId", groupId).list(JamfMembership.class);
    for (JamfMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }
    HibernateSession.byObjectStatic().delete(group);
    ok(mockServiceResponse, "<user_group><id>" + groupId + "</id></user_group>");
  }

  // ==================== helpers ====================

  private JamfAccount findAccount(String field, String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    if ("name".equals(field)) {
      // account names are case-insensitive in matching; stored lowercase
      return HibernateSession.byHqlStatic()
          .createQuery("from JamfAccount where lower(name) = :theName")
          .setString("theName", value.toLowerCase()).uniqueResult(JamfAccount.class);
    }
    return HibernateSession.byHqlStatic()
        .createQuery("from JamfAccount where id = :theId")
        .setString("theId", value).uniqueResult(JamfAccount.class);
  }

  private Element parseBody(MockServiceRequest mockServiceRequest) {
    String body = mockServiceRequest.getRequestBody();
    if (StringUtils.isBlank(body)) {
      throw new RuntimeException("Request body is required");
    }
    try {
      Document document = DocumentHelper.parseText(body);
      return document.getRootElement();
    } catch (Exception e) {
      throw new RuntimeException("Error parsing request XML: " + StringUtils.abbreviate(body, 1000), e);
    }
  }

  private static void setIfPresent(Element root, String tag, java.util.function.Consumer<String> setter) {
    if (root.element(tag) != null) {
      setter.accept(root.elementTextTrim(tag));
    }
  }

  private static void appendEl(StringBuilder sb, String tag, String value) {
    sb.append('<').append(tag).append('>')
      .append(value == null ? "" : GrouperUtil.xmlEscape(value))
      .append("</").append(tag).append('>');
  }

  private static String newNumericId() {
    return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999999));
  }

  private static void ok(MockServiceResponse mockServiceResponse, String xml) {
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/xml");
    mockServiceResponse.setResponseBody(xml);
  }

  private static void created(MockServiceResponse mockServiceResponse, String xml) {
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/xml");
    mockServiceResponse.setResponseBody(xml);
  }

  private static void notFound(MockServiceResponse mockServiceResponse) {
    mockServiceResponse.setResponseCode(404);
    mockServiceResponse.setContentType("application/xml");
    mockServiceResponse.setResponseBody("<error>not found</error>");
  }

  private static void conflict(MockServiceResponse mockServiceResponse, String message) {
    mockServiceResponse.setResponseCode(409);
    mockServiceResponse.setContentType("application/xml");
    mockServiceResponse.setResponseBody("<error>" + GrouperUtil.xmlEscape(message) + "</error>");
  }

}
