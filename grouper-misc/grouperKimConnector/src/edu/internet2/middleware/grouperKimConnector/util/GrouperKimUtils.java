/*******************************************************************************
 * Copyright 2012 Internet2
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
/**
 * @author mchyzer
 * $Id: GrouperKimUtils.java,v 1.6 2009-12-21 06:15:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kim.bo.Person;
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
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
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
   * current date time for xslt
   * @return string of date time
   */
  public static String currentDateTime() {
    return new Date().toString();
  }

  /**
   * @param operation e.g. AttributeDefNamePicker.index
   * @param arg1name e.g. attributeDefNamePickerName
   * @param arg1value e.g. orgPicker
   * @param arg2name e.g. attributeDefNamePickerElementName
   * @param arg2value e.g. attributeDefName0
   * @param windowName e.g. orgPicker
   * @param width e.g. 700
   * @param height e.g. 500
   * @param buttonText e.g. Find org
   * @return the html button text
   * 
   */
  public static String xslGrouperButton(String operation, String arg1name, String arg1value, String arg2name,
      String arg2value, String windowName, String width, String height, String buttonText) {
    
    //  <!--  button style="white-space: nowrap;"
    //  onclick="var theWindow = window.open('http://localhost:8089/grouper/grouperUi/appHtml/grouper.html?operation=AttributeDefNamePicker.index&amp;attributeDefNamePickerName=orgPicker&amp;attributeDefNamePickerElementName=attributeDefName0','orgPicker', 'scrollbars,resizable,width=700,height=500'); theWindow.focus(); return false;"
    //>Find org</button -->

    StringBuilder result = new StringBuilder();
    
    result.append("      <button style=\"white-space: nowrap;\"\n");
    
    String fullUrl = GrouperClientUtils.propertiesValue("grouperClient.ui.url", true);
    //strip last slash
    if (fullUrl.endsWith("/")) {
      fullUrl = fullUrl.substring(0, fullUrl.length()-1);
    }
    
    result.append("      onclick=\"var theWindow = window.open('" + fullUrl 
        + "/grouperUi/appHtml/grouper.html?operation=" + operation + "&amp;" + arg1name + "=" 
        + arg1value + "&amp;" + arg2name + "=" + arg2value + "','" + windowName 
        + "', 'scrollbars,resizable,width=" + width + ",height=" + height + "'); theWindow.focus(); return false;\"\n");
    result.append(">" + buttonText + "</button>");
    return result.toString();
    
    
  }
  
  /**
   * get the doc type label for a doc type name
   * @param doctypeName
   * @return the label
   */
  public static String xslDoctypeLabel(String doctypeName) {
    DocumentTypeService documentTypeService = KEWServiceLocator.getDocumentTypeService();
    DocumentType doctype = documentTypeService.findByName(doctypeName);
    return doctype.getLabel();
  }
  
  /**
   * get the xsl principal id
   * @return the principal id
   */
  public static String xslPrincipalId() {
    UserSession userSession=UserSession.getAuthenticatedUser();
    Person user = userSession.getPerson();
    return user.getPrincipalId();
  }
  
  /**
   * get the xsl principal name
   * @return the principal name
   */
  public static String xslPrincipalName() {
    UserSession userSession=UserSession.getAuthenticatedUser();
    Person user = userSession.getPerson();
    return user.getPrincipalName();
  }
  
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
   * translate a kim group id to a grouper group name
   * @param kimGroupId
   * @return the grouper group name
   */
  public static String translateGroupName(String kimGroupId) {
    String grouperGroupName = GrouperClientUtils.propertiesValue("grouper.kim.kimGroupIdToGrouperName_" + kimGroupId, false);
    if (!GrouperClientUtils.isBlank(grouperGroupName)) {
      return grouperGroupName;
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
    String entityId = untranslatePrincipalId(wsSubject.getSourceId(), wsSubject.getId());
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
      
      //this will break routing, so see if there is a default
      if (GrouperClientUtils.isBlank(emailAddress)) {
        String defaultEmailAddress = GrouperClientUtils.propertiesValue("kuali.identity.defaultEmailAddress", false);
        //if its there use it
        if (!GrouperClientUtils.isBlank(defaultEmailAddress)) {
          emailAddress = defaultEmailAddress;
        }
      }
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
    grouperKimEntityNameInfo.setEntityNameId(untranslatePrincipalId(wsSubject.getSourceId(), wsSubject.getId()));
    
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
    
    name = GrouperClientUtils.trim(name);
    
    grouperKimEntityNameInfo.setFormattedName(name);

    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getFirstNameAttribute()) ) {
      String firstName = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getFirstNameAttribute());
      grouperKimEntityNameInfo.setFirstName(firstName);
    }
    int nameSpaceIndex = !GrouperClientUtils.isBlank(name) ? name.indexOf(' ') : -1;
    
    //if no first name, take it from name
    if (grouperKimIdentitySourceProperties != null && GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getFirstNameAttribute())
        && nameSpaceIndex != -1) {
      String firstName = name.substring(0,nameSpaceIndex);
      grouperKimEntityNameInfo.setFirstName(firstName);
    }

    if (grouperKimIdentitySourceProperties != null && !GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getLastNameAttribute()) ) {
      String lastName = subjectAttributeValue(wsSubject, attributeNames, grouperKimIdentitySourceProperties.getLastNameAttribute());
      grouperKimEntityNameInfo.setLastName(lastName);
    }

    //if no last name, take it from name
    if (grouperKimIdentitySourceProperties != null && GrouperClientUtils.isBlank(grouperKimIdentitySourceProperties.getLastNameAttribute())
        && nameSpaceIndex != -1) {
      String lastName = name.substring(nameSpaceIndex+1, name.length());
      lastName = GrouperClientUtils.trim(lastName);
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
   * use the field cache, expire every day (just to be sure no leaks)
   */
  private static ExpirableCache<Boolean, Map<String,String>> subjectIdToPrincipalIdCache = null;

  /**
   * clear this cache (e.g. for testing)
   */
  public static void subjectIdToPrincipalIdCacheClear() {
    subjectIdToPrincipalIdCache().clear();
  }

  /**
   * lazy load
   * @return field set cache
   */
  private static ExpirableCache<Boolean, Map<String,String>> subjectIdToPrincipalIdCache() {
    if (subjectIdToPrincipalIdCache == null) {
      subjectIdToPrincipalIdCache = new ExpirableCache<Boolean, Map<String,String>>(2);
    }
    return subjectIdToPrincipalIdCache;
  }

  /**
   * 
   * @return the map
   */
  private static Map<String,String> subjectIdToPrincipalIdMap() {
    Map<String,String> map = subjectIdToPrincipalIdCache().get(Boolean.TRUE);
    if (map == null) {
      
      map = new HashMap<String, String>();
      
      Properties properties = GrouperClientUtils.grouperClientProperties();

      //add test properties 
      properties.putAll(GrouperClientUtils.grouperClientOverrideMap());
      
      //reverse lookup the mapping
      //NOTE: this can be more efficient
      for (Object keyObject : properties.keySet()) {
        String keyString = GrouperClientUtils.trimToEmpty((String)keyObject);
        if (keyString.startsWith("grouper.kim.kimPrincipalIdToSubjectId_")) {
          map.put(properties.getProperty(keyString), 
              keyString.substring("grouper.kim.kimPrincipalIdToSubjectId_".length(), keyString.length()));
        }
      }
      
      subjectIdToPrincipalIdCache().put(Boolean.TRUE, map);
    }
    return map;
  }
  
  /**
   * convert ws subject to string for log
   * @param wsSubject
   * @param attributeNames
   * @return the string
   */
  public static String convertWsSubjectToStringForLog(WsSubject wsSubject, String[] attributeNames) {
    
    if (wsSubject == null) {
      return "Subject: null";
    }
    
    StringBuilder result = new StringBuilder();
    result.append("Subject: id: ").append(wsSubject.getId());
    if (!GrouperClientUtils.isBlank(wsSubject.getName())) {
      result.append(", name: ").append(wsSubject.getName());
    }
    if (!GrouperClientUtils.isBlank(wsSubject.getIdentifierLookup())) {
      result.append("\nidentifier lookup: ").append(wsSubject.getIdentifierLookup());
    }
    for (String attributeName : GrouperClientUtils.nonNull(attributeNames, String.class)) {
      result.append("\n").append(attributeName).append(": ").append(subjectAttributeValue(wsSubject, attributeNames, attributeName));
    }
    return result.toString();
  }
  
  /**
   * translate a kim principal id to a grouper subject id.  perhaps cut off source prefix
   * @param kimPrincipalId
   * @return the grouper subject id
   */
  public static String translatePrincipalId(String kimPrincipalId) {
    String grouperSubjectId = GrouperClientUtils.propertiesValue("grouper.kim.kimPrincipalIdToSubjectId_" + kimPrincipalId, false);
    if (!GrouperClientUtils.isBlank(grouperSubjectId)) {
      return grouperSubjectId;
    }
    kimPrincipalId = separateSourceIdSuffix(kimPrincipalId);
    return kimPrincipalId;
  }
  
  /**
   * translate a kim principal name to a grouper subject identifier
   * @param kimPrincipalName
   * @return the grouper subject identifier or a sourceId/subject identifier with separator
   */
  public static String translatePrincipalName(String kimPrincipalName) {
    String grouperSubjectIdentifier = GrouperClientUtils.propertiesValue("grouper.kim.kimPrincipalNameToSubjectIdentifier_" + kimPrincipalName, false);
    if (!GrouperClientUtils.isBlank(grouperSubjectIdentifier)) {
      return grouperSubjectIdentifier;
    }
    return kimPrincipalName;
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
    kimPrincipalInfo.setEntityId(untranslatePrincipalId(wsSubject.getSourceId(), wsSubject.getId()));
    kimPrincipalInfo.setPrincipalId(kimPrincipalInfo.getEntityId());
    String principalName = convertWsSubjectToPrincipalName(wsSubject, attributeNames);
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

  /**
   * untranslate a kim principal id from a grouper subject identifier
   * @param sourceId
   * @param subjectId
   * @return the kim entity id from the grouper subject id
   */
  public static String untranslatePrincipalId(String sourceId, String subjectId) {
    
    Map<String,String> subjectIdToEntityIdMap = subjectIdToPrincipalIdMap();
    
    //if we are translating, do that here
    if (subjectIdToEntityIdMap.containsKey(subjectId)) {
      return subjectIdToEntityIdMap.get(subjectId);
    }

    if (GrouperClientUtils.propertiesValueBoolean("kuali.identity.ignoreSourceAppend", false, false)) {
      return subjectId;
    }
    
    //see if we are ignoring concatenating this one
    String ignoreIdsString = GrouperClientUtils.propertiesValue(
        "kuali.identity.ignoreSourceAppend.principalIds", false);
    if (!GrouperClientUtils.isBlank(ignoreIdsString)) {
      String[] ignoreIds = GrouperClientUtils.splitTrim(ignoreIdsString, ",");
      if (GrouperClientUtils.contains(ignoreIds, subjectId)) {
        return subjectId;
      }
    }
    
    if (GrouperClientUtils.equals("g:gsa", sourceId)) {
      return subjectId;
    }
    
    //if they are equal, then append the source
    return GrouperKimUtils.concatenateSourceSeparator(sourceId,
        subjectId);
  }

}
