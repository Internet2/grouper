/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;

/** 
 * {@link Grouper} session class.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.42 2004-09-19 03:10:42 blair Exp $
 */
public class GrouperSession {

  // Internal reference to the Grouper environment
  private Grouper         _G;
  // Internal reference to the Access interface
  private GrouperAccess   intAccess;
  // Internal reference to the Naming interface
  private GrouperNaming   intNaming;
  // FIXME How many of these variables are actually used?
  // FIXME And what is the purpose of those that are used?
  private Connection      con;
  private String          cred;
  private SessionFactory  factory;
  private String          presentationID;
  private Session         session;
  private String          sessionID;
  private String          startTime;
  private GrouperMember   subject;
  private String          subjectID;

  private Configuration   cfg;

  /**
   * Create a session object that will provide a context for future
   * operations.
   */
  public GrouperSession() {
    this.cfg            = null;
    this._G             = null;
    this.con            = null;
    this.cred           = null;
    this.intAccess      = null;
    this.intNaming      = null;
    this.factory        = null;
    this.presentationID = null;
    this.session        = null;
    this.sessionID      = null;
    this.startTime      = null;
    this.subject        = null;
    this.subjectID      = null;
  }

  /**
   * {@link Grouper} run-time configuration parameter getter.
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public String config(String parameter) {
    return this._G.config(parameter);
  }

  /**
   * Return subject of current session as a {@link GrouperMember}
   * object.
   *
   * @return  Subject of current session as {@link GrouperMember}
   * object.
   */
  public GrouperMember subject() {
    return (GrouperMember) this.subject;
  }

  /**
   * Confirm whether a given group field is valid for a given group
   * type.
   * <p>
   * FIXME This belongs elsewhere.
   *
   * @return  Boolean true if {@link GroupField} is valid for the given
   * {@link GroupType}, false otherwise.
   */
  public boolean groupField(String type, String field) {
    List typeDefs = this._G.groupTypeDefs();
    for (Iterator iter = typeDefs.iterator(); iter.hasNext();) {
      GrouperTypeDef td = (GrouperTypeDef) iter.next();
      if ( 
          (td.groupType().equals(type)) && // If the group type matches
          (td.groupField().equals(field))  // .. and the group field matches
         )
      {
        // Then we are considered validated.
        return true;
      }
    }
    return false;
  }

  /**
   * Confirm validity of a group type.
   * <p>
   * FIXME This belongs elsewhere.
   *
   * @return  Boolean true if {@link GroupType} is valid, false
   * otherwise.
   */
  public boolean groupType(String type) {
    List types = this._G.groupTypes();
    for (Iterator iter = types.iterator(); iter.hasNext();) {
      GrouperType t = (GrouperType) iter.next();
      if ( t.toString().equals(type) ) {
        return true;
      }
    }
    return false;
  }

  /*
   * BELOW LURKS FAR MORE MADNESS THAN ABOVE
   */

  /**
   * Start a {@link Grouper} session.
   * <p>
   * <ul>
   *  <li>Use executive session and subject interface to identify
   *      subject</li>
   *  <li>Update <i>grouper_session</i> table.</li>
   * <p>
   * TODO Plugin an external session handling mechanism?  Yes, please.
   *
   * @param   G         @{link Grouper} environment
   * @param   subjectID The subject to act as for the duration of this
   *   session.
   */
  public void start(Grouper G, String subjectID) {
    // Internal reference to the Grouper object
    this._G = G;

    // XXX Ugh
    this.subjectID  = subjectID;
    this.cred       = subjectID;

    // Register a new session
    this._registerSession();
  }

  /**
   * Start a session.
   * <p>
   * <ul>
   *  <li>Using the executive session, lookup "subjectID" and return a
   *      {@link GrouperMember} object.</li>
   *  <li>Update <i>grouper_session</i> table.</li>
   *
   * @param   G         @{link Grouper} environment
   * @param   subjectID The subject to act as for the duration of this
   *   session.
   * @param   isMember  If true, the subjectID is assumed to be a
   *  memberID and not a presentationID.
   */
  public void start(Grouper G, String subjectID, boolean isMember) {
    // Internal reference to the Grouper object
    this._G = G;

    // XXX Bad assumptions!
    this.subjectID  = subjectID;
    this.cred       = subjectID;

    // Register a new session
    this._registerSession();
  }

  /**
   * End the session.
   * <p>
   * <ul>
   *  <li>Update <i>grouper_session</i> table.</li>
   *  <li>Close JDBC connection.</li>
   * </ul>
   */
  public void end() { 
    // It looks like we have a session.  Attempt to close it.
    if (this.session != null) {
      try {
        this.session.close();
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1); 
      }
    }
  }

  /**
   * Identify the subject of this session.
   * <p>
   * <ul>
   *  <li>Calls <i>whoAmI()</i> on the {@link GrouperMember} object
   *      that represents the current subject.</li>
   * </ul>
   *
   * @return  Identity of the current session's subject.
   */
  public String whoAmI() {
    System.err.println("whoAmI()");
    return this.subject.memberID();
  }

  /**
   * Look up a subject via the {@link GrouperSubject} interface.
   * <p>
   * XXX What is meant by "id"?
   *
   * @param   id  The identity of the subject to look up.
   * @return  A {@link GrouperMember} object.
   */
  public GrouperMember lookup(String id) {
    return _lookup(subjectID);
  }

  /**
   * Grant specified privilege on specified group to specified member.
   * <p>
   * Dispatches to the configured implementation of either the 
   * {@link GrouperAccess} or {@link GrouperNaming} interfaces.
   *
   * @param   g     Grant privilege on this group.
   * @param   m     Grant privilege to this member.
   * @param   priv  Privilege to grant.
   */
  public void grantPriv(GrouperGroup g, GrouperMember m, String priv) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // XXX Should the naming priv require a group object?
    // this.intAccess.grant(g, m, priv);
    // this.intNaming.grant(g, m, priv);
  }

  /**
   * Revoke specified privilege from specified member on specified
   * group.
   * <p>
   * Dispatches to the configured implementation of either the
   * {@link GrouperAccess} or {@link GrouperNaming} interfaces.
   *
   * @param   g     Revoke privilege on this group.
   * @param   m     Revoke privilege for this member.
   * @param   priv  Privilege to revoke.
   */
  public void revokePriv(GrouperGroup g, GrouperMember m, String priv) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // XXX Should the naming priv require a group object?
    // this.intAccess.revoke(g, m, priv);
    // this.intNaming.revoke(g, m, priv);
  }

  /**
   * List privileges for current subject on the specified group.
   *
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public List hasPriv(GrouperGroup g) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // return this.intAccess.has(g);
    // return this.intNaming.has(g);
    List privs = new ArrayList();
    return privs;
  }

  /**
   * List privileges for specified member on the specified group.
   *
   * @param   g   List privileges on this group.
   * @param   m   List privileges for this member.
   * @return  List of privileges.
   */
  public List hasPriv(GrouperGroup g, GrouperMember m) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // return this.intAccess.has(g, m);
    // return this.intNaming.has(g, m);
    List privs = new ArrayList();
    return privs;
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   *
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean hasPriv(GrouperGroup g, String priv) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // return this.intAccess.has(g, priv);
    // return this.intNaming.has(g, priv);
    return false;
  }

  /**
   * Verify whether the specified subject has the specified privilege
   * on the specified group.
   *
   * @param   g     Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean hasPriv(GrouperGroup g, GrouperMember m, String priv) {
    // XXX DTRT depending upon whether it is an "access" or a "naming"
    //     privilege
    // return this.intAccess.has(g, m, priv);
    // return this.intNaming.has(g, m, priv);
    return false;
  }

  /**
   * Provide access to the session's JDBC connection handle.
   * <p>
   * XXX This may not remain here, may not remain public, etc.
   *
   * @return JDBC connection handle for this session. 
   */
  public Connection connection() {
    return this.con;
  }

  /**
   * Provide access to the Hibernate session.
   * <p>
   * XXX This may not remain here, may not remain public, etc.
   *
   * @return Hibernate session
   */
  public Session session() {
    if (this.session != null) {
      // Session exists.  Assume it is valid and return it.
      return this.session;
    } else {
      try {
        if (this.factory == null) {
          this._hibernateConf();
          // TODO Should we attempt to create a factory and then return
          //      a session?
          try {
            this.factory = this.cfg.buildSessionFactory();
          } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
          }
        }
        this.session = this.factory.openSession();
        return this.session;
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    } 
    return null;
  }

  /*
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW.
   */

  /*
   * Instantiate internal references to the  access, naming, and
   * subject interfaces.
   */ 
  private void _createInterfaces() {
    // Create internal references to the various interfaces
    this.intAccess  = (GrouperAccess)  this._createObject( _G.config("interface.access") );
    this.intNaming  = (GrouperNaming)  this._createObject( _G.config("interface.naming") );
  }

  /*
   * Instantiate an object -- reflectively
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
   * Look up a subject via subject interface.
   * <p>
   * TODO Add a `type' parameter?
   *
   * @param id  The identify of the subject to look up.
   * @return  A {@link GrouperMember} object.
   */
  private GrouperMember _lookup(String id) {
    GrouperMember m = null;

    // TODO Don't hardcode type
    m = GrouperSubject.lookup(id, "person");
    if (m == null) {
      // XXX This should instead throw some sort of an exception.
      //     Or something.
      System.exit(1);
    }
    this.subjectID = id;

    return m;
  }

  /*
   * Register a new session with the groups registry.
   */
  private void _hibernateConf() {
    if (this.cfg == null) {
      try {
        this.cfg = new Configuration()
          .addFile("conf/Grouper.hbm.xml");
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
  }

  private void _registerSession() {
    // Create internal representations of the various Grouper
    // interfaces
    this._createInterfaces();

    // XXX Bad assumption!
    this.subject = this.lookup(subjectID);

    // Open or fetch a Hibernate session
    this.session = this.session();

    // TODO Make this configurable.  Or something.
    this._cullSessions();

    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date now = new java.util.Date();

    this.setCred( this.cred );
    // XXX Switch to a generated sequence?
    this.setSessionID( Long.toString(now.getTime()) );
    // TODO Switch to GMT/UTC
    this.setStartTime( Long.toString(now.getTime()) );

    try {
      Transaction t = this.session.beginTransaction();
      this.session.save(this);
      t.commit();
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  /*
   * Cull old sessions
   */
  private void _cullSessions() {
    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date now     = new java.util.Date();
    long nowTime = now.getTime();
    long tooOld  = nowTime - 360000;

    try {
      int cnt = ( (Integer) this.session.iterate(
                  "SELECT count(*) FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperSession " +
                  "WHERE startTime > " + nowTime
                  ).next() ).intValue();
      if (cnt > 0) {
        // XXX This is sort of redundant.
        try {
          this.session.delete(
                  "FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperSession " +
                  "WHERE startTime > " + nowTime
                  );
        } catch (Exception e) {
          System.err.println(e);
          System.exit(1);
        }
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    try {
      int cnt = ( (Integer) this.session.iterate(
                  "SELECT count(*) FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperSession " +
                  "WHERE " + tooOld + " > startTime"
                  ).next() ).intValue();
      if (cnt > 0) {
        // XXX This is sort of redundant.
        try {
          this.session.delete(
                  "FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperSession " +
                  "WHERE " + tooOld + " > startTime"
                  );
        } catch (Exception e) {
          System.err.println(e);
          System.exit(1);
        }
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }

  }

  /*
   * Below for Hibernate
   */
  
  private String getSessionID() {
    return this.sessionID;
  }

  private void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  private String getCred() {
    return this.cred;
  }

  private void setCred(String cred) {
    this.cred = cred;
  }

  private String getStartTime() {
    return this.startTime;
  }

  private void setStartTime(String startTime) {
    this.startTime = startTime;
  }
}

