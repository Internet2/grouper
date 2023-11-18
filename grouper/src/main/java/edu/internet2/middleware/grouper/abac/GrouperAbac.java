package edu.internet2.middleware.grouper.abac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperAbac {

  public static final String GROUPER_JEXL_SCRIPT_MARKER_DEF = "grouperJexlScriptMarkerDef";
  
  public static final String GROUPER_JEXL_SCRIPT_MARKER = "grouperJexlScriptMarker";

  public static final String GROUPER_JEXL_SCRIPT_VALUE_DEF = "grouperJexlScriptValueDef";

  public static final String GROUPER_JEXL_SCRIPT_JEXL_SCRIPT = "grouperJexlScriptJexlScript";
  
  public static final String GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES = "grouperJexlScriptIncludeInternalSources";

  /**
   * 
   * @return the stem name
   */
  public static String jexlScriptStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:abacJexlScript";
  }

  /**
   * 
   * @param sourceId
   * @return true
   */
  public static boolean internalSourceId(String sourceId) {
    return StringUtils.equals(sourceId, "g:gsa") 
        || StringUtils.equals(sourceId, "g:isa") 
        || StringUtils.equals(sourceId, "grouperEntities")
        || StringUtils.equals(sourceId, "grouperExternal");
  }
  
  /**
   * return the error message or null for valid
   * @param script
   * @return the error message or null for valid
   */
  public static String validScript(String script) {
    if (script == null) {
      return "script is null";
    }
    script = script.trim();
    try {
      GrouperAbacEntity grouperAbacEntity = new GrouperAbacEntity();
      grouperAbacEntity.setMultiValuedGroupExtensionInFolder(new HashMap<String, Set<String>>());
      grouperAbacEntity.setSingleValuedGroupExtensionInFolder(new HashMap<String, String>());
      grouperAbacEntity.setMemberOfGroupNames(new HashSet<String>());
      final JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(false);
      jexlEngine.setLenient(true);

      jexlEngine.createScript(script.substring(2, script.length()-1));
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      
      variableMap.put("entity", grouperAbacEntity);
      
      Object result = runScriptStatic(script, variableMap);
      GrouperUtil.booleanValue(result);
      
      Pattern patternGroupWithDoubleQuote = Pattern.compile("entity\\.memberOf\\s*\\(\\s*\"");
      Matcher matcherGroupWithDoubleQuote = patternGroupWithDoubleQuote.matcher(script);
      if (matcherGroupWithDoubleQuote.matches()) {
        return GrouperTextContainer.textOrNull("grouperLoaderEditJexlScriptInvalidDoubleQuote");
      }
      // TODO make sure count of entity.memberOf equals count of regex
      // TODO make sure group names exist
      // TODO make sure user can read group names
      
      return null;
    } catch (RuntimeException re) {
      return GrouperUtil.getFullStackTrace(re);
    }

  }
  
  public static Object runScriptStatic(String script, Map<String, Object> variableMap) {
    Object result = GrouperUtil.substituteExpressionLanguageScript(script, variableMap, true, false, true);
    return result;
    
  }
  
}
