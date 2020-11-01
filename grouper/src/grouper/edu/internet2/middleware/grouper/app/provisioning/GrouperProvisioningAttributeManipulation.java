package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperProvisioningAttributeManipulation {

  public GrouperProvisioningAttributeManipulation() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public void manipulateAttributesGroups(List<ProvisioningGroup> provisioningGroups) {
    
    int[] manipulateAttributesGroupsCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        manipulateValue(provisioningGroup, grouperProvisioningConfigurationAttribute, manipulateAttributesGroupsCount);
      }
    }
    if (manipulateAttributesGroupsCount[0] > 0) {
      manipulateAttributesGroupsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesGroupsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesGroupsCount", manipulateAttributesGroupsCount[0]);
      
    }

  }

  public void manipulateAttributesEntities(List<ProvisioningEntity> provisioningEntities) {
    
    int[] manipulateAttributesEntitiesCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        manipulateValue(provisioningEntity, grouperProvisioningConfigurationAttribute, manipulateAttributesEntitiesCount);
      }
    }
    if (manipulateAttributesEntitiesCount[0] > 0) {
      manipulateAttributesEntitiesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesEntitiesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesEntitiesCount", manipulateAttributesEntitiesCount[0]);
      
    }
  }

  public void manipulateAttributesMemberships(List<ProvisioningMembership> provisioningMemberships) {
    
    int[] manipulateAttributesMembershipsCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        manipulateValue(provisioningMembership, grouperProvisioningConfigurationAttribute, manipulateAttributesMembershipsCount);
      }
    }
    if (manipulateAttributesMembershipsCount[0] > 0) {
      manipulateAttributesMembershipsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesMembershipsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesMembershipsCount", manipulateAttributesMembershipsCount[0]);
      
    }
  }

  public void manipulateValue(ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    Object originalValue = currentValue;
    
    if (grouperProvisioningConfigurationAttribute == null || currentValue == null) {
      return;
    }
    GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
    if (valueType == null) {
      return;
    }
    
    if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
      if (currentValue instanceof Set) {
        // we good
      } else if (currentValue instanceof Collection) {
        currentValue = new HashSet((Collection)currentValue);
        
      } else if (currentValue.getClass().isArray()) {
        Set newValue = new HashSet();
        for (int i=0;i<Array.getLength(currentValue); i++) {
          newValue.add(Array.get(currentValue, i));
        }
        currentValue = newValue;
      } else {
        currentValue = GrouperUtil.toSet(currentValue);
      }
      if (!valueType.correctTypeForSet((Set)currentValue)) {
        Set newValue = new HashSet();
        for (Object value : (Set)currentValue) {
          newValue.add(valueType.convert(value));
        }
        currentValue = newValue;
      }
      if (originalValue != currentValue && count!= null) {
        count[0]++;
      }
      provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), currentValue);
      return;
    }
    // unwrap collections?
    if (currentValue instanceof Collection) {
      Collection collection = (Collection)currentValue;
      if (collection.size() == 1) {
        currentValue = collection.iterator().next();
      } else if (collection.size() == 0) {
        return;
      } else {
        throw new RuntimeException("Attribute should not be a collection: " + grouperProvisioningConfigurationAttribute.getName());
      }
    } else if (currentValue.getClass().isArray()) {
      if (Array.getLength(currentValue) == 1) {
        currentValue = Array.get(currentValue, 0);
      } else if (Array.getLength(currentValue) == 0) {
        return;
      } else {
        throw new RuntimeException("Attribute should not be an array: " + grouperProvisioningConfigurationAttribute.getName());
      }
    }
    Object newValue = valueType.convert(currentValue);
    if (originalValue != newValue && count!= null) {
      count[0]++;
    }
    provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), newValue);
  }

  public void filterGroupFieldsAndAttributes(List<ProvisioningGroup> provisioningGroups, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig();
    
    int[] filterGroupFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      
      provisioningGroup.setId((String)filterField(provisioningGroup.getId(), groupFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setDisplayName((String)filterField(provisioningGroup.getDisplayName(), groupFieldNameToConfig.get("displayName"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setName((String)filterField(provisioningGroup.getName(), groupFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setIdIndex((Long)filterField(provisioningGroup.getIdIndex(), groupFieldNameToConfig.get("idIndex"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          provisioningGroup.removeAttribute(attributeName);
          filterGroupFieldsAndAttributesCount[0]++;
        }
      }
    }
    if (filterGroupFieldsAndAttributesCount[0] > 0) {
      filterGroupFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterGroupFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterGroupFieldsAndAttributesCount", filterGroupFieldsAndAttributesCount[0]);
      
    }
  }

  public void filterEntityFieldsAndAttributes(List<ProvisioningEntity> provisioningEntities, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> entityFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig();
    
    int[] filterEntityFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      provisioningEntity.setId((String)filterField(provisioningEntity.getId(), entityFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setLoginId((String)filterField(provisioningEntity.getLoginId(), entityFieldNameToConfig.get("loginId"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setName((String)filterField(provisioningEntity.getName(), entityFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setEmail((String)filterField(provisioningEntity.getEmail(), entityFieldNameToConfig.get("email"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterEntityFieldsAndAttributesCount[0]++;
          provisioningEntity.removeAttribute(attributeName);
        }
      }
    }
    if (filterEntityFieldsAndAttributesCount[0] > 0) {
      filterEntityFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterEntityFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterEntityFieldsAndAttributesCount", filterEntityFieldsAndAttributesCount[0]);
      
    }

  }

  public void filterMembershipFieldsAndAttributes(List<ProvisioningMembership> provisioningMemberships, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> membershipFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig();
    
    int[] filterMembershipFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      provisioningMembership.setId((String)filterField(provisioningMembership.getId(), membershipFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningEntityId((String)filterField(provisioningMembership.getProvisioningEntityId(), membershipFieldNameToConfig.get("provisioningEntityId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningGroupId((String)filterField(provisioningMembership.getProvisioningGroupId(), membershipFieldNameToConfig.get("provisioningGroupId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterMembershipFieldsAndAttributesCount[0]++;
          provisioningMembership.removeAttribute(attributeName);
        }
      }
    }
    if (filterMembershipFieldsAndAttributesCount[0] > 0) {
      filterMembershipFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterMembershipFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterMembershipFieldsAndAttributesCount", filterMembershipFieldsAndAttributesCount[0]);
      
    }

  }

  private Object filterField(Object value,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, boolean filterSelect, boolean filterInsert, boolean filterUpdate, int[] count) {
    if (value == null || grouperProvisioningConfigurationAttribute == null) {
      return value;
    }
    if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
        || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
        || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
      count[0]++;
      return null;
    }
    return value;
      
  }



}
