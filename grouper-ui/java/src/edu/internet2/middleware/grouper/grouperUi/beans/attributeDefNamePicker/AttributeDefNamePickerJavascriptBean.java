/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker;

import java.util.Map;


/**
 *
 */
public class AttributeDefNamePickerJavascriptBean {

  /** */
  private String description;
  /** */
  private String id;
  /** */
  private String name;
  /** */
  private String displayName;
  /**
   * display name 
   * @return display name
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * display name
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * @param displayName1
   * @param description1
   * @param id1
   * @param name1
   */
  public AttributeDefNamePickerJavascriptBean(
      String description1, String id1, String name1, String displayName1) {
    super();
    this.description = description1;
    
    this.id = id1;
    this.name = name1;
    this.displayName = displayName1;
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
   * @return the id
   */
  public String getId() {
    return this.id;
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

}
