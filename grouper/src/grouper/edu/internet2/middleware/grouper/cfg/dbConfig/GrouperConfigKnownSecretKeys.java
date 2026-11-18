/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.regex.Pattern;

/**
 * <p>Config keys that hold secrets even when the metadata in the shipped base properties
 * file does not say so (older releases have <code>valueType: "string"</code> or no metadata
 * at all for them).</p>
 *
 * <p>Whether a config value is a secret is decided from its metadata
 * (<code>sensitive: true</code> or <code>valueType: "password"</code>), from words in the key
 * (pass, secret, private), or from the password checkbox.  A secret key that has none of these
 * is saved to the database in clear, shown unmasked in the config screens and export, and
 * returned unmasked by the MCP config search.  This class marks the known keys as secrets when
 * their metadata is parsed, so a site gets the right classification from a drop-in class,
 * without having to replace its base properties file (which differs in every release).</p>
 *
 * <p>Only java.util.regex and the long standing ConfigItemMetadata setters are used, so this
 * one version works in every release from 5.0.0 on.</p>
 */
public class GrouperConfigKnownSecretKeys {

  /**
   * one known secret key: the pattern a real key matches, an example key the metadata regex
   * should match, and a sibling key (same prefix, different last part) it should not match
   */
  private static class KnownSecretKey {

    /** real keys (or sample keys) that are this secret */
    private final Pattern keyPattern;

    /** an example of this key, e.g. grouper.teamDynamix.myTeamDynamix.webServicesKey */
    private final String exampleKey;

    /** a key with the same prefix that is not a secret, to skip broad metadata regexes */
    private final String siblingKey;

    /**
     * @param theKeyPattern
     * @param theExampleKey
     * @param theSiblingKey
     */
    private KnownSecretKey(String theKeyPattern, String theExampleKey, String theSiblingKey) {
      this.keyPattern = Pattern.compile(theKeyPattern);
      this.exampleKey = theExampleKey;
      this.siblingKey = theSiblingKey;
    }
  }

  /**
   * the known secret keys.  Keep this list short and precise: every key here is shown as a
   * password field, encrypted when saved, and masked when displayed or searched.
   */
  private static final KnownSecretKey[] KNOWN_SECRET_KEYS = new KnownSecretKey[] {

      // TeamDynamix web services key, exchanged for an admin bearer token at api/auth/loginadmin
      new KnownSecretKey("^grouper\\.teamDynamix\\.[^.]+\\.webServicesKey$",
          "grouper.teamDynamix.myTeamDynamix.webServicesKey",
          "grouper.teamDynamix.myTeamDynamix.zzNotSecretProbe"),

      // ESB change log consumer symmetric key, decrypts every outbound change log message.
      // encryptionKeySecret is the preferred name (EsbListenerBase reads it first)
      new KnownSecretKey("^changeLog\\.consumer\\.[^.]+\\.encryptionKey$",
          "changeLog.consumer.myEsb.encryptionKey",
          "changeLog.consumer.myEsb.zzNotSecretProbe"),
      new KnownSecretKey("^changeLog\\.consumer\\.[^.]+\\.encryptionKeySecret$",
          "changeLog.consumer.myEsb.encryptionKeySecret",
          "changeLog.consumer.myEsb.zzNotSecretProbe"),
  };

  /**
   * see if a config item is a known secret, by its key (or sample key) or by its metadata regex
   * @param keyOrSampleKey real key or sample key of the config item, may be null
   * @param metadataRegex regex from the metadata json, may be null
   * @return true if a known secret
   */
  public static boolean isKnownSecret(String keyOrSampleKey, String metadataRegex) {

    // match the key itself
    if (keyOrSampleKey != null && keyOrSampleKey.length() > 0) {
      for (KnownSecretKey knownSecretKey : KNOWN_SECRET_KEYS) {
        if (knownSecretKey.keyPattern.matcher(keyOrSampleKey.trim()).matches()) {
          return true;
        }
      }
    }

    // when the base properties are parsed, the metadata is processed before the key line is
    // read, so only the regex is known then.  The regex must match the example key but not a
    // sibling, so a broad regex that covers a whole config id is not made a password
    if (metadataRegex != null && metadataRegex.length() > 0) {
      Pattern metadataPattern = null;
      try {
        metadataPattern = Pattern.compile(metadataRegex);
      } catch (RuntimeException re) {
        // invalid regex, the metadata code reports that, this is not a known secret
        return false;
      }
      for (KnownSecretKey knownSecretKey : KNOWN_SECRET_KEYS) {
        if (metadataPattern.matcher(knownSecretKey.exampleKey).matches()
            && !metadataPattern.matcher(knownSecretKey.siblingKey).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * if this config item is a known secret, mark it sensitive and a password.  Both are set:
   * the config module forms only look at sensitive, other code looks at either
   * @param configItemMetadata
   * @return true if it was a known secret
   */
  public static boolean applyTo(ConfigItemMetadata configItemMetadata) {
    if (configItemMetadata == null) {
      return false;
    }
    if (!isKnownSecret(configItemMetadata.getKeyOrSampleKey(), configItemMetadata.getRegex())) {
      return false;
    }
    configItemMetadata.setSensitive(true);
    configItemMetadata.setValueType(ConfigItemMetadataType.PASSWORD);
    return true;
  }

  /**
   * called at the end of the metadata lookup by key (ConfigFileMetadata.findConfigItemMetdataFromConfig).
   * A known secret key that has no password metadata in this release's base properties (the ESB
   * changeLog.consumer.&lt;name&gt;.encryptionKey has no entry at all before mainline) gets password
   * metadata here, so the save path (DbConfigEngine), isPassword, the config check redaction and
   * the MCP config search all treat it as a secret without new base properties
   * @param configFileName file whose metadata was searched, may be null
   * @param key real key that was looked up
   * @param configItemMetadata metadata found in the base properties, or null
   * @return the metadata to use (the found one, or new password metadata for a known secret)
   */
  public static ConfigItemMetadata adjustMetadataForKey(ConfigFileName configFileName, String key,
      ConfigItemMetadata configItemMetadata) {

    // the known secret keys are all grouper-loader.properties keys.  Only answer for that file so
    // ConfigFileName.findConfigItemMetdata (which searches every file) does not find it twice
    if (configFileName != ConfigFileName.GROUPER_LOADER_PROPERTIES || !isKnownSecret(key, null)) {
      return configItemMetadata;
    }

    // already a password (e.g. mainline base properties, or fixed up by applyTo)
    if (configItemMetadata != null && (configItemMetadata.isSensitive()
        || configItemMetadata.getValueType() == ConfigItemMetadataType.PASSWORD)) {
      return configItemMetadata;
    }

    // a new object, not a change to a shared cached item that may match other keys
    ConfigItemMetadata secretMetadata = new ConfigItemMetadata();
    secretMetadata.setKey(key);
    secretMetadata.setSensitive(true);
    secretMetadata.setValueType(ConfigItemMetadataType.PASSWORD);
    return secretMetadata;
  }

}
