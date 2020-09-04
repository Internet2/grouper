package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * group in target system
 * @author mchyzer
 *
 */
public class ProvisioningGroup extends ProvisioningUpdatable {
  
  private ProvisioningGroupWrapper provisioningGroupWrapper;
  
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

  public ProvisioningGroupWrapper getProvisioningGroupWrapper() {
    return provisioningGroupWrapper;
  }


  public void setProvisioningGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrapper = provisioningGroupWrapper;
  }

  public String toString() {
    StringBuilder result = new StringBuilder("ProvisioningGroup(");
    boolean firstField = true;
    if (this.id != null) {result.append(", id: ").append("id");}
    return result.append(")").toString();
  }
  
}
