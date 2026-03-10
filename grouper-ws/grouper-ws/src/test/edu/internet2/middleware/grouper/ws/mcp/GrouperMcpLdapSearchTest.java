/*******************************************************************************
 * Copyright 2024 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.io.File;
import java.lang.reflect.Field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpLdapSearch (ldap MCP tool).
 * requires Docker to run the OpenLDAP container with seed data
 * (same setup as LDAP provisioning tests).
 *
 * @author mchyzer
 */
public class GrouperMcpLdapSearchTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpLdapSearchTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpLdapSearchTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpLdapSearchTest("testFilterByUid"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** grouper session */
  private GrouperSession grouperSession = null;

  /** saved grouperHome to restore in tearDown */
  private String savedGrouperHome = null;

  /**
   * LdapProvisionerTestUtils.startLdapContainer() uses GrouperUtil.getGrouperHome()
   * to locate grouper-misc/openldap-dinkel-grouper.  When running from the grouper-ws
   * module the working directory is grouper-ws/grouper-ws so the relative path
   * "../grouper-misc" does not resolve correctly.  Fix this by temporarily pointing
   * grouperHome at the "grouper" module directory (two levels up from grouper-ws/grouper-ws).
   */
  private void fixGrouperHomeForLdapContainer() {
    try {
      Field field = GrouperUtil.class.getDeclaredField("grouperHome");
      field.setAccessible(true);
      this.savedGrouperHome = (String) field.get(null);

      // from grouper-ws/grouper-ws go up to repo root, then into grouper/
      String grouperModuleDir = new File(this.savedGrouperHome)
          .getParentFile().getParentFile().getAbsolutePath()
          + File.separator + "grouper";
      field.set(null, grouperModuleDir);
    } catch (Exception e) {
      throw new RuntimeException("Could not set grouperHome for LDAP container", e);
    }
  }

  /**
   * restore the original grouperHome value
   */
  private void restoreGrouperHome() {
    if (this.savedGrouperHome != null) {
      try {
        Field field = GrouperUtil.class.getDeclaredField("grouperHome");
        field.setAccessible(true);
        field.set(null, this.savedGrouperHome);
      } catch (Exception e) {
        throw new RuntimeException("Could not restore grouperHome", e);
      }
    }
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    try {
      this.grouperSession = GrouperSession.startRootSession();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // fix the grouperHome path so LdapProvisionerTestUtils can find
    // grouper-misc/openldap-dinkel-grouper from the grouper-ws module
    fixGrouperHomeForLdapContainer();

    // start the LDAP Docker container with seed data (same as provisioning tests)
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();

    // restore grouperHome now that container is started
    restoreGrouperHome();

    // configure the MCP LDAP external system so the tool recognizes personLdap
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.ldap.personLdap.baseDn", "dc=example,dc=edu");
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    restoreGrouperHome();
    GrouperSession.stopQuietly(this.grouperSession);
    GrouperContext.deleteDefaultContext();
    super.tearDown();
  }

  // ========================================================================
  // tool definition tests
  // ========================================================================

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpLdapSearch.toolDefinition();

    assertEquals("ldap", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("action"));
    assertNotNull(properties.get("externalSystemId"));
    assertNotNull(properties.get("baseDn"));
    assertNotNull(properties.get("searchScope"));
    assertNotNull(properties.get("filter"));
    assertNotNull(properties.get("attributes"));

    // action should have an enum with 2 values
    JsonNode actionEnum = properties.get("action").get("enum");
    assertNotNull(actionEnum);
    assertTrue(actionEnum.isArray());
    assertEquals(2, actionEnum.size());

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("action", required.get(0).asText());
  }

  // ========================================================================
  // error/validation tests
  // ========================================================================

  /**
   * test missing action returns an error
   */
  public void testMissingAction() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for missing action", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("action is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test null arguments returns an error
   */
  public void testNullArguments() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode result = GrouperMcpLdapSearch.execute(null, authUser);

      assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("action is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test unknown action returns an error
   */
  public void testUnknownAction() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "bogusAction");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for unknown action", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Unknown action"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter action without externalSystemId returns an error
   */
  public void testFilterMissingExternalSystemId() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("filter", "(uid=jsmith)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for missing externalSystemId",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("externalSystemId is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter action without filter returns an error
   */
  public void testFilterMissingFilter() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for missing filter", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("filter is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter action with unconfigured external system returns an error
   */
  public void testFilterUnconfiguredExternalSystem() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "bogusLdap99999");
      arguments.put("filter", "(uid=jsmith)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for unconfigured external system",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not configured"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // listExternalSystems tests
  // ========================================================================

  /**
   * test listExternalSystems returns the configured personLdap system
   */
  public void testListExternalSystems() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listExternalSystems");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode systems = responseNode.get("externalSystems");
        assertNotNull(systems);
        assertTrue(systems.isArray());
        assertTrue("Should have at least 1 system", systems.size() >= 1);

        // find personLdap in the list
        boolean foundPersonLdap = false;
        for (int i = 0; i < systems.size(); i++) {
          if ("personLdap".equals(systems.get(i).get("id").asText())) {
            foundPersonLdap = true;
            assertEquals("dc=example,dc=edu", systems.get(i).get("baseDn").asText());
          }
        }
        assertTrue("Should find personLdap in external systems", foundPersonLdap);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // filter tests (against live LDAP with seed data)
  // ========================================================================

  /**
   * test searching for a single user by uid (jsmith from seed data)
   */
  public void testFilterByUid() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("filter", "(uid=jsmith)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("entryCount").asInt());
        assertFalse(responseNode.get("truncated").asBoolean());

        JsonNode entries = responseNode.get("entries");
        assertNotNull(entries);
        assertTrue(entries.isArray());
        assertEquals(1, entries.size());

        // check the DN
        String dn = entries.get(0).get("dn").asText();
        assertTrue("DN should contain jsmith",
            dn.toLowerCase().contains("uid=jsmith"));

        // check attributes
        JsonNode attrs = entries.get(0).get("attributes");
        assertNotNull(attrs);
        // uid should be jsmith
        assertNotNull(attrs.get("uid"));
        assertEquals("jsmith", attrs.get("uid").get(0).asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test searching for multiple users by objectClass
   */
  public void testFilterMultipleResults() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("baseDn", "ou=People,dc=example,dc=edu");
      arguments.put("filter", "(objectClass=person)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        int entryCount = responseNode.get("entryCount").asInt();
        assertTrue("Should have multiple person entries", entryCount > 1);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test searching with specific attributes to return
   */
  public void testFilterWithSpecificAttributes() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("filter", "(uid=banderson)");

      ArrayNode attrs = objectMapper.createArrayNode();
      attrs.add("uid");
      attrs.add("cn");
      attrs.add("sn");
      arguments.set("attributes", attrs);

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("entryCount").asInt());

        JsonNode entryAttrs = responseNode.get("entries").get(0).get("attributes");
        assertNotNull(entryAttrs);
        // should have uid, cn, sn
        assertNotNull("Should have uid", entryAttrs.get("uid"));
        assertNotNull("Should have cn", entryAttrs.get("cn"));
        assertNotNull("Should have sn", entryAttrs.get("sn"));
        assertEquals("banderson", entryAttrs.get("uid").get(0).asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter that returns no results
   */
  public void testFilterNoResults() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("filter", "(uid=bogusNonExistentUser99999)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(0, responseNode.get("entryCount").asInt());
        assertFalse(responseNode.get("truncated").asBoolean());
        assertEquals(0, responseNode.get("entries").size());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter with ONELEVEL_SCOPE search scope
   */
  public void testFilterOneLevelScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("baseDn", "ou=People,dc=example,dc=edu");
      arguments.put("searchScope", "ONELEVEL_SCOPE");
      arguments.put("filter", "(uid=jsmith)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("entryCount").asInt());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter with compound LDAP filter (AND)
   */
  public void testFilterCompoundFilter() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("baseDn", "ou=People,dc=example,dc=edu");
      arguments.put("filter", "(&(objectClass=person)(sn=Smith))");

      ArrayNode attrs = objectMapper.createArrayNode();
      attrs.add("uid");
      attrs.add("cn");
      attrs.add("sn");
      arguments.set("attributes", attrs);

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertTrue("Should find at least one Smith",
            responseNode.get("entryCount").asInt() >= 1);

        // all results should have sn=Smith
        JsonNode entries = responseNode.get("entries");
        for (int i = 0; i < entries.size(); i++) {
          JsonNode sn = entries.get(i).get("attributes").get("sn");
          assertNotNull("Each entry should have sn", sn);
          assertEquals("Smith", sn.get(0).asText());
        }
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter with custom baseDn overriding the configured default
   */
  public void testFilterCustomBaseDn() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("baseDn", "ou=People,dc=example,dc=edu");
      arguments.put("filter", "(uid=kwhite)");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals(1, responseNode.get("entryCount").asInt());
        String dn = responseNode.get("entries").get(0).get("dn").asText();
        assertTrue("DN should be under ou=People", dn.contains("ou=People"));
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test filter with invalid searchScope returns an error
   */
  public void testFilterInvalidSearchScope() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "filter");
      arguments.put("externalSystemId", "personLdap");
      arguments.put("filter", "(uid=jsmith)");
      arguments.put("searchScope", "INVALID_SCOPE");

      ObjectNode result = GrouperMcpLdapSearch.execute(arguments, authUser);

      assertTrue("Expected error for invalid searchScope",
          result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("Invalid searchScope"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ========================================================================
  // validateExternalSystemAllowed direct method tests
  // ========================================================================

  /**
   * test validateExternalSystemAllowed directly
   */
  public void testValidateExternalSystemAllowed() {

    // blank should be error
    String error = GrouperMcpLdapSearch.validateExternalSystemAllowed(null);
    assertNotNull(error);
    assertTrue(error.contains("externalSystemId is required"));

    error = GrouperMcpLdapSearch.validateExternalSystemAllowed("");
    assertNotNull(error);
    assertTrue(error.contains("externalSystemId is required"));

    // configured system should be allowed
    assertNull("personLdap should be allowed",
        GrouperMcpLdapSearch.validateExternalSystemAllowed("personLdap"));

    // unconfigured system should be denied
    error = GrouperMcpLdapSearch.validateExternalSystemAllowed("bogusLdap99999");
    assertNotNull(error);
    assertTrue(error.contains("not configured"));
  }
}
