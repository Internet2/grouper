package edu.internet2.middleware.grouper.app.externalSystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperHttpClientSetupAuthorization;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class WsBearerTokenExternalSystemTest extends GrouperTest {

  private static final String CONFIG_ID = "testAdditionalHeaders";
  private static final String PREFIX = "grouper.wsBearerToken." + CONFIG_ID + ".";

  public WsBearerTokenExternalSystemTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(WsBearerTokenExternalSystemTest.class);
  }

  private GrouperLoaderConfig configureHttpClient() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    config.propertiesOverrideMap().put(PREFIX + "accessTokenPassword", "test-api-key");
    config.propertiesOverrideMap().put(PREFIX + "httpHeader", "x-api-key");
    config.propertiesOverrideMap().put(PREFIX + "prependBearerTokenPrefix", "false");
    return config;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> headers(GrouperHttpClient client) {
    return (Map<String, String>) GrouperUtil.fieldValue(client, "headers");
  }

  public void testAdditionalHeadersAndSecrets() {
    GrouperLoaderConfig config = configureHttpClient();
    config.propertiesOverrideMap().put(PREFIX + "numberOfAdditionalHeaders", "2");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.name", "anthropic-version");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.value", "2023-06-01");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.name", "X-Tenant-Secret");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.value", "tenant-secret");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.valueIsSecret", "true");

    GrouperHttpClient client = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID, config, null);

    assertEquals("test-api-key", headers(client).get("x-api-key"));
    assertEquals("2023-06-01", headers(client).get("anthropic-version"));
    assertEquals("tenant-secret", headers(client).get("X-Tenant-Secret"));
    assertTrue(client.getDoNotLogHeaders().contains("X-Tenant-Secret"));
    assertFalse(client.getDoNotLogHeaders().contains("anthropic-version"));
  }

  public void testNoAdditionalHeadersByDefault() {
    GrouperLoaderConfig config = configureHttpClient();
    GrouperHttpClient defaultClient = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(defaultClient, CONFIG_ID, config, null);
    assertEquals(1, headers(defaultClient).size());
    String[] counts = { "", "0" };
    for (String count : counts) {
      config.propertiesOverrideMap().put(PREFIX + "numberOfAdditionalHeaders", count);
      // Leftover settings must not be used when their count is blank or zero.
      config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.name", "anthropic-version");
      config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.value", "2023-06-01");
      GrouperHttpClient client = new GrouperHttpClient();
      WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID, config, null);
      assertEquals(1, headers(client).size());
      assertFalse(headers(client).containsKey("anthropic-version"));
    }
  }

  public void testCallerOverrideWinsIncludingRetries() {
    GrouperLoaderConfig config = configureHttpClient();
    config.propertiesOverrideMap().put(PREFIX + "numberOfAdditionalHeaders", "1");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.name", "anthropic-version");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.value", "2023-06-01");
    GrouperHttpClient client = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID, config, null);
    client.addHeader("Anthropic-Version", "caller-version");

    GrouperHttpClientSetupAuthorization callback = (GrouperHttpClientSetupAuthorization)
        GrouperUtil.fieldValue(client, "grouperHttpClientSetupAuthorization");
    callback.setupAuthorization(client);
    callback.setupAuthorization(client);
    assertEquals("caller-version", headers(client).get("anthropic-version"));
    assertEquals(2, headers(client).size());

    GrouperHttpClient callerFirst = new GrouperHttpClient();
    callerFirst.addHeader("ANTHROPIC-VERSION", "caller-first");
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(callerFirst, CONFIG_ID, config, null);
    assertEquals("caller-first", headers(callerFirst).get("anthropic-version"));
    assertEquals(2, headers(callerFirst).size());
  }

  public void testBasicAuthenticationAdditionalHeaders() {
    GrouperLoaderConfig config = configureHttpClient();
    config.propertiesOverrideMap().put(PREFIX + "httpAuthnType", "basicAuth");
    config.propertiesOverrideMap().put(PREFIX + "basicAuthUser", "test-user");
    config.propertiesOverrideMap().put(PREFIX + "numberOfAdditionalHeaders", "1");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.name", "Accept");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.value", "application/json");
    GrouperHttpClient client = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID, config, null);
    assertEquals("application/json", headers(client).get("accept"));
  }

  public void testHeadersSentOnceAndSecretLogsMaskedOnRetry() throws Exception {
    GrouperLoaderConfig config = configureHttpClient();
    config.propertiesOverrideMap().put(PREFIX + "numberOfAdditionalHeaders", "2");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.name", "anthropic-version");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.0.value", "2023-06-01");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.name", "X-Tenant-Secret");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.value", "hidden-tenant-value");
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.1.valueIsSecret", "true");
    final List<List<String>> versions = Collections.synchronizedList(new ArrayList<List<String>>());
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        versions.add(new ArrayList<String>(exchange.getRequestHeaders().get("anthropic-version")));
        // A response may echo the same secret header with different capitalization.
        exchange.getResponseHeaders().set("x-tenant-secret", "hidden-tenant-value");
        exchange.sendResponseHeaders(versions.size() == 1 ? 429 : 200, -1);
        exchange.close();
      }
    });
    server.start();
    GrouperHttpClientLog log = new GrouperHttpClientLog();
    GrouperHttpClient.logStart(log);
    try {
      GrouperHttpClient client = new GrouperHttpClient();
      client.assignUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/");
      client.assignGrouperHttpMethod("GET");
      client.assignTimeoutMillies(5000);
      client.setRetryForThrottlingOrNetworkIssues(1);
      client.setRetryForThrottlingOrNetworkIssuesSleepMillis(0);
      client.setRetryForThrottlingOrNetworkIssuesBackOffMillis(0);
      WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID, config, null);
      client.addHeader("Anthropic-Version", "caller-version");
      client.executeRequest();
      assertEquals(200, client.getResponseCode());
      assertEquals(2, versions.size());
      for (List<String> sentVersions : versions) {
        assertEquals(1, sentVersions.size());
        assertEquals("caller-version", sentVersions.get(0));
      }
      assertTrue(log.getLog().toString().contains("caller-version"));
      assertFalse(log.getLog().toString().contains("hidden-tenant-value"));
      assertFalse(log.getLog().toString().contains("test-api-key"));
    } finally {
      GrouperHttpClient.logEnd();
      server.stop(0);
    }
  }

  private WsBearerTokenExternalSystem externalSystem() {
    WsBearerTokenExternalSystem system = new WsBearerTokenExternalSystem();
    system.setConfigId(CONFIG_ID);
    Map<String, GrouperConfigurationModuleAttribute> attributes = system.retrieveAttributes();
    attributes.get("endpoint").setValue("https://api.example.edu/v1");
    attributes.get("accessTokenPassword").setValue("test-api-key");
    attributes.get("numberOfAdditionalHeaders").setValue("1");
    attributes.get("additionalHeader.0.name").setValue("anthropic-version");
    attributes.get("additionalHeader.0.value").setValue("2023-06-01");
    return system;
  }

  public void testValidateHeaderNames() {
    GrouperSession.startRootSession();
    WsBearerTokenExternalSystem system = externalSystem();
    Map<String, GrouperConfigurationModuleAttribute> attributes = system.retrieveAttributes();
    attributes.get("httpHeader").setValue("X-Api-Key");
    attributes.get("apiKeyHeaderName").setValue("X-Client-Key");
    String[] invalidNames = { "", " ", "bad header", "bad:header", "bad\r\nheader",
        "x-API-key", "X-CLIENT-KEY", "authorization" };
    for (String name : invalidNames) {
      attributes.get("additionalHeader.0.name").setValue(name);
      Map<String, String> validation = new HashMap<String, String>();
      system.validatePreSave(true, false, new ArrayList<String>(), validation);
      assertTrue("Should reject header: " + name,
          validation.containsKey(attributes.get("additionalHeader.0.name").getHtmlForElementIdHandle()));
    }

    attributes.get("httpHeader").setValue("");
    attributes.get("additionalHeader.0.name").setValue("AUTHORIZATION");
    Map<String, String> validation = new HashMap<String, String>();
    system.validatePreSave(true, false, new ArrayList<String>(), validation);
    assertTrue(validation.containsKey(attributes.get("additionalHeader.0.name").getHtmlForElementIdHandle()));

    attributes.get("additionalHeader.0.name").setValue("anthropic-version");
    validation.clear();
    system.validatePreSave(true, false, new ArrayList<String>(), validation);
    assertTrue(validation.toString(), validation.isEmpty());
  }

  public void testValidateCountAndHeaderValue() {
    GrouperSession.startRootSession();
    WsBearerTokenExternalSystem system = externalSystem();
    Map<String, GrouperConfigurationModuleAttribute> attributes = system.retrieveAttributes();
    attributes.get("numberOfAdditionalHeaders").setValue("6");
    Map<String, String> validation = new HashMap<String, String>();
    system.validatePreSave(true, false, new ArrayList<String>(), validation);
    assertTrue(validation.containsKey(attributes.get("numberOfAdditionalHeaders").getHtmlForElementIdHandle()));

    attributes.get("numberOfAdditionalHeaders").setValue("1");
    attributes.get("additionalHeader.0.value").setValue("value\r\nInjected: header");
    validation.clear();
    system.validatePreSave(true, false, new ArrayList<String>(), validation);
    assertTrue(validation.containsKey(attributes.get("additionalHeader.0.value").getHtmlForElementIdHandle()));
  }

  public void testSecretValueEncryptedAndMasked() {
    GrouperSession.startRootSession();
    WsBearerTokenExternalSystem system = externalSystem();
    Map<String, GrouperConfigurationModuleAttribute> attributes = system.retrieveAttributes();
    attributes.get("additionalHeader.0.valueIsSecret").setValue("true");
    attributes.get("additionalHeader.0.value").setValue("tenant-secret");
    GrouperConfigurationModuleAttribute value = system.retrieveAttributes().get("additionalHeader.0.value");
    assertTrue(value.isPassword());
    assertEquals(ConfigItemFormElement.PASSWORD, value.getFormElement());

    List<String> errors = new ArrayList<String>();
    Map<String, String> validation = new HashMap<String, String>();
    system.insertConfig(false, new StringBuilder(), errors, validation, new ArrayList<String>());
    assertTrue(errors.toString(), errors.isEmpty());
    assertTrue(validation.toString(), validation.isEmpty());
    Set<GrouperConfigHibernate> saved = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_LOADER_PROPERTIES, null, PREFIX + "additionalHeader.0.value");
    assertEquals(1, saved.size());
    GrouperConfigHibernate stored = saved.iterator().next();
    assertTrue(stored.isConfigEncrypted());
    assertFalse("tenant-secret".equals(stored.retrieveValue()));

    WsBearerTokenExternalSystem reloaded = new WsBearerTokenExternalSystem();
    reloaded.setConfigId(CONFIG_ID);
    value = reloaded.retrieveAttributes().get("additionalHeader.0.value");
    assertTrue(value.isPassword());
    assertEquals(GrouperConfigHibernate.ESCAPED_PASSWORD, value.getValueOrExpressionEvaluation());

    GrouperHttpClient client = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, CONFIG_ID);
    assertEquals("tenant-secret", headers(client).get("anthropic-version"));
    assertTrue(client.getDoNotLogHeaders().contains("anthropic-version"));
  }

  public void testHeaderCountDropdownOptions() {
    GrouperConfigurationModuleAttribute count = externalSystem().retrieveAttributes().get("numberOfAdditionalHeaders");
    assertEquals("0", count.getDefaultValue());
    assertEquals(6, count.getDropdownValuesAndLabels().size());
    assertEquals("", count.getDropdownValuesAndLabels().get(0).getKey(0));
    for (int i = 1; i <= 5; i++) {
      assertEquals(Integer.toString(i), count.getDropdownValuesAndLabels().get(i).getKey(0));
    }
  }

  public void testSecretFlagAndHeaderVisibility() {
    WsBearerTokenExternalSystem system = externalSystem();
    Map<String, GrouperConfigurationModuleAttribute> attributes = system.retrieveAttributes();
    assertTrue(attributes.get("additionalHeader.0.name").isShow());
    assertFalse(attributes.get("additionalHeader.1.name").isShow());
    assertFalse(attributes.get("additionalHeader.0.value").isPassword());
    attributes.get("additionalHeader.0.valueIsSecret").setValue("true");
    assertTrue(system.retrieveAttributes().get("additionalHeader.0.value").isPassword());
    attributes.get("additionalHeader.0.valueIsSecret").setValue("false");
    assertFalse(system.retrieveAttributes().get("additionalHeader.0.value").isPassword());
  }
}
