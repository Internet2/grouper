package edu.internet2.middleware.grouper.provisioning;

import java.util.Map;

/**
 * tuple of group and entity in target system
 * @author mchyzer
 *
 */
public class TargetMembership {

  /**
   * id of membership (optional)
   */
  private String id;
  
  /**
   * group of membership
   */
  private TargetGroup targetGroup;
  
  /**
   * entity of membership
   */
  private TargetEntity targetEntity;

  /**
   * more attributes in name/value pairs
   */
  private Map<String, TargetAttribute> attributes;

  /**
   * id of membership (optional)
   * @return id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * id of membership (optional)
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * group of membership
   * @return group
   */
  public TargetGroup getTargetGroup() {
    return this.targetGroup;
  }

  /**
   * group of membership
   * @param targetGroup1
   */
  public void setTargetGroup(TargetGroup targetGroup1) {
    this.targetGroup = targetGroup1;
  }

  /**
   * entity of membership
   * @return the entity
   */
  public TargetEntity getTargetEntity() {
    return this.targetEntity;
  }

  /**
   * entity of membership
   * @param targetEntity1
   */
  public void setTargetEntity(TargetEntity targetEntity1) {
    this.targetEntity = targetEntity1;
  }

  /**
   * more attributes in name/value pairs
   * @return attributes
   */
  public Map<String, TargetAttribute> getAttributes() {
    return attributes;
  }

  /**
   * more attributes in name/value pairs
   * @param attributes1
   */
  public void setAttributes(Map<String, TargetAttribute> attributes1) {
    this.attributes = attributes1;
  }
  
}
