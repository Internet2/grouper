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
package edu.internet2.middleware.grouper.app.customComposite;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.ConfigFile;

/**
 * where the value of a config key comes from.  a screen which edits config through the database
 * can only change a value which is not set in a config file: the file is later in the hierarchy
 * than the base and earlier than the database, but a value in the file is what an administrator
 * put on disk on purpose, and the screen should not quietly shadow it
 *
 * @author shilen
 */
public enum GrouperConfigSourceType {

  /** not set anywhere */
  none,

  /** only in the base properties file which ships with Grouper */
  base,

  /** in a properties file on disk (or a test override), which this screen does not change */
  configFile,

  /** in the database, which this screen can change */
  database;

  /**
   * whether a value from this source can be changed through database config.  a value from the base
   * file is not: the screen could shadow it in the database, but could never delete it, so a delete
   * would report success while the entry stays
   * @return true if it can be changed
   */
  public boolean isEditable() {
    return this == none || this == database;
  }

  /**
   * find where the value for a key comes from.  the last source in the hierarchy which has the
   * key (or its expression language form) wins, the same way config values cascade
   * @param config the config, e.g. GrouperConfig.retrieveConfig()
   * @param key the config key
   * @return the source, never null
   */
  public static GrouperConfigSourceType sourceOf(ConfigPropertiesCascadeBase config, String key) {

    String elKey = key + ".elConfig";

    // a test override (or thread local one) is not something this screen can change
    Map<String, String> overrideMap = config.propertiesThreadLocalOverrideMap();
    if (overrideMap.containsKey(key) || overrideMap.containsKey(elKey)) {
      return configFile;
    }
    overrideMap = config.propertiesOverrideMap();
    if (overrideMap.containsKey(key) || overrideMap.containsKey(elKey)) {
      return configFile;
    }

    List<ConfigFile> configFiles = config.internalRetrieveConfigFiles();

    // the source of the value in effect, from the last file in the hierarchy which has the key
    GrouperConfigSourceType result = none;

    for (int i = GrouperUtil.length(configFiles) - 1; i >= 0; i--) {

      // not named configFile, which would hide the enum constant returned below
      ConfigFile hierarchyFile = configFiles.get(i);

      if (hierarchyFile.getProperties() == null) {
        continue;
      }

      if (!hierarchyFile.getProperties().containsKey(key) && !hierarchyFile.getProperties().containsKey(elKey)) {
        continue;
      }

      // the first file in the hierarchy is always the base
      GrouperConfigSourceType source = i == 0 ? base
          : (isDatabase(hierarchyFile) ? database : configFile);

      if (result == none) {
        result = source;
        // a value from a file is in effect, nothing below it matters
        if (source != database) {
          return result;
        }
        continue;
      }

      // the database value in effect is overriding a config file.  deleting the database value
      // would bring the file value back while the screen said it was deleted, so treat it as set in
      // the file, which is changed in the file
      if (source == configFile) {
        return configFile;
      }
    }

    return result;
  }

  /**
   * if a file in the config hierarchy is the database
   * @param hierarchyFile the file
   * @return true if database
   */
  private static boolean isDatabase(ConfigFile hierarchyFile) {
    String originalConfig = StringUtils.defaultString(hierarchyFile.getOriginalConfig());
    String configType = StringUtils.trim(GrouperUtil.prefixOrSuffix(originalConfig, ":", true));
    return "database".equalsIgnoreCase(configType);
  }
}
