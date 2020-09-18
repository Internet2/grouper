package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslatorBase {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget() {
    this.translateGrouperToTarget(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects(), 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects(), false);
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTargetIncludeDeletes() {
    // note, this wont do anything in a full sync since the includeDelete objects are not there
    this.translateGrouperToTarget(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjectsIncludeDeletes(), 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes(), true);
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget(GrouperProvisioningLists grouperList, GrouperProvisioningLists targetList, boolean includeDelete) {
    

    {
      List<ProvisioningGroup> grouperProvisioningGroups = grouperList.getProvisioningGroups();
      List<ProvisioningGroup> grouperTargetGroups = translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, false);
      targetList.setProvisioningGroups(grouperTargetGroups);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = grouperList.getProvisioningEntities();
      List<ProvisioningEntity> grouperTargetEntities = translateGrouperToTargetEntities(
          grouperProvisioningEntities, includeDelete);
      targetList.setProvisioningEntities(grouperTargetEntities);
    }    

    {
      List<ProvisioningMembership> grouperProvisioningMemberships = grouperList.getProvisioningMemberships();
      List<ProvisioningMembership> grouperTargetMemberships = translateGrouperToTargetMemberships(
          grouperProvisioningMemberships, includeDelete);
      targetList.setProvisioningMemberships(grouperTargetMemberships);
    }    
    
  }

  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   */
  private static ThreadLocal<ProvisioningMembershipWrapper> provisioningMembershipWrapperThreadLocal = new InheritableThreadLocal<ProvisioningMembershipWrapper>();
  
  
  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   * @return membership wrapper
   */
  public static ProvisioningMembershipWrapper retrieveProvisioningMembershipWrapper() {
    return provisioningMembershipWrapperThreadLocal.get();
  }

  public List<ProvisioningMembership> translateGrouperToTargetMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships, boolean includeDelete) {
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
 
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();

      ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();
      grouperTargetMembership.setProvisioningMembershipWrapper(grouperProvisioningMembership.getProvisioningMembershipWrapper());
 
      provisioningMembershipWrapperThreadLocal.set(grouperProvisioningMembership.getProvisioningMembershipWrapper());
      try {
      
        for (String script: scripts) {
         
          Map<String, Object> elVariableMap = new HashMap<String, Object>();
   
          elVariableMap.put("grouperProvisioningGroup", grouperProvisioningMembership.getProvisioningGroup());
          elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
          elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
          elVariableMap.put("gcGrouperSyncGroup", provisioningGroupWrapper.getGcGrouperSyncGroup());
   
          elVariableMap.put("grouperProvisioningEntity", grouperProvisioningMembership.getProvisioningEntity());
          elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
          elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
          elVariableMap.put("gcGrouperSyncMember", provisioningEntityWrapper.getGcGrouperSyncMember());
          
          elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
          elVariableMap.put("provisioningMembershipWrapper", grouperProvisioningMembership.getProvisioningMembershipWrapper());
          elVariableMap.put("grouperTargetMembership", grouperTargetMembership);
          elVariableMap.put("gcGrouperSyncMembership", grouperProvisioningMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership());

          runScript(script, elVariableMap);
          
        }
      } finally {
        provisioningMembershipWrapperThreadLocal.remove();
      }
      if (grouperTargetMembership.isRemoveFromList()) {
        continue;
      }
      if (grouperTargetGroup != null) {
        if (!StringUtils.equals(grouperTargetGroup.getId(), grouperTargetMembership.getProvisioningGroupId())) {
          grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup.getId());
          grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
        }
      }
      
      if (grouperTargetEntity != null) {
        if (!StringUtils.equals(grouperTargetEntity.getId(), grouperTargetMembership.getProvisioningEntityId())) {
          grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity.getId());
          grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
        }
      }

      if (includeDelete) {
        grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembershipIncludeDelete(grouperTargetMembership);
      } else {
        grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembership(grouperTargetMembership);
      }

      grouperTargetMemberships.add(grouperTargetMembership); 
    }
    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete) {
    
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    if (GrouperUtil.length(scripts) == 0) {
      return grouperTargetEntities;
    }
 
    for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();
      grouperTargetEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", grouperProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("gcGrouperSyncMember", grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember());
        elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
        
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetEntity.isRemoveFromList()) {
        continue;
      }
      
      grouperTargetEntities.add(grouperTargetEntity);
      
      if (includeDelete) {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntityIncludeDelete(grouperTargetEntity);
      } else {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntity(grouperTargetEntity);
      }

    }
    return grouperTargetEntities;
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Group"));

    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("GroupCreateOnly")));
    }
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();

    if (GrouperUtil.length(scripts) == 0) {
      return grouperTargetGroups;
    }
    
    for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();
      grouperTargetGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());
      for (String script: scripts) {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", grouperProvisioningGroup.getProvisioningGroupWrapper());
        elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
        elVariableMap.put("gcGrouperSyncGroup", grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup());
        
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetGroup.isRemoveFromList()) {
        continue;
      }

      if (includeDelete) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroupIncludeDelete(grouperTargetGroup);
      } else {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroup(grouperTargetGroup);
      }
      grouperTargetGroups.add(grouperTargetGroup);
        
    }
    return grouperTargetGroups;
  }

  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    String groupIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdExpression(); 

    String groupIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdAttribute();

    String groupIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdField();

    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute) && StringUtils.isBlank(groupIdField)) {
      return;
    }
    
    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Object id = null;
      if (!StringUtils.isBlank(groupIdField)) {
        if ("id".equals(groupIdField)) {
          id = targetGroup.getId();
        } else if ("idIndex".equals(groupIdField)) {
          id = targetGroup.getIdIndex();
        } else if ("name".equals(groupIdField)) {
          id = targetGroup.getName();
        } else {
          throw new RuntimeException("Invalid groupTargetIdField, expecting id, idIndex, or name");
        }
        targetGroup.setTargetId(id);
      } else if (!StringUtils.isBlank(groupIdAttribute)) {
        Object idValue = targetGroup.retrieveAttributeValue(groupIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + groupIdAttribute + "', " + targetGroup);
        }
        id = idValue;
        targetGroup.setTargetId(id);
      } else if (!StringUtils.isBlank(groupIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetGroup", targetGroup);
        
        id = runScript(groupIdScript, elVariableMap);

        targetGroup.setTargetId(id);
                
      } else {
        throw new RuntimeException("Must have groupTargetIdField, groupTargetIdAttribute, or groupTargetIdExpression");
      }

    }
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    if (GrouperUtil.isBlank(targetEntities)) {
      return;
    }

    String entityIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdExpression(); 

    String entityIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdAttribute();

    String entityIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdField();

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute) && StringUtils.isBlank(entityIdField)) {
      return;
    }
    
    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Object id = null;
      if (!StringUtils.isBlank(entityIdField)) {
        if ("id".equals(entityIdField)) {
          id = targetEntity.getId();
        } else if ("subjectId".equals(entityIdField)) {
          id = targetEntity.getSubjectId();
        } else if ("loginId".equals(entityIdField)) {
          id = targetEntity.getLoginId();
        } else {
          throw new RuntimeException("Invalid entityTargetIdField, expecting id, subjectId, or loginId");
        }
        targetEntity.setTargetId(id);
      } else if (!StringUtils.isBlank(entityIdAttribute)) {
        Object idValue = targetEntity.retrieveAttributeValue(entityIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + entityIdAttribute + "', " + targetEntity);
        }
        id = idValue;
        targetEntity.setTargetId(id);
      } else if (!StringUtils.isBlank(entityIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetEntity", targetEntity);
        
        id = runScript(entityIdScript, elVariableMap);

        targetEntity.setTargetId(id);
                
      } else {
        throw new RuntimeException("Must have entityTargetIdField, entityTargetIdAttribute, or entityTargetIdExpression");
      }

    }
  }

  public void idTargetMemberships(List<ProvisioningMembership> targetMemberships) {

    if (GrouperUtil.isBlank(targetMemberships)) {
      return;
    }
    String membershipIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdExpression(); 

    String membershipIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdAttribute();

    String membershipIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdField();

    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute) && StringUtils.isBlank(membershipIdField)) {
      return;
    }
    
    for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      Object id = null;
      if (!StringUtils.isBlank(membershipIdField)) {
        if ("id".equals(membershipIdField)) {
          id = targetMembership.getId();
        } else if ("provisioningGroupId,provisioningMembershipId".equals(membershipIdField)) {
          id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          throw new RuntimeException("Invalid membershipTargetIdField, expecting id or 'provisioningGroupId,provisioningMembershipId'");
        }
        targetMembership.setTargetId(id);
      } else if (!StringUtils.isBlank(membershipIdAttribute)) {
        Object idValue = targetMembership.retrieveAttributeValue(membershipIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + membershipIdAttribute + "', " + targetMembership);
        }
        id = idValue;
        targetMembership.setTargetId(id);
      } else if (!StringUtils.isBlank(membershipIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetMembership", targetMembership);
        
        id = runScript(membershipIdScript, elVariableMap);

        targetMembership.setTargetId(id);
                
      } else {
        throw new RuntimeException("Must have membershipTargetIdField, membershipTargetIdAttribute, or membershipTargetIdExpression");
      }

    }

  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public Object runExpression(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public void targetIdTargetObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningMemberships());
  }
  
  public void targetIdGrouperObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());

  }
  public void targetIdGrouperObjectsIncludeDeletes() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningMemberships());

  }

}