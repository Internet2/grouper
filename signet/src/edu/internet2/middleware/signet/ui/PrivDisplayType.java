/*--
$Id: PrivDisplayType.java,v 1.6 2007-08-07 23:26:18 ddonn Exp $
$Date: 2007-08-07 23:26:18 $
 
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
	public static final String PrivNameCurrRcvd_key = "PrivDisplayType.current_received.txt";
	public static final String PrivNameCurrGrnt_key = "PrivDisplayType.current_granted.txt";
	public static final String PrivNameFrmrRcvd_key = "PrivDisplayType.former_received.txt";
	public static final String PrivNameFrmrGrnt_key = "PrivDisplayType.former_granted.txt";
	public static final String PrivNameDefaultValue_key = "PrivDisplayType.default_value";


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
  public static final PrivDisplayType CURRENT_RECEIVED =
		new PrivDisplayType(PrivNameCurrRcvd_key,
				ResLoaderUI.getString(PrivNameCurrRcvd_key));

  /**
   * The instance that indicates a display of currently-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType CURRENT_GRANTED =
		new PrivDisplayType(PrivNameCurrGrnt_key,
				ResLoaderUI.getString(PrivNameCurrGrnt_key));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies received by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_RECEIVED =
		new PrivDisplayType(PrivNameFrmrRcvd_key,
				ResLoaderUI.getString(PrivNameFrmrRcvd_key));

  /**
   * The instance that indicates a display of no-longer-active Assignments and
   * Proxies granted by a specific PrivilegedSubject.
   */
  public static final PrivDisplayType FORMER_GRANTED =
		new PrivDisplayType(PrivNameFrmrGrnt_key,
				ResLoaderUI.getString(PrivNameFrmrGrnt_key));
}
