/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;


/** 
 * Class representing a {@link Grouper} session.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.60 2004-11-29 19:05:17 blair Exp $
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
      Grouper.LOGGER.info("Started session for " + s);
      return true;
    } 
    Grouper.LOGGER.info("Failed to start session for " + s);
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
      Grouper.LOGGER.info("Failed to stop session");
      return false;
    }
    Grouper.LOGGER.info("Stopped session for " + this.subject);
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

