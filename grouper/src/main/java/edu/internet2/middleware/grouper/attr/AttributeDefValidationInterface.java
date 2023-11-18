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
 * @author mchyzer
 *
 */
public interface AttributeDefValidationInterface {

  /**
   * name of this validation
   * @return the name of this validation
   */
  public String name();
  
  /**
   * format an input
   * @param input
   * @param argument0
   * @param argument1
   * @return the string, integer, double, or memberId
   * @throws AttributeDefValidationNotImplemented
   */
  public Object formatToDb(Object input, String argument0, String argument1);
  
  /**
   * validate that an object is not null
   * @param input
   * @param argument0
   * @param argument1
   * @return the error string if there is one
   * @throws AttributeDefValidationNotImplemented
   */
  public String validate(Object input, String argument0, String argument1);

  /**
   * format an input
   * @param input could be integer, string, double, or memberId
   * @param argument0
   * @param argument1
   * @return the representation for a screen
   * @throws AttributeDefValidationNotImplemented
   */
  public String formatFromDb(Object input, String argument0, String argument1);

}
