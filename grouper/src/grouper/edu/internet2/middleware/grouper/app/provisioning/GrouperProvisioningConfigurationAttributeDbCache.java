package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

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

      // TODO finish this for object cache
//      // see if there is an object cached
//      for (GrouperProvisioningConfigurationAttributeDbCache cache :
//        grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
//        if (cache == null || cache.getSource() != source || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
//          continue;
//        }
//        
//        
//        
//        Object value = gcGrouperSyncGroup.retrieveField("groupAttributeValueCache" + cache.getIndex());
//        if (!GrouperUtil.isBlank(value) && !GrouperUtil.equals(value, currentValue) && !cachedValues.contains(value)) {
//          cachedValues.add(value);
//        }
//      }

      
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
  
}
