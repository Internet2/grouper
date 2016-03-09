/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;


/**
 *
 */
public class AsasApiEntity {

  /**
   * 
   */
  public AsasApiEntity() {
  }

  private String sourceId;
  
  private String subjectId;

  
  /**
   * @return the sourceId
   */
  public String getSourceId() {
    return this.sourceId;
  }

  
  /**
   * @param sourceId the sourceId to set
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  
  /**
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  
  /**
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }
  
  
  
}
