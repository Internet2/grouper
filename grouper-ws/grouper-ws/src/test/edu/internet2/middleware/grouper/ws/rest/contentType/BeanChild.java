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
 * $Id: BeanChild.java,v 1.1 2008-03-25 05:15:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;


/**
 * child bean
 */
public class BeanChild {
  /** field */
  private String childField1;
  
  /** field */
  private String childField2;
  
  /** field */
  private String[] childStringArray;

  /** int array */
  private int[] childIntegerArray;
  
  /**
   * empty
   */
  public BeanChild() {
    //empty
  }
  
  /**
   * @param _childField1
   * @param _childField2
   * @param _childStringArray
   * @param _childIntegerArray
   */
  public BeanChild(String _childField1, String _childField2, String[] _childStringArray,
      int[] _childIntegerArray) {
    super();
    this.childField1 = _childField1;
    this.childField2 = _childField2;
    this.childStringArray = _childStringArray;
    this.childIntegerArray = _childIntegerArray;
  }


  /**
   * field
   * @return the childField1
   */
  public String getChildField1() {
    return this.childField1;
  }

  
  /**
   * field
   * @param _childField1 the childField1 to set
   */
  public void setChildField1(String _childField1) {
    this.childField1 = _childField1;
  }

  
  /**
   * field
   * @return the childField2
   */
  public String getChildField2() {
    return this.childField2;
  }

  
  /**
   * field
   * @param _childField2 the childField2 to set
   */
  public void setChildField2(String _childField2) {
    this.childField2 = _childField2;
  }

  
  /**
   * field
   * @return the childStringArray
   */
  public String[] getChildStringArray() {
    return this.childStringArray;
  }

  
  /**
   * field
   * @param _childStringArray the childStringArray to set
   */
  public void setChildStringArray(String[] _childStringArray) {
    this.childStringArray = _childStringArray;
  }


  
  /**
   * field
   * @return the childIntegerArray
   */
  public int[] getChildIntegerArray() {
    return this.childIntegerArray;
  }


  
  /**
   * field
   * @param _childIntegerArray the childIntegerArray to set
   */
  public void setChildIntegerArray(int[] _childIntegerArray) {
    this.childIntegerArray = _childIntegerArray;
  }
}
