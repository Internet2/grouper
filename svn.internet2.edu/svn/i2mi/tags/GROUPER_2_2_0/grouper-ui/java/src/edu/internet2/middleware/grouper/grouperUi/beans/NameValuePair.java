/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer $Id: NameValuePair.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
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
