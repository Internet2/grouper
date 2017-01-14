/**
 * Copyright 2017 Internet2
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
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;


/**
 * maps to test testgrouper_incremental_loader table
 */
@SuppressWarnings("serial")
public class TestgrouperIncrementalLoader extends GrouperAPI implements Hib3GrouperVersioned {
  
  private long id;
  
  private String subjectId;

  private String subjectIdentifier;

  private String subjectIdOrIdentifier;
  
  private String sourceId;

  private String loaderGroupName;

  private long timestamp;

  private Long completedTimestamp;


  /**
   * @param id
   * @param subjectId
   * @param subjectIdentifier
   * @param subjectIdOrIdentifier
   * @param sourceId
   * @param loaderGroupName
   * @param timestamp
   * @param completedTimestamp
   */
  public TestgrouperIncrementalLoader(long id, String subjectId, String subjectIdentifier,
      String subjectIdOrIdentifier, String sourceId, String loaderGroupName,
      long timestamp, Long completedTimestamp) {
    this.id = id;
    this.subjectId = subjectId;
    this.subjectIdentifier = subjectIdentifier;
    this.subjectIdOrIdentifier = subjectIdOrIdentifier;
    this.sourceId = sourceId;
    this.loaderGroupName = loaderGroupName;
    this.timestamp = timestamp;
    this.completedTimestamp = completedTimestamp;
  }




  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperIncrementalLoader(this.id, this.subjectId, this.subjectIdentifier, this.subjectIdOrIdentifier, this.sourceId, this.loaderGroupName, this.timestamp, this.completedTimestamp);
  }
  
  
  /**
   * 
   */
  public TestgrouperIncrementalLoader() {
    super();
  }

  
  
  /**
   * @return the id
   */
  public long getId() {
    return id;
  }




  
  /**
   * @param id the id to set
   */
  public void setId(long id) {
    this.id = id;
  }




  
  /**
   * @return the subjectId
   */
  public String getSubjectId() {
    return subjectId;
  }




  
  /**
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }




  
  /**
   * @return the subjectIdentifier
   */
  public String getSubjectIdentifier() {
    return subjectIdentifier;
  }




  
  /**
   * @param subjectIdentifier the subjectIdentifier to set
   */
  public void setSubjectIdentifier(String subjectIdentifier) {
    this.subjectIdentifier = subjectIdentifier;
  }




  
  /**
   * @return the subjectIdOrIdentifier
   */
  public String getSubjectIdOrIdentifier() {
    return subjectIdOrIdentifier;
  }




  
  /**
   * @param subjectIdOrIdentifier the subjectIdOrIdentifier to set
   */
  public void setSubjectIdOrIdentifier(String subjectIdOrIdentifier) {
    this.subjectIdOrIdentifier = subjectIdOrIdentifier;
  }




  
  /**
   * @return the sourceId
   */
  public String getSourceId() {
    return sourceId;
  }




  
  /**
   * @param sourceId the sourceId to set
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }




  
  /**
   * @return the loaderGroupName
   */
  public String getLoaderGroupName() {
    return loaderGroupName;
  }




  
  /**
   * @param loaderGroupName the loaderGroupName to set
   */
  public void setLoaderGroupName(String loaderGroupName) {
    this.loaderGroupName = loaderGroupName;
  }




  
  /**
   * @return the timestmp
   */
  public long getTimestamp() {
    return timestamp;
  }




  
  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }




  
  /**
   * @return the completedTimestamp
   */
  public Long getCompletedTimestamp() {
    return completedTimestamp;
  }




  
  /**
   * @param completedTimestamp the completedTimestamp to set
   */
  public void setCompletedTimestamp(Long completedTimestamp) {
    this.completedTimestamp = completedTimestamp;
  } 
}
