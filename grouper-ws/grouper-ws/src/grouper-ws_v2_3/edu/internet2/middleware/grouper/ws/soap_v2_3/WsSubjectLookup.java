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
package edu.internet2.middleware.grouper.ws.soap_v2_3;


/**
 * <pre>
 * template to lookup a subject.
 * 
 * to lookup a group as a subject, use the group uuid (e.g. fa2dd790-d3f9-4cf4-ac41-bb82e63bff66) in the 
 * subject id of the subject lookup.  Optionally you can use g:gsa as
 * the source id.
 * 
 * developers make sure each setter calls this.clearSubject();
 * 
 * </pre>
 * @author mchyzer
 */
public class WsSubjectLookup {

  /** the one id of the subject */
  private String subjectId;

  /** any identifier of the subject */
  private String subjectIdentifier;

  /** optional: source of subject in the subject api source list */
  private String subjectSourceId;

  /**
   * optional: source of subject in the subject api source list
   * @return the subjectSource
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * optional: source of subject in the subject api source list
   * @param subjectSource1 the subjectSource to set
   */
  public void setSubjectSourceId(String subjectSource1) {
    this.subjectSourceId = subjectSource1;
  }

  /**
   * id of the subject
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * id of the subject
   * @param subjectId1 the subjectId to set
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * any identifier of the subject
   * @return the subjectIdentifier
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * any identifier of the subject
   * @param subjectIdentifier1 the subjectIdentifier to set
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * 
   */
  public WsSubjectLookup() {
    //blank
  }
}
