package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public void translateGrouperToTarget() {
    
    GrouperProvisioningLists grouperCommonObjects = new GrouperProvisioningLists();
    
    this.getGrouperProvisioner().getGrouperProvisioningData().setGrouperCommonObjects(grouperCommonObjects);
    
    List<ProvisioningGroup> grouperCommonGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningEntity> grouperCommonEntities = new ArrayList<ProvisioningEntity>();
    List<ProvisioningMembership> grouperCommonMemberships = new ArrayList<ProvisioningMembership>();
    
    grouperCommonObjects.setProvisioningGroups(grouperCommonGroups);
    grouperCommonObjects.setProvisioningEntities(grouperCommonEntities);
    grouperCommonObjects.setProvisioningMemberships(grouperCommonMemberships);

//    Map<String, Set<String>> allMembershipsByGroupId = new HashMap<String, Set<String>>();
    
    List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities();
    List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningMemberships();
    List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups();
    
    
    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      ProvisioningMembership grouperCommonMembership = new ProvisioningMembership();
      grouperCommonMembership.setProvisioningMembershipWrapper(grouperProvisioningMembership.getProvisioningMembershipWrapper());
      grouperCommonMembership.getProvisioningMembershipWrapper().setGrouperCommonMembership(grouperCommonMembership);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("membership")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
        elVariableMap.put("grouperCommonMembership", grouperCommonMembership);
        elVariableMap.put("gcGrouperSyncMembership", grouperProvisioningMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      grouperCommonMemberships.add(grouperCommonMembership); 
      
    }
    
    for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperCommonEntity = new ProvisioningEntity();
      grouperCommonEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());
      grouperCommonEntity.getProvisioningEntityWrapper().setGrouperCommonEntity(grouperCommonEntity);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("entity")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
        elVariableMap.put("grouperCommonEntity", grouperCommonEntity);
        elVariableMap.put("gcGrouperSyncMember", grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      grouperCommonEntities.add(grouperCommonEntity); 
      
    }
    
    
    for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperCommonGroup = new ProvisioningGroup();
      grouperCommonGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());
      grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperCommonGroup(grouperCommonGroup);
      
      for (String script: GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGrouperProvisioningToCommonTranslation()).get("group")) {
       
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
        elVariableMap.put("grouperCommonGroup", grouperCommonGroup);
        elVariableMap.put("gcGrouperSyncGroup", grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup());
        
        GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
        
      }
      
      grouperCommonGroups.add(grouperCommonGroup);
      
    }
    
    
  }
}