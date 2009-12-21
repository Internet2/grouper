/**
 * @author mchyzer
 * $Id: GrouperKimUtils.java,v 1.6 2009-12-21 06:15:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.bo.types.dto.KimTypeInfo;
import org.kuali.rice.kim.service.KIMServiceLocator;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;


/**
 * utility methods for grouper kim integration
 */
public class GrouperKimUtils {

  /**
   * filter out subjects which are groups and not in the kim stem
   * @param wsSubjects
   */
  public static void filterGroupsNotInKimStem(List<WsSubject> wsSubjects) {
    
    if (GrouperClientUtils.length(wsSubjects) == 0) {
      return;
    }
    
    Set<String> wsSubjectsSet = new HashSet<String>();
    List<String> wsGroupSubjectsList = new ArrayList<String>();

    //filter out the group subjects
    for (int i=0;i<GrouperClientUtils.length(wsSubjects);i++) {
      WsSubject wsSubject = wsSubjects.get(i);
      if (GrouperClientUtils.equals("g:gsa", wsSubject.getSourceId())) {
        wsGroupSubjectsList.add(wsSubject.getId());
      } else {
        wsSubjectsSet.add(wsSubject.getId());
      }
    }
    //if no groups, all good
    if (wsGroupSubjectsList.size() == 0) {
      return;
    }

    //lookup the groups and see if in the right stem
    GcFindGroups gcFindGroups = new GcFindGroups();
    
    for (String groupId : wsGroupSubjectsList) {
      gcFindGroups.addGroupUuid(groupId);
    }
    
    WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
    WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
    
    Map<String, WsGroup> groupLookup = new HashMap<String, WsGroup>();
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
      groupLookup.put(wsGroup.getUuid(), wsGroup);
    }
    
    String kimStem = GrouperKimUtils.kimStem() + ":";
    
    Iterator<WsSubject> iterator = wsSubjects.iterator();
    
    while (iterator.hasNext()) {
      WsSubject wsSubject = iterator.next();
      if (wsSubjectsSet.contains(wsSubject.getId())) {
        continue;
      }
      WsGroup wsGroup = groupLookup.get(wsSubject.getId());
      //if group not readable, or not in right stem, remove
      if (wsGroup == null || !wsGroup.getName().startsWith(kimStem)) {
        iterator.remove();
      }
    }
  }
  
  /**
   * filter out subjects which are groups and not in the kim stem
   * @param wsMemberships
   */
  public static void filterMembershipGroupsNotInKimStem(List<WsMembership> wsMemberships) {
    
    if (GrouperClientUtils.length(wsMemberships) == 0) {
      return;
    }
    
    Set<String> wsMembershipsSet = new HashSet<String>();
    List<String> wsGroupMembershipsList = new ArrayList<String>();

    Map<String, WsMembership> membershipMap = new HashMap<String, WsMembership>();
    
    //filter out the group subjects
    for (int i=0;i<GrouperClientUtils.length(wsMemberships);i++) {
      WsMembership currentMembership = wsMemberships.get(i);
      membershipMap.put(currentMembership.getMembershipId(), currentMembership);
      WsMembership wsMembership = currentMembership;
      if (GrouperClientUtils.equals("g:gsa", wsMembership.getSubjectSourceId())) {
        wsGroupMembershipsList.add(wsMembership.getMembershipId());
      } else {
        wsMembershipsSet.add(wsMembership.getMembershipId());
      }
    }
    //if no groups, all good
    if (wsGroupMembershipsList.size() == 0) {
      return;
    }

    //lookup the groups and see if in the right stem
    GcFindGroups gcFindGroups = new GcFindGroups();
    
    for (String membershipId : wsGroupMembershipsList) {
      WsMembership currentMembership = membershipMap.get(membershipId);
      String currentGroupId = currentMembership.getSubjectId();
      gcFindGroups.addGroupUuid(currentGroupId);
    }
    
    WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
    WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
    
    Map<String, WsGroup> groupLookup = new HashMap<String, WsGroup>();
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
      groupLookup.put(wsGroup.getUuid(), wsGroup);
    }
    
    String kimStem = GrouperKimUtils.kimStem() + ":";
    
    Iterator<WsMembership> iterator = wsMemberships.iterator();
    
    while (iterator.hasNext()) {
      WsMembership wsMembership = iterator.next();
      if (wsMembershipsSet.contains(wsMembership.getMembershipId())) {
        continue;
      }
      WsGroup wsGroup = groupLookup.get(wsMembership.getSubjectId());
      //if group not readable, or not in right stem, remove
      if (wsGroup == null || !wsGroup.getName().startsWith(kimStem)) {
        iterator.remove();
      }
    }
  }
  
  /**
   * sources id where non group source ids can live
   * @return the source ids, comma separated
   */
  public static String[] subjectSourceIds() {
    //lets see if there is a source to use
    String sourceIds = GrouperClientUtils.propertiesValue("grouper.kim.plugin.subjectSourceIds", false);
    sourceIds = GrouperClientUtils.isBlank(sourceIds) ? subjectSourceId() : sourceIds;
    
    String[] result = GrouperClientUtils.splitTrim(sourceIds, ",");
    
    return result;
  }
  
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
      //override e.g. for testing
      typeId = GrouperClientUtils.propertiesValue("kim.override.groupTypeId", false);
      if (GrouperClientUtils.isBlank(typeId)) {
        KimTypeInfo typeInfo = KIMServiceLocator.getTypeInfoService().getKimTypeByName("KUALI", "Default");
        typeId = typeInfo.getKimTypeId();
      }
    }
    return typeId;
  }
  
  /**
   * utility method to convert group infos to group ids
   * @param groupInfos
   * @return the list of group ids
   */
  public static List<String> convertGroupInfosToGroupIds(List<GroupInfo> groupInfos) {
    if (groupInfos == null) {
      return null;
    }
    
    List<String> groupIds = new ArrayList<String>();
    
    for (GroupInfo groupInfo : groupInfos) {
      groupIds.add(groupInfo.getGroupId());
    }
    
    return groupIds;

  }
  
  /**
   * convert a ws group to a group info
   * @param wsGroup
   * @return the group info
   */
  public static GroupInfo convertWsGroupToGroupInfo(WsGroup wsGroup) {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
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
