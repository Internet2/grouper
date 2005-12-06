/*--
 $Id: Difference.java,v 1.1 2005-12-06 22:34:51 acohen Exp $
 $Date: 2005-12-06 22:34:51 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet.ui;

/**
 * This is a typesafe enumeration that identifies the various types of changes
 * that a Grantable object can undergo.
 *  
 */
public class Difference
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   */
  private Difference()
  {
    super();
  }

  /**
   * The instance that represents a new grant.
   */
  public static final Difference GRANT
  	= new Difference();

  /**
   * The instance that represents a revocation.
   */
  public static final Difference REVOKE
    = new Difference();

  /**
   * The instance that represents some other change.
   */
  public static final Difference MODIFY
    = new Difference();
}