/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.lang.reflect.*;  
import  java.util.*;

/** 
 * {@link Grouper} session class.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.54 2004-11-06 03:45:06 blair Exp $
 */
public class GrouperSession {

  // Subject this session is running under
  private GrouperMember   subject;
  // The memberID of the session's subject
  private String          subjectID;

  // Internal reference to the Access interface
  private GrouperAccess   intAccess;
  // Internal reference to the Naming interface
  private GrouperNaming   intNaming;
  // FIXME How many of these variables are actually used?
  // FIXME And what is the purpose of those that are used?
  private String sessionID;
  private String startTime;

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
   * @param G {@link Grouper} object.
   * @param s {@link GrouperMember} member object to act as
   * for the duration of this session.
   */
  public void start(GrouperMember s) {
    // Keep track of who we are
    this.subject    = s;
    this.subjectID  = s.id();

    // Register a new session
    this._registerSession();
  }

  /**
   * Stop the {@link Grouper} session.
   * <p>
   * TODO Update <i>grouper_session</i> table.
   */
  public void stop() { 
    // FIXME What do we do here?
    // Maybe? this._init();
    // Wipe out entry from session table?
  }

  /**
   * Return subject of current session as a {@link GrouperMember}
   * object.
   *
   * @return  Subject of current session as a {@link GrouperMember}
   * object.
   */
  public GrouperMember subject() {
    return (GrouperMember) this.subject;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.intAccess  = null;
    this.intNaming  = null;
    this.sessionID  = null;
    this.startTime  = null;
    this.subject    = null;
    this.subjectID  = null;
  }

  /*
   * Instantiate internal references to the  access, naming, and
   * subject interfaces.
   *
   * TODO Is this the right location for such code?
   */ 
  private void _createInterfaces() {
    // Create internal references to the various interfaces
    this.intAccess  = (GrouperAccess)  this._createObject( Grouper.config("interface.access") );
    this.intNaming  = (GrouperNaming)  this._createObject( Grouper.config("interface.naming") );
  }

  /*
   * Instantiate an object -- reflectively
   *
   * TODO Is this the right location for such code?
   */
  private Object _createObject(String name) {
    Object    object      = null;
    Class[]   paramsClass = new Class[]  { GrouperSession.class };
    Object[]  params      = new Object[] { this };

    try {
      Class classType         = Class.forName(name);
      Constructor constructor = classType.getDeclaredConstructor(paramsClass);
      object                  = constructor.newInstance(params);
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  
    return object;
  }
     
  /*
   * Register a new session with the groups registry.
   */
  private void _registerSession() {
    // Create internal representations of the various Grouper
    // interfaces
    this._createInterfaces();

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

