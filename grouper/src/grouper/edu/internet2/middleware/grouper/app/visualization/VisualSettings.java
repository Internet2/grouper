/**
 * Copyright 2018 Internet2
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.visualization;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Imports visualization settings from grouper.properties and sets up
 * StyleSet objects containing multiple Styles
 */
public class VisualSettings {

  //todo refresh on a schedule?

  public static final String CONFIG_PREFIX = "visualization";
  public static final String DEFAULT_STYLESET_NAME = "default";

  private boolean isInitted = false;
  private Map<String, VisualStyleSet> styleSets;

  private static final Log LOG = GrouperUtil.getLog(VisualSettings.class);

  /**
   * Constructor to read grouper.properties and parse into a data store containing the
   * default style set and style sets for all the defined modules
   */
  public VisualSettings() {
    if (!isInitted) {
      reloadSettings();
    }
  }

  /**
   * reloads the data stores from grouper.properties
   */
  public void reloadSettings() {
    GrouperConfig config = GrouperConfig.retrieveConfig();

    styleSets = new HashMap<String, VisualStyleSet>();

    // sanity check the default styles, is every standard one in the enum list defined?
    // Note, extras outside of the enum list are ok; user is extending the properties
//    for (VisualStyle.Property property : EnumSet.allOf(VisualStyle.Property.class)) {
//      String propertyName = CONFIG_PREFIX + ".style." + DEFAULT_STYLESET_NAME + "." + property.getName();
//      if (!config.containsKey(propertyName)) {
//        LOG.warn("Default visualization style does not define a default for property " + propertyName);
//      }
//    }

    VisualStyleSet defaultStyleSet = new VisualStyleSet(DEFAULT_STYLESET_NAME,CONFIG_PREFIX + ".style", config, null);
    styleSets.put("default", defaultStyleSet);

    if (GrouperUtil.isNotBlank(config.propertyValueString(CONFIG_PREFIX + ".modules"))) {
      for (String module : GrouperUtil.splitTrim(config.propertyValueString(CONFIG_PREFIX + ".modules"), ",")) {

        styleSets.put(module,
          new VisualStyleSet(module, CONFIG_PREFIX + ".module." + module, config, defaultStyleSet));
      }
    }

    isInitted = true;
  }

  /**
   * returns the set of all style set names, including the default set
   *
   * @return set of StyleSet names
   */
  public Set<String> getStyleSetNames() {
    return styleSets.keySet();
  }

  /**
   * retrieves a StyleSet based on it's module name
   *
   * @param name style set key name
   * @return the style set
   */
  public VisualStyleSet getStyleSet(String name) {
    return styleSets.get(name);
  }

  /**
   * retrieves the default style set
   *
   * @return default style set
   */
  public VisualStyleSet getDefaultStyleSet() {
    return styleSets.get(DEFAULT_STYLESET_NAME);
  }
}
