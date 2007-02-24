/*--
$Id: PrivDisplayType.java,v 1.5 2007-02-24 02:11:32 ddonn Exp $
$Date: 2007-02-24 02:11:32 $
 
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
public class PrivDisplayType extends TypeSafeEnumeration
{
	public static final String PrivNameCurrRcvd = "current_received";
	public static final String PrivNameCurrGrnt = "current_granted";
	public static final String PrivNameFrmrRcvd = "former_received";
	public static final String PrivNameFrmrGrnt = "former_granted";

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
  	= new PrivDisplayType(PrivNameCurrRcvd, ResLoaderUI.getString("PrivDisplayType.current_received.txt"));

  /**
   * The instance that indicates a display of currently-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType CURRENT_GRANTED
  	= new PrivDisplayType
  			(PrivNameCurrGrnt, ResLoaderUI.getString("PrivDisplayType.current_granted.txt"));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies received by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_RECEIVED
    = new PrivDisplayType(PrivNameFrmrRcvd, ResLoaderUI.getString("PrivDisplayType.former_received.txt"));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_GRANTED
    = new PrivDisplayType
        (PrivNameFrmrGrnt, ResLoaderUI.getString("PrivDisplayType.former_granted.txt"));
}
