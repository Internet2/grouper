/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various types that a
 * Signet Limit-value may have.
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