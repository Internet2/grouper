package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.Date;
import  java.util.ArrayList;
import  java.util.List;
import  java.util.Properties;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;

/** 
 * Class representing a {@link Grouper} session.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.29 2004-07-27 20:15:43 blair Exp $
 */
public class GrouperSession {

  // XXX Clarify the purpose of all of these variables
  private Connection      con             = null;
  private Grouper         intG            = null;
  private GrouperAccess   intAccess       = null;
  private GrouperNaming   intNaming       = null;
  private GrouperSubject  intSubject      = null;
  private String          presentationID  = null;
  private SessionFactory  sessionFactory  = null;
  private GrouperMember   subject         = null;
  private Session         session         = null;
  private String          subjectID       = null;

  // XXX HACK HACK HACK
  private String          cred            = null;
  private String          sessionID       = null;
  private String          startTime       = null;

  private SessionFactory  sessions        = null;


  /**
   * Create a session object that will provide a context for future
   * operations.
   */
  public GrouperSession() {
    // Nothing 
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
   */
  public void start(Grouper G, String subjectID) {
    // Internal reference to the Grouper object
    this.intG = G;

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
    this.intG = G;

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
    return this.subject.whoAmI();
  }

  /**
   * Looks up subject via {@link GrouperSubject} interface.
   *
   * @param   subjectID The subject to look up.
   * @return  GrouperMember object.
   */
  public GrouperMember lookupSubject(String subjectID) {
    return _lookupSubject(subjectID);
  }

  /**
   * Looks up subject via {@link GrouperSubject} interface.
   *
   * @param   subjectID The subject to look up.
   * @param   isMember  ...
   * @return  GrouperMember object.
   */
  public GrouperMember lookupSubject(String subjectID, boolean isMember) {
    return _lookupSubject(subjectID);
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
   * XXX This may not return here, may not remain public, etc.
   *
   * @return JDBC connection handle for this session. 
   */
  public Connection connection() {
    return this.con;
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
    this.intAccess  = (GrouperAccess)  this._createObject( intG.config("interface.access") );
    this.intNaming  = (GrouperNaming)  this._createObject( intG.config("interface.naming") );
    this.intSubject = (GrouperSubject) this._createObject( intG.config("interface.subject") );
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
   */
  private GrouperMember _lookupSubject(String subjectID) {
    GrouperMember m = null;

    if (subjectID.equals( intG.config("member.system") )) {
      m = new GrouperMember(this, subjectID, false);
    } else {
      m = this.intSubject.lookup(subjectID);
      if (m != null) {
        this.subjectID = subjectID;
      }
    }

    if (m == null) {
      // XXX This should instead throw some sort of an exception.
      //     Or something.
      System.err.println("XXX " + subjectID + " IS NOT A VALID CREDENTIAL!");
      System.exit(1);
    }

    return m;
  }

  /*
   * Register a new session with the groups registry.
   */
  private void _registerSession() {
    // Create internal representations of the various Grouper
    // interfaces
    this._createInterfaces();

    // XXX Bad assumption!
    this.subject = this.lookupSubject(subjectID);

    try {
      Configuration cfg = new Configuration()
        .addFile("conf/Grouper.hbm.xml");
      try {
        sessions = cfg.buildSessionFactory();
        this.session = sessions.openSession();
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }

    // TODO Make this configurable.  Or something.
    this._cullSessions();

    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    Date now = new Date();

    this.setCred( this.cred );
    // XXX Switch to a generated sequence?
    this.setSessionID( Long.toString(now.getTime()) );
    // TODO Switch to GMT/UTC
    this.setStartTime( Long.toString(now.getTime()) );

    try {
      Transaction t = session.beginTransaction();
      session.save(this);
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
    Date now     = new Date();
    long nowTime = now.getTime();
    long tooOld  = nowTime - 360000;

    try {
      int cnt = ( (Integer) this.session.iterate(
                  "SELECT count(*) FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.directory.grouper.GrouperSession " +
                  "WHERE startTime > " + nowTime
                  ).next() ).intValue();
      if (cnt > 0) {
        // XXX This is sort of redundant.
        try {
          this.session.delete(
                  "FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.directory.grouper.GrouperSession " +
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
                  "IN CLASS edu.internet2.middleware.directory.grouper.GrouperSession " +
                  "WHERE " + tooOld + " > startTime"
                  ).next() ).intValue();
      if (cnt > 0) {
        // XXX This is sort of redundant.
        try {
          this.session.delete(
                  "FROM grouper_session " +
                  "IN CLASS edu.internet2.middleware.directory.grouper.GrouperSession " +
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

