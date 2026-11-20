package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import junit.textui.TestRunner;

/**
 * GRP-7532: the OAuth redirect URI allowlist key was misspelled grouper.oauth.redrectUri.  Both spellings are
 * read, and upgrade task V47 renames the database configs to grouper.oauth.redirectUri
 * @author shilen
 */
public class UpgradeTaskV47Test extends GrouperTest {

  /** misspelled prefix */
  private static final String OLD_PREFIX = "grouper.oauth.redrectUri.";

  /** correct prefix */
  private static final String NEW_PREFIX = "grouper.oauth.redirectUri.";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UpgradeTaskV47Test("testRenamesDatabaseConfigs"));
  }

  /**
   * @param name
   */
  public UpgradeTaskV47Test(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    deleteRedirectUriConfigs();
  }

  @Override
  protected void tearDown() {
    deleteRedirectUriConfigs();
    super.tearDown();
  }

  /**
   * a redirect URI is allowed by the correct key, by the misspelled key, and by both together
   */
  public void testOldAndNewSpellingBothAllowed() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(OLD_PREFIX + "oldApp.regex", "^https://old\\.example\\.edu/.*$");
    assertNull(GrouperOAuthStore.validateRedirectUriAllowed("https://old.example.edu/callback"));
    assertNotNull(GrouperOAuthStore.validateRedirectUriAllowed("https://new.example.edu/callback"));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(NEW_PREFIX + "newApp.regex", "^https://new\\.example\\.edu/.*$");
    assertNull(GrouperOAuthStore.validateRedirectUriAllowed("https://old.example.edu/callback"));
    assertNull(GrouperOAuthStore.validateRedirectUriAllowed("https://new.example.edu/callback"));
    assertNotNull(GrouperOAuthStore.validateRedirectUriAllowed("https://other.example.edu/callback"));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(OLD_PREFIX + "oldApp.regex");
    assertNotNull(GrouperOAuthStore.validateRedirectUriAllowed("https://old.example.edu/callback"));
    assertNull(GrouperOAuthStore.validateRedirectUriAllowed("https://new.example.edu/callback"));
  }

  /**
   * V47 renames misspelled database keys, keeps the value and encrypted flag, keeps an existing correct key,
   * and does nothing when run again
   */
  public void testRenamesDatabaseConfigs() {

    GrouperSession.startRootSession();

    String encryptedValue = Morph.encrypt("^https://enc\\.example\\.edu/.*$");

    // plain old key, encrypted old key, and an old key whose correct key already exists
    saveConfig(OLD_PREFIX + "plain.regex", "^https://plain\\.example\\.edu/.*$", false);
    saveConfig(OLD_PREFIX + "enc.regex", encryptedValue, true);
    saveConfig(OLD_PREFIX + "both.regex", "^https://oldvalue\\.example\\.edu/.*$", false);
    saveConfig(NEW_PREFIX + "both.regex", "^https://newvalue\\.example\\.edu/.*$", false);

    new UpgradeTaskV47().updateVersionFromPrevious(null);

    // no misspelled keys left
    assertNull(findConfig(OLD_PREFIX + "plain.regex"));
    assertNull(findConfig(OLD_PREFIX + "enc.regex"));
    assertNull(findConfig(OLD_PREFIX + "both.regex"));

    GrouperConfigHibernate plain = findConfig(NEW_PREFIX + "plain.regex");
    assertNotNull(plain);
    assertEquals("^https://plain\\.example\\.edu/.*$", plain.retrieveValue());
    assertFalse(plain.isConfigEncrypted());

    GrouperConfigHibernate enc = findConfig(NEW_PREFIX + "enc.regex");
    assertNotNull(enc);
    assertEquals(encryptedValue, enc.retrieveValue());
    assertTrue(enc.isConfigEncrypted());

    // the existing correct key wins
    GrouperConfigHibernate both = findConfig(NEW_PREFIX + "both.regex");
    assertNotNull(both);
    assertEquals("^https://newvalue\\.example\\.edu/.*$", both.retrieveValue());

    // run again: nothing changes
    new UpgradeTaskV47().updateVersionFromPrevious(null);

    assertEquals(3, findConfigsWithPrefix(NEW_PREFIX));
    assertEquals(0, findConfigsWithPrefix(OLD_PREFIX));
    assertEquals("^https://plain\\.example\\.edu/.*$", findConfig(NEW_PREFIX + "plain.regex").retrieveValue());
  }

  /**
   * save a grouper.properties database config
   * @param configKey
   * @param value
   * @param encrypted
   */
  private static void saveConfig(String configKey, String value, boolean encrypted) {
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(encrypted);
    grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
    grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
    grouperConfigHibernate.setConfigKey(configKey);
    grouperConfigHibernate.setValueToSave(value);
    grouperConfigHibernate.saveOrUpdate(true);
  }

  /**
   * @param configKey
   * @return the grouper.properties database config with this key, or null
   */
  private static GrouperConfigHibernate findConfig(String configKey) {
    Set<GrouperConfigHibernate> configs = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, configKey);
    assertTrue(GrouperUtil.length(configs) <= 1);
    return GrouperUtil.length(configs) == 0 ? null : configs.iterator().next();
  }

  /**
   * @param prefix
   * @return number of grouper.properties database configs whose key starts with the prefix
   */
  private static int findConfigsWithPrefix(String prefix) {
    int count = 0;
    for (GrouperConfigHibernate grouperConfigHibernate : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, null))) {
      if (grouperConfigHibernate.getConfigKey() != null && grouperConfigHibernate.getConfigKey().startsWith(prefix)) {
        count++;
      }
    }
    return count;
  }

  /**
   * remove redirect URI database configs (both spellings) and overrides, so tests start clean
   */
  private static void deleteRedirectUriConfigs() {
    // GrouperTest.setUp stops all sessions, and delete() writes an audit entry, so run as root
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (GrouperConfigHibernate grouperConfigHibernate : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getConfig()
            .findAll(ConfigFileName.GROUPER_PROPERTIES, null, null))) {
          String configKey = grouperConfigHibernate.getConfigKey();
          if (configKey != null && (configKey.startsWith(OLD_PREFIX) || configKey.startsWith(NEW_PREFIX))) {
            grouperConfigHibernate.delete();
          }
        }
        return null;
      }
    });
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(OLD_PREFIX + "oldApp.regex");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(NEW_PREFIX + "newApp.regex");
  }

}
