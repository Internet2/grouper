/*--
$Id: ValueType.java,v 1.3 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various types that a
 * Signet Limit-value may have.
 *  
 */
public class ValueType
	extends TypeSafeEnumeration
{

  /**
   * The instance that represents a "string" limit-type.
   */
  public static final ValueType STRING
  	= new ValueType("string", "Any alphanumeric value");

  /**
   * The instance that represents an inactive entity.
   */
  public static final ValueType NUMERIC
  	= new ValueType
  			("numeric", "Any integer or decimal value");

  /**
   * The instance that represents a pending entity.
   */
  public static final ValueType DATE
  	= new ValueType
  			("date",
  			 "Any calendar date");

  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   * 		the external name of the ValueType value.
   * @param description
   *    the human-readable description of the ValueType value,
   * 	  by which it is presented in the user interface.
   */
  private ValueType(String name, String description)
  {
    super(name, description);
  }
}