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
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;


/**
 * validation of an attribute value
 * @author mchyzer
 *
 */
public class AttributeDefValidation {

  /** id of object */
  private String id;
  
  /** id of the validation def */
  private String validationDefId;
  
  /** argument 0 if applicable */
  private String argument0;
  
  /** argument 1 if applicable */
  private String argument1;

  /**
   * id of object
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of object
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * id of the validation def
   * @return id
   */
  public String getValidationDefId() {
    return this.validationDefId;
  }

  /**
   * id of the validation def
   * @param validationDefId1
   */
  public void setValidationDefId(String validationDefId1) {
    this.validationDefId = validationDefId1;
  }

  /**
   * id of the validation def
   * @return
   */
  public String getArg0() {
    return this.argument0;
  }

  /**
   * id of the validation def
   * @param _arg0
   */
  public void setArg0(String _arg0) {
    this.argument0 = _arg0;
  }

  /**
   * id of the validation def
   * @return arg1
   */
  public String getArg1() {
    return this.argument1;
  }

  /**
   * id of the validation def
   * @param _arg1
   */
  public void setArg1(String _arg1) {
    this.argument1 = _arg1;
  }
  
}
