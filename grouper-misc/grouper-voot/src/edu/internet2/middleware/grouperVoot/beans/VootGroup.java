package edu.internet2.middleware.grouperVoot.beans;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * voot group bean that gets transformed to json
 * @author mchyzer
 *
 */
public class VootGroup {
  
  /**
   * default constructor
   */
  public VootGroup() {
    
  }
  
  /**
   * construct with group
   * @param group
   */
  public VootGroup(Group group) {
    this.setId(group.getName());
    this.setName(group.getDisplayName());
    this.setDescription(group.getDescription());
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    VootGroup vootGroup = new VootGroup();
    
    vootGroup.setDescription("the description");
    vootGroup.setId("the id");
    vootGroup.setName("the name");
    
    String json = GrouperUtil.jsonConvertToNoWrap(vootGroup);
    
    System.out.println(json);
    
    vootGroup = (VootGroup)GrouperUtil.jsonConvertFrom(json, VootGroup.class);
    
    System.out.println("ID is: " + vootGroup.getId());
    
  }
  
  /**
   * system name of group
   */
  private String id;

  /**
   * display name of group
   */
  private String name;
  
  /**
   * description of group
   */
  private String description;

  /**
   * manager, admin, member
   */
  private String voot_membership_role;
  
  /**
   * manager, admin, member
   * @return manager, admin, member
   */
  public String getVoot_membership_role() {
    return this.voot_membership_role;
  }

  /**
   * manager, admin, member
   * @param voot_membership_role1
   */
  public void setVoot_membership_role(String voot_membership_role1) {
    this.voot_membership_role = voot_membership_role1;
  }

  /**
   * system name of group
   * @return system name of group
   */
  public String getId() {
    return this.id;
  }

  /**
   * system name of group
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * display name of group
   * @return display name of group
   */
  public String getName() {
    return this.name;
  }

  /**
   * display name of group
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * description of group
   * @return description of group
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description of group
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  
  
}
