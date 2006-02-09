/*--
$Id: DataType.java,v 1.4 2006-02-09 10:19:09 lmcrae Exp $
$Date: 2006-02-09 10:19:09 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
