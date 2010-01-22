/*
 * @author mchyzer
 * $Id: GrouperHookTypeInterface.java,v 1.2 2008-09-29 03:38:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;



/**
 * Each hook type implements this interface
 */
public interface GrouperHookTypeInterface {

  /**
   * property file key for this hook class
   * e.g. hooks.field.class
   * @return the propertyFileKey
   */
  public String getPropertyFileKey();

  
  /**
   * base class for this hook class
   * e.g. FieldHooks.class
   * @return the baseClass
   */
  public Class<?> getBaseClass();

}
