/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;


/** 
 * Class representing a {@link Grouper} session.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.58 2004-11-23 19:43:26 blair Exp $
 */
public class GrouperSession {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  // Subject this session is running under
  private Subject subject;
  // The subjectID of the session's subject
  // The id of the session's subject.  Despite the fact that I can get
  // this via the subject object, I stash it into a variable to play
  // nicer with Hibernate.
  private String  subjectID;
  // FIXME
  private String sessionID;
  private String startTime;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a session object that will provide a context for future
   * operations.
   */
  public GrouperSession() {
    this._init();
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Start a {@link Grouper} session.
   * <p>
   * TODO Plugin an external session handling mechanism?  Yes, please.
   * TODO Cache privs|memberships?
   *
   * @param   s   {@link Subject} subject object to act as
   *  for the duration of this session.
   * @return  Boolean true if successful, false otherwise.
   */
  public boolean start(Subject s) {
    // Keep track of who we are
    this.subject    = s;
    this.subjectID  = s.getId();

    // Register a new session
    if (this._registerSession()) {
      return true;
    } 
    return false;
  }

  /**
   * Stop the {@link Grouper} session.
   * <p>
   * TODO Update <i>grouper_session</i> table.
   */
  public boolean stop() { 
    // FIXME What do we do here?
    // Maybe? this._init();
    // Wipe out entry from session table?
    if (this.subject == null || this.subjectID == null) {
      return false;
    }
    return true;
  }

  /**
   * Return subject of current session as a {@link Subject}
   * object.
   *
   * @return  Subject of current session as a {@link Subject}
   * object.
   */
  public Subject subject() {
    return (Subject) this.subject;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.sessionID  = null;
    this.startTime  = null;
    this.subject    = null;
    this.subjectID  = null;
  }

  /*
   * Register a new session with the groups registry.
   */
  private boolean _registerSession() {
    // TODO Make this configurable.  Or something.
    GrouperBackend.cullSessions();

    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date now = new java.util.Date();

    // XXX Switch to a generated sequence?
    this.setSessionID( Long.toString(now.getTime()) );
    // TODO Switch to GMT/UTC
    this.setStartTime( Long.toString(now.getTime()) );

    // And now save the session
    GrouperBackend.addSession(this);

    return true;
  }


  /*
   * HIBERNATE
   */
  
  private String getSessionID() {
    return this.sessionID;
  }

  private void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  private String getSubjectID() {
    return this.subjectID;
  }

  private void setSubjectID(String subjectID) {
    this.subjectID = subjectID;
  }

  private String getStartTime() {
    return this.startTime;
  }

  private void setStartTime(String startTime) {
    this.startTime = startTime;
  }
}

