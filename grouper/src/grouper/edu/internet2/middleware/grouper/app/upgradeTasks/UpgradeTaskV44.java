package edu.internet2.middleware.grouper.app.upgradeTasks;


import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import edu.internet2.middleware.morphString.Morph;

/**
 * GRP-7247: the OAuth JWT signing private key was stored unencrypted in database config.  Newly
 * generated keys are now encrypted at rest by GrouperOAuthSigningKey, but a system that already
 * generated a key before this fix has a plaintext private key sitting in grouper_config.  This task
 * encrypts any such existing value in place.
 *
 * <p>Not a DDL task - it rewrites a single config row.  Idempotent: a value that is already flagged
 * config_encrypted (or is already our Morph ciphertext) is left alone, so it is safe to re-run and a
 * fresh install with no key, or one whose key was created after the fix, has nothing to do.</p>
 */
public class UpgradeTaskV44 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV44.class);

  /** config key whose value must be encrypted at rest (name contains "private") */
  private static final String CONFIG_KEY_PRIVATE_KEY = "grouper.oauth.signingKey.privateKey";

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.4.0");
  }

  @Override
  public void updateVersionFromPrevious(final OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        encryptOAuthSigningPrivateKey(otherJobInput);
        return null;
      }
    });
  }

  /**
   * GRP-7247: encrypt any plaintext grouper.oauth.signingKey.privateKey value stored in the database
   * config.  Skips values that are already encrypted, so this is idempotent and a no-op when there is
   * no key or the key was created after the fix.
   * @param otherJobInput
   */
  private void encryptOAuthSigningPrivateKey(OtherJobInput otherJobInput) {

    Set<GrouperConfigHibernate> existing = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, CONFIG_KEY_PRIVATE_KEY);

    if (GrouperUtil.length(existing) == 0) {
      return;
    }

    for (GrouperConfigHibernate grouperConfigHibernate : existing) {

      // idempotent: the config_encrypted flag is what the read path uses to decide whether to
      // decrypt, so it is the authoritative signal.  if it is already set, the value is already
      // protected and there is nothing to do.
      if (grouperConfigHibernate.isConfigEncrypted()) {
        continue;
      }

      String currentValue = grouperConfigHibernate.retrieveValue();

      // nothing to protect if it is blank
      if (StringUtils.isBlank(currentValue)) {
        continue;
      }

      grouperConfigHibernate.setConfigEncrypted(true);
      grouperConfigHibernate.setValueToSave(Morph.encrypt(currentValue));
      grouperConfigHibernate.saveOrUpdate(false);

      LOG.info("encrypted plaintext OAuth signing private key in database config");

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(
            ", encrypted OAuth signing private key at rest");
      }
    }
  }

}
