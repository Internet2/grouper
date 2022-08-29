package edu.internet2.middleware.grouper.app.membershipRequire;

/**
 * 
 * @author mchyzer
 *
 */
public class MembershipRequireConfigBean {

  /**
   * 
   */
  public MembershipRequireConfigBean() {
  }

  /**
   * attribute uuid of attribute assigned
   */
  private String attributeDefNameId;
  
  /**
   * attribute uuid of attribute assigned
   * @return attribute id
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute uuid of attribute assigned
   * @param attributeId1
   */
  public void setAttributeDefNameId(String attributeId1) {
    this.attributeDefNameId = attributeId1;
  }

  /**
   * group uuid of the require group
   */
  private String requireGroupId;

  /**
   * group uuid of the require group
   * @return group uuid
   */
  public String getRequireGroupId() {
    return this.requireGroupId;
  }

  /**
   * group uuid of the require group
   * @param requireGroupId1
   */
  public void setRequireGroupId(String requireGroupId1) {
    this.requireGroupId = requireGroupId1;
  }

  /**
   * configId of this config bean
   */
  private String configId;

  /**
   * configId of this config bean
   * @return config id
   */
  public String getConfigId() {
    return this.configId;
  }

  /**
   * configId of this config bean
   * @param configId1
   */
  public void setConfigId(String configId1) {
    this.configId = configId1;
  }
  
  /**
   *  # ui key to externalize text
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.uiKey\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.uiKey.0 = customVetoCompositeRequireEmployee
   */
  private String uiKey;
  
  /**
   *  # ui key to externalize text
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.uiKey\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.uiKey.0 = customVetoCompositeRequireEmployee
   * @return ui key
   */
  public String getUiKey() {
    return this.uiKey;
  }

  /**
   *  # ui key to externalize text
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.uiKey\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.uiKey.0 = customVetoCompositeRequireEmployee
   * @param uiKey1
   */
  public void setUiKey(String uiKey1) {
    this.uiKey = uiKey1;
  }

  /**
   *  # attribute name that signifies this requirement
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.compositeType\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.attributeName.0 = etc:attribute:customComposite:requireEmployee
   * @return attribute name
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   *  # attribute name that signifies this requirement
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.compositeType\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.attributeName.0 = etc:attribute:customComposite:requireEmployee
   * @param attributeName1
   */
  public void setAttributeName(String attributeName1) {
    this.attributeName = attributeName1;
  }
  
  /**
   *  # group name which is the population group
   *  # {valueType: "group", regex: "^grouper\\.membershipVeto\\.customComposite\\.groupName\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.groupName.0 = org:centralIt:staff:itStaff
   * @return group name
   */
  public String getRequireGroupName() {
    return this.requireGroupName;
  }

  /**
   *  # group name which is the population group
   *  # {valueType: "group", regex: "^grouper\\.membershipVeto\\.customComposite\\.groupName\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.groupName.0 = org:centralIt:staff:itStaff
   * @param groupName1
   */
  public void setRequireGroupName(String groupName1) {
    this.requireGroupName = groupName1;
  }

  /**
   *  # attribute name that signifies this requirement
   *  # {valueType: "string", regex: "^grouper\\.membershipVeto\\.customComposite\\.compositeType\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.attributeName.0 = etc:attribute:customComposite:requireEmployee
   */
  private String attributeName;

  /**
   *  # group name which is the population group
   *  # {valueType: "group", regex: "^grouper\\.membershipVeto\\.customComposite\\.groupName\\.\\d+$"}
   *  #grouper.membershipVeto.customComposite.groupName.0 = org:centralIt:staff:itStaff
   */
  private String requireGroupName;
  
}
