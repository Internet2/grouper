package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

/**
 * provisioning target and it's attributes 
 */
public class GrouperProvisioningTarget {
  
  /**
   * target key. It's used to pull the label for ui
   */
  private String key;
  
  /**
   * group whose members are allowed to assign this target
   */
  private String groupAllowedToAssign;
  
  /**
   * should the target be assignable to only one stem
   */
  private boolean allowAssignmentsOnlyOnOneStem;
  
  /**
   * should the target be only readable and not assignable
   */
  private boolean readOnly;
  
  /**
   * name of the target (part of the config keys)
   */
  private String name;
  
  /**
   * 
   * @param key configured .key property
   * @param name is name of the target (part of the config keys)
   */
  public GrouperProvisioningTarget(String key, String name) {
   
    if (StringUtils.isBlank(key) || StringUtils.isBlank(name)) {
      throw new RuntimeException("key and name cannnot be blank or null");
    }

    this.key = key;
    this.name = name;
  }

  /**
   * target key. It's used to pull the label for ui
   * @return
   */
  public String getKey() {
    return key;
  }
  
  /**
   * externalized name or the key
   * @return the name
   */
  public String getExternalizedName() {

    String externalizedName = GrouperTextContainer.textOrNull("provisioningUiLabelForKey_" + this.key);
    externalizedName = StringUtils.defaultIfEmpty(externalizedName, this.key);
    return externalizedName;
  }
  
  /**
   * name of the target (part of the config keys)
   * @return
   */
  public String getName() {
    return name;
  }
  
  /**
   * group whose members are allowed to assign this target
   * @param groupAllowedToAssign
   */
  public void setGroupAllowedToAssign(String groupAllowedToAssign) {
    this.groupAllowedToAssign = groupAllowedToAssign;
  }
  
  /**
   * group whose members are allowed to assign this target
   * @return
   */
  public String getGroupAllowedToAssign() {
    return groupAllowedToAssign;
  }

  /**
   * should the target be assignable to only one stem
   * @param allowAssignmentsOnlyOnOneStem
   */
  public void setAllowAssignmentsOnlyOnOneStem(boolean allowAssignmentsOnlyOnOneStem) {
    this.allowAssignmentsOnlyOnOneStem = allowAssignmentsOnlyOnOneStem;
  }
  
  /**
   * should the target be assignable to only one stem
   * @return
   */
  public Boolean isAllowAssignmentsOnlyOnOneStem() {
    return allowAssignmentsOnlyOnOneStem;
  }

  /**
   * should the target be only readable and not assignable
   * @param readOnly
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * should the target be only readable and not assignable
   * @return
   */
  public boolean isReadOnly() {
    return readOnly;
  }
  
}
