/**
 * @author mchyzer
 * $Id: GrouperKimUtils.java,v 1.2 2009-12-13 22:33:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * utility methods for grouper kim integration
 */
public class GrouperKimUtils {

  /**
   * source id to use for all subjects, or null if none specified (dont bind to one source)
   * @return the source id
   */
  public static String subjectSourceId() {
    //lets see if there is a source to use
    String sourceId = GrouperClientUtils.propertiesValue("grouper.kim.plugin.subjectSourceId", false);
    sourceId = GrouperClientUtils.isBlank(sourceId) ? null : sourceId;
    return sourceId;
  }
  
  /**
   * 
   * @param debugMap
   * @return the string
   */
  public static String mapForLog(Map<String, Object> debugMap) {
    if (GrouperClientUtils.length(debugMap) == 0) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (String key : debugMap.keySet()) {
      Object valueObject = debugMap.get(key);
      String value = valueObject == null ? null : valueObject.toString();
      result.append(key).append(": ").append(GrouperClientUtils.abbreviate(value, 100)).append(", ");
    }
    //take off the last two chars
    result.delete(result.length()-2, result.length());
    return result.toString();
  }
  
  /**
   * stem where KIM groups are.  The KIM namespace is underneath, then the group.
   * @return the rice stem in the registry, without trailing colon
   */
  public static String kimStem() {
    String kimStem = GrouperClientUtils.propertiesValue("kim.stem", true);
    if (kimStem.endsWith(":")) {
      kimStem = kimStem.substring(0,kimStem.length()-1);
    }
    return kimStem;
  }
   
  /**
   * return the grouper type of kim groups or null if none
   * @return the type
   */
  public static String[] grouperTypesOfKimGroups() {
    String typeString = GrouperClientUtils.propertiesValue("grouper.types.of.kim.groups", false);
    if (GrouperClientUtils.isBlank(typeString)) {
      return null;
    }
    return GrouperClientUtils.splitTrim(typeString, ",");
  }
  
}
