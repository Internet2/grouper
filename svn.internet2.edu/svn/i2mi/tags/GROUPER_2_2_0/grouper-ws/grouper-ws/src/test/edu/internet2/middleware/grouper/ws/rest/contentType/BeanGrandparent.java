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
/*
 * @author mchyzer
 * $Id: BeanGrandparent.java,v 1.1 2008-03-25 05:15:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;


/**
 *
 */
public class BeanGrandparent {
  
  /** field 1 */
  private String field1;
  
  /** field 2*/
  private String field2;
  
  /** string array */
  private String[] stringArray;
  
  /** field */
  private BeanParent beanParent;
  
  /** field */
  private BeanParent[] beanParents;

  /**
   * empty
   */
  public BeanGrandparent() {
    //empty
  }
  
  /**
   * @param _field1
   * @param _field2
   * @param _stringArray
   * @param _beanParent
   * @param _beanParents
   */
  public BeanGrandparent(String _field1, String _field2, String[] _stringArray,
      BeanParent _beanParent, BeanParent[] _beanParents) {
    super();
    this.field1 = _field1;
    this.field2 = _field2;
    this.stringArray = _stringArray;
    this.beanParent = _beanParent;
    this.beanParents = _beanParents;
  }


  /**
   * @return the field1
   */
  public String getField1() {
    return this.field1;
  }

  
  /**
   * @param _field1 the field1 to set
   */
  public void setField1(String _field1) {
    this.field1 = _field1;
  }

  
  /**
   * @return the field2
   */
  public String getField2() {
    return this.field2;
  }

  
  /**
   * @param _field2 the field2 to set
   */
  public void setField2(String _field2) {
    this.field2 = _field2;
  }

  
  /**
   * @return the stringArray
   */
  public String[] getStringArray() {
    return this.stringArray;
  }

  
  /**
   * @param _stringArray the stringArray to set
   */
  public void setStringArray(String[] _stringArray) {
    this.stringArray = _stringArray;
  }

  
  /**
   * @return the beanParent
   */
  public BeanParent getBeanParent() {
    return this.beanParent;
  }

  
  /**
   * @param _beanParent the beanParent to set
   */
  public void setBeanParent(BeanParent _beanParent) {
    this.beanParent = _beanParent;
  }

  
  /**
   * @return the beanParents
   */
  public BeanParent[] getBeanParents() {
    return this.beanParents;
  }

  
  /**
   * @param _beanParents the beanParents to set
   */
  public void setBeanParents(BeanParent[] _beanParents) {
    this.beanParents = _beanParents;
  }
}
