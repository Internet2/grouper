/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
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
import  edu.internet2.middleware.grouper.database.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;


/** 
 * Class modeling a {@link Grouper} session.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.78 2005-03-07 19:30:41 blair Exp $
 */
public class GrouperSession implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  // Member & Subject that this session is running under
  private transient GrouperMember m; 
  private transient Subject       subject;

  // Object containing the Hibernate session for this session
  private transient Session       dbSess;

  /* 
   * The id of the session's subject.  Despite the fact that I can get
   * this via the subject object, I stash it into a variable to play
   * nicer with Hibernate.
   */
  private String  memberID;
  private String  sessionID;
  private String  startTime;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperSession() {
    this._init();
  }

  /* (!javadoc)
   * Construct a new GrouperSession object and assign it a Subject.
   */
  private GrouperSession(Subject subj) {
    this._init();
    this.subject  = subj;
    this.m        = GrouperMember.load(this, subj);
    if (m == null) {
      throw new RuntimeException("Unable to load member object");
    }
    this.memberID = m.memberID();
  }


  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * Start a {@link Grouper} session.
   * <p />
   * @param   s   I2MI {@link Subject} to act as for the duration
   *   of this session.
   * @return  True if session started.
   */
  public static GrouperSession start(Subject s) {
    // TODO Plugin an external session handling mechanism?  Yes, please!
    // TODO Should I cache privs + memberships?
    GrouperSession gs = null;
    // Register the Grouper session
    if (s != null) {
      gs = new GrouperSession(s);
      boolean rv = gs._registerSession();
      if (rv) {
        // Initialize the Hibernate session
       gs.dbSess = new Session(); 
      }
      Grouper.log().sessionStart(rv, gs);
    }
    return gs;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Stop the {@link Grouper} session.
   * <p />
   * @return  True if session stopped.
   */
  public boolean stop() { 
    boolean rv = false;
    if (this.sessionID != null) {
      if (GrouperBackend.sessionDel(this)) {
        rv = true;
      }
    }
    this.dbSess.stop();
    Grouper.log().sessionStop(rv, this);
    return rv;
  }

  /**
   * Retrieve session's {@link Subject}.
   * <p />
   * @return  A {@link Subject} object.
   */
  public Subject subject() {
    return (Subject) this.subject;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Return the database session.
   */
  protected Session dbSess() {
    return this.dbSess;
  }

  /*
   * Return the session id
   */
  protected String id() {
    return this.getSessionID();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.dbSess     = null;
    this.memberID   = null;
    this.sessionID  = null;
    this.startTime  = null;
    this.subject    = null;
  }

  // Deserialize an object
  private void readObject(ObjectInputStream ois)
                 throws ClassNotFoundException, IOException 
  {
    // Perform default deserialization
     ois.defaultReadObject();

    // Restore GrouperMember object
    this.m = GrouperMember.load(this, this.memberID);

    // Restore Subject object
    this.subject = GrouperSubject.load(m.subjectID(), m.typeID());
  }

  /*
   * Register a new session with the groups registry.
   */
  private boolean _registerSession() {
    // TODO Make this configurable.  Or something.
    // GrouperBackend.sessionsCull();

    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    this.setSessionID( GrouperBackend.uuid() );
    java.util.Date now = new java.util.Date();
    this.setStartTime( Long.toString(now.getTime()) );

    // And now save the session
    GrouperBackend.sessionAdd(this);

    return true;
  }


  /*
   * HIBERNATE
   */
  
  private String getMemberID() {
    return this.memberID;
  }

  private void setMemberID(String memberID) {
    this.memberID = memberID;
  }

  private String getSessionID() {
    return this.sessionID;
  }

  private void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  private String getStartTime() {
    return this.startTime;
  }

  private void setStartTime(String startTime) {
    this.startTime = startTime;
  }

}

