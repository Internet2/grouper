/*
 * @author mchyzer
 * $Id: GrouperHookTypeInterface.java,v 1.1 2008-07-10 05:55:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import edu.internet2.middleware.grouper.hooks.FieldHooks;


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
