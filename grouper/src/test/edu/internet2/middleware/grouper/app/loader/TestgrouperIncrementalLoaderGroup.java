/**
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
 */
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;


/**
 * maps to test testgrouper_incremental_loader_group table
 */
@SuppressWarnings("serial")
public class TestgrouperIncrementalLoaderGroup extends GrouperAPI implements Hib3GrouperVersioned {

  private long id;

  private String loaderGroupName;

  private String groupName;

  private long timestamp;

  private Long completedTimestamp;

  private String syncType;


  /**
   * @param id
   * @param loaderGroupName
   * @param groupName
   * @param timestamp
   * @param completedTimestamp
   */
  public TestgrouperIncrementalLoaderGroup(long id, String loaderGroupName,
      String groupName, long timestamp, Long completedTimestamp) {
    this(id, loaderGroupName, groupName, timestamp, completedTimestamp, null);
  }

  /**
   * @param id
   * @param loaderGroupName
   * @param groupName
   * @param timestamp
   * @param completedTimestamp
   * @param syncType
   */
  public TestgrouperIncrementalLoaderGroup(long id, String loaderGroupName,
      String groupName, long timestamp, Long completedTimestamp, String syncType) {
    this.id = id;
    this.loaderGroupName = loaderGroupName;
    this.groupName = groupName;
    this.timestamp = timestamp;
    this.completedTimestamp = completedTimestamp;
    this.syncType = syncType;
  }



  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return new TestgrouperIncrementalLoaderGroup(this.id, this.loaderGroupName, this.groupName, this.timestamp, this.completedTimestamp, this.syncType);
  }


  /**
   *
   */
  public TestgrouperIncrementalLoaderGroup() {
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
   * @return the groupName
   */
  public String getGroupName() {
    return groupName;
  }





  /**
   * @param groupName the groupName to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }





  /**
   * @return the timestamp
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

  /**
   * @return the syncType
   */
  public String getSyncType() {
    return syncType;
  }

  /**
   * @param syncType the syncType to set
   */
  public void setSyncType(String syncType) {
    this.syncType = syncType;
  }
}
