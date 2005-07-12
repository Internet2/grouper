/*--
 $Id: UnusableStyle.java,v 1.1 2005-07-12 23:13:26 acohen Exp $
 $Date: 2005-07-12 23:13:26 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet.ui;

/**
 * This is a typesafe enumeration that identifies the various styles for display
 * of an unusable UI component.
 *  
 */
public class UnusableStyle
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the value.
   * @param description
   *          the human readable description of the status value, by which it is
   *          presented in the user interface.
   */
  private UnusableStyle()
  {
    super();
  }

  /**
   * The instance that represents a text-message, usually explaining why the
   * UI component is not available.
   */
  public static final UnusableStyle TEXTMSG
  	= new UnusableStyle();

  /**
   * The instance that represents a dimmed UI component.
   */
  public static final UnusableStyle DIM
    = new UnusableStyle();
}