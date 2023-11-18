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
/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup an attribute def via web service
 * 
 * developers make sure each setter calls this.clearAttributeDef();
 * </pre>
 * @author mchyzer
 */
public class WsExternalSubjectLookup {

  /**
   * 
   * @param theIdentifier
   */
  public WsExternalSubjectLookup(String theIdentifier) {
    this.identifier = theIdentifier;
  }
  
  /** name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef */
  private String identifier;

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @return the theName
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @param theName the theName to set
   */
  public void setIdentifier(String theName) {
    this.identifier = theName;
  }

  /**
   * 
   */
  public WsExternalSubjectLookup() {
    //blank
  }

}
