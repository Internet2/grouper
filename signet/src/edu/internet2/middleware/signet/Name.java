/*--
$Id: Name.java,v 1.4 2006-02-09 10:22:18 lmcrae Exp $
$Date: 2006-02-09 10:22:18 $

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
* This interface specifies some common methods that every 
* named Signet entity must provide.
* 
*/
interface Name
{
  /**
   * Gets the descriptive name of this entity.
   * 
   * @return Returns a descriptive name which will appear in UIs and
   * 		documents exposed to users.
   */
  public String getName();
}
