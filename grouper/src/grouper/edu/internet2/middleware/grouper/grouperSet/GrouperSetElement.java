/**
 * @author mchyzer
 * $Id: GrouperSetElement.java,v 1.1 2009-09-16 08:52:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperSet;


/**
 *
 */
public interface GrouperSetElement {

  /**
   * name of this object (for logging)
   * @return name
   */
  public String __getName();
  
  /**
   * if of this object
   * @return id
   */
  public String __getId();
  
}
