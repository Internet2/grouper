package edu.internet2.middleware.directory.grouper;

/** 
 * Provides a GrouperSession.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.3 2004-04-12 04:14:36 blair Exp $
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
    /* XXX How do I get G's properties? */
    /* XXX Initialize jdbc handle now? Or in start()? */
  }

  /**
   * Starts a {@link Grouper} session.
   * <p>
   * XXX This is no longer remotely accurate with what I am
   * contemplating.
   * <p>
   * <ul>
   *  <li>Inserts record into <i>grouper_session</i> table</li>
   *  <li>Looks up subjectID with {@link GrouperSubject} interface</li>
   *  <li>Creates {@link GrouperMember} object for subjectID</li>
   *  <li>Inserts memberID into <i>grouper_session</i> table</li>
   * </ul>
   *
   * @param subjectID The subject to act as for the duration of this
   * session.
   * @param isMember  If true, the subjectID is assumed to be a
   * memberID and not a presentationID.
   * @return True if the session was successfully started, false
   * otherwise.
   */
  public boolean start(String subjectID, boolean isMember) {
    /*
     *
     * if (subjectID == "grouperSystem") {
     *   // A GrouperMember object is not needed as grouperSystem
     *   // can do whatever the fuck it wants.  
     *
     *   // Would it be safer to use something like grouperAuth
     *   // (or whatever)?
     *
     *   <insert record into grouper_session table>
     *   return true;
     * } else {
     *   <insert stub record into grouper_session table>
     *
     *   // As every operation save for GrouperSession.start()
     *   // requires a valid session, we need to cheat a little
     *   // bit by creating a new session as the grouperSystem
     *   // member in order to allow us to then confirm the 
     *   // validity of subjectID and then create a new session.
     *
     *   // I can't decide if this is a hack or the way things ought to
     *   // work.
     *
     *   GrouperSession authSess = new GrouperSession(G);
     *   authSess.start("grouperSystem", true);
     *   memberID = GrouperSubject.lookup(authSess, subjectID);
     *   GrouperMember member = new GrouperMember(authSess, memberID, false);
     *   authSess.end();
     *
     *   <insert record into grouper_session table>
     *   return true;
     * }
     *
     */

    /*
     * XXX Should we perform garbage collection on old/stale jdbc conns
     *     at this time as well?  Or in end()?  But what if no one ever
     *     calls end()?
     */

    /*
    // XXX Assert parameters

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
    return false;
  }

  /**
   * Ends the {@link Grouper} session.
   * <p>
   * <ul>
   *  <li>Removes session from <i>grouper_session</i> table</li>
   *  <li>Closes JDBC connection to groups registry</li>
   * </ul>
   *
   * @return True if the session was successfully ended, false
   * otherwise.
   */
  public boolean end() { 
    /*
    // XXX  Assert parameters
    // ???  Why do I pass in sessionID?  Does that imply that there
    //      are multiple sessions per instantiation?
    //      Of course, if we move to more of a web services model,
    //      the ability to specify the session id could be crucial.
    //      And yes, I'm ignoring entirely the security issues that
    //      loom in such circumstances.
    //      But then: we *are* assuming a great deal of trust.  
    if (this.cred == null) {
      // XXX Ugh
      System.err.println("No known credentials!");
      return false;
    }
    if (this.sessionID <= 0) {
      // XXX Ugh
      System.err.println("No known session id!");
      return false;
    }
    if (this.sessionID != sessionID) {
      // XXX Ugh
      System.err.println("Attempting to end an invalid session!");
      return false;
    }
    this.cred       = null;
    this.sessionID  = -1;

    // ???  Should this be within a 'finally'?  And/Or should it go
    //      elsewhere?
    if (con != null) {
      try { con.close( ); }
      catch( Exception e ) { e.printStackTrace( ); }
    }

    return true;
    */
    return false;
  }

}

