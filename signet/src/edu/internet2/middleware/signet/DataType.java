/*--
$Id: DataType.java,v 1.3 2005-10-31 18:31:44 acohen Exp $
$Date: 2005-10-31 18:31:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various types of 
 * Signet {@link Limit} data.
 *  
 */
public class DataType
	extends TypeSafeEnumeration
{

  /**
   * The instance that represents a "string" limit-type.
   */
  public static final DataType TEXT
  	= new DataType("text", "Any alphanumeric value");

  /**
   * The instance that represents an inactive entity.
   */
  public static final DataType NUMERIC
  	= new DataType
  			("numeric", "Any integer or decimal value");

  /**
   * The instance that represents a pending entity.
   */
  public static final DataType DATE
  	= new DataType
  			("date",
  			 "Any calendar date");

  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   * 		the external name of the DataType value.
   * @param description
   *    the human-readable description of the DataType value,
   * 	  by which it is presented in the user interface.
   */
  private DataType(String name, String description)
  {
    super(name, description);
  }
}