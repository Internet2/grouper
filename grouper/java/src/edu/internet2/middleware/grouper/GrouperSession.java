package edu.internet2.middleware.directory.grouper;

/** 
 * Provides a GrouperSession.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.1 2004-04-02 21:04:11 blair Exp $
 */
public class GrouperSession {

  public GrouperSession(Grouper G) { 
    /* Initialize jdbc handle now? */
  }

  /**
   * Starts a {@link Grouper} session.
   *
   * @param  subject  The subject to act as for the duration of this
   * session.
   * @return true if the session was successfully started, false
   * otherwise.
   */
  public boolean start(String subject) {
    /*
      + (boolean|void) void start(String subjectID, boolean isMember)
      - subjectID == memberID || presentationID
      - XXX Why can I not recall what 'isMember' is supposed to
            represent?
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
   *
   * @return true if the sesesion was successfully ended, false
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

  /**
   * XXX THIS SHOULD NOT BE HERE
   */
  public GrouperMember lookupSubject(String subject, boolean isMember) {
    /*
      + GrouperMember lookupSubject(String subjectID, boolean isMember)
        - This is an interface (or something like that)
        - subjectID == memberID || presentationID
        - XXX Why can I not recall what 'isMember' is supposed to
            represent?
    */
    return null;
  }

}

