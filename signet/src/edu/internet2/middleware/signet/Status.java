/*--
 $Id: Status.java,v 1.4 2005-01-21 20:30:47 acohen Exp $
 $Date: 2005-01-21 20:30:47 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various statuses that a
 * Signet entity may have.
 *  
 */
public class Status
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
  private Status(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that represents an active entity.
   */
  public static final Status ACTIVE
  	= new Status("active", "currently active");

  /**
   * The instance that represents an inactive entity.
   */
  public static final Status INACTIVE
  	= new Status
  			("inactive", "inactive, exists only for the historical record");

  /**
   * The instance that represents a pending entity.
   */
  public static final Status PENDING
  	= new Status
  			("pending",
  			 "pending, will become active when prerequisites are fulfilled");
}