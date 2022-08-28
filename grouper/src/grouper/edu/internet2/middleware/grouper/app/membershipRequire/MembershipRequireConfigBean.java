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
