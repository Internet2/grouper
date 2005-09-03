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
import  java.util.*;
import  java.lang.reflect.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.ToStringBuilder;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * Class modeling a {@link Grouper} session.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.106 2005-09-03 04:26:40 blair Exp $
 */
public class GrouperSession implements Serializable {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static GrouperSession rs;
  private static Log            log = LogFactory.getLog(GrouperSession.class);

  /*
   * PRIVATE INSTANCE VARIABLES
   */

  // Member & Subject that this session is running under
  private transient SimpleCache     cache = new SimpleCache();
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
    this.m        = GrouperMember.load(this, subj);
    if (m == null) {
      throw new RuntimeException("Unable to load member: " + subj);
    }
    this.memberID = m.memberID();
  }


  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * Start a {@link Grouper} session.
   * <p />
   * <pre class="eg">
   * // Start a session for the retrieved subject
   * GrouperSession s = GrouperSession.start(
   *   SubjectFactory.getSubject(id, type);
   * );
   * </pre>
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
    }
    log.info(
      "Started session " + s.getSessionID() + " for " +
      s.subject().getId() + "/" + s.subject().getType().getName() +
      "/" + s.subject().getSource()
    );
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
   * Return this session's {@link Subject} as a {@link GrouperMember}
   * object.
   * <p/>
   * <pre class="eg">
   * GrouperSession s = GrouperSession.start(subject);
   * GrouperMember  m = s.getMember();
   * </pre>
   * @return  {@link GrouperMember} object.
   */
  public GrouperMember getMember() {
    return this.m;
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
   * <pre>
   * // Stop the current session
   * if (!s.stop()) {
   *   // We failed to stop the session
   * }
   * </pre>
   * @return  True if session stopped.
   */
  public boolean stop() { 
    log.info("Stopping session: " + this);
    boolean rv = false;
    if (this.sessionID != null) {
      // Remove from the grouper_session table
      this.dbSess.txStart();
      this.delete();
      this.dbSess.txCommit();
    }
    this.dbSess.stop();
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

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this)    .
      append("session", this.sessionID) .
      append("subject", this.subject()) .
      toString();
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Retrieve a shared root session
   * TODO Should I have a pool of root sessions?
   */
  protected static GrouperSession getRootSession() {
    if ( (rs != null) && (rs instanceof GrouperSession) ) {
      // TODO In an ideal world I would also check to confirm that the 
      //      session was still valid and reconnect if not.
      log.debug("Reusing root session: " + rs);
      return rs;
    } 
    try {
      rs = GrouperSession.start(
        SubjectFactory.getSubject(
          Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
        )
      );
      log.debug("Creating root session: " + rs);
      return rs;
    } catch (SubjectNotFoundException e) {
      log.debug("Failed to create root session: " + e.getMessage());
      throw new RuntimeException(
        "Unable to start internal root session: " + e.getMessage()
      );
    }
  }

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
   * Can the subject ADMIN?
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canADMIN(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking ADMIN for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_ADMIN);
    } catch (CacheNotFoundException e) {
      if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has ADMIN on " + g + ": ADMIN");
        can = new Boolean(true);
      }
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_ADMIN, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  /*
   * Can the subject CREATE?
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canCREATE(GrouperStem ns) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking CREATE for " + this + " on " + ns);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(ns.key(), Grouper.PRIV_CREATE);
    } catch (CacheNotFoundException e) {
      if        (this.naming().has(this, ns, Grouper.PRIV_CREATE)) {
        log.info(this + " has CREATE on " + ns + ": CREATE");
        can = new Boolean(true); 
      } else if (this.naming().has(this, ns, Grouper.PRIV_CREATE)) {
        log.info(this + " has CREATE on " + ns + ": STEM");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(ns.key(), Grouper.PRIV_CREATE, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  /*
   * Can the subject READ?
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canREAD(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking READ for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_READ);
    } catch (CacheNotFoundException e) {
      if        (this.access().has(this, g, Grouper.PRIV_READ)) {
        log.info(this + " has READ on " + g + ": READ");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has READ on " + g + ": ADMIN");
        can = new Boolean(true); 
      } else if ( 
        (
          this.access().whoHas(
            GrouperSession.getRootSession(), g, Grouper.PRIV_READ
          ).size() == 0
        )
      ) 
      {
        /*
         * TODO Check who has this priv as root so that the subject's 
         * own privs don't get in the way.  And, yes, this is a hack 
         * that will be going away soon enough.
         */
        log.info(this + " has READ on " + g + ": Default READ");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_READ, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  protected void canOPTIN(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking OPTIN for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_OPTIN);
    } catch (CacheNotFoundException e) {
      if        (this.access().has(this, g, Grouper.PRIV_OPTIN)) {
        log.info(this + " has OPTIN on " + g + ": OPTIN");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_UPDATE)) {
        log.info(this + " has OPTIN on " + g + ": UPDATE");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has OPTIN on " + g + ": ADMIN");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_OPTIN, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  protected void canOPTOUT(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking OPTOUT for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_OPTOUT);
    } catch (CacheNotFoundException e) {
      if        (this.access().has(this, g, Grouper.PRIV_OPTOUT)) {
        log.info(this + " has OPTOUT on " + g + ": OPTOUT");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_UPDATE)) {
        log.info(this + " has OPTOUT on " + g + ": UPDATE");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has OPTOUT on " + g + ": ADMIN");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_OPTOUT, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  /*
   * Can the subject STEM?
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canSTEM(GrouperStem ns) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking STEM for " + this + " on " + ns);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(ns.key(), Grouper.PRIV_STEM);
    } catch (CacheNotFoundException e) {
      if (this.naming().has(this, ns, Grouper.PRIV_STEM)) {
        log.info(this + " has STEM on " + ns + ": STEM");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(ns.key(), Grouper.PRIV_STEM, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  protected void canUPDATE(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking UPDATE for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_UPDATE);
    } catch (CacheNotFoundException e) {
      if        (this.access().has(this, g, Grouper.PRIV_UPDATE)) {
        log.info(this + " has UPDATE on " + g + ": UPDATE");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has UPDATE on " + g + ": ADMIN");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_CREATE, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  /*
   * Can the subject VIEW?
   * @throws {@link InsufficientPrivilegeException}
   * TODO Should I use canREAD, et. al.?  Would that buy me additional
   *      caching for free?
   */
  protected void canVIEW(Group g) 
    throws InsufficientPrivilegeException
  {
    log.debug("Checking VIEW for " + this + " on " + g);
    Boolean can = new Boolean(false);
    try {
      can = (Boolean) this.cache.get(g.key(), Grouper.PRIV_VIEW);
    } catch (CacheNotFoundException e) {
      if        (this.access().has(this, g, Grouper.PRIV_VIEW)) {
        log.info(this + " has VIEW on " + g + ": VIEW");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_READ)) {
        log.info(this + " has VIEW on " + g + ": READ");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_UPDATE)) {
        log.info(this + " has VIEW on " + g + ": UPDATE");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_ADMIN)) {
        log.info(this + " has VIEW on " + g + ": ADMIN");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_OPTIN)) {
        log.info(this + " has VIEW on " + g + ": OPTIN");
        can = new Boolean(true); 
      } else if (this.access().has(this, g, Grouper.PRIV_OPTOUT)) {
        log.info(this + " has VIEW on " + g + ": OPTOUT");
        can = new Boolean(true); 
      } else if ( 
        (
          this.access().whoHas(
            GrouperSession.getRootSession(), g, Grouper.PRIV_VIEW
          ).size() == 0
        )
      ) 
      {
        /*
         * TODO Check who has this priv as root so that the subject's 
         * own privs don't get in the way.  And, yes, this is a hack 
         * that will be going away soon enough.
         */
        log.info(this + " has VIEW on " + g + ": Default VIEW");
        can = new Boolean(true); 
      } 
      // Update cache
      this.cache.put(g.key(), Grouper.PRIV_VIEW, (Object) can);
    }
    if (!can.booleanValue()) {
      // TODO What is an appropriate message to return?
      throw new InsufficientPrivilegeException();
    }
  }

  /*
   * Dispatch field level access checking to the appropriate method
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canReadField(Group g, String field)
    throws InsufficientPrivilegeException
  {
    this.canFieldDispatch(
      g, field, GrouperField.field(field).readPriv()
    );
  }

  /*
   * Dispatch field level access checking to the appropriate method
   * @throws {@link InsufficientPrivilegeException}
   */
  protected void canWriteField(Group g, String field)
    throws InsufficientPrivilegeException
  {
    this.canFieldDispatch(
      g, field, GrouperField.field(field).writePriv()
    );
  }

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

  // Dispatch priv checking to appropriate method
  private void canFieldDispatch(Group g, String field, String priv) 
    throws InsufficientPrivilegeException
  {
    if        (priv.equals(Grouper.PRIV_VIEW)) {
      this.canVIEW(g);
    } else if (priv.equals(Grouper.PRIV_READ)) {
      this.canREAD(g);
    } else if (priv.equals(Grouper.PRIV_UPDATE)) {
      this.canUPDATE(g);
    } else if (priv.equals(Grouper.PRIV_ADMIN)) {
      this.canADMIN(g);
    } else if (priv.equals(Grouper.PRIV_OPTIN)) {
      this.canOPTIN(g);
    } else if (priv.equals(Grouper.PRIV_OPTOUT)) {
      this.canOPTOUT(g);
    } else if (priv.equals(Grouper.PRIV_CREATE)) {
      this.canCREATE( (GrouperStem) g );
    } else if (priv.equals(Grouper.PRIV_STEM)) {
      this.canSTEM( (GrouperStem) g );
    } else if (priv.equals("SYSTEM")) {
      throw new RuntimeException(field + " is system maintained");
    } else {
      throw new RuntimeException(
        "Unable to check field access for " + field + "/" + priv
      );
    }
  }

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
      throw new RuntimeException(
        "Error deleting session: " + e.getMessage()
      );
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

    try {
      // Restore Subject object
      this.subject = SubjectFactory.getSubject(m.subjectID(), m.typeID());
    } catch (SubjectNotFoundException e) {
      throw new RuntimeException(
        "Error restoring subject during deserialization: " +
        e.getMessage()
      );
    }

    log.info("Deserialized: " + this);
  }

  // Serialize the session
  private void writeObject(ObjectOutputStream oos) throws IOException {
    log.info("Serialized: " + this);

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
      throw new RuntimeException(
        "Error saving session: " + e.getMessage()
      );
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

