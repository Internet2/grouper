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

import java.util.*;

/** a VisualStyleSet represents style properties for a specific module. The
 * styles are stored as a map keyed by a name string, such as "color" or "font".
 * The style set has an optional parent style set that it may inherit properties from.
 */
public class VisualStyleSet {

  Map<String, VisualStyle> styleMap;
  VisualStyleSet parentStyleSet;
  String name;


  /**
   * This constructs a new style set based on a previously loaded grouper.properties
   * config. The style set will have a unique module name. Styles will be read from
   * the config based on the prefix, followed by the string name of the {@link StyleObjectType}
   * enum. Styles not in the {@link StyleObjectType} will be ignored.
   *
   * @param name the module name
   * @param prefix the config property prefix, for example
   *               visualization.style.default or visualization.module.graphviz
   * @param config an instantiated grouper config to read properties from
   * @param parentStyleSet an optional style set to inerit from, or null if not relevant or
   *                       is already the top level style set
   */
  protected VisualStyleSet(String name, String prefix, GrouperConfig config, VisualStyleSet parentStyleSet) {
    this.name = name;
    this.parentStyleSet = parentStyleSet;

    styleMap = new HashMap();

    for (StyleObjectType type : EnumSet.allOf(StyleObjectType.class)) {
      VisualStyle style = new VisualStyle(prefix, config, type, this, parentStyleSet);

      styleMap.put(type.getName(), style);
    }
  }

  /**
   * retrieves a style based on its name
   *
   * @param name lookup key for the style
   * @return style matching this name // todo or null?
   */
  public VisualStyle getStyle(String name) {
    return styleMap.get(name);
  }

  /**
   * retrieves a style based on its ObjectType enum
   *
   * @param objectType object type enum
   * @return style for this object type // todo or null?
   */
  public VisualStyle getStyle(StyleObjectType objectType) {
    return styleMap.get(objectType.getName());
  }

  /**
   * gets the value for a specific style and property
   *
   * @param typeName lookup key for the style
   * @param propertyName lookup key for the property within the style
   * @return style property value, or null if either the style or property is not found
   */
  public String getStyleProperty(String typeName, String propertyName) {
    return getStyleProperty(typeName, propertyName, null);
  }

  /**
   * gets the value for a specific style and property, or a fallback default value
   *
   * @param typeName lookup key for the style
   * @param propertyName lookup key for the property within the style
   * @return style property value, or default value if either the style or property is not found
   */
  public String getStyleProperty(String typeName, String propertyName, String defaultValue) {
    VisualStyle style = getStyle(typeName);
    return (style == null) ? null : style.getProperty(propertyName, defaultValue);
  }

  /**
   * gets the value for a specific style and property
   *
   * @param objectType object type enum for the style
   * @param propertyName lookup key for the property within the style
   * @return style property value, or null if either the style or property is not found
   */
  public String getStyleProperty(StyleObjectType objectType, String propertyName) {
    return getStyleProperty(objectType, propertyName, null);
  }

  /**
   * gets the value for a specific style and property, or a fallback default value
   *
   * @param objectType object type enum for the style
   * @param propertyName lookup key for the property within the style
   * @return style property value, or default value if either the style or property is not found
   */
  public String getStyleProperty(StyleObjectType objectType, String propertyName, String defaultValue) {
    VisualStyle style = getStyle(objectType);
    return (style == null) ? null : style.getProperty(propertyName, defaultValue);
  }


  /**
   * gets the default object type style
   *
   * @return style for the default object type
   */
  public VisualStyle getDefaultStyle() {
    return styleMap.get(StyleObjectType.DEFAULT.getName());
  }

  /**
   * retrieves the set of all style names
   *
   * @return set of key names for all defined styles in the StyleSet
   */
  public Set<String> getStyleNames() {
    return styleMap.keySet();
  }
}
