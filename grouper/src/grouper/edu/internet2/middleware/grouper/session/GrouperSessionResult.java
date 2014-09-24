/**
 * @author mchyzer
 * $Id$
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
