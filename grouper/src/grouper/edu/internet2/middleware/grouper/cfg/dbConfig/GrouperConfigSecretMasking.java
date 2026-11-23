/*******************************************************************************
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.cfg.dbConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * <p>Decides whether a config value is a secret that must be masked when config is shown or
 * searched outside the config screens (for example the MCP admin_config_search tool, regex and
 * Lucene).  It keeps every rule of {@link GrouperConfigHibernate#isPasswordHelper} (ciphertext,
 * password or sensitive metadata, key names with pass, secret or private) and adds the sibling
 * flag rule below.</p>
 *
 * <p>Sibling flag rule: some config families say per entry whether a value is a secret, with a
 * boolean next to it, for example
 * <code>grouper.wsBearerToken.&lt;id&gt;.additionalHeader.N.value</code> and
 * <code>...additionalHeader.N.valueIsSecret</code>.  The value key has no secret-like word and
 * plain string metadata, and the config cascade has already decrypted values that were stored
 * encrypted, so the generic rules see an ordinary string.  So a key ending in <code>.value</code>
 * (or its <code>.value.elConfig</code> form) is a secret when <code>&lt;prefix&gt;.valueIsSecret</code>
 * is true in the same config.  A flag that is set but not a boolean counts as true (fail closed).
 * The rule is not specific to one family, so a later family with the same pattern is covered.</p>
 *
 * <p>Checked against the config cascade, so it covers values written by the external system
 * screen, config import, the generic config editor, GSH, the WS/MCP config tools and files.</p>
 */
public class GrouperConfigSecretMasking {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperConfigSecretMasking.class);

  /** suffix of a value whose secrecy is decided by a sibling flag */
  private static final String VALUE_SUFFIX = ".value";

  /** suffix of the el form of such a value */
  private static final String VALUE_EL_SUFFIX = ".value.elConfig";

  /** the sibling flag, after the common prefix */
  private static final String VALUE_IS_SECRET_SUFFIX = ".valueIsSecret";

  /**
   * no instances
   */
  private GrouperConfigSecretMasking() {
  }

  /**
   * whether a config value must be masked
   * @param configFileName the config file, or null if the metadata is passed in (so it is not
   * looked up again)
   * @param metadata the metadata of the key if already known, or null
   * @param config the config cascade the key is in, for the sibling flag, or null to skip that rule
   * @param key the config key
   * @param value the value as the cascade returns it (decrypted)
   * @return true if the value is a secret
   */
  public static boolean isSensitive(ConfigFileName configFileName, ConfigItemMetadata metadata,
      ConfigPropertiesCascadeBase config, String key, String value) {

    if (GrouperConfigHibernate.isPasswordHelper(configFileName, metadata, key, value,
        StringUtils.isNotBlank(value), null)) {
      return true;
    }
    return siblingValueIsSecret(config, key);
  }

  /**
   * the sibling flag rule
   * @param config the config cascade, null means the rule does not apply
   * @param key the config key
   * @return true if key is a .value (or .value.elConfig) whose .valueIsSecret sibling is true
   */
  public static boolean siblingValueIsSecret(ConfigPropertiesCascadeBase config, String key) {

    if (config == null || StringUtils.isBlank(key)) {
      return false;
    }

    String prefix = null;
    if (key.endsWith(VALUE_EL_SUFFIX)) {
      prefix = key.substring(0, key.length() - VALUE_EL_SUFFIX.length());
    } else if (key.endsWith(VALUE_SUFFIX)) {
      prefix = key.substring(0, key.length() - VALUE_SUFFIX.length());
    }
    if (StringUtils.isBlank(prefix)) {
      return false;
    }

    String flag = null;
    try {
      flag = config.propertyValueString(prefix + VALUE_IS_SECRET_SUFFIX);
    } catch (Exception e) {
      // cannot tell, so treat it as a secret
      LOG.debug("Could not read " + prefix + VALUE_IS_SECRET_SUFFIX, e);
      return true;
    }
    if (StringUtils.isBlank(flag)) {
      return false;
    }
    try {
      return GrouperUtil.booleanValue(flag);
    } catch (Exception e) {
      // set, but not a boolean: fail closed
      return true;
    }
  }

  /**
   * @return what a masked value is shown as, the same as the config screens
   */
  public static String maskedValue() {
    return GrouperConfigHibernate.ESCAPED_PASSWORD;
  }
}
