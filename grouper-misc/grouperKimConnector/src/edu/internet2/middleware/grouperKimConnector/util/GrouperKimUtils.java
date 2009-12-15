/**
 * @author mchyzer
 * $Id: GrouperKimUtils.java,v 1.3 2009-12-15 17:07:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.Map;

import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.bo.types.dto.KimTypeInfo;
import org.kuali.rice.kim.service.KIMServiceLocator;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;


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
  
  /**
   * cache the group type id since it doesnt change
   */
  private static String typeId = null;
  
  /**
   * get the default group type id 
   * @return the type id
   */
  public static String grouperDefaultGroupTypeId() {
    if (typeId == null) {
      KimTypeInfo typeInfo = KIMServiceLocator.getTypeInfoService().getKimTypeByName("KUALI", "Default");
      typeId = typeInfo.getKimTypeId();
    }
    return typeId;
  }
  
  /**
   * convert a ws group to a group info
   * @param wsGroup
   * @return the group info
   */
  public static GroupInfo convertWsGroupToGroupInfo(WsGroup wsGroup) {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setGroupId(wsGroup.getUuid());
    groupInfo.setGroupName(wsGroup.getExtension());
    groupInfo.setGroupDescription(wsGroup.getDescription());
    groupInfo.setKimTypeId(GrouperKimUtils.grouperDefaultGroupTypeId());
    groupInfo.setNamespaceCode(GrouperKimUtils.calculateNamespaceCode(wsGroup.getName()));
    WsGroupDetail detail = wsGroup.getDetail();
    
    //if there is a detail and attributes, then set the attributeSet
    if (detail != null) {
      int attributeLength = GrouperClientUtils.length(detail.getAttributeNames());
      if (attributeLength > 0) {
        AttributeSet attributeSet = new AttributeSet();
        groupInfo.setAttributes(attributeSet);
        
        for (int i=0;i<attributeLength;i++) {
          attributeSet.put(detail.getAttributeNames()[i], detail.getAttributeValues()[i]);
        }
      }
    }
    return groupInfo;
  }
  
  /**
   * if group name is: a:b:c:d, and the kuali stem is a:b, then the namespace is c
   * @param groupName
   * @return the namespace code
   */
  public static String calculateNamespaceCode(String groupName) {
    if (GrouperClientUtils.isBlank(groupName)) {
      return groupName;
    }
    int lastColonIndex = groupName.lastIndexOf(':');
    if (lastColonIndex == -1) {
      throw new RuntimeException("Not expecting a name with no folders: '" + groupName + "'");
    }
    String stem = groupName.substring(0,lastColonIndex);
    String kimStem = kimStem();
    if (!stem.startsWith(kimStem)) {
      throw new RuntimeException("Why does the stem not start with kimStem? '" + groupName + "', '" + kimStem + "'");
    }
    //group is in the kim stem, no namespace
    if (stem.equals(kimStem)) {
      return null;
    }
    //add one for the colon
    String namespace = stem.substring(kimStem.length() + 1);
    return namespace;
  }
  
}
