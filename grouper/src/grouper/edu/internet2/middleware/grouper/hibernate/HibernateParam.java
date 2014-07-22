/**
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

/**
 * param for a query
 * @author mchyzer
 *
 */
class HibernateParam {

  /** name of param */
  private String name;
  
  /** value of param */
  private Object value;
  
  /** type of param */
  private Class type;

  /**
   * name or param
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name or param
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value of param
   * @return the value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * value of param
   * @param value1 the value to set
   */
  public void setValue(Object value1) {
    this.value = value1;
  }

  /**
   * type of param
   * @return the type
   */
  public Class getType() {
    return this.type;
  }

  /**
   * to string
   * @return the string
   */
  @Override
  public String toString() {
    return "Param (" + this.type + "): '" + this.name + "'->'" + this.value + "'"; 
  }
  /** no arg constructor */
  public HibernateParam() {
    
  }
  
  /**
   * constructor with fields
   * @param name name
   * @param value value
   * @param type type
   */
  public HibernateParam(String name, Object value, Class type) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  /**
   * type of param
   * @param type1 the type to set
   */
  public void setType(Class type1) {
    this.type = type1;
  }
  
  
}
