/*--
$Id: SelectionType.java,v 1.2 2006-02-09 10:24:33 lmcrae Exp $
$Date: 2006-02-09 10:24:33 $

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
