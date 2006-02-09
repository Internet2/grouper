/*--
$Id: Status.java,v 1.5 2006-02-09 10:25:10 lmcrae Exp $
$Date: 2006-02-09 10:25:10 $
 
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
