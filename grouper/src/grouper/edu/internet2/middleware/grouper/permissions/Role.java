/**
 * @author mchyzer
 * $Id: Role.java,v 1.3 2009-09-17 22:40:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;


/**
 *
 */
public interface Role extends GrouperSetElement {

  /**
   * delete this role.  Note if the role participates in role
   * inheritance, you need to break that inheritance first
   */
  public void delete();
  
  /**
   * uuid of role
   * @return id
   */
  public String getId();
  
  /**
   * name of role
   * @return name
   */
  public String getName();

  /**
   * description of role, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @return the description
   */
  public String getDescription();

  /**
   * displayExtension of role
   * @return display extension
   */
  public String getDisplayExtension();

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayName();

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtension();

  /**
   * stem that this attribute is in
   * @return the stem id
   */
  public String getStemId();

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @param description1
   */
  public void setDescription(String description1);

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtension(String displayExtension1);

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayName(String displayName1);

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtension(String extension1);

  /**
   * id of this attribute def name
   * @param id1
   */
  public void setId(String id1);

  /**
   * 
   * @param name1
   */
  public void setName(String name1);

  /**
   * stem that this attribute is in
   * @param stemId1
   */
  public void setStemId(String stemId1);
  
  /**
   * delegate calls to this class for role hierarchy stuff
   * @return the delegate
   */
  public RoleInheritanceDelegate getRoleInheritanceDelegate();
}
