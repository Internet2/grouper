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
package edu.internet2.middleware.grouper.instrumentation;

import java.util.Date;
import java.util.Map;

/**
 * @author shilen
 */
public class InstrumentationDataInstanceCounts {

  private Date startTime;
  
  private Long duration;
  
  private Date createdOn;
  
  private Map<String, Long> counts;

  
  /**
   * @return the startTime
   */
  public Date getStartTime() {
    return startTime;
  }

  
  /**
   * @param startTime the startTime to set
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  
  /**
   * @return the duration
   */
  public Long getDuration() {
    return duration;
  }

  
  /**
   * @param duration the duration to set
   */
  public void setDuration(Long duration) {
    this.duration = duration;
  }

  
  /**
   * @return the counts
   */
  public Map<String, Long> getCounts() {
    return counts;
  }

  
  /**
   * @param counts the counts to set
   */
  public void setCounts(Map<String, Long> counts) {
    this.counts = counts;
  }


  
  /**
   * @return the createdOn
   */
  public Date getCreatedOn() {
    return createdOn;
  }


  
  /**
   * @param createdOn the createdOn to set
   */
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }
}
