package edu.internet2.middleware.grouper.app.upgradeTasks;


import java.util.Set;

import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * GRP-7532: the OAuth redirect URI allowlist key was misspelled grouper.oauth.redrectUri.&lt;configId&gt;.regex.
 * The correct key is grouper.oauth.redirectUri.&lt;configId&gt;.regex.  This task renames the database config
 * rows (grouper.properties) from the old spelling to the new one.  The old spelling is still read, so a
 * value in a properties file keeps working until the site renames it.
 *
 * <p>Not a DDL task.  Each row is renamed by adding the new key with the same value, hierarchy and encrypted
 * flag, then deleting the old key, through the config API so the change is audited, kept in the config
 * history and pushed to the other nodes.  If the new key already exists for the same hierarchy, it wins and
 * only the old key is deleted.  Idempotent: once no old keys are left there is nothing to do.</p>
 * @author shilen
 */
public class UpgradeTaskV47 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV47.class);

  /** prefix of the misspelled keys */
  private static final String OLD_PREFIX = "grouper.oauth.redrectUri.";

  /** prefix of the correct keys */
  private static final String NEW_PREFIX = "grouper.oauth.redirectUri.";

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.9.2");
  }

  @Override
  public void updateVersionFromPrevious(final OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        renameRedirectUriConfigs(otherJobInput);
        return null;
      }
    });
  }

  /**
   * rename grouper.oauth.redrectUri.* database configs to grouper.oauth.redirectUri.*
   * @param otherJobInput
   */
  private void renameRedirectUriConfigs(OtherJobInput otherJobInput) {

    Set<GrouperConfigHibernate> grouperConfigs = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, null);

    for (GrouperConfigHibernate oldConfig : GrouperUtil.nonNull(grouperConfigs)) {

      String oldKey = oldConfig.getConfigKey();
      if (oldKey == null || !oldKey.startsWith(OLD_PREFIX)) {
        continue;
      }

      // keeps any suffix, e.g. .regex or .regex.elConfig
      String newKey = NEW_PREFIX + oldKey.substring(OLD_PREFIX.length());

      // if the correctly spelled key is already there for the same hierarchy, keep it
      boolean newKeyExists = false;
      Set<GrouperConfigHibernate> newKeyConfigs = GrouperDAOFactory.getFactory().getConfig()
          .findAll(ConfigFileName.GROUPER_PROPERTIES, null, newKey);
      for (GrouperConfigHibernate newKeyConfig : GrouperUtil.nonNull(newKeyConfigs)) {
        if (Strings.CS.equals(newKeyConfig.getConfigFileHierarchyDb(), oldConfig.getConfigFileHierarchyDb())) {
          newKeyExists = true;
          break;
        }
      }

      if (newKeyExists) {
        LOG.warn("Config key '" + newKey + "' already exists, deleting the misspelled '" + oldKey + "'");
      } else {
        GrouperConfigHibernate newConfig = new GrouperConfigHibernate();
        newConfig.setConfigFileNameDb(oldConfig.getConfigFileNameDb());
        newConfig.setConfigFileHierarchyDb(oldConfig.getConfigFileHierarchyDb());
        newConfig.setConfigKey(newKey);
        newConfig.setConfigComment(oldConfig.getConfigComment());
        // the stored value as is: if it is encrypted it stays encrypted with the same flag
        newConfig.setConfigEncrypted(oldConfig.isConfigEncrypted());
        newConfig.setValueToSave(oldConfig.retrieveValue());
        newConfig.saveOrUpdate(true);
        LOG.info("renamed config key '" + oldKey + "' to '" + newKey + "'");
      }

      oldConfig.delete();

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", renamed config " + oldKey);
      }
    }
  }

}
