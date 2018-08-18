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

package edu.internet2.middleware.grouper.session;

import edu.internet2.middleware.grouper.GrouperSession;


/**
 * result contains the session and if created
 */
public class GrouperSessionResult {

  /**
   * if created this session
   */
  private boolean created = false;
  
  /**
   * @return the created
   */
  public boolean isCreated() {
    return this.created;
  }
  
  /**
   * @param created the created to set
   */
  public void setCreated(boolean created) {
    this.created = created;
  }
  
  /** grouper session */
  private GrouperSession grouperSession;
  
  /**
   * grouper session
   * @return the grouperSession
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }
  
  /**
   * grouper session
   * @param grouperSession the grouperSession to set
   */
  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
  }

  /**
   * if this was just created, close it, otherwise, let the calling creator close it
   */
  public void stopQuietlyIfCreated() {
    if (!this.created) {
      return;
    }
    GrouperSession.stopQuietly(this.grouperSession);
  }
  
}
