package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperProvisioningAttributeManipulation {

  public static final String DEFAULT_VALUE_EMPTY_STRING_CONFIG = "<emptyString>";
  
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

  /**
   * provisioner can decide to convert all nulls to empty
   * @return if convert nulls to empty
   */
  public boolean isConvertNullValuesToEmpty() {
    return false;
  }
  
  public Set<ProvisioningGroup> manipulateAttributesGroups(List<ProvisioningGroup> provisioningGroups) {
    Set<ProvisioningGroup> changedGroups = new LinkedHashSet<>();
    int[] manipulateAttributesGroupsCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};
    int[] removeAccentedCharsCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    
    boolean removeAccentedChars = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isRemoveAccentedChars();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        manipulateValue((Collection<Object>)(Object)changedGroups, provisioningGroup, grouperProvisioningConfigurationAttribute, manipulateAttributesGroupsCount);
        convertNullsEmpties((Collection<Object>)(Object)changedGroups, provisioningGroup, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
        if (removeAccentedChars) {
          removeAccentedCharacters((Collection<Object>)(Object)changedGroups, provisioningGroup, grouperProvisioningConfigurationAttribute, removeAccentedCharsCount);
        }
      }
    }
    if (manipulateAttributesGroupsCount[0] > 0) {
      manipulateAttributesGroupsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesGroupsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesGroupsCount", manipulateAttributesGroupsCount[0]);
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
    }
    if (removeAccentedCharsCount[0] > 0) {
      removeAccentedCharsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("removeAccentedCharsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("removeAccentedCharsCount", removeAccentedCharsCount[0]);
    }
    return changedGroups;
  }

  public Set<ProvisioningEntity> manipulateAttributesEntities(List<ProvisioningEntity> provisioningEntities) {
    
    Set<ProvisioningEntity> changedEntities = new LinkedHashSet<ProvisioningEntity>();
    
    int[] manipulateAttributesEntitiesCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};
    int[] removeAccentedCharsCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    
    boolean removeAccentedChars = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isRemoveAccentedChars();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        manipulateValue((Collection<Object>)(Object)changedEntities, provisioningEntity, grouperProvisioningConfigurationAttribute, manipulateAttributesEntitiesCount);
        convertNullsEmpties((Collection<Object>)(Object)changedEntities, provisioningEntity, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
        if (removeAccentedChars) {
          removeAccentedCharacters((Collection<Object>)(Object)changedEntities, provisioningEntity, grouperProvisioningConfigurationAttribute, removeAccentedCharsCount);
        }
      }
      
    }
    if (manipulateAttributesEntitiesCount[0] > 0) {
      manipulateAttributesEntitiesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesEntitiesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesEntitiesCount", manipulateAttributesEntitiesCount[0]);
      
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
    }

    if (removeAccentedCharsCount[0] > 0) {
      removeAccentedCharsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("removeAccentedCharsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("removeAccentedCharsCount", removeAccentedCharsCount[0]);
    }
    return changedEntities;
  }

  public Set<ProvisioningMembership> manipulateAttributesMemberships(List<ProvisioningMembership> provisioningMemberships) {
    
    Set<ProvisioningMembership> changedObjects = new LinkedHashSet<ProvisioningMembership>();
    
    int[] manipulateAttributesMembershipsCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        manipulateValue((Collection<Object>)(Object)changedObjects, provisioningMembership, grouperProvisioningConfigurationAttribute, manipulateAttributesMembershipsCount);
        convertNullsEmpties((Collection<Object>)(Object)changedObjects, provisioningMembership, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
      }
    }
    if (manipulateAttributesMembershipsCount[0] > 0) {
      manipulateAttributesMembershipsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesMembershipsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesMembershipsCount", manipulateAttributesMembershipsCount[0]);
      
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
      
    }
    return changedObjects;
  }

  /**
   * @param provisioningGroups
   * @param attribute null for all or an attribute name for a specific one
   * @return changed groups
   */
  public Set<ProvisioningGroup> assignDefaultsForGroups(List<ProvisioningGroup> provisioningGroups, GrouperProvisioningConfigurationAttribute attribute) {
    
    Set<ProvisioningGroup> changedGroups = new LinkedHashSet<>();
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();

    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = groupFieldNameToConfig.get("id");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeDisplayName = groupFieldNameToConfig.get("displayName");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = groupFieldNameToConfig.get("name");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeIdIndex = groupFieldNameToConfig.get("idIndex");

    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeId) {
        provisioningGroup.setId((String)assignDefaultField((Collection<Object>)(Object)changedGroups, provisioningGroup, provisioningGroup.getId(), grouperProvisioningConfigurationAttributeId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeDisplayName) {
        provisioningGroup.setDisplayName((String)assignDefaultField((Collection<Object>)(Object)changedGroups, provisioningGroup, provisioningGroup.getDisplayName(), grouperProvisioningConfigurationAttributeDisplayName,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeName) {
        provisioningGroup.setName((String)assignDefaultField((Collection<Object>)(Object)changedGroups, provisioningGroup, provisioningGroup.getName(), grouperProvisioningConfigurationAttributeName,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeIdIndex) {
        provisioningGroup.setIdIndex(GrouperUtil.longObjectValue(assignDefaultField((Collection<Object>)(Object)changedGroups, provisioningGroup, provisioningGroup.getIdIndex(), grouperProvisioningConfigurationAttributeIdIndex, assignDefaultFieldsAndAttributesCount), true));
      }
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        if (attribute == null || attribute == grouperProvisioningConfigurationAttribute) {
          assignDefault((Collection<Object>)(Object)changedGroups, provisioningGroup, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
        }
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
    return changedGroups;
  }

  /**
   * 
   * @param provisioningEntities
   * @param checkProcessedFlag true if not process if already processed
   * @param filterSelect
   * @param filterInsert
   * @param filterUpdate
   * @return changed entities
   */
  public Set<ProvisioningEntity> manipulateDefaultsFilterAttributesEntities(List<ProvisioningEntity> provisioningEntities, boolean assignDefaults, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {

    Set<ProvisioningEntity> changedEntities = new LinkedHashSet<ProvisioningEntity>();
    
    if (GrouperUtil.length(provisioningEntities) > 0) {

      if (assignDefaults) {
        changedEntities.addAll(assignDefaultsForEntities(provisioningEntities, null));
      }

      changedEntities.addAll(filterEntityFieldsAndAttributes(provisioningEntities, filterSelect, filterInsert, filterUpdate));
      
      changedEntities.addAll(manipulateAttributesEntities(provisioningEntities));

    }
    
    return changedEntities;
  }
  
  /**
   * 
   * @param provisioningGroups
   * @param checkProcessedFlag true if not process if already processed
   * @param filterSelect
   * @param filterInsert
   * @param filterUpdate
   */
  public Set<ProvisioningGroup> manipulateDefaultsFilterAttributesGroups(List<ProvisioningGroup> provisioningGroups, boolean assignDefaults, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Set<ProvisioningGroup> changedGroups = new LinkedHashSet<ProvisioningGroup>();
    
    if (GrouperUtil.length(provisioningGroups) == 0) {
      return changedGroups;
    }
    
    if (assignDefaults) {
      changedGroups.addAll(assignDefaultsForGroups(provisioningGroups, null));
    }

    changedGroups.addAll(filterGroupFieldsAndAttributes(provisioningGroups, filterSelect, filterInsert, filterUpdate));
    
    changedGroups.addAll(manipulateAttributesGroups(provisioningGroups));

    return changedGroups;
  }
  
  /**
   * @param provisioningEntities
   * @param attribute null for all or an attribute name for a specific one
   * @return changedEntities
   */
  public Set<ProvisioningEntity> assignDefaultsForEntities(List<ProvisioningEntity> provisioningEntities, GrouperProvisioningConfigurationAttribute attribute) {
    
    Set<ProvisioningEntity> changedEntities = new LinkedHashSet<ProvisioningEntity>();
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> entityFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    
    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = entityFieldNameToConfig.get("id");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeLoginId = entityFieldNameToConfig.get("loginId");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = entityFieldNameToConfig.get("name");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeEmail = entityFieldNameToConfig.get("email");

    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeId) {
        provisioningEntity.setId((String)assignDefaultField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getId(), grouperProvisioningConfigurationAttributeId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeLoginId) {
        provisioningEntity.setLoginId((String)assignDefaultField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getLoginId(), grouperProvisioningConfigurationAttributeLoginId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeName) {
        provisioningEntity.setName((String)assignDefaultField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getName(), grouperProvisioningConfigurationAttributeName,  assignDefaultFieldsAndAttributesCount));
      }

      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeEmail) {
        provisioningEntity.setEmail((String)assignDefaultField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getEmail(), grouperProvisioningConfigurationAttributeEmail, assignDefaultFieldsAndAttributesCount));
      }

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        if (attribute == null || attribute == grouperProvisioningConfigurationAttribute) {
          assignDefault((Collection<Object>)(Object)changedEntities, provisioningEntity, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
        }
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
    return changedEntities;
  }

  /**
   * 
   * @param provisioningMemberships
   * @return changedEntities
   */
  public Set<ProvisioningMembership> assignDefaultsForMemberships(List<ProvisioningMembership> provisioningMemberships) {
    Set<ProvisioningMembership> changedMemberships = new LinkedHashSet<ProvisioningMembership>();

    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> membershipFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    
    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      provisioningMembership.setId((String)assignDefaultField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getId(), membershipFieldNameToConfig.get("id"),  assignDefaultFieldsAndAttributesCount));
      provisioningMembership.setProvisioningEntityId((String)assignDefaultField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getProvisioningEntityId(), membershipFieldNameToConfig.get("provisioningEntityId"),  assignDefaultFieldsAndAttributesCount));
      provisioningMembership.setProvisioningGroupId((String)assignDefaultField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getProvisioningGroupId(), membershipFieldNameToConfig.get("provisioningGroupId"),  assignDefaultFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        assignDefault((Collection<Object>)(Object)changedMemberships, provisioningMembership, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
    return changedMemberships;
  }

  /**
   * 
   * @param currentValue
   * @param grouperProvisioningConfigurationAttribute
   * @param assignDefaultFieldsAndAttributesCount
   * @return return the current or new field
   */
  public Object assignDefaultField(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable, Object currentValue, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      int[] assignDefaultFieldsAndAttributesCount) {

    if (grouperProvisioningConfigurationAttribute != null) {

      // set a default value if blank and is a grouper object
      if (currentValue == null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        changedObjects.add(provisioningUpdatable);
        assignDefaultFieldsAndAttributesCount[0]++;
        if (grouperProvisioningConfigurationAttribute.getDefaultValue().equals(DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
          return "";
        }
        
        return grouperProvisioningConfigurationAttribute.getDefaultValue();
      }
    }

    return currentValue;
  }

  public void assignDefault(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    if (grouperProvisioningConfigurationAttribute == null || StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
      return;
    }
    
    String defaultValue = grouperProvisioningConfigurationAttribute.getDefaultValue();
    if (StringUtils.equals(defaultValue, DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
      defaultValue = "";
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    
    // scalar
    if (!grouperProvisioningConfigurationAttribute.isMultiValued()) {
      if (currentValue == null) {
        provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), defaultValue);
        count[0]++;
        changedObjects.add(provisioningUpdatable);
      }
      return;
    }

    if (currentValue == null) {
      changedObjects.add(provisioningUpdatable);
      count[0]++;
      provisioningUpdatable.addAttributeValue(grouperProvisioningConfigurationAttribute.getName(), defaultValue);
      return;
    }
    if (currentValue instanceof Collection) {
      Collection currentValueCollection = (Collection)currentValue;
      if (currentValueCollection.size() == 0) {
        changedObjects.add(provisioningUpdatable);
        currentValueCollection.add(defaultValue);
        count[0]++;
      }
      return;
    }
    throw new RuntimeException("Not expecting attribute type: " + currentValue.getClass());
  }

  public void manipulateValue(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    if (grouperProvisioningConfigurationAttribute == null) {
      return;
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    Object originalValue = currentValue;

    if (currentValue == null) {
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
      if (originalValue != currentValue) {
        if (count!= null) {
          count[0]++;
        }
        changedObjects.add(provisioningUpdatable);
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
    }
    Object newValue = valueType.convert(currentValue);
    if (originalValue != newValue) {
      if (count!= null) {
        count[0]++;
      }
      changedObjects.add(provisioningUpdatable);
    }
    provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), newValue);
  }

  
  public void removeAccentedCharacters(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {
    
    // not sure what this is
    if (grouperProvisioningConfigurationAttribute == null) {
      return;
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());

    GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
    
    // only do this for strings
    if (valueType == null) {
      return;
    }
    
    if (valueType != GrouperProvisioningConfigurationAttributeValueType.STRING) {
      return;
    }
    
    if (currentValue == null) {
      return;
    }
    
    if (currentValue instanceof String) {
      String newValue = org.apache.commons.lang3.StringUtils.stripAccents((String)currentValue); 
      if (!GrouperUtil.equals(currentValue, newValue)) {
        provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), newValue);
        changedObjects.add(provisioningUpdatable);
        
        // keep a count if supposed to
        if (count!= null) {
          count[0]++;
        }
      }
    }
    
    if (currentValue instanceof Collection) {
      Collection<Object> newCollection = new HashSet<>();
      Collection collection = (Collection)currentValue;
      boolean changed = false;
      for (Object obj: collection) {
        if (obj instanceof String) {
          String newIndividualValue = org.apache.commons.lang3.StringUtils.stripAccents((String)obj); 
          newCollection.add(newIndividualValue);
          if (changed == false && !GrouperUtil.equals(obj, newIndividualValue)) {
            changed = true;
            changedObjects.add(provisioningUpdatable);
            if (count!= null) {
              count[0]++;
            }
          }
          
        } else {
          newCollection.add(obj);
        }
      }
      if (changed) {
        provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), newCollection);
      }
      
    }
    
  }
  
  /**
   * if the provisioner should equate nulls and empties, then convert nulls to empties
   * @param provisioningUpdatable
   * @param grouperProvisioningConfigurationAttribute
   * @param count
   */
  public void convertNullsEmpties(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    // should we convert null to empty?
    if (!this.isConvertNullValuesToEmpty()) {
      return;
    }

    // not sure what this is
    if (grouperProvisioningConfigurationAttribute == null) {
      return;
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());

    // its notnull so ignore
    if (currentValue != null) {
      return;
    }
    GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
    
    // only do this for strings
    if (valueType == null || valueType != GrouperProvisioningConfigurationAttributeValueType.STRING) {
      return;
    }

    // but not multivalued strings
    if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
      return;
    }
    
    changedObjects.add(provisioningUpdatable);
    
    // keep a count if supposed to
    if (count!= null) {
      count[0]++;
    }
    // assign empty
    provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), "");
  }

  public Set<ProvisioningGroup> filterGroupFieldsAndAttributes(List<ProvisioningGroup> provisioningGroups, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Set<ProvisioningGroup> changedGroups = new LinkedHashSet<ProvisioningGroup>();
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().isTranslateGrouperToTargetAutomatically()) {
      return changedGroups;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    
    int[] filterGroupFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      
      provisioningGroup.setId((String)filterField((Collection<Object>)(Object) changedGroups, provisioningGroup, provisioningGroup.getId(), groupFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setDisplayName((String)filterField((Collection<Object>)(Object) changedGroups, provisioningGroup, provisioningGroup.getDisplayName(), groupFieldNameToConfig.get("displayName"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setName((String)filterField((Collection<Object>)(Object) changedGroups, provisioningGroup, provisioningGroup.getName(), groupFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setIdIndex((Long)filterField((Collection<Object>)(Object) changedGroups, provisioningGroup, provisioningGroup.getIdIndex(), groupFieldNameToConfig.get("idIndex"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        //TODO maybe change this to not filter the membership attributes
        //If we are storing something to the grouper side for example in entity attribute the membership label
        // We are not selecting, inserting, or updating but we do want this attribute to exist
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect() && (grouperProvisioningConfigurationAttribute.isInsert() || grouperProvisioningConfigurationAttribute.isUpdate()) )
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          provisioningGroup.removeAttribute(attributeName);
          filterGroupFieldsAndAttributesCount[0]++;
          changedGroups.add(provisioningGroup);
        }
      }
      
      GrouperUtil.nonNull(provisioningGroup.getAttributes()).keySet().retainAll(groupAttributeNameToConfig.keySet());

    }
    if (filterGroupFieldsAndAttributesCount[0] > 0) {
      filterGroupFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterGroupFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterGroupFieldsAndAttributesCount", filterGroupFieldsAndAttributesCount[0]);
      
    }
    return changedGroups;
  }

  /**
   * 
   * @param provisioningEntities
   * @param filterSelect
   * @param filterInsert
   * @param filterUpdate
   * @return changed entities
   */
  public Set<ProvisioningEntity> filterEntityFieldsAndAttributes(List<ProvisioningEntity> provisioningEntities, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    Set<ProvisioningEntity> changedEntities = new LinkedHashSet<ProvisioningEntity>();
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().isTranslateGrouperToTargetAutomatically()) {
      return changedEntities;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> entityFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    
    int[] filterEntityFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      provisioningEntity.setId((String)filterField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getId(), entityFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setLoginId((String)filterField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getLoginId(), entityFieldNameToConfig.get("loginId"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setName((String)filterField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getName(), entityFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setEmail((String)filterField((Collection<Object>)(Object)changedEntities, provisioningEntity, provisioningEntity.getEmail(), entityFieldNameToConfig.get("email"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        //TODO maybe change this to not filter the membership attributes
        //If we are storing something to the grouper side for example in entity attribute the membership label
        // We are not selecting, inserting, or updating but we do want this attribute to exist
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect() && (grouperProvisioningConfigurationAttribute.isInsert() || grouperProvisioningConfigurationAttribute.isUpdate()) )
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterEntityFieldsAndAttributesCount[0]++;
          changedEntities.add(provisioningEntity);
          provisioningEntity.removeAttribute(attributeName);
        }
      }
      
      GrouperUtil.nonNull(provisioningEntity.getAttributes()).keySet().retainAll(entityAttributeNameToConfig.keySet());
      
    }
    if (filterEntityFieldsAndAttributesCount[0] > 0) {
      filterEntityFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterEntityFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterEntityFieldsAndAttributesCount", filterEntityFieldsAndAttributesCount[0]);
      
    }
    return changedEntities;
  }

  public Set<ProvisioningMembership> filterMembershipFieldsAndAttributes(List<ProvisioningMembership> provisioningMemberships, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    Set<ProvisioningMembership> changedMemberships = new LinkedHashSet<ProvisioningMembership>();
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().isTranslateGrouperToTargetAutomatically()) {
      return changedMemberships;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> membershipFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    if (GrouperUtil.length(membershipAttributeNameToConfig) == 0 && GrouperUtil.length(membershipFieldNameToConfig) == 0) {
      return changedMemberships;
    }
    int[] filterMembershipFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      provisioningMembership.setId((String)filterField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getId(), membershipFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningEntityId((String)filterField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getProvisioningEntityId(), membershipFieldNameToConfig.get("provisioningEntityId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningGroupId((String)filterField((Collection<Object>)(Object)changedMemberships, provisioningMembership, provisioningMembership.getProvisioningGroupId(), membershipFieldNameToConfig.get("provisioningGroupId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterMembershipFieldsAndAttributesCount[0]++;
          provisioningMembership.removeAttribute(attributeName);
          changedMemberships.add(provisioningMembership);
        }
      }
      GrouperUtil.nonNull(provisioningMembership.getAttributes()).keySet().retainAll(membershipAttributeNameToConfig.keySet());

    }
    if (filterMembershipFieldsAndAttributesCount[0] > 0) {
      filterMembershipFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterMembershipFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterMembershipFieldsAndAttributesCount", filterMembershipFieldsAndAttributesCount[0]);
      
    }
    return changedMemberships;
  }

  private Object filterField(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable, Object value,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, boolean filterSelect, boolean filterInsert, boolean filterUpdate, int[] count) {
    if (value == null || grouperProvisioningConfigurationAttribute == null) {
      // if not configured to have this field, then dont
      return null;
    }
    if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
        || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
        || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
      changedObjects.add(provisioningUpdatable);
      count[0]++;
      return null;
    }
    return value;
      
  }

  /**
   * 
   * @param provisioningMemberships
   * @param checkProcessedFlag true if not process if already processed
   * @param filterSelect
   * @param filterInsert
   * @param filterUpdate
   * @return changed entities
   */
  public Set<ProvisioningMembership> manipulateDefaultsFilterAttributesMemberships(List<ProvisioningMembership> provisioningMemberships, boolean assignDefaults, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
  
    Set<ProvisioningMembership> changedMemberships = new LinkedHashSet<ProvisioningMembership>();
    
    if (GrouperUtil.length(provisioningMemberships) > 0) {
  
      if (assignDefaults) {
        changedMemberships.addAll(assignDefaultsForMemberships(provisioningMemberships));
      }
  
      changedMemberships.addAll(filterMembershipFieldsAndAttributes(provisioningMemberships, filterSelect, filterInsert, filterUpdate));
      
      changedMemberships.addAll(manipulateAttributesMemberships(provisioningMemberships));
  
    }
    
    return changedMemberships;
  }



}
