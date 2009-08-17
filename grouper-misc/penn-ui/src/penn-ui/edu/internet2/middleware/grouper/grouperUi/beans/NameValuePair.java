/**
 * @author mchyzer $Id: NameValuePair.java,v 1.1 2009-08-17 17:48:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Name value pair
 * 
 */
public class NameValuePair implements Serializable {

  /**
   * Default constructor.
   * 
   */
  public NameValuePair() {
  }

  /**
   * Constructor.
   * @param name1 The name.
   * @param value1 The value.
   */
  public NameValuePair(String name1, String value1) {
    this.name = name1;
    this.value = value1;
  }

  /**
   * Name.
   */
  private String name = null;

  /**
   * Value.
   */
  private String value = null;

  /**
   * Set the name.
   *
   * @param name1 The new name
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * Return the name.
   *
   * @return String name The name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the value.
   *
   * @param value1 The new value.
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  /**
   * Return the current value.
   *
   * @return String value The current value.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.name + ": " + this.value;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object another) {
    if (another == this) {
      return true;
    }
    if (!(another instanceof NameValuePair)) {
      return false;
    }
    NameValuePair anotherPair = (NameValuePair) another;
    return StringUtils.equals(this.name, anotherPair.name) && StringUtils.equals(this.value, anotherPair.value);
  }

  /**
   * the hashcode
   * @return the hashcode
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.name).append(this.value).hashCode();
  }
  
}
