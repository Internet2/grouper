/*--
$Id: SelectionType.java,v 1.1 2005-04-05 23:11:38 acohen Exp $
$Date: 2005-04-05 23:11:38 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various selection-types
 * that a Signet Limit may have.
 *  
 */

public final class SelectionType
extends TypeSafeEnumeration
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the SelectionType value.
   * @param description
   *          the human readable description of the SelectionType value, by
   *          which it is presented in the user interface.
   */
  private SelectionType(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that represents a single-select.
   */
  public static final SelectionType SINGLE
  	= new SelectionType("single", "Select only one from the set");

  /**
   * The instance that represents a multiple-select.
   */
  public static final SelectionType MULTIPLE
  	= new SelectionType
  			("multiple", "Select one or more from the set");
}
