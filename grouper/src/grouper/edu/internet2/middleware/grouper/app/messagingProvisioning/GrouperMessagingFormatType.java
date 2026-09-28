package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GrouperMessagingFormatType {
  
  EsbEventJson {

    @Override
    public ObjectNode toEntityJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingEntity grouperMessagingEntity, String eventType) {
      ObjectNode result = GrouperUtil.jsonJacksonNode();
    
      GrouperUtil.jsonJacksonAssignString(result, "id", grouperMessagingEntity.getId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectId", grouperMessagingEntity.getSubjectId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectSourceId", grouperMessagingEntity.getSubjectSourceId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectIdentifier0", grouperMessagingEntity.getSubjectIdentifier0());
    
      return result;
    }

    @Override
    public ObjectNode toGroupJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingGroup grouperMessagingGroup, String eventType) {
      ObjectNode result = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(result, "id", grouperMessagingGroup.getId());
      GrouperUtil.jsonJacksonAssignString(result, "description", grouperMessagingGroup.getDescription());
      GrouperUtil.jsonJacksonAssignString(result, "displayExtension", grouperMessagingGroup.getDisplayExtension());
      GrouperUtil.jsonJacksonAssignString(result, "displayName", grouperMessagingGroup.getDisplayName());
      GrouperUtil.jsonJacksonAssignString(result, "groupId", grouperMessagingGroup.getGroupId());
      GrouperUtil.jsonJacksonAssignString(result, "groupName", grouperMessagingGroup.getGroupName());
      GrouperUtil.jsonJacksonAssignString(result, "name", grouperMessagingGroup.getName());
      GrouperUtil.jsonJacksonAssignString(result, "parentStemId", grouperMessagingGroup.getParentStemId());
    
      return result;
    }

    @Override
    public ObjectNode toMembershipJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingMembership grouperMessagingMembership, String eventType) {
      
      ObjectNode result = GrouperUtil.jsonJacksonNode();
      
      GrouperUtil.jsonJacksonAssignString(result, "id", grouperMessagingMembership.getId());
      GrouperUtil.jsonJacksonAssignString(result, "fieldId", grouperMessagingMembership.getFieldId());
      GrouperUtil.jsonJacksonAssignString(result, "fieldName", grouperMessagingMembership.getFieldName());
      GrouperUtil.jsonJacksonAssignString(result, "groupId", grouperMessagingMembership.getGroupId());
      GrouperUtil.jsonJacksonAssignString(result, "groupName", grouperMessagingMembership.getGroupName());
      GrouperUtil.jsonJacksonAssignString(result, "memberId", grouperMessagingMembership.getMemberId());
      GrouperUtil.jsonJacksonAssignString(result, "membershipType", grouperMessagingMembership.getMembershipType());
      GrouperUtil.jsonJacksonAssignString(result, "sourceId", grouperMessagingMembership.getSourceId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectId", grouperMessagingMembership.getSubjectId());
    
      return result;
    }
  },

  /**
   * the format of each message body is a map returned by a JEXL script configured for groups, entities, and/or memberships.
   * If there is no script for an object type then the EsbEventJson format is used for that object type
   */
  TranslationScript {

    @Override
    public ObjectNode toEntityJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingEntity grouperMessagingEntity, String eventType) {
      
      String script = grouperMessagingConfiguration.getEntityFormatTranslationScript();
      if (StringUtils.isBlank(script)) {
        return EsbEventJson.toEntityJson(grouperMessagingConfiguration, grouperMessagingEntity, eventType);
      }
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("eventType", eventType);
      elVariableMap.put("targetEntity", grouperMessagingEntity.getProvisioningEntity());
      
      return runTranslationScript(script, elVariableMap, "entity");
    }

    @Override
    public ObjectNode toGroupJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingGroup grouperMessagingGroup, String eventType) {
      
      String script = grouperMessagingConfiguration.getGroupFormatTranslationScript();
      if (StringUtils.isBlank(script)) {
        return EsbEventJson.toGroupJson(grouperMessagingConfiguration, grouperMessagingGroup, eventType);
      }
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("eventType", eventType);
      elVariableMap.put("targetGroup", grouperMessagingGroup.getProvisioningGroup());
      
      return runTranslationScript(script, elVariableMap, "group");
    }

    @Override
    public ObjectNode toMembershipJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingMembership grouperMessagingMembership, String eventType) {
      
      String script = grouperMessagingConfiguration.getMembershipFormatTranslationScript();
      if (StringUtils.isBlank(script)) {
        return EsbEventJson.toMembershipJson(grouperMessagingConfiguration, grouperMessagingMembership, eventType);
      }
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("eventType", eventType);
      elVariableMap.put("targetMembership", grouperMessagingMembership.getProvisioningMembership());
      elVariableMap.put("targetGroup", grouperMessagingMembership.getProvisioningMembership().getProvisioningGroup());
      elVariableMap.put("targetEntity", grouperMessagingMembership.getProvisioningMembership().getProvisioningEntity());
      
      return runTranslationScript(script, elVariableMap, "membership");
    }
  };
  
  public static GrouperMessagingFormatType valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GrouperMessagingFormatType.class, input, exceptionIfNotFound);
  }
  

  /**
   * run a format script which must return a map, and convert that map to the message body
   * @param script
   * @param elVariableMap
   * @param objectType for error messages: entity, group, or membership
   * @return the message body
   */
  private static ObjectNode runTranslationScript(String script, Map<String, Object> elVariableMap, String objectType) {
    
    Object result = null;
    
    try {
      // lenient so a null property (e.g. an entity with no email) is null in the message instead of an exception
      String scriptWithWrapper = script.contains("${") ? script : "${" + script + "}";
      result = GrouperUtil.substituteExpressionLanguageScript(scriptWithWrapper, elVariableMap, true, false, true);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", messaging " + objectType + " format script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
    
    if (!(result instanceof Map)) {
      throw new RuntimeException("Messaging " + objectType + " format script must return a map but returned: "
          + (result == null ? "null" : result.getClass().getName()) + ", script: '" + script + "'");
    }
    
    try {
      return GrouperUtil.objectMapper.valueToTree((Map<?, ?>)result);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", messaging " + objectType + " format script returned a map which cannot be converted to json, script: '" + script + "'");
      throw re;
    }
  }

  public abstract ObjectNode toEntityJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingEntity grouperMessagingEntity, String eventType);

  public abstract ObjectNode toGroupJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingGroup grouperMessagingGroup, String eventType);

  public abstract ObjectNode toMembershipJson(GrouperMessagingConfiguration grouperMessagingConfiguration, GrouperMessagingMembership grouperMessagingMembership, String eventType);
  
}
