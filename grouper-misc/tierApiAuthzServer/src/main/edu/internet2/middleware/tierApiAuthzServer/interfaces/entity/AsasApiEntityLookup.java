/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierApiAuthzServer.interfaces.entity;

import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasFilterJ2ee;

/**
 * lookup entity lookup
 * @author mchyzer
 *
 */
public class AsasApiEntityLookup {

  /**
   * logged in entity
   * @return the lookup
   */
  public static AsasApiEntityLookup retrieveLoggedInUser() {

    AsasApiEntityLookup asasApiEntityLookup = new AsasApiEntityLookup();
    String lookupString = TaasFilterJ2ee.retrieveUserPrincipalNameFromRequest();
    asasApiEntityLookup.setLookupString(lookupString);
    return asasApiEntityLookup;

  }

  /** string to lookup entity */
  private String lookupString;

  /** handle name of a way to refer to an entity */
  private String handleName;
  
  /** handle value of a way to refer to an entity */
  private String handleValue;

  /**
   * string to lookup entity
   * @return the lookupString
   */
  public String getLookupString() {
    return this.lookupString;
  }

  /**
   * string to lookup entity
   * @param lookupString1 the lookupString to set
   */
  public void setLookupString(String lookupString1) {
    this.lookupString = lookupString1;
  }

  /**
   * handle name of a way to refer to an entity
   * @return the handleName
   */
  public String getHandleName() {
    return handleName;
  }

  /**
   * handle value of a way to refer to an entity
   * @return the handleValue
   */
  public String getHandleValue() {
    return handleValue;
  }

  /**
   * handle name of a way to refer to an entity
   * @param handleName the handleName to set
   */
  public void setHandleName(String handleName) {
    this.handleName = handleName;
  }

  /**
   * handle value of a way to refer to an entity
   * @param handleValue the handleValue to set
   */
  public void setHandleValue(String handleValue) {
    this.handleValue = handleValue;
  }
  
}
