package edu.internet2.middleware.grouper.app.messagingProvisioning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GrouperMessagingFormatType {
  
  EsbEventJson {

    @Override
    public ObjectNode toEntityJson(GrouperMessagingEntity grouperMessagingEntity) {
      ObjectNode result = GrouperUtil.jsonJacksonNode();
    
      GrouperUtil.jsonJacksonAssignString(result, "id", grouperMessagingEntity.getId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectId", grouperMessagingEntity.getSubjectId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectSourceId", grouperMessagingEntity.getSubjectSourceId());
      GrouperUtil.jsonJacksonAssignString(result, "subjectIdentifier0", grouperMessagingEntity.getSubjectIdentifier0());
    
      return result;
    }

    @Override
    public ObjectNode toGroupJson(GrouperMessagingGroup grouperMessagingGroup) {
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
    public ObjectNode toMembershipJson(GrouperMessagingMembership grouperMessagingMembership) {
      
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
  };
  
  public static GrouperMessagingFormatType valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GrouperMessagingFormatType.class, input, exceptionIfNotFound);
  }
  
  public abstract ObjectNode toEntityJson(GrouperMessagingEntity grouperMessagingEntity);

  public abstract ObjectNode toGroupJson(GrouperMessagingGroup grouperMessagingGroup);

  public abstract ObjectNode toMembershipJson(GrouperMessagingMembership grouperMessagingMembership);
  
}
