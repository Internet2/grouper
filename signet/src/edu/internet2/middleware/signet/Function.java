/*--
$Id: Function.java,v 1.8 2005-10-31 22:45:28 acohen Exp $
$Date: 2005-10-31 22:45:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
* Function organizes a group of {@link Permission}s. Each Function is
* intended to correspond to a business-level task that a
* {@link PrivilegedSubject} must perform in order to accomplish some business
* operation.
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
   * Gets the <code>Category</code> associated with this <code>Function</code>.
   * 
   * @return Returns the category.
   */
  public Category getCategory();

  /**
   * Gets the {@link Permission}s associated with this <code>Function</code>.
   * 
   * @return Returns the permissions.
   */
  public Set getPermissions();
  
  /**
   * Gets the {@link Limit}s associated with this <code>Function</code>'s
   * {@link Permission}s.
   * @return Returns the <code>Set</code> of <code>Limit</code>s.
   */
  public Set getLimits();
  
  /**
   * Adds a <code>Permission</code> to the set of <code>Permission</code>s
   * associated with this <code>Function</code>.
   * 
   * @param permission The <code>Permission</code> to be associated with this
   * <code>Function</code>.
   */
  void addPermission(Permission permission);
}
