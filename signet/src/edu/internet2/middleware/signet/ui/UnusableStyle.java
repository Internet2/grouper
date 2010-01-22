/*--
$Id: UnusableStyle.java,v 1.2 2006-02-09 10:34:15 lmcrae Exp $
$Date: 2006-02-09 10:34:15 $
 
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
