package edu.internet2.middleware.grouper.provisioning;

import java.util.Map;

/**
 * group in target system
 * @author mchyzer
 *
 */
public class TargetGroup {

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   */
  private String id;

  /**
   * name of group in target system.  could be group system name, extension, or other
   */
  private String name;
  
  /**
   * id index in target (optional)
   */
  private Long idIndex;

  /**
   * display name (optional)
   */
  private String displayName;
  
  /**
   * more attributes in name/value pairs
   */
  private Map<String, TargetAttribute> attributes;

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * id index in target (optional)
   * @return id index
   */
  public Long getIdIndex() {
    return this.idIndex;
  }

  /**
   * id index in target (optional)
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * display name (optional)
   * @return display name
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * display name (optional)
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * more attributes in name/value pairs
   * @return attributes
   */
  public Map<String, TargetAttribute> getAttributes() {
    return this.attributes;
  }

  /**
   * more attributes in name/value pairs
   * @param attributes1
   */
  public void setAttributes(Map<String, TargetAttribute> attributes1) {
    this.attributes = attributes1;
  }

}
