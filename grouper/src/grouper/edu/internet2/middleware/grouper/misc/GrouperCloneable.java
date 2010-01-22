/*
 * @author mchyzer
 * $Id: GrouperCloneable.java,v 1.1 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;


/**
 * Implement this interface if the object is cloneable
 */
public interface GrouperCloneable  {

  /** 
   * clone an object (deep clone, on fields that make sense)
   * @see Object#clone()
   * @return the clone of the object
   */
  public Object clone();
  
}
