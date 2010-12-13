/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


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
    this.validate();
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
    this.validate();
  }

  /**
   * @param subjectId1
   * @param subjectSource1
   * @param subjectIdentifier1
   */
  public WsSubjectLookup(String subjectId1, String subjectSource1,
      String subjectIdentifier1) {
    this.subjectId = subjectId1;
    this.subjectSourceId = subjectSource1;
    this.subjectIdentifier = subjectIdentifier1;
    this.validate();
  }

  /**
   * validate the subject lookup
   */
  public void validate() {
    //CH 20101213: You can set both if searching by one or the other if they are equal
    if (!GrouperClientUtils.isBlank(this.subjectId) && !GrouperClientUtils.isBlank(this.subjectIdentifier)
        && !GrouperClientUtils.equals(this.subjectId, this.subjectIdentifier)) {
      throw new RuntimeException("You must only specify the subjectId '" + this.subjectId 
          + "' or subjectIdentifer '" + this.subjectIdentifier + "', but not both (unless they are equal and searching by idOrIdentifier)");
    }
  }
  
  /**
   * 
   */
  public WsSubjectLookup() {
    //blank
  }
}
