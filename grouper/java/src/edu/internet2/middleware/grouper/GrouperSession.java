package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.lang.reflect.*;
import  java.sql.*;

/** 
 * Provides a GrouperSession.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.12 2004-04-29 15:42:00 blair Exp $
 */
public class GrouperSession {

  private Grouper           intG      = null;
  private GrouperNaming     intName   = null;
  private GrouperPrivilege  intPriv   = null;
  private GrouperSubject    intSubj   = null;
  private Connection        con       = null;
  private GrouperMember     subject   = null;
  private String            subjectID = null;

  /**
   * Create a {@link GrouperSession} object through which all further
   * {@link Grouper} actions will be performed.
   * <p>
   * <ul>
   *  <li>Opens JDBC connection to groups registry</li>
   * </ul>
   */
  // XXX public GrouperSession(Grouper G) { 
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
   * Starts a {@link Grouper} session.
   * <p>
   * <ul>
   *  <li>Using the exective {@link GrouperSession}, "subjectID" is
   *      looked up via the {@link GrouperSubject} interface and a
   *      {@link GrouperMember} object is returned.</li>
   *  <li>Inserts record into <i>grouper_session</i> table.</li>
   *
   * @param subjectID The subject to act as for the duration of this
   * session.
   */
  public void start(String subjectID) {
    // XXX Bad assumption!
    this.subject = this.lookupSubject(subjectID);

    if (this.subject != null) {
      // Create internal representations of the various Grouper
      // interfaces
      this.createInterfaces();

      // Register a new session
      this.registerSession();
    } // XXX else...

  }

  /**
   * Starts a {@link Grouper} session.
   * <p>
   * <ul>
   *  <li>Using the exective {@link GrouperSession}, "subjectID" is
   *      looked up via the {@link GrouperSubject} interface and a
   *      {@link GrouperMember} object is returned.</li>
   *  <li>Inserts record into <i>grouper_session</i> table.</li>
   *
   * @param subjectID The subject to act as for the duration of this
   * session.
   * @param isMember  If true, the subjectID is assumed to be a
   * memberID and not a presentationID.
   */
  public void start(String subjectID, boolean isMember) {
    // Create internal representations of the various Grouper
    // interfaces
    this.createInterfaces();

    // XXX Bad assumptions!
    this.subject    = this.lookupSubject(subjectID);
    this.subjectID  = this.subjectID;

    // Register a new session
    this.registerSession();

  }

  /**
   * Ends the {@link Grouper} session.
   * <p>
   * <ul>
   *  <li>Removes session from <i>grouper_session</i> table</li>
   *  <li>Closes JDBC connection to groups registry</li>
   * </ul>
   */
  public void end() { 
    // Nothing -- Yet
  }

  /**
   * Identifies subject that a given session is running as.
   * <p>
   * <ul>
   *  <li>Calls the internal {@link GrouperMember} object's
   *     <i>whoami</i> method.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return String representing the <i>memberID</i> for this session's
   * subject.
   */
  public String whoami() {
    return this.subjectID;
  }

  public GrouperMember lookupSubject(String subjectID) {
    return _lookupSubject(subjectID);
  }

  public GrouperMember lookupSubject(String subjectID, boolean isMember) {
    return _lookupSubject(subjectID);
  }

  private GrouperMember _lookupSubject(String subjectID) {
    GrouperMember m = null;

    // XXX Grab this from config
    if (subjectID == "GrouperSystem") {
      m = new GrouperMember(this, subjectID, false);
    } else {
      m = this.intSubj.lookup(subjectID);
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

  private void createInterfaces() {
    // Create internal references to the various interfaces
    this.intName            = (GrouperNaming)    createObject( intG.config("interface.naming") );
    this.intPriv            = (GrouperPrivilege) createObject( intG.config("interface.privilege") );
    this.intSubj            = (GrouperSubject)   createObject( intG.config("interface.subject") );
  }

  private Object createObject(String name) {
    Object object         = null;
    Class[]  paramsClass  = new Class[]  { GrouperSession.class };
    Object[] params       = new Object[] { this };

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
     
  public Connection connection() {
    return this.con;
  }
 
  private void registerSession() {
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

