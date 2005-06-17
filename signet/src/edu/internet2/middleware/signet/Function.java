/*--
$Id: Function.java,v 1.5 2005-06-17 23:24:28 acohen Exp $
$Date: 2005-06-17 23:24:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.subject.Subject;

/**
* Function organizes a group of {@link Permission}s. Each Function is
* intended to correspond to a business-level task that a {@link Subject}
* must perform in order to accomplish some business operation.
* 
*/

public interface Function
extends SubsystemPart, HelpText, Name, Comparable
{
  /**
   * Gets the ID of this entity.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   *    documents and other documents used by analysts.
   */
  public String getId();
  
  /**
   * Gets the Category associated with this Function.
   * 
   * @return Returns the category.
   */
  public Category getCategory();

  /**
   * Gets the Permissions associated with this Function.
   * 
   * @return Returns the permissions.
   */
  public Permission[] getPermissionsArray();
  
  /**
   * Gets the Limits associated with this Function's Permissions.
   * @return Returns the limits, in ascending order of Limit.getDisplayOrder().
   */
  public Limit[] getLimitsArray();

  /**
   * Sets the Category associated with this Function.
   * 
   * @param category The category to set.
   */
  void setCategory(Category Category);
  
  /**
   * Sets the Permissions associated with this Function.
   * 
   * @param permissions The permissions to set.
   */
  void setPermissionsArray(Permission[] permissions);
  
  /**
   * Adds a permission to the set of Permissions associated with this
   * Function.
   * 
   * @param permission
   */
  void addPermission(Permission permission);
}
