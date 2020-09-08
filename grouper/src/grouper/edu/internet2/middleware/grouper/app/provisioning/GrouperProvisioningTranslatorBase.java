package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
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
    this.translateGrouperToTarget(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects(), 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects());
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget(GrouperProvisioningLists grouperList, GrouperProvisioningLists targetList) {
    

    {
      List<ProvisioningGroup> grouperProvisioningGroups = grouperList.getProvisioningGroups();
      List<ProvisioningGroup> grouperTargetGroups = translateGrouperToTargetGroups(grouperProvisioningGroups);
      targetList.setProvisioningGroups(grouperTargetGroups);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = grouperList.getProvisioningEntities();
      List<ProvisioningEntity> grouperTargetEntities = translateGrouperToTargetEntities(
          grouperProvisioningEntities);
      targetList.setProvisioningEntities(grouperTargetEntities);
    }    

    {
      List<ProvisioningMembership> grouperProvisioningMemberships = grouperList.getProvisioningMemberships();
      List<ProvisioningMembership> grouperTargetMemberships = translateGrouperToTargetMemberships(
          grouperProvisioningMemberships);
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
      List<ProvisioningMembership> grouperProvisioningMemberships) {
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("membership"));

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

      grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembership(grouperTargetMembership);
      grouperTargetMemberships.add(grouperTargetMembership); 
    }
    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities) {
    
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("entity"));
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
      grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntity(grouperTargetEntity);
    }
    return grouperTargetEntities;
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups) {

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("group"));

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

      grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroup(grouperTargetGroup);
      grouperTargetGroups.add(grouperTargetGroup);
        
    }
    return grouperTargetGroups;
  }

  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    String groupIdScript = this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetGroupIdExpression(); 
        
    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      // why already set???
      if (targetGroup.getTargetId() != null) {
        throw new RuntimeException("Why is target id already set???? " + targetGroup);
      }
      Object id = null;
      if (!StringUtils.isBlank(groupIdScript)) {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetGroup", targetGroup);
        
        id = runScript(groupIdScript, elVariableMap);
        
        // TODO remove from list somehow (from caller?)
//        if (targetGroup.isRemoveFromList()) {
//          continue;
//        }

        
      } else {
        id = targetGroup.getId();
      }

      targetGroup.setTargetId(id);
    }
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    String entityIdScript = this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetEntityIdExpression(); 
        
    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      // why already set???
      if (targetEntity.getTargetId() != null) {
        throw new RuntimeException("Why is target id already set???? " + targetEntity);
      }
      Object id = null;
      if (!StringUtils.isBlank(entityIdScript)) {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetEntity", targetEntity);
        
        id = runScript(entityIdScript, elVariableMap);
        
        // TODO remove from list somehow (from caller?)
//        if (targetEntity.isRemoveFromList()) {
//          continue;
//        }

        
      } else {
        id = targetEntity.getId();
      }

      targetEntity.setTargetId(id);
    }
  }

  public void idTargetMemberships(List<ProvisioningMembership> targetMemberships) {

    String membershipIdScript = this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetMembershipIdExpression(); 
        
    for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      // why already set???
      if (targetMembership.getTargetId() != null) {
        throw new RuntimeException("Why is target id already set???? " + targetMembership);
      }
      Object id = null;
      if (!StringUtils.isBlank(membershipIdScript)) {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetMembership", targetMembership);
        
        id = runScript(membershipIdScript, elVariableMap);
        
        // TODO remove from list somehow (from caller?)
//        if (targetMembership.isRemoveFromList()) {
//          continue;
//        }

        
      } else {
        
        id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        
      }

      targetMembership.setTargetId(id);
    }
  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, true);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public void targetIdTargetObjects() {
    idTargetGroups(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningMemberships());
  }
  
  public void targetIdGrouperObjects() {
    idTargetGroups(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());

  }

}