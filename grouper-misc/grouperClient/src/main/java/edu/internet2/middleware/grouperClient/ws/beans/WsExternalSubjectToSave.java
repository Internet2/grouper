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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to save a external subject via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsExternalSubjectToSave {

  /** external subject lookup (blank if insert) */
  private WsExternalSubjectLookup wsExternalSubjectLookup;

  /** external subject to save */
  private WsExternalSubject wsExternalSubject;

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * 
   */
  public WsExternalSubjectToSave() {
    // empty constructor
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * @return the wsExternalSubjectLookup
   */
  public WsExternalSubjectLookup getWsExternalSubjectLookup() {
    return this.wsExternalSubjectLookup;
  }

  /**
   * @param wsExternalSubjectLookup1 the wsGroupLookup to set
   */
  public void setWsExternalSubjectLookup(WsExternalSubjectLookup wsExternalSubjectLookup1) {
    this.wsExternalSubjectLookup = wsExternalSubjectLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsExternalSubject getWsExternalSubject() {
    return this.wsExternalSubject;
  }

  /**
   * @param wsExternalSubject1 the wsGroup to set
   */
  public void setWsExternalSubject(WsExternalSubject wsExternalSubject1) {
    this.wsExternalSubject = wsExternalSubject1;
  }
}
