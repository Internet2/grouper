package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningConfigurationAttributeDbCache {

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
