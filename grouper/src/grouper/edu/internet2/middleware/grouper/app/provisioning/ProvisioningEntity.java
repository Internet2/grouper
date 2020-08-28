package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * entity is a member of a group which is typically a user/account or person
 * @author mchyzer
 *
 */
public class ProvisioningEntity implements ProvisioningUpdatable {

  /**
   * id uniquely identifies this record, might be a target uuid, or subject id
   */
  private String id;
  
  /**
   * login id could be a subject identifier or subject id (optional)
   */
  private String loginId;

  /**
   * name field in the entity (optional)
   */
  private String name;
  
  /**
   * email of entity (optional)
   */
  private String email;
  
  /**
   * more attributes in name/value pairs
   */
  private Map<String, ProvisioningAttribute> attributes = new HashMap<String, ProvisioningAttribute>();

  private Map<MultiKey, Object> internal_fieldsToUpdate = null;
  
  
  private ProvisioningEntityWrapper provisioningEntityWrapper;
  
  /**
   * multikey is either the string "field", "attribute", the second param is field name or attribute name
   * third param is "insert", "update", or "delete"
   * and the value is the old value
   * @return
   */
  public Map<MultiKey, Object> getInternal_fieldsToUpdate() {
    return internal_fieldsToUpdate;
  }

  
  public void setInternal_fieldsToUpdate(Map<MultiKey, Object> internal_fieldsToUpdate) {
    this.internal_fieldsToUpdate = internal_fieldsToUpdate;
  }
  
  
  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @return login id
   */
  public String getLoginId() {
    return this.loginId;
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @param login1
   */
  public void setLoginId(String login1) {
    this.loginId = login1;
  }

  /**
   * name field in the entity (optional)
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name field in the entity (optional)
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * email of entity (optional)
   * @return email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * email of entity (optional)
   * @param email1
   */
  public void setEmail(String email1) {
    this.email = email1;
  }

  /**
   * more attributes in name/value pairs
   * @return
   */
  public Map<String, ProvisioningAttribute> getAttributes() {
    return this.attributes;
  }

  /**
   * more attributes in name/value pairs
   * @param attributes1
   */
  public void setAttributes(Map<String, ProvisioningAttribute> attributes1) {
    this.attributes = attributes1;
  }


  
  public ProvisioningEntityWrapper getProvisioningEntityWrapper() {
    return provisioningEntityWrapper;
  }


  
  public void setProvisioningEntityWrapper(ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrapper = provisioningEntityWrapper;
  }
  
  
}
