package edu.internet2.middleware.grouper.app.jamf;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Static wrappers around the Jamf Pro Classic API accounts endpoints. All calls speak XML
 * (the Classic API is XML-native; its JSON rendering is inconsistent). Authentication is OAuth
 * client-credentials, handled by the WsBearerToken external system -- the provisioner never sees
 * the bearer token directly.
 *
 * <p>Endpoints used:</p>
 * <ul>
 *   <li>GET  /JSSResource/accounts                    -- list accounts + account groups (id/name)</li>
 *   <li>GET  /JSSResource/accounts/username/{name}    -- find one account by EPPN</li>
 *   <li>GET  /JSSResource/accounts/groupid/{id}       -- one role with its members</li>
 *   <li>POST /JSSResource/accounts/userid/0           -- create an account</li>
 *   <li>PUT  /JSSResource/accounts/groupid/{id}       -- replace a role's member list</li>
 * </ul>
 */
public class JamfApiCommands {

  /** never log the Authorization header or the OAuth client secret */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /** never log the client secret in token-request parameters */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /** character set for generated account passwords (Classic API requires a password on create) */
  private static final String PASSWORD_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_!@#$%";

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Execute an HTTP method against the Jamf Classic API and return the raw response body.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for provisioner call stats
   * @param httpMethodName GET, POST, PUT, DELETE
   * @param configId the WsBearerToken external system config id
   * @param urlSuffix path after the base URL (e.g. /JSSResource/accounts)
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array to receive the actual status code
   * @param xmlBody the XML request body, or null for GET/DELETE
   * @return the raw XML response body, or null if blank
   */
  private static String executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String xmlBody) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders).assignDoNotLogParameters(doNotLogParameters);

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    // fetches/caches the OAuth client-credentials token and adds the Authorization: Bearer header
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(
        grouperHttpClient, configId, grouperLoaderConfig, debugMap);

    String url = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".endpoint");
    url = GrouperUtil.stripLastSlashIfExists(url);
    if (!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    debugMap.put("url", url);
    debugMap.put("method", httpMethodName);

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);

    // the Classic API returns XML by default; ask for it explicitly and send XML on writes
    grouperHttpClient.addHeader("Accept", "application/xml");
    if (StringUtils.isNotBlank(xmlBody)) {
      grouperHttpClient.assignBody(xmlBody);
      grouperHttpClient.addHeader("Content-Type", "application/xml");
    }

    long httpCallStartMillis = System.currentTimeMillis();
    try {
      grouperHttpClient.executeRequest();
    } finally {
      GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
          System.currentTimeMillis() - httpCallStartMillis);
    }

    int code;
    String body;
    try {
      code = grouperHttpClient.getResponseCode();
      returnCode[0] = code;
      body = grouperHttpClient.getResponseBody();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException("Invalid return code '" + code + "', expecting: "
          + GrouperUtil.setToString(allowedReturnCodes) + ". '" + debugMap.get("url") + "' " + body);
    }

    return StringUtils.isBlank(body) ? null : body;
  }

  /**
   * Parse an XML string into a dom4j Document.
   * @param xml the XML text
   * @return the parsed Document, or null if xml is blank
   */
  private static Document parseXml(String xml) {
    if (StringUtils.isBlank(xml)) {
      return null;
    }
    try {
      return DocumentHelper.parseText(xml);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing Jamf XML response: '" + StringUtils.abbreviate(xml, 2000) + "'", e);
    }
  }

  /**
   * Retrieve all account groups (roles) from GET /JSSResource/accounts. The list carries only
   * id, name, and site -- member lists require a per-group {@link #retrieveAccountGroup} call.
   * @param configId the external system config id
   * @param ignoreRoleNames role names to exclude
   * @return the list of roles (never null)
   */
  public static List<JamfAccountGroup> retrieveAccountGroups(String configId, Set<String> ignoreRoleNames) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAccountGroups");
    long startNanos = System.nanoTime();

    List<JamfAccountGroup> result = new ArrayList<JamfAccountGroup>();
    try {
      String xml = executeMethod(debugMap, "retrieveAccountGroups", "GET", configId,
          "/JSSResource/accounts", GrouperUtil.toSet(200), new int[] {-1}, null);

      Document document = parseXml(xml);
      if (document != null) {
        Element groupsElement = document.getRootElement().element("groups");
        if (groupsElement != null) {
          for (Object obj : groupsElement.elements("group")) {
            Element groupElement = (Element) obj;
            JamfAccountGroup group = new JamfAccountGroup();
            group.setId(groupElement.elementTextTrim("id"));
            group.setName(groupElement.elementTextTrim("name"));
            Element siteElement = groupElement.element("site");
            if (siteElement != null) {
              group.setSiteId(siteElement.elementTextTrim("id"));
              group.setSiteName(siteElement.elementTextTrim("name"));
            }
            if (!isIgnored(group.getName(), ignoreRoleNames)) {
              result.add(group);
            }
          }
        }
      }
      debugMap.put("groupCount", result.size());
      return result;
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve all accounts (id + name only) from GET /JSSResource/accounts.
   * @param configId the external system config id
   * @param ignoreAccountNames account names (EPPNs) to exclude
   * @return the list of accounts (never null)
   */
  public static List<JamfAccount> retrieveAccounts(String configId, Set<String> ignoreAccountNames) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAccounts");
    long startNanos = System.nanoTime();

    List<JamfAccount> result = new ArrayList<JamfAccount>();
    try {
      String xml = executeMethod(debugMap, "retrieveAccounts", "GET", configId,
          "/JSSResource/accounts", GrouperUtil.toSet(200), new int[] {-1}, null);

      Document document = parseXml(xml);
      if (document != null) {
        Element usersElement = document.getRootElement().element("users");
        if (usersElement != null) {
          for (Object obj : usersElement.elements("user")) {
            Element userElement = (Element) obj;
            JamfAccount account = new JamfAccount();
            account.setId(userElement.elementTextTrim("id"));
            account.setName(userElement.elementTextTrim("name"));
            if (!isIgnored(account.getName(), ignoreAccountNames)) {
              result.add(account);
            }
          }
        }
      }
      debugMap.put("accountCount", result.size());
      return result;
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve a single account by name (EPPN) from GET /JSSResource/accounts/username/{name}.
   * The name is used as-is (same value the account was created with, so create and lookup stay
   * consistent). In production the name is the EPPN (subjectIdentifier2), which is already lowercase.
   * @param configId the external system config id
   * @param name the account name / EPPN
   * @return the account, or null if not found (404)
   */
  public static JamfAccount retrieveAccountByName(String configId, String name) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAccountByName");
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      String encodedName = GrouperUtil.escapeUrlEncode(name);
      int[] returnCode = new int[] {-1};
      String xml = executeMethod(debugMap, "retrieveAccountByName", "GET", configId,
          "/JSSResource/accounts/username/" + encodedName, GrouperUtil.toSet(200, 404), returnCode, null);

      if (returnCode[0] == 404) {
        return null;
      }
      Document document = parseXml(xml);
      if (document == null) {
        return null;
      }
      JamfAccount account = accountFromElement(document.getRootElement());
      return account;
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve a single account group (role) with its members from
   * GET /JSSResource/accounts/groupid/{id}.
   * @param configId the external system config id
   * @param groupId the native role id
   * @return the role with {@link JamfAccountGroup#getMembers()} populated, or null if not found
   */
  public static JamfAccountGroup retrieveAccountGroup(String configId, String groupId) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAccountGroup");
    long startNanos = System.nanoTime();

    try {
      int[] returnCode = new int[] {-1};
      String xml = executeMethod(debugMap, "retrieveAccountGroup", "GET", configId,
          "/JSSResource/accounts/groupid/" + GrouperUtil.escapeUrlEncode(groupId),
          GrouperUtil.toSet(200, 404), returnCode, null);

      if (returnCode[0] == 404) {
        return null;
      }
      Document document = parseXml(xml);
      if (document == null) {
        return null;
      }
      Element groupElement = document.getRootElement();
      JamfAccountGroup group = new JamfAccountGroup();
      group.setId(groupElement.elementTextTrim("id"));
      group.setName(groupElement.elementTextTrim("name"));
      group.setAccessLevel(groupElement.elementTextTrim("access_level"));
      group.setPrivilegeSet(groupElement.elementTextTrim("privilege_set"));
      Element siteElement = groupElement.element("site");
      if (siteElement != null) {
        group.setSiteId(siteElement.elementTextTrim("id"));
        group.setSiteName(siteElement.elementTextTrim("name"));
      }
      List<String> members = new ArrayList<String>();
      Element membersElement = groupElement.element("members");
      if (membersElement != null) {
        for (Object obj : membersElement.elements("user")) {
          String memberName = ((Element) obj).elementTextTrim("name");
          if (!StringUtils.isBlank(memberName)) {
            members.add(memberName);
          }
        }
      }
      group.setMembers(members);
      debugMap.put("memberCount", members.size());
      return group;
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Create an account via POST /JSSResource/accounts/userid/0. A random password is generated
   * (the Classic API requires one even though console login is via SSO). The account is created
   * with the access level supplied on the JamfAccount (Grouper uses "Group Access", so privileges
   * come from role membership, not the account itself).
   * @param configId the external system config id
   * @param account the account to create (name = lowercased EPPN)
   * @return the account with its new native id set
   */
  public static JamfAccount createAccount(String configId, JamfAccount account) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "createAccount");
    debugMap.put("name", account.getName());
    long startNanos = System.nanoTime();

    try {
      String xmlBody = buildCreateAccountXml(account);
      int[] returnCode = new int[] {-1};
      String xml = executeMethod(debugMap, "createAccount", "POST", configId,
          "/JSSResource/accounts/userid/0", GrouperUtil.toSet(200, 201), returnCode, xmlBody);

      Document document = parseXml(xml);
      if (document != null) {
        // create response is <account><id>NNN</id></account>
        String newId = document.getRootElement().elementTextTrim("id");
        account.setId(newId);
        debugMap.put("newId", newId);
      }
      return account;
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Delete an account via DELETE /JSSResource/accounts/userid/{id}. A 404 (already gone) is treated
   * as success. Grouper only calls this for accounts it manages (delete is gated by the provisioner
   * config so pre-existing / unmanaged accounts are never touched).
   * @param configId the external system config id
   * @param accountId the native Jamf account id
   */
  public static void deleteAccount(String configId, String accountId) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteAccount");
    debugMap.put("accountId", accountId);
    long startNanos = System.nanoTime();
    try {
      if (StringUtils.isBlank(accountId)) {
        throw new RuntimeException("account id is required for deleteAccount");
      }
      int[] returnCode = new int[] {-1};
      executeMethod(debugMap, "deleteAccount", "DELETE", configId,
          "/JSSResource/accounts/userid/" + GrouperUtil.escapeUrlEncode(accountId),
          GrouperUtil.toSet(200, 201, 404), returnCode, null);
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Replace an account group's entire member list via PUT /JSSResource/accounts/groupid/{id}.
   * The Classic API has no atomic add/remove for account groups, so the caller supplies the full
   * desired member list (the DAO computes it via retrieve-modify-write).
   *
   * <p>Only <code>name</code> and <code>members</code> are sent -- deliberately NOT
   * <code>privilege_set</code> or <code>privileges</code> -- so a membership change can never
   * rewrite (or wipe) a role's privilege definition. This relies on the Classic API treating an
   * omitted <code>privileges</code> block as "leave privileges unchanged". VERIFY this against a
   * Custom role before enabling in production.</p>
   *
   * @param configId the external system config id
   * @param groupId the native role id
   * @param groupName the role name (restated to avoid a 409 on partial update)
   * @param memberNames the complete desired member list (account names / EPPNs)
   */
  public static void replaceAccountGroupMembers(String configId, String groupId, String groupName,
      List<String> memberNames) {
    Map<String, Object> debugMap = new java.util.LinkedHashMap<String, Object>();
    debugMap.put("method", "replaceAccountGroupMembers");
    debugMap.put("groupId", groupId);
    debugMap.put("memberCount", memberNames == null ? 0 : memberNames.size());
    long startNanos = System.nanoTime();

    try {
      String xmlBody = buildGroupMembersXml(groupName, memberNames);
      int[] returnCode = new int[] {-1};
      executeMethod(debugMap, "replaceAccountGroupMembers", "PUT", configId,
          "/JSSResource/accounts/groupid/" + GrouperUtil.escapeUrlEncode(groupId),
          GrouperUtil.toSet(200, 201), returnCode, xmlBody);
    } finally {
      JamfLog.jamfLog(debugMap, startNanos);
    }
  }

  /**
   * Build a JamfAccount from a Jamf {@code <account>} element (from a detail GET).
   * @param accountElement the {@code <account>} element
   * @return the JamfAccount
   */
  private static JamfAccount accountFromElement(Element accountElement) {
    JamfAccount account = new JamfAccount();
    account.setId(accountElement.elementTextTrim("id"));
    account.setName(accountElement.elementTextTrim("name"));
    account.setFullName(accountElement.elementTextTrim("full_name"));
    account.setEmail(accountElement.elementTextTrim("email"));
    account.setAccessLevel(accountElement.elementTextTrim("access_level"));
    account.setPrivilegeSet(accountElement.elementTextTrim("privilege_set"));
    account.setEnabled(accountElement.elementTextTrim("enabled"));
    String directoryUser = accountElement.elementTextTrim("directory_user");
    if (!StringUtils.isBlank(directoryUser)) {
      account.setDirectoryUser(GrouperUtil.booleanObjectValue(directoryUser));
    }
    return account;
  }

  /**
   * Build the XML body for an account create.
   * @param account the account (name required)
   * @return the {@code <account>...</account>} XML
   */
  private static String buildCreateAccountXml(JamfAccount account) {
    StringBuilder sb = new StringBuilder();
    sb.append("<account>");
    appendElement(sb, "name", account.getName());
    appendElement(sb, "full_name", account.getFullName());
    appendElement(sb, "email", account.getEmail());
    appendElement(sb, "email_address", account.getEmail());
    appendElement(sb, "password", generatePassword());
    appendElement(sb, "access_level",
        GrouperUtil.defaultIfBlank(account.getAccessLevel(), JamfAccount.ACCESS_LEVEL_GROUP_ACCESS));
    appendElement(sb, "enabled", JamfAccount.ENABLED);
    sb.append("</account>");
    return sb.toString();
  }

  /**
   * Build the XML body for a role membership replace ({@code <group><name>..</name><members>..}).
   * @param groupName the role name
   * @param memberNames the complete member list (may be empty for "no members")
   * @return the {@code <group>...</group>} XML
   */
  private static String buildGroupMembersXml(String groupName, List<String> memberNames) {
    StringBuilder sb = new StringBuilder();
    sb.append("<group>");
    appendElement(sb, "name", groupName);
    sb.append("<members>");
    if (memberNames != null) {
      for (String memberName : memberNames) {
        if (!StringUtils.isBlank(memberName)) {
          sb.append("<user>");
          appendElement(sb, "name", memberName);
          sb.append("</user>");
        }
      }
    }
    sb.append("</members>");
    sb.append("</group>");
    return sb.toString();
  }

  /**
   * Append {@code <tag>escaped-value</tag>} if value is non-blank.
   */
  private static void appendElement(StringBuilder sb, String tag, String value) {
    if (value == null) {
      return;
    }
    sb.append('<').append(tag).append('>')
      .append(GrouperUtil.xmlEscape(value))
      .append("</").append(tag).append('>');
  }

  /**
   * Generate a random 40-character password for a new account.
   * @return the generated password
   */
  private static String generatePassword() {
    StringBuilder sb = new StringBuilder(40);
    for (int i = 0; i < 40; i++) {
      sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
    }
    return sb.toString();
  }

  /**
   * Parse a comma-separated ignore list into a lowercased set for case-insensitive matching.
   * @param commaSeparated the config value (may be null/blank)
   * @return the set of lowercased names (never null)
   */
  public static Set<String> parseIgnoreSet(String commaSeparated) {
    Set<String> result = new LinkedHashSet<String>();
    if (!StringUtils.isBlank(commaSeparated)) {
      for (String item : GrouperUtil.splitTrim(commaSeparated, ",")) {
        result.add(item.toLowerCase());
      }
    }
    return result;
  }

  /**
   * @param name the name to test
   * @param ignoreSet the lowercased ignore set (from {@link #parseIgnoreSet})
   * @return true if name is in the ignore set (case-insensitive)
   */
  public static boolean isIgnored(String name, Set<String> ignoreSet) {
    return name != null && ignoreSet != null && ignoreSet.contains(name.toLowerCase());
  }

}
