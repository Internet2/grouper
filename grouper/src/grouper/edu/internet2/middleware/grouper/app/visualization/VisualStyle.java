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

import java.util.HashMap;
import java.util.Map;


/**
 * A visual style containing a set of properties as defined in grouper.properties.
 * The property names are strings that map to a string value. The values can be
 * null (since GrouperConfig doesn't easily read in blank values), so callers should
 * query properties using the optional default if non-null properties are required.
 * If the style has a property for "inherit", retrieval will look into its inherited
 * style hierarchy, recursively, until it finds a non-null value.
 */
public class VisualStyle {

  // Other than INHERIT, these are mostly here for illustration and to have standard
  // names for reference
  public enum Property {
    INHERIT("inherit"), STYLE("style"), COLOR("color"), BGCOLOR("bgcolor"), BORDER("border"), FONT("font"),
    FONT_SIZE("font-size"), FONT_COLOR("font-color"), SHAPE("shape"), SHAPE_STYLE("shape-style"),
    ARROWTAIL("arrowtail"), ARROWHEAD("arrowhead"), DIR("dir");

    private String name;

    private Property(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private StyleObjectType objectType;
  VisualStyleSet owningStyleSet;
  VisualStyleSet defaultStyleSet;

  Map<String, String> propertyMap;

  private static final Log LOG = GrouperUtil.getLog(VisualStyle.class);


  /**
   * constructs a style mapping between property names and values
   *
   * @param prefix config property prefix, for example visualization.style.stem
   *               or visualization.module.graphviz.stem
   * @param config previously instantiated GrouperConfig
   * @param objectType the style object type
   * @param owningStyleSet the current style set module this style resides in, or the default module
   * @param defaultStyleSet the default style set, or null if this is the default
   */
  public VisualStyle(String prefix, GrouperConfig config, StyleObjectType objectType,
                     VisualStyleSet owningStyleSet, VisualStyleSet defaultStyleSet) {
    this.objectType = objectType;
    this.owningStyleSet = owningStyleSet;
    this.defaultStyleSet = defaultStyleSet;

    String typePrefix = prefix + "." + objectType.getName() + ".";

    propertyMap = new HashMap<String, String>();

    for (String propName : config.propertyNames()) {
      if (propName.startsWith(typePrefix)) {
        String propNameTail = propName.substring(typePrefix.length());
        this.propertyMap.put(propNameTail, config.propertyValueString(propName));
      }
    }

    // inheritence logic is greatly simplified by copying from default
    // if the module's default isn't set but the default's for the same type is
    if (getPropertyDirect(Property.INHERIT.getName())==null
              && defaultStyleSet != null
              && defaultStyleSet.getStyle(objectType.getName()) != null
              && defaultStyleSet.getStyle(objectType.getName()).getPropertyDirect(Property.INHERIT.getName())!=null) {
      this.propertyMap.put(Property.INHERIT.getName(),
        defaultStyleSet.getStyle(objectType.getName()).getPropertyDirect(Property.INHERIT.getName()));
    }
  }

  /* internal method, get the inherited style in the current module */
  private VisualStyle fetchModuleInheritStyle() {
    String inheritStyleName = getPropertyDirect(Property.INHERIT.getName());
    if (inheritStyleName != null) {
      VisualStyle inheritStyle = owningStyleSet.getStyle(inheritStyleName);
      if (inheritStyle == null) {
        LOG.warn("Visual style " + this.objectType.getName() + " specifies to inherit from unknown style '" + inheritStyleName + "' and will be ignored");
      }
      return inheritStyle;
    }
    return null;
  }

  /**
   * retrieves the property, or the default value if null
   *
   * @param name property name
   * @param defaultValue fallback value when property is null
   * @return property value or default
   */
  public String getProperty(String name, String defaultValue) {
    String val = getProperty(name);
    return val != null ? val : defaultValue;
  }

  /**
   * This method pursues a few different lines to get at the property value. First,
   * it may be directly defined. Second, //todo pass all unit tests before finishing this
   *
   * @param name property name
   * @return property value, directly or through inherited styles
   */
  public String getProperty(String name) {
    String result = getPropertyDirect(name);
    if (result != null) {
      return result;
    }

    if (owningStyleSet != defaultStyleSet
      && defaultStyleSet != null
      && defaultStyleSet.getStyle(objectType.getName()) != null) {
      result = defaultStyleSet.getStyle(objectType.getName()).getProperty(name);
      if (result != null) {
        return result;
      }
    }

    if (fetchModuleInheritStyle() != null) {
      result = fetchModuleInheritStyle().getProperty(name);
      if (result != null) {
        return result;
      }
    }

    //todo this may be redundant
    if (owningStyleSet != null && owningStyleSet.getDefaultStyle() != null) {
      result = owningStyleSet.getDefaultStyle().getPropertyDirect(name);
      if (result != null) {
        return result;
      }
    }

    if (owningStyleSet != defaultStyleSet
          && defaultStyleSet != null
          && defaultStyleSet.getStyle(objectType.getName()).fetchModuleInheritStyle() != null) {
      result = defaultStyleSet.getStyle(objectType.getName()).fetchModuleInheritStyle().getProperty(name);
      if (result != null) {
        return result;
      }
    }

    if (defaultStyleSet != null && defaultStyleSet.getDefaultStyle() != null) {
      return defaultStyleSet.getDefaultStyle().getPropertyDirect(name);
    }

    return null;
  }

  /**
   * retrieves the property directly or null, without looking through the inheritance hierarchy
   *
   * @param name
   * @return
   */
  public String getPropertyDirect(String name) {
    if (propertyMap.containsKey(name)) {
      return propertyMap.get(name);
    }
    return null;
  }

  public void setProperty(String name, String value) {
    propertyMap.put(name, value);
  }
}
