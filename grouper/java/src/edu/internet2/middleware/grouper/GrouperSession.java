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
 * @version $Id: GrouperSession.java,v 1.48 2004-10-05 18:35:54 blair Exp $
 */
public class GrouperSession {

  // Internal reference to the Grouper environment
  private Grouper         _G;
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
  public void start(Grouper G, GrouperMember s) {
    // Our environment
    this._G         = G;
    // Keep track of who we are
    this.subject    = s;
    this.subjectID  = s.memberID();

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
    // this._init();
  }

  public Grouper env() {
    return this._G;
  }

  /*
   * BELOW LURKS FAR MORE MADNESS THAN ABOVE
   */

  /**
   * {@link Grouper} run-time configuration parameter getter.
   * <p>
   * FIXME Why is this here?
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
   * @return  Subject of current session as a {@link GrouperMember}
   * object.
   */
  public GrouperMember subject() {
    return (GrouperMember) this.subject;
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

  /*
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW.
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this._G         = null;
    this.intAccess  = null;
    this.intNaming  = null;
    this.sessionID  = null;
    this.startTime  = null;
    this.subject    = null;
    this.subjectID  = null;
  }

  /*
   * BELOW LURKS FAR MORE MADNESS THAN ABOVE
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
   * Below for Hibernate
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

