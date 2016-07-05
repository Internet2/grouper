/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;


/**
 * if changing to or from versions, customize it a bit
 */
public interface ChangeToVersionCustomizable {

  /**
   * 
   * @param objectToConvertTo
   */
  public void customizeChangeFromVersion(Object objectToConvertTo);
  
  /**
   * 
   * @param objectToConvertFrom
   */
  public void customizeChangeToVersion(Object objectToConvertFrom);
  
}
