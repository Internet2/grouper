/*
 * @author mchyzer
 * $Id: GrouperEngineBuiltin.java,v 1.1 2009-02-08 21:30:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;


/**
 * built in grouper engines.  Note, if we prefix builtins with "grouper"
 * there will not be conflicts with custom engines
 */
public enum GrouperEngineBuiltin implements GrouperEngineIdentifier {

  /** gsh engine */
  GSH("grouperShell"), 
  
  /** default group ui engine */
  UI("grouperUI"), 
  
  /** web service engine */
  WS("grouperWS"), 

  /** loader engine */
  LOADER("grouperLoader"), 

  /** ldappc engine */
  LDAPPC("grouperLdappc"),

  /** junit engine */
  JUNIT("grouperJunit");

  /**
   * have the label different from name
   * @param theGrouperEngine
   */
  private GrouperEngineBuiltin(String theGrouperEngine) {
    this.grouperEngine = theGrouperEngine;
  }
  
  /**
   * string that goes to the DB
   */
  private String grouperEngine;

  /**
   * @see edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier#getGrouperEngine()
   */
  public String getGrouperEngine() {
    return this.grouperEngine;
  }
}
