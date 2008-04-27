/*
 * $Id: Grouper.java,v 1.1.1.1 2004-02-27 17:55:52 blair Exp $
 */

// XXX package edu.internet2.middleware.mace.grouper;

public class Grouper {

  private int     sessionID;
  private String  cred;

  public Grouper() {
    sessionID = -1;
    cred      = null;
  }

  public int Session_start (String cred) {
    // XXX Assert parameters
    this.cred       = cred;
    this.sessionID  = 1;    // XXX
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
    return true;
  }

}

