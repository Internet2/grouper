/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;


/**
 * group returned from duo
 */
public class GrouperDuoGroup {

  /**
   * id
   */
  private String id;
  
  /**
   * name
   */
  private String name;
  
  /**
   * description
   */
  private String description;
  
  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "GrouperDuoGroup [id=" + this.id + ", name=" + this.name + ", description=" + this.description + "]";
  }

  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }



  
  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }



  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }



  
  /**
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }



  
  /**
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }



  /**
   * 
   */
  public GrouperDuoGroup() {
  }

}
