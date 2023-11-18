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

import java.util.ArrayList;
import java.util.List;

/**
 * @author shilen
 */
public class InstrumentationDataCounts {

  private List<Long> timestamps = new ArrayList<Long>();

  /**
   * Add count
   * @param timestampInMillis
   */
  public void addCount(long timestampInMillis) {
    synchronized (this) {
      timestamps.add(timestampInMillis);
    }
  }
  
  /**
   * Clear and return counts
   * @return list of timestamps in millis
   */
  public List<Long> clearCounts() {
    synchronized(this) {
      List<Long> removes = new ArrayList<Long>(timestamps);
      timestamps.clear();
      return removes;
    }
  }
}
