package edu.internet2.middleware.directory.grouper;

/** 
 * Provides a GrouperSession.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.6 2004-04-28 16:24:42 blair Exp $
 */
public class GrouperSession {

  /**
   * Create a {@link GrouperSession} object through which all further
   * {@link Grouper} actions will be performed.
   * <p>
   * <ul>
   *  <li>Opens JDBC connection to groups registry</li>
   * </ul>
   */
  public GrouperSession(Grouper G) { 
    // Nothing -- Yet
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
    // Nothing -- Yet
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
    // XXX Assert parameters

    /*
    // We blindly trust the credentials we are passed.
    this.cred           = cred;

    // And now we need a session id.
    // ???  Whether this is the best way to approach the problem is
    //      another matter.
    java.util.Random r  = new java.util.Random();
    // XXX Well that is certainly arbitrary.
    this.sessionID      = Math.abs( r.nextInt( 65535 ) );

    // XXX Ugh.  Where to put this? 
    try {
      Class.forName(jdbcDriver).newInstance( );
      con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }
    catch( Exception e ) {
      // XXX Specify *what* JDBC driver.
      System.out.println("Failed to load JDBC driver '" + jdbcDriver + "'");
      // XXX  And I should probably do something else in this
      //      case other than just return an invalid session id.
      // XXX  And I should document somewhere what valid session
      //      ids are -- once I know myself.
      return -1;
    }

    return this.sessionID;
    */
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
    // Nothing -- Yet
  }

}

