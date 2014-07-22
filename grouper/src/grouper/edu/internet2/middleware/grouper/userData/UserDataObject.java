/**
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
 */
package edu.internet2.middleware.grouper.userData;

/**
 * generic object in the user data
 * @author mchyzer
 */
public class UserDataObject {

  /**
   * construct with fields
   * @param uuid
   * @param theTimestamp
   */
  public UserDataObject(String uuid, long theTimestamp) {
    super();
    this.uuid = uuid;
    this.timestamp = theTimestamp;
  }

  /**
   * 
   * @param uuid
   */
  public UserDataObject(String uuid) {
    super();
    this.uuid = uuid;
  }

  /**
   * 
   */
  public UserDataObject() {
    super();
  }

  /**
   * uuid of the object
   */
  private String uuid;
  
  /**
   * timestamp that the thing was last favorited or whatever
   */
  private Long timestamp;


  /**
   * uuid of the object
   * @return
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * uuid of the object
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * timestamp that the thing was last favorited or whatever
   * @return timestamp in millis since 1970
   */
  public Long getTimestamp() {
    return this.timestamp;
  }

  /**
   * timestamp that the thing was last favorited or whatever
   * @param timestamp1 in millis since 1970
   */
  public void setTimestamp(Long timestamp1) {
    this.timestamp = timestamp1;
  }
  
}
