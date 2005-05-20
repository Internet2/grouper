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


import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.lang.reflect.*;
import  net.sf.hibernate.*;


/** 
 * Class modeling a {@link Grouper} session.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.93 2005-05-20 15:46:36 blair Exp $
 */
public class GrouperSession implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */

  // Member & Subject that this session is running under
  private transient GrouperMember   m; 
  private transient Subject         subject;

  // Object containing the Hibernate session for this session
  private transient DbSess          dbSess;

  // Access and Naming privilege interfaces
  private transient GrouperAccess   access; 
  private transient GrouperNaming   naming; 

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
    this.createInterfaces();
  }

  /* (!javadoc)
   * Construct a new GrouperSession object and assign it a Subject.
   */
  private GrouperSession(Subject subj) {
    this.createInterfaces();
    this.dbSess   = new DbSess(); 
    this.subject  = subj;
    this.m        = GrouperMember.load(subj);
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
   * @param   subj  I2MI {@link Subject} to act as for the duration
   *   of this session.
   * @return  {@link GrouperSession} object if session started.
   */
  public static GrouperSession start(Subject subj) {
    // TODO Plugin an external session handling mechanism?  Yes, please!
    // TODO Should I cache privs + memberships?
    GrouperSession s = null;
    // Register the Grouper session
    if (subj != null) {
      s = new GrouperSession(subj);
      s.dbSess.txStart();
      /*
       * FIXME Until I find the time to identify a better way of
       * managing sessions -- which I *know* exists -- be crude 
       * about it. 
       */
      s.setSessionID( new GrouperUUID().toString() );
      java.util.Date now = new java.util.Date();
      s.setStartTime( Long.toString(now.getTime()) );
      s.save();
      s.dbSess.txCommit();
      Grouper.log().sessionStart(s);
    }
    return s;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve this session's {@link GrouperAccess} access privilege resolver.
   * <p />
   * @return  {@link GrouperAccess} object.
   */
  public GrouperAccess access() {
    return this.access;
  }

  /**
   * Retrieve this session's {@link GrouperNaming} naming privilege resolver.
   * <p />
   * @return  {@link GrouperNaming} object.
   */
  public GrouperNaming naming() {
    return this.naming;
  }

  /**
   * Stop this session.
   * <p />
   * @return  True if session stopped.
   */
  public boolean stop() { 
    boolean rv = false;
    if (this.sessionID != null) {
      // Remove from the grouper_session table
      this.dbSess.txStart();
      this.delete();
      this.dbSess.txCommit();
    }
    this.dbSess.stop();
    Grouper.log().sessionStop(this);
    return rv;
  }

  /**
   * Retrieve this session's {@link Subject} object.
   * <p />
   * @return  A {@link Subject} object.
   */
  public Subject subject() {
    return (Subject) this.subject;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Simple object validation.
   */
  protected static void validate(GrouperSession s) {
    if (s == null) {
      throw new RuntimeException("session is null");
    }
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Return the database session.
   */
  protected DbSess dbSess() {
    return this.dbSess;
  }

  /*
   * Return the session id
   */
  protected String id() {
    return this.getSessionID();
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Instantiate an interface reflectively
   */
  private static Object createInterface(String name) {
    try {
      Class classType     = Class.forName(name);
      Class[] paramsClass = new Class[] { };
      try {
        Constructor con = classType.getDeclaredConstructor(paramsClass);
        Object[] params = new Object[] { };
        try {
          return con.newInstance(params);
        } catch (Exception e) {
          throw new RuntimeException("Unable to instantiate class: " + name);
        }
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Unable to find constructor for class: " + name);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Unable to find class: " + name);
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Create and attach privilege interfaces to session.
   */
  private void createInterfaces() {
    this.access = (GrouperAccess) createInterface(
                    Grouper.config("interface.access")
                  );
    this.naming = (GrouperNaming) createInterface(
                    Grouper.config("interface.naming")
                  );
  }

  /*
   * Delete a session from the groups registry.
   */
  private void delete() {
    try {
      this.dbSess.session().delete(this);
    } catch (HibernateException e) {
      throw new RuntimeException("Error deleting sessin: " + e);
    }
  }

  // Deserialize the session
  private void readObject(ObjectInputStream ois)
                 throws ClassNotFoundException, IOException 
  {
    // Perform default deserialization
     ois.defaultReadObject();

    // Open a Hibernate session
    this.dbSess = new DbSess(); 

    // Restore GrouperMember object
    this.m = GrouperMember.load(this, this.memberID);

    // Restore Subject object
    this.subject = SubjectFactory.getSubject(m.subjectID(), m.typeID());
  }

  // Serialize the session
  private void writeObject(ObjectOutputStream oos) throws IOException {
    // Stop the Hibernate session
    this.dbSess.stop();

    // Perform default serialization
    oos.defaultWriteObject();
  }

  /*
   * Save the session to the groups registry.
   */
  private void save() {
    try {
      this.dbSess.session().save(this);
    } catch (HibernateException e) {
      throw new RuntimeException("Error saving session: " + e);
    }
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

