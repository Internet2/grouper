package edu.internet2.middleware.grouper.app.provisioning;


/**
 * name value pair could be multi valued
 * @author mchyzer
 *
 */
public class ProvisioningAttribute {

  /**
   * if this attribute represents a membership keep that link here
   */
  private ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
  

  /**
   * if this attribute represents a membership keep that link here
   * @return
   */
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }

  /**
   * if this attribute represents a membership keep that link here
   * @param provisioningMembershipWrapper
   */
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }

  /**
   * name of attribute
   */
  private String name;
  
  /**
   * value could be multi valued
   */
  private Object value;

  /**
   * name of attribute
   * @return name of attribute
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of attribute
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value could be multi valued
   * @return value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * value could be multi valued
   * @param value1
   */
  public void setValue(Object value1) {
    this.value = value1;
  }
  
}
