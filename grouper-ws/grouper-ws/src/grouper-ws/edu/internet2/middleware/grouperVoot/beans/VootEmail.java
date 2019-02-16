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

package edu.internet2.middleware.grouperVoot.beans;

/**
 * Class representing VOOT email field in the object representing a person.
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public class VootEmail {
  /** Type of email e.g. 'type' */
  private String type;
  
  /** Value of email e.g. john@smith.edu */
  private String value;
  
  /**
   * Method to check if an object is equal to the current object.
   * @param otherVootEmail the other object to check
   */
  @Override
  public boolean equals(Object otherVootEmail) {
    if (otherVootEmail instanceof VootEmail) {
      VootEmail other = (VootEmail) otherVootEmail;
      if (!other.getType().equals(type)) return false;
      if (!other.getValue().equals(value)) return false;
      
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Type of email. Should be a value between "home", "work", "other". e.g. 'work'
   * @return type of the email.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Type of email. Should be a value between "home", "work", "other". e.g. 'work'
   * @param type1 the tile of the email.
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * Value of email e.g. john@smith.edu
   * @return value the email address.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Value of email e.g. john@smith.edu
   * @param value1 the email address.
   */
  public void setValue(String value1) {
    this.value = value1;
  }
  
  /**
   * Enum to represnet email types, as supported by VOOT protocol.
   * 
   * @author Andrea Biancini <andrea.biancini@gmail.com>
   */
  public enum MailTypes {
    /** Work type for email address */
    WORK ("work"),
    /** Home type for email address */
    HOME ("home"),
    /** Generic other type for email address */
    OTHER ("other");

    private final String type;       

    private MailTypes(String type1) {
      this.type = type1;
    }

    public String toString(){
       return type;
    }
  }
}
