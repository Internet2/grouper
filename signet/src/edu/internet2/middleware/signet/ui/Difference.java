/*--
$Id: Difference.java,v 1.2 2006-02-09 10:31:19 lmcrae Exp $
$Date: 2006-02-09 10:31:19 $
 
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
 * This is a typesafe enumeration that identifies the various types of changes
 * that a Grantable object can undergo.
 *  
 */
public class Difference
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   */
  private Difference()
  {
    super();
  }

  /**
   * The instance that represents a new grant.
   */
  public static final Difference GRANT
  	= new Difference();

  /**
   * The instance that represents a revocation.
   */
  public static final Difference REVOKE
    = new Difference();

  /**
   * The instance that represents some other change.
   */
  public static final Difference MODIFY
    = new Difference();
}
