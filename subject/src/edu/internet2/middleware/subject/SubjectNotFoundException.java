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
package edu.internet2.middleware.subject;

/**
 * Indicates that a Subject is not found within a Source.
 */
@SuppressWarnings("serial")
public class SubjectNotFoundException extends RuntimeException {

  
  
  /**
   * subject id
   */
  private String subjectId;
  
  /**
   * subjectId
   * @return subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }
  
  /**
   * subjectId
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * 
   * @param msg
   */
	public SubjectNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public SubjectNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

  /**
   * 
   * @param msg
   */
  public SubjectNotFoundException(String subjectId1, String msg) {
    super(msg);
    this.subjectId = subjectId1;
  }

  /**
   * 
   * @param msg
   * @param cause
   */
  public SubjectNotFoundException(String subjectId1, String msg, Throwable cause) {
    super(msg, cause);
    this.subjectId = subjectId1;
  }

}
