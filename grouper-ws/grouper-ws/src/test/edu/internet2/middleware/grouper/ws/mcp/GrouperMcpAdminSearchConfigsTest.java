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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpConfigSearchIndex;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpAdminSearchConfigs (admin_config_search MCP tool).
 * includes tests to verify that sensitive values (passwords, secrets, private keys)
 * are masked and never returned in cleartext.
 *
 * @author mchyzer
 */
public class GrouperMcpAdminSearchConfigsTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpAdminSearchConfigsTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpAdminSearchConfigsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpAdminSearchConfigsTest("testPasswordsAreMasked"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * build a GrouperMcpAuthUser for the root session (GrouperSystem)
   * @return the auth user
   */
  private GrouperMcpAuthUser buildRootAuthUser() {
    GrouperSession.startRootSession();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(
        SubjectFinder.findRootSubject());
    return authUser;
  }

  /**
   * test basic config search returns results (regex mode)
   */
  public void testSearchConfigsBasic() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", ".*mcp.*");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected at least 1 matching config",
          responseNode.get("matchCount").asInt() >= 1);
      JsonNode configs = responseNode.get("configs");
      assertNotNull(configs);
      assertTrue(configs.isArray());
      assertTrue(configs.size() >= 1);

      // verify each entry has key, value, configFile
      for (int i = 0; i < configs.size(); i++) {
        assertNotNull(configs.get(i).get("key"));
        assertNotNull(configs.get(i).get("value"));
        assertNotNull(configs.get(i).get("configFile"));
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that password configs are masked and never returned in cleartext.
   * searches for configs with "pass" in the key name (which includes
   * hibernate.connection.password) and verifies any with values are masked.
   */
  public void testPasswordsAreMasked() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    // set a password config so we know there's a value to mask
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "some.test.password.config", "superSecretPassword123");

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", ".*pass.*");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");
      assertNotNull(configs);

      // verify no config value contains the actual password
      for (int i = 0; i < configs.size(); i++) {
        JsonNode entry = configs.get(i);
        String value = entry.get("value").asText();
        assertFalse("Password value should not contain 'superSecretPassword123' for key: "
            + entry.get("key").asText(),
            value.contains("superSecretPassword123"));

        // if the entry has a non-empty value and the key contains "pass",
        // the value should be the escaped password marker
        if (entry.has("sensitive") && entry.get("sensitive").asBoolean()) {
          assertEquals("Sensitive value should be masked for key: "
              + entry.get("key").asText(),
              GrouperConfigHibernate.ESCAPED_PASSWORD, value);
        }
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that secret configs are masked
   */
  public void testSecretsAreMasked() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "some.test.secret.config", "myTopSecret456");

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "some\\.test\\.secret\\.config");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");
      assertTrue("Expected at least 1 config", configs.size() >= 1);

      boolean foundSecret = false;
      for (int i = 0; i < configs.size(); i++) {
        JsonNode entry = configs.get(i);
        if ("some.test.secret.config".equals(entry.get("key").asText())) {
          foundSecret = true;
          String value = entry.get("value").asText();
          assertFalse("Secret value should not be in cleartext",
              value.contains("myTopSecret456"));
          assertEquals("Secret should be masked",
              GrouperConfigHibernate.ESCAPED_PASSWORD, value);
          assertTrue("Should be marked sensitive",
              entry.has("sensitive") && entry.get("sensitive").asBoolean());
        }
      }
      assertTrue("Expected to find the secret config entry", foundSecret);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that private key configs are masked
   */
  public void testPrivateKeysAreMasked() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "some.test.privateKey.config", "-----BEGIN PRIVATE KEY-----abc123");

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "some\\.test\\.privateKey\\.config");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");

      boolean foundKey = false;
      for (int i = 0; i < configs.size(); i++) {
        JsonNode entry = configs.get(i);
        if ("some.test.privateKey.config".equals(entry.get("key").asText())) {
          foundKey = true;
          String value = entry.get("value").asText();
          assertFalse("Private key value should not be in cleartext",
              value.contains("BEGIN PRIVATE KEY"));
          assertEquals("Private key should be masked",
              GrouperConfigHibernate.ESCAPED_PASSWORD, value);
        }
      }
      assertTrue("Expected to find the private key config entry", foundKey);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that non-sensitive configs are returned in cleartext
   */
  public void testNonSensitiveConfigsNotMasked() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "grouper\\.oauth\\.accessToken\\.expirationSeconds");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");
      assertTrue("Expected at least 1 config", configs.size() >= 1);

      boolean found = false;
      for (int i = 0; i < configs.size(); i++) {
        JsonNode entry = configs.get(i);
        if ("grouper.oauth.accessToken.expirationSeconds".equals(entry.get("key").asText())) {
          found = true;
          String value = entry.get("value").asText();
          // should not be masked
          assertFalse("Non-sensitive value should not be masked",
              GrouperConfigHibernate.ESCAPED_PASSWORD.equals(value));
          // should have the actual value
          assertEquals("14400", value);
        }
      }
      assertTrue("Expected to find the config entry", found);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test filtering by specific config file
   */
  public void testSearchConfigsByFile() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", ".*loader.*");
    arguments.put("searchType", "regex");
    arguments.put("configFile", "grouper-loader.properties");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");

      // all results should be from the loader config file
      for (int i = 0; i < configs.size(); i++) {
        assertEquals("grouper-loader.properties",
            configs.get(i).get("configFile").asText());
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test with invalid config file name
   */
  public void testSearchConfigsInvalidConfigFile() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", ".*");
    arguments.put("searchType", "regex");
    arguments.put("configFile", "bogus.properties");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("Unknown config file"));
  }

  /**
   * test with invalid regex when searchType is regex
   */
  public void testSearchConfigsInvalidRegex() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "[invalid(regex");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("Invalid regex pattern"));
  }

  /**
   * test that missing searchRegex returns an error
   */
  public void testSearchConfigsMissingSearchRegex() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("searchRegex is required"));
  }

  /**
   * test with null arguments
   */
  public void testSearchConfigsNullArguments() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(null, authUser);

    assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("searchRegex is required"));
  }

  /**
   * test that no config value in any search result contains cleartext passwords.
   * does a broad search and scans all returned values to ensure none
   * contain obvious password patterns.
   */
  public void testBroadSearchNoPasswordLeakage() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    // set some password configs with known values
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "test.mcp.password.check1", "LeakyPassword1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "test.mcp.secret.check1", "LeakySecret1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "test.mcp.private.check1", "LeakyPrivateKey1");

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "test\\.mcp\\.");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();

    // the full response text should never contain the cleartext passwords
    assertFalse("Response should not contain LeakyPassword1",
        text.contains("LeakyPassword1"));
    assertFalse("Response should not contain LeakySecret1",
        text.contains("LeakySecret1"));
    assertFalse("Response should not contain LeakyPrivateKey1",
        text.contains("LeakyPrivateKey1"));
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpAdminSearchConfigs.toolDefinition();

    assertEquals("admin_config_search", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("searchRegex"));
    assertNotNull(properties.get("configFile"));
    assertNotNull(properties.get("searchType"));

    // verify searchType enum values
    JsonNode searchTypeEnum = properties.get("searchType").get("enum");
    assertNotNull(searchTypeEnum);
    assertEquals(2, searchTypeEnum.size());
    assertEquals("lucene", searchTypeEnum.get(0).asText());
    assertEquals("regex", searchTypeEnum.get(1).asText());

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("searchRegex", required.get(0).asText());
  }

  /**
   * test lucene search returns results
   */
  public void testLuceneSearchBasic() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    // force rebuild so the index is fresh
    GrouperMcpConfigSearchIndex.forceRebuild();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "mcp");
    arguments.put("searchType", "lucene");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("lucene", responseNode.get("searchType").asText());
      assertTrue("Expected at least 1 matching config",
          responseNode.get("matchCount").asInt() >= 1);
      JsonNode configs = responseNode.get("configs");
      assertNotNull(configs);
      assertTrue(configs.isArray());
      assertTrue(configs.size() >= 1);

      // verify each entry has key, value, configFile
      for (int i = 0; i < configs.size(); i++) {
        assertNotNull(configs.get(i).get("key"));
        assertNotNull(configs.get(i).get("value"));
        assertNotNull(configs.get(i).get("configFile"));
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test lucene search masks passwords
   */
  public void testLucenePasswordsAreMasked() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "some.test.password.lucene", "superSecretLucene123");

    GrouperMcpConfigSearchIndex.forceRebuild();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "some test password lucene");
    arguments.put("searchType", "lucene");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    assertFalse("Response should not contain cleartext password",
        text.contains("superSecretLucene123"));
  }

  /**
   * test lucene search with configFile filter
   */
  public void testLuceneSearchWithConfigFileFilter() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperMcpConfigSearchIndex.forceRebuild();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "loader");
    arguments.put("searchType", "lucene");
    arguments.put("configFile", "grouper-loader.properties");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");

      // all results should be from the loader config file
      for (int i = 0; i < configs.size(); i++) {
        assertEquals("grouper-loader.properties",
            configs.get(i).get("configFile").asText());
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test lucene search includes metadata fields (configuredIn, defaultValue, etc.)
   */
  public void testLuceneSearchMetadata() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperMcpConfigSearchIndex.forceRebuild();

    ObjectNode arguments = objectMapper.createObjectNode();
    // search for a well-known config that should have metadata
    arguments.put("searchRegex", "grouper mcp tools allow");
    arguments.put("searchType", "lucene");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      JsonNode configs = responseNode.get("configs");
      assertTrue("Expected at least 1 config", configs.size() >= 1);

      // find the grouper.mcp.tools.allow entry
      boolean found = false;
      for (int i = 0; i < configs.size(); i++) {
        JsonNode entry = configs.get(i);
        if ("grouper.mcp.tools.allow".equals(entry.get("key").asText())) {
          found = true;
          // should have configuredIn
          assertNotNull("Expected configuredIn", entry.get("configuredIn"));
          break;
        }
      }
      assertTrue("Expected to find grouper.mcp.tools.allow config", found);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that regex searchType still works (backward compatibility)
   */
  public void testRegexSearchType() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", ".*mcp.*");
    arguments.put("searchType", "regex");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("regex", responseNode.get("searchType").asText());
      assertTrue("Expected at least 1 matching config",
          responseNode.get("matchCount").asInt() >= 1);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that default searchType is lucene (not regex)
   */
  public void testDefaultSearchTypeIsLucene() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    GrouperMcpConfigSearchIndex.forceRebuild();

    // no searchType specified - should default to lucene
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchRegex", "mcp");

    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("lucene", responseNode.get("searchType").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }
}
