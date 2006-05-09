/*--
$Id: PrivDisplayType.java,v 1.4 2006-05-09 01:33:33 ddonn Exp $
$Date: 2006-05-09 01:33:33 $
 
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
package edu.internet2.middleware.signet.ui;

import edu.internet2.middleware.signet.resource.ResLoaderUI;

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
  	= new PrivDisplayType("current_received", ResLoaderUI.getString("PrivDisplayType.current_received.txt"));

  /**
   * The instance that indicates a display of currently-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType CURRENT_GRANTED
  	= new PrivDisplayType
  			("current_granted", ResLoaderUI.getString("PrivDisplayType.current_granted.txt"));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies received by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_RECEIVED
    = new PrivDisplayType("former_received", ResLoaderUI.getString("PrivDisplayType.former_received.txt"));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_GRANTED
    = new PrivDisplayType
        ("former_granted", ResLoaderUI.getString("PrivDisplayType.former_granted.txt"));
}
