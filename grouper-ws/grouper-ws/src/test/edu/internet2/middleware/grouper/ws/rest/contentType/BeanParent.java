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
 * $Id: BeanParent.java,v 1.1 2008-03-25 05:15:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;


/**
 * parent bean
 */
public class BeanParent {
  /** field */
  private String parentField1;
  
  /** field */
  private String parentField2;
  
  /** field */
  private String[] parentStringArray;

  /** field */
  private int intField;
  
  /** ean child */
  private BeanChild beanChild;

  /** child */
  private BeanChild beanChild2;
  
  /** child */
  private BeanChild[] beanArray;
  
  /** child */
  private BeanChild[] beanArray2;
  
  /**
   * @return the parentField1
   */
  public String getParentField1() {
    return this.parentField1;
  }

  
  /**
   * @param _parentField1 the parentField1 to set
   */
  public void setParentField1(String _parentField1) {
    this.parentField1 = _parentField1;
  }

  
  /**
   * @return the parentField2
   */
  public String getParentField2() {
    return this.parentField2;
  }

  
  /**
   * @param _parentField2 the parentField2 to set
   */
  public void setParentField2(String _parentField2) {
    this.parentField2 = _parentField2;
  }

  
  /**
   * @return the parentStringArray
   */
  public String[] getParentStringArray() {
    return this.parentStringArray;
  }

  
  /**
   * @param _parentStringArray the parentStringArray to set
   */
  public void setParentStringArray(String[] _parentStringArray) {
    this.parentStringArray = _parentStringArray;
  }

  /**
   * empty
   */
  public BeanParent() {
    //empty
  }
  

  /**
   * @param _parentField1
   * @param _parentField2
   * @param _parentStringArray
   * @param _intField
   * @param _beanChild
   * @param _beanChild2
   * @param _beanArray
   * @param _beanArray2
   */
  public BeanParent(String _parentField1, String _parentField2, String[] _parentStringArray,
      int _intField, BeanChild _beanChild, BeanChild _beanChild2, BeanChild[] _beanArray,
      BeanChild[] _beanArray2) {
    super();
    this.parentField1 = _parentField1;
    this.parentField2 = _parentField2;
    this.parentStringArray = _parentStringArray;
    this.intField = _intField;
    this.beanChild = _beanChild;
    this.beanChild2 = _beanChild2;
    this.beanArray = _beanArray;
    this.beanArray2 = _beanArray2;
  }


  /**
   * @return the intField
   */
  public int getIntField() {
    return this.intField;
  }


  
  /**
   * @param _intField the intField to set
   */
  public void setIntField(int _intField) {
    this.intField = _intField;
  }


  
  /**
   * @return the beanChild
   */
  public BeanChild getBeanChild() {
    return this.beanChild;
  }


  
  /**
   * @param _beanChild the beanChild to set
   */
  public void setBeanChild(BeanChild _beanChild) {
    this.beanChild = _beanChild;
  }


  
  /**
   * @return the beanChild2
   */
  public BeanChild getBeanChild2() {
    return this.beanChild2;
  }


  
  /**
   * @param _beanChild2 the beanChild2 to set
   */
  public void setBeanChild2(BeanChild _beanChild2) {
    this.beanChild2 = _beanChild2;
  }


  
  /**
   * @return the beanArray
   */
  public BeanChild[] getBeanArray() {
    return this.beanArray;
  }


  
  /**
   * @param _beanArray the beanArray to set
   */
  public void setBeanArray(BeanChild[] _beanArray) {
    this.beanArray = _beanArray;
  }


  
  /**
   * @return the beanArray2
   */
  public BeanChild[] getBeanArray2() {
    return this.beanArray2;
  }


  
  /**
   * @param _beanArray2 the beanArray2 to set
   */
  public void setBeanArray2(BeanChild[] _beanArray2) {
    this.beanArray2 = _beanArray2;
  }
  
  
  
}
