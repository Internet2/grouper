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
package edu.internet2.middleware.grouper.ldap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author shilen
 */
public class LdapAttribute {

  private String name;
  
  private Collection<String> stringValues;
  
  private Collection<byte[]> binaryValues;

  /**
   * @param name
   */
  public LdapAttribute(String name) {
    this.name = name;
  }
  
  /**
   * @return the stringValues
   */
  public Collection<String> getStringValues() {
    if (stringValues == null) {
      return new ArrayList<String>();
    }
    
    return stringValues;
  }

  
  /**
   * @param stringValues the stringValues to set
   */
  public void setStringValues(Collection<String> stringValues) {
    this.stringValues = stringValues;
  }

  
  /**
   * @return the binaryValues
   */
  public Collection<byte[]> getBinaryValues() {
    if (binaryValues == null) {
      return new ArrayList<byte[]>();
    }
    
    return binaryValues;
  }

  
  /**
   * @param binaryValues the binaryValues to set
   */
  public void setBinaryValues(Collection<byte[]> binaryValues) {
    this.binaryValues = binaryValues;
  }

  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @param value
   */
  public void addStringValue(String value) {
    if (stringValues == null) {
      stringValues = new ArrayList<String>();
    }
    
    stringValues.add(value);
  }
  
  /**
   * @param value
   */
  public void addBinaryValue(byte[] value) {
    if (binaryValues == null) {
      binaryValues = new ArrayList<byte[]>();
    }
    
    binaryValues.add(value);
  }
}
