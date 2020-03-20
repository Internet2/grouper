/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class CustomUiUtil {

  /**
   * 
   */
  public CustomUiUtil() {
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CustomUiUtil.class);

  /**
   * 
   * @param groupId
   * @param groupName
   * @param group
   */
  public static void validateGroup(String groupId, String groupName, Group group) {
    if (StringUtils.isBlank(groupId) && StringUtils.isBlank(groupName)) {
      return;
    }
    if (!StringUtils.isBlank(groupId) && !StringUtils.isBlank(groupName)) {
      throw new RuntimeException("Cant set groupId '" + groupId + "' and groupName '" + groupName + "' at same time!");
    }
    if (!StringUtils.isBlank(groupId) && group == null) {
      throw new RuntimeException("Cant find group by groupId '" + groupId + "'");
    }
    if (!StringUtils.isBlank(groupName) && group == null) {
      throw new RuntimeException("Cant find group by groupName '" + groupName + "'");
    }
  }
  
  /**
   * 
   * @param stemId
   * @param stemName
   * @param stem
   */
  public static void validateStem(String stemId, String stemName, Stem stem) {
    if (StringUtils.isBlank(stemId) && StringUtils.isBlank(stemName)) {
      return;
    }
    if (!StringUtils.isBlank(stemId) && !StringUtils.isBlank(stemName)) {
      throw new RuntimeException("Cant set stemId '" + stemId + "' and stemName '" + stemName + "' at same time!");
    }
    if (!StringUtils.isBlank(stemId) && stem == null) {
      throw new RuntimeException("Cant find stem by stemId '" + stemId + "'");
    }
    if (!StringUtils.isBlank(stemName) && stem == null) {
      throw new RuntimeException("Cant find stem by stemName '" + stemName + "'");
    }
  }
  
  /**
   * 
   * @param attributeDefId
   * @param nameOfAttributeDef
   * @param attributeDef
   */
  public static void validateAttributeDef(String attributeDefId, String nameOfAttributeDef, AttributeDef attributeDef) {
    if (StringUtils.isBlank(attributeDefId) && StringUtils.isBlank(nameOfAttributeDef)) {
      return;
    }
    if (!StringUtils.isBlank(attributeDefId) && !StringUtils.isBlank(nameOfAttributeDef)) {
      throw new RuntimeException("Cant set attributeDefId '" + attributeDefId + "' and nameOfAttributeDef '" + nameOfAttributeDef + "' at same time!");
    }
    if (!StringUtils.isBlank(attributeDefId) && attributeDef == null) {
      throw new RuntimeException("Cant find attributeDef by attributeDefId '" + attributeDefId + "'");
    }
    if (!StringUtils.isBlank(nameOfAttributeDef) && attributeDef == null) {
      throw new RuntimeException("Cant find attributeDef by nameOfAttributeDef '" + nameOfAttributeDef + "'");
    }
  }
  
  /**
   * 
   * @param string
   * @param group
   * @param stem 
   * @param attributeDef 
   * @param subject
   * @param externalVariableMap
   * @return the substituted
   */
  public static String substituteExpressionLanguage(String string, Group group, Stem stem, AttributeDef attributeDef, 
      Subject subject, Map<String, Object> externalVariableMap) {
    
    return substituteExpressionLanguage(string, group, stem, attributeDef, 
        subject, externalVariableMap, false);

  }

  
  /**
   * @param variableMap 
   * @param group
   * @param key 
   */
  public static void guiGroupAssign(Map<String, Object> variableMap, Group group, String key) {
    if (group == null) {
      return;
    }
    Object grouperRequestContainer = variableMap.get("grouperRequestContainer");

    if (grouperRequestContainer == null) {
      throw new RuntimeException("Why no grouperRequestContainer in variableMap?");
    }

    Class<?> guiGroupClass = GrouperUtil.forName("edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup");
    Object guiGroup = GrouperUtil.newInstance(guiGroupClass);
    GrouperUtil.callMethod(guiGroupClass, guiGroup, "setGroup", Group.class, group);
    variableMap.put(key, guiGroup);
    
  }

  /**
   * 
   * @param string
   * @param group
   * @param stem 
   * @param attributeDef 
   * @param subject
   * @param externalVariableMap
   * @param useGuiObjects 
   * @return the substituted
   */
  public static String substituteExpressionLanguage(String string, Group group, Stem stem, AttributeDef attributeDef, 
      Subject subject, Map<String, Object> externalVariableMap, boolean useGuiObjects) {
    
    if (string == null || !string.contains("${")) {
      return string;
    }
    Map<String, Object> variableMap = new HashMap<String, Object>();

    if (externalVariableMap != null) {
      variableMap.putAll(externalVariableMap);
    }

    if (attributeDef != null) {
      variableMap.put("attributeDef", attributeDef);
      if (useGuiObjects) {
        guiAttributeDefAssign(variableMap, attributeDef, "guiAttributeDef");
      }
    }
    if (stem != null) {
      variableMap.put("stem", stem);
      if (useGuiObjects) {
        guiStemAssign(variableMap, stem, "guiStem");
      }
    }
    if (group != null) {
      variableMap.put("group", group);
      if (useGuiObjects) {
        guiGroupAssign(variableMap, group, "guiGroup");
      }
    }
    if (subject != null) {
      variableMap.put("subject", subject);
    }
    variableMap.put("grouperUtil", new GrouperUtil());
    try {
      GrouperTextContainer.assignThreadLocalVariableMap(variableMap);
      string = GrouperUtil.substituteExpressionLanguage(string, variableMap, true, false, false);
    } finally {
      GrouperTextContainer.resetThreadLocalVariableMap();
    }
    return string;
  }

  /**
   * @param variableMap
   * @param stem
   * @param key
   */
  private static void guiStemAssign(Map<String, Object> variableMap, Stem stem,
      String key) {
    if (stem == null) {
      return;
    }
    Object grouperRequestContainer = variableMap.get("grouperRequestContainer");

    if (grouperRequestContainer == null) {
      throw new RuntimeException("Why no grouperRequestContainer in variableMap?");
    }

    Class<?> guiStemClass = GrouperUtil.forName("edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem");
    Object guiStem = GrouperUtil.newInstance(guiStemClass);
    GrouperUtil.callMethod(guiStemClass, guiStem, "setStem", Stem.class, stem);
    variableMap.put(key, guiStem);

  }

  /**
   * @param variableMap
   * @param attributeDef
   * @param string
   */
  private static void guiAttributeDefAssign(Map<String, Object> variableMap,
      AttributeDef attributeDef, String key) {
    if (attributeDef == null) {
      return;
    }
    Object grouperRequestContainer = variableMap.get("grouperRequestContainer");

    if (grouperRequestContainer == null) {
      throw new RuntimeException("Why no grouperRequestContainer in variableMap?");
    }

    Class<?> guiAttributeDefClass = GrouperUtil.forName("edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef");
    Object guiAttributeDef = GrouperUtil.newInstance(guiAttributeDefClass);
    GrouperUtil.callMethod(guiAttributeDefClass, guiAttributeDef, "setAttributeDef", AttributeDef.class, attributeDef);
    variableMap.put(key, guiAttributeDef);

  }
  
}
