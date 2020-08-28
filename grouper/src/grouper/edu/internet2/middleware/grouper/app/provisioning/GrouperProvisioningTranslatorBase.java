package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

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
  public void translateGrouperToCommon() {
    this.translateGrouperToCommon(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects(), 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects());
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateTargetToCommon() {
    this.translateTargetToCommon(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects(), 
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetCommonObjects());
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToCommon(GrouperProvisioningLists grouperList, GrouperProvisioningLists commonList) {
    

    {
      List<ProvisioningGroup> grouperProvisioningGroups = grouperList.getProvisioningGroups();
      List<ProvisioningGroup> grouperCommonGroups = translateGrouperToCommonGroups(grouperProvisioningGroups);
      commonList.setProvisioningGroups(grouperCommonGroups);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = grouperList.getProvisioningEntities();
      List<ProvisioningEntity> grouperCommonEntities = translateGrouperToCommonEntities(
          grouperProvisioningEntities);
      commonList.setProvisioningEntities(grouperCommonEntities);
    }    

    {
      List<ProvisioningMembership> grouperProvisioningMemberships = grouperList.getProvisioningMemberships();
      List<ProvisioningMembership> grouperCommonMemberships = translateGrouperToCommonMemberships(
          grouperProvisioningMemberships);
      commonList.setProvisioningMemberships(grouperCommonMemberships);
    }    
    
  }

  public List<ProvisioningMembership> translateGrouperToCommonMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships) {
    List<ProvisioningMembership> grouperCommonMemberships = new ArrayList<ProvisioningMembership>();
    
    int commonMshipsWithNullIds = 0;
    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperCommonGroup = provisioningGroupWrapper.getGrouperCommonGroup();
 
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperCommonEntity = provisioningEntityWrapper.getGrouperCommonEntity();

      if (StringUtils.isBlank(grouperCommonGroup.getId()) || StringUtils.isBlank(grouperCommonEntity.getId())) {
        commonMshipsWithNullIds++;
        continue;
      }

      ProvisioningMembership grouperCommonMembership = new ProvisioningMembership();
      grouperCommonMembership.setProvisioningMembershipWrapper(grouperProvisioningMembership.getProvisioningMembershipWrapper());
      grouperCommonMembership.getProvisioningMembershipWrapper().setGrouperCommonMembership(grouperCommonMembership);
 
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("membership")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
 
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("grouperCommonGroup", grouperCommonGroup);
        elVariableMap.put("gcGrouperSyncGroup", provisioningGroupWrapper.getGcGrouperSyncGroup());
 
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("grouperCommonEntity", grouperCommonEntity);
        elVariableMap.put("gcGrouperSyncMember", provisioningEntityWrapper.getGcGrouperSyncMember());
        
        elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", grouperProvisioningMembership.getProvisioningMembershipWrapper());
        elVariableMap.put("grouperCommonMembership", grouperCommonMembership);
        elVariableMap.put("gcGrouperSyncMembership", grouperProvisioningMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.equals(grouperCommonGroup.getId(), grouperCommonMembership.getProvisioningGroupId())) {
        grouperCommonMembership.setProvisioningGroupId(grouperCommonGroup.getId());
        grouperCommonMembership.setProvisioningGroup(grouperCommonGroup);
      }
 
      if (!StringUtils.equals(grouperCommonEntity.getId(), grouperCommonMembership.getProvisioningEntityId())) {
        grouperCommonMembership.setProvisioningEntityId(grouperCommonEntity.getId());
        grouperCommonMembership.setProvisioningEntity(grouperCommonEntity);
      }
 
      grouperCommonMemberships.add(grouperCommonMembership); 
      
    }
    if (commonMshipsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperCommonMshipsWithNullIds", commonMshipsWithNullIds);
    }
    return grouperCommonMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToCommonEntities(
      List<ProvisioningEntity> grouperProvisioningEntities) {
    
    List<ProvisioningEntity> grouperCommonEntities = new ArrayList<ProvisioningEntity>();
    
    int commonEntitiesWithNullIds = 0;
 
    for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperCommonEntity = new ProvisioningEntity();
      grouperCommonEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());
      grouperCommonEntity.getProvisioningEntityWrapper().setGrouperCommonEntity(grouperCommonEntity);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("entity")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", grouperProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("grouperCommonEntity", grouperCommonEntity);
        elVariableMap.put("gcGrouperSyncMember", grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      if (!StringUtils.isBlank(grouperCommonEntity.getId())) {
        grouperCommonEntities.add(grouperCommonEntity); 
      } else {
        commonEntitiesWithNullIds++;
      }
    }
    if (commonEntitiesWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperCommonEntitiesWithNullIds", commonEntitiesWithNullIds);
    }
    return grouperCommonEntities;
  }

  public List<ProvisioningGroup> translateGrouperToCommonGroups(List<ProvisioningGroup> grouperProvisioningGroups) {
    
    List<ProvisioningGroup> grouperCommonGroups = new ArrayList<ProvisioningGroup>();

    int commonGroupsWithNullIds = 0;
    for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperCommonGroup = new ProvisioningGroup();
      grouperCommonGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());
      grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperCommonGroup(grouperCommonGroup);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("group")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", grouperProvisioningGroup.getProvisioningGroupWrapper());
        elVariableMap.put("grouperCommonGroup", grouperCommonGroup);
        elVariableMap.put("gcGrouperSyncGroup", grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.isBlank(grouperCommonGroup.getId())) {
        grouperCommonGroups.add(grouperCommonGroup);
      } else {
        commonGroupsWithNullIds++;
      }
    }
    if (commonGroupsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperCommonGroupsWithNullIds", commonGroupsWithNullIds);
    }
    return grouperCommonGroups;
  }

  
  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateTargetToCommon(GrouperProvisioningLists targetList, GrouperProvisioningLists commonList) {
    

    {
      List<ProvisioningGroup> targetProvisioningGroups = targetList.getProvisioningGroups();
      List<ProvisioningGroup> targetCommonGroups = translateTargetToCommonGroups(targetProvisioningGroups);
      commonList.setProvisioningGroups(targetCommonGroups);
    }
    
    {
      List<ProvisioningEntity> targetProvisioningEntities = targetList.getProvisioningEntities();
      List<ProvisioningEntity> targetCommonEntities = translateTargetToCommonEntities(
          targetProvisioningEntities);
      commonList.setProvisioningEntities(targetCommonEntities);
    }    

    {
      List<ProvisioningMembership> targetProvisioningMemberships = targetList.getProvisioningMemberships();
      List<ProvisioningMembership> targetCommonMemberships = translateTargetToCommonMemberships(
          targetProvisioningMemberships);
      commonList.setProvisioningMemberships(targetCommonMemberships);
    }    
    
  }

  public List<ProvisioningMembership> translateTargetToCommonMemberships(
      List<ProvisioningMembership> targetProvisioningMemberships) {
    List<ProvisioningMembership> targetCommonMemberships = new ArrayList<ProvisioningMembership>();
    int commonMshipsWithNullIds = 0;
    for (ProvisioningMembership targetProvisioningMembership: GrouperUtil.nonNull(targetProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = targetProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup targetCommonGroup = provisioningGroupWrapper.getTargetCommonGroup();
 
      ProvisioningEntityWrapper provisioningEntityWrapper = targetProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity targetCommonEntity = provisioningEntityWrapper.getTargetCommonEntity();

      if (StringUtils.isBlank(targetCommonGroup.getId()) || StringUtils.isBlank(targetCommonEntity.getId())) {
        commonMshipsWithNullIds++;
        continue;
      }

      ProvisioningMembership targetCommonMembership = new ProvisioningMembership();
      targetCommonMembership.setProvisioningMembershipWrapper(targetProvisioningMembership.getProvisioningMembershipWrapper());
      targetCommonMembership.getProvisioningMembershipWrapper().setTargetCommonMembership(targetCommonMembership);
 
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("membership")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
 
        elVariableMap.put("targetProvisioningGroup", targetProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("targetCommonGroup", targetCommonGroup);
 
        elVariableMap.put("targetProvisioningEntity", targetProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("targetCommonEntity", targetCommonEntity);
        
        elVariableMap.put("targetProvisioningMembership", targetProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", targetProvisioningMembership.getProvisioningMembershipWrapper());
        elVariableMap.put("targetCommonMembership", targetCommonMembership);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.equals(targetCommonGroup.getId(), targetCommonMembership.getProvisioningGroupId())) {
        targetCommonMembership.setProvisioningGroupId(targetCommonGroup.getId());
        targetCommonMembership.setProvisioningGroup(targetCommonGroup);
      }
 
      if (!StringUtils.equals(targetCommonEntity.getId(), targetCommonMembership.getProvisioningEntityId())) {
        targetCommonMembership.setProvisioningEntityId(targetCommonEntity.getId());
        targetCommonMembership.setProvisioningEntity(targetCommonEntity);
      }
 
      targetCommonMemberships.add(targetCommonMembership); 
      
    }
    if (commonMshipsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonMshipsWithNullIds", commonMshipsWithNullIds);
    }
    return targetCommonMemberships;
  }

  public List<ProvisioningEntity> translateTargetToCommonEntities(
      List<ProvisioningEntity> targetProvisioningEntities) {
    List<ProvisioningEntity> targetCommonEntities = new ArrayList<ProvisioningEntity>();

    int commonEntitiesWithNullIds = 0;
 
    for (ProvisioningEntity targetProvisioningEntity: GrouperUtil.nonNull(targetProvisioningEntities)) {
      
      ProvisioningEntity targetCommonEntity = new ProvisioningEntity();
      targetCommonEntity.setProvisioningEntityWrapper(targetProvisioningEntity.getProvisioningEntityWrapper());
      targetCommonEntity.getProvisioningEntityWrapper().setTargetCommonEntity(targetCommonEntity);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("entity")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningEntity", targetProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", targetProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("targetCommonEntity", targetCommonEntity);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      if (!StringUtils.isBlank(targetCommonEntity.getId())) {
        targetCommonEntities.add(targetCommonEntity); 
      } else {
        commonEntitiesWithNullIds++;
      }
    }
    if (commonEntitiesWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonEntitiesWithNullIds", commonEntitiesWithNullIds);
    }
    return targetCommonEntities;
  }

  public List<ProvisioningGroup> translateTargetToCommonGroups(
      List<ProvisioningGroup> targetProvisioningGroups) {
    List<ProvisioningGroup> targetCommonGroups = new ArrayList<ProvisioningGroup>();
    
    int commonGroupsWithNullIds = 0;
    for (ProvisioningGroup targetProvisioningGroup: GrouperUtil.nonNull(targetProvisioningGroups)) {
      
      ProvisioningGroup targetCommonGroup = new ProvisioningGroup();
      targetCommonGroup.setProvisioningGroupWrapper(targetProvisioningGroup.getProvisioningGroupWrapper());
      targetProvisioningGroup.getProvisioningGroupWrapper().setTargetCommonGroup(targetCommonGroup);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("group")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningGroup", targetProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", targetProvisioningGroup.getProvisioningGroupWrapper());
        elVariableMap.put("targetCommonGroup", targetCommonGroup);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.isBlank(targetCommonGroup.getId())) {
        targetCommonGroups.add(targetCommonGroup);
      } else {
        commonGroupsWithNullIds++;
      }
    }
    if (commonGroupsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonGroupsWithNullIds", commonGroupsWithNullIds);
    }
    return targetCommonGroups;
  }

  public List<ProvisioningEntity> translateCommonToTargetEntities(
      List<ProvisioningEntity> targetProvisioningEntities) {
    List<ProvisioningEntity> targetCommonEntities = new ArrayList<ProvisioningEntity>();
  
    int commonEntitiesWithNullIds = 0;
  
    for (ProvisioningEntity targetProvisioningEntity: GrouperUtil.nonNull(targetProvisioningEntities)) {
      
      ProvisioningEntity targetCommonEntity = new ProvisioningEntity();
      targetCommonEntity.setProvisioningEntityWrapper(targetProvisioningEntity.getProvisioningEntityWrapper());
      targetCommonEntity.getProvisioningEntityWrapper().setTargetCommonEntity(targetCommonEntity);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("entity")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningEntity", targetProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", targetProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("targetCommonEntity", targetCommonEntity);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      if (!StringUtils.isBlank(targetCommonEntity.getId())) {
        targetCommonEntities.add(targetCommonEntity); 
      } else {
        commonEntitiesWithNullIds++;
      }
    }
    if (commonEntitiesWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonEntitiesWithNullIds", commonEntitiesWithNullIds);
    }
    return targetCommonEntities;
  }

  public List<ProvisioningGroup> translateCommonToTargetGroups(
      List<ProvisioningGroup> targetProvisioningGroups) {
    List<ProvisioningGroup> targetCommonGroups = new ArrayList<ProvisioningGroup>();
    
    int commonGroupsWithNullIds = 0;
    for (ProvisioningGroup targetProvisioningGroup: GrouperUtil.nonNull(targetProvisioningGroups)) {
      
      ProvisioningGroup targetCommonGroup = new ProvisioningGroup();
      targetCommonGroup.setProvisioningGroupWrapper(targetProvisioningGroup.getProvisioningGroupWrapper());
      targetProvisioningGroup.getProvisioningGroupWrapper().setTargetCommonGroup(targetCommonGroup);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("group")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningGroup", targetProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", targetProvisioningGroup.getProvisioningGroupWrapper());
        elVariableMap.put("targetCommonGroup", targetCommonGroup);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.isBlank(targetCommonGroup.getId())) {
        targetCommonGroups.add(targetCommonGroup);
      } else {
        commonGroupsWithNullIds++;
      }
    }
    if (commonGroupsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonGroupsWithNullIds", commonGroupsWithNullIds);
    }
    return targetCommonGroups;
  }

  public List<ProvisioningMembership> translateCommonToTargetMemberships(
      List<ProvisioningMembership> targetProvisioningMemberships) {
    List<ProvisioningMembership> targetCommonMemberships = new ArrayList<ProvisioningMembership>();
    int commonMshipsWithNullIds = 0;
    for (ProvisioningMembership targetProvisioningMembership: GrouperUtil.nonNull(targetProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = targetProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup targetCommonGroup = provisioningGroupWrapper.getTargetCommonGroup();
  
      ProvisioningEntityWrapper provisioningEntityWrapper = targetProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity targetCommonEntity = provisioningEntityWrapper.getTargetCommonEntity();
  
      if (StringUtils.isBlank(targetCommonGroup.getId()) || StringUtils.isBlank(targetCommonEntity.getId())) {
        commonMshipsWithNullIds++;
        continue;
      }
  
      ProvisioningMembership targetCommonMembership = new ProvisioningMembership();
      targetCommonMembership.setProvisioningMembershipWrapper(targetProvisioningMembership.getProvisioningMembershipWrapper());
      targetCommonMembership.getProvisioningMembershipWrapper().setTargetCommonMembership(targetCommonMembership);
  
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("membership")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
  
        elVariableMap.put("targetProvisioningGroup", targetProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("targetCommonGroup", targetCommonGroup);
  
        elVariableMap.put("targetProvisioningEntity", targetProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("targetCommonEntity", targetCommonEntity);
        
        elVariableMap.put("targetProvisioningMembership", targetProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", targetProvisioningMembership.getProvisioningMembershipWrapper());
        elVariableMap.put("targetCommonMembership", targetCommonMembership);
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      if (!StringUtils.equals(targetCommonGroup.getId(), targetCommonMembership.getProvisioningGroupId())) {
        targetCommonMembership.setProvisioningGroupId(targetCommonGroup.getId());
        targetCommonMembership.setProvisioningGroup(targetCommonGroup);
      }
  
      if (!StringUtils.equals(targetCommonEntity.getId(), targetCommonMembership.getProvisioningEntityId())) {
        targetCommonMembership.setProvisioningEntityId(targetCommonEntity.getId());
        targetCommonMembership.setProvisioningEntity(targetCommonEntity);
      }
  
      targetCommonMemberships.add(targetCommonMembership); 
      
    }
    if (commonMshipsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonMshipsWithNullIds", commonMshipsWithNullIds);
    }
    return targetCommonMemberships;
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateCommonToTarget() {
    
    this.translateCommonToTarget(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectDeletes(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes());

    this.translateCommonToTarget(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectInserts(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts());

    this.translateCommonToTarget(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectUpdates(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates());

  
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateCommonToTarget(GrouperProvisioningLists commonList, GrouperProvisioningLists targetList) {
  
    {
      List<ProvisioningGroup> commonProvisioningGroups = commonList.getProvisioningGroups();
      List<ProvisioningGroup> targetProvisioningGroups = translateCommonToTargetGroups(commonProvisioningGroups);
      targetList.setProvisioningGroups(targetProvisioningGroups);
    }
    
    {
      List<ProvisioningEntity> commonProvisioningEntities = commonList.getProvisioningEntities();
      List<ProvisioningEntity> targetProvisioningEntities = translateCommonToTargetEntities(
          commonProvisioningEntities);
      targetList.setProvisioningEntities(targetProvisioningEntities);
    }    
  
    {
      List<ProvisioningMembership> commonProvisioningMemberships = commonList.getProvisioningMemberships();
      List<ProvisioningMembership> targetProvisioningMemberships = translateCommonToTargetMemberships(
          commonProvisioningMemberships);
      targetList.setProvisioningMemberships(targetProvisioningMemberships);
    }    
    
  }
}