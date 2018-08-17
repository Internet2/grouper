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

package edu.internet2.middleware.grouper.j2ee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperThreadLocalState;


/**
 *
 */
public class ServletRequestUtils {

  /** logger */
  private static Log LOG = LogFactory.getLog(ServletRequestUtils.class);

  /**
   * 
   */
  public ServletRequestUtils() {
  }

  /**
   * end of request in j2ee call this method, this is a failsafe method
   */
  public static void requestEnd() {
    try {
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.j2eeRequestEndRemoveThreadlocals", true)) {
        GrouperThreadLocalState.removeCurrentThreadLocals();
      }
    } catch (Exception e) {
      LOG.error("Error ending request", e);
    }

  }

}
