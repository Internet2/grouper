package edu.internet2.middleware.grouper.provisioning;

import java.util.Map;

/**
 * entity is a member of a group which is typically a user/account or person
 * @author mchyzer
 *
 */
public class TargetEntity {

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
  private Map<String, TargetAttribute> attributes;

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
