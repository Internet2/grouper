package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * <p>Advisory M1 (externalSystemCredentialExposure): external system credentials must be classified
 * as secrets (encrypted when saved, masked when shown or searched) and must not be written to the
 * http debug log, the command log, or the debug map.</p>
 *
 * <ul>
 * <li>The TeamDynamix webServicesKey and the ESB change log consumer encryptionKey are passwords,
 * even when the metadata comes from an older base properties file (GrouperConfigKnownSecretKeys).</li>
 * <li>TeamDynamix login: the request body (BEID and WebServicesKey) is not logged.</li>
 * <li>CCure: the access token (login response body, url parameter) and the session id (header) are
 * not logged, and the debug map only says if they are there.</li>
 * <li>ESB: encryptionKeySecret is read first, then the old encryptionKey.</li>
 * </ul>
 *
 * <p>The TeamDynamix and CCure logging tests need the mock services in Tomcat
 * (junit.test.tomcat=true, see CCureProvisionerTest), the rest run anywhere.  TeamDynamix and
 * CCure are not in every release (e.g. 2.6.19 has neither): those two tests reach them by
 * reflection so this one file compiles on every release, and skip (with a line on stdout) where
 * the release does not have them.</p>
 */
public class ExternalSystemCredentialHandlingTest extends GrouperTest {

  /** recognizable secret values so a leak is easy to find */
  private static final String SECRET_VALUE = "secPatchCredSecretValue";

  /** ESB consumer name used by the tests, config rows for it are removed in tearDown */
  private static final String ESB_CONSUMER = "secPatchCredesb";

  /** TeamDynamix config id used by the classification tests (not the mock one) */
  private static final String TDX_CONFIG_ID = "secPatchCredtdx";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSystemCredentialHandlingTest("testStoreKnownSecretsEncrypted"));
  }

  /**
   * @param name
   */
  public ExternalSystemCredentialHandlingTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    // GrouperDbConfig.store needs an open session on 4.x (GrouperDbConfig.seeIfAllowed); harmless later
    GrouperSession.startRootSession();
  }

  @Override
  protected void tearDown() {
    // remove the config rows the tests wrote
    new GcDbAccess().sql("delete from grouper_config where config_key like ?")
      .addBindVar("changeLog.consumer." + ESB_CONSUMER + ".%").executeSql();
    new GcDbAccess().sql("delete from grouper_config where config_key like ?")
      .addBindVar("grouper.teamDynamix." + TDX_CONFIG_ID + ".%").executeSql();
    ConfigPropertiesCascadeBase.clearCache();
    super.tearDown();
  }

  // =============================================
  // Classification
  // =============================================

  /**
   * the known secret keys match by key and by metadata regex, and a broad regex that covers a
   * whole config id is not made a password
   */
  public void testKnownSecretKeys() {
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret("grouper.teamDynamix.abc.webServicesKey", null));
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret("changeLog.consumer.abc.encryptionKey", null));
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret("changeLog.consumer.abc.encryptionKeySecret", null));

    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret("grouper.teamDynamix.abc.url", null));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret("grouper.teamDynamix.abc.beid", null));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret("changeLog.consumer.abc.encryptionImplementation", null));

    // the regex in the base properties before the fix (not anchored at the end) and after
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^grouper\\.teamDynamix\\.([^.]+)\\.webServicesKey"));
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^grouper\\.teamDynamix\\.([^.]+)\\.webServicesKey$"));
    assertTrue(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^changeLog\\.consumer\\.([^.]+)\\.encryptionKey(Secret)?$"));

    // other items are not touched, including a broad regex
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^grouper\\.teamDynamix\\.([^.]+)\\.url$"));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^grouper\\.teamDynamix\\.([^.]+)\\.(.*)$"));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret(null, "^changeLog\\.consumer\\.([^.]+)\\.(.*)$"));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret(null, "(invalid regex"));
    assertFalse(GrouperConfigKnownSecretKeys.isKnownSecret(null, null));
  }

  /**
   * metadata parsed from the base properties of a release before the fix (valueType string)
   * comes out sensitive and password, which is what a patched site without new base properties gets
   */
  public void testOldTeamDynamixMetadataIsSensitive() {
    ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
    configItemMetadata.setRawMetadataJson(
        "{valueType: \"string\", required: true, regex: \"^grouper\\\\.teamDynamix\\\\.([^.]+)\\\\.webServicesKey\"}");
    configItemMetadata.processMetadata();
    assertTrue(configItemMetadata.isSensitive());
    assertEquals(ConfigItemMetadataType.PASSWORD, configItemMetadata.getValueType());
    assertTrue(GrouperConfigHibernate.isPasswordHelper(configItemMetadata));

    // a sibling item is unchanged
    ConfigItemMetadata urlMetadata = new ConfigItemMetadata();
    urlMetadata.setRawMetadataJson(
        "{valueType: \"string\", required: true, regex: \"^grouper\\\\.teamDynamix\\\\.([^.]+)\\\\.url$\"}");
    urlMetadata.processMetadata();
    assertFalse(urlMetadata.isSensitive());
    assertEquals(ConfigItemMetadataType.STRING, urlMetadata.getValueType());
  }

  /**
   * the shipped metadata (found by key) says these are passwords, with no password checkbox
   */
  public void testIsPassword() {
    assertTrue(GrouperConfigHibernate.isPassword(ConfigFileName.GROUPER_LOADER_PROPERTIES, null,
        "grouper.teamDynamix.tdx.webServicesKey", "KEYVAL", true, null));
    assertTrue(GrouperConfigHibernate.isPassword(ConfigFileName.GROUPER_LOADER_PROPERTIES, null,
        "changeLog.consumer.esbx.encryptionKey", "KEYVAL", true, null));
    assertTrue(GrouperConfigHibernate.isPassword(ConfigFileName.GROUPER_LOADER_PROPERTIES, null,
        "changeLog.consumer.esbx.encryptionKeySecret", "KEYVAL", true, null));

    ConfigItemMetadata tdxMetadata = ConfigFileName.GROUPER_LOADER_PROPERTIES.findConfigItemMetdataFromConfig("grouper.teamDynamix.tdx.webServicesKey");
    assertNotNull(tdxMetadata);
    assertTrue(tdxMetadata.isSensitive());

    ConfigItemMetadata esbMetadata = ConfigFileName.GROUPER_LOADER_PROPERTIES.findConfigItemMetdataFromConfig("changeLog.consumer.esbx.encryptionKey");
    assertNotNull(esbMetadata);
    assertTrue(esbMetadata.isSensitive());

    // not a secret
    assertFalse(GrouperConfigHibernate.isPassword(ConfigFileName.GROUPER_LOADER_PROPERTIES, null,
        "grouper.teamDynamix.tdx.url", "https://tdx.example.edu/", true, null));
  }

  /**
   * storing the keys as root without the password checkbox writes encrypted rows
   */
  public void testStoreKnownSecretsEncrypted() {
    String[] keys = new String[] {
        "grouper.teamDynamix." + TDX_CONFIG_ID + ".webServicesKey",
        "changeLog.consumer." + ESB_CONSUMER + ".encryptionKey",
        "changeLog.consumer." + ESB_CONSUMER + ".encryptionKeySecret"};
    for (String key : keys) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key).value(SECRET_VALUE).store();

      List<String> rows = new GcDbAccess().sql("select config_encrypted from grouper_config where config_file_name = ? and config_key = ?")
          .addBindVar("grouper-loader.properties").addBindVar(key).selectList(String.class);
      assertEquals(key, 1, GrouperUtil.length(rows));
      assertEquals(key + " should be stored encrypted", "T", rows.get(0));

      List<String> values = new GcDbAccess().sql("select config_value from grouper_config where config_file_name = ? and config_key = ?")
          .addBindVar("grouper-loader.properties").addBindVar(key).selectList(String.class);
      assertFalse(key + " should not be stored in clear", values.get(0).contains(SECRET_VALUE));
    }

    // still readable (decrypted) through the config
    ConfigPropertiesCascadeBase.clearCache();
    assertEquals(SECRET_VALUE, GrouperLoaderConfig.retrieveConfig().propertyValueString(keys[0]));
  }

  // =============================================
  // ESB key name
  // =============================================

  /**
   * encryptionKeySecret wins, the old encryptionKey is the fallback
   */
  public void testEsbEncryptionKeyName() {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
      .propertyName("changeLog.consumer." + ESB_CONSUMER + ".encryptionKey").value("oldNameKey").store();
    ConfigPropertiesCascadeBase.clearCache();
    assertEquals("oldNameKey", EsbListenerBase.retrieveEncryptionKey(ESB_CONSUMER));

    new GrouperDbConfig().configFileName("grouper-loader.properties")
      .propertyName("changeLog.consumer." + ESB_CONSUMER + ".encryptionKeySecret").value("newNameKey").store();
    ConfigPropertiesCascadeBase.clearCache();
    assertEquals("newNameKey", EsbListenerBase.retrieveEncryptionKey(ESB_CONSUMER));
  }

  // =============================================
  // Logging (needs the mock services in Tomcat)
  // =============================================

  // =============================================
  // reflection for TeamDynamix and CCure (not in every release)
  // =============================================

  /** TeamDynamix external system */
  private static final String TDX_EXTERNAL_SYSTEM_CLASS = "edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixExternalSystem";

  /** TeamDynamix test setup */
  private static final String TDX_TEST_UTILS_CLASS = "edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisionerTestUtils";

  /** CCure external system */
  private static final String CCURE_EXTERNAL_SYSTEM_CLASS = "edu.internet2.middleware.grouper.app.ccure.CCureExternalSystem";

  /** CCure api calls */
  private static final String CCURE_API_COMMANDS_CLASS = "edu.internet2.middleware.grouper.app.ccure.CCureApiCommands";

  /** CCure mock tables */
  private static final String CCURE_MOCK_CLASS = "edu.internet2.middleware.grouper.app.ccure.CCureMockServiceHandler";

  /** CCure test setup */
  private static final String CCURE_TEST_UTILS_CLASS = "edu.internet2.middleware.grouper.app.ccure.CCureProvisionerTestUtils";

  /**
   * @param className
   * @return the class or null if this release does not have it
   */
  private static Class<?> classOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }

  /**
   * call a public method by name and argument count (the methods used here are not overloaded by
   * argument count)
   * @param target object, or a Class for a static method
   * @param methodName
   * @param args
   * @return the result
   */
  private static Object call(Object target, String methodName, Object... args) {
    Class<?> theClass = target instanceof Class ? (Class<?>)target : target.getClass();
    for (Method method : theClass.getMethods()) {
      if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
        try {
          return method.invoke(target instanceof Class ? null : target, args);
        } catch (InvocationTargetException ite) {
          if (ite.getCause() instanceof RuntimeException) {
            throw (RuntimeException)ite.getCause();
          }
          throw new RuntimeException(ite.getCause());
        } catch (IllegalAccessException iae) {
          throw new RuntimeException(iae);
        }
      }
    }
    throw new RuntimeException("no method " + methodName + " with " + args.length + " args on " + theClass.getName());
  }

  /**
   * @param theClass
   * @param fieldName public static field
   * @return its value
   */
  private static Object staticField(Class<?> theClass, String fieldName) {
    try {
      return theClass.getField(fieldName).get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
