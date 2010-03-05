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

import org.kuali.rice.kim.bo.entity.dto.KimEntityDefaultInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityEntityTypeDefaultInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityEntityTypeInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityExternalIdentifierInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNamePrincipalNameInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.bo.entity.impl.KimEntityEmailImpl;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.reference.dto.EmailTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.EntityTypeInfo;
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
import edu.internet2.middleware.grouperKimConnector.identity.GrouperKimEntityNameInfo;
import edu.internet2.middleware.grouperKimConnector.identity.GrouperKimIdentitySourceProperties;


/**
 * utility methods for grouper kim integration
 */
public class GrouperKimUtils {

  /**
   * get the first name from the name.  If the name has a space, do stuff before first space
   * note, if there is only one name, that goes in the last name with blank first name
   * @param name
   * @return the first name
   */
  public static String firstName(String name) {
    if (GrouperClientUtils.isBlank(name)) {
      return name;
    }
    name = name.trim();
    int spaceIndex = name.indexOf(' ');
    if (spaceIndex == -1) {
      // note, if there is only one name, that goes in the last name with blank first name
      return null;
    }
    return name.substring(0,spaceIndex);
  }
  
  /**
   * get the last name from the name.  If the name has a space, do stuff after last space
   * @param name
   * @return the last name
   */
  public static String lastName(String name) {
    if (GrouperClientUtils.isBlank(name)) {
      return name;
    }
    name = name.trim();
    int spaceIndex = name.lastIndexOf(' ');
    if (spaceIndex == -1) {
      // note, if there is only one name, that goes in the last name with blank first name
      return name;
    }
    return name.substring(spaceIndex+1,name.length());
  }
  
  /**
   * get the middle name from the name.  If the name has two spaces, do stuff between the first and last names
   * note this isnt precise, e.g. someone could have a spac in the last name
   * @param name
   * @return the last name
   */
  public static String middleName(String name) {
    if (GrouperClientUtils.isBlank(name)) {
      return name;
    }
    name = name.trim();
    int spaceIndex = name.indexOf(' ');
    if (spaceIndex == -1) {
      return null;
    }
    String lastPart = name.substring(spaceIndex+1,name.length());
    
    if (GrouperClientUtils.isBlank(lastPart)) {
      return null;
    }
    lastPart = lastPart.trim();
    spaceIndex = lastPart.lastIndexOf(' ');
    if (spaceIndex == -1) {
      return null;
    }
    return lastPart.substring(0, spaceIndex);
  }
  
  /**
   * translate a kim group id to a grouper group id
   * @param kimGroupId
   * @return the grouper group id
   */
  public static String translateGroupId(String kimGroupId) {
    String grouperGroupId = GrouperClientUtils.propertiesValue("grouper.kim.kimGroupIdToGrouperId_" + kimGroupId, false);
    if (!GrouperClientUtils.isBlank(grouperGroupId)) {
      return grouperGroupId;
    }
    return kimGroupId;
  }

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
   * get the attribute value of an attribute name of a subject
   * @param wsSubject subject
   * @param attributeNames list of attribute names in the subject
   * @param attributeName to query
   * @return the value or null
   */
  public static String subjectAttributeValue(WsSubject wsSubject, String[] attributeNames, String attributeName) {
    for (int i=0;i<GrouperClientUtils.length(attributeNames);i++) {
      
      if (GrouperClientUtils.equals(attributeName, attributeNames[i])) {
        //got it
        return wsSubject.getAttributeValue(i);
      }
    }
    return null;
  }
  
  
  /**
   * convert a ws subject to an entity default info
   * @param wsSubject
   * @param attributeNames list of names in the 
   * @return the entity name info
   */
  public static KimEntityDefaultInfo convertWsSubjectToEntityDefaultInfo(WsSubject wsSubject, String[] attributeNames) {
    KimEntityDefaultInfo kimEntityDefaultInfo = new KimEntityDefaultInfo();
    kimEntityDefaultInfo.setActive(true);
    kimEntityDefaultInfo.setAffiliations(null);
    kimEntityDefaultInfo.setDefaultAffiliation(null);
    
    
    kimEntityDefaultInfo.setDefaultName(convertWsSubjectToEntityNameInfo(wsSubject, attributeNames));
    String entityId = concatenateSourceSeparator(wsSubject.getSourceId(), wsSubject.getId());
    kimEntityDefaultInfo.setEntityId(entityId);
    
    GrouperKimIdentitySourceProperties grouperKimIdentitySourceProperties = GrouperKimIdentitySourceProperties
      .grouperKimIdentitySourceProperties(wsSubject.getSourceId());
    
    String entityTypeCode = "PERSON";
    
    //see if overridden
    if (!GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getEntityTypeCode())) {
      entityTypeCode = grouperKimIdentitySourceProperties.getEntityTypeCode();
    }
    
    KimEntityEntityTypeDefaultInfo kimEntityEntityTypeDefaultInfo = new KimEntityEntityTypeDefaultInfo();
    kimEntityEntityTypeDefaultInfo.setEntityTypeCode(entityTypeCode);
    
    String emailAttribute = grouperKimIdentitySourceProperties.getEmailAttribute();
    if (!GrouperClientUtils.isBlank(emailAttribute)) {
      String emailAddress = GrouperKimUtils.subjectAttributeValue(wsSubject, attributeNames, emailAttribute);
      if (!GrouperClientUtils.isBlank(emailAddress)) {
        KimEntityEmailImpl kimEntityEmailImpl = new KimEntityEmailImpl();
        kimEntityEmailImpl.setActive(true);
        kimEntityEmailImpl.setDefault(true);
        kimEntityEmailImpl.setEmailAddress(emailAddress);
        EmailTypeInfo emailTypeInfo = KIMServiceLocator.getIdentityManagementService().getEmailType("WRK");
        kimEntityEmailImpl.setEmailType(emailTypeInfo);
        kimEntityEmailImpl.setEmailTypeCode("WRK");
        kimEntityEmailImpl.setEntityId(entityId);
        kimEntityEmailImpl.setEntityTypeCode(entityTypeCode);
        kimEntityEntityTypeDefaultInfo.setDefaultEmailAddress(kimEntityEmailImpl);
        kimEntityEntityTypeDefaultInfo.setEntityTypeCode(entityTypeCode);
      }
    }
    
    kimEntityDefaultInfo.setEntityTypes(GrouperClientUtils.toList(kimEntityEntityTypeDefaultInfo));
    
    //this is important, this cannot return null or will give an NPE...
    kimEntityDefaultInfo.setExternalIdentifiers(new ArrayList<KimEntityExternalIdentifierInfo>());
    kimEntityDefaultInfo.setPrimaryEmployment(null);
    
    List<KimPrincipalInfo> kimPrincipalInfos = new ArrayList<KimPrincipalInfo>();
    kimPrincipalInfos.add(convertWsSubjectToPrincipalInfo(wsSubject, attributeNames));
    
    kimEntityDefaultInfo.setPrincipals(kimPrincipalInfos);
    kimEntityDefaultInfo.setPrivacyPreferences(null);
    return kimEntityDefaultInfo;
    
  }
  
  /**
   * convert a default info to an info
   * @param kimEntityDefaultInfo
   * @return the info
   */
  public static KimEntityInfo convertKimEntityDefaultInfoToKimEntityInfo(KimEntityDefaultInfo kimEntityDefaultInfo) {
    if (kimEntityDefaultInfo == null) {
      return null;
    }
    KimEntityInfo kimEntityInfo = new KimEntityInfo();
    
    kimEntityInfo.setActive(kimEntityDefaultInfo.isActive());
    kimEntityInfo.setAffiliations(kimEntityDefaultInfo.getAffiliations());
    kimEntityInfo.setBioDemographics(null);
    kimEntityInfo.setCitizenships(null);
    kimEntityInfo.setDefaultAffiliation(kimEntityDefaultInfo.getDefaultAffiliation());
    kimEntityInfo.setDefaultName(kimEntityDefaultInfo.getDefaultName());
    kimEntityInfo.setEmploymentInformation(null);
    kimEntityInfo.setEntityId(kimEntityDefaultInfo.getEntityId());
    
    if (kimEntityDefaultInfo.getEntityTypes() != null) {
      List<KimEntityEntityTypeInfo> kimEntityEntityTypeInfos = new ArrayList<KimEntityEntityTypeInfo>();
      kimEntityInfo.setEntityTypes(kimEntityEntityTypeInfos);

      for (KimEntityEntityTypeDefaultInfo kimEntityEntityTypeDefaultInfo : kimEntityDefaultInfo.getEntityTypes()) {
        KimEntityEntityTypeInfo kimEntityEntityTypeInfo = new KimEntityEntityTypeInfo();
        kimEntityEntityTypeInfos.add(kimEntityEntityTypeInfo);
        
        kimEntityEntityTypeInfo.setActive(true);
        kimEntityEntityTypeInfo.setAddresses(kimEntityEntityTypeDefaultInfo.getDefaultAddress() == null ? null 
            : GrouperClientUtils.toList(kimEntityEntityTypeDefaultInfo.getDefaultAddress()));
        kimEntityEntityTypeInfo.setDefaultAddress(kimEntityEntityTypeDefaultInfo.getDefaultAddress());
        kimEntityEntityTypeInfo.setDefaultEmailAddress(kimEntityEntityTypeDefaultInfo.getDefaultEmailAddress());
        kimEntityEntityTypeInfo.setDefaultPhoneNumber(kimEntityEntityTypeDefaultInfo.getDefaultPhoneNumber());
        kimEntityEntityTypeInfo.setEmailAddresses(kimEntityEntityTypeDefaultInfo.getDefaultEmailAddress() == null ? null 
            : GrouperClientUtils.toList(kimEntityEntityTypeDefaultInfo.getDefaultEmailAddress()));
        
        EntityTypeInfo entityTypeInfo = KIMServiceLocator.getIdentityManagementService()
          .getEntityType(kimEntityEntityTypeDefaultInfo.getEntityTypeCode());
        
        kimEntityEntityTypeInfo.setEntityType(entityTypeInfo);
        kimEntityEntityTypeInfo.setEntityTypeCode(kimEntityEntityTypeDefaultInfo.getEntityTypeCode());
        kimEntityEntityTypeInfo.setPhoneNumbers(kimEntityEntityTypeDefaultInfo.getDefaultPhoneNumber() == null ? null 
            : GrouperClientUtils.toList(kimEntityEntityTypeDefaultInfo.getDefaultPhoneNumber()));
            
      }
      
    }
    
    kimEntityInfo.setEthnicities(null);
    kimEntityInfo.setExternalIdentifiers(kimEntityDefaultInfo.getExternalIdentifiers());
    kimEntityInfo.setNames(GrouperClientUtils.toList(kimEntityDefaultInfo.getDefaultName()));
    kimEntityInfo.setPrimaryEmployment(kimEntityDefaultInfo.getPrimaryEmployment());
    kimEntityInfo.setPrincipals(kimEntityDefaultInfo.getPrincipals());
    kimEntityInfo.setPrivacyPreferences(kimEntityDefaultInfo.getPrivacyPreferences());
    kimEntityInfo.setResidencies(null);
    kimEntityInfo.setVisas(null);
    return kimEntityInfo;
  }
  
  
  /**
   * convert a ws subject to an entity name info
   * @param wsSubject
   * @param attributeNames list of names in the 
   * @return the entity name info
   */
  public static KimEntityNameInfo convertWsSubjectToEntityNameInfo(WsSubject wsSubject, String[] attributeNames) {

    GrouperKimEntityNameInfo grouperKimEntityNameInfo = new GrouperKimEntityNameInfo();
    
    grouperKimEntityNameInfo.setActive(true);
    grouperKimEntityNameInfo.setDefault(true);
    grouperKimEntityNameInfo.setEntityNameId(concatenateSourceSeparator(wsSubject.getSourceId(), wsSubject.getId()));
    
//    Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
//    
//    substituteMap.put("grouperClientUtils", new GrouperClientUtils());
//
//    String outputTemplate = GrouperClientUtils.propertiesValue("webService.addMember.output", true);
//      
//    String output = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);

    GrouperKimIdentitySourceProperties grouperKimIdentitySourceProperties = GrouperKimIdentitySourceProperties
      .grouperKimIdentitySourceProperties(wsSubject.getSourceId());
    
    String name = null;
    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getNameAttribute()) ) {
      name = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getNameAttribute());
    }
    if (GrouperClientUtils.isBlank(name)) {
      name = wsSubject.getName();
    }
    grouperKimEntityNameInfo.setFormattedName(name);

    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getFirstNameAttribute()) ) {
      String firstName = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getFirstNameAttribute());
      grouperKimEntityNameInfo.setFirstName(firstName);
    }

    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getLastNameAttribute()) ) {
      String lastName = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getLastNameAttribute());
      grouperKimEntityNameInfo.setLastName(lastName);
    }

    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getMiddleNameAttribute()) ) {
      String middleName = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getMiddleNameAttribute());
      grouperKimEntityNameInfo.setMiddleName(middleName);
    }

    return grouperKimEntityNameInfo;
    
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

  /**
   * translate a kim entity id to a grouper subject id
   * @param kimEntityId
   * @return the grouper subject id or source/subject combined with separator
   */
  public static String translateEntityId(String kimEntityId) {
    String grouperSubjectId = GrouperClientUtils.propertiesValue("grouper.kim.kimEntityIdToSubjectId_" + kimEntityId, false);
    if (!GrouperClientUtils.isBlank(grouperSubjectId)) {
      return grouperSubjectId;
    }
    return kimEntityId;
  }
  
  /**
   * translate a kim principal id to a grouper subject identifier
   * @param kimPrincipalId
   * @return the grouper subject identifier or a sourceId/subject identifier with separator
   */
  public static String translatePrincipalId(String kimPrincipalId) {
    String grouperSubjectIdentifier = GrouperClientUtils.propertiesValue("grouper.kim.kimPrincipalIdToSubjectIdentifier_" + kimPrincipalId, false);
    if (!GrouperClientUtils.isBlank(grouperSubjectIdentifier)) {
      return grouperSubjectIdentifier;
    }
    return kimPrincipalId;
  }
  
  /**
   * translate a kim principal name to a grouper subject identifier
   * @param kimPrincipalId
   * @return the grouper subject identifier or a sourceId/subject identifier with separator
   */
  public static String translatePrincipalName(String kimPrincipalId) {
    String grouperSubjectIdentifier = GrouperClientUtils.propertiesValue("grouper.kim.kimPrincipalNameToSubjectIdentifier_" + kimPrincipalId, false);
    if (!GrouperClientUtils.isBlank(grouperSubjectIdentifier)) {
      return grouperSubjectIdentifier;
    }
    return kimPrincipalId;
  }
  
  /**
   * if the input is sourceId::::entityId and the separator is :::: then return sourceId.
   * If there is no separator configured, or none in entityId, then return the entityId
   * @param entityId
   * @return the sourceId
   */
  public static String separateSourceId(String entityId) {
    if (GrouperClientUtils.isBlank(entityId)) {
      return entityId;
    }
    String separator = GrouperClientUtils.propertiesValue("kuali.identity.sourceSeparator", false);
    if (GrouperClientUtils.isBlank(separator)) {
      return null;
    }
    
    int separatorIndex = entityId.indexOf(separator);
    
    if (separatorIndex == -1) {
      return null;
    }
    
    String sourceId = entityId.substring(0, separatorIndex);
    
    return sourceId;
    
  }
  
  /**
   * if the input is sourceId::::entityId and the separator is :::: then return sourceId.
   * If there is no separator configured, or none in entityId, then return the entityId
   * @param entityId
   * @return the sourceId
   */
  public static String separateSourceIdSuffix(String entityId) {
    
    if (GrouperClientUtils.isBlank(entityId)) {
      return entityId;
    }
    String separator = GrouperClientUtils.propertiesValue("kuali.identity.sourceSeparator", false);
    if (GrouperClientUtils.isBlank(separator)) {
      return entityId;
    }
    
    int separatorIndex = entityId.indexOf(separator);
    
    if (separatorIndex == -1) {
      return entityId;
    }
    
    String suffixId = entityId.substring(separatorIndex + separator.length(), entityId.length());
    
    return suffixId;

  }
  
  
  /**
   * convert a ws subject to an entity name info
   * @param wsSubject
   * @param attributeNames list of names in the 
   * @return the entity name info
   */
  public static KimEntityNamePrincipalNameInfo convertWsSubjectToPrincipalNameInfo(WsSubject wsSubject, String[] attributeNames) {

    KimEntityNamePrincipalNameInfo kimEntityNamePrincipalNameInfo = new KimEntityNamePrincipalNameInfo();

    String identifier = convertWsSubjectToPrincipalName(wsSubject, attributeNames);
    
    kimEntityNamePrincipalNameInfo.setPrincipalName(identifier);
    
    kimEntityNamePrincipalNameInfo.setDefaultEntityName(convertWsSubjectToEntityNameInfo(wsSubject, attributeNames));
    
    return kimEntityNamePrincipalNameInfo;
    
  }

  /**
   * if there is a source separator configured, and the other stuff is there, concatenate
   * @param sourceId
   * @param suffix
   * @return the concatenation
   */
  public static String concatenateSourceSeparator(String sourceId, String suffix) {
    String separator = GrouperClientUtils.propertiesValue("kuali.identity.sourceSeparator", false);
    String result = suffix;
    if (!GrouperClientUtils.isBlank(separator) && !GrouperClientUtils.isBlank(sourceId)) {
      result = sourceId + separator + suffix;
    }
    return result;
  }
  
  /**
   * convert a ws subject to an entity name info
   * @param wsSubject
   * @param attributeNames list of names in the 
   * @return the entity name info
   */
  public static KimPrincipalInfo convertWsSubjectToPrincipalInfo(WsSubject wsSubject, String[] attributeNames) {

    KimPrincipalInfo kimPrincipalInfo = new KimPrincipalInfo();
    kimPrincipalInfo.setActive(true);
    kimPrincipalInfo.setEntityId(concatenateSourceSeparator(wsSubject.getSourceId(), wsSubject.getId()));
    String principalName = convertWsSubjectToPrincipalName(wsSubject, attributeNames);
    kimPrincipalInfo.setPrincipalId(concatenateSourceSeparator(wsSubject.getSourceId(), principalName));
    kimPrincipalInfo.setPrincipalName(principalName);
    return kimPrincipalInfo;
  }

  /**
   * 
   * @param wsSubject
   * @param attributeNames
   * @return the principal
   */
  public static String convertWsSubjectToPrincipalName(WsSubject wsSubject, String[] attributeNames) {
    //NOTE if this isnt here, get from attribute
    String identifier = wsSubject.getIdentifierLookup();
    

    GrouperKimIdentitySourceProperties grouperKimIdentitySourceProperties = GrouperKimIdentitySourceProperties
      .grouperKimIdentitySourceProperties(wsSubject.getSourceId());
    
    if (GrouperClientUtils.isBlank(identifier) && grouperKimIdentitySourceProperties != null 
        && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getIdentifierAttribute()) ) {
     identifier = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getIdentifierAttribute());
    }
    return identifier;
  }
    
}
