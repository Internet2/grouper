/*
 * @author mchyzer
 * $Id: GrouperEngineIdentifier.java,v 1.1 2009-02-08 21:30:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;


/**
 * object which has a grouper engine string inside.
 * generally you will use the enum GrouperEngineBuiltin
 */
public interface GrouperEngineIdentifier {

  /** 
   * get the string for the db col.  Generally grouper built in engines
   * start with "grouper" 
   * @return the engine
   */
  public String getGrouperEngine();
  
}
