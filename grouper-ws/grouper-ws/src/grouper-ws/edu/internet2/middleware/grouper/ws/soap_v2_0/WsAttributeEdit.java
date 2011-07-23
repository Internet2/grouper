/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * attribute name, value, and if delete
 * @author mchyzer
 *
 */
public class WsAttributeEdit {

  /**
   * constructor with fields
   * @param name1
   * @param value1
   * @param delete1
   */
  public WsAttributeEdit(String name1, String value1, String delete1) {
    this.name = name1;
    this.value = value1;
    this.delete = delete1;
  }

  /**
   * empty constructor
   */
  public WsAttributeEdit() {
    //empty
  }

  /** name of attribute */
  private String name;

  /** value of attribute */
  private String value;

  /** should be T|F */
  private String delete;

  /**
   * provide a helpful toString method based on fields
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * validate this attribute edit, return the error message
   * @return the error message or null if none
   */
  public String validate() {

    //no matter what, the name must be there
    if (StringUtils.isBlank(this.name)) {
      return "All attribute edits must have names";
    }

    boolean deleteBoolean = false;
    //see if boolean ok
    try {
      deleteBoolean = this.deleteBoolean();
    } catch (RuntimeException re) {
      return re.getMessage();
    }
    if (deleteBoolean && !StringUtils.isBlank(this.value)) {
      return "If deleting, value must be empty";
    }
    return null;
  }

  /**
   * convert the delete to a boolean and return
   * @return the boolean
   */
  public boolean deleteBoolean() {
    return GrouperUtil.booleanValue(this.delete, false);
  }

  /**
   * name of attribute
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of attribute
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value of attribute
   * @return the value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  /**
   * if we should delete this attribute
   * @return the delete
   */
  public String getDelete() {
    return this.delete;
  }

  /**
   * @param delete1 the delete to set
   */
  public void setDelete(String delete1) {
    this.delete = delete1;
  }
}
