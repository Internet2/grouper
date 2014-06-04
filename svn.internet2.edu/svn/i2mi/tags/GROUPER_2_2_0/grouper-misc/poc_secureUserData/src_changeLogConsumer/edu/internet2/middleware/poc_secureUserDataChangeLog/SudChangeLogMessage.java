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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserDataChangeLog;


/**
 * simple json message for xml
 */
public class SudChangeLogMessage {

  /** rowGroupChange, or permissionRefresh */
  private String changeType;
  
  /** if rowGroupChange, row group extension */
  private String rowGroupExtension;

  /** if rowGroupChange, subjectId */
  private String rowSubjectId;

  
  /**
   * rowGroupChange, or permissionRefresh
   * @return the changeType
   */
  public String getChangeType() {
    return this.changeType;
  }

  
  /**
   * rowGroupChange, or permissionRefresh
   * @param changeType1 the changeType to set
   */
  public void setChangeType(String changeType1) {
    this.changeType = changeType1;
  }

  
  /**
   * if rowGroupChange, row group extension
   * @return the rowGroupExtension
   */
  public String getRowGroupExtension() {
    return this.rowGroupExtension;
  }

  
  /**
   * if rowGroupChange, row group extension
   * @param rowGroupExtension1 the rowGroupExtension to set
   */
  public void setRowGroupExtension(String rowGroupExtension1) {
    this.rowGroupExtension = rowGroupExtension1;
  }

  
  /**
   * if rowGroupChange, subjectId
   * @return the rowSubjectId
   */
  public String getRowSubjectId() {
    return this.rowSubjectId;
  }

  
  /**
   * if rowGroupChange, subjectId
   * @param rowSubjectId1 the rowSubjectId to set
   */
  public void setRowSubjectId(String rowSubjectId1) {
    this.rowSubjectId = rowSubjectId1;
  }
  
}
