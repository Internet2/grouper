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
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("membership"));

    int commonMshipsWithNullIds = 0;
    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperCommonGroup = provisioningGroupWrapper.getGrouperCommonGroup();
 
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperCommonEntity = provisioningEntityWrapper.getGrouperCommonEntity();

      if (StringUtils.isBlank(grouperProvisioningMembership.getProvisioningGroupId()) 
          || StringUtils.isBlank(grouperProvisioningMembership.getProvisioningEntityId())) {
        commonMshipsWithNullIds++;
        continue;
      }

      ProvisioningMembership grouperCommonMembership = new ProvisioningMembership();
      grouperCommonMembership.setProvisioningMembershipWrapper(grouperProvisioningMembership.getProvisioningMembershipWrapper());
 
      for (String script: scripts) {
       
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
        
        runScript(script, elVariableMap);
        
      }
      
      if (grouperCommonMembership.isRemoveFromList()) {
        continue;
      }
      if (grouperCommonGroup != null) {
        if (!StringUtils.equals(grouperCommonGroup.getId(), grouperCommonMembership.getProvisioningGroupId())) {
          grouperCommonMembership.setProvisioningGroupId(grouperCommonGroup.getId());
          grouperCommonMembership.setProvisioningGroup(grouperCommonGroup);
        }
      }
      
      if (grouperCommonEntity != null) {
        if (!StringUtils.equals(grouperCommonEntity.getId(), grouperCommonMembership.getProvisioningEntityId())) {
          grouperCommonMembership.setProvisioningEntityId(grouperCommonEntity.getId());
          grouperCommonMembership.setProvisioningEntity(grouperCommonEntity);
        }
      }

      if (StringUtils.isBlank(grouperCommonMembership.getProvisioningGroupId()) 
          || StringUtils.isBlank(grouperCommonMembership.getProvisioningEntityId())) {
        commonMshipsWithNullIds++;
        continue;
      }

      grouperCommonMembership.getProvisioningMembershipWrapper().setGrouperCommonMembership(grouperCommonMembership);
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

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("entity"));
    if (GrouperUtil.length(scripts) == 0) {
      return grouperCommonEntities;
    }
    int commonEntitiesWithNullIds = 0;
 
    for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperCommonEntity = new ProvisioningEntity();
      grouperCommonEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", grouperProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("grouperCommonEntity", grouperCommonEntity);
        elVariableMap.put("gcGrouperSyncMember", grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember());
        
        runScript(script, elVariableMap);
        
      }

      if (grouperCommonEntity.isRemoveFromList()) {
        continue;
      }
      
      if (!StringUtils.isBlank(grouperCommonEntity.getId())) {
        grouperCommonEntities.add(grouperCommonEntity);
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperCommonEntity(grouperCommonEntity);
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

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("group"));

    List<ProvisioningGroup> grouperCommonGroups = new ArrayList<ProvisioningGroup>();

    if (GrouperUtil.length(scripts) == 0) {
      return grouperCommonGroups;
    }
    
    int commonGroupsWithNullIds = 0;
    for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperCommonGroup = new ProvisioningGroup();
      grouperCommonGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());
      for (String script: scripts) {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", grouperProvisioningGroup.getProvisioningGroupWrapper());
        elVariableMap.put("grouperCommonGroup", grouperCommonGroup);
        elVariableMap.put("gcGrouperSyncGroup", grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup());
        
        runScript(script, elVariableMap);
        
      }

      if (grouperCommonGroup.isRemoveFromList()) {
        continue;
      }

      if (!StringUtils.isBlank(grouperCommonGroup.getId())) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperCommonGroup(grouperCommonGroup);
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
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("membership"));
    if (GrouperUtil.length(scripts) == 0) {
      return targetCommonMemberships;
    }
    int commonMshipsWithNullIds = 0;
    
    Map<String, ProvisioningGroupWrapper> grouperCommonGroupIdToCommonGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects().getProvisioningGroups())) {
      grouperCommonGroupIdToCommonGroupWrapper.put(provisioningGroup.getId(), provisioningGroup.getProvisioningGroupWrapper());
    }

    Map<String, ProvisioningEntityWrapper> grouperCommonEntityIdToCommonEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects().getProvisioningEntities())) {
      grouperCommonEntityIdToCommonEntityWrapper.put(provisioningEntity.getId(), provisioningEntity.getProvisioningEntityWrapper());
    }

    
    Map<MultiKey, ProvisioningMembershipWrapper> grouperCommonGroupIdEntityIdToCommonMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects().getProvisioningMemberships())) {
      grouperCommonGroupIdEntityIdToCommonMembershipWrapper.put(
          new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), 
          provisioningMembership.getProvisioningMembershipWrapper());
    }
    
    for (ProvisioningMembership targetProvisioningMembership: GrouperUtil.nonNull(targetProvisioningMemberships)) {
      
      ProvisioningMembership targetCommonMembership = new ProvisioningMembership();
 
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
 
        elVariableMap.put("targetProvisioningGroup", targetProvisioningMembership.getProvisioningGroup());
 
        elVariableMap.put("targetProvisioningEntity", targetProvisioningMembership.getProvisioningEntity());
        
        elVariableMap.put("targetProvisioningMembership", targetProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", targetProvisioningMembership.getProvisioningMembershipWrapper());
        elVariableMap.put("targetCommonMembership", targetCommonMembership);
        
        runScript(script, elVariableMap);
        
      }

      // setup wrappers while we have the provisioning and common objects handy
      ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
      ProvisioningGroupWrapper provisioningGroupWrapper = null;
      ProvisioningEntityWrapper provisioningEntityWrapper = null;
     
      if (targetCommonMembership.isRemoveFromList()) {
        continue;
      }
      
      if (!StringUtils.isBlank(targetCommonMembership.getProvisioningGroupId()) && !StringUtils.isBlank(targetCommonMembership.getProvisioningEntityId()) ) {
        targetCommonMemberships.add(targetCommonMembership); 
        
        provisioningMembershipWrapper = grouperCommonGroupIdEntityIdToCommonMembershipWrapper.get(new MultiKey(targetCommonMembership.getProvisioningGroupId(), targetCommonMembership.getProvisioningEntityId()));
       
      } else {
        commonMshipsWithNullIds++;
        continue;
      }
      
      if (provisioningMembershipWrapper == null) {
        //we cant link this to other objects yet, so just make a new wrapper
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);

      }
      targetCommonMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
      targetProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
      provisioningMembershipWrapper.setTargetCommonMembership(targetCommonMembership);
      provisioningMembershipWrapper.setTargetProvisioningMembership(targetProvisioningMembership);

      if (!StringUtils.isBlank(targetCommonMembership.getProvisioningGroupId())) {
        provisioningGroupWrapper = grouperCommonGroupIdToCommonGroupWrapper.get(targetCommonMembership.getProvisioningGroupId());
        if (provisioningGroupWrapper != null) {
         targetCommonMembership.setProvisioningGroup(provisioningGroupWrapper.getTargetCommonGroup());
        }
      }
      if (!StringUtils.isBlank(targetCommonMembership.getProvisioningEntityId())) {
        provisioningEntityWrapper = grouperCommonEntityIdToCommonEntityWrapper.get(targetCommonMembership.getProvisioningEntityId());
        if (provisioningEntityWrapper != null) {
          targetCommonMembership.setProvisioningEntity(provisioningEntityWrapper.getTargetCommonEntity());
         }
      }
           
    }
    if (commonMshipsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonMshipsWithNullIds", commonMshipsWithNullIds);
    }
    return targetCommonMemberships;
  }

  public List<ProvisioningEntity> translateTargetToCommonEntities(
      List<ProvisioningEntity> targetProvisioningEntities) {
    List<ProvisioningEntity> targetCommonEntities = new ArrayList<ProvisioningEntity>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("entity"));
    if (GrouperUtil.length(scripts) == 0) {
      return targetCommonEntities;
    }
    int commonEntitiesWithNullIds = 0;
 
    Map<String, ProvisioningEntityWrapper> grouperCommonEntityIdToCommonEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects().getProvisioningEntities())) {
      grouperCommonEntityIdToCommonEntityWrapper.put(provisioningEntity.getId(), provisioningEntity.getProvisioningEntityWrapper());
    }

    for (ProvisioningEntity targetProvisioningEntity: GrouperUtil.nonNull(targetProvisioningEntities)) {
      
      ProvisioningEntity targetCommonEntity = new ProvisioningEntity();
      targetCommonEntity.setProvisioningEntityWrapper(targetProvisioningEntity.getProvisioningEntityWrapper());
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningEntity", targetProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", targetProvisioningEntity.getProvisioningEntityWrapper());
        elVariableMap.put("targetCommonEntity", targetCommonEntity);
        
        runScript(script, elVariableMap);
        
      }
      
      if (targetCommonEntity.isRemoveFromList()) {
        continue;
      }
      
      // setup wrapper while we have the provisioning entity and common entity here
      ProvisioningEntityWrapper provisioningEntityWrapper = null;
      
      if (!StringUtils.isBlank(targetCommonEntity.getId())) {
        targetCommonEntities.add(targetCommonEntity); 
        provisioningEntityWrapper = grouperCommonEntityIdToCommonEntityWrapper.get(targetCommonEntity.getId());
        targetCommonEntity.getProvisioningEntityWrapper().setTargetCommonEntity(targetCommonEntity);
      } else {
        commonEntitiesWithNullIds++;
      }
      
      if (provisioningEntityWrapper == null) {
        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      }
      targetCommonEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      targetProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      provisioningEntityWrapper.setTargetCommonEntity(targetCommonEntity);
      provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);

    }
    if (commonEntitiesWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonEntitiesWithNullIds", commonEntitiesWithNullIds);
    }
    return targetCommonEntities;
  }

  public List<ProvisioningGroup> translateTargetToCommonGroups(
      List<ProvisioningGroup> targetProvisioningGroups) {
    List<ProvisioningGroup> targetCommonGroups = new ArrayList<ProvisioningGroup>();
    
    List<String> scripts = GrouperUtil.nonNull(
        GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getTargetProvisioningToCommonTranslation()).get("group"));

    if (GrouperUtil.length(scripts) == 0) {
      return targetCommonGroups;
    }
    int commonGroupsWithNullIds = 0;
    
    Map<String, ProvisioningGroupWrapper> grouperCommonGroupIdToCommonGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperCommonObjects().getProvisioningGroups())) {
      grouperCommonGroupIdToCommonGroupWrapper.put(provisioningGroup.getId(), provisioningGroup.getProvisioningGroupWrapper());
    }
    
    for (ProvisioningGroup targetProvisioningGroup: GrouperUtil.nonNull(targetProvisioningGroups)) {
      
      ProvisioningGroup targetCommonGroup = new ProvisioningGroup();
      targetCommonGroup.setProvisioningGroupWrapper(targetProvisioningGroup.getProvisioningGroupWrapper());
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetProvisioningGroup", targetProvisioningGroup);
        elVariableMap.put("targetCommonGroup", targetCommonGroup);
        
        runScript(script, elVariableMap);
        
      }
      
      if (targetCommonGroup.isRemoveFromList()) {
        continue;
      }
      
      // setup wrapper while we have the provisioning entity and common entity here
      ProvisioningGroupWrapper provisioningGroupWrapper = null;
      
      if (!StringUtils.isBlank(targetCommonGroup.getId())) {
        targetCommonGroups.add(targetCommonGroup);
        
        provisioningGroupWrapper = grouperCommonGroupIdToCommonGroupWrapper.get(targetCommonGroup.getId());
        targetProvisioningGroup.getProvisioningGroupWrapper().setTargetCommonGroup(targetCommonGroup);
      } else {
        commonGroupsWithNullIds++;
      }
      
      if (provisioningGroupWrapper == null) {
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);

      }
      targetCommonGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      targetProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      provisioningGroupWrapper.setTargetCommonGroup(targetCommonGroup);
      provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);

    }
    if (commonGroupsWithNullIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonGroupsWithNullIds", commonGroupsWithNullIds);
    }
    
    return targetCommonGroups;
  }

  public List<ProvisioningEntity> translateCommonToTargetEntities(String action, 
      List<ProvisioningEntity> commonEntities) {

    List<ProvisioningEntity> commonProvisionToTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration()
        .getCommonProvisioningToTargetTranslation()).get("entity"));
    if (GrouperUtil.length(scripts) == 0) {
      return commonProvisionToTargetEntities;
    }
    
    for (ProvisioningEntity commonEntity: GrouperUtil.nonNull(commonEntities)) {
      
      ProvisioningEntity commonProvisionToTargetEntity = new ProvisioningEntity();
      ProvisioningEntityWrapper provisioningEntityWrapper = commonEntity.getProvisioningEntityWrapper();
      commonProvisionToTargetEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("action", action);
        elVariableMap.put("commonProvisionToTargetEntity", commonProvisionToTargetEntity);
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("commonEntity", commonEntity);
        
        runScript(script, elVariableMap);
        
      }
      
      if (commonProvisionToTargetEntity.isRemoveFromList()) {
        continue;
      }
      
      commonProvisionToTargetEntities.add(commonProvisionToTargetEntity);
      provisioningEntityWrapper.setCommonProvisionToTargetEntity(commonProvisionToTargetEntity);
      
    }
    return commonProvisionToTargetEntities;
  }

  public List<ProvisioningGroup> translateCommonToTargetGroups(String action, 
      List<ProvisioningGroup> commonGroups) {
    List<ProvisioningGroup> commonProvisionToTargetGroups = new ArrayList<ProvisioningGroup>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration()
        .getCommonProvisioningToTargetTranslation()).get("group"));

    if (GrouperUtil.length(scripts) == 0) {
      return commonProvisionToTargetGroups;
    }
    for (ProvisioningGroup commonGroup: GrouperUtil.nonNull(commonGroups)) {
      
      ProvisioningGroup commonProvisionToTargetGroup = new ProvisioningGroup();
      ProvisioningGroupWrapper provisioningGroupWrapper = commonGroup.getProvisioningGroupWrapper();
      commonProvisionToTargetGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      
      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("action", action);
        elVariableMap.put("commonProvisionToTargetGroup", commonProvisionToTargetGroup);
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("commonGroup", commonGroup);
        
        runScript(script, elVariableMap);
        
      }

      if (commonProvisionToTargetGroup.isRemoveFromList()) {
        continue;
      }
      commonProvisionToTargetGroups.add(commonProvisionToTargetGroup);
      provisioningGroupWrapper.setCommonProvisionToTargetGroup(commonProvisionToTargetGroup);
    }
    return commonProvisionToTargetGroups;
  }

  public List<ProvisioningMembership> translateCommonToTargetMemberships(String action, 
      List<ProvisioningMembership> commonMemberships) {
    List<ProvisioningMembership> commonProvisionToTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration()
        .getCommonProvisioningToTargetTranslation()).get("membership"));
    if (GrouperUtil.length(scripts) == 0) {
      return commonProvisionToTargetMemberships;
    }
    for (ProvisioningMembership commonMembership: GrouperUtil.nonNull(commonMemberships)) {
      
      ProvisioningMembership commonProvisionToTargetMembership = new ProvisioningMembership();
      ProvisioningMembershipWrapper provisioningMembershipWrapper = commonMembership.getProvisioningMembershipWrapper();
      commonProvisionToTargetMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);

      ProvisioningGroupWrapper provisioningGroupWrapper = commonMembership.getProvisioningGroup() == null ? null : commonMembership.getProvisioningGroup().getProvisioningGroupWrapper();
  
      ProvisioningEntityWrapper provisioningEntityWrapper = commonMembership.getProvisioningEntity() == null ? null : commonMembership.getProvisioningEntity().getProvisioningEntityWrapper();

      for (String script: scripts) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("action", action);
        elVariableMap.put("commonProvisionToTargetMembership", commonProvisionToTargetMembership);
        elVariableMap.put("provisioningMembershipWrapper", provisioningMembershipWrapper);
        elVariableMap.put("commonMembership", commonMembership);
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("commonProvisionToTargetGroup", provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getCommonProvisionToTargetGroup());
        elVariableMap.put("commonProvisionToTargetEntity", provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getCommonProvisionToTargetEntity());
        
        runScript(script, elVariableMap);
        
      }

      if (commonProvisionToTargetMembership.isRemoveFromList()) {
        continue;
      }
      commonProvisionToTargetMemberships.add(commonProvisionToTargetMembership);
      provisioningMembershipWrapper.setCommonProvisionToTargetMembership(commonProvisionToTargetMembership);

    }
    return commonProvisionToTargetMemberships;
  }

  public void runScript(String script, Map<String, Object> elVariableMap) {
    try {
      GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, true);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }


  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateCommonToTarget() {
    
    this.translateCommonToTarget("delete", this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectDeletes(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes());

    this.translateCommonToTarget("insert", this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectInserts(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts());

    this.translateCommonToTarget("update", this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectUpdates(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates());

  
  }

  /**
   * @param action insert, update, or delete
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateCommonToTarget(String action, GrouperProvisioningLists commonList, GrouperProvisioningLists targetList) {
  
    {
      List<ProvisioningGroup> commonProvisioningGroups = commonList.getProvisioningGroups();
      List<ProvisioningGroup> targetProvisioningGroups = translateCommonToTargetGroups(action, commonProvisioningGroups);
      targetList.setProvisioningGroups(targetProvisioningGroups);
    }
    
    {
      List<ProvisioningEntity> commonProvisioningEntities = commonList.getProvisioningEntities();
      List<ProvisioningEntity> targetProvisioningEntities = translateCommonToTargetEntities(action, 
          commonProvisioningEntities);
      targetList.setProvisioningEntities(targetProvisioningEntities);
    }    
  
    {
      List<ProvisioningMembership> commonProvisioningMemberships = commonList.getProvisioningMemberships();
      List<ProvisioningMembership> targetProvisioningMemberships = translateCommonToTargetMemberships(action, 
          commonProvisioningMemberships);
      targetList.setProvisioningMemberships(targetProvisioningMemberships);
    }    
    
  }
}