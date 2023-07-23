package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class GrouperProvisioningConfigurationAttributeDbCache {

  /**
   * get all the cached values for a group attribute.
   * these are prioritized by most important.
   * note, the current value will not be returned
   * @param someTargetGroup
   * @param attributeName
   * @return the set of values
   */
  public static Set<Object> cachedValuesForGroup(ProvisioningGroup someTargetGroup, String attributeName) {
    Set<Object> cachedValues = new LinkedHashSet<Object>();
    if (someTargetGroup.getProvisioningGroupWrapper() == null 
        || someTargetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup() == null) {
      return cachedValues;
    }
    GcGrouperSyncGroup gcGrouperSyncGroup = someTargetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
    Object currentValue = someTargetGroup.retrieveAttributeValue(attributeName);
    GrouperProvisioner grouperProvisioner = someTargetGroup.getGrouperProvisioner();
    // look in target first
    for (GrouperProvisioningConfigurationAttributeDbCacheSource source : 
      new GrouperProvisioningConfigurationAttributeDbCacheSource[] {
          GrouperProvisioningConfigurationAttributeDbCacheSource.target,
          GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
      }) {

      // see if there is an attribute cached
      for (GrouperProvisioningConfigurationAttributeDbCache cache :
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
        if (cache == null || cache.getSource() != source || !StringUtils.equals(attributeName, cache.getAttributeName())) {
          continue;
        }
        Object value = gcGrouperSyncGroup.retrieveField("groupAttributeValueCache" + cache.getIndex());
        if (!GrouperUtil.isBlank(value) && !GrouperUtil.equals(value, currentValue) && !cachedValues.contains(value)) {
          cachedValues.add(value);
        }
      }

      // see if there is an object cached
      for (GrouperProvisioningConfigurationAttributeDbCache cache :
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
        if (cache == null || cache.getSource() != source || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          continue;
        }
        
        Object value = gcGrouperSyncGroup.retrieveField("groupAttributeValueCache" + cache.getIndex());
        if (!GrouperUtil.isBlank(value)) {
          try {
            ProvisioningGroup provisioningGroup = grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup((String)value);
            if (provisioningGroup != null) {
              value = provisioningGroup.retrieveAttributeValue(attributeName);
              if (!GrouperUtil.equals(value, currentValue) && !cachedValues.contains(value)) {
                cachedValues.add(value);
              }
            }
          } catch (Exception e) {
            LOG.error("Error retrieving from cache! " + gcGrouperSyncGroup.getId() + ", " + gcGrouperSyncGroup.getGroupName(), e);
          }
        }
      }
    }
    return cachedValues;
  }
  
  /**
   * get all the cached values for an entity attribute.
   * these are prioritized by most important.
   * note, the current value will not be returned
   * @param someTargetEntity
   * @param attributeName
   * @return the set of values
   */
  public static Set<Object> cachedValuesForEntity(ProvisioningEntity someTargetEntity, String attributeName) {
    Set<Object> cachedValues = new LinkedHashSet<Object>();
    if (someTargetEntity.getProvisioningEntityWrapper() == null 
        || someTargetEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember() == null) {
      return cachedValues;
    }
    GcGrouperSyncMember gcGrouperSyncMember = someTargetEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
    Object currentValue = someTargetEntity.retrieveAttributeValue(attributeName);
    GrouperProvisioner grouperProvisioner = someTargetEntity.getGrouperProvisioner();
    // look in target first
    for (GrouperProvisioningConfigurationAttributeDbCacheSource source : 
      new GrouperProvisioningConfigurationAttributeDbCacheSource[] {
          GrouperProvisioningConfigurationAttributeDbCacheSource.target,
          GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
      }) {

      // see if there is an attribute cached
      for (GrouperProvisioningConfigurationAttributeDbCache cache :
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
        if (cache == null || cache.getSource() != source) {
          continue;
        }
        
        String cacheAttributeName = cache.getAttributeName();
        
        if (StringUtils.isBlank(cacheAttributeName) && source == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
            && cache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript) {
          for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
              grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()) {
            if (grouperProvisioningConfigurationAttribute.getTranslateExpressionType() == GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningEntityField
                && StringUtils.equals(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField(), cache.getCacheName())) {
              cacheAttributeName = grouperProvisioningConfigurationAttribute.getName();
              break;
            }
          }
        }
        
        if (StringUtils.equals(attributeName, cacheAttributeName)) {
          Object value = gcGrouperSyncMember.retrieveField("entityAttributeValueCache" + cache.getIndex());
          if (!GrouperUtil.isBlank(value) && !GrouperUtil.equals(value, currentValue) && !cachedValues.contains(value)) {
            cachedValues.add(value);
          }
        } else {
          
        }
      }

      // see if there is an object cached
      for (GrouperProvisioningConfigurationAttributeDbCache cache :
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
        if (cache == null || cache.getSource() != source || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          continue;
        }
        
        Object value = gcGrouperSyncMember.retrieveField("entityAttributeValueCache" + cache.getIndex());
        if (!GrouperUtil.isBlank(value)) {
          try {
            ProvisioningEntity provisioningEntity = grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity((String)value);
            if (provisioningEntity != null) {
              value = provisioningEntity.retrieveAttributeValue(attributeName);
              if (!GrouperUtil.equals(value, currentValue) && !cachedValues.contains(value)) {
                cachedValues.add(value);
              }
            }
          } catch (Exception e) {
            LOG.error("Error retrieving from cache! " + gcGrouperSyncMember.getId() + ", " + gcGrouperSyncMember.getSubjectId(), e);
          }
        }
      }
    }
    return cachedValues;
  }
  
  public String toString() {
    StringBuilder result = new StringBuilder(this.objectType).append("Cache(");
    result.append("index: ").append(this.index);
    result.append(", source: ").append(this.source);
    result.append(", type: ").append(this.type);
    if (!StringUtils.isBlank(this.attributeName)) {
      result.append(", attributeName: ").append(this.attributeName);
    }
    if (!StringUtils.isBlank(this.translationScript)) {
      result.append(", translationScript: ").append(this.translationScript);
    }
    
    return result.append(")").toString();
  }

  private String cacheName = null;
  
  public String getCacheName() {
    if (this.cacheName == null) {
      this.cacheName = this.objectType + "AttributeValueCache" + this.index;
    }
    return this.cacheName;
  }
  
  public GrouperProvisioningConfigurationAttributeDbCache(GrouperProvisioner grouperProvisioner1, int index1, String objectType1) {
    this.grouperProvisioner = grouperProvisioner1;
    this.index = index1;
    this.objectType = objectType1;
  }
  
  public GrouperProvisioningConfigurationAttribute retrieveAttribute() {
    if (this.type != GrouperProvisioningConfigurationAttributeDbCacheType.attribute
        || StringUtils.isBlank(this.attributeName)) {
      return null;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = null;
    
    if (StringUtils.equals("group", this.objectType)) {
      targetGroupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    } else if (StringUtils.equals("entity", this.objectType)) {
      targetGroupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    } else {
      throw new RuntimeException("Invalid object type '" + this.objectType + "'");
    }
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = targetGroupAttributeNameToConfig.get(this.attributeName);
    
    GrouperUtil.assertion(grouperProvisioningConfigurationAttribute != null, this.objectType + " attribute cache " + this.index + " attribute not found: '" + this.attributeName + "'");
    
    return grouperProvisioningConfigurationAttribute;
  }
  
  private GrouperProvisioner grouperProvisioner = null;
  
  /**
   * group or entity
   */
  private String objectType;  
  
  /**
   * group or entity
   * @return the object type
   */
  public String getObjectType() {
    return objectType;
  }

  private int index;
  
  
  private GrouperProvisioningConfigurationAttributeDbCacheSource source;
  
  private GrouperProvisioningConfigurationAttributeDbCacheType type;
  
  private String attributeName;
  
  private String translationScript;

  private boolean nullChecksInScript;

  private String translationContinueCondition;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningConfigurationAttributeDbCache.class);

  public int getIndex() {
    return index;
  }

  public GrouperProvisioningConfigurationAttributeDbCacheSource getSource() {
    return source;
  }

  public void setSource(GrouperProvisioningConfigurationAttributeDbCacheSource source) {
    this.source = source;
  }

  public GrouperProvisioningConfigurationAttributeDbCacheType getType() {
    return type;
  }

  public void setType(GrouperProvisioningConfigurationAttributeDbCacheType type) {
    this.type = type;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getTranslationScript() {
    return translationScript;
  }

  public void setTranslationScript(String translationScript) {
    this.translationScript = translationScript;
  }

  public void setNullChecksInScript(boolean theEntityAttributeValueCacheNullChecksInScript) {
   this.nullChecksInScript = theEntityAttributeValueCacheNullChecksInScript;
  }

  public void setTranslationContinueConditon(String translationContinueCondition) {
    this.translationContinueCondition = translationContinueCondition;
  }
  
  public boolean isNullChecksInScript() {
    return nullChecksInScript;
  }

  public String getTranslationContinueCondition() {
    return translationContinueCondition;
  }
  
}
