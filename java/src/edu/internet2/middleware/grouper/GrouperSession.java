package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.ArrayList;
import  java.util.List;

/** 
 * Class representing a {@link Grouper} session.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.23 2004-05-02 14:26:36 blair Exp $
 */
public class GrouperSession {

  private Grouper         intG            = null;
  private GrouperNaming   intNaming       = null;
  private GrouperAccess   intAccess       = null;
  private GrouperSubject  intSubject      = null;
  private Connection      con             = null;
  private GrouperMember   subject         = null;
  private String          subjectID       = null;
  private String          presentationID  = null;

  /**
   * Create a session object that will provide a context for future
   * operations.
   * <p>
   * <ul>
   *  <li>Opens JDBC connection to groups registry</li>
   * </ul>
   *
   * @param   G Grouper environment.
   */
  public GrouperSession(Grouper G) {
    // Internal reference to the Grouper object
    this.intG = G;

    try {
      Class.forName( intG.config("jdbc.driver") ).newInstance();
      con = DriverManager.getConnection(intG.config("jdbc.url"),
                                        intG.config("jdbc.username"),
                                        intG.config("jdbc.password"));
    }
    catch(Exception e) {
      System.err.println("Failed to load JDBC driver '" + 
                          intG.config("jdbc.driver") + "'");
      System.exit(1);
    }

  }

  /**
   * Start a session.
   * <p>
   * <ul>
   *  <li>Using the executive session, lookup "subjectID" and return a
   *      {@link GrouperMember} object.</li>
   *  <li>Update <i>grouper_session</i> table.</li>
   *
   * @param   subjectID The subject to act as for the duration of this
   *   session.
   */
  public void start(String subjectID) {
    // XXX Bad assumption!
    this.subject = this.lookupSubject(subjectID);

    if (this.subject != null) {
      // Create internal representations of the various Grouper
      // interfaces
      this._createInterfaces();

      // Register a new session
      this._registerSession();
    } // XXX else...

  }

  /**
   * Start a session.
   * <p>
   * <ul>
   *  <li>Using the executive session, lookup "subjectID" and return a
   *      {@link GrouperMember} object.</li>
   *  <li>Update <i>grouper_session</i> table.</li>
   *
   * @param   subjectID The subject to act as for the duration of this
   *   session.
   * @param   isMember  If true, the subjectID is assumed to be a
   *  memberID and not a presentationID.
   */
  public void start(String subjectID, boolean isMember) {
    // Create internal representations of the various Grouper
    // interfaces
    this._createInterfaces();

    // XXX Bad assumptions!
    this.subject    = this.lookupSubject(subjectID);
    this.subjectID  = this.subjectID;

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
    // Nothing -- Yet
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
    Statement stmt = null;

    try { 
      stmt = this.connection().createStatement();
      String insertSession = "INSERT INTO grouper_session " +
                             "(cred, startTime) " +
                             "VALUES (" +
                             "'" + this.subjectID + "', " +
                             "'RIGHT NOW'" +
                             ")";
      try { 
        stmt.executeUpdate(insertSession);
      } catch (Exception e) {
        System.err.println("Unable to insert session: " + insertSession);
        System.exit(1);
      }
    } catch (Exception e) {
      System.err.println("Unable to create statement");
      System.exit(1);
    }
  }

}

