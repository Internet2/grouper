/*
 * @author mchyzer
 * $Id: Scalarable.java,v 1.1.2.1 2009-04-07 16:21:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

/**
 *
 */
public interface Scalarable {
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public Object setScalar(String bindVarName, Object value);
}
