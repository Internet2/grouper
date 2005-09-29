/*--
 $Id: PrivDisplayType.java,v 1.1 2005-09-29 01:35:31 acohen Exp $
 $Date: 2005-09-29 01:35:31 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet.ui;

/**
 * This is a typesafe enumeration that identifies the various statuses that a
 * Signet entity may have.
 *  
 */
public class PrivDisplayType
	extends TypeSafeEnumeration
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the status value.
   * @param description
   *          the human readable description of the status value, by which it is
   *          presented in the user interface.
   */
  private PrivDisplayType(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that indicates a display of currently-active Assignments and
   * Proxies received by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType CURRENT_RECEIVED
  	= new PrivDisplayType("current_received", "current privileges");

  /**
   * The instance that indicates a display of currently-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType CURRENT_GRANTED
  	= new PrivDisplayType
  			("current_granted", "current assignments to others");
}