/*
 * @author mchyzer
 * $Id: HqlQuery.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

/**
 *
 */
public interface HqlQuery {
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public HqlQuery setScalar(String bindVarName, Object value);
  
  /**
   * set a string as a bind variable
   * @param bindVarName
   * @param value
   * @return this fro chaining
   */
  public HqlQuery setString(String bindVarName, String value);
}
