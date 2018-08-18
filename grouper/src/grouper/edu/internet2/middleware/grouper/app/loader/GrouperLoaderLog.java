/**
 * Copyright 2018 Internet2
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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * logger for loader events
 */
public class GrouperLoaderLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderLog.class);

  /**
   * 
   * @return true if debug enabled
   */
  static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }
  
  /**
   * 
   * @param theString
   */
  static void logDebug(String theString) {
    LOG.debug(theString);
  }
  
}
