/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.util.versioningV2;

/**
 * 
 * @author mchyzer
 *
 */
public class BeanA {

  /** */
  private String field1;
  
  /** */
  private String field1b;
  
  /**
   * 
   * @return val
   */
  public String getField1b() {
    return this.field1b;
  }

  /**
   * 
   * @param _field1a
   */
  public void setField1b(String _field1a) {
    this.field1b = _field1a;
  }

  /** */
  private String[] field2;
  
  /** */
  private BeanB field3;
  
  /** */
  private BeanB[] field4;

  /**
   * 
   * @return val
   */
  public String getField1() {
    return this.field1;
  }

  /**
   * 
   * @param _field1
   */
  public void setField1(String _field1) {
    this.field1 = _field1;
  }

  /**
   * 
   * @return val
   */
  public String[] getField2() {
    return this.field2;
  }

  /**
   * 
   * @param _field2
   */
  public void setField2(String[] _field2) {
    this.field2 = _field2;
  }

  /**
   * 
   * @return field3
   */
  public BeanB getField3() {
    return this.field3;
  }

  /**
   * 
   * @param _field3
   */
  public void setField3(BeanB _field3) {
    this.field3 = _field3;
  }

  /**
   * 
   * @return val
   */
  public BeanB[] getField4() {
    return this.field4;
  }

  /**
   * 
   * @param _field4
   */
  public void setField4(BeanB[] _field4) {
    this.field4 = _field4;
  }
  
  
}
