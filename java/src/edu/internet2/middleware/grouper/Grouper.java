/*
 * $Id: Grouper.java,v 1.2 2004-02-27 19:29:13 blair Exp $
 */

// XXX package edu.internet2.middleware.mace.grouper;

import java.sql.*;

public class Grouper {

  private Connection  con;
  private int         sessionID;
  private String      cred;

  // XXX Ugh.  Make this configurable.
  private String      url = "jdbc:postgresql://fifty6th:5432/wm_logs";

  public Grouper() {
    con       = null;
    sessionID = -1;
    cred      = null;
  }

  public int Session_start (String cred) {
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
      // XXX Ugh.  Make this configurable.
      String driver = "org.postgresql.Driver";
      Class.forName(driver).newInstance( );
      // XXX Ugh.  Make this configurable.
      con = DriverManager.getConnection(url, "wm_logs", "");
    }
    catch( Exception e ) {
      // XXX Specify *what* JDBC driver.
      System.out.println("Failed to load JDBC driver.");
      // XXX  And I should probably do something else in this
      //      case other than just return an invalid session id.
      // XXX  And I should document somewhere what valid session
      //      ids are -- once I know myself.
      return -1;
    }

    return this.sessionID;
  }

  public boolean Session_end (int sessionID) {
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
  }
}

